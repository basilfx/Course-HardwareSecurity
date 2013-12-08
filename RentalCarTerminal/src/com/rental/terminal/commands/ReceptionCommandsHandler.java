package com.rental.terminal.commands;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import com.rental.terminal.CardUtils;
import com.rental.terminal.Terminal;
import com.rental.terminal.db.Car;
import com.rental.terminal.db.Smartcard;
import com.rental.terminal.encryption.RSAHandler;


/**
 * Reception Terminal application.
 * 
 * @author	Jelte & Erwin.
 */
public class ReceptionCommandsHandler extends BaseCommandsHandler {
	
	/** Init Bytes */
	private static final byte CLA_INIT = (byte) 0xB2;
	private static final byte INIT_START = (byte) 0x01;
	private static final byte INIT_AUTHENTICATED = (byte) 0x02;
	private static final byte INIT_SECOND_NONCE = (byte) 0x03;
	private static final byte INIT_SET_CAR_KEY_MODULUS = (byte) 0x04;
	private static final byte INIT_SET_CAR_KEY_EXPONENT = (byte) 0x05;
	private static final byte INIT_CHECK_CAR_KEY_SIGNATURE = (byte) 0x06;
	private static final byte INIT_SET_SIGNED_ENCRYPTED_CAR_DATA = (byte) 0x07;
	private static final byte INIT_CHECK_MEM_AVAILABLE = (byte) 0x08;

	/** Read Bytes */
	private static final byte CLA_READ = (byte) 0xB3;
	private static final byte READ_MILEAGE_SIGNED_NONCE = (byte) 0x01;
	private static final byte READ_MILEAGE_START_MILEAGE = (byte) 0x02;
	private static final byte READ_MILEAGE_FINAL_MILEAGE = (byte) 0x03;

	/** Reset Bytes */
	private static final byte CLA_RESET = (byte) 0xB4;
	private static final byte RESET_CARD = (byte) 0x01;

	RSAPrivateKey private_key_rt;

