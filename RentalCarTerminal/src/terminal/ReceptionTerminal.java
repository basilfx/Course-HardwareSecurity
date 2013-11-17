package terminal;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

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
			getKeys();
			tempNonce++;
			CommandAPDU capdu = new CommandAPDU(CLA_INIT, INIT_START, (byte) 0, (byte) 0, short2bytes(tempNonce));
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			byte[] data = rapdu.getData();
			short received_nonce = bytes2short(data[0], data[1]);
			if (tempNonce == received_nonce){//TODO
				// were fine!
			} else {
				//Oh noes!, throw exception or something
			}
			capdu = new CommandAPDU(CLA_INIT, INIT_AUTHENTICATED, (byte) 0, (byte) 0);
			rapdu = sendCommandAPDU(capdu);
			byte[] encrypted_nonce = rapdu.getData();
			byte[] decrypted_nonce = rsaHandler.decrypt(private_key_rt, encrypted_nonce);
			
			
			capdu = new CommandAPDU(CLA_INIT, INIT_SECOND_NONCE, (byte) 0, (byte) 0, decrypted_nonce);
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			
			byte[] signed_car_public_key_modulus = rsaHandler.encrypt(private_key_rt, public_key_ct.getModulus().toByteArray());
			capdu = new CommandAPDU(CLA_INIT, INIT_SET_SIGNED_CAR_KEY_MODULUS, (byte) 0, (byte) 0, signed_car_public_key_modulus);
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			
			byte[] signed_car_public_key_exponent = rsaHandler.encrypt(private_key_rt, public_key_ct.getPublicExponent().toByteArray());
			capdu = new CommandAPDU(CLA_INIT, INIT_SET_SIGNED_CAR_KEY_EXPONENT, (byte) 0, (byte) 0, signed_car_public_key_exponent);
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			
			capdu = new CommandAPDU(CLA_INIT, INIT_SET_SIGNED_ENCRYPTED_CAR_DATA, (byte) 0, (byte) 0, getEncryptedCarData());
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}
	//TODO: real implementation
	void read() throws CardException {
		try {
			getKeys();
			
			tempNonce++;
			byte[] data = rsaHandler.encrypt(currentSmartcard.getPublicKey(), short2bytes(tempNonce));
			CommandAPDU capdu = new CommandAPDU(CLA_READ, READ_MILEAGE_SIGNED_NONCE, (byte) 0, (byte) 0, data);
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			short received_nonce = bytes2short(data[0], data[1]);
			if (tempNonce == received_nonce){
				//continue
			} else {
				//exception
			}
			
			//TODO: invent better implementation, nonce.length = 2, encrypted_start_mileage = 128
			// Maximum block size for encryption: 128
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
	//TODO: real implementation
	void reset() throws CardException {
		try {

			CommandAPDU capdu = new CommandAPDU(CLA_RESET, RESET_CARD, (byte) 0, (byte) 0);
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			byte[] data = rapdu.getData();
			
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}
	//TODO fix hardcoded values
	byte[] getEncryptedCarData(){
		tempNonce++;
		short car_id = 12;
		byte[] date = new byte[3];
		date[0] = 15;
		date[1] = 11;
		date[2] = 13;
		byte[] data = mergeByteArrays(short2bytes(tempNonce), date);
		return mergeByteArrays(data, short2bytes(car_id));
	}
	
	
}