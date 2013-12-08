package com.rental.terminal.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Builder for a message box
 */
public class MessageBoxBuilder {
	private MessageBox messageBox;

	public MessageBoxBuilder(Shell shell, int style) {
		this.messageBox = new MessageBox(shell, style);
	}
	
	public MessageBoxBuilder(Shell shell) {
		this(shell, SWT.NONE);
	}
	
	public MessageBoxBuilder(BaseDialog dialog) {
		this(dialog.getShell());
	}
	
	public MessageBoxBuilder(BaseDialog dialog, int style) {
		this(dialog.getShell(), style);
	}
	
	public MessageBoxBuilder setTitle(String title) {
		this.messageBox.setText(title);
		
		return this;
	}
	
	public MessageBoxBuilder setMessage(String message) {
		this.messageBox.setMessage(message);
		
		return this;
	}
	
	public void open() {
		this.messageBox.open();
	}
}