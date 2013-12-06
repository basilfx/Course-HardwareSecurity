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
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * Car terminal for the Rental Car applet.
 * 
 * @author Jelte & Erwin.
 * 
 */
public class CarCommandsHandler extends BaseCommandsHandler{

	/** Start Bytes */
	private static final byte CLA_START = (byte) 0xB5;
	private static final byte SET_START_MILEAGE = (byte) 0x01;
	private static final byte GET_CAR_DATA_SIGNATURE = (byte) 0x02;

	/** Stop Bytes */
	private static final byte CLA_STOP = (byte) 0xB6;
	private static final byte STOP_CAR = (byte) 0x01;
	private static final byte SET_FINAL_MILEAGE = (byte) 0x02;
	private static final byte FINAL_MILEAGE_SIGNATURE = (byte) 0x03;

	
	/** Car terminal data */
	boolean car_may_start = false;
	private int mileage;
	

	/**
	 * Constructs the terminal application.
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public CarCommandsHandler(Terminal terminal) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		super(terminal);
	
		mileage = 1000;
	}



	/**
	 * Starts the car, and sets the start mileage on the smartcard.
	 * @param car - Car object which represents the currently used car.
	 * @require car.getPrivateKey() != null.
	 * @require car.id != null.
	 * @throws CardException
	 */
	public void startCar(Car car) throws CardException {
		try {
			Smartcard currentSmartcard = new Smartcard();
			getKeys(currentSmartcard);
			
			// Set the start mileage and retrieve the response to the nonce-challenge.
			CommandAPDU capdu = new CommandAPDU(CLA_START, SET_START_MILEAGE, (byte) 0, (byte) 0, getEncryptedNonceAndMileage(car), BLOCKSIZE);
			ResponseAPDU rapdu = terminal.sendCommandAPDU(capdu);
			
			byte[] retrievedNonceAndCarData = rapdu.getData();
			byte[] return_nonce = checkCarData(car, retrievedNonceAndCarData);
			
			// Check the signature
			capdu = new CommandAPDU(CLA_START, GET_CAR_DATA_SIGNATURE, (byte) 0, (byte) 0, BLOCKSIZE);
			rapdu = terminal.sendCommandAPDU(capdu);
			
			boolean verified = rsaHandler.verify(currentSmartcard.getPublicKey(), retrievedNonceAndCarData, rapdu.getData());
			JCUtil.log("Has the signature correctly been set? : " + Boolean.toString(verified));
			
			// Check the nonce.
			if (Arrays.equals(return_nonce, nonce)) {
				car_may_start = true;
				JCUtil.log("The car is allowed to start!");
			}
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}

	/**
	 * Stops the car, and sets the final 
	 * @param car - Car object which represents the currently used car.
	 * @require car.getPrivateKey() != null.
	 * @throws CardException
	 */
	public void stopCar(Car car) throws CardException {
		try {
			CommandAPDU capdu = new CommandAPDU(CLA_STOP, STOP_CAR, (byte) 0, (byte) 0, NONCESIZE);
			ResponseAPDU rapdu = terminal.sendCommandAPDU(capdu);
			byte[] data = rapdu.getData();
			byte[] receivedNonce = JCUtil.subArray(data, 0, NONCESIZE);
			
			// Send final mileage.
			byte[] encryptedNonceAndMileage = getEncryptedNonceAndMileage(car);
			capdu = new CommandAPDU(CLA_STOP, SET_FINAL_MILEAGE, (byte) 0, (byte) 0, encryptedNonceAndMileage);
			terminal.sendCommandAPDU(capdu);
			
			// Send signature of final mileage and receivedNonce.
			byte[] signature = rsaHandler.sign(car.getPrivateKey(), JCUtil.mergeByteArrays(receivedNonce, encryptedNonceAndMileage));
			capdu = new CommandAPDU(CLA_STOP, FINAL_MILEAGE_SIGNATURE, (byte) 0, (byte) 0, signature);
			terminal.sendCommandAPDU(capdu);
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}

	//TODO: check if this implementation is actually safe....
	/**
	 * Encrypts a nonce and the current mileage using the private key of the currently used car.
	 * @param car - Car object which represents the currently used car.
	 * @require car.getPrivateKey() != null.
	 * @return A nonce and the current mileage encrypted with car.getPrivateKey()
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	byte[] getEncryptedNonceAndMileage(Car car) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		randomizeNonce();
		byte[] bytes_mileage = JCUtil.intToBytes(mileage);
		//byte[] encrypted_mileage = rsaHandler.encrypt(public_key_rt, JCUtil.mergeByteArrays(bytes_nonce, bytes_mileage));
		byte[] encrypted_mileage = rsaHandler.encrypt(car.getPrivateKey(), JCUtil.mergeByteArrays(nonce, bytes_mileage));
		return encrypted_mileage;
	}
	
	//TODO check if data is actually correct
	/**
	 * Checks the received data against the currently used car, 
	 * and checks whether the date is still valid.
	 * @param car - the currently used car.
	 * @param data - The received car data.
	 * @require car.getId() != null.
	 * @return the nonce.
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	byte[] checkCarData(Car car, byte[] data) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
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