package terminal;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigInteger;
import java.util.List;
import javax.swing.*;

import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;

import javax.smartcardio.*;

/**
 * Base class for the Terminal applets.
 * 
 * @author	Group 3
 */
public class BaseTerminal {
	protected static final int BLOCKSIZE = 128;
	protected static final byte[] APPLET_AID = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x01 };
	
	// Select-instruction.
	protected static final byte INS_SELECT = (byte) 0xA4;
	
	// Error codes.
	protected static final short SW_NO_ERROR = (short) 0x9000;

	// The C-APDU that is used for selecting the applet on the card (see section 9.9 of GP Card spec v2.1.1).
	protected static final CommandAPDU SELECT_APDU = new CommandAPDU((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, APPLET_AID);

	// States.
	protected static final int STATE_INIT = 0;
	protected static final int STATE_ISSUED = 1;

	// The card applet.
	protected CardChannel applet;

	/**
	 * Constructs the terminal application.
	 */
	public RentalCarTerminal() {
		(new CardThread()).start();
	}

	class CardThread extends Thread {
		public void run() {
			try {
				TerminalFactory tf = TerminalFactory.getDefault();
				CardTerminals ct = tf.terminals();
				List<CardTerminal> cs = ct
						.list(CardTerminals.State.CARD_PRESENT);
				if (cs.isEmpty()) {
					log("No terminals with a card found.");
					return;
				}

				while (true) {
					try {
						for (CardTerminal c : cs) {
							if (c.isCardPresent()) {
								try {
									Card card = c.connect("*");
									try {
										applet = card.getBasicChannel();
										ResponseAPDU resp = applet
												.transmit(SELECT_APDU);
										if (resp.getSW() != 0x9000) {
											throw new Exception("Select failed");
										}

										// Wait for the card to be removed
										while (c.isCardPresent())
											;
										break;
									} catch (Exception e) {
										log("Card does not contain CryptoApplet?!");
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

	/**
	 * Handles (button) events.
	 * 
	 * TODO!
	 * 
	 * @param	ae	Event indicating a button was pressed.
	 */
	public void actionPerformed(ActionEvent ae) {
		try {
			Object src = ae.getSource();
			
			//
			// TODO!
			//
			
		}
		catch (Exception e) {
			System.out.println("Error in action listener: " + e.getMessage());
		}
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
	protected ResponseAPDU sendCommandAPDU(CommandAPDU capdu)
			throws CardException {
		log(capdu);
		ResponseAPDU rapdu = applet.transmit(capdu);
		log(rapdu);
		return rapdu;
	}

	protected String toHexString(byte[] in) {
		StringBuilder out = new StringBuilder(2*in.length);
		for(int i = 0; i < in.length; i++) {
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
	 * @return byte array with contents of the file.
	 * 
	 * @throws IOException
	 *             if file could not be read.
	 */
	protected byte[] readFile() throws IOException {
		/*JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(currentDir);
		int n = chooser.showOpenDialog(this);
		if (n != JFileChooser.APPROVE_OPTION) {
			throw new IOException("No file selected");
		}
		File file = chooser.getSelectedFile();
		FileInputStream in = new FileInputStream(file);
		int length = in.available();
		byte[] data = new byte[length];
		in.read(data);
		in.close();
		currentDir = file.getParentFile();
		return data;*/
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

	/**
	 * Creates an instance of this class.
	 * 
	 * @param	arg	command line arguments.
	 */
	public static void main(String[] arg) {
		IssuingTerminal terminal = new IssuingTerminal();
	}
}