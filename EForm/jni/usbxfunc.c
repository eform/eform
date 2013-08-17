#include <libusb.h>

void usbx_strerror(int code)
{
  libusb_setlocale("zh");
  printf("%s\n", libusb_strerror(code));
}

int usbx_init(libusb_context **context)
{
  int ret = libusb_init(context);
  if (ret != 0)
    {
      usbx_strerror(ret);
      return ret;
    }
  if (context)
    {
      libusb_set_debug(*context, LIBUSB_LOG_LEVEL_DEBUG);
    }
  return ret;
}

void usbx_exit(libusb_context *context)
{
  libusb_exit(context);
}

