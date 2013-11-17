package terminal;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigInteger;
import java.util.List;

import javax.crypto.Cipher;
import javax.swing.*;

import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;

import javax.smartcardio.*;

import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * Issuing Terminal application.
 * 
 * @author	Group 3.
 */
public class IssuingTerminal extends BaseTerminal {
	
	// CLA of this Terminal.
	static final byte CLA_TERMINAL_IS = (byte) 0x01;

	// Instructions.
	private static final byte INS_SET_PUB_MODULUS = (byte) 0x03;
	private static final byte INS_SET_PRIV_MODULUS = (byte) 0x12;
	private static final byte INS_SET_PRIV_EXP = (byte) 0x22;
	private static final byte INS_SET_PUB_EXP = (byte) 0x32;
	private static final byte INS_ISSUE = (byte) 0x40;
	private static final byte INS_ENCRYPT = (byte) 0xE0;
	private static final byte INS_DECRYPT = (byte) 0xD0;
	
	// The card applet.
	CardChannel applet;

	/**
	 * Constructs the terminal application.
	 */
	public IssuingTerminal() {
		super();
	}
	
	/**
	 * Issues the card.
	 */
	public void issueCard() throws Exception {
		//
		// First create the certificate of the public key of the SC (signed with the private key of the RT).
		//
		
		// Obtain the private key of the RT by reading the key file.
		try {
			byte[] data = readFile("keys/private_key_rt");
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(data);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			RSAPrivateKey RTPrivateKey = (RSAPrivateKey) factory.generatePrivate(spec);
			
			try {
				byte[] dataPk = readFile("keys/public_key_rt");
				X509EncodedKeySpec specPk = new X509EncodedKeySpec(dataPk);
				KeyFactory factoryPk = KeyFactory.getInstance("RSA");
				RSAPublicKey RTPublicKey = (RSAPublicKey) factoryPk.generatePublic(specPk);
				
				try {
					// Test encryption/decryption.
					String testString = "test";
					byte[] testStringByteArray = testString.getBytes();
					
					Cipher encrypt_cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
					encrypt_cipher.init(Cipher.ENCRYPT_MODE, RTPublicKey);
			        byte[] encryptedTestStringByteArray = encrypt_cipher.doFinal(testStringByteArray);
			        
			        log("encryption: " + (new String(encryptedTestStringByteArray)));
			        
			        Cipher decrypt_cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			        decrypt_cipher.init(Cipher.DECRYPT_MODE, RTPrivateKey);			        
			        byte[] decryptedTestStringByteArray = decrypt_cipher.doFinal(encryptedTestStringByteArray);
			        
			        log("decryption: " + (new String(decryptedTestStringByteArray)));
			        
			        try {
			        	// Test sign/verify.
			        	Signature sig_sign = Signature.getInstance("SHA1withRSA");
			        	sig_sign.initSign(RTPrivateKey);
			        	sig_sign.update(testStringByteArray);
			        	byte[] signature = sig_sign.sign();
			        	
			        	log("signature: " + (new String(signature)));
			        	
			        	Signature sig_verify = Signature.getInstance("SHA1withRSA");
			        	sig_verify.initVerify(RTPublicKey);
			        	sig_verify.update(testStringByteArray);
			        	boolean verified = sig_verify.verify(signature);
			        	
			        	log("signature verified: " + verified);
			        }
			        catch (Exception e) {
			        	log("[Error] IssuingTerminal: Exception when trying to sign (" + e.getMessage() + ")");
			        }
				}
				catch (Exception e) {
					log("[Error] IssuingTerminal: Exception when trying to encrypt/decrypt (" + e.getMessage() + ")");
				}
			}
			catch (Exception e) {
				log("[Error] IssuingTerminal: Exception when trying to read public key file (" + e.getMessage() + ")");
			}
		}
		catch (Exception e) {
			log("[Error] IssuingTerminal: Exception when trying to read private key file (" + e.getMessage() + ")");
		}
	}

	/**
	 * Handles 'set pub key' button event.
	 * 
	 * @throws CardException
	 *             if something goes wrong.
	 */
	void setPubKey() throws CardException {
		try {
			byte[] data = readFile("");
			X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			RSAPublicKey key = (RSAPublicKey) factory.generatePublic(spec);

			byte[] modulus = getBytes(key.getModulus());

			CommandAPDU capdu;
			capdu = new CommandAPDU(CLA_TERMINAL_IS, INS_SET_PUB_MODULUS, (byte) 0,
					(byte) 0, modulus);
			sendCommandAPDU(capdu);

			byte[] exponent = getBytes(key.getPublicExponent());
			capdu = new CommandAPDU(CLA_TERMINAL_IS, INS_SET_PUB_EXP, (byte) 0,
					(byte) 0, exponent);
			sendCommandAPDU(capdu);
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}

	/**
	 * Handles 'set priv key' button event.
	 * 
	 * @throws CardException
	 *             if something goes wrong.
	 */
	void setPrivKey() throws CardException {
		try {
			byte[] data = readFile("");
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(data);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			RSAPrivateKey key = (RSAPrivateKey) factory.generatePrivate(spec);

			byte[] modulus = getBytes(key.getModulus());
			CommandAPDU capdu;
			capdu = new CommandAPDU(CLA_TERMINAL_IS, INS_SET_PRIV_MODULUS, (byte) 0,
					(byte) 0, modulus);
			sendCommandAPDU(capdu);

			byte[] exponent = getBytes(key.getPrivateExponent());
			capdu = new CommandAPDU(CLA_TERMINAL_IS, INS_SET_PRIV_EXP, (byte) 0,
					(byte) 0, exponent);
			sendCommandAPDU(capdu);

		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}

	/**
	 * Handles 'issue' button event.
	 * 
	 * @throws CardException
	 *             if something goes wrong.
	 */
	void issue() throws CardException {
		try {
			CommandAPDU capdu = new CommandAPDU(CLA_TERMINAL_IS, INS_ISSUE,
					(byte) 0, (byte) 0);
			sendCommandAPDU(capdu);
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}

	/**
	 * Handles 'encrypt' button event.
	 * 
	 * @throws CardException
	 *             if something goes wrong.
	 */
	void encrypt() throws CardException {
		try {
			byte[] data = readFile("");
			if (data.length > BLOCKSIZE) {
				throw new CardException("File too large.");
			}
			CommandAPDU capdu = new CommandAPDU(CLA_TERMINAL_IS, INS_ENCRYPT,
					(byte) 0, (byte) 0, data, BLOCKSIZE);
			sendCommandAPDU(capdu);
		} catch (IOException e) {
			throw new CardException(e.getMessage());
		}
	}

	/**
	 * Handles 'decrypt' button event.
	 * 
	 * @throws CardException
	 *             if something goes wrong.
	 */
	void decrypt() throws Exception {
		try {
			byte[] data = readFile("");
			if (data.length > BLOCKSIZE) {
				throw new Exception("File too large.");
			}
			CommandAPDU capdu = new CommandAPDU(CLA_TERMINAL_IS, INS_DECRYPT,
					(byte) 0, (byte) 0, data, BLOCKSIZE);
			applet.transmit(capdu);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}
}