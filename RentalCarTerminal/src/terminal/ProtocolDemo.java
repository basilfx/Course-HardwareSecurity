package terminal;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import encryption.RSAHandler;

/**
 * An application that runs a demo of the car rental protocol.
 * 
 * @author	Jelte
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
			RSAPrivateKey private_key_sc = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_sc");
			smartcard.setPrivateKey(private_key_sc);

			
			issuingCommands.issueCard(smartcard);
			
			System.out.println("-----------------");
			System.out.println("INITIATING THE INIT-PHASE");
			System.out.println("-----------------");
			Thread.sleep(1000);
			
			Car car = new Car();
			car.setId((short)34);
			RSAPublicKey public_key_ct = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_ct");
			car.setPublicKey(public_key_ct);
			RSAPrivateKey private_key_ct = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_ct");
			car.setPrivateKey(private_key_ct);
			byte[] date = new byte[3];
			date[0] = 2;
			date[1] = 11;
			date[2] = 13;
			car.setDate(date);
			
			receptionCommands.initCard(smartcard, car);
			
			System.out.println("-----------------");
			System.out.println("INITIATING THE START-PHASE");
			System.out.println("-----------------");
			Thread.sleep(1000);
			
			carCommands.startCar(car);
			
			System.out.println("-----------------");
			System.out.println("INITIATING THE STOP-PHASE");
			System.out.println("-----------------");
			Thread.sleep(1000);
			
			carCommands.stopCar(car);
			
			terminal.stopRunning();
		}
		catch (Exception e) {
			System.out.println("[Error] Exception in ProtocolDemo: " + e.getMessage());
		}
	}
	

}