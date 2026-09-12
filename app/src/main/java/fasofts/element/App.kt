package fasofts.element

import android.app.Application
import fasofts.element.security.Native
import fasofts.element.security.Security
import fasofts.element.security.RuntimeWatchdog

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1) valida assinatura + package
        Security.validateSignature(this)

        // 2) validação nativa simples
        val nativeOk = try { Native.checkIntegrity(this.packageName) } catch (e: Throwable) { false }
        if (!nativeOk) throw SecurityException("")

        // 3) inicializa watchdog runtime (checagens periódicas)
        RuntimeWatchdog.start(this)
    }
}