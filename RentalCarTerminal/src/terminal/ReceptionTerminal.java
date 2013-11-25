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
	private static final byte INIT_SET_CAR_KEY_MODULUS = (byte) 0x04;
	private static final byte INIT_SET_CAR_KEY_EXPONENT = (byte) 0x05;
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


	// The card applet.
	CardChannel applet;
	
	RSAHandler rsaHandler;
	
	RSAPublicKey pubic_key_sc;
	RSAPublicKey public_key_ct;
	RSAPublicKey public_key_rt;
	RSAPrivateKey private_key_rt;
	
	public int start_mileage;
	public int final_mileage;

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
	
	public void initCard() throws CardException {
		try {
			// RT -> SC: init
			// SC -> RT: {|sc_id,pubkey_sc|}privkey_rt
			getKeys();
			
			// Because pubkey_sc is known to the public (the APDU's in the getKeys() method can be forged by everyone), we should
			// make it infeasible for an attacker to pre-compute {|N1|}pubkey_sc for every (or a substantial part of) the possible N1's.
			// N1 should therefore be a large random number or text.
			randomizeNonce();
				
				
			// TODO: Ruud: die nonce (N1) hier is niet encrypted met de pubkey van de SC, dat moet wel volgens onze	specs
			// RT -> SC: {|N1|}pubkey_sc
			// SC -> RT: N1
			// [RT: if N1 = N1 then SC is authenticated]
			CommandAPDU capdu = new CommandAPDU(CLA_INIT, INIT_START, (byte) 0, (byte) 0, nonce, NONCESIZE);
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			byte[] data = rapdu.getData();
			if (Arrays.equals(nonce, data)) {
				// were fine!
				log("The nonces match! The SC is now authenticated to the RT.");
			} else {
				//Oh noes!, throw exception or something
				log("ERROR! The nonces do not match! The SC has not been authenticated to the RT.");
				// TODO: Ruud: moeten we hier niet exiten?
				//exit();
			}
						
			
			// SC -> RT {|N2|}pubkey_rt
			// RT -> N2
			capdu = new CommandAPDU(CLA_INIT, INIT_AUTHENTICATED, (byte) 0, (byte) 0, BLOCKSIZE);
			rapdu = sendCommandAPDU(capdu);
			byte[] encrypted_nonce = rapdu.getData();
			byte[] decrypted_nonce = rsaHandler.decrypt(private_key_rt, encrypted_nonce);
			
			capdu = new CommandAPDU(CLA_INIT, INIT_SECOND_NONCE, (byte) 0, (byte) 0, decrypted_nonce);
			sendCommandAPDU(capdu);
						
			
			// TODO: Ruud: Moeten deze niet gesigned worden?
			// RT -> SC: {|pubkey_ct|}privkey_rt
			//byte[] car_public_key_modulus = rsaHandler.encrypt(private_key_rt, public_key_ct.getModulus().toByteArray());
			byte[] car_public_key_modulus = JCUtil.getBytes(public_key_ct.getModulus());
			capdu = new CommandAPDU(CLA_INIT, INIT_SET_CAR_KEY_MODULUS, (byte) 0, (byte) 0, car_public_key_modulus);
			sendCommandAPDU(capdu);
			
			//byte[] car_public_key_exponent = rsaHandler.encrypt(private_key_rt, public_key_ct.getPublicExponent().toByteArray());
			byte[] car_public_key_exponent = JCUtil.getBytes(public_key_ct.getPublicExponent());
			capdu = new CommandAPDU(CLA_INIT, INIT_SET_CAR_KEY_EXPONENT, (byte) 0, (byte) 0, car_public_key_exponent);
			sendCommandAPDU(capdu);
			
			/*byte[] car_public_key = mergeByteArrays(car_public_key_modulus, car_public_key_exponent);
			byte[] signature = rsaHandler.sign(private_key_rt, car_public_key);
			capdu = new CommandAPDU(CLA_INIT, INIT_CHECK_CAR_KEY_SIGNATURE, (byte) 0, (byte) 0, signature);
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();*/
			
			
			// RT->SC: {|car_id, date, sc_id, N0|}pubkey_ct
			capdu = new CommandAPDU(CLA_INIT, INIT_SET_SIGNED_ENCRYPTED_CAR_DATA, (byte) 0, (byte) 0, getEncryptedCarData());
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}
	
	// TODO: real implementation
	public void read() throws CardException {
		try {
			getKeys();
			
			randomizeNonce();
			
			byte[] data = rsaHandler.encrypt(currentSmartcard.getPublicKey(), nonce);
			CommandAPDU capdu = new CommandAPDU(CLA_READ, READ_MILEAGE_SIGNED_NONCE, (byte) 0, (byte) 0, data, NONCESIZE);
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();

			if (Arrays.equals(nonce, data)){
				// were fine!
				log("The nonces match! The SC is now authenticated to the RT.");
			} else {
				//Oh noes!, throw exception or something
				log("ERROR! The nonces do not match! The SC has not been authenticated to the RT.");
				// TODO: Ruud: moeten we hier niet exiten?
				//exit();
			}
			
			capdu = new CommandAPDU(CLA_READ, READ_MILEAGE_START_MILEAGE, (byte) 0, (byte) 0, NONCESIZE + MILEAGESIZE + BLOCKSIZE);
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			verifyMileage(data);
			
			
			capdu = new CommandAPDU(CLA_READ, READ_MILEAGE_FINAL_MILEAGE, (byte) 0, (byte) 0, BLOCKSIZE);
			rapdu = sendCommandAPDU(capdu);
			data = rapdu.getData();
			verifyMileage(data);
			
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}
	
	//TODO: unsure if correct
	public void verifyMileage(byte[] data) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		byte[] nonce = JCUtil.subArray(data, 0, NONCESIZE);
		byte[] mileage = JCUtil.subArray(data, NONCESIZE, MILEAGESIZE);
		byte[] encrypted_signed_mileage = JCUtil.subArray(data, NONCESIZE + MILEAGESIZE, BLOCKSIZE);
		byte[] signed_mileage = rsaHandler.decrypt(private_key_rt, encrypted_signed_mileage);
		// TODO: Ruud: moet hier niet gewoon de signature van worden geverified en afgestript
		byte[] unsigned_mileage = rsaHandler.decrypt(public_key_ct, signed_mileage);
		byte[] nonce_mileage = JCUtil.subArray(unsigned_mileage, 0, NONCESIZE + MILEAGESIZE);
		
		// TODO: Ruud: Wat doet dit precies? De arrays gaan nooit hetzelfde zijn omdat je de gedecrypte en geunsignde versie vergelijkt met de geencrypte en gesignde versie
		// TODO: Ruud: we kunnen de 'mileage' of '-1' returnen?
		if (JCUtil.compareArrays(JCUtil.mergeByteArrays(nonce, mileage), nonce_mileage)){
			//store values
		} else {
			//exception
		}
	}

	public void reset() throws CardException {
		try {
			CommandAPDU capdu = new CommandAPDU(CLA_RESET, RESET_CARD, (byte) 0, (byte) 0);
			ResponseAPDU rapdu = sendCommandAPDU(capdu);
			byte[] data = rapdu.getData();
			
		} catch (Exception e) {
			throw new CardException(e.getMessage());
		}
	}
	
	// TODO fix hardcoded values
	// TODO: Ruud: is dat echt een ramp?
	byte[] getEncryptedCarData() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		// TODO: Ruud: moet hier niet ook de sc_id in?	
		// TODO: Ruud: moet dit niet ook signed zijn?
		randomizeNonce();
		short car_id = 12;
		byte[] date = new byte[3];		
		date[0] = 15;
		date[1] = 11;
		date[2] = 13;		
		byte[] data = JCUtil.mergeByteArrays(nonce, JCUtil.shortToBytes(car_id));
		byte[] car_data = JCUtil.mergeByteArrays(data, date);
		return rsaHandler.encrypt(private_key_rt, car_data);
	}
	
	
}