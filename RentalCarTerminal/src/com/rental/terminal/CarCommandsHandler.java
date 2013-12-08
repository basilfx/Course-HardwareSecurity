package com.rental.terminal;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import com.rental.terminal.db.Car;
import com.rental.terminal.db.Smartcard;


/**
 * Car terminal for the Rental Car applet.
 * 
 * @author Jelte & Erwin.
 * 
 */
public class CarCommandsHandler extends BaseCommandsHandler {

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

	/** Exception classes */
	public class CarTerminalInvalidCarIdException extends Exception {
	}

	public class CarTerminalInvalidDateException extends Exception {
	}

	/**
	 * Constructs the terminal application.
	 * 
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
	 * 
	 * @param car
	 *            - Car object which represents the currently used car.
	 * @require car.getPrivateKey() != null.
	 * @require car.id != null.
	 * @throws CardException
	 * @throws CarTerminalInvalidDateException 
	 * @throws CarTerminalInvalidCarIdException 
	 */
	public boolean startCar(Car car) throws CardException, TerminalNonceMismatchException, CarTerminalInvalidCarIdException, CarTerminalInvalidDateException {
		Smartcard currentSmartcard = new Smartcard();
		
		try {
			getKeys(currentSmartcard);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		// Set the start mileage and retrieve the response to the
		// nonce-challenge.
		CommandAPDU capdu;
		try {
			capdu = new CommandAPDU(CLA_START, SET_START_MILEAGE, (byte) 0, (byte) 0,
					getEncryptedNonceAndMileage(car), BLOCKSIZE + NONCESIZE);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		// Retrieve the car data.
		ResponseAPDU rapdu = terminal.sendCommandAPDU(capdu);
		
		byte[] retrievedNonceAndCarData = rapdu.getData();
		System.out.println("length: " + retrievedNonceAndCarData.length);
		byte[] return_nonce = checkCarData(car, retrievedNonceAndCarData);
		
		// Retrieve and check the signature of the car data.
		capdu = new CommandAPDU(CLA_START, GET_CAR_DATA_SIGNATURE, (byte) 0, (byte) 0, BLOCKSIZE);
		rapdu = terminal.sendCommandAPDU(capdu);
		
		boolean verified;
		try {
			verified = rsaHandler
					.verify(currentSmartcard.getPublicKey(), retrievedNonceAndCarData, rapdu.getData());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		JCUtil.log("Has the signature correctly been set? : " + Boolean.toString(verified));
		
		// Check the nonce.
		if (Arrays.equals(return_nonce, nonce)) {
			car_may_start = true;
			JCUtil.log("The car is allowed to start!");
		} else {
			throw new TerminalNonceMismatchException("Nonce mismatch");
		}
		
		return true;
	}

	/**
	 * Stops the car, and sets the final
	 * 
	 * @param car
	 *            - Car object which represents the currently used car.
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
			byte[] signature = rsaHandler.sign(car.getPrivateKey(), JCUtil.mergeByteArrays(encryptedNonceAndMileage,
					receivedNonce));
			capdu = new CommandAPDU(CLA_STOP, FINAL_MILEAGE_SIGNATURE, (byte) 0, (byte) 0, signature);
			terminal.sendCommandAPDU(capdu);
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}

	// TODO: check if this implementation is actually safe....
	/**
	 * Encrypts a nonce and the current mileage using the private key of the
	 * currently used car.
	 * 
	 * @param car
	 *            - Car object which represents the currently used car.
	 * @require car.getPrivateKey() != null.
	 * @return A nonce and the current mileage encrypted with
	 *         car.getPrivateKey()
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeySpecException 
	 */
	byte[] getEncryptedNonceAndMileage(Car car) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
		randomizeNonce();
		byte[] bytes_mileage = JCUtil.intToBytes(mileage);
		// byte[] encrypted_mileage = rsaHandler.encrypt(public_key_rt,
		// JCUtil.mergeByteArrays(bytes_nonce, bytes_mileage));
		byte[] encrypted_mileage = rsaHandler
				.encrypt(car.getPrivateKey(), JCUtil.mergeByteArrays(nonce, bytes_mileage));
		return encrypted_mileage;
	}

	/**
	 * Checks the received data against the currently used car, and checks
	 * whether the date is still valid.
	 * 
	 * @param car
	 *            - the currently used car.
	 * @param nonce_and_encrypted_car_data
	 *            - The received car data.
	 * @require car.getId() != null.
	 * @return the nonce.
	 * @throws CarTerminalInvalidCarIdException 
	 * @throws CarTerminalInvalidDateException
	 */
	byte[] checkCarData(Car car, byte[] nonce_and_car_data) throws CarTerminalInvalidCarIdException, CarTerminalInvalidDateException {
		try {			
			// Check the decrypted data.
			byte[] nonce = JCUtil.subArray(nonce_and_car_data, 0, NONCESIZE);
		
			byte[] car_data = JCUtil.subArray(nonce_and_car_data, NONCESIZE, BLOCKSIZE);
			
			// Decrypt encrypted car data.
			byte[] data = rsaHandler.decrypt(public_key_rt, car_data);

			short decrypted_car_id = JCUtil.bytesToShort(data[0], data[1]);
			short day = data[2];
			short month = data[3];
			short year = data[4];

			System.out.println("car ID from terminal: " + car.getId());
			System.out.println("car ID from SC: " + decrypted_car_id);
			Calendar expirationDate = Calendar.getInstance();
			expirationDate.set(year, month, day);
			// Check whether the decrypted car ID matches the ID of this car.
			if (car.getId() != decrypted_car_id) {
				throw new CarTerminalInvalidCarIdException();
			}
			// Check whether the received expiration date is before today.
			else if (expirationDate.before(Calendar.getInstance())) {
				throw new CarTerminalInvalidDateException();
			}
			
			return nonce;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getMileage() {
		return mileage;
	}

	public void setMileage(int mileage) {
		this.mileage = mileage;
	}

}