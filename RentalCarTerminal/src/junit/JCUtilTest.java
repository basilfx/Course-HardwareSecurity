package junit;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import terminal.BaseTerminal;
import terminal.JCUtil;

public class JCUtilTest {
	
	short short_test = 9536;
	byte short_first = 0x25;
	byte short_second = 0x40;
	int int_test = 123456789;
	byte[] int_test_bytes = {(byte)0x07, (byte)0x5b, (byte)0xcd, (byte)0x15}; 
	String testString = "JUnit provides static methods in the Assert class to test for certain conditions.";
	byte[] testData = testString.getBytes();


	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testBytesToShortCorrect(){
		assertEquals("Bytes To short test correct",short_test, JCUtil.bytesToShort(short_first, short_second));
	}
	
	@Test
	public void testBytesToShortIncorrect(){
		assertFalse("Bytes To short test incorrect",JCUtil.bytesToShort(short_first, short_second) == (short_test+1));
	}
	
	@Test
	public void testShortToBytesCorrect(){
		byte[] bytes = JCUtil.shortToBytes(short_test);
		assertEquals("Short To bytes correct, first byte", short_first, bytes[0]);
		assertEquals("Short To bytes correct, second byte", short_second, bytes[1]);
	}
	
	@Test
	public void testShortToBytesIncorrect(){
		byte[] bytes = JCUtil.shortToBytes(short_test);
		assertFalse("Short To bytes incorrect, first byte", short_first == bytes[1]);
		assertFalse("Short To bytes incorrect, second byte", short_second == bytes[0]);
	}
	
	@Test
	public void testIntToBytesCorrect(){
		assertTrue("Int to Bytes test correct", JCUtil.compareArrays(int_test_bytes, JCUtil.intToBytes(int_test)));
	}
	
	@Test
	public void testBytesToIntCorrect(){
		assertEquals("Short To bytes correct, second byte", int_test, JCUtil.bytesToInt(int_test_bytes));
	}	
	
	@Test
	public void testMergeCorrect(){
		String string = "random";
		byte[] test1 = JCUtil.mergeByteArrays(testData, string.getBytes());
		byte[] test2 = (testString + string).getBytes();
		assertTrue("Test merge arrays correct", JCUtil.compareArrays(test1, test2));
	}
	
	@Test
	public void testMergeIncorrect(){
		String string = "random";
		byte[] test1 = JCUtil.mergeByteArrays(testData, string.getBytes());
		assertFalse("Test merge arrays correct", JCUtil.compareArrays(test1, testString.getBytes()));
	}	
	
}
