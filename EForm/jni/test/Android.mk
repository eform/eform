# Copyright 2013 Cansiny Techonology Trade Co.,Ltd.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE     := usbxlist
LOCAL_SRC_FILES  := usbxlist.c
LOCAL_C_INCLUDES += libusbx-1.0.16/libusb
LOCAL_CFLAGS     += -Wall
LOCAL_LDLIBS     := -llog -L../libs/armeabi -lusbx
TARGET_PLATFORM  := android-14
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_MODULE     := wbt1372
LOCAL_SRC_FILES  := wbt1372.c
LOCAL_C_INCLUDES += libusbx-1.0.16/libusb
LOCAL_CFLAGS     += -Wall
LOCAL_LDLIBS     := -llog -L../libs/armeabi -lusbx
TARGET_PLATFORM  := android-14
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_MODULE     := lq90kp
LOCAL_SRC_FILES  := lq90kp.c
LOCAL_C_INCLUDES += libusbx-1.0.16/libusb
LOCAL_CFLAGS     += -Wall
LOCAL_LDLIBS     := -llog -L../libs/armeabi -lusbx
TARGET_PLATFORM  := android-14
include $(BUILD_EXECUTABLE)
