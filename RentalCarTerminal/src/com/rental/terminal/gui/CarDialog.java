package com.rental.terminal.gui;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Strings;
import com.rental.terminal.model.Car;

/**
 * Add/edit car dialog
 * 
 * @author Bas Stottelaar
 * @author Jeroen Senden
 */
public class CarDialog extends BaseDialog {
	
	private class View {
		private Shell shell;
		private Button ok;
		private Button cancel;
		
		private Text name;
		private Text publicKey;
		private Text privateKey;
		
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
	        
	        // Public key field
	        Composite publicKeyField = new Composite(this.shell, SWT.None);
	        publicKeyField.setLayout(new GridLayout(2, true));
	        publicKeyField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        
	        Label publicKeyLabel = new Label(publicKeyField, SWT.None);
	        publicKeyLabel.setText("Public key:");
	        
	        this.publicKey = new Text(publicKeyField, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
	        this.publicKey.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        this.publicKey.setText("keys/public_key_ct");
	        
	        // Private key field
	        Composite privateKeyField = new Composite(this.shell, SWT.None);
	        privateKeyField.setLayout(new GridLayout(2, true));
	        privateKeyField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        
	        Label privateKeyLabel = new Label(privateKeyField, SWT.None);
	        privateKeyLabel.setText("Private key:");
	        
	        this.privateKey = new Text(privateKeyField, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
	        this.privateKey.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        this.privateKey.setText("keys/private_key_ct");
	        
	        // OK/Cancel buttons
	        Composite buttonField = new Composite(this.shell, SWT.RIGHT);
	        buttonField.setLayout(new RowLayout());
	        
	        this.ok = new Button(buttonField, SWT.NONE);
	        this.ok.setText("OK");
	        
	        this.cancel = new Button(buttonField, SWT.NONE);
	        this.cancel.setText("Cancel");
		}
	}
	
    private Car car;
 
    private View view;
    
    public CarDialog(Shell parent) {
		super(parent);
	}
    
    public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	@Override
	public Shell getShell() {
		return this.view.shell;
	}
	
	@Override
    public void setup() {
		this.view = new View(this.getParent());
		
    	this.setupForm();
    	this.setupButtons();
    }
    
    public void setupForm() {
    	this.view.name.setText(Strings.nullToEmpty(this.car.getName()));
    }
    
    public void setupButtons() {
    	this.view.ok.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				String name = view.name.getText();
				String publicKey = view.publicKey.getText();
				String privateKey = view.privateKey.getText();
				
				if (name.isEmpty() || publicKey.isEmpty() || privateKey.isEmpty()) {
					new MessageBoxBuilder(CarDialog.this.getShell())
						.setTitle("Name is missing")
						.setMessage("The name of the car, public key and/or private key is missing.")
						.open();

					return;
				}
				
				CarDialog.this.car.setName(name);
				
				try {
					CarDialog.this.car.setPublicKeyFromFile(publicKey);
					CarDialog.this.car.setPrivateKeyFromFile(privateKey);
				} catch (IOException e) {
					new MessageBoxBuilder(CarDialog.this)
						.setTitle("File error")
						.setMessage("Could not load public and/or private key from file.")
						.open();
					
					return;
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				
				// Done
				CarDialog.this.close(0);
			}
		});
    	
    	this.view.cancel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				CarDialog.this.close(1);
			}
		});
    }
}
