package org.nectarframework.base.service.xml;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.xerces.impl.dv.util.Base64;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.tools.Stopwatch;

public class RSAKeyGenerator {

	public static void main(String[] args) {

		// TODO this is just sample code...

		try {

			// server side
			// the below should happen on the server, when the service is
			// starting
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			Stopwatch sw = new Stopwatch();
			sw.start();
			keyPairGenerator.initialize(4096);
			KeyPair keyPair = keyPairGenerator.genKeyPair();
			sw.stop();
			System.out.println("RSA 4096 generation: " + sw.toString());

			
			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
			String pub = Base64.encode(x509EncodedKeySpec.getEncoded());

			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
			
			String priv = Base64.encode(pkcs8EncodedKeySpec.getEncoded());

			System.out.println(pub);
			System.out.println(priv);

		} catch (NoSuchAlgorithmException e) {
			Log.fatal(e);
		}
	}

}
