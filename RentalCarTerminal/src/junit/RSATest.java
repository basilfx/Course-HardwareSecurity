package junit;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import encryption.RSAHandler;

public class RSATest {
	RSAHandler rsaHandler;
	RSAPublicKey public_key_sc;
	RSAPublicKey public_key_rt;
	RSAPrivateKey private_key_rt;
	byte[] testData = "JUnit provides static methods in the Assert class to test for certain conditions.".getBytes();
	
	@Before
	public void setUp() throws Exception {
		rsaHandler = new RSAHandler();
		public_key_sc = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_sc");
		public_key_rt = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");
		private_key_rt = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_rt");
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testCorrectSignature() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
		byte[] signature = rsaHandler.sign(private_key_rt, public_key_sc.getEncoded());
		boolean result = rsaHandler.verify(public_key_rt, public_key_sc.getEncoded(), signature);
		assertTrue("test Correct Signature",result);
	}
	
	@Test
	public void testIncorrectSignatureKey() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
		byte[] signature = rsaHandler.sign(private_key_rt, public_key_sc.getEncoded());
		boolean result = rsaHandler.verify(public_key_sc, public_key_sc.getEncoded(), signature);
		assertFalse("test incorrect Signature key",result);
	}
	
	@Test
	public void testIncorrectSignatureData() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
		byte[] signature = rsaHandler.sign(private_key_rt, public_key_sc.getEncoded());
		byte[] data = public_key_sc.getEncoded();
		data[0] = (byte) (data[0] + 1);
		boolean result = rsaHandler.verify(public_key_sc, data , signature);
		assertFalse("test incorrect Signature data",result);
	}	
	
	@Test
	public void testCorrectPublicEncryption() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		byte[] data = rsaHandler.encrypt(public_key_rt, testData);
		byte[] decrypted_data = rsaHandler.decrypt(private_key_rt, data);
		assertTrue("test correct public key Encryption", Arrays.equals(testData, decrypted_data));
	}
	@Test
	public void testCorrectPrivateEncryption() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		byte[] data = rsaHandler.encrypt(private_key_rt, testData);
		byte[] decrypted_data = rsaHandler.decrypt(public_key_rt, data);
		assertTrue("test correct private key Encryption", Arrays.equals(testData, decrypted_data));
	}
	
	@Test(expected = BadPaddingException.class)
	public void testIncorrectEncryptionKey() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		byte[] data = rsaHandler.encrypt(public_key_sc, testData);
		@SuppressWarnings("unused")
		byte[] decrypted_data = rsaHandler.decrypt(private_key_rt, data);
	}	
	
	@Test(expected = BadPaddingException.class)
	public void testIncorrectDecryptionKey() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		byte[] data = rsaHandler.encrypt(public_key_rt, testData);
		@SuppressWarnings("unused")
		byte[] decrypted_data = rsaHandler.decrypt(public_key_rt, data);
	}
	
	@Test(expected = IllegalBlockSizeException.class)
	public void testDataToLong() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		@SuppressWarnings("unused")
		byte[] data = rsaHandler.encrypt(public_key_rt, public_key_sc.getEncoded());
	}
	

	

}
