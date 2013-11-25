package terminal;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

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
	private static final byte GET_CAR_DATA_SIGNATURE = (byte) 0x02;

	/** Stop Bytes */
	private static final byte CLA_STOP = (byte) 0xB6;
	private static final byte STOP_CAR = (byte) 0x01;
	private static final byte SET_FINAL_MILEAGE = (byte) 0x02;
	private static final byte FINAL_MILEAGE_SIGNATURE = (byte) 0x03;

	
	
	private static final int STATE_INIT = 0;
	private static final int STATE_ISSUED = 1;

	/** The card applet. */
	CardChannel applet;

	
	/** Car terminal data */
	boolean car_may_start = false;
	private int mileage;
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
		public_key_ct = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_ct");
		public_key_rt = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");
		private_key_ct = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_ct");
		
		mileage = 0;
	}


	public void startCar() throws CardException {
		try {
			getKeys();
			
			// Set the start mileage and retrieve the response to the nonce-challenge.
			CommandAPDU capdu = new CommandAPDU(CLA_START, SET_START_MILEAGE, (byte) 0, (byte) 0, getEncryptedNonceAndMileage(), BLOCKSIZE);
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			
			byte[] retrievedNonceAndCarData = rapdu.getData();
			byte[] return_nonce = checkCarData(retrievedNonceAndCarData);
			
			// Check the signature
			capdu = new CommandAPDU(CLA_START, GET_CAR_DATA_SIGNATURE, (byte) 0, (byte) 0, BLOCKSIZE);
			rapdu = sendCommandAPDU(capdu);
			
			boolean verified = rsaHandler.verify(currentSmartcard.getPublicKey(), retrievedNonceAndCarData, rapdu.getData());
			log("Has the signature correctly been set? : " + Boolean.toString(verified));
			
			// Check the nonce.
			if (Arrays.equals(return_nonce, nonce)) {
				car_may_start = true;
				log("The car is allowed to start!");
			}
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}

	public void stopCar() throws CardException {
		try {
			CommandAPDU capdu = new CommandAPDU(CLA_STOP, STOP_CAR, (byte) 0, (byte) 0, NONCESIZE);
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			byte[] data = rapdu.getData();
			byte[] receivedNonce = JCUtil.subArray(data, 0, NONCESIZE);
			
			// Send final mileage.
			byte[] encryptedNonceAndMileage = getEncryptedNonceAndMileage();
			capdu = new CommandAPDU(CLA_STOP, SET_FINAL_MILEAGE, (byte) 0, (byte) 0, encryptedNonceAndMileage);
			sendCommandAPDU(capdu);
			
			// Send signature of final mileage and receivedNonce.
			byte[] signature = rsaHandler.sign(private_key_ct, JCUtil.mergeByteArrays(receivedNonce, encryptedNonceAndMileage));
			capdu = new CommandAPDU(CLA_STOP, FINAL_MILEAGE_SIGNATURE, (byte) 0, (byte) 0, signature);
			sendCommandAPDU(capdu);
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}

	//TODO: check if this implementation is actually safe....
	byte[] getEncryptedNonceAndMileage() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		randomizeNonce();
		byte[] bytes_mileage = JCUtil.intToBytes(mileage);
		//byte[] encrypted_mileage = rsaHandler.encrypt(public_key_rt, JCUtil.mergeByteArrays(bytes_nonce, bytes_mileage));
		byte[] encrypted_mileage = rsaHandler.encrypt(private_key_ct, JCUtil.mergeByteArrays(nonce, bytes_mileage));
		return encrypted_mileage;
	}
	
	//TODO check if data is actually correct
	byte[] checkCarData(byte[] data) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		byte[] nonce = JCUtil.subArray(data, 0, NONCESIZE);
		byte[] car_data = JCUtil.subArray(data, NONCESIZE, BLOCKSIZE);
		short decrypted_car_id = JCUtil.bytesToShort(car_data[0], car_data[1]);
		short day = car_data[2];
		short month = car_data[3];
		short year = car_data[4];
		short sc_id = JCUtil.bytesToShort(car_data[5], car_data[6]);
		
		// TODO.
		
		return nonce;
	}
	
	public int getMileage() {
		return mileage;
	}

	public void setMileage(int mileage) {
		this.mileage = mileage;
	}

}