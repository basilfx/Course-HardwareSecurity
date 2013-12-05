package com.rental.terminal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Strings;
import com.rental.terminal.model.Car;

public class CarDialog extends Dialog {
	
	private class View {
		private Shell shell;
		private Button ok;
		private Button cancel;
		
		private Text name;
		private Text publicKey;
		
		private View(Shell parent) {
			this.shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			
			this.shell.setLayout(new GridLayout(1, false));
	        this.shell.setSize(450, 300);
	        this.shell.setText("Add car");
	        	        
	        // Name field
	        this.name = new Text(this.shell, SWT.BORDER | SWT.SINGLE);
	        this.name.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        
	        // Public key field
	        this.publicKey = new Text(this.shell, SWT.BORDER | SWT.MULTI);
	        this.publicKey.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        
	        // OK/Cancel buttons
	        this.ok = new Button(shell, SWT.NONE);
	        this.ok.setText("OK");
	        
	        this.cancel = new Button(shell, SWT.NONE);
	        this.cancel.setText("Cancel");
		}
	}
	
    private int result;
    private Car car;
 
    private View view;
    

    public CarDialog(Shell parent, int style) {
        super(parent, style);
    }

    public CarDialog(Shell parent) {
        this(parent, SWT.NONE);
    }

    public int open() {
    	// Default is canceled
        this.result = 1;
        
        this.view = new View(getParent());
        
        this.setupForm();
        this.setupButtons();
        
        this.view.shell.open();
        this.view.shell.layout();
        
        Display display = getParent().getDisplay();
        
        while (!this.view.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        
        // Return dialog result
        return this.result;
    }
    
    public void close() {
    	CarDialog.this.view.shell.close();
    	CarDialog.this.view.shell.dispose();
    	
    }
    
    public void setupForm() {
    	this.view.name.setText(Strings.nullToEmpty(this.car.getName()));
		this.view.publicKey.setText(Strings.nullToEmpty(this.car.getPublicKey()));
    }
    
    public void setupButtons() {
    	this.view.ok.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				CarDialog.this.result = 0;
				
				String name = CarDialog.this.view.name.getText();
				String publicKey = CarDialog.this.view.publicKey.getText();
				
				if (name.isEmpty() || publicKey.isEmpty()) {
					MessageBox message = new MessageBox(CarDialog.this.view.shell);
					
					message.setMessage("Name or public key missing");
					message.setText("Validation error");
					
					message.open();
					
					// Stop here
					return;
				}
				
				CarDialog.this.car.setName(name);
				CarDialog.this.car.setPublicKey(publicKey);
				
				// Done
				CarDialog.this.close();
			}
		});
    	
    	this.view.cancel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				CarDialog.this.result = 1;
				
				// Done
				CarDialog.this.close();
			}
		});
    }

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}
}
