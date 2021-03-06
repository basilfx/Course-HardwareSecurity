package com.rental.terminal.gui;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.smartcardio.CardException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
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
import org.eclipse.swt.widgets.TypedCombo;

import com.rental.terminal.Terminal;
import com.rental.terminal.commands.BaseCommandsHandler;
import com.rental.terminal.commands.CarCommandsHandler;
import com.rental.terminal.commands.IssuingCommandsHandler;
import com.rental.terminal.commands.ReceptionCommandsHandler;
import com.rental.terminal.commands.BaseCommandsHandler.TerminalNonceMismatchException;
import com.rental.terminal.commands.CarCommandsHandler.CarTerminalInvalidCarIdException;
import com.rental.terminal.commands.CarCommandsHandler.CarTerminalInvalidDateException;
import com.rental.terminal.model.Car;
import com.rental.terminal.model.Manager;
import com.rental.terminal.model.Smartcard;

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
		private TypedCombo<Smartcard> setupSmartcard;
		private Button setupAddSmartcard;
		private Button setupEditSmartcard;
		private Button setupDeleteSmartcard;
		
		private Button deskInit;
		private Button deskReset;
		private Button deskRead;
		private TypedCombo<Car> deskCars;
		private Button deskAddCar;
		private Button deskEditCar;
		private Button deskDeleteCar;
		private Text deskDate;
		
		private Button carStart;
		private Button carStop;
		private Button carDrive;
		private TypedCombo<Car> carCars;
		private Label carMileage;
		
		public View() {			
			//
			// Window
			//
			this.display = new Display();
			this.shell = new Shell(this.display);
			
			this.shell.setLayout(new FormLayout());
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
			smartCardSelectGroup.setText("Select smartcard");
		    
		    // Smartcard selector
			this.setupSmartcard = new TypedCombo<Smartcard>(smartCardSelectGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			this.setupSmartcard.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			this.setupAddSmartcard = new Button(smartCardSelectGroup, SWT.None);
			this.setupAddSmartcard.setText("Add");
			
			this.setupEditSmartcard = new Button(smartCardSelectGroup, SWT.None);
			this.setupEditSmartcard.setText("Edit");
			
			this.setupDeleteSmartcard = new Button(smartCardSelectGroup, SWT.None);
			this.setupDeleteSmartcard.setText("Delete");
			
			// Issue button
			Group setupActionsGroup = new Group(setupForm, SWT.NONE);
			setupActionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			setupActionsGroup.setLayout(new GridLayout(4, false));
			setupActionsGroup.setText("Actions");
			
		    this.setupIssue = new Button(setupActionsGroup, SWT.PUSH);
		    this.setupIssue.setText("Issue card");
		    this.setupIssue.setEnabled(false);
		    
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
			this.deskCars = new TypedCombo<Car>(carSelectGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
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
			c.add(Calendar.DATE, 7);
			this.deskDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));
			this.deskDate.setEnabled(false);
			
			// Actions
			Group deskActionsGroup = new Group(deskForm, SWT.NONE);
			deskActionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			deskActionsGroup.setLayout(new GridLayout(4, false));
			deskActionsGroup.setText("Actions");
			
			// Init button
		    this.deskInit = new Button(deskActionsGroup, SWT.None);
		    this.deskInit.setText("Init card");
		    this.deskInit.setEnabled(false);
			
		    // Read button
		    this.deskRead = new Button(deskActionsGroup, SWT.None);
		    this.deskRead.setText("Read card");
		    this.deskRead.setEnabled(false);
		    
			// Reset button
		    this.deskReset = new Button(deskActionsGroup, SWT.None);
		    this.deskReset.setText("Reset card");
		    this.deskReset.setEnabled(false);
		    
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
			this.carCars = new TypedCombo<Car>(carActionsGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			this.carCars.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			// Stop button
			this.carStart = new Button(carActionsGroup, SWT.None);
			this.carStart.setText("Start car");
			this.carStart.setEnabled(false);
			
			// Stop button
			this.carStop = new Button(carActionsGroup, SWT.None);
			this.carStop.setText("Stop car");
			this.carStop.setEnabled(false);
			
			// Stop button
			this.carDrive = new Button(carActionsGroup, SWT.None);
			this.carDrive.setText("Drive 10KM");
			this.carDrive.setEnabled(false);
			
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
			this.log.setSelection(this.log.getItemCount() - 1);
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
		// Smartcard
		//
		
		// Smartcard select on change
		this.view.setupSmartcard.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				boolean condition = (view.setupSmartcard.getSelectionIndex() != -1);
				
				view.setupIssue.setEnabled(condition);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
		
		// Add smartcard button
		this.view.setupAddSmartcard.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				SmartcardDialog dialog = new SmartcardDialog(view.shell);
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
					view.addLogItem("Added smartcard with ID " + smartCard.getId());
					view.setupSmartcard.add(smartCard);
				}
			}
		});
		
		// Edit smartcard button
		this.view.setupEditSmartcard.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				int index = view.setupSmartcard.getSelectionIndex();
				Smartcard smartCard = view.setupSmartcard.getSelected();

				if (smartCard != null) {
					// Display dialog
					SmartcardDialog dialog = new SmartcardDialog(view.shell);
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
						view.addLogItem("Edited smartcard " + smartCard);
						view.setupSmartcard.setItem(index, smartCard);
					}
				}
			}
		});
		
		// Delete smartcard button
		this.view.setupDeleteSmartcard.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				Smartcard smartcard = view.setupSmartcard.getSelected();
				
				if (smartcard != null) {
					try {
						manager.getSmartCardDao().delete(smartcard);
					} catch (SQLException e) {
						LOGGER.log(Level.SEVERE, "Exception", e);
						return;
					}
	
					// Update GUI
					view.addLogItem("Deleted smartcard " + smartcard);
					view.setupSmartcard.remove(smartcard);
				}
			}
		});
		
		// Issue button
		this.view.setupIssue.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				view.setupIssue.setEnabled(false);
				
				try {
					Smartcard smartcard = view.setupSmartcard.getSelected();
				
					// Make sure smartcard is selected
					if (smartcard == null) {
						view.addLogItem("No smartcard selected");
					}
					
					// Make sure card reader is connected
					if (!terminal.isCardPresent()) {
						view.addLogItem("No card present");
						return;
					}
					
					// Issue card
					try {
						new IssuingCommandsHandler(terminal).issueCard(smartcard);
					} catch (Exception e) {
						view.addLogItem("Card issueing failed. Card already issued?");
						LOGGER.log(Level.SEVERE, "Exception", e);
						
						return;
					}
				} finally {
					view.setupIssue.setEnabled(true);
				}
				
				// Done
				view.addLogItem("Card issueing complete");
			}
		});
		
		//
		// Desk
		//
		
		// Car select on change
		this.view.deskCars.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				boolean condition = (view.deskCars.getSelectionIndex() != -1);
				
				view.deskDate.setEnabled(condition);
				view.deskInit.setEnabled(condition);
				view.deskRead.setEnabled(condition);
				view.deskReset.setEnabled(condition);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
		
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
					view.addLogItem("Added car " + car);
					
					view.deskCars.add(car);
					view.carCars.add(car);
				}
			}
		});
		
		// Edit car button
		this.view.deskEditCar.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				int index = view.deskCars.getSelectionIndex();
				Car car = view.deskCars.getSelected();
				
				if (car != null) {
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
			}
		});
		
		// Delete car button
		this.view.deskDeleteCar.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				Car car = view.deskCars.getSelected();
				
				if (car != null) {
					// Find car
					try {
						manager.getCarDao().delete(car);
					} catch (SQLException e) {
						LOGGER.log(Level.SEVERE, "Exception", e);
						return;
					}
	
					// Update GUI
					view.addLogItem("Deleted car " + car);
					view.deskCars.remove(car);
					view.carCars.remove(car);
				}
			}
		});
		
		// Init button
		this.view.deskInit.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				// Disable button
				view.deskInit.setEnabled(false);
				
				try {
					Calendar calendar;
					Smartcard smartcard = new Smartcard();
					Car car = view.deskCars.getSelected();
					
					// Make sure something is selected
					if (car == null) {
						view.addLogItem("No car selected");
						return;
					}
					
					// Make sure card reader is connected
					if (!terminal.isCardPresent()) {
						view.addLogItem("No card present");
						return;
					}
					
					// Parse data
					try {
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				        calendar = Calendar.getInstance();
				        calendar.setTime(formatter.parse(view.deskDate.getText()));
					} catch (Exception e) {
						view.addLogItem("Invalid date");
						return;
					}
					
					// Read smartcard ID
					try {
						new BaseCommandsHandler(terminal).getKeys(smartcard);	
						
						// Read ID into object, then read smartcard from DB
						try {
							smartcard = manager.getSmartCardDao().queryForEq("cardId", smartcard.getCardId()).get(0);
						} catch (ArrayIndexOutOfBoundsException e) {
							view.addLogItem("Smartcard with card ID " + smartcard.getCardId() + " not found!");
							return;
						}
						
						// Update car
						car.setDate(calendar);
						car.setStarts(0);
						car.setIssuedMileage(car.getMileage());
						manager.getCarDao().update(car);
						
						// Invoke init command
						new ReceptionCommandsHandler(terminal).initCard(smartcard, car);
					} catch (Exception e) {
						view.addLogItem("Failed initializing card");
						LOGGER.log(Level.SEVERE, "Exception", e);
						
						return;
					}
				} finally {
					view.deskInit.setEnabled(true);
				}
				
				// Done
				view.addLogItem("Card initialized");
			}
		});
		
		// Read button
		this.view.deskRead.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				view.deskRead.setEnabled(false);
				
				try {
					Smartcard smartcard = new Smartcard();
					Car car = view.deskCars.getSelected();
					
					// Make sure card reader is connected
					if (!terminal.isCardPresent()) {
						view.addLogItem("No card present");
						return;
					}
					
					// Read smartcard ID
					try {
						new BaseCommandsHandler(terminal).getKeys(smartcard);	
						
						// Read ID into object, then read smartcard from DB
						try {
							smartcard = manager.getSmartCardDao().queryForEq("cardId", smartcard.getCardId()).get(0);
						} catch (ArrayIndexOutOfBoundsException e) {
							view.addLogItem("Smartcard with card ID " + smartcard.getCardId() + " not found!");
							return;
						}
						
						// Invoke read command
						new ReceptionCommandsHandler(terminal).read(smartcard, car);
						
						int distance = car.getMileage() - car.getStartMileage();
						view.addLogItem(String.format("Car drove %d KM", distance));
					} catch (Exception e) {
						view.addLogItem("Failed initializing card");
						LOGGER.log(Level.SEVERE, "Exception", e);
						
						return;
					}
				} finally {
					view.deskRead.setEnabled(true);
				}
				
				// Done
				view.addLogItem("Card read");
			}
		});
		
		// Reset button
		this.view.deskReset.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				view.deskReset.setEnabled(false);
				
				try {
					// Make sure card reader is connected
					if (!terminal.isCardPresent()) {
						view.addLogItem("No card present");
						return;
					}
					
					// Reset card
					try {
						new ReceptionCommandsHandler(terminal).reset();
					} catch (Exception e) {
						view.addLogItem("Card reset failed");
						LOGGER.log(Level.SEVERE, "Exception", e);
						return;
					}
				} finally {				
					view.deskReset.setEnabled(true);
				}
				
				// Done
				view.addLogItem("Card reset succesfully");
			}
		});
		
		//
		// Car
		//
		// Smartcard select on change
		this.view.carCars.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				boolean condition = (view.carCars.getSelectionIndex() != -1);
				
				view.carStart.setEnabled(condition);
				view.carStop.setEnabled(condition);
				view.carDrive.setEnabled(condition);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
		
		// Car start button
		this.view.carStart.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				view.carStart.setEnabled(false);
				
				try {
					// Make sure card reader is connected
					if (!terminal.isCardPresent()) {
						view.addLogItem("No card present");
						return;
					}
					
					try {
						CarCommandsHandler carCommands = new CarCommandsHandler(terminal);
						Car car = view.carCars.getSelected();
						
						if (car != null) {
							carCommands.startCar(car);
							
							// Update info
							car.setStarts(car.getStarts() + 1);
							
							if (car.getStarts() == 1) {
								view.addLogItem("Car first started");
								car.setStartMileage(car.getStartMileage());
							}
							
							// Store in database;
							manager.getCarDao().update(car);
							
							// Update view
							view.carMileage.setText("Current mileage: " + car.getMileage());
						}
					} catch (CarTerminalInvalidCarIdException e) {
						view.addLogItem("The card is not registered for this car.");
						return;
					} catch (CarTerminalInvalidDateException e) {
						view.addLogItem("The car is not allowed to start anymore. Date expired");
						return;
					} catch (Exception e) {
						view.addLogItem("Failed starting car");
						LOGGER.log(Level.SEVERE, "Exception", e);
						return;
					}
				} finally {
					view.carStart.setEnabled(true);
				}
				
				// Done
				view.addLogItem("Car started");
			}
		});
		
		// Car stop button
		view.carStop.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				view.carStop.setEnabled(false);
				
				try {
					// Make sure card reader is connected
					if (!terminal.isCardPresent()) {
						view.addLogItem("No card present");
						return;
					}
					
					try {
						Car car = view.carCars.getSelected();
						
						if (car != null) {
							CarCommandsHandler carCommands = new CarCommandsHandler(terminal);
							
							carCommands.setMileage(car.getMileage());
							carCommands.stopCar(car);
							
							// Save car
							manager.getCarDao().update(car);
							
							// Update GUI
							view.carMileage.setText("Current mileage: --");
						}
					} catch (Exception e) {
						view.addLogItem("Failed stopping car");
						LOGGER.log(Level.SEVERE, "Exception", e);
						
						return;
					}
				} finally {
					view.carStop.setEnabled(true);
				}
				
				// Done
				view.addLogItem("Car stopped");
			}
		});
		
		// Car drive button
		this.view.carDrive.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				view.carDrive.setEnabled(false);
				
				try {
					// Make sure card reader is connected
					if (!terminal.isCardPresent()) {
						view.addLogItem("No card present");
						return;
					}
					
					try {
						Car car = view.carCars.getSelected();
						
						if (car != null) {
							car.setMileage(car.getMileage() + 10);
							manager.getCarDao().update(car);
							
							view.carMileage.setText("Current mileage: " + car.getMileage());			
						}
					} catch (Exception e) {
						view.addLogItem("Failed increasing mileage");
						LOGGER.log(Level.SEVERE, "Exception", e);
						
						return;
					}
				} finally {
					view.carDrive.setEnabled(true);
				}
				
				// Done
				view.addLogItem("Increased mileage");
			}
		});
	}
	
	public void setupSmartCards() {
		try {
			this.view.setupSmartcard.setItems(this.manager.getSmartCardDao().queryForAll());
			this.view.addLogItem("Smartcards loaded");
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
			return;
		}
	}

	public void setupCars() {
		try {
			java.util.List<Car> cars = this.manager.getCarDao().queryForAll();
			
			this.view.carCars.setItems(cars);
			this.view.deskCars.setItems(cars);
			
			this.view.addLogItem("Cars loaded");
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
			return;
		}
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
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, "Exception", e);
			return;
		}

		// Build GUI
		this.view = new View();
		
		this.setupButtons();
		this.setupSmartCards();
		this.setupCars();
		
		// Notify ready
		this.view.setStatus("Not connected");
		this.view.addLogItem("Application started");
		
		// Add timer for smartcard status polling
		this.view.display.timerExec(500, new Runnable() {
			@Override
			public void run() {
				if (MainWindow.this.terminal.isCardPresent()) {
					view.setStatus("Connected");
				} else {
					view.setStatus("Not connected");
				}
				
				// Schedule next attempt
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
