package com.rental.terminal;

import java.sql.SQLException;
import java.util.logging.Level;
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
import org.eclipse.swt.widgets.Composite;
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
import com.rental.terminal.model.Customer;
import com.rental.terminal.model.SmartCard;

public class Terminal {
	/**
	 * @var Logger instance
	 */
	private final static Logger LOGGER = Logger.getLogger(Terminal.class.getName());
	
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
		private Combo setupSmartcard;
		private Button setupAddSmartcard;
		private Button setupEditSmartcard;
		private Button setupDeleteSmartcard;
		
		private Button deskRent;
		private Button deskReset;
		private Combo deskCars;
		private Button deskAddCar;
		private Button deskEditCar;
		private Button deskDeleteCar;
		
		private Combo deskCustomers;
		private Button deskAddCustomer;
		private Button deskEditCustomer;
		private Button deskDeleteCustomer;
		
		private Button carStartStop;
		private Combo carCars;
		
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
			Composite setupForm = new Composite(folder, SWT.NONE);
			setupForm.setLayout(new GridLayout(1, true));
			
			this.setupTerminal = new TabItem(folder, SWT.NULL);
			this.setupTerminal.setText("Setup Terminal");
			this.setupTerminal.setControl(setupForm);
		    
		    RowLayout rowLayout = new RowLayout();
			rowLayout.fill = true;
			
			Group smartCardSelectGroup = new Group(setupForm, SWT.NONE);
			smartCardSelectGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			smartCardSelectGroup.setLayout(new GridLayout(4, false));
			smartCardSelectGroup.setText("Select smart card");
		    
		    // Smartcard selector
			this.setupSmartcard = new Combo(smartCardSelectGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			this.setupSmartcard.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			this.setupAddSmartcard = new Button(smartCardSelectGroup, SWT.None);
			this.setupAddSmartcard.setText("Add");
			
			this.setupEditSmartcard = new Button(smartCardSelectGroup, SWT.None);
			this.setupEditSmartcard.setText("Edit");
			
			this.setupDeleteSmartcard = new Button(smartCardSelectGroup, SWT.None);
			this.setupDeleteSmartcard.setText("Delete");
			
			// Init button
		    this.setupInit = new Button(setupForm, SWT.PUSH);
		    
		    this.setupInit.setText("Init");
		    this.setupInit.addListener(SWT.Selection, buttonListener);
			
			//
			// Desk terminal tab
			//
		    Composite deskForm = new Composite(folder, SWT.NONE);
		    deskForm.setLayout(new GridLayout(1, true));
		    
			this.deskTerminal = new TabItem(folder, SWT.NULL);
			this.deskTerminal.setText("Desk Terminal");
			this.deskTerminal.setControl(deskForm);
			
			// Car selector row
			Group carSelectGroup = new Group(deskForm, SWT.NONE);
			carSelectGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			carSelectGroup.setLayout(new GridLayout(4, false));
			carSelectGroup.setText("Select car");
			
			// Car selector
			this.deskCars = new Combo(carSelectGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			this.deskCars.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			this.deskAddCar = new Button(carSelectGroup, SWT.None);
			this.deskAddCar.setText("Add");
			
			this.deskEditCar = new Button(carSelectGroup, SWT.None);
			this.deskEditCar.setText("Edit");
			
			this.deskDeleteCar = new Button(carSelectGroup, SWT.None);
			this.deskDeleteCar.setText("Delete");
			
			// Add/edit
			Group customerSelectGroup = new Group(deskForm, SWT.NONE);
			customerSelectGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			customerSelectGroup.setLayout(new GridLayout(4, false));
			customerSelectGroup.setText("Select customer");
		    
		    // Customer selector
			this.deskCustomers = new Combo(customerSelectGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			this.deskCustomers.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			// Add/edit and delete buttons for customer
			this.deskAddCustomer = new Button(customerSelectGroup, SWT.None);
			this.deskAddCustomer.setText("Add");
			
			this.deskEditCustomer = new Button(customerSelectGroup, SWT.None);
			this.deskEditCustomer.setText("Edit");
			
			this.deskDeleteCustomer = new Button(customerSelectGroup, SWT.None);
			this.deskDeleteCustomer.setText("Delete");
			
			//Rent out a car button
			this.deskRent = new Button(deskForm, SWT.None);
		    
		    this.deskRent.setText("Rent out car");
		    this.deskRent.addListener(SWT.Selection, buttonListener);
			
			// Reset button
		    this.deskReset = new Button(deskForm, SWT.None);
		    
		    this.deskReset.setText("Reset card");
		    this.deskReset.addListener(SWT.Selection, buttonListener);
		    
			//
			// Car terminal tab
			//
		    SashForm carForm = new SashForm(folder, SWT.HORIZONTAL);
		    
			this.carTerminal = new TabItem(folder, SWT.NULL);
			this.carTerminal.setText("Car Terminal");
			this.carTerminal.setControl(carForm);
			
			// Car selector
			this.carCars = new Combo(carForm, SWT.DROP_DOWN | SWT.READ_ONLY);
			this.carCars.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
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
		//
		// Smart card
		//
		
		// Add smart card button
		this.view.setupAddSmartcard.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				SmartCardDialog dialog = new SmartCardDialog(view.shell);
				dialog.setSmartCard(new SmartCard());
				
				if (dialog.open() == 0) {
					SmartCard smartCard = dialog.getSmartCard();
					
					// Save to database
					try {
						Terminal.this.manager.getSmartCardDao().create(smartCard);
					} catch (SQLException e) {
						e.printStackTrace();
						return;
					}
					
					// Update GUI
					Terminal.this.view.addLogItem("Added smart card with ID " + smartCard.getId());
					
					view.setupSmartcard.add(smartCard.getCardId());
					((java.util.List<Integer>) view.setupSmartcard.getData()).add(smartCard.getId());
				}
			}
		});
		
