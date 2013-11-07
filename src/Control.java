import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * 
 * Control class is the main class in iDo application. 
 * 
 *
 */
public class Control extends Application {
	// Restriction message from executing specific commands during process of synchronization
	private static final String MESSAGE_UNDO_RESTRICTION = "Cannot undo during process of synchronization";
	private static final String MESSAGE_REDO_RESTRICTION = "Cannot redo during process of synchronization";
	private static final String MESSAGE_EXIT_RESTRICTION = "Cannot exit during process of synchronization";
	// Display message in system tray
	private static final String POPUP_MESSAGE_START_DATE = "Task \"%1$s\" will begin after the next %2$s minutes";
	private static final String POPUP_MESSAGE_END_DATE = "Task \"%1$s\" will end after the next %2$s minutes";
	// Filename for storage
	private static final String TASK_FILENAME = "task_storage.xml";
	private static final String SETTING_FILENAME = "setting_storage.xml";
	// Logging object
	private static Logger logger = Logger.getLogger("Control");
	
	// Indicator whether application is under real time search or not
	private static boolean isRealTimeSearch = false;
	// Model in the application, containing info of tasks and settings
	private Model model = new Model();
	// History keep track of previous commands
	private History commandHistory = new History();
	// View in the application, providing the GUI
	private View view;
	// Storages
	private Storage taskFile;
	private Storage settingStore;
	// Sync thread of Control class
	public static SyncCommand syncThread;
	private Synchronization sync = new Synchronization(model);
	// Timer for auto sync
	private Timer syncTimer;
	
	/**
	 * Main Function of the application
	 * @param args
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		loadData();
		loadGUI(primaryStage);
		loadTimer();
	}
	
	/**
	 * Load the data from storage files
	 */
	public void loadData() {
		try {
			loadTask();
			loadSettings();
			CustomDate.setDisplayRemaining(model.doDisplayRemaining());
		} catch (IOException e) {
			logger.log(Level.INFO,"Cannot read the given file");
		}
	}

	/**
	 * Load the settings data
	 */
	private void loadSettings() throws IOException {
		settingStore = new SettingsStorage(SETTING_FILENAME, model);
		settingStore.loadFromFile();
	}

	/**
	 * Load the task data
	 */
	private void loadTask() throws IOException {
		taskFile = new TaskStorage(TASK_FILENAME, model);
		taskFile.loadFromFile();
	}
	
	/**
	 * Initialize the graphical user interface of the application. Setup the
	 * listeners and key bindings in the interface
	 * 
	 * @param primaryStage
	 *            the main window of the application
	 */
	private void loadGUI(Stage primaryStage) {
		view = new View(model, primaryStage, settingStore);
		addListenerForPreferences();
		handleEventForCommandLine();
		updateLastOverdueTasks();
		storeSettings();
	}
	
	/**
	 * Store settings data into storage files
	 */
	private void storeSettings() {
		try{
			settingStore.storeToFile();
		} catch(IOException io) {
			logger.log(Level.INFO, "Cannot store with the given filename");
		}
	}
	
	/**
	 * Update the lines separating overdue tasks and ongoing tasks in all lists
	 */
	private void updateLastOverdueTasks() {
		updateOverdueLine(model.getPendingList());
		updateOverdueLine(model.getCompleteList());
		updateOverdueLine(model.getTrashList());
	}
	
	/**
	 * Setup handling for all necessary events for command line
	 */
	private void handleEventForCommandLine() {
		setupChangeListener();
		handleKeyEvent();
	}
	
	/**
	 * Setup the change listener in the command line
	 */
	private void setupChangeListener() {
		view.txt.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				realTimeUpdate();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				realTimeUpdate();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
	
			}
			
			/**
			 * Real time update in the interface due to changes in the content of command line
			 */
			private void realTimeUpdate() {
				String command = view.txt.getText();
				update(command);
				checkValid(command);
			}
			
