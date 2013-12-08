package com.rental.terminal.encryption;

import java.io.*;

import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;

/**
 * Encryption of a message stored in file <code>plaintext</code> with the
 * public key stored in file <code>publickey</code>. Use RSAKeyGen to generate
 * the <code>publickey</code> and <code>privatekey</code> files.
 *
 * @see RSAKeyGen
 * @see RSADecrypt
 *
 * @version $Revision: 1.1 $
 */
public class RSAEncrypt
{
   /**
    * Reads the message in file <code>filename</code> and the public key in
    * file <code>publickey</code>, encrypts the message with public key and
    * writes it to file <code>ciphertext</code>.
    *
    * @param filename the name of the file containing the plaintext.
    */
   public RSAEncrypt(String inFileName, String outFileName) {
      try {
         /* Get the secret message from file. */
         FileInputStream plainfile = new FileInputStream(inFileName);
         byte[] plaintext = new byte[plainfile.available()];
         plainfile.read(plaintext);
         plainfile.close();

         /* Get the public key from file. */
         PublicKey publickey = readPublicKey("publickey");

         /* Create a cipher for encrypting. */
         Cipher encrypt_cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
         encrypt_cipher.init(Cipher.ENCRYPT_MODE, publickey);

         /* Encrypt the secret message and store in file. */
         byte[] ciphertext = encrypt_cipher.doFinal(plaintext);
         FileOutputStream cipherfile = new FileOutputStream(outFileName);
         cipherfile.write(ciphertext);
         cipherfile.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Reads the X.509 standard encoded RSA public key in <code>filename</code>.
    *
    * @param filename the name of the file with the RSA public key.
    *
    * @return the public key in <code>filename</code>.
    *
    * @throws Exception if something goes wrong.
    */
   public static PublicKey readPublicKey(String filename) throws Exception { 
      FileInputStream file = new FileInputStream(filename);
      byte[] bytes = new byte[file.available()];
      file.read(bytes);
      file.close();
      X509EncodedKeySpec pubspec = new X509EncodedKeySpec(bytes);
      KeyFactory factory = KeyFactory.getInstance("RSA");
      PublicKey pubkey = factory.generatePublic(pubspec);
      return pubkey;
   }

   /**
    * The main method just calls constructor.
    *
    * @param arg The command line arguments.
    */
   public static void main(String[] arg) {
      if (arg.length != 2) {
         System.err.println("Usage:  java RSAEncrypt <src file> <dest file>");
      } else {
         new RSAEncrypt(arg[0],arg[1]);
      }
   }
}

