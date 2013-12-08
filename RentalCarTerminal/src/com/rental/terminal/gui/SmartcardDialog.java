package com.rental.terminal.gui;

import java.io.IOException;
import java.util.Random;

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

import com.rental.terminal.model.Smartcard;

/**
 * Add/edit smart card dialog
 * 
 * @author Bas Stottelaar
 * @author Jeroen Senden
 */
public class SmartcardDialog extends BaseDialog {
	private class View {
		private Shell shell;
		private Button ok;
		private Button cancel;
		
		private Text cardId;
		private Button generate;
		private Text publicKey;
		private Text privateKey;

		private View(Shell parent) {
			this.shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			
			this.shell.setLayout(new GridLayout(1, false));
	        this.shell.setSize(450, 300);
	        this.shell.setText("Add smart card");
	        	        
	        // Card ID field
	        Composite cardIdField = new Composite(this.shell, SWT.None);
	        cardIdField.setLayout(new GridLayout(3, true));
	        cardIdField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        
	        Label nameLabel = new Label(cardIdField, SWT.None);
	        nameLabel.setText("Name:");
	        
	        this.cardId = new Text(cardIdField, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
	        this.cardId.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        
	        this.generate = new Button(cardIdField, SWT.None);
	        this.generate.setText("Generate card ID");
	        
	        // Public key field
	        Composite publicKeyField = new Composite(this.shell, SWT.None);
	        publicKeyField.setLayout(new GridLayout(2, true));
	        publicKeyField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        
	        Label publicKeyLabel = new Label(publicKeyField, SWT.None);
	        publicKeyLabel.setText("Public key:");
	        
	        this.publicKey = new Text(publicKeyField, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
	        this.publicKey.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        this.publicKey.setText("keys/public_key_sc");
	        
	        // Private key field
	        Composite privateKeyField = new Composite(this.shell, SWT.None);
	        privateKeyField.setLayout(new GridLayout(2, true));
	        privateKeyField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        
	        Label privateKeyLabel = new Label(privateKeyField, SWT.None);
	        privateKeyLabel.setText("Private key:");
	        
	        this.privateKey = new Text(privateKeyField, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
	        this.privateKey.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        this.privateKey.setText("keys/private_key_sc");
	        
	        // OK/Cancel buttons
	        Composite buttonField = new Composite(this.shell, SWT.RIGHT);
	        buttonField.setLayout(new RowLayout());
	        
	        this.ok = new Button(buttonField, SWT.NONE);
	        this.ok.setText("OK");
	        
	        this.cancel = new Button(buttonField, SWT.NONE);
	        this.cancel.setText("Cancel");
		}
	}
	
    private Smartcard smartcard;
 
    private View view;

    public SmartcardDialog(Shell parent) {
        super(parent);
    }

	public Smartcard getSmartCard() {
		return smartcard;
	}

	public void setSmartCard(Smartcard smartcard) {
		this.smartcard = smartcard;
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
    	this.view.cardId.setText(this.smartcard.getCardId() + "");
    }
    
    public void setupButtons() {
    	this.view.generate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				// Generate unique ID
				short cardId = (short) new Random().nextInt(Short.MAX_VALUE + 1);
				view.cardId.setText(cardId + "");
			}
    	});
    	
    	this.view.ok.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				Short cardId;
				String publicKey = view.publicKey.getText();
				String privateKey = view.privateKey.getText();
				
				try {
					cardId = Short.parseShort(SmartcardDialog.this.view.cardId.getText());
				} catch (NumberFormatException e) {
					new MessageBoxBuilder(SmartcardDialog.this)
						.setTitle("Validation error")
						.setMessage("Card ID missing or not a number")
						.open();
					
					return;
				}
				
				if (publicKey.isEmpty() || privateKey.isEmpty()) {
					new MessageBoxBuilder(SmartcardDialog.this)
						.setTitle("Validation error")
						.setMessage("Public and/or private key missing")
						.open();
				
					return;
				}
				
				// Set the card parameters
				smartcard.setCardId(cardId);
			
				try {
					smartcard.setPublicKeyFromFile(publicKey);
					smartcard.setPrivateKeyFromFile(privateKey);
				} catch (IOException e) {
					new MessageBoxBuilder(SmartcardDialog.this)
						.setTitle("File error")
						.setMessage("Could not load public and/or private key from file.")
						.open();
					
					return;
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				
				// Done
				SmartcardDialog.this.close(0);
			}
		});
    	
    	this.view.cancel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				SmartcardDialog.this.close(1);
			}
		});
    }
}
