package terminal;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * Issuing Terminal application.
 * 
 * @author Group 3.
 */
public class IssuingTerminal extends BaseTerminal {

	// CLA of this Terminal.
	static final byte CLA_ISSUE = (byte) 0xB1;

	// Instructions.
	private static final byte SET_PUBLIC_KEY_SIGNATURE = (byte) 0x01;
	private static final byte SET_PUBLIC_KEY_MODULUS_SC = (byte) 0x02;
	private static final byte SET_PUBLIC_KEY_EXPONENT_SC = (byte) 0x03;
	private static final byte SET_SC_ID = (byte) 0x04;
	private static final byte SET_PRVATE_KEY_MODULUS_SC = (byte) 0x05;
	private static final byte SET_PRIVATE_KEY_EXPONENT_SC = (byte) 0x06;
	private static final byte SET_PUBLIC_KEY_MODULUS_RT = (byte) 0x07;
	private static final byte SET_PUBLIC_KEY_EXPONENT_RT = (byte) 0x08;
	private static final byte SET_RANDOM_DATA_SEED = (byte) 0x09;

	// The card applet.
	CardChannel applet;
	RSAPublicKey public_key_rt;
	RSAPrivateKey private_key_sc;
	RSAPrivateKey private_key_rt;

	/**
	 * Constructs the terminal application.
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public IssuingTerminal() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		super();
		currentSmartcard.setPublicKey(rsaHandler.readPublicKeyFromFileSystem("keys/public_key_sc"));
		public_key_rt = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");
		private_key_rt = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_rt");
		private_key_sc = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_sc");
	}

	/**
	 * Issues the card.
	 * 
	 * @param Integer
	 *            smartCardId The ID of the SC.
	 */
	public void issueCard(short smartCardId) throws Exception {
		
		CommandAPDU capdu;
		
		// Send the SC id to the SC. IS -> SC : sc_id
		capdu = new CommandAPDU(CLA_ISSUE, SET_SC_ID, (byte) 0, (byte) 0, JCUtil.shortToBytes(smartCardId), SCIDSIZE);
		ResponseAPDU rapdu = sendCommandAPDU(capdu);
		
		byte[] data = rapdu.getData();
		log("Card ID has been set to: " + Short.toString(JCUtil.bytesToShort(data[0], data[1])));
		
		// Send the random seed to the SC. IS -> SC : random_data_seed
		SecureRandom random = new SecureRandom();
		byte[] random_seed = new byte[6];
		random.nextBytes(random_seed);
		capdu = new CommandAPDU(CLA_ISSUE, SET_RANDOM_DATA_SEED, (byte) 0, (byte) 0, random_seed, 6);
		sendCommandAPDU(capdu);
		
		
		// Send the public key of the SC to the SC. IS -> SC : pubkey_sc
		byte[] modulus = JCUtil.getBytes(currentSmartcard.getPublicKey().getModulus());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_MODULUS_SC, (byte) 0, (byte) 0, modulus, modulus.length);
		rapdu = sendCommandAPDU(capdu);
		
		byte[] modulusResponse = rapdu.getData();
		
		if (Arrays.equals(modulus, modulusResponse)) {
			log("received pubkey_sc modulus: " + new String(modulusResponse));
			log("pubkey_sc modulus has been set to: " + currentSmartcard.getPublicKey().getModulus().toString());
		}
		else {
			log("pubkey_sc modulus has NOT CORRECTLY BEEN SET!");
			log("sent pubkey_sc modulus: " + new String(modulus));
			log("received pubkey_sc modulus: " + new String(modulusResponse));
		}
		
		byte[] exponent = JCUtil.getBytes(currentSmartcard.getPublicKey().getPublicExponent());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_EXPONENT_SC, (byte) 0, (byte) 0, exponent, exponent.length);
		rapdu = sendCommandAPDU(capdu);
		
		byte[] exponentResponse = rapdu.getData();
		if (Arrays.equals(exponent, exponentResponse)) {
			log("received pubkey_sc exponent: " + new String(exponentResponse));
			log("pubkey_sc exponent has been set to: " + currentSmartcard.getPublicKey().getPublicExponent().toString());
		}
		else {
			log("pubkey_sc exponent has NOT CORRECTLY BEEN SET!");
			log("sent pubkey_sc exponent: " + new String(exponent));
			log("received pubkey_sc exponent: " + new String(exponentResponse));
		}
		
		// TODO:
		// Ruud: klopt het dat hier alleen de signature wordt verzonden en niet de inhoud ervan?
		// Ruud: daarna checken we of de ontvangen signature correct is mbv de public_key van de RT, maar de SC kan nooit die signature maken omdat hij de priv_key van de RT
		//			niet heeft, toch? Of stuurt ie gewoon dezelfde signature nog een keer terug voor verificatie? Ik vind onderstaand een beetje vreemd.
		// Send signature. IS -> SC : {|sc_id, pubkey_sc|}privkey_rt
		byte[] mergedData = JCUtil.mergeByteArrays(JCUtil.shortToBytes(smartCardId), currentSmartcard.getPublicKey().getEncoded());
		byte[] signature = rsaHandler.sign(private_key_rt, mergedData);
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_SIGNATURE, (byte) 0, (byte) 0, signature, BLOCKSIZE);
		rapdu = sendCommandAPDU(capdu);
		
		byte[] responseSignature = rapdu.getData();
		
		log("Sent signature: " + new String(signature));
		log("Retrieved signature: " + new String(responseSignature));
		
		boolean verified = rsaHandler.verify(public_key_rt, mergedData, responseSignature);
		log("Has the signature correctly been set? : " + Boolean.toString(verified));
		
		// Send the private key of the SC to the SC. IS -> SC : privkey_sc
		modulus = JCUtil.getBytes(private_key_sc.getModulus());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PRVATE_KEY_MODULUS_SC, (byte) 0, (byte) 0, modulus);
		sendCommandAPDU(capdu);
				
		exponent = JCUtil.getBytes(private_key_sc.getPrivateExponent());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PRIVATE_KEY_EXPONENT_SC, (byte) 0, (byte) 0, exponent);
		sendCommandAPDU(capdu);
		
		
		// Send the public key of the RT to the SC. IS -> SC : pubkey_rt
		modulus = JCUtil.getBytes(public_key_rt.getModulus());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_MODULUS_RT, (byte) 0, (byte) 0, modulus);
		sendCommandAPDU(capdu);		
		
		exponent = JCUtil.getBytes(public_key_rt.getPublicExponent());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_EXPONENT_RT, (byte) 0, (byte) 0, exponent);
		sendCommandAPDU(capdu);
	}

}