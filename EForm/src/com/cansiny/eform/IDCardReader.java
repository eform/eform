package com.cansiny.eform;

public class IDCardReader
{
	public IDCardInfo getCardInfo() {
		IDCardInfo info = new IDCardInfo();
		return info;
	}

	
	public class IDCardInfo
	{
		public String name;
		public boolean sex;
		public String nation;
	}
}
