package com.rental.terminal.encryption;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Generate an RSA public/private keypair.
 *
 * @version $Revision: 1.1 $
 */
public class RSAKeyGen
{
   /**
    * Generates an RSA public/private key pair.
    */
   public RSAKeyGen(String public_key_filename, String private_key_filename) {
      try {
         /* Generate keypair. */
         System.out.println("Generating keys...");
         KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
         generator.initialize(1024);
         KeyPair keypair = generator.generateKeyPair();
         RSAPublicKey publickey = (RSAPublicKey)keypair.getPublic();
         RSAPrivateKey privatekey = (RSAPrivateKey)keypair.getPrivate();

         /* Write public key to file. */
         writeKey(publickey, public_key_filename);

         /* Write private key to file. */
         writeKey(privatekey, private_key_filename);

         System.out.println("modulus = " + publickey.getModulus());
         System.out.println("pubexpint = " + publickey.getPublicExponent());
         System.out.println("privexpint = " + privatekey.getPrivateExponent());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Writes <code>key</code> to file with name <code>filename</code> in
    * standard encoding (X.509 for RSA public key, PKCS#8 for RSA private key).
    *
    * @param key the key to write.
    * @param filename the name of the file.
    *
    * @throws IOException if something goes wrong.
    */
   public static void writeKey(Key key, String filename) throws IOException {
      FileOutputStream file = new FileOutputStream(filename);
      file.write(key.getEncoded());
      file.close();
   }

   /**
    * The main method just calls the constructor.
    *
    * @param arg The command line arguments.
    */
}

