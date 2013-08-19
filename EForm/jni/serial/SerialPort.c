/*
 * Copyright 2009-2011 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <errno.h>
#include <stdio.h>
#include <jni.h>

#include "SerialPort.h"
#include "android/log.h"

//static const char *TAG = "serialPort";
#define TAG "SerialPort"

#define LOGI(fmt, args...) \
	__android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) \
	__android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) \
 	__android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

static speed_t getBaudrate(jint baudrate);

static char errbuf[1024] = { '\0', };

/*
 * Class:     com_cansiny_eform_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL
Java_com_cansiny_eform_SerialPort_open(JNIEnv *env,
				       jclass  thiz,
				       jstring path,
				       jint    baudrate,
				       jint    flags)
{
  int fd;
  speed_t speed;
  jobject mFileDescriptor;
  jboolean iscopy;
  const char *path_utf;
  struct termios cfg;
  jclass cFileDescriptor;
  jmethodID iFileDescriptor;
  jfieldID descriptorID;

  speed = getBaudrate(baudrate);
  if (speed == -1)
    {
      snprintf(errbuf, sizeof(errbuf), "波特率 %d 无效", baudrate);
      return NULL;
    }
  path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
  fd = open(path_utf, O_RDWR | flags);
  if (fd == -1)
    {
      snprintf(errbuf, sizeof(errbuf),
	       "不能打开串口 %s: %s", path_utf, strerror(errno));
      (*env)->ReleaseStringUTFChars(env, path, path_utf);
      return NULL;
    }
  (*env)->ReleaseStringUTFChars(env, path, path_utf);

  if (tcgetattr(fd, &cfg))
    {
      snprintf(errbuf, sizeof(errbuf),
	       "tcgetattr() 错误: %s", strerror(errno));
      close(fd);
      return NULL;
    }
  cfmakeraw(&cfg);
  cfsetispeed(&cfg, speed);
  cfsetospeed(&cfg, speed);
  if (tcsetattr(fd, TCSANOW, &cfg))
    {
      snprintf(errbuf, sizeof(errbuf),
	       "tcsetattr() 错误: %s", strerror(errno));
      close(fd);
      return NULL;
    }

  /* Create a corresponding file descriptor */
  cFileDescriptor =
    (*env)->FindClass(env, "java/io/FileDescriptor");
  iFileDescriptor =
    (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
  descriptorID =
    (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
  mFileDescriptor =
    (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);

  (*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint)fd);
  return mFileDescriptor;
}

/*
 * Class:     com_cansiny_eform_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_com_cansiny_eform_SerialPort_close(JNIEnv *env, jobject thiz)
{
  jclass SerialPortClass = (*env)->GetObjectClass (env, thiz);
  jclass FileDescriptorClass =
    (*env)->FindClass (env, "java/io/FileDescriptor");
  jfieldID mFdID =
    (*env)->GetFieldID (env, SerialPortClass, "mFd",
			"Ljava/io/FileDescriptor;");
  jfieldID descriptorID =
    (*env)->GetFieldID (env, FileDescriptorClass, "descriptor", "I");

  jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
  jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

  close(descriptor);
}

/*
 * Class:     com_cansiny_eform_SerialPort
 * Method:    getErrbuf
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring
JNICALL Java_com_cansiny_eform_SerialPort_getErrbuf(JNIEnv *env,
						    jclass  thiz)
{
  return (*env)->NewStringUTF(env, errbuf);
}

speed_t getBaudrate(jint baudrate)
{
  switch(baudrate)
    {
    case 0: return B0;
    case 50: return B50;
    case 75: return B75;
    case 110: return B110;
    case 134: return B134;
    case 150: return B150;
    case 200: return B200;
    case 300: return B300;
    case 600: return B600;
    case 1200: return B1200;
    case 1800: return B1800;
    case 2400: return B2400;
    case 4800: return B4800;
    case 9600: return B9600;
    case 19200: return B19200;
    case 38400: return B38400;
    case 57600: return B57600;
    case 115200: return B115200;
    case 230400: return B230400;
    case 460800: return B460800;
    case 500000: return B500000;
    case 576000: return B576000;
    case 921600: return B921600;
    case 1000000: return B1000000;
    case 1152000: return B1152000;
    case 1500000: return B1500000;
    case 2000000: return B2000000;
    case 2500000: return B2500000;
    case 3000000: return B3000000;
    case 3500000: return B3500000;
    case 4000000: return B4000000;
    default: return -1;
    }
}
