LOCAL_PATH := $(call my-dir)
LIBAV_PATH := $(LOCAL_PATH)

ifeq ($(TARGET_ARCH),arm)
        ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
		LIBAV_CONFIG := Android_config
        else
		LIBAV_CONFIG := Android_config_no_neon
        endif
else
	LIBAV_CONFIG := Android_config_$(TARGET_ARCH)
endif

include $(LIBAV_PATH)/libavcodec/Android.mk
include $(LIBAV_PATH)/libswscale/Android.mk
include $(LIBAV_PATH)/libavutil/Android.mk
include $(LIBAV_PATH)/libavfilter/Android.mk
include $(LIBAV_PATH)/libavformat/Android.mk
include $(LIBAV_PATH)/libavresample/Android.mk
include $(LIBAV_PATH)/libavdevice/Android.mk


include $(CLEAR_VARS)
LOCAL_PATH := $(LIBAV_PATH)
ifeq ($(TARGET_ARCH),arm)
        ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
			CONFIG := Android_config
        else
			CONFIG := Android_config_no_neon
        endif
else
		CONFIG := Android_config_$(TARGET_ARCH)
endif
LOCAL_MODULE  := androidlibav
LOCAL_SRC_FILES := avconv_opt.c cmdutils.c avconv_filter.c avconv.c LibavJni.c encode_mp3.c
# These need to be in the right order
LOCAL_C_INCLUDES +=	$(LOCAL_PATH)/libavdevice \
			$(LOCAL_PATH)/$(CONFIG) \
			$(LOCAL_PATH)/$(CONFIG)/x264 \
			$(LOCAL_PATH)/$(CONFIG)/lame \
			$(LOCAL_PATH)/libavformat \
			$(LOCAL_PATH)/libavcodec \
 			$(LOCAL_PATH)/libavfilter \
 			$(LOCAL_PATH)/libswscale \
 			$(LOCAL_PATH)/libavresample \
 			$(LOCAL_PATH)/libavutil 
 			

# ffmpeg uses its own deprecated functions liberally, so turn off that annoying noise
LOCAL_CFLAGS += -g -Wno-deprecated-declarations -UNDEBUG 
LOCAL_LDLIBS += -llog -lz -landroid
LOCAL_SHARED_LIBRARIES := libavdevice libavformat libavcodec libavfilter libswscale libavutil libavresample
include $(BUILD_SHARED_LIBRARY)



#LOCAL_PATH := $(call my-dir)
#include $(CLEAR_VARS)
#LOCAL_MODULE  := final_file
#LOCAL_SRC_FILES := "final_file.c"
# ffmpeg uses its own deprecated functions liberally, so turn off that annoying noise
#LOCAL_CFLAGS += -g -Wno-deprecated-declarations 
#LOCAL_LDLIBS += -llog -lz
#LOCAL_SHARED_LIBRARIES := libavconv
#include $(BUILD_SHARED_LIBRARY)


#LOCAL_PATH := $(call my-dir)
#include $(CLEAR_VARS)
#LOCAL_MODULE    := hello-jni
#LOCAL_SRC_FILES := "hello-jni.c"
#LOCAL_SHARED_LIBRARIES := libavconv
#include $(BUILD_SHARED_LIBRARY)
