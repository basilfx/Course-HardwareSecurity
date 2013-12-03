package com.rental.terminal;

import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class TerminalChooser {
	/**
	 * @var Logger instance
	 */
	private final static Logger LOGGER = Logger.getLogger(TerminalChooser.class.getName());
	
	/**
	 * The private view class defines all components.
	 */
	private class View {
		private static final long serialVersionUID = -1780251187989519796L;
		
		private Display display;
	    private Shell shell;
		
	    private Sash sash;
	    
		private TabFolder folder;
		
		private TabItem setupTerminal;
		private TabItem deskTerminal;
		private TabItem carTerminal;
		
		private Group group;
		
		private Label status;
		private List log;
		
		public View() {
			//
			// Window
			//
			this.display = new Display();
			this.shell = new Shell(this.display);
			this.shell.setLayout(new FormLayout());
			//this.shell.setMenu(new Menu(this.display));
			
			this.shell.setSize(1024, 500);
			this.shell.setText("Terminal Emulator");
			
			//
			// Splitter
			//
			this.sash = new Sash(shell, SWT.VERTICAL);
			
			final int limit = 2; 
			final int percent = 60;
			final FormData sashData = new FormData();
			sashData.left = new FormAttachment(percent, 0);
			sashData.top = new FormAttachment(0, 0);
			sashData.bottom = new FormAttachment(100, 0);
			this.sash.setLayoutData(sashData);
			this.sash.addListener(SWT.Selection, new Listener () {
				public void handleEvent (Event e) {
					Rectangle sashRect = View.this.sash.getBounds ();
					Rectangle shellRect = shell.getClientArea ();
					
					int right = shellRect.width - sashRect.width - limit;
					e.x = Math.max (Math.min (e.x, right), limit);
					
					if (e.x != sashRect.x)  {
						sashData.left = new FormAttachment (0, e.x);
						shell.layout ();
					}
				}
			});
			
			//
			// Initialize tab folder
			//
			this.folder = new TabFolder(this.shell, SWT.BORDER);
			
			FormData folderData = new FormData();
			folderData.left = new FormAttachment(0, 0);
			folderData.right = new FormAttachment(this.sash, 0);
			folderData.top = new FormAttachment(0, 0);
			folderData.bottom = new FormAttachment(100, 0);
			this.folder.setLayoutData(folderData);
			
			//
			// Setup terminal tab
			//
			this.setupTerminal = new TabItem(this.folder, SWT.NULL);
			this.setupTerminal.setText("Setup Terminal");
						
		    SashForm setupForm = new SashForm(folder, SWT.HORIZONTAL);
		    Button reset = new Button(setupForm, SWT.PUSH);
		    reset.setText("Reset");
		    Button init = new Button(setupForm, SWT.PUSH);
		    init.setText("Init");
			this.setupTerminal.setControl(setupForm);
			
			
			Listener listener = new Listener(){
				@Override
				public void handleEvent(Event e) {
					System.out.println();
					addLogItem(((Button)e.widget).getText() + " pressed");
					if(((Button)e.widget).getText() == "Reset"){
						//iets 
					}
				}
			};
			init.addListener(SWT.Selection, listener);
			reset.addListener(SWT.Selection, listener);
			
			//
			// Desk terminal tab
			//
			this.deskTerminal = new TabItem(this.folder, SWT.NULL);
			this.deskTerminal.setText("Desk Terminal");
			
			SashForm DeskForm = new SashForm(this.folder, SWT.BORDER);
			Button ok = new Button(DeskForm, SWT.NULL);
			ok.setText("OK");
			this.deskTerminal.setControl(DeskForm);
			
			ok.addListener(SWT.Selection, listener);
			
			//
			// Car terminal tab
			//
			this.carTerminal = new TabItem(this.folder, SWT.NULL);
			this.carTerminal.setText("Car Terminal");
			
			//
			// Logging sidebar
			//
			
			// Group
			this.group = new Group(this.shell, SWT.NULL);
			this.group.setLayout(new GridLayout(1, false));
			
			FormData groupData = new FormData();
			groupData.left = new FormAttachment(this.sash, 0);
			groupData.right = new FormAttachment(100, 0);
			groupData.top = new FormAttachment(0, 0);
			groupData.bottom = new FormAttachment(100, 0);
			this.group.setLayoutData(groupData);
			
			// Status label
			this.status = new Label(this.group, SWT.NULL);
			this.status.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			// Log
			this.log = new List(this.group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
			this.log.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			// Clear button
			Button clear = new Button(this.group, SWT.None);
			
			clear.setText("Clear log");
			clear.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			clear.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					View.this.log.removeAll();
				}
			});
			
			// Marker button
			Button mark = new Button(this.group, SWT.None);
			
			mark.setText("Add marker");
			mark.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			mark.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					View.this.addLogItem("===================================");
				}
			});
		}
		
		public void setStatus(String status) {
			this.status.setText("Status: " + status);
		}
		
		public void addLogItem(String message) {
			this.log.add(message);
		}
		
	}
	
	/**
	 * @var Reference to view instance
	 */
	private View view;;
	
	/**
	 * 
	 */
	public TerminalChooser() {
		this.view = new View();
		
		this.view.setStatus("Not connected");
		this.view.addLogItem("Application started");
		
		//this.view.shell.pack();
		
		// Start SWT GUI thread.
		try {
			try {
				this.view.shell.open();

				while (!this.view.shell.isDisposed()) {
					if (!this.view.display.readAndDispatch()) {
						this.view.display.sleep();
					}
				}
			} finally {
				if (!this.view.shell.isDisposed()) {
					this.view.shell.dispose();
				}
			}
		} finally {
			this.view.display.dispose();
		}
	    
	    // Done
		System.exit(0);
	}
}
