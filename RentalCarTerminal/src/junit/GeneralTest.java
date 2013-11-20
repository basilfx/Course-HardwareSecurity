package junit;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import terminal.BaseTerminal;

public class GeneralTest {
	
	short test = 9536;
	byte first = 0x25;
	byte second = 0x40;
	String testString = "JUnit provides static methods in the Assert class to test for certain conditions.";
	byte[] testData = testString.getBytes();


	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testBytes2ShortCorrect(){
		assertEquals("Bytes 2 short test correct",test, BaseTerminal.bytes2short(first, second));
	}
	
	@Test
	public void testBytes2ShortIncorrect(){
		assertFalse("Bytes 2 short test incorrect",BaseTerminal.bytes2short(first, second) == (test+1));
	}
	
	@Test
	public void testShort2BytesCorrect(){
		byte[] bytes = BaseTerminal.short2bytes(test);
		assertEquals("Short 2 bytes correct, first byte", first, bytes[0]);
		assertEquals("Short 2 bytes correct, second byte", second, bytes[1]);
	}
	
	@Test
	public void testShort2BytesIncorrect(){
		byte[] bytes = BaseTerminal.short2bytes(test);
		assertFalse("Short 2 bytes incorrect, first byte", first == bytes[1]);
		assertFalse("Short 2 bytes incorrect, second byte", second == bytes[0]);
	}
	
	@Test
	public void testMergeCorrect(){
		String string = "random";
		byte[] test1 = BaseTerminal.mergeByteArrays(testData, string.getBytes());
		byte[] test2 = (testString + string).getBytes();
		assertTrue("Test merge arrays correct", AllTests.compareArrays(test1, test2));
	}
	
	@Test
	public void testMergeIncorrect(){
		String string = "random";
		byte[] test1 = BaseTerminal.mergeByteArrays(testData, string.getBytes());
		assertFalse("Test merge arrays correct", AllTests.compareArrays(test1, testString.getBytes()));
	}
	
}
