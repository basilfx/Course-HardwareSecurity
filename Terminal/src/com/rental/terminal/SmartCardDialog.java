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
import com.rental.terminal.model.SmartCard;

public class SmartCardDialog extends Dialog {
	
	private class View {
		private Shell shell;
		private Button ok;
		private Button cancel;
		
		private Text cardId;
		private Button read;
		
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
	        
	        this.read = new Button(cardIdField, SWT.None);
	        this.read.setText("Read ID");
	        
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
    	this.view.cardId.setText(Strings.nullToEmpty(this.smartcard.getCardId()));
    }
    
    public void setupButtons() {
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
				
				// Store data
				SmartCardDialog.this.smartcard.setCardId(cardId);
				
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
}
