package com.rental.terminal;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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

import com.google.common.collect.Lists;
import com.rental.terminal.db.Manager;
import com.rental.terminal.model.Car;

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
		
		private TabItem setupTerminal;
		private TabItem deskTerminal;
		private TabItem carTerminal;
		
		private Label status;
		private List log;
		
		private Button setupInit;
		
		private Combo deskCars;
		private Button deskReset;
		private Button deskAddCar;
		private Button deskEditCar;
		private Button deskDeleteCar;
		
		private Button carStartStop;
		
		public View() {
			//
			// General
			//
			Listener buttonListener = new Listener(){
				@Override
				public void handleEvent(Event e) {
					addLogItem("Button '" + ((Button)e.widget).getText() + "' pressed");
				}
			};
			
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
			final Sash sash = new Sash(shell, SWT.VERTICAL);
			
			final int limit = 2; 
			final int percent = 60;
			
			final FormData sashData = new FormData();
			sashData.left = new FormAttachment(percent, 0);
			sashData.top = new FormAttachment(0, 0);
			sashData.bottom = new FormAttachment(100, 0);
			
			sash.setLayoutData(sashData);
			sash.addListener(SWT.Selection, new Listener () {
				public void handleEvent (Event e) {
					Rectangle sashRect = sash.getBounds ();
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
			TabFolder folder = new TabFolder(this.shell, SWT.BORDER);
			
			FormData folderData = new FormData();
			folderData.left = new FormAttachment(0, 0);
			folderData.right = new FormAttachment(sash, 0);
			folderData.top = new FormAttachment(0, 0);
			folderData.bottom = new FormAttachment(100, 0);
			folder.setLayoutData(folderData);
			
			//
			// Setup terminal tab
			//
			SashForm setupForm = new SashForm(folder, SWT.HORIZONTAL);
			
			this.setupTerminal = new TabItem(folder, SWT.NULL);
			this.setupTerminal.setText("Setup Terminal");
			this.setupTerminal.setControl(setupForm);
		    
		    // Init button
		    this.setupInit = new Button(setupForm, SWT.PUSH);
		    
		    this.setupInit.setText("Init");
		    this.setupInit.addListener(SWT.Selection, buttonListener);
			
			//
			// Desk terminal tab
			//
		    SashForm deskForm = new SashForm(folder, SWT.BORDER);
		    
			this.deskTerminal = new TabItem(folder, SWT.NULL);
			this.deskTerminal.setText("Desk Terminal");
			this.deskTerminal.setControl(deskForm);
			
			// Car selector row
			RowLayout rowLayout = new RowLayout();
			rowLayout.fill = true;
			
			Group carSelectGroup = new Group(deskForm, SWT.NONE);
			carSelectGroup.setLayout(new GridLayout(4, false));
			
			// Car selector
			this.deskCars = new Combo(carSelectGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			this.deskCars.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			// Add/edit and delete buttons
			this.deskAddCar = new Button(carSelectGroup, SWT.None);
			this.deskAddCar.setText("Add");
			
			this.deskEditCar = new Button(carSelectGroup, SWT.None);
			this.deskEditCar.setText("Edit");
			
			this.deskDeleteCar = new Button(carSelectGroup, SWT.None);
			this.deskDeleteCar.setText("Delete");
			
			// Reset button
		    this.deskReset = new Button(deskForm, SWT.None);
		    
		    this.deskReset.setText("Reset");
		    this.deskReset.addListener(SWT.Selection, buttonListener);
			
			//
			// Car terminal tab
			//
		    SashForm carForm = new SashForm(folder, SWT.HORIZONTAL);
		    
			this.carTerminal = new TabItem(folder, SWT.NULL);
			this.carTerminal.setText("Car Terminal");
			this.carTerminal.setControl(carForm);
			
			// Stop button
			this.carStartStop = new Button(carForm, SWT.None);
			
			this.carStartStop.setText("Stop car");
			this.carStartStop.addListener(SWT.Selection, buttonListener);
			
			// Mileage label
			Label mileage = new Label(carForm, SWT.None);
			
			mileage.setText("Current mileage: 0");
			mileage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			//
			// Logging sidebar
			//
			
			// Group
			Group group = new Group(this.shell, SWT.NULL);
			group.setLayout(new GridLayout(1, false));
			
			FormData groupData = new FormData();
			groupData.left = new FormAttachment(sash, 0);
			groupData.right = new FormAttachment(100, 0);
			groupData.top = new FormAttachment(0, 0);
			groupData.bottom = new FormAttachment(100, 0);
			
			group.setLayoutData(groupData);
			
			// Status label
			this.status = new Label(group, SWT.NULL);
			this.status.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			// Log
			this.log = new List(group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
			this.log.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			// Clear button
			Button clear = new Button(group, SWT.None);
			
			clear.setText("Clear log");
			clear.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			clear.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					View.this.log.removeAll();
				}
			});
			
			// Marker button
			Button mark = new Button(group, SWT.None);
			
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
	 * @var Reference to the Database manager
	 */
	private Manager manager;
	
	/**
	 * 
	 */
	public void setupButtons() {
		// Add button
		this.view.deskAddCar.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				CarDialog dialog = new CarDialog(view.shell);
				dialog.setCar(new Car());
				
				if (dialog.open() == 0) {
					Car car = dialog.getCar();
					int id;
					
					// Save to database
					try {
						id = TerminalChooser.this.manager.getCarDao().create(car);
					} catch (SQLException e) {
						e.printStackTrace();
						return;
					}
					
					// Update GUI
					TerminalChooser.this.view.addLogItem("Added car with ID " + id);
					
					view.deskCars.add(car.getName());
					((java.util.List<Integer>) view.deskCars.getData()).add(id);
				}
			}
		});
		
		// Edit button
		this.view.deskEditCar.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				java.util.List<Integer> carIds = (java.util.List<Integer>) view.deskCars.getData();
				Car car;
				
				// Determine car ID
				int index = view.deskCars.getSelectionIndex();
				
				if (index == -1) {
					return;
				}
				
				int id = carIds.get(index);
				
				// Find car
				try {
					car = manager.getCarDao().queryForId(id);
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				}
				
				// Display dialog
				CarDialog dialog = new CarDialog(view.shell);
				dialog.setCar(car);
				
				if (dialog.open() == 0) {
					car = dialog.getCar();
					
					try {
						TerminalChooser.this.manager.getCarDao().update(car);						
					} catch (SQLException e) {
						e.printStackTrace();
						return;
					}
					
					// Update GUI
					view.addLogItem("Edited car with ID " + car.getId());
					view.deskCars.setItem(index, car.getName());
				}
			}
		});
		
		// Delete button
		this.view.deskDeleteCar.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				java.util.List<Integer> carIds = (java.util.List<Integer>) view.deskCars.getData();
				
				// Determine car ID
				int index = view.deskCars.getSelectionIndex();
				
				if (index == -1) {
					return;
				}
				
				int id = carIds.get(index);
				
				// Find car
				try {
					manager.getCarDao().deleteById(id);
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				}

				// Update GUI
				view.addLogItem("Deleted car with ID " + id);
				view.deskCars.remove(index);
			}
		});
	}
	
	public void setupCars() {
		java.util.List<Car> cars;
		
		// Query for all
		try {
			cars = this.manager.getCarDao().queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		// Add to the list
		java.util.List<Integer> carIds = Lists.newArrayList();
		
		for (Car car : cars) {
			this.view.deskCars.add(car.getName());
			carIds.add(car.getId());
		}
		
		// Save IDs
		this.view.deskCars.setData(carIds);
	}
	
	/**
	 * 
	 */
	public TerminalChooser() {
		// Setup the Database
		try {
			this.manager = new Manager();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Setup the view
		this.view = new View();
		
		this.setupButtons();
		this.setupCars();
		
		// Notify ready
		this.view.setStatus("Not connected");
		this.view.addLogItem("Application started");
		
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
