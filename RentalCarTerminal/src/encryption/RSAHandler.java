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

	/** Reads a stored public key from the file system and returns it as a RSAPublicKey
	 * @param filename: the location of the key
	 * @return RSAPublicKey read from the filesystem
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
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
	
	/** Read a stored private key from the filesystem and return it as a RSAPrivateKey object
	 * @param filename: the location of the key
	 * @return RSAPrivateKey from the filesystem
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
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
	
	/** Converts a modulus and exponent into a public key
	 * @param modulus
	 * @param exponent
	 * @return RSAPublicKey corresponding to the modulus and exponent
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public RSAPublicKey getPublicKeyFromModulusExponent(byte[] modulus, byte[] exponent) throws NoSuchAlgorithmException, InvalidKeySpecException{
		RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent));
		KeyFactory factory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
		return (RSAPublicKey) factory.generatePublic(spec);
	}
	
	/** Signs the data using a private key. The algorithm used is SHA1 with RSA, PKCS1
	 * @param privateKey: the key with witch the data will be signed.
	 * @param data: The data to be signed
	 * @return returns the signature
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public byte[] sign(RSAPrivateKey privateKey, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
		Signature instance = Signature.getInstance(SIGNATURE_ALGORITHM);
		instance.initSign(privateKey);
		instance.update(data);
		return instance.sign();
	}
	
	/** Verifies a signature based on a public key and data.
	 * @param publicKey: The public key corresponding the the private key used to sign the data.
	 * @param data: The data which is signed by the signature.
	 * @param signature: The signature to be verified.
	 * @return True if the signature could be verified, false otherwise
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public boolean verify(RSAPublicKey publicKey, byte[] data, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
		Signature instance = Signature.getInstance(SIGNATURE_ALGORITHM);
		instance.initVerify(publicKey);
		instance.update(data);
		return instance.verify(signature);
	}
	
	/** Asymmetrically encrypt the data using a RSA key.
	 * @param key: an RSA public or private key.
	 * @param data: The data to be encrypted.
	 * @return returns the encrypted data.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] encrypt(Key key, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(data);		
	}
	
	/** Asymmetrically decrypt the data using a RSA key.
	 * @param key: an RSA public or private key.
	 * @param data: The data to be decrypted.
	 * @return returns the decrypted data.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] decrypt(Key key, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(data);		
	}
	
	/** Signs a key using a RSA private key.
	 * @param privateKey: The key which will be used to sign the key.
	 * @param key_to_be_signed: The key which will be signed.
	 * @return The signature.
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 */
	public byte[] getKeySignature(RSAPrivateKey privateKey, Key key_to_be_signed) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
		return sign(privateKey, key_to_be_signed.getEncoded());
	}

}
