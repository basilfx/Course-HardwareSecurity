package com.rental.terminal;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

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

import terminal.IssuingCommandsHandler;
import terminal.Smartcard;
import terminal.Terminal;

import com.google.common.base.Strings;
import com.rental.terminal.model.SmartCard;

import encryption.RSAHandler;

public class SmartCardDialog extends Dialog {
	private Terminal terminal;
	
	private class View {
		private Shell shell;
		private Button ok;
		private Button cancel;
		
		private Text cardId;
		private Button issue;

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
	        
	        this.cardId = new Text(cardIdField, SWT.SINGLE | SWT.BORDER); //| SWT.READ_ONLY);
	        this.cardId.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	        
	        this.issue = new Button(cardIdField, SWT.None);
	        this.issue.setText("Issue new card");
	        
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
    private SmartCard smartcard;
 
    private View view;
    

    public SmartCardDialog(Shell parent, int style) {
        super(parent, style);
    }

    public SmartCardDialog(Shell parent) {
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
    	SmartCardDialog.this.view.shell.close();
    	SmartCardDialog.this.view.shell.dispose();
    	
    }
    
    public void setupForm() {
    	this.view.cardId.setText(this.smartcard.getCardId() + "");
    }
    
    public void setupButtons() {
    	this.view.issue.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				RSAHandler rsaHandler = new RSAHandler();
				Smartcard smartcard = new Smartcard();
				
				RSAPublicKey public_key_sc;
				RSAPrivateKey private_key_sc;
				
				try {
					public_key_sc = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_sc");
					private_key_sc = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_sc");
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				
				short cardId = (short) new Random().nextInt(Short.MAX_VALUE + 1);
				SmartCardDialog.this.smartcard.setCardId(cardId);
				
				smartcard.setScId(cardId);			
				smartcard.setPublicKey(public_key_sc);
				smartcard.setPrivateKey(private_key_sc);
				
				IssuingCommandsHandler issuingCommands;
				
				try {
					issuingCommands = new IssuingCommandsHandler(terminal);
					issuingCommands.issueCard(smartcard);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    	});
    	
    	this.view.ok.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				SmartCardDialog.this.result = 0;
				
				String cardId = SmartCardDialog.this.view.cardId.getText();
				
				if (cardId.isEmpty()) {
					MessageBox message = new MessageBox(SmartCardDialog.this.view.shell);
					
					message.setMessage("Card ID missing");
					message.setText("Validation error");
					
					message.open();
					
					// Stop here
					return;
				}
				
				// Done
				SmartCardDialog.this.close();
			}
		});
    	
    	this.view.cancel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				SmartCardDialog.this.result = 1;
				
				// Done
				SmartCardDialog.this.close();
			}
		});
    }

	public SmartCard getSmartCard() {
		return smartcard;
	}

	public void setSmartCard(SmartCard smartcard) {
		this.smartcard = smartcard;
	}
	
	public Terminal getTerminal() {
		return this.terminal;
	}
	
	public void setTerminal(Terminal terminal) {
		this.terminal = terminal;
	}
}
