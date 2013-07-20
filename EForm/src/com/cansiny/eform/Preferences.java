/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 * 
 * Preferences - Remember user prefer settings.
 *
 * Authors:
 *   Xiaohu <xiaohu417@gmail.com>, 2013.6.12, hefei
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
}
