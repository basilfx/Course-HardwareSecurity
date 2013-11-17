package terminal;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.swing.*;

import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;

import javax.smartcardio.*;

import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * Issuing Terminal application.
 * 
 * @author	Group 3.
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

	/**
	 * Constructs the terminal application.
	 */
	public IssuingTerminal() {
		super();
	}
	
	/**
	 * Issues the card.
	 * 
	 * @param  Integer  smartCardId  The ID of the SC.
	 */
	public void issueCard(short smartCardId) throws Exception {
		//
		// Send the SC id to the SC.
		//
		// IS -> SC : sc_id
		//
		
		CommandAPDU capdu = new CommandAPDU(CLA_ISSUE, SET_SC_ID, (byte) 0, (byte) 0, short2bytes(smartCardId));
		ResponseAPDU rapdu = sendCommandAPDU(capdu);
		
		byte[] data = rapdu.getData();
		
		log("Card ID has been set to: " + Short.toString(bytes2short(data[0], data[1])));
		
		/*
		
		//
		// Send the public key of the SC to the SC.
		//
		// IS -> SC : pubkey_sc
		//
		
		// Get the public key of the SC by reading the key file.
		RSAPublicKey SCPublicKey;
		try {
			SCPublicKey = getRSAPublicKeyFromFile("keys/public_key_sc");
		}
		catch (Exception e) {
			throw new Exception("[Error] IssuingTerminal: Exception when trying to get public key of SC (" + e.getMessage() + ")");
		}
		
		// Send the public key of the SC to the SC.
		
		// TODO.
		
		//
		// Create a signature of the concatenation of the SC id and the public key of the SC.
		// The data is signed with the private key of the RT.
		// Send the signature to the SC.
		//
		// IS -> SC : {|sc_id, pubkey_sc|}privkey_rt
		//
		
		// Get the private key of the RT by reading the key file.
		RSAPrivateKey RTPrivateKey;
		try {
			RTPrivateKey = getRSAPrivateKeyFromFile("keys/private_key_rt");
		}
		catch (Exception e) {
			throw new Exception("[Error] IssuingTerminal: Exception when trying to get private key of RT (" + e.getMessage() + ")");
		}
		
		// Concatenate the SC id and the public key of the SC.
		byte[] toBeSigned = (smartCardId.toString() + SCPublicKey.toString()).getBytes();
		
		// Sign the concatenation of the SC id and the public key of the SC.
		Signature signatureIntance = Signature.getInstance("SHA1withRSA");
		signatureIntance.initSign(RTPrivateKey);
		signatureIntance.update(toBeSigned);
    	byte[] signature = signatureIntance.sign();
    	
    	
    	
    	//
    	// Send the private key of the SC to the SC.
    	//
    	// IS -> SC : privkey_sc
    	//
    	
    	// TODO.
    	
    	//
    	// Send the public key of the RT to the SC.
    	//
    	// IS -> SC : pubkey_rt
    	//
    	
    	// TODO.*/
	}

}