# Copyright © 2013 Cansiny Trade Co.,Ltd.
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE     := usbdump
LOCAL_C_INCLUDES += libusbx-1.0.16/libusb usbhid-dump usbhid-dump/include
LOCAL_C_INCLUDES += .
LOCAL_SRC_FILES  := usbhid-dump.c
LOCAL_LDLIBS     := -llog -L../libs/armeabi -lusbx
LOCAL_STATIC_LIBRARIES := libuhd
TARGET_PLATFORM  := android-14
include $(BUILD_EXECUTABLE)
