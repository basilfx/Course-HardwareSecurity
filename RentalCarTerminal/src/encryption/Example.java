package encryption;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Example {

	
	public static void main(String[] arg) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeySpecException {

		signatureExample();
		encryptDecryptExample();		
	}
	
	public static void encryptDecryptExample() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		String plaintext = "This is the message being encrypted";
		RSAHandler rsaHandler = new RSAHandler();
		RSAPrivateKey privateKey = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_rt");
		RSAPublicKey publicKey = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");
		byte[] encrypted_data = rsaHandler.encrypt(publicKey, plaintext.getBytes());
		byte[] data = rsaHandler.decrypt(privateKey, encrypted_data);
		String text = new String(data);
		System.out.println("Encrypted string: " + plaintext);
		System.out.println("Encrypted data: " + bytes2String(encrypted_data));
		System.out.println("Encrypted length: " + encrypted_data.length);
		System.out.println("Decrypted string: " + text);

	}
	
	public static void signatureExample() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, SignatureException{
		String plaintext = "This is the message being signed";
		
		RSAHandler rsaHandler = new RSAHandler();
		
		//Read private key from file and generate signature
		RSAPrivateKey privateKey = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_rt");
		byte[] signature = rsaHandler.sign(privateKey, plaintext.getBytes());
		
		System.out.println("Input data: " + plaintext);
		System.out.println("Signature: " + bytes2String(signature));
		System.out.println("Signature length: " + signature.length);
		
		//Read public key from file and verify signature
		RSAPublicKey publicKey = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_rt");
		boolean verify = rsaHandler.verify(publicKey, plaintext.getBytes(), signature);
				
		System.out.println("Verify signature: " + verify);
	}
	
	
	private static String bytes2String(byte[] bytes) {
	    StringBuilder string = new StringBuilder();
	    for (byte b : bytes) {
	        String hexString = Integer.toHexString(0x00FF & b);
	        string.append(hexString.length() == 1 ? "0" + hexString : hexString);
	    }
	    return string.toString();
	}
}
