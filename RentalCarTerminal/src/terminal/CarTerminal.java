package terminal;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * Car terminal for the Rental Car applet.
 * 
 */
public class CarTerminal extends BaseTerminal{

	/** Start Bytes */
	private static final byte CLA_START = (byte) 0xB5;
	private static final byte SET_START_MILEAGE = (byte) 0x01;

	/** Stop Bytes */
	private static final byte CLA_STOP = (byte) 0xB6;
	private static final byte STOP_CAR = (byte) 0x01;
	private static final byte SET_FINAL_MILEAGE = (byte) 0x02;

	
	
	private static final int STATE_INIT = 0;
	private static final int STATE_ISSUED = 1;

	/** The card applet. */
	CardChannel applet;

	
	/** Car terminal data */
	boolean car_may_start = false;
	int mileage;
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
		super();
		tempNonce = 0;
		public_key_ct = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_ct");
		public_key_rt = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");
		private_key_ct = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_ct");
		
	}



	void startCar() throws CardException {
		try {
			getKeys();
			
			tempNonce++;
			CommandAPDU capdu = new CommandAPDU(CLA_START, SET_START_MILEAGE, (byte) 0, (byte) 0, getEncryptedMileage(tempNonce));
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
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
			
			capdu = new CommandAPDU(CLA_STOP, SET_FINAL_MILEAGE, (byte) 0, (byte) 0, getEncryptedMileage(nonce));
			rapdu = sendCommandAPDU(capdu);
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}

	//TODO: cant encrypt, 2 + 128 > 128
	byte[] getEncryptedMileage(short nonce) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		byte[] b_nonce = short2bytes(nonce);
		byte[] signed_mileage = rsaHandler.encrypt(private_key_ct, int2bytes(mileage));
		byte[] encrypted_singed_mileage = rsaHandler.encrypt(public_key_rt, signed_mileage);
		byte[] data = mergeByteArrays(b_nonce, encrypted_singed_mileage);
		byte[] signed_data = rsaHandler.encrypt(private_key_ct, data);
		byte[] encrypted_signed_data = rsaHandler.encrypt(currentSmartcard.getPublicKey(), signed_data);
		return rsaHandler.encrypt(currentSmartcard.getPublicKey(), encrypted_signed_data);
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

}