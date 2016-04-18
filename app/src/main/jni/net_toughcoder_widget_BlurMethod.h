#include <jni.h>

#ifndef __NATIVE_BLUR_
#define __NATIVE_BLUR_

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_net_toughcoder_widget_BlurMethod_nativeBlur(JNIEnv* env, jclass clazz, jobject bitmap, jint radius);

#ifdef __cplusplus
}
#endif

#endif
