LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off
OPENCV_LIB_TYPE:=STATIC
#include D:\C_Code\Android\OpenCV-android-sdk\sdk\native\jni\OpenCV.mk
include D:\TrianglePage\OpenCV-android-sdk\sdk\native\jni\OpenCV.mk
#config by kevin
#include /home/kevin/puzzleworld/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := img_processor
LOCAL_SRC_FILES := img_processor.cpp
LOCAL_LDLIBS    += -lm -llog -landroid

include $(BUILD_SHARED_LIBRARY)