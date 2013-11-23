package junit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.smartcardio.CardException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import terminal.BaseTerminal;
import terminal.CarTerminal;
import terminal.IssuingTerminal;
import terminal.ReceptionTerminal;
import terminal.Smartcard;
import encryption.RSAHandler;

public class TerminalTest {
	
	IssuingTerminal issueingTerminal;
	ReceptionTerminal receptionTerminal;
	CarTerminal carTerminal;
	static short smartCardId = 3489;
	RSAHandler rsaHandler;

	@BeforeClass
	public static void classSetup() throws Exception{
		IssuingTerminal tempIT = new IssuingTerminal();
		tempIT.issueCard(smartCardId);
	}

	@Before
	public void setUp() throws Exception {
		rsaHandler = new RSAHandler();
		issueingTerminal = new IssuingTerminal();
		receptionTerminal = new ReceptionTerminal();
		carTerminal = new CarTerminal();		
	}

	@After
	public void tearDown() throws Exception {
		receptionTerminal.reset();
		issueingTerminal = null;
		receptionTerminal = null;
		carTerminal = null;		
	}
	
	@Test (expected = CardException.class)
	public void testIssue() throws Exception{
		issueingTerminal = new IssuingTerminal();
	}
	
	@Test
	public void testKeys() throws Exception{
		receptionTerminal.init();
		assertEquals("Check if smart card id matches", smartCardId, receptionTerminal.currentSmartcard.getScId());
		Smartcard first = issueingTerminal.currentSmartcard;
		Smartcard second = receptionTerminal.currentSmartcard;
		assertTrue("Check if pubkey matches", BaseTerminal.compareArrays(first.getPublicKey().getEncoded(), second.getPublicKey().getEncoded()));
		
		RSAPublicKey public_key_rt = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");
		byte[] data = BaseTerminal.mergeByteArrays(BaseTerminal.shortToBytes(smartCardId), first.getPublicKey().getEncoded());
		boolean result = rsaHandler.verify(public_key_rt, data, second.getSignature());
		assertTrue("validate signature", result);
	}
	
	@Test
	public void testSetMileage() throws Exception{
		receptionTerminal.init();
		int start_mileage = 500;
		int final_mileage = 1000;
		carTerminal.setMileage(start_mileage);
		carTerminal.startCar();
		
		carTerminal.setMileage(final_mileage);
		carTerminal.stopCar();
		
		receptionTerminal.read();
		assertEquals("Start mileage", start_mileage, receptionTerminal.start_mileage);
		assertEquals("Final mileage", final_mileage, receptionTerminal.final_mileage);
		
	}
	
	@Test(expected = CardException.class)
	public void testReset() throws Exception{
		receptionTerminal.init();
		receptionTerminal.reset();
		receptionTerminal.init();
		receptionTerminal.getKeys();
	}
	
	@Test(expected = CardException.class)
	public void testResetKeys() throws Exception{
		receptionTerminal.init();
		receptionTerminal.reset();
		receptionTerminal.getKeys();
	}
	
	
	@Test(expected = CardException.class)
	public void testDoubleInit() throws Exception{
		receptionTerminal.init();
		receptionTerminal.init();
	}

}
