package terminal;

import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

/**
 * Base class for the Terminal applications.
 * 
 * @author Jelte & Erwin.
 */
public class Terminal {
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

	// The card applet.
	protected CardChannel applet;
	
	private boolean running;

	/**
	 * Constructs the terminal application.
	 * @throws InterruptedException 
	 */
	public Terminal() throws InterruptedException {
		
		running = true;
		
		(new CardThread()).start();
		Thread.sleep(2000);
	}

	class CardThread extends Thread {
		public void run() {
			try {
				TerminalFactory tf = TerminalFactory.getDefault();
				CardTerminals ct = tf.terminals();
				List<CardTerminal> cs = ct.list(CardTerminals.State.CARD_PRESENT);

				if (cs.isEmpty()) {
					JCUtil.log("No terminals with a card found.");
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
										JCUtil.log("Card does not contain applet!");
										sleep(2000);
										continue;
									}
								} catch (CardException e) {
									JCUtil.log("Couldn't connect to card!");
									sleep(2000);
									continue;
								}
							} else {
								JCUtil.log("No card present!");
								sleep(2000);
								continue;
							}
						}
					} catch (CardException e) {
						JCUtil.log("Card status problem!");
					}
				}
			} catch (Exception e) {
				JCUtil.log("ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public boolean isCardPresent(){		
		TerminalFactory tf = TerminalFactory.getDefault();
		CardTerminals ct = tf.terminals();
		List<CardTerminal> cs;
		try {
			cs = ct.list(CardTerminals.State.CARD_PRESENT);
			for (CardTerminal c : cs) {
				if (c.isCardPresent()){
					return true;
				}
			}
		} catch (CardException e) {
		}
		return false;
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
		JCUtil.log(capdu);
		ResponseAPDU rapdu = applet.transmit(capdu);
		JCUtil.log(rapdu);

		return rapdu;
	}

	public void stopRunning() throws InterruptedException {
		running = false;
		Thread.sleep(1000);
	}
	

}