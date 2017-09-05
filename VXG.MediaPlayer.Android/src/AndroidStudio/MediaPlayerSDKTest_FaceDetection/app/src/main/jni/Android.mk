LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := MediaPlayerSDKTest
LOCAL_SRC_FILES := MediaPlayerSDKTest.cpp

include $(BUILD_SHARED_LIBRARY)
