package fasofts.element.security

import android.content.Context
import android.os.Handler
import android.os.Looper

object RuntimeWatchdog {
    private const val INTERVAL_MS = 15_000L

    fun start(context: Context) {
        val handler = Handler(Looper.getMainLooper())
        val r = object : Runnable {
            override fun run() {
                try {
                    Security.validateSignature(context)
                } catch (t: Throwable) {
                    // se falhar -> termina processo imediatamente
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
                handler.postDelayed(this, INTERVAL_MS)
            }
        }
        handler.post(r)
    }
}