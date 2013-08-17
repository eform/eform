# Copyright © 2013 Cansiny Trade Co.,Ltd.
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE     := usbx
LOCAL_C_INCLUDES += libusbx-1.0.16/          \
		    libusbx-1.0.16/libusb/   \
		    libusbx-1.0.16/libusb/os
LOCAL_C_INCLUDES += .. . os
LOCAL_SRC_FILES  := core.c descriptor.c io.c sync.c hotplug.c strerror.c \
	            os/linux_usbfs.c os/linux_netlink.c os/poll_posix.c \
		    os/threads_posix.c
LOCAL_LDLIBS     := -llog
TARGET_PLATFORM  := android-14

include $(BUILD_SHARED_LIBRARY)
