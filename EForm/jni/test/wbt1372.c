#include <libusb.h>
#include <stdio.h>

#include "usbxfunc.c"

int main()
{
  libusb_context *context;
  libusb_device_handle *handle;
  int configuration_value = 1, interface_number = 1, rv;

  usbx_init(&context);

  handle = libusb_open_device_with_vid_pid(context, 0x16C0, 0x06EA);
  if (!handle)
    {
      usbx_exit(context);
      return -1;
    }
  libusb_set_auto_detach_kernel_driver(handle, 1);

  rv = libusb_set_configuration(handle, configuration_value);
  if (rv != 0)
    {
      usbx_strerror(rv);
      libusb_close(handle);
      usbx_exit(context);
      return -1;
    }
  rv = libusb_claim_interface(handle, interface_number);
  if (rv != 0)
    {
      usbx_strerror(rv);
      libusb_close(handle);
      usbx_exit(context);
      return -1;
    }
  rv = usbx_read(handle, 0x82);

  libusb_release_interface(handle, interface_number);
  //libusb_reset_device(handle);
  libusb_close(handle);
  usbx_exit(context);
  return 0;
}
