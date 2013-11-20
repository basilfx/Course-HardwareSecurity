package terminal;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;

import encryption.RSAHandler;

public class Smartcard{
	
	private byte[] signature;
	private RSAPublicKey public_key;
	private short sc_id;
	
	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
	public byte[] getSignature() {
		return signature;
	}
	public void setPublicKey(RSAPublicKey public_key) {
		this.public_key = public_key;
	}
	public RSAPublicKey getPublicKey() {
		return public_key;
	}
	public void setScId(short sc_id) {
		this.sc_id = sc_id;
	}
	public short getScId() {
		return sc_id;
	}
	
	public boolean validateSignature(RSAPublicKey pubkey) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException{
		byte[] data = BaseTerminal.mergeByteArrays(BaseTerminal.short2bytes(sc_id), public_key.getEncoded());
		RSAHandler rsaHandler = new RSAHandler();
		return rsaHandler.verify(pubkey, data, signature);
	}
	
}