package terminal;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.smartcardio.CardException;

/**
 * An application that runs a demo of the car rental protocol.
 * 
 * @author	Group 3
 */
public class ProtocolDemo {
	
	private short smartCartId;

	/**
	 * Constructor.
	 */
	public ProtocolDemo() {
		smartCartId = (short) 1337;
	}
	
	public short getSmartCartId() {
		return smartCartId;
	}

	/**
	 * Run a demo of the car rental protocol.
	 * 
	 * @param	arg	command line arguments.
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] arg) throws InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		ProtocolDemo demo = new ProtocolDemo();
		IssuingTerminal terminalIT = new IssuingTerminal();
		ReceptionTerminal terminalRT = new ReceptionTerminal();
		CarTerminal terminalCT = new CarTerminal();
		
		
		// Wait 2 seconds so that the smart card can be selected.
		System.out.println("wait 2 seconds so that card can be selected.");
		Thread.sleep(2000);
		System.out.println("proceed...");
		
		try {
			System.out.println("-----------------");
			System.out.println("INITIATING THE ISSUE-PHASE");
			System.out.println("-----------------");
			
			terminalIT.issueCard(demo.getSmartCartId());
			terminalIT.stopRunning();
			
			System.out.println("-----------------");
			System.out.println("INITIATING THE INIT-PHASE");
			System.out.println("-----------------");
			Thread.sleep(1000);
			
			terminalRT.initCard();
			terminalRT.stopRunning();
			
			System.out.println("-----------------");
			System.out.println("INITIATING THE START-PHASE");
			System.out.println("-----------------");
			Thread.sleep(1000);
			
			terminalCT.startCar();
			
			System.out.println("-----------------");
			System.out.println("INITIATING THE STOP-PHASE");
			System.out.println("-----------------");
			Thread.sleep(1000);
			
			terminalCT.stopCar();
			
			terminalCT.stopRunning();
		}
		catch (Exception e) {
			System.out.println("[Error] Exception in ProtocolDemo: " + e.getMessage());
		}
	}
	

}