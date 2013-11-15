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
 * Reception Terminal application.
 * 
 * @author	Group 3.
 */
public class ReceptionTerminal extends BaseTerminal {
	
	// CLA of this Terminal.
	static final byte CLA_TERMINAL_RT = (byte) 0x02;

	// Instructions.
	//

	// The card applet.
	CardChannel applet;

	/**
	 * Constructs the terminal application.
	 */
	public ReceptionTerminal() {
		super();
	}
}