			/**
			 * The real time update including updating in the feedback and the
			 * results of searching
			 * 
			 * @param command
			 *            the current content in the command line
			 */
			private void update(String command) {
				realTimeFeedback(command);
				
				if (isSearchCommand(command)) {
					realTimeSearch(command);
				} else if (isValidIndexCommand(command)) {
					realTimeCommandExecution(command);
				}
			}
			
			/**
			 * Check whether this command is valid for real time search
			 * @param command current content in the command line
			 */
			private void checkValid(String command) {
				if (isInvalidRealTimeCommand(command)) {
					isRealTimeSearch = false;
					executeShowCommand();
				}
			}
			
			// Indicate if this is an invalid real time command and currently
			// under real time searched
			private boolean isValidIndexCommand(String command) {
				return isRemoveCommand(command) || isRecoverCommand(command)
						|| isMarkCommand(command) || isUnmarkCommand(command)
						|| isCompleteCommand(command)
						|| isIncompleteCommand(command);
			}

			private boolean isInvalidRealTimeCommand(String command) {
				return isRealTimeSearch && !isSearchCommand(command)
						&& !isValidIndexCommand(command);
			}

			private void realTimeCommandExecution(String command) {
				String content = Common.removeFirstWord(command);

				if (!content.matches("\\s*") && !content.matches("\\s*\\d+.*")) {
						realTimeSearch("search " + content);
				}
			}
			
			// Indicator whether this is a remove command
			private boolean isRemoveCommand(String command) {
				return Parser.determineCommandType(command) == Common.COMMAND_TYPES.REMOVE;
			}
			
			// Indicator whether this is a recover command
			private boolean isRecoverCommand(String command){
				return Parser.determineCommandType(command) == Common.COMMAND_TYPES.RECOVER;
			}
			
			// Indicator whether this is a mark command
			private boolean isMarkCommand(String command){
				return Parser.determineCommandType(command) == Common.COMMAND_TYPES.MARK;
			}
			
			// Indicator whether this is an unmark command
			private boolean isUnmarkCommand(String command){
				return Parser.determineCommandType(command) == Common.COMMAND_TYPES.UNMARK;
			}
			
			// Indicator whether this is a complete command
			private boolean isCompleteCommand(String command){
				return Parser.determineCommandType(command) == Common.COMMAND_TYPES.COMPLETE;
			}
			
			// Indicator whether this is an incomplete command
			private boolean isIncompleteCommand(String command){
				return Parser.determineCommandType(command) == Common.COMMAND_TYPES.INCOMPLETE;
			}
			
			// Indicator whether this is a search command
			private boolean isSearchCommand(String command) {
				return Parser.determineCommandType(command) == Common.COMMAND_TYPES.SEARCH;
			}
			
