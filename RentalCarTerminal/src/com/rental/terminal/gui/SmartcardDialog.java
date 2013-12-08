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
				
				try {
					cardId = Short.parseShort(SmartcardDialog.this.view.cardId.getText());
				} catch (NumberFormatException e) {
					new MessageBoxBuilder(SmartcardDialog.this)
						.setTitle("Validation error")
						.setMessage("Card ID missing or not a number")
						.open();
					
					return;
				}
				
				// Parse the number
				smartcard.setCardId(cardId);
				
				try {
					smartcard.setPublicKeyFromFile("keys/public_key_sc");
					smartcard.setPrivateKeyFromFile("keys/private_key_sc");
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
