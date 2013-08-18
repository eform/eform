# Copyright 2013 Cansiny Techonology Trade Co.,Ltd.
#
LOCAL_PATH := $(call my-dir)
LOCAL_PATH_SAVE := $(LOCAL_PATH)

include $(CLEAR_VARS)
subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, serial))
include $(subdirs)

include $(CLEAR_VARS)
LOCAL_PATH := $(LOCAL_PATH_SAVE)
subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, \
	libusbx-1.0.16))
include $(subdirs)

include $(CLEAR_VARS)
LOCAL_PATH := $(LOCAL_PATH_SAVE)
subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, usbutils))
include $(subdirs)

include $(CLEAR_VARS)
LOCAL_PATH := $(LOCAL_PATH_SAVE)
subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, \
	usbhid-dump))
include $(subdirs)

include $(CLEAR_VARS)
LOCAL_PATH := $(LOCAL_PATH_SAVE)
subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, test))
include $(subdirs)
