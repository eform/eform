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

int usbx_read(libusb_device_handle *handle, unsigned char endpoint)
{
  static unsigned char buff[1280];
  int transferred = 0, rv;

  rv = libusb_clear_halt(handle, endpoint);
  if (rv != 0)
    {
      usbx_strerror(rv);
      return -1;
    }

  rv = libusb_bulk_transfer(handle, endpoint, buff, sizeof(buff),
			    &transferred, 8000);
  if (rv != 0)
    {
      usbx_strerror(rv);
      return -1;
    }
  printf("read %d bytes\n", transferred);
  if (transferred > 0)
    printf("%s\n", buff);

  return transferred;
}
