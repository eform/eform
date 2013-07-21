/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

abstract public class Upgrader
{
    public static final int UPGRADE_FROM_USBDISK = 1;
    public static final int UPGRADE_FROM_NETWORK = 2;
	
    static public Upgrader getUpgrader(int from) throws Exception {
	switch (from) {
	case UPGRADE_FROM_USBDISK:
	    return new UpgradeUsbdisk();
	case UPGRADE_FROM_NETWORK:
	    return new UpgradeNetwork();
	default:
	    throw new Exception();
	}
    }

    abstract public void getUpgradeIDL();
    //	abstract public void reverse();
}

/**
 * A upgrade subclass to upgrade system from usb disk.
 */
class UpgradeUsbdisk extends Upgrader
{
    public UpgradeUsbdisk() {
	
    }

    @Override
    public void getUpgradeIDL() {
		
    }
	
}

/**
 * A upgrade subclass to upgrade system from network.
 */
class UpgradeNetwork extends Upgrader
{
    public UpgradeNetwork() {
		
    }

    @Override
    public void getUpgradeIDL() {
		
    }
}
