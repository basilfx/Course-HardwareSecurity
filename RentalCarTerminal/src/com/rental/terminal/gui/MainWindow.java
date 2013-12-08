package com.rental.terminal.gui;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Lists;
import com.rental.terminal.BaseCommandsHandler;
import com.rental.terminal.CarCommandsHandler;
import com.rental.terminal.IssuingCommandsHandler;
import com.rental.terminal.ReceptionCommandsHandler;
import com.rental.terminal.Terminal;
import com.rental.terminal.db.Car;
import com.rental.terminal.db.Manager;
import com.rental.terminal.db.Smartcard;
import com.rental.terminal.encryption.RSAHandler;

/**
 * Main window
 * 
 * @author Bas Stottelaar
 * @author Jeroen Senden
 */
public class MainWindow {
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
		
		private Button setupIssue;
		private Combo setupSmartcard;
		private Button setupAddSmartcard;
		private Button setupEditSmartcard;
		private Button setupDeleteSmartcard;
		
		
		private Button deskInit;
		private Button deskReset;
		private Combo deskCars;
		private Button deskAddCar;
		private Button deskEditCar;
		private Button deskDeleteCar;
		private Text deskDate;
		
		private Button carStart;
		private Button carStop;
		private Button carDrive;
		private Combo carCars;
		private Label carMileage;
		
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
			Group setupActionsGroup = new Group(setupForm, SWT.NONE);
			setupActionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			setupActionsGroup.setLayout(new GridLayout(4, false));
			setupActionsGroup.setText("Actions");
			
		    this.setupIssue = new Button(setupActionsGroup, SWT.PUSH);
		    
		    this.setupIssue.setText("Issue card");
		    
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
		
			// Date selector
			Group deskPeriodGroup = new Group(deskForm, SWT.NONE);
			deskPeriodGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			deskPeriodGroup.setLayout(new GridLayout(4, false));
			deskPeriodGroup.setText("Select period");
			
			this.deskDate = new Text(deskPeriodGroup, SWT.BORDER);
			
