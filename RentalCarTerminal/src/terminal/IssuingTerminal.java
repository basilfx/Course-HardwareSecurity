package terminal;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigInteger;
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
	static final byte CLA_TERMINAL_IS = (byte) 0x01;

	// Instructions.
	private static final byte INS_SET_PUB_MODULUS = (byte) 0x03;
	private static final byte INS_SET_PRIV_MODULUS = (byte) 0x12;
	private static final byte INS_SET_PRIV_EXP = (byte) 0x22;
	private static final byte INS_SET_PUB_EXP = (byte) 0x32;
	private static final byte INS_ISSUE = (byte) 0x40;
	private static final byte INS_ENCRYPT = (byte) 0xE0;
	private static final byte INS_DECRYPT = (byte) 0xD0;
	
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
	public void issueCard(Integer smartCardId) throws Exception {
		//
		// Create a signature of the concatenation of the SC id and the public key of the SC.
		// The data is signed with the private key of the RT.
		// Send the signature, the id of the SC and the public key of the SC to the SC.
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
		
		// Get the public key of the SC by reading the key file.
		RSAPublicKey SCPublicKey;
		try {
			SCPublicKey = getRSAPublicKeyFromFile("keys/public_key_sc");
		}
		catch (Exception e) {
			throw new Exception("[Error] IssuingTerminal: Exception when trying to get public key of SC (" + e.getMessage() + ")");
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
    	
    	// TODO.
	}

}