			/**
			 * This function check whether this command is an index command
			 * 
			 * @param command
			 *            content of the command line
			 * @return true if this command works with indices, vice versa
			 */
			private boolean isIndexCommand(String command) {
				String content = Common.removeFirstWord(command);
				String[] splitContent = Common.splitBySpace(content);
				String checkedIndex = splitContent[0];
				try {
					checkIndex(checkedIndex);
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			}
			
			// Process checking the index
			private void checkIndex(String checkedIndex) throws NumberFormatException {
				if(checkedIndex.contains(Common.HYPHEN)){
					checkRangeIndex(checkedIndex);
				} else{
					Integer.valueOf(checkedIndex);
				}
			}
			
			// Process checking specifically for the range of indices
			private void checkRangeIndex(String rangeIndex) throws NumberFormatException{
				String[] splitContent = rangeIndex.split(Common.HYPHEN);
				if(splitContent.length != 2)
					throw new NumberFormatException("Invalid range for index");
				Integer.parseInt(splitContent[0]);
				Integer.parseInt(splitContent[1]);
			}
			
			/**
			 * Setup the real time feedback or suggestion to users
			 * 
			 * @param command
			 *            content of the command line
			 */
			private void realTimeFeedback(String command) {
				if (Parser.checkEmptyCommand(command)) {
					view.setFeedback(Common.MESSAGE_REQUEST_COMMAND);
				} else {
					updateFeedback(command);
				}
			}
			
			/**
			 * Update the real time feedback according to type of the command
			 * 
			 * @param command
			 *            content of the command line
			 */
			private void updateFeedback(String command) {
				Common.COMMAND_TYPES commandType = Parser
						.determineCommandType(command);
				switch (commandType) {
				case ADD:
					view.setFeedback(Common.MESSAGE_ADD_TIP);
					break;
				case EDIT:
					view.setFeedback(Common.MESSAGE_EDIT_TIP);
					break;
				case REMOVE:
					if (isIndexCommand(command)) {
						view.setFeedback(Common.MESSAGE_REMOVE_INDEX_TIP);
					} else {
						view.setFeedback(Common.MESSAGE_REMOVE_INFO_TIP);
					}
					break;
				case RECOVER:
					view.setFeedback(Common.MESSAGE_RECOVER_TIP);
					break;
				case SEARCH:
					view.setFeedback(Common.MESSAGE_SEARCH_TIP);
					break;
				case SHOW_ALL:
					view.setFeedback(Common.MESSAGE_SHOW_ALL_TIP);
					break;
				case UNDO:
					view.setFeedback(Common.MESSAGE_UNDO_TIP);
					break;
				case REDO:
					view.setFeedback(Common.MESSAGE_REDO_TIP);
					break;
				case MARK:
					view.setFeedback(Common.MESSAGE_MARK_TIP);
					break;
				case UNMARK:
					view.setFeedback(Common.MESSAGE_UNMARK_TIP);
					break;
				case COMPLETE:
					view.setFeedback(Common.MESSAGE_COMPLETE_TIP);
					break;
				case INCOMPLETE:
					view.setFeedback(Common.MESSAGE_INCOMPLETE_TIP);
					break;
				case TODAY:
					view.setFeedback(Common.MESSAGE_TODAY_TIP);
					break;
				case CLEAR_ALL:
					view.setFeedback(Common.MESSAGE_CLEAR_ALL_TIP);
					break;
				case HELP:
					view.setFeedback(Common.MESSAGE_HELP_TIP);
					break;
				case SYNC:
					view.setFeedback(Common.MESSAGE_SYNC_TIP);
					break;
				case SETTINGS:
					view.setFeedback(Common.MESSAGE_SETTINGS_TIP);
					break;
				case EXIT:
					view.setFeedback(Common.MESSAGE_EXIT_TIP);
					break;
				case INVALID:
					view.setFeedback(command);
					break;
				}
			}
		});
	}
	
	/**
	 * Setup handling key events executed in the interface
	 */
	private void handleKeyEvent() {
		setupHotkeys();
		setupKeyBindingsForCommandLine();
	}
	
	// Setup key bindings for the command line
	private void setupKeyBindingsForCommandLine() {
		InputMap map = view.txt.getInputMap();
		addKeyBindingForExecution(map);
		addKeyBindingForUndo(map);
		addKeyBindingForRedo(map);
		addKeyBidningForHelp(map);
	}
	
	// Setup the hot keys in the application
	private void setupHotkeys() {
		view.generalBase.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent keyEvent) {
				if (Common.undo_hot_key.match(keyEvent)) {
					isRealTimeSearch = false;
					String feedback = executeCommand("undo");
					updateFeedback(feedback);
				} else if (Common.redo_hot_key.match(keyEvent)) {
					isRealTimeSearch = false;
					String feedback = executeCommand("redo");
					updateFeedback(feedback);
				} else if (keyEvent.getCode() == KeyCode.F1) {
					isRealTimeSearch = false;
					String feedback = executeCommand("help");
					updateFeedback(feedback);
				}
			}
		});
	}
	
	/*
	 * Key binding for ENTER
	 */
	private void addKeyBindingForExecution(InputMap map) {
		Action enterAction = new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						isRealTimeSearch = false;
						String feedback = executeCommand(view.txt.getText());
						updateFeedback(feedback);
					}
				});
			}
		};
		
		// Get KeyStroke for enter key
		KeyStroke enterKey = KeyStroke.getKeyStroke(
				com.sun.glass.events.KeyEvent.VK_ENTER, 0);
		// Override enter for a pane
		map.put(enterKey, enterAction);
	}
	
	/*
	 *  Key binding for Ctrl + Z
	 */
	private void addKeyBindingForUndo(InputMap map) {
		Action undoAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						isRealTimeSearch = false;
						String feedback = executeCommand("undo");
						updateFeedback(feedback);
					}
				});
			}
		};
		KeyStroke undoKey = KeyStroke.getKeyStroke(
				com.sun.glass.events.KeyEvent.VK_Z,
				java.awt.event.InputEvent.CTRL_DOWN_MASK);
		map.put(undoKey, undoAction);
	}	
	
	/*
	 * Key binding for Ctrl + Y
	 */
	private void addKeyBindingForRedo(InputMap map) {
		Action redoAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						isRealTimeSearch = false;
						String feedback = executeCommand("redo");
						updateFeedback(feedback);
					}
				});
			}
		};
		KeyStroke redoKey = KeyStroke.getKeyStroke(
				com.sun.glass.events.KeyEvent.VK_Y,
				java.awt.event.InputEvent.CTRL_DOWN_MASK);
		map.put(redoKey, redoAction);
	}
	
	/*
	 * Key binding for F1
	 */
	private void addKeyBidningForHelp(InputMap map) {
		Action helpAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						isRealTimeSearch = false;
						String feedback = executeCommand("help");
						updateFeedback(feedback);
					}
				});
			}
		};
		KeyStroke helpKey = KeyStroke.getKeyStroke(
				com.sun.glass.events.KeyEvent.VK_F1, 0);
		map.put(helpKey, helpAction);
	}

	/*
	 * Add the listener for the Preferences MenuItem in the PopupMenu in system
	 * tray of the application
	 */
	private void addListenerForPreferences(){
		view.getSettingsItem().addActionListener(createPreferencesListenerInSystemTray());
	}
	
	// Create the specific ActionListener
	private ActionListener createPreferencesListenerInSystemTray() {
		return new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						isRealTimeSearch = false;
						view.stage.toBack();
						String feedback = executeCommand("settings");
						updateFeedback(feedback);
					}
				});
			}
		};
	}
	
	/**
	 * Update the feedback in the interface according to the types of feedback
	 * 
	 * @param feedback
	 *            the specific feedback
	 */
	private void updateFeedback(String feedback) {
		if (successfulExecution(feedback)) {
			clearCommandLine();
		}
		view.emptyFeedback(0);
		view.setFeedbackStyle(0, feedback, view.getDefaultColor());
	}

	/***************************************** execution part ********************************************/
	private String executeCommand(String userCommand) {
		boolean isEmptyCommand = Parser.checkEmptyCommand(userCommand);
		if (isEmptyCommand) {
			return Common.MESSAGE_EMPTY_COMMAND;
		}
		try {
			Common.COMMAND_TYPES commandType = Parser
					.determineCommandType(userCommand);

			String[] parsedUserCommand = Parser.parseCommand(userCommand,
					commandType, modelHandler, view);

			return executeCommandCorrespondingType(parsedUserCommand,
					commandType);
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private String executeCommandCorrespondingType(String[] parsedUserCommand,
			Common.COMMAND_TYPES commandType) throws IllegalArgumentException,
			IOException {
		switch (commandType) {
		case ADD:
			return executeAddCommand(parsedUserCommand);
		case EDIT:
			return executeEditCommand(parsedUserCommand);
		case REMOVE:
			return executeRemoveCommand(parsedUserCommand);
		case RECOVER:
			return executeRecoverCommand(parsedUserCommand);
		case UNDO:
			return executeUndoCommand();
		case REDO:
			return executeRedoCommand();
		case SEARCH:
			return executeSearchCommand(parsedUserCommand, isRealTimeSearch);
		case TODAY:
			return executeTodayCommand(isRealTimeSearch);
		case SHOW_ALL:
			return executeShowCommand();
		case CLEAR_ALL:
			return executeClearCommand();
		case COMPLETE:
			return executeCompleteCommand(parsedUserCommand);
		case INCOMPLETE:
			return executeIncompleteCommand(parsedUserCommand);
		case MARK:
			return executeMarkCommand(parsedUserCommand);
		case UNMARK:
			return executeUnmarkCommand(parsedUserCommand);
		case SETTINGS:
			return executeSettingsCommand(parsedUserCommand);
		case HELP:
			return executeHelpCommand();
		case SYNC:
			return executeSyncCommand(parsedUserCommand);
		case EXIT:
			return executeExitCommand();
		case INVALID:
			return Common.MESSAGE_INVALID_COMMAND_TYPE;
		default:
			throw new Error("Unrecognised command type.");
		}
	}

	private void realTimeSearch(String command) {
		isRealTimeSearch = true;
		if (command.trim().equals("search"))
			executeShowCommand();
		else {
			executeCommand(command);
		}
	}

	private String executeAddCommand(String[] parsedUserCommand)
			throws IOException {
		int tabIndex = view.getTabIndex();
		Command s = new AddCommand(parsedUserCommand, modelHandler, tabIndex);
		String feedback = s.execute();
		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_ADD)) {
			commandHistory.updateCommand((TwoWayCommand) s);
			taskFile.storeToFile();
			view.setTab(0);
			executeShowCommand();
		}
		return feedback;
	}

	private String executeEditCommand(String[] parsedUserCommand)
			throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = view.getTabIndex();
		Command s = new EditCommand(parsedUserCommand, modelHandler, tabIndex);
		String feedback = s.execute();
		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_EDIT)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			taskFile.storeToFile();
			executeShowCommand();
		}
		return feedback;
	}

	private String executeRemoveCommand(String[] parsedUserCommand)
			throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = view.getTabIndex();
		Command s = new RemoveCommand(parsedUserCommand, modelHandler, tabIndex);
		String feedback = s.execute();
		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_REMOVE)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			taskFile.storeToFile();
			// syncFile.storeToFile();
			executeShowCommand();
		}
		return feedback;
	}

	private String executeUndoCommand() throws IOException {
		if (syncThread != null && syncThread.isRunning()){
			return "Cannot undo during process of synchronization";
		}
		
		if (commandHistory.isUndoable()) {
			executeShowCommand();
			TwoWayCommand undoCommand = commandHistory.getPrevCommandForUndo();
			String feedback = undoCommand.undo();
			taskFile.storeToFile();
			return feedback;
		} else
			return Common.MESSAGE_INVALID_UNDO;
	}

	private String executeRedoCommand() throws IOException {
		if (syncThread != null && syncThread.isRunning()){
			return "Cannot redo during process of synchronization";
		}
		
		if (commandHistory.isRedoable()) {
			if (commandHistory.isAfterSearch()) {
				TwoWayCommand.setIndexType(TwoWayCommand.SEARCHED);
			}
			executeShowCommand();
			TwoWayCommand redoCommand = commandHistory.getPrevCommandForRedo();
			redoCommand.redo();
			taskFile.storeToFile();
			return Common.MESSAGE_SUCCESSFUL_REDO;
		} else
			return Common.MESSAGE_INVALID_REDO;
	}

	private String executeSearchCommand(String[] parsedUserCommand,
			boolean isRealTimeSearch) {
		Command s = new SearchCommand(parsedUserCommand, modelHandler, view,
				isRealTimeSearch);
		return s.execute();
	}

	private String executeTodayCommand(boolean isRealTimeSearch) {
		return executeCommand("search today");
	}

	private String executeClearCommand() throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = view.getTabIndex();
		Command s = new ClearAllCommand(modelHandler, tabIndex);
		String feedback = s.execute();
		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_CLEAR_ALL)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			taskFile.storeToFile();
			executeShowCommand();
		}
		return feedback;
	}

	private String executeCompleteCommand(String[] parsedUserCommand)
			throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = view.getTabIndex();
		Command s = new CompleteCommand(parsedUserCommand, modelHandler,
				tabIndex);
		String feedback = s.execute();

		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_COMPLETE)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			taskFile.storeToFile();
			executeShowCommand();
		}
		return feedback;
	}

	private String executeIncompleteCommand(String[] parsedUserCommand)
			throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = view.getTabIndex();
		Command s = new IncompleteCommand(parsedUserCommand, modelHandler,
				tabIndex);
		String feedback = s.execute();

		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_INCOMPLETE)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			taskFile.storeToFile();
			executeShowCommand();
		}
		return feedback;
	}
	
	private String executeRecoverCommand(String[] parsedUserCommand) throws IOException{
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = view.getTabIndex();
		Command s = new RecoverCommand(parsedUserCommand, modelHandler, tabIndex);
		String feedback = s.execute();
		
		if(feedback.equals(Common.MESSAGE_SUCCESSFUL_RECOVER)){
			commandHistory.updateCommand((TwoWayCommand)s, isAfterSearch);
			taskFile.storeToFile();
			executeShowCommand();
		}
		
		return feedback;
	}

	private String executeMarkCommand(String[] parsedUserCommand)
			throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = view.getTabIndex();
		Command s = new MarkCommand(parsedUserCommand, modelHandler, tabIndex);
		String feedback = s.execute();

		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_MARK)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			taskFile.storeToFile();
			executeShowCommand();
		}
		return feedback;
	}

	private String executeUnmarkCommand(String[] parsedUserCommand)
			throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = view.getTabIndex();
		Command s = new UnmarkCommand(parsedUserCommand, modelHandler, tabIndex);
		String feedback = s.execute();

		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_UNMARK)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			taskFile.storeToFile();
			executeShowCommand();
		}
		return feedback;
	}
	
	private String executeHelpCommand() {
		Command s = new HelpCommand(modelHandler, view);
		return s.execute();
	}

	private String executeSettingsCommand(String[] parsedUserCommand) throws IOException{
		String oldTheme = modelHandler.getThemeMode();
		boolean oldAutoSync = modelHandler.getAutoSync();
		if(syncTimer != null)
			syncTimer.cancel();
		Command s = new SettingsCommand(modelHandler, view, parsedUserCommand);
		String feedback = s.execute();
		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_SETTINGS)) {
			settingStore.storeToFile();
			if(!oldTheme.equals(modelHandler.getThemeMode()))
					view.customizeGUI();
			view.setColourScheme(modelHandler.getColourScheme());
			if(oldAutoSync != modelHandler.getAutoSync()) {
					autoSync();
			}
			CustomDate.setDisplayRemaining(modelHandler.doDisplayRemaining());
			CustomDate.updateCurrentDate();
			handleListener();
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					updateList(modelHandler.getPendingList());
					updateList(modelHandler.getCompleteList());
					updateList(modelHandler.getTrashList());
				}
			});

		}
		return feedback;
	}
	
	private void autoSync( ) {
		syncTimer = new Timer();
		syncTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (modelHandler.getAutoSync() == true) {
					String[] nullContent = new String[1];
					new SyncCommand(nullContent, modelHandler, sync, view, taskFile);
				} else {
					this.cancel();
				}
			}
		}, 0, modelHandler.getSyncPeriod() * Common.MINUTE_IN_MILLIS);
	}

	private String executeSyncCommand(String[] parsedUserCommand)
			throws IOException {
		// Check whether there is already a sync thread
		if (syncThread == null || !syncThread.isRunning())
			syncThread = new SyncCommand(parsedUserCommand, modelHandler, sync, view,
					taskFile);

		// String feedback = s.execute();
		String feedback = syncThread.getFeedback();
		if (feedback == null){
			feedback = Common.MESSAGE_REQUEST_COMMAND;
		}
		view.txt.setText("");
		view.txt.setCaretPosition(0);
		return feedback;
	}

	private String executeExitCommand() {
		int tabIndex = view.getTabIndex();
		Command s = new ExitCommand(modelHandler, tabIndex);
		if(syncThread != null && syncThread.isRunning()){
			return "Cannot exit during process of synchronization";
		}
		
		return s.execute();
	}

	private String executeShowCommand() {
		Command showCommand = new ShowAllCommand(modelHandler, view);
		return showCommand.execute();
	}

	private boolean successfulExecution(String feedback) {
		return feedback.equals(Common.MESSAGE_NO_RESULTS)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_REMOVE)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_RECOVER)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_ADD)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_CLEAR_ALL)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_COMPLETE)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_EDIT)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_INCOMPLETE)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_MARK)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_SEARCH)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_SHOW_ALL)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_UNMARK)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_UNDO)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_REDO)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_HELP)
				|| feedback.equals(Common.MESSAGE_SUCCESSFUL_SETTINGS)
				|| feedback.equals(Common.MESSAGE_SYNC_SUCCESSFUL);
	}

	private void loadUpdateTimer() {
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				CustomDate.updateCurrentDate();
				updateList(modelHandler.getPendingList());
				updateList(modelHandler.getCompleteList());
				updateList(modelHandler.getTrashList());
				displayMessage();
			}
		}, 0, Common.MINUTE_IN_MILLIS);
		
		if(modelHandler.getAutoSync())
			autoSync();
	}

	private void displayMessage() {
		ObservableList<Task> list = modelHandler.getPendingList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getIsImportant() == true) {
				int remainingTimeForStartDate = list.get(i).getStartDate().getRemainingTime();
				int remainingTimeForEndDate = list.get(i).getEndDate().getRemainingTime();
				if (remainingTimeForStartDate <= 60 && remainingTimeForStartDate > 0 && remainingTimeForStartDate % 15 == 0) {
					view.trayIcon.displayMessage("Reminder", "Task \""
							+ list.get(i).getWorkInfo()
							+ "\"  will begin after the next "
							+ remainingTimeForStartDate + " minutes",
							MessageType.INFO);
				} else if (remainingTimeForEndDate <= 60 && remainingTimeForEndDate > 0 && remainingTimeForEndDate % 15 == 0) {
					view.trayIcon.displayMessage("Reminder",
							"Task \""
									+ list.get(i).getWorkInfo()
									+ "\" will end after the next con cac "
									+ remainingTimeForStartDate + " minutes",
							MessageType.INFO);
				}
			}
		}
	}

	private static void updateList(ObservableList<Task> list) {
		for (int i = 0; i < list.size(); i++) {
			list.get(i).updateDateString();
			if (list.get(i).isRecurringTask()) {
				list.get(i).updateDateForRepetitiveTask();
			}
		}
		Common.sortList(list);
	}

	public static void updateOverdueLine(ObservableList<Task> list) {
		boolean hasLastOverdue = false;
		for (int i = list.size() - 1; i >= 0; i--) {
			list.get(i).setIsLastOverdue(false);
			if (!hasLastOverdue && list.get(i).isOverdueTask()) {
				list.get(i).setIsLastOverdue(true);
				hasLastOverdue = true;
			}
		}
	}
}
