package terminal;

import java.security.interfaces.RSAPublicKey;

class Smartcard{
	
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
	
}