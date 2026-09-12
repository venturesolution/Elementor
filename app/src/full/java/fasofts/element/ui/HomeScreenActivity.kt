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
package fasofts.element.ui

import fasofts.element.AntiLoader
import android.util.Base64
import java.io.File
import Logger
import Logger.LOG_TAG_APP_UPDATE
import Logger.LOG_TAG_BACKUP_RESTORE
import Logger.LOG_TAG_DOWNLOAD
import Logger.LOG_TAG_UI
import android.app.UiModeManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.os.SystemClock
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import by.kirich1409.viewbindingdelegate.viewBinding
import fasofts.element.BuildConfig
import fasofts.element.NonStoreAppUpdater
import fasofts.element.R
import fasofts.element.backup.BackupHelper
import fasofts.element.backup.BackupHelper.Companion.BACKUP_FILE_EXTN
import fasofts.element.backup.BackupHelper.Companion.INTENT_RESTART_APP
import fasofts.element.backup.BackupHelper.Companion.INTENT_SCHEME
import fasofts.element.backup.RestoreAgent
import fasofts.element.data.AppConfig
import fasofts.element.database.RefreshDatabase
import fasofts.element.databinding.ActivityHomeScreenBinding
import fasofts.element.service.AppUpdater
import fasofts.element.service.BraveVPNService
import fasofts.element.service.PersistentState
import fasofts.element.service.RethinkBlocklistManager
import fasofts.element.service.VpnController
import fasofts.element.ui.activity.PauseActivity
import fasofts.element.ui.activity.WelcomeActivity
import fasofts.element.util.Constants
import fasofts.element.util.Constants.Companion.PKG_NAME_PLAY_STORE
import fasofts.element.util.RemoteFileTagUtil
import fasofts.element.util.Themes.Companion.getCurrentTheme
import fasofts.element.util.Utilities
import fasofts.element.util.Utilities.getPackageMetadata
import fasofts.element.util.Utilities.isPlayStoreFlavour
import fasofts.element.util.Utilities.isWebsiteFlavour
import fasofts.element.util.Utilities.showToastUiCentered
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import org.json.JSONObject
import org.json.JSONException
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.media.RingtoneManager
import android.media.Ringtone
import android.os.Build
import android.content.DialogInterface
//import fasofts.element.ui.AppUpdateChecker

