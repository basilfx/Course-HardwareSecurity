package terminal;

import javax.smartcardio.CardException;

/**
 * An application that runs a demo of the car rental protocol.
 * 
 * @author	Group 3
 */
public class ProtocolDemo {

	/**
	 * Constructor.
	 */
	public ProtocolDemo() {}

	/**
	 * Run a demo of the car rental protocol.
	 * 
	 * @param	arg	command line arguments.
	 */
	public static void main(String[] arg) {
		IssuingTerminal terminal = new IssuingTerminal();
		try {
			terminal.issueCard();
		}
		catch (Exception e) {
			System.out.println("[Error] Exception in ProtocolDemo: " + e.getMessage());
		}
	}
}