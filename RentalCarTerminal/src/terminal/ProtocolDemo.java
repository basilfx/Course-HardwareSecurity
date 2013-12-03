package terminal;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.smartcardio.CardException;

import encryption.RSAHandler;

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
		Terminal terminal = new Terminal();
		
		IssuingCommandsHandler issuingCommands = new IssuingCommandsHandler(terminal);
		ReceptionCommandsHandler receptionCommands = new ReceptionCommandsHandler(terminal);
		CarCommandsHandler carCommands = new CarCommandsHandler(terminal);
		
		
		// Wait 2 seconds so that the smart card can be selected.
		System.out.println("wait 2 seconds so that card can be selected.");
		Thread.sleep(2000);
		System.out.println("proceed...");
		
		try {
			System.out.println("-----------------");
			System.out.println("INITIATING THE ISSUE-PHASE");
			System.out.println("-----------------");
			
			RSAHandler rsaHandler = new RSAHandler();
			Smartcard smartcard = new Smartcard();
			smartcard.setScId(demo.getSmartCartId());			
			RSAPublicKey public_key_sc = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_sc");
			smartcard.setPublicKey(public_key_sc);
			
			issuingCommands.issueCard(smartcard);
			
			System.out.println("-----------------");
			System.out.println("INITIATING THE INIT-PHASE");
			System.out.println("-----------------");
			Thread.sleep(1000);
			
			receptionCommands.initCard(smartcard);
			
			System.out.println("-----------------");
			System.out.println("INITIATING THE START-PHASE");
			System.out.println("-----------------");
			Thread.sleep(1000);
			
			carCommands.startCar(smartcard);
			
			System.out.println("-----------------");
			System.out.println("INITIATING THE STOP-PHASE");
			System.out.println("-----------------");
			Thread.sleep(1000);
			
			carCommands.stopCar();
			
			terminal.stopRunning();
		}
		catch (Exception e) {
			System.out.println("[Error] Exception in ProtocolDemo: " + e.getMessage());
		}
	}
	

}