	/**
	 * Constructs the terminal application.
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public ReceptionCommandsHandler(Terminal terminal) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		super(terminal);
		rsaHandler = new RSAHandler();
		private_key_rt = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_rt");
	}
	
	/**
	 * Initializes the smartcard
	 * @param currentSmartcard - the currently used smartcard.
	 * @param car - The car which this smartcard may start.
	 * @require car.getPulicKey != null.
	 * @require car.getId != null.
	 * @require car.date != null.
	 * @throws CardException
	 * @throws TerminalNonceMismatchException
	 */
	public void initCard(Smartcard currentSmartcard, Car car) throws CardException, TerminalNonceMismatchException {
		try {
			// RT -> SC: init
			// SC -> RT: {|sc_id,pubkey_sc|}privkey_rt		
			getKeys(currentSmartcard);
			
			// Because pubkey_sc is known to the public (the APDU's in the getKeys() method can be forged by everyone), we should
			// make it infeasible for an attacker to pre-compute {|N1|}pubkey_sc for every (or a substantial part of) the possible N1's.
			// N1 should therefore be a large random number or text.
			randomizeNonce();
					
			// RT -> SC: {|N1|}pubkey_sc
			// SC -> RT: N1
			// [RT: if N1 = N1 then SC is authenticated]
			byte[] encrypted_nonce = rsaHandler.encrypt(currentSmartcard.getPublicKey(), nonce);
			CommandAPDU capdu = new CommandAPDU(CLA_INIT, INIT_START, (byte) 0, (byte) 0, encrypted_nonce, NONCESIZE);
			ResponseAPDU rapdu = terminal.sendCommandAPDU(capdu);
			byte[] data = rapdu.getData();
			if (Arrays.equals(nonce, data)) {
				// The nonces match: the SC is now authenticated to the RT.
				CardUtils.log("The nonces match! The SC is now authenticated to the RT.");
			} else {
				// Nonce mismatch! Abort.
				CardUtils.log("ERROR! The nonces do not match! The SC has not been authenticated to the RT.");
				
				throw new TerminalNonceMismatchException("Nonce mismatch");
			}
			// SC -> RT {|N2|}pubkey_rt
			// RT -> N2			
			capdu = new CommandAPDU(CLA_INIT, INIT_AUTHENTICATED, (byte) 0, (byte) 0, BLOCKSIZE);
			rapdu = terminal.sendCommandAPDU(capdu);
			encrypted_nonce = rapdu.getData();
			byte[] decrypted_nonce = rsaHandler.decrypt(private_key_rt, encrypted_nonce);
			
			capdu = new CommandAPDU(CLA_INIT, INIT_SECOND_NONCE, (byte) 0, (byte) 0, decrypted_nonce);
			terminal.sendCommandAPDU(capdu);
			
			
			// RT -> SC: {|pubkey_ct|}privkey_rt			
			byte[] car_public_key_modulus = CardUtils.getBytes(car.getPublicKey().getModulus());
			capdu = new CommandAPDU(CLA_INIT, INIT_SET_CAR_KEY_MODULUS, (byte) 0, (byte) 0, car_public_key_modulus);
			terminal.sendCommandAPDU(capdu);
			
			byte[] car_public_key_exponent = CardUtils.getBytes(car.getPublicKey().getPublicExponent());
			capdu = new CommandAPDU(CLA_INIT, INIT_SET_CAR_KEY_EXPONENT, (byte) 0, (byte) 0, car_public_key_exponent);
			terminal.sendCommandAPDU(capdu);
			
			byte[] car_public_key = CardUtils.mergeByteArrays(car_public_key_modulus, car_public_key_exponent);
			byte[] signature = rsaHandler.sign(private_key_rt, car_public_key);
			capdu = new CommandAPDU(CLA_INIT, INIT_CHECK_CAR_KEY_SIGNATURE, (byte) 0, (byte) 0, signature);
			terminal.sendCommandAPDU(capdu);
			

			// RT->SC: {|car_id, date, sc_id, N0|}pubkey_ct			
			capdu = new CommandAPDU(CLA_INIT, INIT_SET_SIGNED_ENCRYPTED_CAR_DATA, (byte) 0, (byte) 0, getEncryptedCarData(car));
			terminal.sendCommandAPDU(capdu);
			
			// Check available memory.
			capdu = new CommandAPDU(CLA_INIT, INIT_CHECK_MEM_AVAILABLE, (byte) 0, (byte) 0, (short) 2);
			rapdu = terminal.sendCommandAPDU(capdu);
			byte[] mem_available_array = rapdu.getData();
			short mem_available = CardUtils.bytesToShort(mem_available_array[0], mem_available_array[1]);
			
			System.out.println("Memory available (persistent): " + mem_available);
			
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}
	/**
	 * Reads the mileage from the smartcard.
	 * @param currentSmartcard - The currently used smartcard.
	 * @param car - The car which to read the mileage from.
	 * 				Sets the start mileage and the final mileage of this car instance.
	 * @ensure car.getStartMileage() == the start mileage on the physical smartcard
	 * @ensure car.getFinalMileage() == the final mileage on the physical smartcard
	 * @require car.getPublicKey != null.
	 * 
	 * @throws CardException
	 */
	public void read(Smartcard currentSmartcard, Car car) throws CardException {
		try {
			getKeys(currentSmartcard);
			
			randomizeNonce();
			
			byte[] data = rsaHandler.encrypt(currentSmartcard.getPublicKey(), nonce);
			CommandAPDU capdu = new CommandAPDU(CLA_READ, READ_MILEAGE_SIGNED_NONCE, (byte) 0, (byte) 0, data, NONCESIZE);
			ResponseAPDU rapdu = terminal.sendCommandAPDU(capdu);
			data = rapdu.getData();
			

			if (Arrays.equals(nonce, data)){
				// The nonces match. The SC is now authenticated to the RT. 
				CardUtils.log("The nonces match! The SC is now authenticated to the RT.");
			} else {
				// Nonce mismatch! Abort.
				CardUtils.log("ERROR! The nonces do not match! The SC has not been authenticated to the RT.");
				
				throw new TerminalNonceMismatchException("Nonce mismatch");
			}
			
			capdu = new CommandAPDU(CLA_READ, READ_MILEAGE_START_MILEAGE, (byte) 0, (byte) 0, BLOCKSIZE);
			rapdu = terminal.sendCommandAPDU(capdu);
			data = rapdu.getData();
			int start_mileage = verifyAndReturnMileage(car, data);
			car.setStartMileage(start_mileage);
			CardUtils.log("start mileage: " + start_mileage);
			
			capdu = new CommandAPDU(CLA_READ, READ_MILEAGE_FINAL_MILEAGE, (byte) 0, (byte) 0, BLOCKSIZE);
			rapdu = terminal.sendCommandAPDU(capdu);
			data = rapdu.getData();
			int final_mileage = verifyAndReturnMileage(car, data);
			car.setMileage(final_mileage);
			CardUtils.log("final mileage: " + final_mileage);
			
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}
	
	//TODO: unsure if correct
	/**
	 * Verifies the signed nonce and mileage and returns the nonce incase the verification succeeds.
	 * @param car - The car instance corresponding to the data.
	 * @param data - The received data containing a nonce, mileage and signature
	 * @require car.getPublicKey != null.
	 * @return the nonce.
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeySpecException 
	 */
	private int verifyAndReturnMileage(Car car, byte[] data) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException{
		byte[] decrypted_data = rsaHandler.decrypt(car.getPublicKey(),data);
		
		byte[] nonce = CardUtils.subArray(decrypted_data, 0, NONCESIZE);
		byte[] mileage = CardUtils.subArray(decrypted_data, NONCESIZE, MILEAGESIZE);
		return CardUtils.bytesToInt(mileage);
	}

	/**
	 * Resets the smartcard, removing all data, returning it to the issued state.
	 * @throws CardException
	 */
	public void reset() throws CardException {
		try {
			CommandAPDU capdu = new CommandAPDU(CLA_RESET, RESET_CARD, (byte) 0, (byte) 0);
			terminal.sendCommandAPDU(capdu);
			
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}
	
	/**
	 * Encrypts the id and date of the supplied car and encrypts them.
	 * @param car - The car instance of which the data will be encrypted.
	 * @require car.id != null.
	 * @require car.date != null.
	 * @return The encrypted id and date of the supplied car instance.
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private byte[] getEncryptedCarData(Car car) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{	
		byte[] car_data = CardUtils.mergeByteArrays(CardUtils.shortToBytes((short) car.getId()), CardUtils.dateToBytes(car.getDate()));
		return rsaHandler.encrypt(private_key_rt, car_data);
	}
		
}