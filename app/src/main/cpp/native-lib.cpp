#include <jni.h>
#include <android/log.h>

extern "C"
JNIEXPORT jint JNICALL
Java_fasofts_element__ui_PrepareVpnActivity_nativeAdd(JNIEnv*, jobject, jint a, jint b) {
    __android_log_print(ANDROID_LOG_INFO, "native-lib", "sum=%d", a + b);
    return a + b;
}