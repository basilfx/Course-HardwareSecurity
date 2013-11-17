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

import encryption.RSAHandler;

/**
 * Reception Terminal application.
 * 
 * @author	Group 3.
 */
public class ReceptionTerminal extends BaseTerminal {
	
	/** Init Bytes */
	private static final byte CLA_INIT = (byte) 0xB2;
	private static final byte INIT_START = (byte) 0x01;
	private static final byte INIT_AUTHENTICATED = (byte) 0x02;
	private static final byte INIT_SECOND_NONCE = (byte) 0x03;
	private static final byte INIT_SET_SIGNED_CAR_KEY_MODULUS = (byte) 0x04;
	private static final byte INIT_SET_SIGNED_CAR_KEY_EXPONENT = (byte) 0x05;
	private static final byte INIT_SET_SIGNED_ENCRYPTED_CAR_DATA = (byte) 0x06;

	/** Read Bytes */
	private static final byte CLA_READ = (byte) 0xB3;
	private static final byte READ_MILEAGE_SIGNED_NONCE = (byte) 0x01;
	private static final byte READ_MILEAGE_START_MILEAGE = (byte) 0x02;
	private static final byte READ_MILEAGE_FINAL_MILEAGE = (byte) 0x03;

	/** Reset Bytes */
	private static final byte CLA_RESET = (byte) 0xB4;
	private static final byte RESET_CARD = (byte) 0x01;


	// The card applet.
	CardChannel applet;
	
	RSAHandler rsaHandler;
	
	RSAPublicKey pubic_key_sc;
	RSAPublicKey public_key_ct;
	RSAPublicKey public_key_rt;
	RSAPrivateKey private_key_rt;

	/**
	 * Constructs the terminal application.
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public ReceptionTerminal() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		super();
		rsaHandler = new RSAHandler();
		public_key_ct = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_ct");
		public_key_rt = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");
		private_key_rt = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_rt");
	}
	
	void init() throws CardException {
		try {
			
			CommandAPDU capdu = new CommandAPDU(CLA_INIT, INIT_START, (byte) 0, (byte) 0);
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			byte[] signature = rapdu.getData();
			
			capdu = new CommandAPDU(CLA_INIT, INIT_SECOND_NONCE, (byte) 0, (byte) 0);
			rapdu = sendCommandAPDU(capdu);
			byte[] data = rapdu.getData();
			
			capdu = new CommandAPDU(CLA_INIT, INIT_SET_SIGNED_CAR_KEY_MODULUS, (byte) 0, (byte) 0);
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			
			capdu = new CommandAPDU(CLA_INIT, INIT_SET_SIGNED_CAR_KEY_EXPONENT, (byte) 0, (byte) 0);
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			
			capdu = new CommandAPDU(CLA_INIT, INIT_SET_SIGNED_ENCRYPTED_CAR_DATA, (byte) 0, (byte) 0);
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}
	
	void read() throws CardException {
		try {
			getKeys();
			CommandAPDU capdu = new CommandAPDU(CLA_READ, READ_MILEAGE_SIGNED_NONCE, (byte) 0, (byte) 0);
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			byte[] data = rapdu.getData();
			
			capdu = new CommandAPDU(CLA_READ, READ_MILEAGE_SIGNED_NONCE, (byte) 0, (byte) 0);
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			
			capdu = new CommandAPDU(CLA_READ, READ_MILEAGE_START_MILEAGE, (byte) 0, (byte) 0);
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			
			capdu = new CommandAPDU(CLA_READ, READ_MILEAGE_FINAL_MILEAGE, (byte) 0, (byte) 0);
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			
			data = rapdu.getData();
			
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}
	
	void reset() throws CardException {
		try {

			CommandAPDU capdu = new CommandAPDU(CLA_RESET, RESET_CARD, (byte) 0, (byte) 0);
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			byte[] data = rapdu.getData();
			
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}
	
	
}