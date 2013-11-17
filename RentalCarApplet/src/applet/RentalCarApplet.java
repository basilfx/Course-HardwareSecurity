/*
 * $Id: RentalCarApplet.java,v 0.1 2013/11/15 13:37:07$
 */
package applet;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

/**
 * Class RentalCarApplet.
 * 
 * @author Group ?
 * 
 * @version $Revision: 0.1 $
 */
public class RentalCarApplet extends Applet implements ISO7816 {
	private static final byte STATE_INIT = 0;
	private static final byte STATE_ISSUED = 1;

	/** CLA BYTES: values between 0xB0 and CF can be used */
	
	/** Issue Bytes */
	private static final byte CLA_ISSUE = (byte) 0xB1;
	private static final byte SET_PUBLIC_KEY_SIGNATURE = (byte) 0x01;
	private static final byte SET_PUBLIC_KEY_MODULUS_SC = (byte) 0x02;
	private static final byte SET_PUBLIC_KEY_EXPONENT_SC = (byte) 0x03;
	private static final byte SET_SC_ID = (byte) 0x04;
	private static final byte SET_PRVATE_KEY_MODULUS_SC = (byte) 0x05;
	private static final byte SET_PRIVATE_KEY_EXPONENT_SC = (byte) 0x06;
	private static final byte SET_PUBLIC_KEY_MODULUS_RT = (byte) 0x07;
	private static final byte SET_PUBLIC_KEY_EXPONENT_RT = (byte) 0x08;

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

	/** Start Bytes */
	private static final byte CLA_START = (byte) 0xB5;
	private static final byte SET_START_MILEAGE = (byte) 0x01;

	/** Stop Bytes */
	private static final byte CLA_STOP = (byte) 0xB6;
	private static final byte STOP_CAR = (byte) 0x01;
	private static final byte SET_FINAL_MILEAGE = (byte) 0x02;
	
	/** Keys Bytes */
	private static final byte CLA_KEYS = (byte) 0xB7;
	private static final byte KEYS_START = (byte) 0x01;
	private static final byte GET_PUBLIC_KEY_MODULUS = (byte) 0x02;
	private static final byte GET_PUBLIC_KEY_EXPONENT = (byte) 0x03;
	
	
	/** Temporary buffer in RAM. */
	byte[] tmp;

	/** The applet state (INIT or ISSUED). */
	byte state;

	/** Key for encryption. */
	RSAPublicKey pubKey;

	/** Key for decryption. */
	RSAPrivateKey privKey;

	/** Cipher for encryption and decryption. */
	Cipher cipher;

	public static void install(byte[] bArray, short bOffset, byte bLength) throws SystemException {
		new RentalCarApplet();
	}

