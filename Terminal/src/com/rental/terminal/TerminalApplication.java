package com.rental.terminal;

import javax.swing.UIManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TerminalApplication {
	/**
	 * @var Logger instance
	 */
	private final static Logger LOGGER = Logger.getLogger(TerminalApplication.class.getName());
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Set native UI
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { 
        	LOGGER.log(Level.WARNING, "Error setting native UI", e);
        }

		// Create terminal chooser
		new TerminalChooser();
	}
}
