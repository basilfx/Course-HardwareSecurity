package com.rental.terminal.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Base dialog
 * 
 * @author Bas Stottelaar
 * @author Jeroen Senden
 */
public abstract class BaseDialog extends Dialog {
	protected int result;

    public BaseDialog(Shell parent) {
        super(parent, SWT.NONE);
    }

    public int open() {
    	// Default is canceled
        this.result = 1;
        
        this.setup();
        
        this.getShell().open();
        this.getShell().layout();
        
        Display display = getParent().getDisplay();
        
        while (!this.getShell().isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        
        // Return dialog result
        return this.result;
    }
    
    public void close(int result) {
    	this.result = result;
    	
    	this.getShell().close();
    	this.getShell().dispose();
    	
    }
    
    public abstract void setup();
    
    public abstract Shell getShell();
}
