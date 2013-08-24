# Push executable file to device for test
#
adb push ../libs/armeabi/libusbx.so /system/lib/

adb push usbutils/usb.ids /system/etc/
adb push ../libs/armeabi/lsusb /system/bin/
adb push ../libs/armeabi/usbdump /system/bin/

adb push ../libs/armeabi/usbxlist /system/bin/
adb push ../libs/armeabi/wbt1372 /system/bin/
adb push ../libs/armeabi/lq90kp /system/bin/
