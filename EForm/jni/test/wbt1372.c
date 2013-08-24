#include <libusb.h>
#include <stdio.h>
#include <ctype.h>

static void usbx_strerror(int code)
{
  printf("%s(%s)\n", libusb_strerror(code), libusb_error_name(code));
}

static int readcard(libusb_device_handle *handle)
{
  unsigned char buff[1280];
  int transferred = 0, rv;

  rv = libusb_clear_halt(handle, 0x82);
  if (rv != 0)
    {
      usbx_strerror(rv);
      return -1;
    }
  rv = libusb_bulk_transfer (handle, 0x82, buff, sizeof(buff),
			     &transferred, 10 * 1000);
  if (rv != 0)
    {
      usbx_strerror(rv);
      return -1;
    }
  printf("read %d bytes\n", transferred);
  for (rv = 0; rv < transferred; rv++)
    {
      printf(isprint(buff[rv]) ? "%c" : "\\x%02X", buff[rv]);
    }
  printf("\n");
  return transferred;
}

int main()
{
  libusb_context *context;
  libusb_device_handle *handle;
  int configuration_value = 1, interface_number = 1, rv;

  rv = libusb_init(&context);
  if (rv != 0)
    {
      usbx_strerror(rv);
      return -1;
    }
  libusb_setlocale("zh");
  libusb_set_debug(context, LIBUSB_LOG_LEVEL_DEBUG);

  handle = libusb_open_device_with_vid_pid(context, 0x16C0, 0x06EA);
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
  readcard(handle);

  libusb_release_interface(handle, interface_number);
  libusb_close(handle);
  libusb_exit(context);
  return 0;
}
