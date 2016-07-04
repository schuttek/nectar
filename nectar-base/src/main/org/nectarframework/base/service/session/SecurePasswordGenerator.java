package org.nectarframework.base.service.session;

import org.nectarframework.base.tools.StringTools;

public class SecurePasswordGenerator {
	
	public static void main(String args[]) {
		
		System.out.println(StringTools.randomPassword(16));
				
	}

}
