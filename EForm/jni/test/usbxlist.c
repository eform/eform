#include <libusb.h>
#include <stdio.h>

#include "usbxfunc.c"

static void usbx_check_capability()
{
  if (libusb_has_capability(LIBUSB_CAP_HAS_CAPABILITY))
    {
      if (libusb_has_capability(LIBUSB_CAP_HAS_HOTPLUG))
	printf("libusbx has hotplug capability\n");
      else
	printf("libusbx has *NOT* hotplug capability\n");

      if (libusb_has_capability(LIBUSB_CAP_HAS_HID_ACCESS))
	printf("libusbx has hid access capability\n");
      else
	printf("libusbx has *NOT* hid access capability\n");

      if (libusb_has_capability(LIBUSB_CAP_SUPPORTS_DETACH_KERNEL_DRIVER))
	printf("libusbx supports detach kernel driver\n");
      else
	printf("libusbx *NOT* supports detach kernel driver\n");
    }
  else
    {
      printf("libusbx can NOT check capability\n");
    }
}

static const char* usbx_device_class_name(int code)
{
  switch(code)
    {
    case LIBUSB_CLASS_PER_INTERFACE:
      return "Each interface specifies its own class";
    case LIBUSB_CLASS_AUDIO: return "Audio";
    case LIBUSB_CLASS_COMM: return "Communications";
    case LIBUSB_CLASS_HID: return "Human Interface Device";
    case LIBUSB_CLASS_PHYSICAL: return "Physical";
    case LIBUSB_CLASS_PRINTER: return "Printer";
    case LIBUSB_CLASS_PTP: return "Image";
    case LIBUSB_CLASS_MASS_STORAGE: return "Mass storage";
    case LIBUSB_CLASS_HUB: return "Hub";
    case LIBUSB_CLASS_DATA: return "Data";
    case LIBUSB_CLASS_SMART_CARD: return "Smart Card";
    case LIBUSB_CLASS_CONTENT_SECURITY: return "Content Security";
    case LIBUSB_CLASS_VIDEO: return "Video";
    case LIBUSB_CLASS_PERSONAL_HEALTHCARE: return "Personal Healthcare";
    case LIBUSB_CLASS_DIAGNOSTIC_DEVICE: return "Diagnostic Device";
    case LIBUSB_CLASS_WIRELESS: return "Wireless";
    case LIBUSB_CLASS_APPLICATION: return "Application";
    case LIBUSB_CLASS_VENDOR_SPEC: return "Vendor specific";
    default: return "";
    }
}

static void usbx_list_devices(libusb_context *context)
{
  libusb_device **devices;
  ssize_t i;

  ssize_t count = libusb_get_device_list(context, &devices);
  printf("Number of USB Device: %ld\n", count);

  for (i = 0; i < count; i++)
    {
      char *speed = "";
      struct libusb_device_descriptor desc;
      int j;

      switch(libusb_get_device_speed(devices[i]))
	{
	case LIBUSB_SPEED_LOW:
	  speed = "LOW(1.5MBit/s)";
	  break;
	case LIBUSB_SPEED_FULL:
	  speed = "FULL(12MBit/s)";
	  break;
	case LIBUSB_SPEED_HIGH:
	  speed = "HIGH(480MBit/s)";
	  break;
	case LIBUSB_SPEED_SUPER:
	  speed = "SUPER(5000MBit/s)";
	  break;
	default:
	  speed = "Unknown";
	  break;
	}

      printf("%ld. BUS: %d, Port: %d, Address: %d, Speed: %s\n", i,
	     libusb_get_bus_number(devices[i]),
	     libusb_get_port_number(devices[i]),
	     libusb_get_device_address(devices[i]), speed);

      libusb_get_device_descriptor(devices[i], &desc);
      printf("  Descriptor:\n");
      printf("    Size: %d, Type: %d, bcdUSB: %04X, "
	     "VID: %04x, PID: %04X, bcdDevice: %04X\n"
	     "    Class: %s\n"
	     "    Max Packet Size of Endpoint 0: %d\n"
	     "    Number of Configurations: %d\n",
	     desc.bLength, desc.bDescriptorType, desc.bcdUSB,
	     desc.idVendor, desc.idProduct, desc.bcdDevice,
	     usbx_device_class_name(desc.bDeviceClass),
	     desc.bMaxPacketSize0, desc.bNumConfigurations);

      for (j = 0; j < desc.bNumConfigurations; j++)
	{
	  struct libusb_config_descriptor *conf;
	  int k;

	  libusb_get_config_descriptor(devices[i], j, &conf);
	  printf("    Configuration: %d\n", j);
	  printf("      Size: %d, Type, %d, Total: %d, Value: %d, "
		 "Attrs: %04X, Max Power: %d\n"
		 "      Extra Length: %d, Number of Interface: %d\n",
		 conf->bLength, conf->bDescriptorType,
		 conf->wTotalLength, conf->bConfigurationValue,
		 conf->bmAttributes, conf->MaxPower,
		 conf->extra_length, conf->bNumInterfaces);

	  for (k = 0; k < conf->bNumInterfaces; k++)
	    {
	    }
	  libusb_free_config_descriptor(conf);
	}
    }
}

int main()
{
  usbx_init(NULL);

  usbx_check_capability();
  usbx_list_devices(NULL);

  usbx_exit(NULL);
  return 0;
}
