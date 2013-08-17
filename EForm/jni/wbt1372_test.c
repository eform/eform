#include <libusb.h>
#include <stdio.h>

#include "usbxfunc.c"

int main()
{
  libusb_context *context;
  printf("hello\n");

  usbx_init(&context);

  usbx_exit(context);
  return 0;
}
