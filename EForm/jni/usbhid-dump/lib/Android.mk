# Copyright © 2013 Cansiny Trade Co.,Ltd.
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE     := libuhd
LOCAL_C_INCLUDES += libusbx-1.0.16/libusb usbhid-dump usbhid-dump/include
LOCAL_C_INCLUDES += .
LOCAL_SRC_FILES  := dev.c dev_list.c iface.c iface_list.c libusb.c
TARGET_PLATFORM  := android-14
include $(BUILD_STATIC_LIBRARY)
