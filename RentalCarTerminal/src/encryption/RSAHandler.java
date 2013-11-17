package encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAHandler {
	
	private static final String ENCRYPTION_ALGORITHM = "RSA";
	private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

	public RSAPublicKey readPublicKeyFromFileSystem(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		File public_key_file = new File(filename);
		FileInputStream in = new FileInputStream(public_key_file);
		int length = in.available();
		byte[] data = new byte[length];
		in.read(data);
		in.close();		
		X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
		KeyFactory factory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
		return (RSAPublicKey) factory.generatePublic(spec);
	}

	public RSAPrivateKey readPrivateKeyFromFileSystem(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		File private_key_file = new File(filename);
		FileInputStream in = new FileInputStream(private_key_file);
		int length = in.available();
		byte[] data = new byte[length];
		in.read(data);
		in.close();
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(data);
		KeyFactory factory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
		return (RSAPrivateKey) factory.generatePrivate(spec);
	}
	
	public RSAPublicKey getPublicKeyFromModulusExponent(byte[] modulus, byte[] exponent) throws NoSuchAlgorithmException, InvalidKeySpecException{
		RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent));
		KeyFactory factory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
		return (RSAPublicKey) factory.generatePublic(spec);
	}
	
	public byte[] sign(RSAPrivateKey privateKey, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
		Signature instance = Signature.getInstance(SIGNATURE_ALGORITHM);
		instance.initSign(privateKey);
		instance.update(data);
		return instance.sign();
	}
	
	public boolean verify(RSAPublicKey publicKey, byte[] data, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
		Signature instance = Signature.getInstance(SIGNATURE_ALGORITHM);
		instance.initVerify(publicKey);
		instance.update(data);
		return instance.verify(signature);
	}
	
	public byte[] encrypt(RSAPublicKey publicKey, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(data);		
	}
	
	public byte[] decrypt(RSAPrivateKey privateKey, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(data);		
	}
	
	public byte[] getKeySignature(RSAPrivateKey privateKey, Key key_to_be_signed) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
		return sign(privateKey, key_to_be_signed.getEncoded());
	}

}
