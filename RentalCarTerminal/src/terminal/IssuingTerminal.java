package terminal;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

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

	// The card applet.
	CardChannel applet;
	RSAPublicKey public_key_sc;
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
		
		public_key_sc = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_sc");
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
				
		// Send signature
		byte[] data = mergeByteArrays(short2bytes(smartCardId), public_key_sc.getEncoded());
		byte[] signature = rsaHandler.sign(private_key_rt, data);
		CommandAPDU capdu;
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_SIGNATURE, (byte) 0, (byte) 0, signature);
		sendCommandAPDU(capdu);
		
		
		// Send the public key of the SC to the SC.
		byte[] modulus = getBytes(public_key_sc.getModulus());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_MODULUS_SC, (byte) 0, (byte) 0, modulus);
		sendCommandAPDU(capdu);		
		
		byte[] exponent = getBytes(public_key_sc.getPublicExponent());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_EXPONENT_SC, (byte) 0, (byte) 0, exponent);
		sendCommandAPDU(capdu);


		//
		// Send the SC id to the SC.
		//
		// IS -> SC : sc_id
		//

		// TODO: niet vergeten de lengte van de response mee te sturen als je een response verwacht!
		capdu = new CommandAPDU(CLA_ISSUE, SET_SC_ID, (byte) 0, (byte) 0, short2bytes(smartCardId), SCIDSIZE);
		ResponseAPDU rapdu = sendCommandAPDU(capdu);

		data = rapdu.getData();

		log("Card ID has been set to: " + Short.toString(bytes2short(data[0], data[1])));

		
		// Send the private key of the SC to the SC. // // IS -> SC :
		modulus = getBytes(private_key_sc.getModulus());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PRVATE_KEY_MODULUS_SC, (byte) 0, (byte) 0, modulus);
		sendCommandAPDU(capdu);
				
		exponent = getBytes(private_key_sc.getPrivateExponent());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PRIVATE_KEY_EXPONENT_SC, (byte) 0, (byte) 0, exponent);
		sendCommandAPDU(capdu);
		
		
		// Send the public key of the RT to the SC. // // IS -> SC :
		modulus = getBytes(public_key_rt.getModulus());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_MODULUS_RT, (byte) 0, (byte) 0, modulus);
		sendCommandAPDU(capdu);		
		
		exponent = getBytes(public_key_rt.getPublicExponent());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_EXPONENT_RT, (byte) 0, (byte) 0, exponent);
		sendCommandAPDU(capdu);
	}

}