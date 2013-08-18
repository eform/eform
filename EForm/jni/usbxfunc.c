#include <libusb.h>
#include <stdio.h>
#include <stdlib.h>

void usbx_strerror(int code)
{
  printf("%s(%s)\n", libusb_strerror(code), libusb_error_name(code));
}

void usbx_init(libusb_context **context)
{
  int ret = libusb_init(context);
  if (ret != 0)
    {
      usbx_strerror(ret);
      abort();
    }
  libusb_setlocale("zh");

  if (context)
    libusb_set_debug(*context, LIBUSB_LOG_LEVEL_DEBUG);
}

void usbx_exit(libusb_context *context)
{
  libusb_exit(context);
}
