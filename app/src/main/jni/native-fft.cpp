//
// Created by HW Lee on 28/10/2017.
//

#include <jni.h>
#include <string>
#include "FFT.h"

extern "C"
JNIEXPORT jstring JNICALL Java_com_htc_tonedetector_FFT_getVersion(
        JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(FFT::getVersion().c_str());
}
