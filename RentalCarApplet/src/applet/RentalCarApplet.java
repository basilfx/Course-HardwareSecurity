/*
 * $Id: RentalCarApplet.java,v 0.1 2013/11/15 13:37:07$
 */
package applet;

import org.globalplatform.SecureChannel;

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
	private static final byte INIT_CHECK_CAR_KEY_SIGNATURE = (byte) 0x06;
	private static final byte INIT_SET_SIGNED_ENCRYPTED_CAR_DATA = (byte) 0x07;

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
	
	private static final short BLOCKSIZE = (short) 128;
	private static final short SCIDSIZE = (short) 2;
	private static final short NONCESIZE = (short) 2;
	
	/** Exceptions */
	private static final short NONCE_FAILURE = (short) 37000;
	
	// Temporary buffer in RAM.
	byte[] tmp;

	// The applet state (INIT or ISSUED).
	byte state;
	
	// Is used to send a unique nonce, increment before use!
	short nonce;
	
	// Cipher for encryption and decryption.
	Cipher cipher;

	// privkey_sc.
	RSAPrivateKey privKeySC;
	
	// pubkey_sc.
	RSAPublicKey pubKeySC;

	// pubkey_rt.
	RSAPublicKey pubKeyRT;
	
	// pubkey_ct.
	RSAPublicKey pubKeyCT;
	
	// sc_id.
	private short cardId;
	
	// {|sc_id, pubkey_sc|}privkey_rt.
	private byte[] signatureRT;

	public static void install(byte[] bArray, short bOffset, byte bLength) throws SystemException {
		new RentalCarApplet();
	}

	public RentalCarApplet() {
		tmp = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_RESET);
		cipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);
		
		// Create instances of keys.
		privKeySC = (RSAPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, KeyBuilder.LENGTH_RSA_1024, false);
		pubKeySC = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, KeyBuilder.LENGTH_RSA_1024, false);
		pubKeyRT = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, KeyBuilder.LENGTH_RSA_1024, false);
		pubKeyCT = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, KeyBuilder.LENGTH_RSA_1024, false);
		
		// Initialize signature.
		signatureRT = new byte[128];
		
		// Initialize state.
		state = STATE_INIT;
		
		//Set nonce to 0
		nonce = 0;
		
		// Register this applet instance with the JCRE.
		register();
	}

	public void process(APDU apdu) {
		byte[] buf = apdu.getBuffer();
		byte ins = buf[OFFSET_INS];
		byte cla = buf[OFFSET_CLA];
		short lc = (short) (buf[OFFSET_LC] & 0x00FF);

		if (selectingApplet()) {
			return;
		}
		
		readBuffer(apdu, tmp, (short)0, lc);

		switch (state) {
			case STATE_INIT:
				issue(apdu, ins, lc);
				break;
			case STATE_ISSUED:
				switch (cla) {
					case CLA_ISSUE:
						issue(apdu, ins, lc);
						break;
					case CLA_INIT:
						init(apdu, ins, lc);
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
						keys(apdu, ins, lc);
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

	private void issue(APDU apdu, byte ins, short lc) {
		
		byte[] buf = apdu.getBuffer();
		
		switch (ins) {
			case SET_PUBLIC_KEY_SIGNATURE:
				// Store the signature of the RT.
				Util.arrayCopy(tmp, (short) 0, signatureRT, (short) 0, lc);
				apdu.setOutgoing();//Volgensmij verwijderd deze methode een deel van de buffer, dus eerst deze methode aanroepen, dan de buffer aanpassen.
				// Send signature as a response for debugging info.
				buf = signatureRT;//Ik weet niet of dit wel mag, het lijkt me beter om Util.arrayCopy te gebruiken
				apdu.sendBytes((short)0,(short)128);
				
				break;
			case SET_PUBLIC_KEY_MODULUS_SC:
				// Store the modulus of the public key of the SC.
                pubKeySC.setModulus(tmp, (short) 0, lc);
				break;
			case SET_PUBLIC_KEY_EXPONENT_SC:
				// Store the exponent of the public key of the SC.
                pubKeySC.setExponent(tmp, (short) 0, lc);
				break;
			case SET_SC_ID:
				// Store the ID of the SC.
				cardId = Util.getShort(tmp, (short)0);
				
				// Send cardId as a response for debugging info.
				buf[0] = (byte) ((cardId >> 8) & 0xff);
				buf[1] = (byte) (cardId & 0xff);
				apdu.setOutgoingAndSend((short) 0, (short) 2);
				
				break;
			case SET_PRVATE_KEY_MODULUS_SC:
				// Store the modulus of the private key of the SC.
                privKeySC.setModulus(tmp, (short) 0, lc);
				break;
			case SET_PRIVATE_KEY_EXPONENT_SC:
				// Store the exponent of the private key of the SC.
                privKeySC.setExponent(tmp, (short) 0, lc);
			case SET_PUBLIC_KEY_MODULUS_RT:
				// Store the modulus of the public key of the RT.
                pubKeyRT.setModulus(tmp, (short) 0, lc);
				break;
			case SET_PUBLIC_KEY_EXPONENT_RT:
				// Store the modulus of the public key of the RT.
                pubKeyRT.setExponent(tmp, (short) 0, lc);
                
                // Set state to "Issued".
                state = STATE_ISSUED;
                
				break;
			default:
				ISOException.throwIt(SW_INS_NOT_SUPPORTED);
		}
	}

	private void init(APDU apdu, byte ins, short lc) {
		
		byte[] buf = apdu.getBuffer();
		
		switch (ins) {
		case INIT_START:
			Util.arrayCopy(buf, (short) 0, tmp, (short) 0, lc);
			apdu.setOutgoing();
			cipher.init(privKeySC,Cipher.MODE_DECRYPT);
            cipher.doFinal(tmp,(short)0,(short)2,buf,(short)0);
            apdu.sendBytes((short)0,NONCESIZE);
			break;

		case INIT_AUTHENTICATED:
			nonce++;
			apdu.setOutgoing();
			Util.setShort(tmp, (short) 0, nonce);
			cipher.init(pubKeyRT,Cipher.MODE_ENCRYPT);
			cipher.doFinal(tmp,(short)0,(short)2,buf,(short)0);
			apdu.sendBytes((short)0,NONCESIZE);
			break;
		case INIT_SECOND_NONCE:
			short received_nonce = Util.getShort(buf, (short)0);
			if (received_nonce != nonce){
				ISOException.throwIt(NONCE_FAILURE);
			}
			break;
		case INIT_SET_SIGNED_CAR_KEY_MODULUS:
			//Clear the key

			//Store the length of the modulus as the first 2 bytes in the tmp array
			Util.setShort(tmp,(short) 0 , lc);
			//Store the modulus in tmp
			Util.arrayCopy(buf, (short) 0, tmp, (short) 2, lc);
			break;
		case INIT_SET_SIGNED_CAR_KEY_EXPONENT:
			//Get the length of the modulus as the first 2 bytes in the tmp array
			short length = Util.getShort(tmp, (short)0);
			//Set the length of the exponent as the first 2 bytes after the modulus
			Util.setShort(tmp,(short)(2 + length), lc);
			//Store the exponent in tmp
			Util.arrayCopy(buf, (short) 0, tmp, (short)(4 + length), lc);			
		case INIT_CHECK_CAR_KEY_SIGNATURE:
			short modulus_length = Util.getShort(tmp, (short)0);
			short exponent_length = Util.getShort(tmp, (short)(modulus_length + 2));
			//Check signature
			pubKeyCT.setModulus(tmp, (short)2, modulus_length);
			pubKeyCT.setExponent(tmp, (short)(4 + modulus_length), exponent_length);
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
	
	private void keys(APDU apdu, byte ins, short lc){
		
		byte[] buf = apdu.getBuffer();
		
		switch (ins) {
		case KEYS_START:
			// send SC_ID + signature
			apdu.setOutgoing();
			buf[0] = (byte) ((cardId >> 8) & 0xff);
			buf[1] = (byte) (cardId & 0xff);
			Util.arrayCopy(signatureRT, (short) 0, buf, (short) 2, lc);			
			apdu.sendBytes((short)0, (short) (SCIDSIZE + BLOCKSIZE));
			break;
		case GET_PUBLIC_KEY_MODULUS:
			// send pubkey modulus
			apdu.setOutgoing();
			pubKeySC.getModulus(buf, (short) 0);
			apdu.sendBytes((short)0,BLOCKSIZE);
			break;
		case GET_PUBLIC_KEY_EXPONENT:
			// send pubkey exponent
			apdu.setOutgoing();
			pubKeySC.getExponent(buf, (short) 0);
			apdu.sendBytes((short)0,BLOCKSIZE);
			break;
		default:
			ISOException.throwIt(SW_INS_NOT_SUPPORTED);
		}
	}
}
