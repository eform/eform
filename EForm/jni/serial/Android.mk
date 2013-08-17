# Copyright © 2013 Cansiny Trade Co.,Ltd.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE     := serial
LOCAL_SRC_FILES  := SerialPort.c
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_CFLAGS     += -Wall
LOCAL_LDLIBS     := -llog
TARGET_PLATFORM  := android-14
include $(BUILD_SHARED_LIBRARY)

