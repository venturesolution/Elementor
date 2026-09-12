package fasofts.element

import Logger
import Logger.LOG_TAG_SCHEDULER
import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.StrictMode
import android.os.Build
import android.util.Log
import fasofts.element.scheduler.ScheduleManager
import fasofts.element.scheduler.WorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.ext.android.get
import org.koin.core.context.startKoin
import org.koin.core.context.loadKoinModules
import fasofts.element.security.TamperGuard
import fasofts.element.security.XmlSignatureVerifier
import java.security.MessageDigest
import java.security.cert.CertificateFactory

/*
 * Copyright 2020 Element Powered by @fernandoangeli and its authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class RethinkDnsApplication : Application() {
    companion object {
        var DEBUG: Boolean = false
    }

    /////
    private fun computeSha256Hex(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes).joinToString("") { "%02x".format(it) }
    }
    //////

    override fun onCreate() {
        super.onCreate()

        // Executa verificação de integridade dos XML protegidos
        val problems = XmlSignatureVerifier.verifyAllProtectedXmls(this)
        if (problems.isEmpty()) {
            Log.i("Security", "Protected XMLs OK")
        } else {
            for (p in problems) {
                Log.e("Security", p)
            }
            // Se desejar falhar a inicialização em caso crítico, descomente abaixo:
            // throw RuntimeException("XML integrity check failed: ${problems.joinToString("; ")}")
        }

        // Validação de adulteração/assinatura + existência das classes exigidas
        TamperGuard.validateOrDie(this)

        DEBUG = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE ==
            ApplicationInfo.FLAG_DEBUGGABLE

        startKoin {
            if (DEBUG) androidLogger()
            androidContext(this@RethinkDnsApplication)
            // Carrega módulos da sua app. Mantenha AppModules conforme seu projeto.
            loadKoinModules(AppModules)
        }

        turnOnStrictMode()

        CoroutineScope(SupervisorJob()).launch {
            scheduleJobs()
        }

        // Verificação de fingerprint do certificado vs BuildConfig.EXPECTED_CERT_SHA256 (se presente)
        try {
            val pm = packageManager
            val pkg = packageName
            val expected: String = try {
                val field = Class.forName("$pkg.BuildConfig").getField("EXPECTED_CERT_SHA256")
                (field.get(null) as? String) ?: ""
            } catch (e: Exception) {
                ""
            }

            val certShaHex: String = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val pi = pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES)
                    val signingInfo = pi.signingInfo
                    val sigs = signingInfo?.apkContentsSigners ?: signingInfo?.signingCertificateHistory
                    if (sigs != null && sigs.isNotEmpty()) {
                        val certBytes = sigs[0].toByteArray()
                        val cf = CertificateFactory.getInstance("X.509")
                        val cert = cf.generateCertificate(certBytes.inputStream())
                        computeSha256Hex(cert.encoded)
                    } else ""
                } else {
                    @Suppress("DEPRECATION")
                    val pi = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES)
                    val sigs = pi.signatures
                    if (sigs != null && sigs.isNotEmpty()) {
                        val certBytes = sigs[0].toByteArray()
                        val cf = CertificateFactory.getInstance("X.509")
                        val cert = cf.generateCertificate(certBytes.inputStream())
                        computeSha256Hex(cert.encoded)
                    } else ""
                }
            } catch (e: Exception) {
                Log.e("Security", "Erro ao extrair certificado instalado: ${e.message}")
                ""
            }

            if (expected.isNotEmpty()) {
                if (!expected.equals(certShaHex, ignoreCase = true)) {
                    Log.e("Security", "CERT SHA mismatch. Esperado=${expected}  Instalado=${certShaHex}")
                    // Ação recomendada: trate como possível resigning. Pode lançar exceção se quiser bloquear.
                    // Exemplo: throw RuntimeException("Certificate fingerprint mismatch")
                } else {
                    Log.i("Security", "Certificate fingerprint match with BuildConfig.EXPECTED_CERT_SHA256")
                }
            } else {
                Log.i("Security", "BuildConfig.EXPECTED_CERT_SHA256 não definido. Ignorando verificação de fingerprint.")
            }
        } catch (e: Exception) {
            Log.e("Security", "Erro ao validar fingerprint do certificado: ${e.message}")
        }
    } // finish onCreate

    private suspend fun scheduleJobs() {
        Logger.d(LOG_TAG_SCHEDULER, "Schedule job")
        get<WorkScheduler>().scheduleAppExitInfoCollectionJob()
        get<ScheduleManager>().scheduleDatabaseRefreshJob()
        get<WorkScheduler>().scheduleDataUsageJob()
        get<WorkScheduler>().schedulePurgeConnectionsLog()
    }

    private fun turnOnStrictMode() {
        if (!DEBUG) return
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .permitDiskReads()
                .permitDiskWrites()
                .permitNetwork()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .build()
        )
    }
}