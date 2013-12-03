package terminal;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class JCUtil {
	
	/**
	 * Gets an unsigned byte array representation of <code>big</code>. A leading
	 * zero (present only to hold sign bit) is stripped.
	 * 
	 * @param big
	 *            a big integer.
	 * 
	 * @return a byte array containing a representation of <code>big</code>.
	 */
	public static byte[] getBytes(BigInteger big) {
		byte[] data = big.toByteArray();

		if (data[0] == 0) {
			byte[] tmp = data;
			data = new byte[tmp.length - 1];
			System.arraycopy(tmp, 1, data, 0, tmp.length - 1);
		}

		return data;
	}
	
	/**
	 * Converts an unsigned byte array to a signed byte array by pushing a zero byte to the beginning of the array.
	 * 
	 * @param input
	 * @return
	 */
	public static byte[] unsignedToSigned(byte[] input) {
		byte[] output = new byte[input.length + 1];
		output[0] = (byte) 0;
		
		for (int i = 0; i < input.length; i++) {
			output[i + 1] = input[i];
		}
		
		return output;
	}

	public static short bytesToShort(byte first_byte, byte second_byte) {
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(first_byte);
		bb.put(second_byte);
		return (short) (((first_byte & 0xFF) << 8) | (second_byte & 0xFF));
	}

	/**
	 * Convert short to byte array.
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] shortToBytes(short s) {
		return ByteBuffer.allocate(2).putShort(s).array();
	}

	public static byte[] intToBytes(int i) {
		return ByteBuffer.allocate(4).putInt(i).array();
	}

	public static int bytesToInt(byte[] bytes) {
		int result = 0;
		for (int i = 0; i < 4 && i < bytes.length; i++) {
			result <<= 8;
			result |= (int) bytes[i] & 0xFF;
		}
		return result;
	}

	public static byte[] mergeByteArrays(byte[] first, byte[] second) {
		byte[] result = new byte[first.length + second.length];
		for (int i = 0; i < first.length; i++) {
			result[i] = first[i];
		}

		for (int i = 0; i < second.length; i++) {
			result[first.length + i] = second[i];
		}
		return result;
	}
	
	public static byte[] subArray(byte[] input, int offset, int length){
		byte[] result = new byte[length];
		for(int i = 0; i < length; i++){
			result[i] = input[i + offset];
		}
		return result;
	}
	
	protected String toHexString(byte[] in) {
		StringBuilder out = new StringBuilder(2 * in.length);
		for (int i = 0; i < in.length; i++) {
			out.append(String.format("%02x ", (in[i] & 0xFF)));
		}

		return out.toString().toUpperCase();
	}

}
