# Copyright 2013 Cansiny Techonology Trade Co.,Ltd.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, libusb))
include $(subdirs)