		// Edit smart card button
		this.view.setupEditSmartcard.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				java.util.List<Integer> smartCardIds = (java.util.List<Integer>) view.setupSmartcard.getData();
				SmartCard smartCard;
				
				// Determine car ID
				int index = view.setupSmartcard.getSelectionIndex();
				
				if (index == -1) {
					return;
				}
				
				int id = smartCardIds.get(index);
				
				// Find car
				try {
					smartCard = manager.getSmartCardDao().queryForId(id);
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				}
				
				// Display dialog
				SmartCardDialog dialog = new SmartCardDialog(view.shell);
				dialog.setSmartCard(smartCard);
				
				if (dialog.open() == 0) {
					smartCard = dialog.getSmartCard();
					
					try {
						Terminal.this.manager.getSmartCardDao().update(smartCard);						
					} catch (SQLException e) {
						LOGGER.log(Level.SEVERE, "Exception", e);
						return;
					}
					
					// Update GUI
					view.addLogItem("Edited smart card with ID " + smartCard.getId());
					view.setupSmartcard.setItem(index, smartCard.getCardId());
				}
			}
		});
		
		// Delete smart card button
		this.view.setupDeleteSmartcard.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				java.util.List<Integer> smartCardIds = (java.util.List<Integer>) view.setupSmartcard.getData();
				
				// Determine car ID
				int index = view.setupSmartcard.getSelectionIndex();
				
				if (index == -1) {
					return;
				}
				
				int id = smartCardIds.get(index);
				
				// Find car
				try {
					manager.getSmartCardDao().deleteById(id);
				} catch (SQLException e) {
					LOGGER.log(Level.SEVERE, "Exception", e);
					return;
				}

				// Update GUI
				view.addLogItem("Deleted smart card with ID " + id);
				
				smartCardIds.remove(index);
				view.setupSmartcard.remove(index);
			}
		});
		
		//
		// Cars
		//
		
		// Add car button
		this.view.deskAddCar.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				CarDialog dialog = new CarDialog(view.shell);
				dialog.setCar(new Car());
				
				if (dialog.open() == 0) {
					Car car = dialog.getCar();
					
					// Save to database
					try {
						Terminal.this.manager.getCarDao().create(car);
					} catch (SQLException e) {
						LOGGER.log(Level.SEVERE, "Exception", e);
						return;
					}
					
					// Update GUI
					Terminal.this.view.addLogItem("Added car with ID " + car.getId());
					
					view.deskCars.add(car.getName());
					view.carCars.add(car.getName());
					((java.util.List<Integer>) view.deskCars.getData()).add(car.getId());
				}
			}
		});
		
		// Edit car button
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
					LOGGER.log(Level.SEVERE, "Exception", e);
					return;
				}
				
				// Display dialog
				CarDialog dialog = new CarDialog(view.shell);
				dialog.setCar(car);
				
				if (dialog.open() == 0) {
					car = dialog.getCar();
					
					try {
						Terminal.this.manager.getCarDao().update(car);						
					} catch (SQLException e) {
						LOGGER.log(Level.SEVERE, "Exception", e);
						return;
					}
					
					// Update GUI
					view.addLogItem("Edited car with ID " + car.getId());
					view.deskCars.setItem(index, car.getName());
					view.carCars.setItem(index, car.getName());
				}
			}
		});
		
		// Delete car button
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
					LOGGER.log(Level.SEVERE, "Exception", e);
					return;
				}

				// Update GUI
				view.addLogItem("Deleted car with ID " + id);
				
				carIds.remove(index);
				view.deskCars.remove(index);
				view.carCars.remove(index);
			}
		});
		
		//
		// Customers
		//
		
		// Add customer button
		this.view.deskAddCustomer.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				CustomerDialog dialog = new CustomerDialog(view.shell);
				dialog.setCustomer(new Customer());
				
				if (dialog.open() == 0) {
					Customer customer = dialog.getCustomer();
					
					// Save to database
					try {
						Terminal.this.manager.getCustomerDao().create(customer);
					} catch (SQLException e) {
						LOGGER.log(Level.SEVERE, "Exception", e);
						return;
					}
					
					// Update GUI
					Terminal.this.view.addLogItem("Added customer with ID " + customer.getId());
					
					view.deskCustomers.add(customer.getName());
					((java.util.List<Integer>) view.deskCustomers.getData()).add(customer.getId());
				}
			}
		});
		
		// Edit customer button
		this.view.deskEditCustomer.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				java.util.List<Integer> customerIds = (java.util.List<Integer>) view.deskCustomers.getData();
				Customer customer;
				
				// Determine car ID
				int index = view.deskCustomers.getSelectionIndex();
				
				if (index == -1) {
					return;
				}
				
				int id = customerIds.get(index);
				
				// Find car
				try {
					customer = manager.getCustomerDao().queryForId(id);
				} catch (SQLException e) {
					LOGGER.log(Level.SEVERE, "Exception", e);
					return;
				}
				
				// Display dialog
				CustomerDialog dialog = new CustomerDialog(view.shell);
				dialog.setCustomer(customer);
				
				if (dialog.open() == 0) {
					customer = dialog.getCustomer();
					
					try {
						Terminal.this.manager.getCustomerDao().update(customer);						
					} catch (SQLException e) {
						LOGGER.log(Level.SEVERE, "Exception", e);
						return;
					}
					
					// Update GUI
					view.addLogItem("Edited customer with ID " + customer.getId());
					view.deskCustomers.setItem(index, customer.getName());
				}
			}
		});
		
		// Delete customer button
		this.view.deskDeleteCustomer.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				java.util.List<Integer> customerIds = (java.util.List<Integer>) view.deskCustomers.getData();
				
				// Determine car ID
				int index = view.deskCustomers.getSelectionIndex();
				
				if (index == -1) {
					return;
				}
				
				int id = customerIds.get(index);
				
				// Find car
				try {
					manager.getCustomerDao().deleteById(id);
				} catch (SQLException e) {
					LOGGER.log(Level.SEVERE, "Exception", e);
					return;
				}

				// Update GUI
				view.addLogItem("Deleted customer with ID " + id);
				
				customerIds.remove(index);
				view.deskCustomers.remove(index);
			}
		});
	}
	
	public void setupSmartCards() {
		java.util.List<SmartCard> smartCards;
		
		// Query for all
		try {
			smartCards = this.manager.getSmartCardDao().queryForAll();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
			return;
		}
		
		// Add to the list
		java.util.List<Integer> smartCardIds = Lists.newArrayList();
		
		for (SmartCard smartCard : smartCards) {
			this.view.setupSmartcard.add(smartCard.getCardId() + "");
			//this.view.carCars.add(car.getName());
			smartCardIds.add(smartCard.getId());
		}
		
		// Save IDs
		this.view.setupSmartcard.setData(smartCardIds);
	}
	
	public void setupCustomers() {
		java.util.List<Customer> customers;
		
		// Query for all
		try {
			customers = this.manager.getCustomerDao().queryForAll();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
			return;
		}
		
		// Add to the list
		java.util.List<Integer> customerIds = Lists.newArrayList();
		
		for (Customer customer : customers) {
			this.view.deskCustomers.add(customer.getName());
			//this.view.carCars.add(car.getName());
			customerIds.add(customer.getId());
		}
		
		// Save IDs
		this.view.deskCustomers.setData(customerIds);
	}
	
	public void setupCars() {
		java.util.List<Car> cars;
		
		// Query for all
		try {
			cars = this.manager.getCarDao().queryForAll();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
			return;
		}
		
		// Add to the list
		java.util.List<Integer> carIds = Lists.newArrayList();
		
		for (Car car : cars) {
			this.view.deskCars.add(car.getName());
			this.view.carCars.add(car.getName());
			carIds.add(car.getId());
		}
		
		// Save IDs
		this.view.deskCars.setData(carIds);
	}
	
	/**
	 * 
	 */
	public Terminal() {
		// Setup the Database
		try {
			this.manager = new Manager();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
			return;
		}
		
		// Setup the view
		this.view = new View();
		
		this.setupButtons();
		this.setupSmartCards();
		this.setupCustomers();
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