	public RentalCarApplet() {
		tmp = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_RESET);
		pubKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, KeyBuilder.LENGTH_RSA_1024, false);
		privKey = (RSAPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, KeyBuilder.LENGTH_RSA_1024, false);
		cipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);
		state = STATE_INIT;
		register();
	}

	public void process(APDU apdu) {
		byte[] buf = apdu.getBuffer();
		byte ins = buf[OFFSET_INS];
		byte cla = buf[OFFSET_CLA];
		short lc = (short) (buf[OFFSET_LC] & 0x00FF);
		short outLength;

		if (selectingApplet()) {
			return;
		}

		switch (state) {
		case STATE_INIT:
			issue(ins);
			break;
		case STATE_ISSUED:
			switch (cla) {
			case CLA_ISSUE:
				issue(ins);
				break;
			case CLA_INIT:
				init(ins);
				break;
			case CLA_READ:
				read(ins);
				break;
			case CLA_RESET:
				reset(ins);
				break;
			case CLA_START:
				start(ins);
				break;
			case CLA_STOP:
				stop(ins);
				break;
			case CLA_KEYS:
				keys(ins);
				break;
			default:
				ISOException.throwIt(SW_INS_NOT_SUPPORTED);
			}
			break;
		default:
			ISOException.throwIt(SW_CONDITIONS_NOT_SATISFIED);
		}
	}

	/**
	 * Copies <code>length</code> bytes of data (starting at
	 * <code>OFFSET_CDATA</code>) from <code>apdu</code> to <code>dest</code>
	 * (starting at <code>offset</code>).
	 * 
	 * This method will set <code>apdu</code> to incoming.
	 * 
	 * @param apdu
	 *            the APDU.
	 * @param dest
	 *            destination byte array.
	 * @param offset
	 *            offset into the destination byte array.
	 * @param length
	 *            number of bytes to copy.
	 */
	private void readBuffer(APDU apdu, byte[] dest, short offset, short length) {
		byte[] buf = apdu.getBuffer();
		short readCount = apdu.setIncomingAndReceive();
		short i = 0;
		Util.arrayCopy(buf, OFFSET_CDATA, dest, offset, readCount);
		while ((short) (i + readCount) < length) {
			i += readCount;
			offset += readCount;
			readCount = (short) apdu.receiveBytes(OFFSET_CDATA);
			Util.arrayCopy(buf, OFFSET_CDATA, dest, offset, readCount);
		}
	}

	private void issue(byte ins) {
		switch (ins) {
		case SET_PUBLIC_KEY_SIGNATURE:
			// store signature
			break;
		case SET_PUBLIC_KEY_MODULUS_SC:
			// store key modulus
			break;
		case SET_PUBLIC_KEY_EXPONENT_SC:
			// store key exponent
			break;
		case SET_SC_ID:
			// store sc_id
			break;
		case SET_PRVATE_KEY_MODULUS_SC:
			// store key modulus
			break;
		case SET_PRIVATE_KEY_EXPONENT_SC:
			// store key exponent
		case SET_PUBLIC_KEY_MODULUS_RT:
			// store key modulus
			break;
		case SET_PUBLIC_KEY_EXPONENT_RT:
			// store key exponent
			// state = issued
			break;
		default:
			ISOException.throwIt(SW_INS_NOT_SUPPORTED);
		}
	}

	private void init(byte ins) {
		switch (ins) {
		case INIT_START:
			// decrypt nonce with private_key_sc
			// send decrypted nonce
			break;

		case INIT_AUTHENTICATED:
			// generate nonce
			// store nonce
			// send nonce
			break;
		case INIT_SECOND_NONCE:
			// if received_nonce == stored_nonce continue, else exception
			break;
		case INIT_SET_SIGNED_CAR_KEY_MODULUS:
			// store car key modulus
			break;
		case INIT_SET_SIGNED_CAR_KEY_EXPONENT:
			// store car key exponent
		case INIT_SET_SIGNED_ENCRYPTED_CAR_DATA:
			// store car data
			break;
		default:
			ISOException.throwIt(SW_INS_NOT_SUPPORTED);
		}
	}

	private void read(byte ins) {
		switch (ins) {
		case READ_MILEAGE_SIGNED_NONCE:
			// decrypt nonce with private_key_sc
			// send nonce
			break;
		case READ_MILEAGE_START_MILEAGE:
			// send start mileage
			break;
		case READ_MILEAGE_FINAL_MILEAGE:
			// send final mileage
			break;
		default:
			ISOException.throwIt(SW_INS_NOT_SUPPORTED);
		}
	}

	private void reset(byte ins) {
		switch (ins) {
		case RESET_CARD:
			// started = false
			// final_mileage = 0
			// start_mileage = 0
			// public_key_ct = 0
			// car_data = 0
			break;
		default:
			ISOException.throwIt(SW_INS_NOT_SUPPORTED);
		}
	}

	private void start(byte ins) {
		switch (ins) {
		case SET_START_MILEAGE:
			// started = true
			// if start_mileage == 0, start_mileage = received_start_mileage
			// send encrypt(public_key_sc, {nonce + car_data})
			break;
		default:
			ISOException.throwIt(SW_INS_NOT_SUPPORTED);
		}
	}

	private void stop(byte ins) {
		switch (ins) {
		case STOP_CAR:
			// generate new nonce
			// store nonce
			// send nonce
			break;
		case SET_FINAL_MILEAGE:
			// decrypt(public_key_sc {nonce + final_mileage})
			// if received_nonce == stored_nonce continue, else exception
			// store final_mileage
			break;
		default:
			ISOException.throwIt(SW_INS_NOT_SUPPORTED);
		}
	}
	
	private void keys(byte ins){
		switch (ins) {
		case KEYS_START:
			// send signature
			break;
		case GET_PUBLIC_KEY_MODULUS:
			// send pubkey modulus
		case GET_PUBLIC_KEY_EXPONENT:
			// send pubkey exponent
		default:
			ISOException.throwIt(SW_INS_NOT_SUPPORTED);
		}
	}
}
