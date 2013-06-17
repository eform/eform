/* EForm - Electronic Form System
 *
 * Copyright (C) 2013 Wu Xiaohu. All rights reserved.
 * Copyright (C) 2013 Cansiny Trade Co.,Ltd. All rights reserved.
 * 
 * IDLItemReader - Read a business item IDL
 *
 * Authors:
 *   Xiaohu <xiaohu417@gmail.com>, 2013.6.11, hefei
 */
package com.cansiny.eform;

import java.util.List;

public class IDLItemReader
{
	public String name = null;
	public String label = null;
	public String iconname = null;

	List<IDLItemPage> pages = null;
	
	IDLItemPage getPrevPage() {
		return null;
	}
	IDLItemPage getNextPage() {
		return null;
	}
	boolean isLastPage() {
		return false;
	}
}
