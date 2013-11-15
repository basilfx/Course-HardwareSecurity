package terminal;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigInteger;
import java.util.List;
import javax.swing.*;

import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;

import javax.smartcardio.*;

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
	 * Handles 'set pub key' button event.
	 * 
	 * @throws CardException
	 *             if something goes wrong.
	 */
	void setPubKey() throws CardException {
		try {
			byte[] data = readFile();
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
			byte[] data = readFile();
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
			byte[] data = readFile();
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
			byte[] data = readFile();
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