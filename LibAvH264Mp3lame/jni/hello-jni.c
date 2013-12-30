/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
//#include <android/log.h>
//#include "logjam.h"
#include <avconv.h>
#include <stdlib.h>
#include <stdbool.h>

/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/hellojni/HelloJni.java
 */
/*jstring
Java_com_example_libavndkdemo_Videokit_stringFromJNI( JNIEnv* env,
                                                  jobject thiz )
{
    return (*env)->NewStringUTF(env, "Hello from JNI !");
}*/

jstring
Java_com_example_libavndkdemo_Videokit_unimplementedStringFromJNI( JNIEnv* env,
                                                  jobject thiz )
{
    return (*env)->NewStringUTF(env, "unimplementedStringFromJNI : Hello from JNI !");
}

jstring Java_com_example_libavndkdemo_Videokit_runOnMe( JNIEnv* env,
		jobject obj, jobjectArray args ) {
/*//	LOGD("run() called");
	int i = 0;
	int argc = 0;
	char **argv = NULL;

	if (args != NULL) {
		argc = (*env)->GetArrayLength(env, args);
		argv = (char **) malloc(sizeof(char *) * argc);

		for (i = 0; i < argc; i++) {
			jstring str = (jstring) (*env)->GetObjectArrayElement(env, args, i);
			argv[i] = (char *) (*env)->GetStringUTFChars(env, str, NULL);
		}
	}

//	LOGD("run passing off to main()");
//	main(argc, argv);
	start(argc, argv);*/

	return (*env)->NewStringUTF(env, "Completed !");
}