			Calendar c = Calendar.getInstance(); 
			c.setTime(new Date()); 
			c.add(Calendar.DATE, 7);
			this.deskDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));
			
			// Actions
			Group deskActionsGroup = new Group(deskForm, SWT.NONE);
			deskActionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			deskActionsGroup.setLayout(new GridLayout(4, false));
			deskActionsGroup.setText("Actions");
			
			// Init button
		    this.deskInit = new Button(deskActionsGroup, SWT.None);
		    
		    this.deskInit.setText("Init card");
			
			// Reset button
		    this.deskReset = new Button(deskActionsGroup, SWT.None);
		    
		    this.deskReset.setText("Reset card");
		    
			//
			// Car terminal tab
			//
		    Composite carForm = new Composite(folder, SWT.None);
		    carForm.setLayout(new GridLayout(1, true));
		    
			this.carTerminal = new TabItem(folder, SWT.NULL);
			this.carTerminal.setText("Car Terminal");
			this.carTerminal.setControl(carForm);
			
			Group carActionsGroup = new Group(carForm, SWT.NONE);
			carActionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			carActionsGroup.setLayout(new GridLayout(1, false));
			carActionsGroup.setText("Actions");
			
			// Car selector
			this.carCars = new Combo(carActionsGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			this.carCars.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			// Stop button
			this.carStart = new Button(carActionsGroup, SWT.None);
			this.carStart.setText("Start car");
			
			// Stop button
			this.carStop = new Button(carActionsGroup, SWT.None);
			this.carStop.setText("Stop car");
			
			// Stop button
			this.carDrive = new Button(carActionsGroup, SWT.None);
			this.carDrive.setText("Drive 10KM");
			
			// Mileage label
			this.carMileage = new Label(carActionsGroup, SWT.None);
			
			this.carMileage.setText("Current mileage: 0");
			this.carMileage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
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
	 * @var Reference to the Java Card terminal manager
	 */
	private Terminal terminal;
	
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
				dialog.setSmartCard(new Smartcard());
				
				if (dialog.open() == 0) {
					Smartcard smartCard = dialog.getSmartCard();
					
					// Save to database
					try {
						MainWindow.this.manager.getSmartCardDao().create(smartCard);
					} catch (SQLException e) {
						e.printStackTrace();
						return;
					}
					
					// Update GUI
					MainWindow.this.view.addLogItem("Added smart card with ID " + smartCard.getId());
					
					view.setupSmartcard.add(smartCard.getCardId() + "");
					((java.util.List<Integer>) view.setupSmartcard.getData()).add(smartCard.getId());
				}
			}
		});
		
		// Edit smart card button
		this.view.setupEditSmartcard.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				java.util.List<Integer> smartCardIds = (java.util.List<Integer>) view.setupSmartcard.getData();
				Smartcard smartCard;
				
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
						MainWindow.this.manager.getSmartCardDao().update(smartCard);						
					} catch (SQLException e) {
						LOGGER.log(Level.SEVERE, "Exception", e);
						return;
					}
					
					// Update GUI
					view.addLogItem("Edited smart card with ID " + smartCard.getId());
					view.setupSmartcard.setItem(index, smartCard.getCardId() + "");
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
						MainWindow.this.manager.getCarDao().create(car);
					} catch (SQLException e) {
						LOGGER.log(Level.SEVERE, "Exception", e);
						return;
					}
					
					// Update GUI
					MainWindow.this.view.addLogItem("Added car with ID " + car.getId());
					
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
						MainWindow.this.manager.getCarDao().update(car);						
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
		
		// Issue button
		this.view.setupIssue.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				// Generate unique ID
				java.util.List<Integer> smartCardIds = (java.util.List<Integer>) view.setupSmartcard.getData();
				Smartcard smartCard;
				
				// Determine car ID
				int index = view.setupSmartcard.getSelectionIndex();
				
				if (index == -1) {
					return;
				}
				
				int id = smartCardIds.get(index);
				
				view.setupIssue.setEnabled(false);
				
				// Find car
				try {
					smartCard = manager.getSmartCardDao().queryForId(id);				
				
					// Smart card magic
					RSAHandler rsaHandler = new RSAHandler();
					Smartcard smartcard = new Smartcard();
					
				
					RSAPublicKey public_key_sc = rsaHandler.readPublicKeyFromFileSystem("keys/public_key_sc");
					RSAPrivateKey private_key_sc = rsaHandler.readPrivateKeyFromFileSystem("keys/private_key_sc");
					
					smartcard.setCardId(smartCard.getCardId());			
					smartcard.setPublicKey(public_key_sc);
					smartcard.setPrivateKey(private_key_sc);
					
					IssuingCommandsHandler issuingCommands;
					
					issuingCommands = new IssuingCommandsHandler(terminal);
					issuingCommands.issueCard(smartcard);
					
					// Update GUI
					view.addLogItem("Card issueing complete");
				} catch (Exception e) {
					view.addLogItem("Card issueing failed");
					e.printStackTrace();
				} finally {
					view.setupIssue.setEnabled(true);
				}
				
			}
		});
		
		// Init button
		this.view.deskInit.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				// Disable button
				view.deskInit.setEnabled(false);
				
				// Retrieve card
				java.util.List<Integer> carIds = (java.util.List<Integer>) view.deskCars.getData();
				
				Car car;
				Smartcard smartCard;
				Calendar calendar;
				Smartcard smartCard2 = new Smartcard();
				
				// Parse data
				try {
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			        calendar = Calendar.getInstance();
			        calendar.setTime(formatter.parse(view.deskDate.getText()));
				} catch (Exception e) {
					view.deskInit.setEnabled(true);
					return;
				}
				
				// Read smart card ID
				try {
					new BaseCommandsHandler(terminal).getKeys(smartCard2);	
					
					// Determine car ID
					int index = view.deskCars.getSelectionIndex();
					
					if (index == -1) {
						return;
					}
					
					int id = carIds.get(index);
					
					// Find car
					car = manager.getCarDao().queryForId(id);
					smartCard = manager.getSmartCardDao().queryForEq("cardId", smartCard2.getCardId()).get(0);
					
					// Update car
					car.setDate(calendar);
					
					// Invoke init command
					ReceptionCommandsHandler reception = new ReceptionCommandsHandler(terminal);
					reception.initCard(smartCard2, car);
					
					view.addLogItem("Card initialized");
				} catch (Exception e) {
					view.addLogItem("Failed initializing card");
					
					LOGGER.log(Level.SEVERE, "Exception", e);
					return;
				} finally {
					// Re enable button
					view.deskInit.setEnabled(true);
				}
			}
		});
		
		// Reset button
		this.view.deskReset.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				view.deskReset.setEnabled(false);
				
				// Reset card
				try {
					new ReceptionCommandsHandler(terminal).reset();
					
					view.addLogItem("Card reset succesfully");
				} catch (Exception e) {
					view.addLogItem("Card reset failed");
					LOGGER.log(Level.SEVERE, "Exception", e);
				}
				
				view.deskReset.setEnabled(true);
			}
		});
		
		// Car start button
		view.carStart.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				view.carStart.setEnabled(false);
				
				try {
					CarCommandsHandler carCommands = new CarCommandsHandler(terminal);
					Car car = getCar(view.carCars.getSelectionIndex());
					
					if (car != null) {
						carCommands.startCar(car);
						
						// Update info
						car.setStarts(car.getStarts() + 1);
						
						if (car.getStarts() == 1) {
							car.setStartMileage(car.getStartMileage());
						}
						
						view.carMileage.setText("Current mileage: " + car.getMileage());
						
						// Store in database;
						manager.getCarDao().update(car);
					}
				} catch (Exception e) {
					view.addLogItem("Failed starting car");
					
					LOGGER.log(Level.SEVERE, "Exception", e);
				}
				
				view.carStart.setEnabled(true);
			}
		});
		
		// Car stop button
		view.carStop.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				view.carStop.setEnabled(false);
				
				try {
					CarCommandsHandler carCommands = new CarCommandsHandler(terminal);
					Car car = getCar(view.carCars.getSelectionIndex());
					
					if (car != null) {
						carCommands.setMileage(car.getMileage());
						carCommands.stopCar(car);

						view.carMileage.setText("Current mileage: --");
					}
				} catch (Exception e) {
					view.addLogItem("Failed stopping car");
					
					LOGGER.log(Level.SEVERE, "Exception", e);
				}
				
				view.carStop.setEnabled(true);
			}
		});
		
		// Car drive button
		view.carDrive.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				view.carDrive.setEnabled(false);
				
				try {
					Car car = getCar(view.carCars.getSelectionIndex());
					
					if (car != null) {
						car.setMileage(car.getMileage() + 10);
						manager.getCarDao().update(car);
						
						view.carMileage.setText("Current mileage: " + car.getMileage());
						
						view.addLogItem("Increaded mileage");
					}
				} catch (Exception e) {
					view.addLogItem("Failed increasing mileage");
					
					LOGGER.log(Level.SEVERE, "Exception", e);
				}
				
				view.carDrive.setEnabled(true);
			}
		});
	}
	
	public Car getCar(int index) {
		java.util.List<Integer> carIds = (java.util.List<Integer>) view.deskCars.getData();

		if (index == -1) {
			return null;
		}
		
		int id = carIds.get(index);
		
		// Find car
		try {
			return manager.getCarDao().queryForId(id);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
			return null;
		}
	}
	
	public void setupSmartCards() {
		java.util.List<Smartcard> smartCards;
		
		// Query for all
		try {
			smartCards = this.manager.getSmartCardDao().queryForAll();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
			return;
		}
		
		// Add to the list
		java.util.List<Integer> smartCardIds = Lists.newArrayList();
		
		for (Smartcard smartCard : smartCards) {
			this.view.setupSmartcard.add(smartCard.getCardId() + "");
			//this.view.carCars.add(car.getName());
			smartCardIds.add(smartCard.getId());
		}
		
		// Save IDs
		this.view.setupSmartcard.setData(smartCardIds);
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
	public MainWindow() {
		// Setup the Database
		try {
			this.manager = new Manager();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
			return;
		}
		
		// Setup terminal
		try {
			this.terminal = new Terminal();
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
			return;
		}
		
		// Setup the view
		this.view = new View();
		
		this.setupButtons();
		this.setupSmartCards();
		this.setupCars();
		
		// Notify ready
		this.view.setStatus("Not connected");
		this.view.addLogItem("Application started");
		
		// Add timer for smart card status polling
		this.view.display.timerExec(500, new Runnable() {
			@Override
			public void run() {
				if (MainWindow.this.terminal.isCardPresent()) {
					MainWindow.this.view.setStatus("Connected");
				} else {
					MainWindow.this.view.setStatus("Not connected");
				}
				
				view.display.timerExec(500, this);
			}
		});
		
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
	
	/**
	 * Main method to initiate the GUI
	 * @param args
	 */
	public static void main(String[] args) {
		// Open the main window
		new MainWindow();
	}
}
