package com.rental.terminal;

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
import com.rental.terminal.model.CustomerDB;

public class CustomerDialog extends Dialog {
	
	private class View {
		private Shell shell;
		private Button ok;
		private Button cancel;
		
		private Text name;
		
		private View(Shell parent) {
			this.shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			
			this.shell.setLayout(new GridLayout(1, false));
	        this.shell.setSize(450, 300);
	        this.shell.setText("Add customer");
	        	        
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
    private CustomerDB customer;
 
    private View view;
    

    public CustomerDialog(Shell parent, int style) {
        super(parent, style);
    }

    public CustomerDialog(Shell parent) {
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
    	CustomerDialog.this.view.shell.close();
    	CustomerDialog.this.view.shell.dispose();
    	
    }
    
    public void setupForm() {
    	this.view.name.setText(Strings.nullToEmpty(this.customer.getName()));
    }
    
    public void setupButtons() {
    	this.view.ok.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				CustomerDialog.this.result = 0;
				
				String name = CustomerDialog.this.view.name.getText();
				
				if (name.isEmpty()) {
					MessageBox message = new MessageBox(CustomerDialog.this.view.shell);
					
					message.setMessage("Name or public key missing");
					message.setText("Validation error");
					
					message.open();
					
					// Stop here
					return;
				}
				
				CustomerDialog.this.customer.setName(name);
				
				// Done
				CustomerDialog.this.close();
			}
		});
    	
    	this.view.cancel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				CustomerDialog.this.result = 1;
				
				// Done
				CustomerDialog.this.close();
			}
		});
    }

	public CustomerDB getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerDB customer) {
		this.customer = customer;
	}
}
