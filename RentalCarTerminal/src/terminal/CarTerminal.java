package terminal;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;

import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;

import javax.smartcardio.*;

import encryption.RSAHandler;

/**
 * Sample terminal for the Crypto applet.
 * 
 * @author Martijn Oostdijk (martijno@cs.kun.nl)
 * @author Joeri de Ruiter (joeri@cs.ru.nl)
 * 
 * @version $Revision: 2.0 $
 */
public class CarTerminal {
	static final int BLOCKSIZE = 128;

	static final String TITLE = "Crypto Terminal";
	static final int DISPLAY_WIDTH = 30;
	static final int DISPLAY_HEIGHT = 20;

	static final String MSG_ERROR = "Error";
	static final String MSG_INVALID = "Invalid";

	static final byte[] APPLET_AID = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x01 };
	static final byte CLA_ISO = (byte) 0x00;
	static final byte INS_SELECT = (byte) 0xA4;
	static final short SW_NO_ERROR = (short) 0x9000;
	static final short SW_APPLET_SELECT_FAILED = (short) 0x6999;
	static final short SW_FILE_NOT_FOUND = (short) 0x6A82;

	static final CommandAPDU SELECT_APDU = new CommandAPDU((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, APPLET_AID);

	/** Start Bytes */
	private static final byte CLA_START = (byte) 0xB5;
	private static final byte START_CAR = (byte) 0x01;
	private static final byte GET_PUBLIC_KEY_MODULUS = (byte) 0x02;
	private static final byte GET_PUBLIC_KEY_EXPONENT = (byte) 0x03;
	private static final byte SET_START_MILEAGE = (byte) 0x04;

	/** Stop Bytes */
	private static final byte CLA_STOP = (byte) 0xB6;
	private static final byte STOP_CAR = (byte) 0x01;
	private static final byte SET_FINAL_MILEAGE = (byte) 0x02;

	private static final int STATE_INIT = 0;
	private static final int STATE_ISSUED = 1;

	/** The card applet. */
	CardChannel applet;

	
	/** Car terminal data */
	short tempNonce;
	boolean car_may_start = false;
	int mileage;
	byte[] current_smartcard_signature;
	RSAHandler rsaHandler;
	RSAPublicKey pubic_key_sc;
	RSAPublicKey public_key_ct;
	RSAPublicKey public_key_rt;
	RSAPrivateKey private_key_ct;
	

	/**
	 * Constructs the terminal application.
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public CarTerminal() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		tempNonce = 0;
		rsaHandler = new RSAHandler();
		public_key_ct = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_ct");
		public_key_rt = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");
		private_key_ct = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_ct");
		
		setEnabled(false);
		(new CardThread()).start();
	}

	/**
	 * Enables/disables the buttons.
	 * 
	 * @param b
	 *            boolean indicating whether to enable or disable the buttons.
	 */
	public void setEnabled(boolean b) {

	}

	class CardThread extends Thread {
		public void run() {
			try {
				TerminalFactory tf = TerminalFactory.getDefault();
				CardTerminals ct = tf.terminals();
				List<CardTerminal> cs = ct.list(CardTerminals.State.CARD_PRESENT);
				if (cs.isEmpty()) {
					log("No terminals with a card found.");
					return;
				}

				while (true) {
					try {
						for (CardTerminal c : cs) {
							if (c.isCardPresent()) {
								try {
									Card card = c.connect("*");
									try {
										applet = card.getBasicChannel();
										ResponseAPDU resp = applet.transmit(SELECT_APDU);
										if (resp.getSW() != 0x9000) {
											throw new Exception("Select failed");
										}
										setEnabled(true);

										// Wait for the card to be removed
										while (c.isCardPresent())
											;
										setEnabled(false);
										break;
									} catch (Exception e) {
										log("Card does not contain CryptoApplet?!");
										sleep(2000);
										continue;
									}
								} catch (CardException e) {
									log("Couldn't connect to card!");
									sleep(2000);
									continue;
								}
							} else {
								log("No card present!");
								sleep(2000);
								continue;
							}
						}
					} catch (CardException e) {
						log("Card status problem!");
					}
				}
			} catch (Exception e) {
				setEnabled(false);
				log("ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	void startCar() throws CardException {
		try {

			CommandAPDU capdu = new CommandAPDU(CLA_START, START_CAR, (byte) 0, (byte) 0);
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			current_smartcard_signature = rapdu.getData();
			
			capdu = new CommandAPDU(CLA_START, GET_PUBLIC_KEY_MODULUS, (byte) 0, (byte) 0);
			rapdu = sendCommandAPDU(capdu);
			byte[] modulus = rapdu.getData();
			
			capdu = new CommandAPDU(CLA_START, GET_PUBLIC_KEY_EXPONENT, (byte) 0, (byte) 0);
			rapdu = sendCommandAPDU(capdu);
			byte[] exponent = rapdu.getData();
			pubic_key_sc = rsaHandler.getPublicKeyFromModulusExponent(modulus, exponent);
			
			capdu = new CommandAPDU(CLA_START, SET_START_MILEAGE, (byte) 0, (byte) 0, getStartMileage());
			rapdu = sendCommandAPDU(capdu);
			short return_nonce = checkCarData(rapdu.getData());
			
			if (return_nonce == tempNonce) {
				car_may_start = true;
			}
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}

	void stopCar() throws CardException {
		try {

			CommandAPDU capdu = new CommandAPDU(CLA_STOP, STOP_CAR, (byte) 0, (byte) 0);
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			byte[] data = rapdu.getData();
			Short nonce = bytes2short(data[0], data[1]);
			
			capdu = new CommandAPDU(CLA_STOP, SET_FINAL_MILEAGE, (byte) 0, (byte) 0, getFinalMileage(nonce));
			rapdu = sendCommandAPDU(capdu);
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}

	byte[] getStartMileage() {
		tempNonce++;
		return null;
	}
	
	byte[] getFinalMileage(short nonce) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		byte[] b_nonce = short2bytes(nonce);
		byte[] final_mileage = int2bytes(mileage);
		byte[] data = mergeByteArrays(b_nonce, final_mileage);
		return rsaHandler.encrypt(pubic_key_sc, data);
	}
	//TODO check if data is actually correct
	short checkCarData(byte[] data) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		byte[] decrypted_data = rsaHandler.decrypt(private_key_ct, data);
		short nonce = bytes2short(decrypted_data[0], decrypted_data[1]);
		short decrypted_car_id = bytes2short(decrypted_data[2], decrypted_data[3]);
		short day = bytes2short(decrypted_data[4], decrypted_data[5]);
		short month = bytes2short(decrypted_data[6], decrypted_data[7]);
		short year = bytes2short(decrypted_data[8], decrypted_data[9]);
		short sc_id = bytes2short(decrypted_data[10], decrypted_data[11]);
		return nonce;
	}

	/**
	 * Sends a command to the card.
	 * 
	 * @param capdu
	 *            the command to send.
	 * 
	 * @return the response from the card.
	 * 
	 * @throws CardTerminalException
	 *             if something goes wrong.
	 */
	ResponseAPDU sendCommandAPDU(CommandAPDU capdu) throws CardException {
		log(capdu);
		ResponseAPDU rapdu = applet.transmit(capdu);
		log(rapdu);
		return rapdu;
	}

	String toHexString(byte[] in) {
		StringBuilder out = new StringBuilder(2 * in.length);
		for (int i = 0; i < in.length; i++) {
			out.append(String.format("%02x ", (in[i] & 0xFF)));
		}
		return out.toString().toUpperCase();
	}

	/**
	 * Writes <code>obj</code> to the log.
	 * 
	 * @param obj
	 *            the message to write to the log.
	 */
	void log(ResponseAPDU obj) {
		System.out.println(obj.toString());
	}

	/**
	 * Writes <code>obj</code> to the log.
	 * 
	 * @param obj
	 *            the message to write to the log.
	 */
	void log(CommandAPDU obj) {
		System.out.println(obj.toString());
	}

	/**
	 * Writes <code>obj</code> to the log.
	 * 
	 * @param obj
	 *            the message to write to the log.
	 */
	void log(Object obj) {
		System.out.println(obj.toString());
	}

	/**
	 * Gets an unsigned byte array representation of <code>big</code>. A leading
	 * zero (present only to hold sign bit) is stripped.
	 * 
	 * @param big
	 *            a big integer.
	 * 
	 * @return a byte array containing a representation of <code>big</code>.
	 */
	byte[] getBytes(BigInteger big) {
		byte[] data = big.toByteArray();
		if (data[0] == 0) {
			byte[] tmp = data;
			data = new byte[tmp.length - 1];
			System.arraycopy(tmp, 1, data, 0, tmp.length - 1);
		}
		return data;
	}

	/**
	 * Creates an instance of this class and puts it inside a frame.
	 * 
	 * @param arg
	 *            command line arguments.
	 */
	public static void main(String[] arg) {

	}
	
	public static short bytes2short(byte first_byte, byte second_byte)
	 {
	    return (short)((first_byte<<8) | (second_byte));
	 } 
	
	public static byte[] short2bytes(short s){
		return ByteBuffer.allocate(2).putInt(s).array();
	}
	
	public static byte[] int2bytes(int i){
		return ByteBuffer.allocate(4).putInt(i).array();
	}
	
	public static byte[] mergeByteArrays(byte[] first, byte[] second){
		byte[] result = new byte[first.length + second.length];
		for (int i = 0; i < first.length; i++){
			result[i] = first[i];
		}
		for (int i = first.length; i < first.length + second.length; i++){
			result[i + first.length] = second[i];
		}		
		return result;
	}
}