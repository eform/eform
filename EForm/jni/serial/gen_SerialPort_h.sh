#!/bin/sh
#
# Generate Serial Port JNI interface from class interface
#
javah -o SerialPort.h -jni -classpath ../../bin/classes com.cansiny.eform.SerialPort
