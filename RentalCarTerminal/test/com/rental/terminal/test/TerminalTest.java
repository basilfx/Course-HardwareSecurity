package com.rental.terminal.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.smartcardio.CardException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rental.terminal.Car;
import com.rental.terminal.CarCommandsHandler;
import com.rental.terminal.IssuingCommandsHandler;
import com.rental.terminal.JCUtil;
import com.rental.terminal.ReceptionCommandsHandler;
import com.rental.terminal.Smartcard;
import com.rental.terminal.Terminal;
import com.rental.terminal.encryption.RSAHandler;


public class TerminalTest {
	
	static IssuingCommandsHandler issueCommands;
	static ReceptionCommandsHandler receptionCommands;
	static CarCommandsHandler carCommands;
	static RSAHandler rsaHandler;
	static Terminal terminal;
	static Smartcard smartcard;
	static Car car;
	
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
		RSAPrivateKey private_key_sc = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_sc");
		smartcard.setPrivateKey(private_key_sc);
		
		issueCommands.issueCard(smartcard);
		
		car = new Car();
		car.setId((short)34);
		RSAPublicKey public_key_ct = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_ct");
		car.setPublicKey(public_key_ct);
		RSAPrivateKey private_key_ct = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_ct");
		car.setPrivateKey(private_key_ct);
		byte[] date = new byte[3];
		date[0] = 2;
		date[1] = 11;
		date[2] = 13;
		car.setDate(date);
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
		receptionCommands.initCard(smartcard,car);
		assertEquals("Check if smart card id matches", testSC_ID, smartcard.getScId());		
		assertTrue("Check if pubkey matches", Arrays.equals(first_pubkey, smartcard.getPublicKey().getEncoded()));
		
		RSAPublicKey public_key_rt = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");
		byte[] data = JCUtil.mergeByteArrays(JCUtil.shortToBytes(testSC_ID), smartcard.getPublicKey().getEncoded());
		boolean result = rsaHandler.verify(public_key_rt, data, smartcard.getSignature());
		assertTrue("validate signature", result);
	}
	
	@Test
	public void testSetMileage() throws Exception{
		receptionCommands.initCard(smartcard,car);
		int start_mileage = 500;
		int final_mileage = 1000;
		carCommands.setMileage(start_mileage);
		carCommands.startCar(car);
		
		carCommands.setMileage(final_mileage);
		carCommands.stopCar(car);
		
		receptionCommands.read(smartcard,car);
		assertEquals("Start mileage", start_mileage, car.getStartMileage());
		assertEquals("Final mileage", final_mileage, car.getFinalMileage());
		
	}
	
	@Test(expected = CardException.class)
	public void testReset() throws Exception{
		receptionCommands.initCard(smartcard,car);
		receptionCommands.reset();
		receptionCommands.initCard(smartcard,car);
		receptionCommands.getKeys(smartcard);
	}
	
	@Test(expected = CardException.class)
	public void testResetKeys() throws Exception{
		receptionCommands.initCard(smartcard,car);
		receptionCommands.reset();
		receptionCommands.getKeys(smartcard);
	}
	
	
	@Test(expected = CardException.class)
	public void testDoubleInit() throws Exception{
		receptionCommands.initCard(smartcard,car);
		receptionCommands.initCard(smartcard,car);
	}

}
