package com.rental.terminal.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Strings;
import com.rental.terminal.model.CarDB;

public class CarDialog extends Dialog {
	
	private class View {
		private Shell shell;
		private Button ok;
		private Button cancel;
		
		private Text name;
		
		private View(Shell parent) {
			this.shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			
			this.shell.setLayout(new GridLayout(1, false));
	        this.shell.setSize(450, 300);
	        this.shell.setText("Add car");
	        	        
	        // Name field
	        Composite nameField = new Composite(this.shell, SWT.None);
	        nameField.setLayout(new GridLayout(2, true));
	        nameField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        
	        Label nameLabel = new Label(nameField, SWT.None);
	        nameLabel.setText("Name:");
	        
	        this.name = new Text(nameField, SWT.BORDER | SWT.SINGLE);
	        this.name.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        
	        // OK/Cancel buttons
	        Composite buttonField = new Composite(this.shell, SWT.RIGHT);
	        buttonField.setLayout(new RowLayout());
	        
	        this.ok = new Button(buttonField, SWT.NONE);
	        this.ok.setText("OK");
	        
	        this.cancel = new Button(buttonField, SWT.NONE);
	        this.cancel.setText("Cancel");
		}
	}
	
    private int result;
    private CarDB car;
 
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
    }
    
    public void setupButtons() {
    	this.view.ok.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				CarDialog.this.result = 0;
				
				String name = CarDialog.this.view.name.getText();
				
				if (name.isEmpty()) {
					MessageBox message = new MessageBox(CarDialog.this.view.shell);
					
					message.setMessage("Name is missing");
					message.setText("Validation error");
					
					message.open();
					
					// Stop here
					return;
				}
				
				CarDialog.this.car.setName(name);
				CarDialog.this.car.setPublicKey("keys/public_key_ct");
				CarDialog.this.car.setPrivateKey("keys/private_key_ct");
				
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

	public CarDB getCar() {
		return car;
	}

	public void setCar(CarDB car) {
		this.car = car;
	}
}
