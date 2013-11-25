package junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ RSATest.class, JCUtilTest.class, TerminalTest.class })
public class AllTests {
	
} 
