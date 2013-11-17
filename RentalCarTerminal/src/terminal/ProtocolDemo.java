package terminal;

import javax.smartcardio.CardException;

/**
 * An application that runs a demo of the car rental protocol.
 * 
 * @author	Group 3
 */
public class ProtocolDemo {
	
	private Integer smartCartId;

	/**
	 * Constructor.
	 */
	public ProtocolDemo() {
		smartCartId = 1337;
	}
	
	public Integer getSmartCartId() {
		return smartCartId;
	}

	/**
	 * Run a demo of the car rental protocol.
	 * 
	 * @param	arg	command line arguments.
	 */
	public static void main(String[] arg) {
		ProtocolDemo demo = new ProtocolDemo();
		
		IssuingTerminal terminal = new IssuingTerminal();
		try {
			terminal.issueCard(demo.getSmartCartId());
		}
		catch (Exception e) {
			System.out.println("[Error] Exception in ProtocolDemo: " + e.getMessage());
		}
	}
}