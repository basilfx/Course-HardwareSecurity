package com.rental.terminal.gui;

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
		// Open the main window
		new MainWindow();
	}
}
