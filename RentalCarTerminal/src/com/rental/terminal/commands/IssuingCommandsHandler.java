package com.rental.terminal.commands;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import com.rental.terminal.CardUtils;
import com.rental.terminal.Terminal;
import com.rental.terminal.model.Smartcard;

/**
 * Issuing Terminal application.
 * 
 * @author Jelte & Erwin.
 */
public class IssuingCommandsHandler extends BaseCommandsHandler{

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

	RSAPrivateKey private_key_rt;

	/**
	 * Constructs the terminal application.
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public IssuingCommandsHandler(Terminal terminal) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		super(terminal);
		private_key_rt = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_rt");
	}

	/**
	 * Issues the smartcard, using the data supplied in currentSmartcard.
	 * @param currentSmartcard - an instance of Smartcard, its data will be pushed to the connected smartcard.
	 * @require currentSmartcard.getPublicKey != null
	 * @require currentSmartcard.getPrivateKey != null
	 * @require currentSmartcard.getId != null.
	 * @throws Exception
	 */
	public void issueCard(Smartcard currentSmartcard) throws Exception {
		
		CommandAPDU capdu;
		
		// Send the SC id to the SC. IS -> SC : sc_id
		capdu = new CommandAPDU(CLA_ISSUE, SET_SC_ID, (byte) 0, (byte) 0, CardUtils.shortToBytes(currentSmartcard.getCardId()), SCIDSIZE);
		ResponseAPDU rapdu = terminal.sendCommandAPDU(capdu);
		
		byte[] data = rapdu.getData();
		CardUtils.log("Card ID has been set to: " + Short.toString(CardUtils.bytesToShort(data[0], data[1])));
		
		// Send the random seed to the SC. IS -> SC : random_data_seed
		SecureRandom random = new SecureRandom();
		byte[] random_seed = new byte[6];
		random.nextBytes(random_seed);
		capdu = new CommandAPDU(CLA_ISSUE, SET_RANDOM_DATA_SEED, (byte) 0, (byte) 0, random_seed, 6);
		terminal.sendCommandAPDU(capdu);
		
		// Send the public key of the SC to the SC. IS -> SC : pubkey_sc
		byte[] modulus = CardUtils.getBytes(currentSmartcard.getPublicKey().getModulus());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_MODULUS_SC, (byte) 0, (byte) 0, modulus, modulus.length);
		rapdu = terminal.sendCommandAPDU(capdu);
		
		byte[] modulusResponse = rapdu.getData();
		
		if (Arrays.equals(modulus, modulusResponse)) {
			CardUtils.log("received pubkey_sc modulus: " + new String(modulusResponse));
			CardUtils.log("pubkey_sc modulus has been set to: " + currentSmartcard.getPublicKey().getModulus().toString());
		}
		else {
			CardUtils.log("pubkey_sc modulus has NOT CORRECTLY BEEN SET!");
			CardUtils.log("sent pubkey_sc modulus: " + new String(modulus));
			CardUtils.log("received pubkey_sc modulus: " + new String(modulusResponse));
		}
		
		byte[] exponent = CardUtils.getBytes(currentSmartcard.getPublicKey().getPublicExponent());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_EXPONENT_SC, (byte) 0, (byte) 0, exponent, exponent.length);
		rapdu = terminal.sendCommandAPDU(capdu);
		
		byte[] exponentResponse = rapdu.getData();
		if (Arrays.equals(exponent, exponentResponse)) {
			CardUtils.log("received pubkey_sc exponent: " + new String(exponentResponse));
			CardUtils.log("pubkey_sc exponent has been set to: " + currentSmartcard.getPublicKey().getPublicExponent().toString());
		}
		else {
			CardUtils.log("pubkey_sc exponent has NOT CORRECTLY BEEN SET!");
			CardUtils.log("sent pubkey_sc exponent: " + new String(exponent));
			CardUtils.log("received pubkey_sc exponent: " + new String(exponentResponse));
		}
		
		
		// TODO:
		// Ruud: klopt het dat hier alleen de signature wordt verzonden en niet de inhoud ervan?
		// Ruud: daarna checken we of de ontvangen signature correct is mbv de public_key van de RT, maar de SC kan nooit die signature maken omdat hij de priv_key van de RT
		//			niet heeft, toch? Of stuurt ie gewoon dezelfde signature nog een keer terug voor verificatie? Ik vind onderstaand een beetje vreemd.
		// Send signature. IS -> SC : {|sc_id, pubkey_sc|}privkey_rt
		byte[] mergedData = CardUtils.mergeByteArrays(CardUtils.shortToBytes(currentSmartcard.getCardId()), currentSmartcard.getPublicKey().getEncoded());
		byte[] signature = rsaHandler.sign(private_key_rt, mergedData);
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_SIGNATURE, (byte) 0, (byte) 0, signature, BLOCKSIZE);
		rapdu = terminal.sendCommandAPDU(capdu);
		
		byte[] responseSignature = rapdu.getData();
		
		CardUtils.log("Sent signature: " + new String(signature));
		CardUtils.log("Retrieved signature: " + new String(responseSignature));
		
		boolean verified = rsaHandler.verify(public_key_rt, mergedData, responseSignature);
		CardUtils.log("Has the signature correctly been set? : " + Boolean.toString(verified));
		
		// Send the private key of the SC to the SC. IS -> SC : IS -> SC : privkey_sc
		modulus = CardUtils.getBytes(currentSmartcard.getPrivateKey().getModulus());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PRVATE_KEY_MODULUS_SC, (byte) 0, (byte) 0, modulus);
		terminal.sendCommandAPDU(capdu);
				
		exponent = CardUtils.getBytes(currentSmartcard.getPrivateKey().getPrivateExponent());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PRIVATE_KEY_EXPONENT_SC, (byte) 0, (byte) 0, exponent);
		terminal.sendCommandAPDU(capdu);
		
		
		// Send the public key of the RT to the SC. IS -> SC : pubkey_rt
		modulus = CardUtils.getBytes(public_key_rt.getModulus());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_MODULUS_RT, (byte) 0, (byte) 0, modulus);
		terminal.sendCommandAPDU(capdu);		
		
		exponent = CardUtils.getBytes(public_key_rt.getPublicExponent());
		capdu = new CommandAPDU(CLA_ISSUE, SET_PUBLIC_KEY_EXPONENT_RT, (byte) 0, (byte) 0, exponent);
		terminal.sendCommandAPDU(capdu);
	}

}