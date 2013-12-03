package junit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.smartcardio.CardException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import terminal.CarCommandsHandler;
import terminal.IssuingCommandsHandler;
import terminal.JCUtil;
import terminal.ReceptionCommandsHandler;
import terminal.Smartcard;
import encryption.RSAHandler;

public class TerminalTest {
	
	IssuingCommandsHandler issueingTerminal;
	ReceptionCommandsHandler receptionTerminal;
	CarCommandsHandler carTerminal;
	static short smartCardId = 3489;
	RSAHandler rsaHandler;

	@BeforeClass
	public static void classSetup() throws Exception{
		IssuingCommandsHandler tempIT = new IssuingCommandsHandler();
		tempIT.issueCard(smartCardId);
	}

	@Before
	public void setUp() throws Exception {
		rsaHandler = new RSAHandler();
		issueingTerminal = new IssuingCommandsHandler();
		receptionTerminal = new ReceptionCommandsHandler();
		carTerminal = new CarCommandsHandler();		
	}

	@After
	public void tearDown() throws Exception {
		receptionTerminal.reset();
		receptionTerminal.stopRunning();
		receptionTerminal = null;

		issueingTerminal.stopRunning();
		issueingTerminal = null;

		carTerminal.stopRunning();
		carTerminal = null;		
	}
	
	@Test (expected = CardException.class)
	public void testIssue() throws Exception{
		issueingTerminal = new IssuingCommandsHandler();
	}
	
	@Test
	public void testKeys() throws Exception{
		receptionTerminal.initCard();
		assertEquals("Check if smart card id matches", smartCardId, receptionTerminal.currentSmartcard.getScId());
		Smartcard first = issueingTerminal.currentSmartcard;
		Smartcard second = receptionTerminal.currentSmartcard;
		assertTrue("Check if pubkey matches", Arrays.equals(first.getPublicKey().getEncoded(), second.getPublicKey().getEncoded()));
		
		RSAPublicKey public_key_rt = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");
		byte[] data = JCUtil.mergeByteArrays(JCUtil.shortToBytes(smartCardId), first.getPublicKey().getEncoded());
		boolean result = rsaHandler.verify(public_key_rt, data, second.getSignature());
		assertTrue("validate signature", result);
	}
	
	@Test
	public void testSetMileage() throws Exception{
		receptionTerminal.initCard();
		receptionTerminal.stopRunning();
		int start_mileage = 500;
		int final_mileage = 1000;
		carTerminal.setMileage(start_mileage);
		carTerminal.startCar();
		
		carTerminal.setMileage(final_mileage);
		carTerminal.stopCar();
		carTerminal.stopRunning();
		
		receptionTerminal.read();
		assertEquals("Start mileage", start_mileage, receptionTerminal.start_mileage);
		assertEquals("Final mileage", final_mileage, receptionTerminal.final_mileage);
		
	}
	
	@Test(expected = CardException.class)
	public void testReset() throws Exception{
		receptionTerminal.initCard();
		receptionTerminal.reset();
		receptionTerminal.initCard();
		receptionTerminal.getKeys();
	}
	
	@Test(expected = CardException.class)
	public void testResetKeys() throws Exception{
		receptionTerminal.initCard();
		receptionTerminal.reset();
		receptionTerminal.getKeys();
	}
	
	
	@Test(expected = CardException.class)
	public void testDoubleInit() throws Exception{
		receptionTerminal.initCard();
		receptionTerminal.initCard();
	}

}
