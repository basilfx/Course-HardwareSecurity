package junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ RSATest.class, GeneralTest.class, TerminalTest.class })
public class AllTests {
	
	
	public static boolean compareArrays(byte[] first, byte[] second){
		if (first.length != second.length){
			return false;
		}
		for (int i = 0; i < first.length; i++){
			if (first[i] != second[i]){
				return false;
			}
		}
		return true;
	}

} 
