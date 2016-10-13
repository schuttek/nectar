/**
 * 
 */
package org.nectarframework.junit.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlClientService;
import org.nectarframework.base.service.xml.XmlResponse;
import org.nectarframework.base.service.xml.XmlResponseHandler;
import org.nectarframework.base.service.xml.XmlService;
import org.nectarframework.base.tools.RandUtils;
import org.nectarframework.base.tools.StringTools;

/**
 * @author skander
 *
 */
public class XmlServiceTest {

	@Test
	public void test() {
	}

	private class EchoCheck implements XmlResponseHandler {
		public void handleResponse(XmlResponse xmlResponse) {
			if (xmlResponse.getRequest().getChildren().getFirst()
					.equals(xmlResponse.getResponse().getChildren().getFirst())) {
				// Log.trace("Request " + xmlResponse.getId() + " VALID!");
			} else {
				// Log.warn("Request " + xmlResponse.getId() + " FAIL!!!!");
			}

			// Element e = XmlService.generateTestElement(5, 5, 5);

			// try {
			// ((XmlClientService)
			// ServiceRegister.getServiceByClassName(XmlClientService.class.getName())).sendRequest("echo",
			// e, new EchoCheck());
			// } catch (IOException e1) {
			// Log.warn(e1);
			// }
		}
	}

	public static Element generateTestElement(int childCount, int attrCount, int depth) {
		Element e = new Element(RandUtils.nextPlainStringLowerCase(4, 8));
		Random rand = new Random();
		int n = rand.nextInt(attrCount - 1) + 1;
		for (int t = 0; t < n; t++) {
			e.add(RandUtils.nextPlainStringLowerCase(4, 8), RandUtils.nextPlainStringLowerCase(4, 8));
		}
		if (depth > 0) {
			n = rand.nextInt(childCount - 1) + 1;
			for (int t = 0; t < n; t++) {
				e.add(generateTestElement(childCount, attrCount, depth - 1));
			}
		}

		return e;
	}

	public void runTests() throws IOException {
		Element e = generateTestElement(5, 5, 5);
		byte[] crazyArray = new byte[256];
		int t = 0;
		for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++, t++) {
			crazyArray[t] = b;
		}

		e.addBinary("crazyArray", crazyArray);
		// Log.trace("XmlClientService Test#1 ready");
		getXmlClientService().sendRequest("echo", e, new EchoCheck());
		// Log.trace("XmlClientService Test#1 sent");

		for (int i = 0; i < 0; i++) {
			e = generateTestElement(5, 5, 5);
			getXmlClientService().sendRequest("echo", e, new EchoCheck());
			// Log.trace("XmlClientService Test #" + (i + 2) + " sent");

		}
	}

	XmlClientService getXmlClientService() {
		// FIXME
		return null;
	}
}
