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
import terminal.Terminal;
import encryption.RSAHandler;

public class TerminalTest {
	
	static IssuingCommandsHandler issueCommands;
	static ReceptionCommandsHandler receptionCommands;
	static CarCommandsHandler carCommands;
	static RSAHandler rsaHandler;
	static Terminal terminal;
	static Smartcard smartcard;
	
	static short testSC_ID = 123;

	@BeforeClass
	public static void classSetup() throws Exception{
		terminal = new Terminal();
		issueCommands = new IssuingCommandsHandler(terminal);
		receptionCommands = new ReceptionCommandsHandler(terminal);
		carCommands = new CarCommandsHandler(terminal);
		
		rsaHandler = new RSAHandler();
		smartcard = new Smartcard();
		smartcard.setScId(testSC_ID);			
		RSAPublicKey public_key_sc = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_sc");
		smartcard.setPublicKey(public_key_sc);
		
		issueCommands.issueCard(smartcard);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		receptionCommands.reset();
	}
	
	@Test (expected = CardException.class)
	public void testIssue() throws Exception{
		issueCommands.issueCard(smartcard);
	}
	
	@Test
	public void testKeys() throws Exception{
		byte[] first_pubkey = smartcard.getPublicKey().getEncoded();
		receptionCommands.initCard(smartcard);
		assertEquals("Check if smart card id matches", testSC_ID, smartcard.getScId());		
		assertTrue("Check if pubkey matches", Arrays.equals(first_pubkey, smartcard.getPublicKey().getEncoded()));
		
		RSAPublicKey public_key_rt = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");
		byte[] data = JCUtil.mergeByteArrays(JCUtil.shortToBytes(testSC_ID), smartcard.getPublicKey().getEncoded());
		boolean result = rsaHandler.verify(public_key_rt, data, smartcard.getSignature());
		assertTrue("validate signature", result);
	}
	
	@Test
	public void testSetMileage() throws Exception{
		receptionCommands.initCard(smartcard);
		int start_mileage = 500;
		int final_mileage = 1000;
		carCommands.setMileage(start_mileage);
		carCommands.startCar(smartcard);
		
		carCommands.setMileage(final_mileage);
		carCommands.stopCar();
		
		receptionCommands.read(smartcard);
		assertEquals("Start mileage", start_mileage, receptionCommands.start_mileage);
		assertEquals("Final mileage", final_mileage, receptionCommands.final_mileage);
		
	}
	
	@Test(expected = CardException.class)
	public void testReset() throws Exception{
		receptionCommands.initCard(smartcard);
		receptionCommands.reset();
		receptionCommands.initCard(smartcard);
		receptionCommands.getKeys(smartcard);
	}
	
	@Test(expected = CardException.class)
	public void testResetKeys() throws Exception{
		receptionCommands.initCard(smartcard);
		receptionCommands.reset();
		receptionCommands.getKeys(smartcard);
	}
	
	
	@Test(expected = CardException.class)
	public void testDoubleInit() throws Exception{
		receptionCommands.initCard(smartcard);
		receptionCommands.initCard(smartcard);
	}

}
