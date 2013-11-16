package encryption;

public class KeyGenerator {

	
	public static void main(String[] arg) {
	    new RSAKeyGen("keys/public_key_rt","keys/private_key_rt");
	    new RSAKeyGen("keys/public_key_ct","keys/private_key_ct");
	    new RSAKeyGen("keys/public_key_sc","keys/private_key_sc");
	}
}
