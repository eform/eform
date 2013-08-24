#include <libusb.h>
#include <stdio.h>
#include <ctype.h>

static void usbx_strerror(int code)
{
  printf("%s(%s)\n", libusb_strerror(code), libusb_error_name(code));
}

static int
putstring(libusb_device_handle *handle, unsigned char *data, int length)
{
  int transferred = 0, rv;

  rv = libusb_bulk_transfer (handle, 0x01, data, length,
			     &transferred, 10000);
  if (rv != 0)
    {
      usbx_strerror(rv);
      return -1;
    }
  printf("written %d bytes\n", transferred);
  return transferred;
}

static int
getstring(libusb_device_handle *handle)
{
  unsigned char buff[1280];
  int transferred = 0, rv;

  rv = libusb_bulk_transfer (handle, 0x82, buff, sizeof(buff),
			     &transferred, 10000);
  if (rv != 0)
    {
      usbx_strerror(rv);
      return -1;
    }
  printf("read %d bytes\n", transferred);
  return transferred;
}

int main()
{
  libusb_context *context;
  libusb_device_handle *handle;
  int configuration_value = 1, interface_number = 0, rv;
  char *p;

  rv = libusb_init(&context);
  if (rv != 0)
    {
      usbx_strerror(rv);
      return -1;
    }
  libusb_setlocale("zh");
  libusb_set_debug(context, LIBUSB_LOG_LEVEL_DEBUG);

  handle = libusb_open_device_with_vid_pid(context, 0x04B8, 0x0005);
  if (!handle)
    {
      libusb_exit(context);
      return -1;
    }
  libusb_set_auto_detach_kernel_driver(handle, 1);

  rv = libusb_set_configuration(handle, configuration_value);
  if (rv != 0)
    {
      usbx_strerror(rv);
      libusb_close(handle);
      libusb_exit(context);
      return -1;
    }
  rv = libusb_claim_interface(handle, interface_number);
  if (rv != 0)
    {
      usbx_strerror(rv);
      libusb_close(handle);
      libusb_exit(context);
      return -1;
    }
  /*
  putstring(handle, (unsigned char*) "\x1B\x40", 2);
  putstring(handle, (unsigned char*) "\x1B\x4A\x10", 3);
  putstring(handle, (unsigned char*) "hello\n", 5);
  putstring(handle, (unsigned char*) "\x1B\x6A\x10", 3);
  */

  //putstring(handle, (unsigned char*) "\x1B\x194", 3);
  putstring(handle, (unsigned char*) "hello\n", 6);
  /*
  p = malloc(10000);
  memset(p, ' ', 10000);
  putstring(handle, p, 10000);
  */
  //getstring(handle);
  putstring(handle, (unsigned char*) "\x07", 1);
  //putstring(handle, (unsigned char*) "\x18", 1);
  putstring(handle, (unsigned char*) "\x0C", 1);
  //putstring(handle, (unsigned char*) "\x1B\x190", 3);
  //putstring(handle, (unsigned char*) "\x1B\x40", 2);
  //putstring(handle, (unsigned char*) "\x1B\x4F", 2);

  libusb_release_interface(handle, interface_number);
  libusb_close(handle);
  libusb_exit(context);
  return 0;
}
