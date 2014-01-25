/*
 * encode_mp3.h
 *
 *  Created on: 25-Jan-2014
 *      Author: mohit
 */


#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

int Java_com_example_libavndkdemo_Mp3Encoder_initAudio(JNIEnv* env, jobject obj, jstring filePath);

void Java_com_example_libavndkdemo_Mp3Encoder_writeAudioFrame(JNIEnv* env, jobject this, jshortArray inSample, jint length);

int Java_com_example_libavndkdemo_Mp3Encoder_getFrameSize();
int Java_com_example_libavndkdemo_Mp3Encoder_close(JNIEnv* env, jobject obj);

#ifdef __cplusplus
}
#endif


