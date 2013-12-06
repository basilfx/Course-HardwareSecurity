package com.rental.terminal;

import javax.swing.UIManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {
	/**
	 * @var Logger instance
	 */
	private final static Logger LOGGER = Logger.getLogger(Application.class.getName());
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
		// Set native UI
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { 
        	LOGGER.log(Level.WARNING, "Error setting native UI", e);
        }

		// Open the main window
		new MainWindow();
	}
}