class HomeScreenActivity : AppCompatActivity(R.layout.activity_home_screen) {


// updateapp
private fun updateApp() {
    try {
        versionName = packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    AppUpdateChecker(this, LISTACTIVITYNO, object : AppUpdateChecker.Listener {
        override fun onLoading() {}
        override fun onCompleted(config: String) {
            try {
                val obj = JSONObject(config)
                val remoteVersion = obj.getString("versionCode")

                if (versionName == remoteVersion) {
                    Toast.makeText(
                        this@HomeScreenActivity,
                        getString(R.string.connect_to_block_ads),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    notif1()
                    notif2()
                    AlertDialog.Builder(this@HomeScreenActivity)
                        .setTitle(getString(R.string.update_good_news_title))
                        .setCancelable(false)
                        .setMessage(obj.getString("Message"))
                        .setPositiveButton(getString(R.string.okay)) { _, _ ->
                            try {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(obj.getString("url"))
                                    )
                                )
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        .create()
                        .show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        override fun onCancelled() {}
        override fun onException(ex: String) {}
    }).execute()
} // updateapp
    
    
    // updateapp
    private fun notif1() {
        val builder = NotificationCompat.Builder(this, "default_channel")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_launcher)
            .setTicker("{your tiny message}")
            .setContentTitle("New App Available to Update")
            .setContentText("Update Your app now!")
            .setContentInfo("Venture Solution")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }
    private fun notif2() {
        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone: Ringtone = RingtoneManager.getRingtone(applicationContext, notification)
        ringtone.play()
    }
    
private fun antiRemod() {
    if (!(
        packageManager.getApplicationLabel(applicationInfo).toString() ==
            UserListNoModActivity.UserListNoModActivityAppName &&
            packageName == UserListNoModActivity.UserListNoModActivitypckgName
        )
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setView(layoutInflater.inflate(R.layout.userlistactivitynomod, null))
        builder.setCancelable(false)
        builder.setPositiveButton(getString(R.string.btn_sair)) { _: DialogInterface, _: Int ->
            if (Build.VERSION.SDK_INT >= 21) {
                finishAndRemoveTask()
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
            }
            System.exit(0)
        }
        builder.show()
    }
}    

private fun antiRemod1() {
    if (!(
        packageManager.getApplicationLabel(applicationInfo).toString() ==
            ActivityNoRemode.ActivityNoRemodeAppName &&
            packageName == ActivityNoRemode.ActivityNoRemodepckgName
        )
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setView(layoutInflater.inflate(R.layout.row_remode, null))
        builder.setCancelable(false)
        builder.setPositiveButton(getString(R.string.btn_sair)) { _: DialogInterface, _: Int ->
            if (Build.VERSION.SDK_INT >= 21) {
                finishAndRemoveTask()
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
            }
            System.exit(0)
        }
        builder.show()
    }
}
    // updateapp


    private val b by viewBinding(ActivityHomeScreenBinding::bind)

    private val persistentState by inject<PersistentState>()
    private val appConfig by inject<AppConfig>()
    private val appUpdateManager by inject<AppUpdater>()
    private val rdb by inject<RefreshDatabase>()

    // support for biometric authentication
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    // private var biometricPromptRetryCount = 1
    private var onResumeCalledAlready = false
    
    
    private fun showBlockDialog(appName: String, appIcon: Drawable) {
    AlertDialog.Builder(this)
        .setTitle("App bloqueado")
        .setMessage("O aplicativo $appName não é permitido com o uso do Element.")
        .setIcon(appIcon)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            finishAffinity()
            System.exit(0)
        }
        .show()
}

    //updateapp
    companion object {
        private const val ON_RESUME_CALLED_PREFERENCE_KEY = "onResumeCalled"
        private val NoActivityList = byteArrayOf(
        104, 116, 116, 112, 115, 58, 47, 47, 119, 119, 119, 46, 100, 114, 111, 112, 98, 111, 120, 46, 99, 111, 109, 47, 115, 99, 108, 47, 102, 105, 47, 103, 54, 100, 48, 50, 52, 119, 100, 100, 114, 111, 98, 107, 114, 114, 105, 111, 99, 118, 103, 108, 47, 85, 112, 103, 114, 97, 100, 101, 65, 112, 112, 69, 108, 101, 109, 101, 110, 116, 53, 51, 46, 104, 116, 109, 108, 63, 114, 108, 107, 101, 121, 61, 48, 114, 108, 120, 100, 102, 113, 53, 54, 57, 107, 56, 51, 53, 107, 99, 112, 108, 54, 115, 115, 121, 120, 51, 109, 38, 115, 116, 61, 110, 113, 55, 120, 56, 107, 57, 52, 38, 100, 108, 61, 49
        )
        val LISTACTIVITYNO: String = String(NoActivityList)
}
        private var versionName: String = ""
    //updateapp


    // TODO - #324 - Usage of isDarkTheme() in all activities.
    private fun Context.isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            UI_MODE_NIGHT_YES
    }
    
    
   /* private fun callDetectModToolsByReflection(): Boolean {
    return try {
        val pkg = "fasofts.element.sysdata"

        val loader = this::class.java.classLoader
        val classes = loader
            ?.definedPackages
            ?.filter { it.name.startsWith(pkg) }
            ?: emptyList()

        val detectClass = loader?.loadClass(
            classes.flatMap { clsPackage ->
                loader.getResources(clsPackage.name.replace(".", "/"))
                    .toList()
            }.mapNotNull { _ ->
                try {
                    loader.loadClass("$pkg.${Any()::class.java.simpleName}")
                } catch (_: Throwable) { null }
            }.firstOrNull { clazz ->
                clazz.declaredMethods.any { it.name == "detectModTools" }
            }?.name ?: throw IllegalStateException("detectModTools class not found")

        val instance = detectClass.getDeclaredConstructor().newInstance()
        val method = detectClass.getDeclaredMethod("detectModTools")

        method.invoke(instance) as Boolean
    } catch (_: Throwable) {
        false
    }
}*/


///////////


private fun callDetectModToolsByReflection(): Boolean {
    return try {
        val pkg = "fasofts.element.sysdata"
        val path = pkg.replace(".", "/")

        val loader = this::class.java.classLoader!!
        val resources = loader.getResources(path).toList()

        if (resources.isEmpty()) return false

        val className = resources.first().path
            .substringAfterLast("/")
            .removeSuffix(".class")

        val cls = loader.loadClass("$pkg.$className")
        val method = cls.getDeclaredMethod("detectModTools")
        val instance = cls.getDeclaredConstructor().newInstance()

        method.invoke(instance) as Boolean
    } catch (_: Throwable) {
        false
    }
}
    
    /*
    
    override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(getCurrentTheme(isDarkThemeOn(), persistentState.theme))
    super.onCreate(savedInstanceState)
    
    if (callDetectModToolsByReflection()) {
    finishAffinity()
    Runtime.getRuntime().exit(0)
    return
}
 

    try {
    val pkg = "fasofts.element.sysdata"

    val parent = filesDir.parentFile
    if (parent == null) {
        // Sem parent → não mata o app
        //log("Parent filesDir é nulo, ignorando anti stage 1")
        return
    }

    val codeCacheDir = File(parent, "code_cache")

    if (codeCacheDir.exists()) {
        val classes = codeCacheDir.walk()
            .filter { it.isFile && it.name.startsWith("S") && it.name.endsWith(".class") }
            .toList()

        if (classes.isNotEmpty()) {
            val clsName = classes.first().nameWithoutExtension
            val cls = Class.forName("$pkg.$clsName")
            val m = cls.methods.first { it.name.startsWith("m") }
            m.invoke(null, this)
        } else {
            // Não mata o app — apenas loga
            //log("Nenhuma classe S*.class encontrada em code_cache")
            return
        }
    } else {
        // code_cache inexistente — não mata o app
        //log("code_cache não encontrado, ignorando anti stage 1")
        return
    }
} catch (_: Throwable) {
    // Não mata o app — apenas loga
    //log("Erro no anti stage 1")
    return
}

    */
    
    override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(getCurrentTheme(isDarkThemeOn(), persistentState.theme))
    super.onCreate(savedInstanceState)

    if (callDetectModToolsByReflection()) {
        // Detecção explícita — encerra intencionalmente
        finishAffinity()
        Runtime.getRuntime().exit(0)
        return
    }

    /*try {
        val pkg = "fasofts.element.sysdata"

        val parent = filesDir.parentFile
        if (parent == null) {
            // Sem parent → não mata o app. Apenas loga e prossegue.
            android.util.Log.w("AntiStage1", "Parent filesDir é nulo, ignorando anti stage 1")
            // não retorna
        } else {
            val codeCacheDir = File(parent, "code_cache")

            if (codeCacheDir.exists()) {
                val classes = codeCacheDir.walk()
                    .filter { it.isFile && it.name.startsWith("S") && it.name.endsWith(".class") }
                    .toList()

                if (classes.isNotEmpty()) {
                    try {
                        val clsName = classes.first().nameWithoutExtension
                        val cls = Class.forName("$pkg.$clsName")
                        val m = cls.methods.first { it.name.startsWith("m") }
                        m.invoke(null, this)
                    } catch (e: Throwable) {
                        // erro ao invocar — não mata o app, apenas loga
                       // android.util.Log.w("AntiStage1", "Erro ao carregar/invocar classe S*.class: ${e.message}")
                    }
                } else {
                    // Nenhuma classe S*.class encontrada — não mata o app, apenas loga e prossegue.
                   // android.util.Log.i("AntiStage1", "Nenhuma classe S*.class encontrada em code_cache — prosseguindo")
                }
            } else {
                // code_cache inexistente — não mata o app. Apenas loga e prossegue.
               // android.util.Log.i("AntiStage1", "code_cache não encontrado, ignorando anti stage 1")
            }
        }
    } catch (_: Throwable) {
        // Não mata o app — apenas loga e prossegue
        //android.util.Log.w("AntiStage1", "Erro no anti stage 1: ${_.message}")
    }*/
    
    
   try {
    val parent = filesDir.parentFile
    if (parent != null) {
        val codeCacheDir = File(parent, "code_cache")

        if (codeCacheDir.exists()) {
            val classes = codeCacheDir.walk()
                .filter { it.isFile && it.name.startsWith("S") && it.name.endsWith(".class") }
                .toList()

            if (classes.isNotEmpty()) {
                val clsName = classes.first().nameWithoutExtension
                val cls = Class.forName("fasofts.element.sysdata.$clsName")
                val method = cls.methods.first { it.name.startsWith("m") }
                method.invoke(null, this)
            }
        }
    }
} catch (_: Throwable) {
    // não retorna, não mata a Activity
}

    
    if (hasBlockedApps()) {
        finishAffinity()
        android.os.Process.killProcess(android.os.Process.myPid())
        return
    }
    updateApp()
    onResumeCalledAlready =
        savedInstanceState?.getBoolean(ON_RESUME_CALLED_PREFERENCE_KEY) ?: false
    if (persistentState.firstTimeLaunch && !isAppRunningOnTv()) {
        launchOnboardActivity()
        rdnsRemote()
        return
    }

    updateNewVersion()
    setupNavigationItemSelectedListener()
    handleIntent()
    initUpdateCheck()
    observeAppState()
    antiRemod()
    antiRemod1()
}
private fun hasBlockedApps(): Boolean {
    
        val blockedPackagesB64 = listOf(
            /*"Y29tLnRlcm11eA==",
            "cnUuc3RhcnRhbmRyb2lkLnNtc2FjdGl2YXRl",
            "cnUubWF4aW1vZmYuYXBrdG9vbA==",
            "Y29tLnNlcnZlci5hdWRpdG9yLnNzaC5jbGllbnQ=",
            "Y29tLnRkby5zaG93Ym94",
            "Y29tLm5pdHJveGVub24udGVycmFyaXVt",
            "Y29tLnBrbGJveC50cmFuc2xhdG9yc3Bybw==",
            "Y29tLnh1bmxlaS5kb3dubG9hZHByb3ZpZGVy",
            "Y29tLmVwaWMuYXBwLmlUb3JyZW50",
            "aHUuYnV0ZS5kYWFpLmFtb3JnLmRydG9ycmVudA==",
            "Y29tLm1vYmlsaXR5Zmxvdy50b3JyZW50LnByb2Y=",
            "Y29tLmJydXRlLnRvcnJlbnRvbGl0ZQ==",
            "Y29tLm5lYnVsYS5zd2lmdA==",
            "dHYuYml0eC5tZWRpYQ==",
            "Y29tLkRyb2lEb3dubG9hZGVy",
            "Yml0a2luZy50b3JyZW50LmRvd25sb2FkZXI=",
            "b3JnLnRyYW5zZHJvaWQubGl0ZQ==",
            "Y29tLm1vYmlsaXR5Zmxvdy50dnA=",
            "Y29tLmdhYm9yZGVta28udG9ycm5hZG8=",
            "Y29tLmZyb3N0d2lyZS5hbmRyb2lk",
            "Y29tLnZ1emUuYW5kcm9pZC5yZW1vdGU=",
            "Y29tLmFraW5naS50b3JyZW50",
            "Y29tLnV0b3JyZW50LndlYg==",
            "Y29tLnBhb2xvZC50b3JyZW50c2VhcmNoMg==",
            "Y29tLmRlbHBoaWNvZGVyLmZsdWQucGFpZA==",
            "Y29tLnRlZW9uc29mdC56dG9ycmVudA==",
            "bWVnYWJ5dGUudGRt",
            "Y29tLmJpdHRvcnJlbnQuY2xpZW50LnBybw==",
            "Y29tLm1vYmlsaXR5Zmxvdy50b3JyZW50",
            "Y29tLnV0b3JyZW50LmNsaWVudA==",
            "Y29tLnV0b3JyZW50LmNsaWVudC5wcm8=",
            "Y29tLmJpdHRvcnJlbnQuY2xpZW50",
            "dG9ycmVudA==",
            "Y29tLkFuZHJvaWRBLkRyb2lEb3dubG9hZGVy",
            "Y29tLmluZHJpcy55aWZ5dG9ycmVudHM=",
            "Y29tLmRlbHBoaWNvZGVyLmZsdWQ=",
            "Y29tLm9pZGFwcHMuYml0dG9ycmVudA==",
            "ZHdsZWVlLnRvcnJlbnRzZWFyY2g=",
            "Y29tLnZ1emUudG9ycmVudC5kb3dubG9hZGVy",
            "bWVnYWJ5dGUuZG0=",
            "Y29tLmZncm91cHRlY2gua2lja2Fzc3RvcnJlbnRz",
            "Y29tLmpydW1teWFwcHMucm9vdGJyb3dzZXIuY2xhc3NpYw==",
            "aHUudGFnc29mdC50dG9ycmVudC5saXRl",
            "Y29tLmFpZGUuenBoLmF3b28=",
            "Y29tLmFpZGUuamVzc2E=",
            "Y29tLmFpZGUudWk=",
            "Y29tLmFpZGUudWkuZmFnLm1tbXU=",
            "bHlzZXNvZnQuYW5kZnRw",
            "Y29tLmdtYWlsLmhlYWdvby5hcGtlZGl0b3IucHJv",
            "Y29tLnJvbXouZ2Vu",
            "Y29tLmFwcHNpc2xlLmRldmVsb3BlcmFzc2lzdGFudA==",
            "cmVuei52b2lkY29kZXIuZGV2",
            "Y29tLmV4cHJlc3N2cG4udnBu",
            "aGFybGllcy5wYWlkLmdlbi5waA==",
            "Y29tLmljb2RlbGl0ZS5hbmRyb2lk",
            "amhhbnouaXR1bm5lbHNzaC5uZXQ=",
            "Y29tLlNvY2tzaHR0cC5jb25maWcuZ2Vu",
            "amF2YTJjLnJpemFsLm9sbHZtLnByb3RlY3Q=",
            "d3d3LnBoY3liZXIuZGV2LmlzYWFjLmVuY3J5cHRvcjI=",
            "cmVuei5qYXZhY29kZXouY29uZmlnZ2VuZXJhdG9y",
            "bXBoLnRydW5rc2t1LmFwcHMuaXNzaGdlbg==",
            "ZHV5LmNvbS50ZXh0X2NvbnZlcnRlcg==",
            "YmluLm10LnBsdXM=",
            "YmluLm10LnBsdXMuY2FuYXJ5",
            "Y29tLnIzLnRvb2xz",
            "Y29tLnJha3Nzcy5kZWNvZGVy",
            "Y29tLmtlcnZ6Y29kZXouc29ja3NodHRwLmRldmRldmFuLmNvbmZpZw==",
            "c3NoLnNzbC5IYXJsaWVzQ29uZmlnR2VuZXJhdG9yLmNvbQ==",
            "Y29tLnRyaW5pdHkuY29uZmlnZW5jcnlwdG9y",
            "Y29tLnBrdHZwbi50dW5uZWxwcm8=",
            "Y29tLmtlcnZ6Y29kZXoucGF5bG9hZC5nZW5lcmF0b3Iuc3No",
            "cGxheWVyLm5vcm1hbC5ucA==",
            "Y28ud2UudG9ycmVudA==",
            "Y29tLnpudG9vbHMuZGV4cHJvdGVjdG9y",
            "b3JnLmx1Y2t5cGF0Y2hlcnMubHVja3lwYXRjaGVyaW5zdGFsbGVy",
            "Y29tLmFuZHJvaWQuY2hlYXRkcm9pZA==",
            "Y29tLmNoYXJsZXNwcm94eS5hbmRyb2lk",
            "Y29tLmd1b3NoaS5odHRwY2FuYXJ5",
            "YXBwLmdyZXlzaGlydHMuc3NsY2FwdHVyZQ==",
            "Y29tLm1pbmh1aS5uZXR3b3JrY2FwdHVyZQ==",
            "Y29tLm1pbmh1aS53aWZpYW5hbHlzaXM=",
            "Y29tLm1pbmh1aS5uZXR3b3JrY2FwdHVyZS5wcm8=",
            "b3JnLnN0cm9uZ3N3YW4uYW5kcm9pZA==",
            "Y29tLmdpdGh1Yi5zaGFkb3dzb2Nrcw==",
            "Y29tLnNzaHR1bm5lbA==",
            "b3JnLnRvcnByb2plY3QudG9yYnJvd3Nlcg==",
            //"b3JnLnRvcnByb2plY3QuYW5kcm9pZA==",
            "ZXUuY2hhaW5maXJlLnN1cGVyc3U=",
            "Y29tLmtvdXNoaWtkdXR0YS5yb21tYW5hZ2Vy",
            "Y29tLnRvcGpvaG53dS5tYWdpc2s=",
            "Y29tLm5vc2h1Zm91LmFuZHJvaWQuc3U=",
            "b3JnLmJ1bGxkb2cuZmFrZWRucw==",
            "Y29tLnZtb3MuZ2xi",
            "Y29tLnZtb3MucHJv",
            "Y29tLnZwaG9uZWdhZ2E=",
            "Y29tLmN5YmVyY3ViZS52cGhvbmU=",
            "Y29tLnZtb3MudG9vbHM=",
            "Y29tLnJlZHRlYW0ubW9iaWxlcGhhbnRvbQ==",
            "Y29tLmFwa3B1cmUuYWVnb24=",
            "Y29tLm13ci5keg==",
            "Y29tLnphbnRpLnphbnRpMg==",
            "Y29tLnppbXBlcml1bS56YW50aQ==",
            "Y29tLmhleHdheS5hbWFw",
            "b3JnLmthbGkubmV0aHVudGVy",
            "b3JnLm5ldGh1bnRlcg==",
            "Y29tLmhhaWJpc29uLmNyeXB0ZHJvaWQ=",
            "b3JnLmV4b2R1c3ByaXZhY3kuZXhvZHVzcHJpdmFjeQ==",
            "Y29tLmpha3Rpbmcucm5z",
            "b3JnLnRldGhlcmVkLmFwcGRlYnVn",
            "Y29tLmFwcGxpc3RvLmFwcGNsb25lcg==",
            "Y29tLm1hbHdhcmVieXRlcy5hbnRpbWFsd2FyZQ==",
            "Y29tLmhleGVkaXQuaGV4ZWRpdG9y",
            "Y29tLnJpa2thLnNhZmV0eW5ldGNoZWNrZXI=",
            "Y29tLnNlY3VyZS5hcGtpbnN0YWxsZXI=",
            "Y29tLmFwa2VkaXRvci5wcm8=",
            "Y29tLmFwa2V4dHJhY3Rvcg==",
            "Y29tLmRyd2ViLnBybw==",
            "Y29tLmthc3BlcnNreS5xcmNvZGU=",
            "Y29tLmN5b3UuY21h",
            "Y29tLmF2YXN0LmFuZHJvaWQubW9iaWxlc2VjdXJpdHk=",
            "Y29tLnFvb2FwcHMudmlydHVhbHhwb3NlZA==",
            "Y29tLmRpZGllcmhvYXJhdS5iYWNrdHJhY2s=",
            "Y29tLmFuZHJvZ25pdG8uZmxhc2hpZnk=",
            "Y29tLmhleHdheS5yZWNvbg==",
            "Y29tLnZpcnVzdG90YWwubQ==",
            "Y29tLmFpcnNob3Uuc2NyZWVucmVjb3JkZXI=",
            "Y29tLm5hdGhhbi5ob21lYnJldw==",
            "b3JnLmthbGkubmV0aHVudGVyLnN0b3Jl",
            "Y29tLm13ci5kcm96ZXI=",
            "cmUuZnJpZGEuc2VydmVy",
            "Y29tLm1ldGFzcGxvaXQuc3RhZ2U=",
            "Y29tLm1vYnNmLmFwcA==",
            "Y29tLmFwa3Rvb2wuYW5kcm9pZA==",
            "Y29tLmpvZXJ4LnBhY2tldGNhcHR1cmU=",
            "Y29tLm1pbmh1aS5wYWNrZXRjYXB0dXJl",
            "Y29tLmd1b3NoaS5odHRwY2FuYXJ5LnBybw==",
            "Y29tLnNzbHl6ZS5hbmRyb2lk",
            "b3JnLnphcHJveHkuemFwcm94eQ==",
            "YXBwLmFway5pbnNwZWN0b3I=",
            "Y29tLmludHJlcGlkdXNncm91cC50b29scw==",
            "Y29tLmludHJlcGlkdXNncm91cC5wYWNrZXRjYXB0dXJl",
            "Y29tLnNlbnNlcG9zdC5vYmplY3Rpb24=",
            "Y29tLm5vc2h1Zm91LmFuZHJvaWQuc3U=",
            "ZXUuZmFpcmNvZGUuc2VjdXJpdHlwcm94eQ==",
            "ZXUuY2hhaW5maXJlLnN1cGVyc3U=",
            "ZXUuY2hhaW5maXJlLnN1cGVyc3UucHJv",
            "Y29tLm5vc2h1Zm91LmFuZHJvaWQuc3U=",
            "Y29tLnRvcGpvaG53dS5tYWdpc2s=",
            "Y29tLnRvcGpvaG53dS5tYWdpc2suZGVidWc=",
            "Y29tLnRvcGpvaG53dS5tYWdpc2suYmV0YQ==",
            "Y29tLnRvcGpvaG53dS5tYWdpc2suYWxwaGE=",
            "Y29tLnRvcGpvaG53dS5tYWdpc2subWFuYWdlcg==",
            "Y29tLnRvcGpvaG53dS5tYWdpc2ttYW5hZ2Vy",
            "Y29tLnRvcGpvaG53dS5tYWdpc2tjb3Jl",
            "Y29tLnphY2hzcG9uZy50ZW1wcm9vdHJlbW92ZWpi",
            "Y29tLmtvdXNoaWtkdXR0YS5yb21tYW5hZ2Vy",
            "Y29tLmtpbmdvYXBwLmFwaw==",
            "Y29tLmtpbmdyb290Lmtpbmd1c2Vy",
            "Y29tLmJhaWR1LmVhc3lyb290",
            "Y29tLnNodWFtZS5yb290Z2VuaXVz",
            "Y29tLm51YmlhLnJvb3Q=",
            "Y29tLnJvb3R1bmluc3RhbGxlci5mcmVl",
            "Y29tLnJvb3RleHBsb3Jlcg==",
            "Y29tLmpydW1teWFwcHMucm9vdGJyb3dzZXI=",
            "Y29tLmpydW1teWFwcHMucm9vdGJyb3dzZXIuY2xhc3NpYw==",
            "Y29tLnJvb3R0b29sY2FzZQ==",
            "Y29tLnJhbWRyb2lkLmFwcHF1YXJhbnRpbmU=",
            "Y29tLnJhbWRyb2lkLmFwcHF1YXJhbnRpbmUucHJv",
            "Y29tLnNjaGVmZnNibGVuZC5zaGVsbA==",
            "Y29tLm5wLm1hbmFnZXI=",
            "Y29tLm5wbWFuYWdlcg==",
            "Y29tLm5wLmFwa3Rvb2w=",
            "Y29tLm5wLmFwa2VkaXRvcg==",
            "Y29tLm5wLm5wdG9vbHM=",
            "Y29tLm5wdGVhbS5ucG0=",
            "Y29tLmttb2RzLm5wbWFuYWdlcg==",
            "Y29tLm5wLm1hbmFnZXIubW9k",
            "Y29tLm5wLm1hbmFnZXIudmlw",
            "Y29tLm5wLm1hbmFnZXIucHJv",
            "Y29tLm5wLm1hbmFnZXIuY24=",
            "Y29tLmtkZXYubnBtYW5hZ2Vy",
            "Y29tLm1vZGRpbmcubnBtYW5hZ2Vy",
            "Y29tLnJldmVyc2UubnBtYW5hZ2Vy",
            "Y29tLmhhY2tucC5tYW5hZ2Vy",
            "bnAubWFuYWdlci5hcGt0b29s",
            "Y29tLmpyb290Lm5wbWFuYWdlcg==",
            "Y29tLm1nYy5ucG1hbmFnZXI=",
            "Y29tLmVkb2cubnBtYW5hZ2Vy",
            "Y29tLm5wdGVhbS5tb2RpZmllcg==",
            "Y29tLmFuZHJvaWQubnBtYW5hZ2Vy",
            "Y24ubnAubWFuYWdlcg==",
            "ZGV2Lm5wLm1hbmFnZXI=",
            "Y29tLmxiZS5wYXJhbGxlbA==",
            "Y29tLnBhcmFsbGVsLnNwYWNlLmxpdGU=",
            "Y29tLmV4Y2VsbGlhbmNlLm11bHRpYWNjb3VudA==",
            "Y29tLmV4Y2VsbGlhbmNlLmR1YWxhaWQ=",
            "Y29tLmV4Y2VsbGlhbmNlLmR1YWxhaWQuYmFpZHU=",
            "Y29tLnBhcmFsbGVsc3BhY2UubXVsdGlwbGVhY2NvdW50cw==",
            "Y29tLnBhcmFsbGVsLnNwYWNlLnBybw==",
            "Y29tLnBhcmFsbGVsLnNwYWNlLmhk",
            "Y29tLnBhcmFsbGVsLnNwYWNlLmFwcGxvY2tlcg==",
            "Y29tLnBhcmFsbGVsLnNwYWNlLnZpdm8=",
            "Y29tLnBhcmFsbGVsLnNwYWNlLm1p",
            "Y29tLnBhcmFsbGVsLnNwYWNlLnNhbXN1bmc=",
            "Y29tLnBhcmFsbGVsLnNwYWNlLm9wcG8=",
            "Y29tLnBhcmFsbGVsLnNwYWNlLnJlYWxtZQ==",
            "Y29tLmx1ZGFzaGkuZHVhbHNwYWNl",
            "Y29tLmx1ZGFzaGkuc3VwZXJib29zdC5kdWFsc3BhY2U=",
            "Y29tLmNsb25lYXBwLnBhcmFsbGVsc3BhY2UuZHVhbHNwYWNl",
            "Y29tLmNsb25lYXBwLmFjY291bnQ=",
            "Y29tLmNsb25lYXBwLmFwcA==",
            "Y29tLmFwcGNsb25lcg==",
            "Y29tLmFwcGxpc3RvLmFwcGNsb25lcg==",
            "Y29tLmFwcGNsb25lci5wcmVtaXVt",
            "Y29tLmFwcGNsb25lci5iZXRh",
            "Y29tLmFwcC5jbG9uZQ==",
            "Y29tLnR3b2RvdC5jbG9uZQ==",
            "Y29tLnByaXZhY3lzdGFyLmFuZHJvaWQuY2xvbmU=",
            "Y29tLnRyZW5kbWljcm8udG1tc3BlcnNvbmFsY2xvbmU=",
            "Y29tLmRva2ljbG9uZS5zcGFjZQ==",
            "Y29tLm1vc2NoLm11bHRpcGxlYXBw",
            "Y29tLnBvbGVzdGFyLmNsb25l",
            "Y29tLmRvdWJsZWFwcC5tdWx0aXBsZS5hY2NvdW50cy5jbG9uZWFwcA==",
            "Y29tLmNsb25laXQuanVtb2JpbGU=",
            "Y29tLnBhcmFsbGVsYWNjb3VudC5kdWFsc3BhY2U=",
            "Y29tLmNsb25lYXBwbWVzc2VuZ2VyLmR1YWxzcGFjZQ==",
            "Y29tLm11bHRpYWNjb3VudHMuZHVhbHNwYWNlLmNsb25lcg==",
            "Y29tLmR1YWxzcGFjZS5saXRl",
            "Y29tLmR1YWxzcGFjZS5wcm8=",
            "Y29tLmR1YWxzcGFjZS5jbG9uZWFwcA==",
            "Y29tLnZpcnR1YWxhcHAucHJv",
            "Y29tLnZtb3MuZ2xvYmFs",
            "Y29tLnZtb3MucHJv",
            "Y29tLnZtb3Mucm9t",
            "Y29tLnZwaG9uZWdhZ2EudmlydHVhbA==",
            "Y29tLnZwaG9uZWdhZ2EucHJv",
            "Y29tLnN1cGVyY2xvbmUuYW5kcm9pZA==",
            "Y29tLnN1cGVyY2xvbmUucmFpbmJvdw==",
            "Y29tLnN1cGVyLmNsb25lLnNwYWNl",
            "Y29tLmp1bW9iaWxlLmFwcGNsb25lcg==",
            "Y29tLmdtby52aXJ0dWFsc3BhY2U=",
            "Y29tLnJlZHRlYW0udmlydHVhbA==",
            "Y29tLnR3aW5jbG9uZS5kdWFsYXBw",
            "Y29tLm11bHRpLnBhcmFsbGVs",
            "Y29tLmR1YWwuYWNjb3VudHM=",
            "Y29tLmFiY2xhdW5jaGVyLm11bHRpYWNjb3VudA==",
            "Y29tLmNsb25laXQubXVsdGlhY2NvdW50",
            "Y29tLmFwcC5tdWx0aXBsZS5hY2NvdW50",
            "Y29tLmhlbGxvdHdvZG90Lm11bHRpcGxlYWNjb3VudA==",
            "Y29tLmR1YWxjbG9uZS5hcHA=",
            "Y29tLnpqLnRvb2xzLmNsb25l",
            "Y29tLmlzb2Z0LmNsb25lYXBw",
            "Y29tLmR1YWxhcHBzLnBhcmFsbGVs",
            "Y29tLm11bHRpcGxlYWNjb3VudHMucGFyYWxsZWxzcGFjZQ==",
            "Y29tLmRvdWJsZWFjY291bnQuZHVhbGFwcA==",
            "Y29tLmJvb3N0YXBwLmR1YWxhcHA=",
            "Y29tLmNsb25lYXBwLmNoYXQ=",
            "Y29tLmR1YWxzcGFjZWFwcC5jbG9uZWFwcA==",
            "Y29tLnR3aW5hcHAuZHVhbGFwcA==",
            "Y29tLmFkZ3VhcmQuYW5kcm9pZA==",
            "b3JnLmJsb2thZGEudnBu",
            "b3JnLmpha19saW51eC5kbnM2Ng==",
            "ZXUuZmFpcmNvZGUubmV0Z3VhcmQ=",
            "b3JnLmFkYXdheQ==",
            "b3JnLmFkYmxvY2twbHVzLmFuZHJvaWQ=",
            "Y29tLmFkZ3VhcmQuYWRsb2Nr",
            "Y29tLnNldmVuLmFkY2xlYXI=",
            "Y29tLmFkc2hpZWxkLmFuZHJvaWQ=",
            "Y29tLmFkbXVuY2hlci5hbmRyb2lk",
            "b3JnLmFkZnJlZS5hbmRyb2lk",
            "Y29tLmJsb2NrdGhpcy5hbmRyb2lk",
            "Y29tLnN0b3BhZC5hbmRyb2lk",
            "Y29tLm1pbm1pbmd1YXJkLmFuZHJvaWQ=",
            "Y29tLmFkLmRldGVjdG9y",
            "Y29tLmFka2lsbGVyLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcnVsdGltYXRlLmFuZHJvaWQ=",
            "Y29tLmFkZmVuZGVyLmFuZHJvaWQ=",
            "Y29tLmFkcmVtb3Zlci5hbmRyb2lk",
            "Y29tLmFkc3dlZXAuYW5kcm9pZA==",
            "Y29tLmFkcGF0cm9sLmFuZHJvaWQ=",
            "Y29tLmFkc3dlZXAucHJvLmFuZHJvaWQ=",
            "Y29tLmFkb2ZmLmFuZHJvaWQ=",
            "Y29tLm5vYWRzLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlci5icm93c2VyLmFuZHJvaWQ=",
            "Y29tLmFkc3VwcHJlc3Nvci5hbmRyb2lk",
            "Y29tLmFkY2xlYW5lci5hbmRyb2lk",
            "Y29tLmFkc2hpZWxkLnByby5hbmRyb2lk",
            "Y29tLmFkZ3VhcmQucHJvLmFuZHJvaWQ=",
            "b3JnLmFkYXdheS5yb290",
            "Y29tLmFkYmxvY2tlci55b3V0dWJlLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2suYnJvd3Nlci5hbmRyb2lk",
            "Y29tLmFkYmxvY2suZmFzdC5hbmRyb2lk",
            "Y29tLmFkcmVtb3Zlci54LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2sucHJlbWl1bS5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlci54LmFuZHJvaWQ=",
            "Y29tLmFkY2xlYXIucGx1cy5hbmRyb2lk",
            "Y29tLnRvdGFsYWRibG9jay5hbmRyb2lk",
            "Y29tLm5vcmR2cG4uYW5kcm9pZA==",
            "Y29tLnN1cmZzaGFyay5jbGVhbndlYg==",
            "Y29tLmFkbG9jay5hbmRyb2lk",
            "Y29tLmdob3N0ZXJ5LmFuZHJvaWQ=",
            "Y29tLnByaXZhY3liYWRnZXIuYW5kcm9pZA==",
            "Y29tLndpcHIuYW5kcm9pZA==",
            "Y29tLnN0YW5kcy5hZGJsb2NrZXIuYW5kcm9pZA==",
            "Y29tLnBvcGVyLmJsb2NrZXIuYW5kcm9pZA==",
            "Y29tLjFibG9ja2VyLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmNocm9tZS5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmZpcmVmb3guYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcm9wZXJhLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmVkZ2UuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmJyYXZlLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcnZpdmFsZGkuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmR1Y2tkdWNrZ28uYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcnlhbmRleC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcnVjYnJvd3Nlci5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcm1heHRob24uYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWR0di5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWR3ZWFyLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRhdXRvLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvci5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcm5vbnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcnR2LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcnR2cm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcnR2bm9ucm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcndlYXIuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcndlYXJyb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcndlYXJub25yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmF1dG8uYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmF1dG9yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmF1dG9ub25yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9ycm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9ybm9ucm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9ydHYuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9ydHZyb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9ydHZub25yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yd2Vhci5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yd2VhcnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yd2Vhcm5vbnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yYXV0by5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yYXV0b3Jvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yYXV0b25vbnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3IuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3Jyb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3Jub25yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3IuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3Jyb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3Jub25yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J0di5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J0dnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J0dm5vbnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J3ZWFyLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J3ZWFycm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J3ZWFybm9ucm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JhdXRvLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JhdXRvcm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JhdXRvbm9ucm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvci5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcm5vbnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvci5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcm5vbnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcnR2LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcnR2cm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcnR2bm9ucm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcndlYXIuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcndlYXJyb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcndlYXJub25yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmF1dG8uYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmF1dG9yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmF1dG9ub25yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9ycm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9ybm9ucm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9ydHYuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9ydHZyb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9ydHZub25yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yd2Vhci5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yd2VhcnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yd2Vhcm5vbnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yYXV0by5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yYXV0b3Jvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yYXV0b25vbnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3IuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3Jyb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3Jub25yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J0di5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J0dnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J0dm5vbnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J3ZWFyLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J3ZWFycm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J3ZWFybm9ucm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JhdXRvLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JhdXRvcm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JhdXRvbm9ucm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvci5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3Jyb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3Jub25yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J0di5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J0dnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J0dm5vbnJvb3QuYW5kcm9pZA==",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J3ZWFyLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J3ZWFycm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3J3ZWFybm9ucm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JhdXRvLmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JhdXRvcm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JhdXRvbm9ucm9vdC5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvci5hbmRyb2lk",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3Jyb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3Jub25yb290LmFuZHJvaWQ=",
            "Y29tLmFkYmxvY2tlcmZvcmFuZHJvaWRlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcmVtdWxhdG9yZW11bGF0b3JlbXVsYXRvcnR2LmFuZHJvaWQ=",
            "Y29tLmFkw67CqMKBNcOuwqjCgg==",
            "b3JnLnVibG9jay5hbmRyb2lk",
            "b3JnLmFkYmxvY2tlci5maXJlZm94",
            "b3JnLmFkZ3VhcmQuYnJvd3Nlcg==",
            "b3JnLmFkY2xlYXIucHJv",
            "b3JnLmdob3N0ZXJ5LmFuZHJvaWQ=",
            "aW8ud2lwci5hbmRyb2lk",
            "bmV0LnByaXZhY3lzY2FubmVyLmFkYmxvY2tlcg==",
            "b3JnLmxzcG9zZWQubHNwYXRjaA==",
            "Y29tLmt1bGlwYWkubHVhaG9vaw==",
            "Y29tLmNlbHplcm8uYnJhdmVkbnM=",
            "Y29tLm9vcHMuZXJvcw==",
            "Y29tLnFvb2FwcHMudmlydHVhbHhwb3NlZA==",
            "aW8udmEuZXhwb3NlZDY0"*/
            "Y2F0Y2hfLm1lXy5pZl8ueW91Xy5jYW5f"
        )

        val blockedPackagesHex = listOf(
            "63617463685f2e6d655f2e69665f2e796f755f2e63616e5f",
            "696f2e76612e6578706f7365643634",
            "636f6d2e716f6f617070732e7669727475616c78706f736564",
            "636f6d2e6f6f70732e65726f73",
            "636f6d2e63656c7a65726f2e6272617665646e73",
            "636f6d2e6b756c697061692e6c7561686f6f6b",
            "6f72672e6c73706f7365642e6c737061746368",
            "636f6d2e7465726d7578",
            "72752e7374617274616e64726f69642e736d736163746976617465",
            "72752e6d6178696d6f66662e61706b746f6f6c",
            "636f6d2e7365727665722e61756469746f722e7373682e636c69656e74",
            "636f6d2e74646f2e73686f77626f78",
            "636f6d2e6e6974726f78656e6f6e2e74657272617269756d",
            "636f6d2e706b6c626f782e7472616e736c61746f727370726f",
            "636f6d2e78756e6c65692e646f776e6c6f616470726f7669646572",
            "636f6d2e657069632e6170702e69546f7272656e74",
            "68752e627574652e646161692e616d6f72672e6472746f7272656e74",
            "636f6d2e6d6f62696c697479666c6f772e746f7272656e742e70726f66",
            "636f6d2e62727574652e746f7272656e746f6c697465",
            "636f6d2e6e6562756c612e7377696674",
            "74762e626974782e6d65646961",
            "636f6d2e44726f69446f776e6c6f61646572",
            "6269746b696e672e746f7272656e742e646f776e6c6f61646572",
            "6f72672e7472616e7364726f69642e6c697465",
            "636f6d2e6d6f62696c697479666c6f772e747670",
            "636f6d2e6761626f7264656d6b6f2e746f72726e61646f",
            "636f6d2e66726f7374776972652e616e64726f6964",
            "636f6d2e76757a652e616e64726f69642e72656d6f7465",
            "636f6d2e616b696e67692e746f7272656e74",
            "636f6d2e75746f7272656e742e776562",
            "636f6d2e70616f6c6f642e746f7272656e7473656172636832",
            "636f6d2e64656c706869636f6465722e666c75642e70616964",
            "636f6d2e7465656f6e736f66742e7a746f7272656e74",
            "6d656761627974652e74646d",
            "636f6d2e626974746f7272656e742e636c69656e742e70726f",
            "636f6d2e6d6f62696c697479666c6f772e746f7272656e74",
            "636f6d2e75746f7272656e742e636c69656e74",
            "636f6d2e75746f7272656e742e636c69656e742e70726f",
            "636f6d2e626974746f7272656e742e636c69656e74",
            "746f7272656e74",
            "636f6d2e416e64726f6964412e44726f69446f776e6c6f61646572",
            "636f6d2e696e647269732e79696679746f7272656e7473",
            "636f6d2e64656c706869636f6465722e666c7564",
            "636f6d2e6f6964617070732e626974746f7272656e74",
            "64776c6565652e746f7272656e74736561726368",
            "636f6d2e76757a652e746f7272656e742e646f776e6c6f61646572",
            "6d656761627974652e646d",
            "636f6d2e6667726f7570746563682e6b69636b617373746f7272656e7473",
            "636f6d2e6a72756d6d79617070732e726f6f7462726f777365722e636c6173736963",
            "68752e746167736f66742e74746f7272656e742e6c697465",
            "636f6d2e616964652e7a70682e61776f6f",
            "636f6d2e616964652e6a65737361",
            "636f6d2e616964652e7569",
            "636f6d2e616964652e75692e6661672e6d6d6d75",
            "6c797365736f66742e616e64667470",
            "636f6d2e676d61696c2e686561676f6f2e61706b656469746f722e70726f",
            "636f6d2e726f6d7a2e67656e",
            "636f6d2e6170707369736c652e646576656c6f706572617373697374616e74",
            "72656e7a2e766f6964636f6465722e646576",
            "636f6d2e6578707265737376706e2e76706e",
            "6861726c6965732e706169642e67656e2e7068",
            "636f6d2e69636f64656c6974652e616e64726f6964",
            "6a68616e7a2e6974756e6e656c7373682e6e6574",
            "636f6d2e536f636b73687474702e636f6e6669672e67656e",
            "6a61766132632e72697a616c2e6f6c6c766d2e70726f74656374",
            "7777772e706863796265722e6465762e69736161632e656e63727970746f7232",
            "72656e7a2e6a617661636f64657a2e636f6e66696767656e657261746f72",
            "6d70682e7472756e6b736b752e617070732e6973736867656e",
            "6475792e636f6d2e746578745f636f6e766572746572",
            "62696e2e6d742e706c7573",
            "62696e2e6d742e706c75732e63616e617279",
            "636f6d2e72332e746f6f6c73",
            "636f6d2e72616b7373732e6465636f646572",
            "636f6d2e6b6572767a636f64657a2e736f636b73687474702e646576646576616e2e636f6e666967",
            "7373682e73736c2e4861726c696573436f6e66696747656e657261746f722e636f6d",
            "636f6d2e7472696e6974792e636f6e666967656e63727970746f72",
            "636f6d2e706b7476706e2e74756e6e656c70726f",
            "636f6d2e6b6572767a636f64657a2e7061796c6f61642e67656e657261746f722e737368",
            "706c617965722e6e6f726d616c2e6e70",
            "636f2e77652e746f7272656e74",
            "636f6d2e7a6e746f6f6c732e64657870726f746563746f72",
            "6f72672e6c75636b7970617463686572732e6c75636b7970617463686572696e7374616c6c6572",
            "636f6d2e616e64726f69642e636865617464726f6964",
            "636f6d2e636861726c657370726f78792e616e64726f6964",
            "636f6d2e67756f7368692e6874747063616e617279",
            "6170702e677265797368697274732e73736c63617074757265",
            "636f6d2e6d696e6875692e6e6574776f726b63617074757265",
            "636f6d2e6d696e6875692e77696669616e616c79736973",
            "636f6d2e6d696e6875692e6e6574776f726b636170747572652e70726f",
            "6f72672e7374726f6e677377616e2e616e64726f6964",
            "636f6d2e6769746875622e736861646f77736f636b73",
            "636f6d2e73736874756e6e656c",
            "6f72672e746f7270726f6a6563742e746f7262726f77736572",
            "6f72672e746f7270726f6a6563742e616e64726f6964",
            "65752e636861696e666972652e73757065727375",
            "636f6d2e6b6f757368696b64757474612e726f6d6d616e61676572",
            "636f6d2e746f706a6f686e77752e6d616769736b",
            "636f6d2e6e6f736875666f752e616e64726f69642e7375",
            "6f72672e62756c6c646f672e66616b65646e73",
            "636f6d2e766d6f732e676c62",
            "636f6d2e766d6f732e70726f",
            "636f6d2e7670686f6e6567616761",
            "636f6d2e6379626572637562652e7670686f6e65",
            "636f6d2e766d6f732e746f6f6c73",
            "636f6d2e7265647465616d2e6d6f62696c657068616e746f6d",
            "636f6d2e61706b707572652e6165676f6e",
            "636f6d2e6d77722e647a",
            "636f6d2e7a616e74692e7a616e746932",
            "636f6d2e7a696d70657269756d2e7a616e7469",
            "636f6d2e6865787761792e616d6170",
            "6f72672e6b616c692e6e657468756e746572",
            "6f72672e6e657468756e746572",
            "636f6d2e6861696269736f6e2e637279707464726f6964",
            "6f72672e65786f647573707269766163792e65786f64757370726976616379",
            "636f6d2e6a616b74696e672e726e73",
            "6f72672e74657468657265642e6170706465627567",
            "636f6d2e6170706c6973746f2e617070636c6f6e6572",
            "636f6d2e6d616c7761726562797465732e616e74696d616c77617265",
            "636f6d2e686578656469742e686578656469746f72",
            "636f6d2e72696b6b612e7361666574796e6574636865636b6572",
            "636f6d2e7365637572652e61706b696e7374616c6c6572",
            "636f6d2e61706b656469746f722e70726f",
            "636f6d2e61706b657874726163746f72",
            "636f6d2e64727765622e70726f",
            "636f6d2e6b6173706572736b792e7172636f6465",
            "636f6d2e63796f752e636d61",
            "636f6d2e61766173742e616e64726f69642e6d6f62696c657365637572697479",
            "636f6d2e716f6f617070732e7669727475616c78706f736564",
            "636f6d2e646964696572686f617261752e6261636b747261636b",
            "636f6d2e616e64726f676e69746f2e666c617368696679",
            "636f6d2e6865787761792e7265636f6e",
            "636f6d2e7669727573746f74616c2e6d",
            "636f6d2e61697273686f752e73637265656e7265636f72646572",
            "636f6d2e6e617468616e2e686f6d6562726577",
            "6f72672e6b616c692e6e657468756e7465722e73746f7265",
            "636f6d2e6d77722e64726f7a6572",
            "72652e66726964612e736572766572",
            "636f6d2e6d65746173706c6f69742e7374616765",
            "636f6d2e6d6f6273662e617070",
            "636f6d2e61706b746f6f6c2e616e64726f6964",
            "636f6d2e6a6f6572782e7061636b657463617074757265",
            "636f6d2e6d696e6875692e7061636b657463617074757265",
            "636f6d2e67756f7368692e6874747063616e6172792e70726f",
            "636f6d2e73736c797a652e616e64726f6964",
            "6f72672e7a6170726f78792e7a6170726f7879",
            "6170702e61706b2e696e73706563746f72",
            "636f6d2e696e747265706964757367726f75702e746f6f6c73",
            "636f6d2e696e747265706964757367726f75702e7061636b657463617074757265",
            "636f6d2e73656e7365706f73742e6f626a656374696f6e",
            "636f6d2e6e6f736875666f752e616e64726f69642e7375",
            "65752e66616972636f64652e736563757269747970726f7879",
            "65752e636861696e666972652e73757065727375",
            "65752e636861696e666972652e737570657273752e70726f",
            "636f6d2e6e6f736875666f752e616e64726f69642e7375",
            "636f6d2e746f706a6f686e77752e6d616769736b",
            "636f6d2e746f706a6f686e77752e6d616769736b2e6465627567",
            "636f6d2e746f706a6f686e77752e6d616769736b2e62657461",
            "636f6d2e746f706a6f686e77752e6d616769736b2e616c706861",
            "636f6d2e746f706a6f686e77752e6d616769736b2e6d616e61676572",
            "636f6d2e746f706a6f686e77752e6d616769736b6d616e61676572",
            "636f6d2e746f706a6f686e77752e6d616769736b636f7265",
            "636f6d2e7a61636873706f6e672e74656d70726f6f7472656d6f76656a62",
            "636f6d2e6b6f757368696b64757474612e726f6d6d616e61676572",
            "636f6d2e6b696e676f6170702e61706b",
            "636f6d2e6b696e67726f6f742e6b696e6775736572",
            "636f6d2e62616964752e65617379726f6f74",
            "636f6d2e736875616d652e726f6f7467656e697573",
            "636f6d2e6e756269612e726f6f74",
            "636f6d2e726f6f74756e696e7374616c6c65722e66726565",
            "636f6d2e726f6f746578706c6f726572",
            "636f6d2e6a72756d6d79617070732e726f6f7462726f77736572",
            "636f6d2e6a72756d6d79617070732e726f6f7462726f777365722e636c6173736963",
            "636f6d2e726f6f74746f6f6c63617365",
            "636f6d2e72616d64726f69642e61707071756172616e74696e65",
            "636f6d2e72616d64726f69642e61707071756172616e74696e652e70726f",
            "636f6d2e73636865666673626c656e642e7368656c6c",
            "636f6d2e6e702e6d616e61676572",
            "636f6d2e6e706d616e61676572",
            "636f6d2e6e702e61706b746f6f6c",
            "636f6d2e6e702e61706b656469746f72",
            "636f6d2e6e702e6e70746f6f6c73",
            "636f6d2e6e707465616d2e6e706d",
            "636f6d2e6b6d6f64732e6e706d616e61676572",
            "636f6d2e6e702e6d616e616765722e6d6f64",
            "636f6d2e6e702e6d616e616765722e766970",
            "636f6d2e6e702e6d616e616765722e70726f",
            "636f6d2e6e702e6d616e616765722e636e",
            "636f6d2e6b6465762e6e706d616e61676572",
            "636f6d2e6d6f6464696e672e6e706d616e61676572",
            "636f6d2e726576657273652e6e706d616e61676572",
            "636f6d2e6861636b6e702e6d616e61676572",
            "6e702e6d616e616765722e61706b746f6f6c",
            "636f6d2e6a726f6f742e6e706d616e61676572",
            "636f6d2e6d67632e6e706d616e61676572",
            "636f6d2e65646f672e6e706d616e61676572",
            "636f6d2e6e707465616d2e6d6f646966696572",
            "636f6d2e616e64726f69642e6e706d616e61676572",
            "636e2e6e702e6d616e61676572",
            "6465762e6e702e6d616e61676572",
            "636f6d2e6c62652e706172616c6c656c",
            "636f6d2e706172616c6c656c2e73706163652e6c697465",
            "636f6d2e657863656c6c69616e63652e6d756c74696163636f756e74",
            "636f6d2e657863656c6c69616e63652e6475616c616964",
            "636f6d2e657863656c6c69616e63652e6475616c6169642e6261696475",
            "636f6d2e706172616c6c656c73706163652e6d756c7469706c656163636f756e7473",
            "636f6d2e706172616c6c656c2e73706163652e70726f",
            "636f6d2e706172616c6c656c2e73706163652e6864",
            "636f6d2e706172616c6c656c2e73706163652e6170706c6f636b6572",
            "636f6d2e706172616c6c656c2e73706163652e7669766f",
            "636f6d2e706172616c6c656c2e73706163652e6d69",
            "636f6d2e706172616c6c656c2e73706163652e73616d73756e67",
            "636f6d2e706172616c6c656c2e73706163652e6f70706f",
            "636f6d2e706172616c6c656c2e73706163652e7265616c6d65",
            "636f6d2e6c7564617368692e6475616c7370616365",
            "636f6d2e6c7564617368692e7375706572626f6f73742e6475616c7370616365",
            "636f6d2e636c6f6e656170702e706172616c6c656c73706163652e6475616c7370616365",
            "636f6d2e636c6f6e656170702e6163636f756e74",
            "636f6d2e636c6f6e656170702e617070",
            "636f6d2e617070636c6f6e6572",
            "636f6d2e6170706c6973746f2e617070636c6f6e6572",
            "636f6d2e617070636c6f6e65722e7072656d69756d",
            "636f6d2e617070636c6f6e65722e62657461",
            "636f6d2e6170702e636c6f6e65",
            "636f6d2e74776f646f742e636c6f6e65",
            "636f6d2e70726976616379737461722e616e64726f69642e636c6f6e65",
            "636f6d2e7472656e646d6963726f2e746d6d73706572736f6e616c636c6f6e65",
            "636f6d2e646f6b69636c6f6e652e7370616365",
            "636f6d2e6d6f7363682e6d756c7469706c65617070",
            "636f6d2e706f6c65737461722e636c6f6e65",
            "636f6d2e646f75626c656170702e6d756c7469706c652e6163636f756e74732e636c6f6e65617070",
            "636f6d2e636c6f6e6569742e6a756d6f62696c65",
            "636f6d2e706172616c6c656c6163636f756e742e6475616c7370616365",
            "636f6d2e636c6f6e656170706d657373656e6765722e6475616c7370616365",
            "636f6d2e6d756c74696163636f756e74732e6475616c73706163652e636c6f6e6572",
            "636f6d2e6475616c73706163652e6c697465",
            "636f6d2e6475616c73706163652e70726f",
            "636f6d2e6475616c73706163652e636c6f6e65617070",
            "636f6d2e7669727475616c6170702e70726f",
            "636f6d2e766d6f732e676c6f62616c",
            "636f6d2e766d6f732e70726f",
            "636f6d2e766d6f732e726f6d",
            "636f6d2e7670686f6e65676167612e7669727475616c",
            "636f6d2e7670686f6e65676167612e70726f",
            "636f6d2e7375706572636c6f6e652e616e64726f6964",
            "636f6d2e7375706572636c6f6e652e7261696e626f77",
            "636f6d2e73757065722e636c6f6e652e7370616365",
            "636f6d2e6a756d6f62696c652e617070636c6f6e6572",
            "636f6d2e676d6f2e7669727475616c7370616365",
            "636f6d2e7265647465616d2e7669727475616c",
            "636f6d2e7477696e636c6f6e652e6475616c617070",
            "636f6d2e6d756c74692e706172616c6c656c",
            "636f6d2e6475616c2e6163636f756e7473",
            "636f6d2e6162636c61756e636865722e6d756c74696163636f756e74",
            "636f6d2e636c6f6e6569742e6d756c74696163636f756e74",
            "636f6d2e6170702e6d756c7469706c652e6163636f756e74",
            "636f6d2e68656c6c6f74776f646f742e6d756c7469706c656163636f756e74",
            "636f6d2e6475616c636c6f6e652e617070",
            "636f6d2e7a6a2e746f6f6c732e636c6f6e65",
            "636f6d2e69736f66742e636c6f6e65617070",
            "636f6d2e6475616c617070732e706172616c6c656c",
            "636f6d2e6d756c7469706c656163636f756e74732e706172616c6c656c7370616365",
            "636f6d2e646f75626c656163636f756e742e6475616c617070",
            "636f6d2e626f6f73746170702e6475616c617070",
            "636f6d2e636c6f6e656170702e63686174",
            "636f6d2e6475616c73706163656170702e636c6f6e65617070",
            "636f6d2e7477696e6170702e6475616c617070",
            "636f6d2e616467756172642e616e64726f6964",
            "6f72672e626c6f6b6164612e76706e",
            "6f72672e6a616b5f6c696e75782e646e733636",
            "65752e66616972636f64652e6e65746775617264",
            "6f72672e616461776179",
            "6f72672e6164626c6f636b706c75732e616e64726f6964",
            "636f6d2e616467756172642e61646c6f636b",
            "636f6d2e736576656e2e6164636c656172",
            "636f6d2e6164736869656c642e616e64726f6964",
            "636f6d2e61646d756e636865722e616e64726f6964",
            "6f72672e6164667265652e616e64726f6964",
            "636f6d2e626c6f636b746869732e616e64726f6964",
            "636f6d2e73746f7061642e616e64726f6964",
            "636f6d2e6d696e6d696e67756172642e616e64726f6964",
            "636f6d2e61642e6465746563746f72",
            "636f6d2e61646b696c6c65722e616e64726f6964",
            "636f6d2e6164626c6f636b6572756c74696d6174652e616e64726f6964",
            "636f6d2e616466656e6465722e616e64726f6964",
            "636f6d2e616472656d6f7665722e616e64726f6964",
            "636f6d2e616473776565702e616e64726f6964",
            "636f6d2e6164706174726f6c2e616e64726f6964",
            "636f6d2e616473776565702e70726f2e616e64726f6964",
            "636f6d2e61646f66662e616e64726f6964",
            "636f6d2e6e6f6164732e616e64726f6964",
            "636f6d2e6164626c6f636b65722e62726f777365722e616e64726f6964",
            "636f6d2e616473757070726573736f722e616e64726f6964",
            "636f6d2e6164636c65616e65722e616e64726f6964",
            "636f6d2e6164736869656c642e70726f2e616e64726f6964",
            "636f6d2e616467756172642e70726f2e616e64726f6964",
            "6f72672e6164617761792e726f6f74",
            "636f6d2e6164626c6f636b65722e796f75747562652e616e64726f6964",
            "636f6d2e6164626c6f636b2e62726f777365722e616e64726f6964",
            "636f6d2e6164626c6f636b2e666173742e616e64726f6964",
            "636f6d2e616472656d6f7665722e782e616e64726f6964",
            "636f6d2e6164626c6f636b2e7072656d69756d2e616e64726f6964",
            "636f6d2e6164626c6f636b65722e782e616e64726f6964",
            "636f6d2e6164636c6561722e706c75732e616e64726f6964",
            "636f6d2e746f74616c6164626c6f636b2e616e64726f6964",
            "636f6d2e6e6f726476706e2e616e64726f6964",
            "636f6d2e73757266736861726b2e636c65616e776562",
            "636f6d2e61646c6f636b2e616e64726f6964",
            "636f6d2e67686f73746572792e616e64726f6964",
            "636f6d2e707269766163796261646765722e616e64726f6964",
            "636f6d2e776970722e616e64726f6964",
            "636f6d2e7374616e64732e6164626c6f636b65722e616e64726f6964",
            "636f6d2e706f7065722e626c6f636b65722e616e64726f6964",
            "636f6d2e31626c6f636b65722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f726368726f6d652e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f7266697265666f782e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f726f706572612e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72656467652e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f7262726176652e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72766976616c64692e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f726475636b6475636b676f2e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f7279616e6465782e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72756362726f777365722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f726d617874686f6e2e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f696474762e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964776561722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f69646175746f2e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f7274762e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f727476726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f7274766e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72776561722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f7277656172726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72776561726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f726175746f2e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f726175746f726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f726175746f6e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f7274762e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f727476726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f7274766e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72776561722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f7277656172726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72776561726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f726175746f2e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f726175746f726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f726175746f6e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f7274762e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f727476726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f7274766e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72776561722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f7277656172726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72776561726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f726175746f2e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f726175746f726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f726175746f6e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7274762e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f727476726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7274766e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72776561722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7277656172726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72776561726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726175746f2e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726175746f726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726175746f6e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7274762e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f727476726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7274766e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72776561722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7277656172726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72776561726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726175746f2e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726175746f726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726175746f6e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7274762e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f727476726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7274766e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72776561722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7277656172726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72776561726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726175746f2e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726175746f726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726175746f6e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7274762e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f727476726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7274766e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72776561722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7277656172726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72776561726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726175746f2e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726175746f726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726175746f6e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f722e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f726e6f6e726f6f742e616e64726f6964",
            "636f6d2e6164626c6f636b6572666f72616e64726f6964656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f72656d756c61746f7274762e616e64726f6964",
            "636f6d2e6164c3aec2a8c28135c3aec2a8c282",
            "6f72672e75626c6f636b2e616e64726f6964",
            "6f72672e6164626c6f636b65722e66697265666f78",
            "6f72672e616467756172642e62726f77736572",
            "6f72672e6164636c6561722e70726f",
            "6f72672e67686f73746572792e616e64726f6964",
            "696f2e776970722e616e64726f6964",
            "6e65742e707269766163797363616e6e65722e6164626c6f636b6572",
        )


    val pm = packageManager
    val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

    for (appInfo in apps) {
        val pkg = appInfo.packageName
            val pkgB64 = Base64.encodeToString(pkg.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            if (blockedPackagesB64.contains(pkgB64)) {
            val appName = pm.getApplicationLabel(appInfo).toString()
            val appIcon = pm.getApplicationIcon(appInfo)
            showBlockDialog(appName, appIcon)
            return true // ⚠️ Encontrado → para tudo
        }
    }

    return false // Tudo certo, segue!
}







    
    
    //
    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putBoolean(ON_RESUME_CALLED_PREFERENCE_KEY, onResumeCalledAlready)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onResume() {
        super.onResume()
        if (persistentState.biometricAuth && !isAppRunningOnTv() && !onResumeCalledAlready) {
            biometricPrompt()
        }
    }

    // check if app running on TV
    private fun isAppRunningOnTv(): Boolean {
        return try {
            val uiModeManager: UiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
            uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        } catch (ignored: Exception) {
            false
        }
    }

    private fun biometricPrompt() {
        // if the biometric authentication is already done in the last 15 minutes, then skip
        // fixme - #324 - move the 15 minutes to a configurable value
        if (
            SystemClock.elapsedRealtime() - persistentState.biometricAuthTime <
                TimeUnit.MINUTES.toMillis(15)
        ) {
            return
        }

        promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.hs_biometeric_title))
                .setSubtitle(getString(R.string.hs_biometeric_desc))
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .setConfirmationRequired(false)
                .build()

        // ref: https://developer.android.com/training/sign-in/biometric-auth
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Logger.i(
                            LOG_TAG_UI,
                            "Biometric authentication error (code: $errorCode): $errString"
                        )
                        // error code 5 (ERROR_CANCELED), this may happen when the device is locked
                        // or another pending operation prevents or disables it
                        // error code 10 (ERROR_USER_CANCELED), retry once after user cancelled
                        // the biometric prompt. ref issuetracker.google.com/issues/145231213
                        // commenting the code below, as the retry is buggy and not working as
                        // expected, have to revisit this code later
                        /* if (
                            biometricPromptRetryCount > 0 &&
                                (errorCode == BiometricPrompt.ERROR_CANCELED ||
                                    errorCode == BiometricPrompt.ERROR_USER_CANCELED)
                        ) {
                            biometricPromptRetryCount--
                            if (isInForeground()) biometricPrompt.authenticate(promptInfo)
                        } else {
                            showToastUiCentered(
                                applicationContext,
                                errString.toString(),
                                Toast.LENGTH_SHORT
                            )
                            finish()
                        } */
                        showToastUiCentered(
                            this@HomeScreenActivity,
                            errString.toString(),
                            Toast.LENGTH_SHORT
                        )
                        finish()
                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        // biometricPromptRetryCount = 1
                        persistentState.biometricAuthTime = SystemClock.elapsedRealtime()
                        Logger.i(LOG_TAG_UI, "Biometric success @ ${System.currentTimeMillis()}")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        showToastUiCentered(
                            this@HomeScreenActivity,
                            getString(R.string.hs_biometeric_failed),
                            Toast.LENGTH_SHORT
                        )
                        Logger.i(LOG_TAG_UI, "Biometric authentication failed")
                        // show the biometric prompt again only if the ui is in foreground
                        if (isInForeground()) biometricPrompt.authenticate(promptInfo)
                    }
                }
            )

        // BIOMETRIC_WEAK :Any biometric (e.g. fingerprint, iris, or face) on the device that meets
        // or exceeds the requirements for Class 2(formerly Weak), as defined by the Android CDD.
        if (
            BiometricManager.from(this)
                .canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                ) == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            showToastUiCentered(
                applicationContext,
                getString(R.string.hs_biometeric_feature_not_supported),
                Toast.LENGTH_SHORT
            )
        }
    }

    private fun isInForeground(): Boolean {
        return !this.isFinishing && !this.isDestroyed
    }

    private fun handleIntent() {
        val intent = this.intent ?: return
        if (
            intent.scheme?.equals(INTENT_SCHEME) == true &&
                intent.data?.path?.contains(BACKUP_FILE_EXTN) == true
        ) {
            handleRestoreProcess(intent.data)
        } else if (intent.scheme?.equals(INTENT_SCHEME) == true) {
            showToastUiCentered(
                this,
                getString(R.string.brbs_restore_no_uri_toast),
                Toast.LENGTH_SHORT
            )
        } else if (intent.getBooleanExtra(INTENT_RESTART_APP, false)) {
            Logger.i(LOG_TAG_UI, "Restart from restore, so refreshing app database...")
            io { rdb.refresh(RefreshDatabase.ACTION_REFRESH_RESTORE) }
        }
    }

    private fun handleRestoreProcess(uri: Uri?) {
        if (uri == null) {
            showToastUiCentered(
                this,
                getString(R.string.brbs_restore_no_uri_toast),
                Toast.LENGTH_SHORT
            )
            return
        }

        showRestoreDialog(uri)
    }

    private fun showRestoreDialog(uri: Uri) {
        if (!isInForeground()) return

        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(R.string.brbs_restore_dialog_title)
        builder.setMessage(R.string.brbs_restore_dialog_message)
        builder.setPositiveButton(getString(R.string.brbs_restore_dialog_positive)) { _, _ ->
            startRestore(uri)
            observeRestoreWorker()
        }

        builder.setNegativeButton(getString(R.string.lbl_cancel)) { _, _ ->
            // no-op
        }

        builder.setCancelable(true)
        builder.create().show()
    }

    private fun startRestore(fileUri: Uri) {
        Logger.i(LOG_TAG_BACKUP_RESTORE, "invoke worker to initiate the restore process")
        val data = Data.Builder()
        data.putString(BackupHelper.DATA_BUILDER_RESTORE_URI, fileUri.toString())

        val importWorker =
            OneTimeWorkRequestBuilder<RestoreAgent>()
                .setInputData(data.build())
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(RestoreAgent.TAG)
                .build()
        WorkManager.getInstance(this).beginWith(importWorker).enqueue()
    }

    private fun observeRestoreWorker() {
        val workManager = WorkManager.getInstance(this.applicationContext)

        // observer for custom download manager worker
        workManager.getWorkInfosByTagLiveData(RestoreAgent.TAG).observe(this) { workInfoList ->
            val workInfo = workInfoList?.getOrNull(0) ?: return@observe
            Logger.i(
                LOG_TAG_BACKUP_RESTORE,
                "WorkManager state: ${workInfo.state} for ${RestoreAgent.TAG}"
            )
            if (WorkInfo.State.SUCCEEDED == workInfo.state) {
                showToastUiCentered(
                    this,
                    getString(R.string.brbs_restore_complete_toast),
                    Toast.LENGTH_SHORT
                )
                workManager.pruneWork()
            } else if (
                WorkInfo.State.CANCELLED == workInfo.state ||
                    WorkInfo.State.FAILED == workInfo.state
            ) {
                showToastUiCentered(
                    this,
                    getString(R.string.brbs_restore_no_uri_toast),
                    Toast.LENGTH_SHORT
                )
                workManager.pruneWork()
                workManager.cancelAllWorkByTag(RestoreAgent.TAG)
            } else { // state == blocked
                // no-op
            }
        }
    }

    private fun observeAppState() {
        VpnController.connectionStatus.observe(this) {
            if (it == BraveVPNService.State.PAUSED) {
                startActivity(Intent().setClass(this, PauseActivity::class.java))
                finish()
            }
        }
    }

    private fun removeThisMethod() {
        // change the persistent state for defaultDnsUrl, if its google.com (only for v055d)
        // fixme: remove this post v054.
        // this is to fix the default dns url, as the default dns url is changed from
        // dns.google.com to dns.google. In servers.xml default ips available for dns.google
        // so changing the default dns url to dns.google
        if (persistentState.defaultDnsUrl.contains("dns.google.com")) {
            persistentState.defaultDnsUrl = Constants.DEFAULT_DNS_LIST[2].url
        }
        moveRemoteBlocklistFileFromAsset()
        // reset the bio metric auth time, as now the value is changed from System.currentTimeMillis
        // to SystemClock.elapsedRealtime
        persistentState.biometricAuthTime = SystemClock.elapsedRealtime()
    }

    private fun rdnsRemote() {
        // enforce the dns to sky for play store build, and max for website and f-droid build
        // on first time launch
        io {
            if (isPlayStoreFlavour()) {
                appConfig.switchRethinkDnsToSky()
            } else {
                appConfig.switchRethinkDnsToMax()
            }
        }
    }

    // fixme: find a cleaner way to implement this, move this to some other place
    private fun moveRemoteBlocklistFileFromAsset() {
        io {
            // already there is a remote blocklist file available
            if (
                persistentState.remoteBlocklistTimestamp >
                    Constants.PACKAGED_REMOTE_FILETAG_TIMESTAMP
            ) {
                RethinkBlocklistManager.readJson(
                    this,
                    RethinkBlocklistManager.DownloadType.REMOTE,
                    persistentState.remoteBlocklistTimestamp
                )
                return@io
            }

            RemoteFileTagUtil.moveFileToLocalDir(this.applicationContext, persistentState)
        }
    }

    private fun launchOnboardActivity() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        startActivity(intent)
        finish()
    }

    private fun updateNewVersion() {
        if (!isNewVersion()) return

        val version = getLatestVersion()
        Logger.i(LOG_TAG_UI, "New version detected, updating the app version, version: $version")
        persistentState.appVersion = version
        persistentState.showWhatsNewChip = true

        // FIXME: remove this post v054
        // this is to fix the local blocklist default download location
        removeThisMethod()
    }

    private fun isNewVersion(): Boolean {
        val versionStored = persistentState.appVersion
        val version = getLatestVersion()
        return (version != 0 && version != versionStored)
    }

    private fun getLatestVersion(): Int {
        val pInfo: PackageInfo? = getPackageMetadata(this.packageManager, this.packageName)
        return pInfo?.versionCode ?: 0
    }

    // FIXME - Move it to Android's built-in WorkManager
    private fun initUpdateCheck() {
        if (!isUpdateRequired()) return

        val diff = System.currentTimeMillis() - persistentState.lastAppUpdateCheck

        val daysElapsed = TimeUnit.MILLISECONDS.toDays(diff)
        Logger.i(LOG_TAG_UI, "App update check initiated, number of days: $daysElapsed")
        if (daysElapsed <= 1L) return

        checkForUpdate()
    }

    private fun isUpdateRequired(): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        val day: Int = calendar.get(Calendar.DAY_OF_WEEK)
        return (day == Calendar.FRIDAY || day == Calendar.SATURDAY) &&
            persistentState.checkForAppUpdate
    }

    fun checkForUpdate(
        isInteractive: AppUpdater.UserPresent = AppUpdater.UserPresent.NONINTERACTIVE
    ) {
        // do not check for debug builds
        if (BuildConfig.DEBUG) return

        // Check updates only for play store / website version. Not fDroid.
        if (!isPlayStoreFlavour() && !isWebsiteFlavour()) {
            Logger.d(
                LOG_TAG_APP_UPDATE,
                "Check for update: Not play or website- ${BuildConfig.FLAVOR}"
            )
            return
        }

        if (isGooglePlayServicesAvailable() && isPlayStoreFlavour()) {
            try {
                appUpdateManager.checkForAppUpdate(
                    isInteractive,
                    this,
                    installStateUpdatedListener
                ) // Might be play updater or web updater
            } catch (e: Exception) {
                Logger.crash(LOG_TAG_APP_UPDATE, "err in app update check: ${e.message}", e)
                showDownloadDialog(
                    AppUpdater.InstallSource.STORE,
                    getString(R.string.download_update_dialog_failure_title),
                    getString(R.string.download_update_dialog_failure_message)
                )
            }
        } else {
            try {
                get<NonStoreAppUpdater>()
                    .checkForAppUpdate(
                        isInteractive,
                        this,
                        installStateUpdatedListener
                    ) // Always web updater
            } catch (e: Exception) {
                Logger.e(LOG_TAG_APP_UPDATE, "Error in app (web) update check: ${e.message}", e)
                showDownloadDialog(
                    AppUpdater.InstallSource.OTHER,
                    getString(R.string.download_update_dialog_failure_title),
                    getString(R.string.download_update_dialog_failure_message)
                )
            }
        }
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        // applicationInfo.enabled - When false, indicates that all components within
        // this application are considered disabled, regardless of their individually set enabled
        // status.
        // TODO: prompt dialog to user that Playservice is disabled, so switch to update
        // check for website
        return Utilities.getApplicationInfo(this, PKG_NAME_PLAY_STORE)?.enabled ?: false
    }

    private val installStateUpdatedListener =
        object : AppUpdater.InstallStateListener {
            override fun onStateUpdate(state: AppUpdater.InstallState) {
                Logger.i(LOG_TAG_UI, "InstallStateUpdatedListener: state: " + state.status)
                when (state.status) {
                    AppUpdater.InstallStatus.DOWNLOADED -> {
                        // CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                        showUpdateCompleteSnackbar()
                    }
                    else -> {
                        appUpdateManager.unregisterListener(this)
                    }
                }
            }

            override fun onUpdateCheckFailed(
                installSource: AppUpdater.InstallSource,
                isInteractive: AppUpdater.UserPresent
            ) {
                runOnUiThread {
                    if (isInteractive == AppUpdater.UserPresent.INTERACTIVE) {
                        showDownloadDialog(
                            installSource,
                            getString(R.string.download_update_dialog_failure_title),
                            getString(R.string.download_update_dialog_failure_message)
                        )
                    }
                }
            }

            override fun onUpToDate(
                installSource: AppUpdater.InstallSource,
                isInteractive: AppUpdater.UserPresent
            ) {
                runOnUiThread {
                    if (isInteractive == AppUpdater.UserPresent.INTERACTIVE) {
                        showDownloadDialog(
                            installSource,
                            getString(R.string.download_update_dialog_message_ok_title),
                            getString(R.string.download_update_dialog_message_ok)
                        )
                    }
                }
            }

            override fun onUpdateAvailable(installSource: AppUpdater.InstallSource) {
                runOnUiThread {
                    showDownloadDialog(
                        installSource,
                        getString(R.string.download_update_dialog_title),
                        getString(R.string.download_update_dialog_message)
                    )
                }
            }

            override fun onUpdateQuotaExceeded(installSource: AppUpdater.InstallSource) {
                runOnUiThread {
                    showDownloadDialog(
                        installSource,
                        getString(R.string.download_update_dialog_trylater_title),
                        getString(R.string.download_update_dialog_trylater_message)
                    )
                }
            }
        }

    private fun showUpdateCompleteSnackbar() {
        val snack =
            Snackbar.make(
                b.container,
                getString(R.string.update_complete_snack_message),
                Snackbar.LENGTH_INDEFINITE
            )
        snack.setAction(getString(R.string.update_complete_action_snack)) {
            appUpdateManager.completeUpdate()
        }
        snack.setActionTextColor(ContextCompat.getColor(this, R.color.primaryLightColorText))
        snack.show()
    }

    private fun showDownloadDialog(
        source: AppUpdater.InstallSource,
        title: String,
        message: String
    ) {
        if (!isInForeground()) return

        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)
        if (
            message == getString(R.string.download_update_dialog_message_ok) ||
                message == getString(R.string.download_update_dialog_failure_message) ||
                message == getString(R.string.download_update_dialog_trylater_message)
        ) {
            builder.setPositiveButton(getString(R.string.hs_download_positive_default)) {
                dialogInterface,
                _ ->
                dialogInterface.dismiss()
            }
        } else {
            if (source == AppUpdater.InstallSource.STORE) {
                builder.setPositiveButton(getString(R.string.hs_download_positive_play_store)) {
                    dialogInterface,
                    _ ->
                    appUpdateManager.completeUpdate()
                    dialogInterface.dismiss()
                }
            } else {
                builder.setPositiveButton(getString(R.string.hs_download_positive_website)) {
                    dialogInterface,
                    _ ->
                    initiateDownload()
                    dialogInterface.dismiss()
                }
            }
            builder.setNegativeButton(getString(R.string.hs_download_negative_default)) {
                dialogInterface,
                _ ->
                persistentState.lastAppUpdateCheck = System.currentTimeMillis()
                dialogInterface.dismiss()
            }
        }

        builder.create().show()
    }

    private fun initiateDownload() {
        try {
            val url = Constants.RETHINK_APP_DOWNLOAD_LINK
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showToastUiCentered(this, getString(R.string.no_browser_error), Toast.LENGTH_SHORT)
            Logger.w(Logger.LOG_TAG_VPN, "Failure opening rethink download link: ${e.message}", e)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        } catch (e: IllegalArgumentException) {
            Logger.w(LOG_TAG_DOWNLOAD, "Unregister receiver exception")
        }
    }

    private fun setupNavigationItemSelectedListener() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val btmNavView = findViewById<BottomNavigationView>(R.id.nav_view)
        btmNavView.setupWithNavController(navController)
    }

    private fun io(f: suspend () -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) { f() }
    }

}