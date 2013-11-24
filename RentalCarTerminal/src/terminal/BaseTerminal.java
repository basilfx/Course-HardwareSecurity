package terminal;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import javax.swing.JPanel;

import encryption.RSAHandler;

/**
 * Base class for the Terminal applications.
 * 
 * @author Group 3
 */
public class BaseTerminal extends JPanel {
	protected static final byte[] APPLET_AID = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x01 };

	// Select-instruction.
	protected static final byte INS_SELECT = (byte) 0xA4;

	// Error codes.
	protected static final short SW_NO_ERROR = (short) 0x9000;

	// The C-APDU that is used for selecting the applet on the card (see section
	// 9.9 of GP Card spec v2.1.1).
	protected static final CommandAPDU SELECT_APDU = new CommandAPDU((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, APPLET_AID);

	// States.
	protected static final int STATE_INIT = 0;
	protected static final int STATE_ISSUED = 1;

	/** Keys Bytes */
	private static final byte CLA_KEYS = (byte) 0xB7;
	private static final byte KEYS_START = (byte) 0x01;
	private static final byte GET_PUBLIC_KEY_MODULUS = (byte) 0x02;
	private static final byte GET_PUBLIC_KEY_EXPONENT = (byte) 0x03;

	protected static final int BLOCKSIZE = 128;
	protected static final int NONCESIZE = 2;
	protected static final int SCIDSIZE = 2;
	protected static final int MILEAGESIZE = 4;

	RSAHandler rsaHandler;
	short tempNonce;
	public Smartcard currentSmartcard;

	// Last accessed directory of file browser.
	private File currentDir = new File(".");

	// Cipher used for encryption/decryption.
	protected Cipher cipher;

	// The card applet.
	protected CardChannel applet;
	
	private boolean running;

	/**
	 * Constructs the terminal application.
	 */
	public BaseTerminal() {
		rsaHandler = new RSAHandler();
		currentSmartcard = new Smartcard();
		running = true;
		(new CardThread()).start();
	}

	class CardThread extends Thread {
		public void run() {
			try {
				TerminalFactory tf = TerminalFactory.getDefault();
				CardTerminals ct = tf.terminals();
				List<CardTerminal> cs = ct.list(CardTerminals.State.CARD_PRESENT);

				if (cs.isEmpty()) {
					log("No terminals with a card found.");
					return;
				}

				while (running) {
					try {
						for (CardTerminal c : cs) {
							if (c.isCardPresent()) {
								try {
									Card card = c.connect("*");
									try {
										// Select applet.
										applet = card.getBasicChannel();
										ResponseAPDU resp = applet.transmit(SELECT_APDU);

										if (resp.getSW() != 0x9000) {
											throw new Exception("Select failed");
										}

										// Wait for the card to be removed
										while (c.isCardPresent() && running) {
										}

										break;
									} catch (Exception e) {
										log("Card does not contain applet!");
										sleep(2000);
										continue;
									}
								} catch (CardException e) {
									log("Couldn't connect to card!");
									sleep(2000);
									continue;
								}
							} else {
								log("No card present!");
								sleep(2000);
								continue;
							}
						}
					} catch (CardException e) {
						log("Card status problem!");
					}
				}
			} catch (Exception e) {
				log("ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void getKeys() throws CardException, NoSuchAlgorithmException, InvalidKeySpecException {
		CommandAPDU capdu = new CommandAPDU(CLA_KEYS, KEYS_START, (byte) 0, (byte) 0, SCIDSIZE + BLOCKSIZE);
		ResponseAPDU rapdu = sendCommandAPDU(capdu);
		
		byte[] data = rapdu.getData();
		
		log("Card ID has been read by getKeys(): " + Short.toString(bytesToShort(data[0], data[1])));
		
		// The first two bytes of the response represent the SC ID.
		currentSmartcard.setScId(bytesToShort(data[0], data[1]));
		
		// The second to 130th byte represent the signature.
		currentSmartcard.setSignature(subArray(data, 2, BLOCKSIZE));
		

		capdu = new CommandAPDU(CLA_KEYS, GET_PUBLIC_KEY_MODULUS, (byte) 0, (byte) 0, BLOCKSIZE);
		rapdu = sendCommandAPDU(capdu);
		byte[] modulus = rapdu.getData();

		// TODO unsure of exponent length
		capdu = new CommandAPDU(CLA_KEYS, GET_PUBLIC_KEY_EXPONENT, (byte) 0, (byte) 0, BLOCKSIZE);
		rapdu = sendCommandAPDU(capdu);
		byte[] exponent = rapdu.getData();
		currentSmartcard.setPublicKey(rsaHandler.getPublicKeyFromModulusExponent(modulus, exponent));
		
		log("pubkey_sc has been read by getKeys(). Modulus: " + currentSmartcard.getPublicKey().getModulus().toString() + ", exponent: " + currentSmartcard.getPublicKey().getPublicExponent().toString());
	}

	/**
	 * Handles (button) events.
	 * 
	 * TODO!
	 * 
	 * @param ae
	 *            Event indicating a button was pressed.
	 */
	public void actionPerformed(ActionEvent ae) {
		try {
			Object src = ae.getSource();

			//
			// TODO!
			//

		} catch (Exception e) {
			System.out.println("Error in action listener: " + e.getMessage());
		}
	}

	/**
	 * Generates an instance of RSAPrivateKey from a key-file.
	 * 
	 * @param filePath
	 *            The path of the key-file.
	 * 
	 * @return The RSAPrivateKey instance.
	 */
	protected RSAPrivateKey getRSAPrivateKeyFromFile(String filePath) throws Exception {
		byte[] data = readFile(filePath);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(data);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		RSAPrivateKey key = (RSAPrivateKey) factory.generatePrivate(spec);

		return key;
	}

	/**
	 * Generates an instance of RSAPublicKey from a key-file.
	 * 
	 * @param filePath
	 *            The path of the key-file.
	 * 
	 * @return The RSAPublicKey instance.
	 */
	protected RSAPublicKey getRSAPublicKeyFromFile(String filePath) throws Exception {
		byte[] data = readFile(filePath);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		RSAPublicKey key = (RSAPublicKey) factory.generatePublic(spec);

		return key;
	}

	/**
	 * Sends a command to the card.
	 * 
	 * @param capdu
	 *            the command to send.
	 * 
	 * @return the response from the card.
	 * 
	 * @throws CardTerminalException
	 *             if something goes wrong.
	 */
	protected ResponseAPDU sendCommandAPDU(CommandAPDU capdu) throws CardException {
		log(capdu);
		ResponseAPDU rapdu = applet.transmit(capdu);
		log(rapdu);

		return rapdu;
	}

	protected String toHexString(byte[] in) {
		StringBuilder out = new StringBuilder(2 * in.length);
		for (int i = 0; i < in.length; i++) {
			out.append(String.format("%02x ", (in[i] & 0xFF)));
		}

		return out.toString().toUpperCase();
	}

	/**
	 * Writes <code>obj</code> to the log.
	 * 
	 * @param obj
	 *            the message to write to the log.
	 */
	protected void log(ResponseAPDU obj) {
		System.out.println(obj.toString());
	}

	/**
	 * Writes <code>obj</code> to the log.
	 * 
	 * @param obj
	 *            the message to write to the log.
	 */
	protected void log(CommandAPDU obj) {
		System.out.println(obj.toString());
	}

	/**
	 * Writes <code>obj</code> to the log.
	 * 
	 * @param obj
	 *            the message to write to the log.
	 */
	protected void log(Object obj) {
		System.out.println(obj.toString());
	}

	/**
	 * Pops up dialog to ask user to select file and reads the file.
	 * 
	 * @param String
	 *            file_path The path of the file to read.
	 * 
	 * @return byte array with contents of the file.
	 * 
	 * @throws IOException
	 *             if file could not be read.
	 */
	protected byte[] readFile(String file_path) throws IOException {
		/*
		 * JFileChooser chooser = new JFileChooser();
		 * chooser.setCurrentDirectory(currentDir); int n =
		 * chooser.showOpenDialog(this); if (n != JFileChooser.APPROVE_OPTION) {
		 * throw new IOException("No file selected"); } File file =
		 * chooser.getSelectedFile();
		 */

		File file = new File(file_path);
		FileInputStream in = new FileInputStream(file);
		int length = in.available();
		byte[] data = new byte[length];
		in.read(data);
		in.close();
		currentDir = file.getParentFile();

		return data;
	}

	/**
	 * Gets an unsigned byte array representation of <code>big</code>. A leading
	 * zero (present only to hold sign bit) is stripped.
	 * 
	 * @param big
	 *            a big integer.
	 * 
	 * @return a byte array containing a representation of <code>big</code>.
	 */
	protected byte[] getBytes(BigInteger big) {
		byte[] data = big.toByteArray();

		if (data[0] == 0) {
			byte[] tmp = data;
			data = new byte[tmp.length - 1];
			System.arraycopy(tmp, 1, data, 0, tmp.length - 1);
		}

		return data;
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

	public static boolean compareArrays(byte[] first, byte[] second) {
		if (first.length != second.length) {
			return false;
		}
		for (int i = 0; i < first.length; i++) {
			if (first[i] != second[i]) {
				return false;
			}
		}
		return true;
	}
	
	public void stopRunning() {
		running = false;
	}
}