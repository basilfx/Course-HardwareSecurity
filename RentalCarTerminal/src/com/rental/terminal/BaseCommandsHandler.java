package com.rental.terminal;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import com.rental.terminal.encryption.RSAHandler;


/**
 * @author Jelte & Erwin.
 *
 */
public class BaseCommandsHandler {
	
	/** Keys Bytes */
	private static final byte CLA_KEYS = (byte) 0xB7;
	private static final byte KEYS_START = (byte) 0x01;
	private static final byte GET_PUBLIC_KEY_MODULUS = (byte) 0x02;
	private static final byte GET_PUBLIC_KEY_EXPONENT = (byte) 0x03;

	protected static final int BLOCKSIZE = 128;
	protected static final int NONCESIZE = 6;
	protected static final int SCIDSIZE = 2;
	protected static final int MILEAGESIZE = 4;
	
	protected RSAHandler rsaHandler;
	
	protected byte[] nonce;	
	
	// Cipher used for encryption/decryption.
	protected Cipher cipher;
	
	protected Terminal terminal;
	
	protected RSAPublicKey public_key_rt;
	
	/** Exception classes */
	public class TerminalNonceMismatchException extends Exception {
		public TerminalNonceMismatchException(String message) {
			super(message);
		}
		
		public TerminalNonceMismatchException(String message, Throwable throwable) {
			super(message, throwable);
		}
	}
	
	public BaseCommandsHandler(Terminal terminal) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException{
		rsaHandler = new RSAHandler();
		nonce = new byte[NONCESIZE];
		this.terminal = terminal;
		public_key_rt = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");

	}

	/**
	 * Obtains the keys, id and signature from the smartcard and verifies them.
	 * @param currentSmartcard - A Smart Card instance. 
	 * This method will set the public key, the smartcard id and the signature of this instance.
	 * @ensure currentSmartcard.getPublickey() == public key present on physical smartcard
	 * @ensure currentSmartcard.getId() == the id of the physical smartcard
	 * @ensure currentSmartcard.getSignature == the signature present on the physical smartcard
	 * @throws CardException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public void getKeys(Smartcard currentSmartcard) throws CardException, NoSuchAlgorithmException, InvalidKeySpecException {
		CommandAPDU capdu = new CommandAPDU(CLA_KEYS, KEYS_START, (byte) 0, (byte) 0, SCIDSIZE + BLOCKSIZE);
		ResponseAPDU rapdu = terminal.sendCommandAPDU(capdu);
		
		byte[] data = rapdu.getData();
		
		JCUtil.log("Card ID has been read by getKeys(): " + Short.toString(JCUtil.bytesToShort(data[0], data[1])));
		
		// The first two bytes of the response represent the SC ID.
		currentSmartcard.setScId(JCUtil.bytesToShort(data[0], data[1]));
		
		// The second to 130th byte represent the signature.
		currentSmartcard.setSignature(JCUtil.subArray(data, 2, BLOCKSIZE));
		

		capdu = new CommandAPDU(CLA_KEYS, GET_PUBLIC_KEY_MODULUS, (byte) 0, (byte) 0, BLOCKSIZE);
		rapdu = terminal.sendCommandAPDU(capdu);
		byte[] modulus = rapdu.getData();
		JCUtil.log("received pubkey_sc modulus: " + new String(modulus));

		// TODO: Note that the expected response is hard-coded. It would be better to first obtain the exponent length from the SC.
		capdu = new CommandAPDU(CLA_KEYS, GET_PUBLIC_KEY_EXPONENT, (byte) 0, (byte) 0, (short) 3);
		rapdu = terminal.sendCommandAPDU(capdu);
		byte[] exponent = rapdu.getData();
		
		JCUtil.log("received pubkey_sc exponent: " + new String(exponent));
		
		// Because the SC sends unsigned byte arrays, we convert them to signed byte arrays for the BigInteger conversion.
		currentSmartcard.setPublicKey(rsaHandler.getPublicKeyFromModulusExponent(JCUtil.unsignedToSigned(modulus), JCUtil.unsignedToSigned(exponent)));
		
		JCUtil.log("pubkey_sc has been read by getKeys(). Modulus: " + currentSmartcard.getPublicKey().getModulus().toString() + ", exponent: " + currentSmartcard.getPublicKey().getPublicExponent().toString());
	}
	
	/**
	 * Assigns a random value to the nonce property of this class.
	 */
	protected void randomizeNonce() {
		SecureRandom random = new SecureRandom();
		random.nextBytes(nonce);
	}
}
