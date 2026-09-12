package fasofts.element.security

object Native {
    init {
        System.loadLibrary("native-lib")
    }

    @JvmStatic external fun checkIntegrity(pkg: String): Boolean
}