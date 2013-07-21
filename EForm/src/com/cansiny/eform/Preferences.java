/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 */
package com.cansiny.eform;

public class Preferences
{
    static Preferences _singlePreferences = null;
    static Preferences getPreferences() {
	if (_singlePreferences == null)
	    _singlePreferences = new Preferences();
	return _singlePreferences;
    }

    private Preferences() {
		
    }
    
    public String magcardReaderPath() {
	return "/dev/ttyS0";
    }

}
