package junit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.interfaces.RSAPublicKey;

import javax.smartcardio.CardException;

import org.junit.After;
import org.junit.Before;
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
	short smartCardId = 3489;
	RSAHandler rsaHandler;


	@Before
	public void setUp() throws Exception {
		issueingTerminal = new IssuingTerminal();
		receptionTerminal = new ReceptionTerminal();
		carTerminal = new CarTerminal();
		rsaHandler = new RSAHandler();
	}

	@After
	public void tearDown() throws Exception {
		issueingTerminal = null;
		receptionTerminal = null;
		carTerminal = null;
	}
	
	@Test
	public void testIssue() throws Exception{		
		issueingTerminal.issueCard(smartCardId);
	}
	
	@Test
	public void testKeys() throws Exception{
		issueingTerminal.issueCard(smartCardId);
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
		issueingTerminal.issueCard(smartCardId);
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
		issueingTerminal.issueCard(smartCardId);
		receptionTerminal.init();
		receptionTerminal.reset();
		receptionTerminal.init();
		receptionTerminal.getKeys();
	}
	
	@Test(expected = CardException.class)
	public void testResetKeys() throws Exception{
		issueingTerminal.issueCard(smartCardId);
		receptionTerminal.init();
		receptionTerminal.reset();
		receptionTerminal.getKeys();
	}
	
	@Test(expected = CardException.class)
	public void testDoubleIssue() throws Exception{
		issueingTerminal.issueCard(smartCardId);
		issueingTerminal.issueCard(smartCardId);
	}
	
	@Test(expected = CardException.class)
	public void testDoubleInit() throws Exception{
		receptionTerminal.init();
		receptionTerminal.init();
	}

}
