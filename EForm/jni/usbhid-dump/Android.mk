# Copyright 2013 Cansiny Techonology Trade Co.,Ltd.
#
LOCAL_PATH:= $(call my-dir)
USBHIDDUMP_PATH := $(LOCAL_PATH)

include $(CLEAR_VARS)
subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, lib))
include $(subdirs)

include $(CLEAR_VARS)
LOCAL_PATH := $(USBHIDDUMP_PATH)
subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, src))
include $(subdirs)
