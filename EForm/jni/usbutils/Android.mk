# Copyright 2013 Cansiny Techonology Trade Co.,Ltd.
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE     := lsusb
LOCAL_C_INCLUDES += usbutils libusbx-1.0.16/libusb
LOCAL_C_INCLUDES += .
LOCAL_CFLAGS     += -DDATADIR=\"/etc\"
LOCAL_SRC_FILES  := lsusb.c names.c usbmisc.c lsusb-t.c
LOCAL_LDLIBS     := -llog -L../libs/armeabi -lusbx
TARGET_PLATFORM  := android-14
include $(BUILD_EXECUTABLE)
