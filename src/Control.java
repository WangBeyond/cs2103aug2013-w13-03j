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
	public static final String CALL_NORMAL_SETTINGS = "Normal settings";
	public static final String CALL_SETTINGS_FROM_SYNC = "Settings from sync";
	
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
	private Synchronization sync = new Synchronization(model, commandHistory);
	// Timer for auto sync
	private Timer syncTimer;
	//tabIndex for test cases to modify
	private int tabIndexTest = 0;
	
	//@author A0098077N
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
	
	//@author A0105667B
	/**
	 * Load the data from storage files
	 */
	void loadData() {
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
		settingStore = new SettingsStorage(Common.setting_fileName, model);
		settingStore.loadFromFile();
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
	 * Load the task data
	 */
	private void loadTask() throws IOException {
		taskFile = new TaskStorage(Common.task_fileName, model);
		taskFile.loadFromFile();
	}
	
	//@author A0098077N
	/**
	 * Initialize the graphical user interface of the application. Setup the
	 * listeners and key bindings in the interface
	 * 
	 * @param primaryStage
	 *            the main window of the application
	 */
	private void loadGUI(Stage primaryStage) {
		view = new View(model, primaryStage);
		addListenerForPreferences();
		handleEventForCommandLine();
		updateLastOverdueTasks();
		storeSettings();
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
		view.getCommandLine().getDocument().addDocumentListener(new DocumentListener() {
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
				String command = view.getCommandLine().getText();
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
					setFeedbackForRemoveCommand(command);
					break;
				case RECOVER:
					setFeedbackForRecoverCommand(command);
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
					setFeedbackForMarkCommand(command);
					break;
				case UNMARK:
					setFeedbackForUnmarkCommand(command);
					break;
				case COMPLETE:
					setFeedbackForCompleteCommand(command);
					break;
				case INCOMPLETE:
					setFeedbackForIncompleteCommand(command);
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

			private void setFeedbackForIncompleteCommand(String command) {
				if (isIndexCommand(command)) {
					view.setFeedback(Common.MESSAGE_INCOMPLETE_INDEX_TIP);
				} else {
					view.setFeedback(Common.MESSAGE_INCOMPLETE_INFO_TIP);
				}
			}

			private void setFeedbackForCompleteCommand(String command) {
				if (isIndexCommand(command)) {
					view.setFeedback(Common.MESSAGE_COMPLETE_INDEX_TIP);
				} else {
					view.setFeedback(Common.MESSAGE_COMPLETE_INFO_TIP);
				}
			}

			private void setFeedbackForUnmarkCommand(String command) {
				if (isIndexCommand(command)) {
					view.setFeedback(Common.MESSAGE_UNMARK_INDEX_TIP);
				} else {
					view.setFeedback(Common.MESSAGE_UNMARK_INFO_TIP);
				}
			}

			private void setFeedbackForMarkCommand(String command) {
				if (isIndexCommand(command)) {
					view.setFeedback(Common.MESSAGE_MARK_INDEX_TIP);
				} else {
					view.setFeedback(Common.MESSAGE_MARK_INFO_TIP);
				}
			}

			private void setFeedbackForRecoverCommand(String command) {
				if (isIndexCommand(command)) {
					view.setFeedback(Common.MESSAGE_RECOVER_INDEX_TIP);
				} else {
					view.setFeedback(Common.MESSAGE_RECOVER_INFO_TIP);
				}
			}

			private void setFeedbackForRemoveCommand(String command) {
				if (isIndexCommand(command)) {
					view.setFeedback(Common.MESSAGE_REMOVE_INDEX_TIP);
				} else {
					view.setFeedback(Common.MESSAGE_REMOVE_INFO_TIP);
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
		InputMap map = view.getCommandLine().getInputMap();
		assert map != null;
		addKeyBindingForExecution(map);
		addKeyBindingForUndo(map);
		addKeyBindingForRedo(map);
		addKeyBidningForHelp(map);
	}
	
	// Setup the hot keys in the application
	private void setupHotkeys() {
		view.getMainRoot().setOnKeyPressed(new EventHandler<KeyEvent>() {
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
	@SuppressWarnings("serial")
	private void addKeyBindingForExecution(InputMap map) {
		Action enterAction = new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						isRealTimeSearch = false;
						String feedback = executeCommand(view.getCommandLine().getText());
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
	@SuppressWarnings("serial")
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
	@SuppressWarnings("serial")
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
	@SuppressWarnings("serial")
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
		view.getSettingsItemInPopupMenu().addActionListener(createPreferencesListenerInSystemTray());
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
						String feedback = executeCommand("settings");
						updateFeedback(feedback);
					}
				});
			}
		};
	}
	
	//@author A0105667B
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
	
	//@author A0105523U
	/******************************************************* EXECUTION SECTION *************************************************************/
	/**
	 * This function is the main function for executing all command inputs from
	 * users
	 * 
	 * @param userCommand
	 *            the command input from the user
	 * @return the corresponding feedback according to the command
	 */
	public String executeCommand(String userCommand) {
		boolean isEmptyCommand = Parser.checkEmptyCommand(userCommand);
		if (isEmptyCommand) {
			return Common.MESSAGE_EMPTY_COMMAND;
		}
		
		try {
			Common.COMMAND_TYPES commandType = Parser.determineCommandType(userCommand);
			int tabIndex = getTabIndex();
			assert tabIndex >= 0 && tabIndex <= 2;
			String[] parsedUserCommand = Parser.parseCommand(userCommand, commandType, model, tabIndex);
			return executeCommandCorrespondingType(parsedUserCommand, commandType);
		} catch (Exception e) {
			return e.getMessage(); // the corresponding error message
		}
	}
	
	//@author A0098077N
	private int getTabIndex() {
		if(view == null){
			return tabIndexTest;
		}
		int tabIndex = view.getTabIndex();
		return tabIndex;
	}
	
	//@author A0105667B
	void setTabForTest(int tabIndex) {
		tabIndexTest = tabIndex;
	}
	
	//@author A0105523U
	/**
	 * This function is used to execute according to each specific command
	 * 
	 * @param parsedUserCommand
	 *            the parsed command array
	 * @param commandType
	 *            type of command
	 * @return the corresponding feedback
	 */
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
			return executeSettingsCommand(CALL_NORMAL_SETTINGS);
		case HELP:
			return executeHelpCommand();
		case SYNC:
			return executeSyncCommand();
		case EXIT:
			return executeExitCommand();
		case INVALID:
			return Common.MESSAGE_INVALID_COMMAND_TYPE;
		default:
			throw new Error("Unrecognised command type.");
		}
	}
	
	//@author A0105667B
	/**
	 * Start real time search from the specific search Command
	 * 
	 * @param searchCommand
	 *            the input search command
	 */
	private void realTimeSearch(String searchCommand) {
		isRealTimeSearch = true;
		boolean hasOnlySearchCommandType = searchCommand.trim().equals("search") || searchCommand.trim().equals("find");
		if (hasOnlySearchCommandType) {
			executeShowCommand();
		} else {
			executeCommand(searchCommand);
		}
	}
	
	//@author A0105523U
	/**
	 * ADD command execution
	 */
	private String executeAddCommand(String[] parsedUserCommand)
			throws IOException {
		int tabIndex = getTabIndex();
		assert tabIndex >= 0 && tabIndex <= 2;
		Command addCommand = new AddCommand(parsedUserCommand, model, tabIndex);
		String feedback = addCommand.execute();
		
		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_ADD)) {
			commandHistory.updateCommand((TwoWayCommand) addCommand);
			storeTask();
			if(view != null) {
				view.setTab(Common.PENDING_TAB);
				executeShowCommand();
			}
		}
		return feedback;
	}
	
	//@author A0105667B
	/*
	 * Store task info into storage file
	 */
	private void storeTask() throws IOException {
		taskFile.storeToFile();
	}
	
	/**
	 * EDIT command execution
	 */
	private String executeEditCommand(String[] parsedUserCommand) throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = getTabIndex();
		assert tabIndex >= 0 && tabIndex <= 2;
		Command editCommand = new EditCommand(parsedUserCommand, model, tabIndex);
		String feedback = editCommand.execute();
		
		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_EDIT)) {
			commandHistory.updateCommand((TwoWayCommand) editCommand, isAfterSearch);
			storeTask();
			if(view != null) {
				executeShowCommand();
			}
		}
		return feedback;
	}
	
	/**
	 * REMOVE command execution
	 */
	private String executeRemoveCommand(String[] parsedUserCommand) throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = getTabIndex();
		assert tabIndex >= 0 && tabIndex <= 2;
		Command removeCommand = new RemoveCommand(parsedUserCommand, model, tabIndex);
		String feedback = removeCommand.execute();
		
		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_REMOVE)) {
			commandHistory.updateCommand((TwoWayCommand) removeCommand, isAfterSearch);
			storeTask();
			if(view != null) {
				executeShowCommand();
			}
		}
		return feedback;
	}
	
	//@author A0100927M
	/**
	 * UNDO command execution
	 */
	private String executeUndoCommand() throws IOException {
		if (isUnderSyncingProcess()){
			return Common.MESSAGE_UNDO_RESTRICTION;
		}
		
		if (commandHistory.isUndoable()) {
			if(view != null) {
				executeShowCommand();
			}
			TwoWayCommand undoCommand = commandHistory.getPrevCommandForUndo();
			String feedback = undoCommand.undo();
			storeTask();
			return feedback;
		} 
		return Common.MESSAGE_INVALID_UNDO;
	}
	
	/**
	 * REDO command execution
	 */
	private String executeRedoCommand() throws IOException {
		if (isUnderSyncingProcess()){
			return Common.MESSAGE_REDO_RESTRICTION;
		}
		
		if (commandHistory.isRedoable()) {
			if (commandHistory.isAfterSearch()) {
				TwoWayCommand.setIndexType(TwoWayCommand.SEARCHED);
			}
			if(view != null) {
				executeShowCommand();
			}
			TwoWayCommand redoCommand = commandHistory.getPrevCommandForRedo();
			redoCommand.redo();
			storeTask();
			return Common.MESSAGE_SUCCESSFUL_REDO;
		} 
		return Common.MESSAGE_INVALID_REDO;
	}
	
	//@author A0098077N
	/**
	 * SEARCH command execution
	 */
	private String executeSearchCommand(String[] parsedUserCommand,
			boolean isRealTimeSearch) {
		Command searchCommand = new SearchCommand(parsedUserCommand, model, view,
				isRealTimeSearch);
		return searchCommand.execute();
	}
	
	/**
	 * TODAY command execution
	 */
	private String executeTodayCommand(boolean isRealTimeSearch) {
		return executeCommand("search today");
	}
	
	//@author A0100927M
	/**
	 * CLEAR command execution
	 */
	private String executeClearCommand() throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = getTabIndex();
		assert tabIndex >= 0 && tabIndex <= 2;
		Command clearCommand = new ClearAllCommand(model, tabIndex);
		String feedback = clearCommand.execute();
		
		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_CLEAR_ALL)) {
			commandHistory.updateCommand((TwoWayCommand) clearCommand, isAfterSearch);
			storeTask();
			if(view != null) {
				executeShowCommand();
			}
		}
		return feedback;
	}
	
	/**
	 * COMPLETE command execution
	 */
	private String executeCompleteCommand(String[] parsedUserCommand)
			throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = getTabIndex();
		assert tabIndex >= 0 && tabIndex <= 2;
		Command completeCommand = new CompleteCommand(parsedUserCommand, model, tabIndex);
		String feedback = completeCommand.execute();

		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_COMPLETE)) {
			commandHistory.updateCommand((TwoWayCommand) completeCommand, isAfterSearch);
			storeTask();
			if(view != null) {
				executeShowCommand();
			}
		}
		return feedback;
	}
	
	/**
	 * INCOMPLETE command execution
	 */
	private String executeIncompleteCommand(String[] parsedUserCommand)
			throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = getTabIndex();
		assert tabIndex >= 0 && tabIndex <= 2;
		Command incompleteCommand = new IncompleteCommand(parsedUserCommand, model, tabIndex);
		String feedback = incompleteCommand.execute();

		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_INCOMPLETE)) {
			commandHistory.updateCommand((TwoWayCommand) incompleteCommand, isAfterSearch);
			storeTask();
			if(view != null) {
				executeShowCommand();
			}
		}
		return feedback;
	}
	
	//@author A0098077N
	/**
	 * RECOVER command execution
	 */
	private String executeRecoverCommand(String[] parsedUserCommand) throws IOException{
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = getTabIndex();
		assert tabIndex >= 0 && tabIndex <= 2;
		Command recoverCommand = new RecoverCommand(parsedUserCommand, model, tabIndex);
		String feedback = recoverCommand.execute();
		
		if(feedback.equals(Common.MESSAGE_SUCCESSFUL_RECOVER)){
			commandHistory.updateCommand((TwoWayCommand)recoverCommand, isAfterSearch);
			storeTask();
			if(view != null) {
				executeShowCommand();
			}
		}
		return feedback;
	}
	
	//@author A0100927M
	/**
	 * MARK command execution
	 */
	private String executeMarkCommand(String[] parsedUserCommand)
			throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = getTabIndex();
		assert tabIndex >= 0 && tabIndex <= 2;
		Command markCommand = new MarkCommand(parsedUserCommand, model, tabIndex);
		String feedback = markCommand.execute();

		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_MARK)) {
			commandHistory.updateCommand((TwoWayCommand) markCommand, isAfterSearch);
			storeTask();
			if(view != null) {
				executeShowCommand();
			}
		}
		return feedback;
	}
	
	/**
	 * UNMARK command execution
	 */
	private String executeUnmarkCommand(String[] parsedUserCommand)
			throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = getTabIndex();
		assert tabIndex >= 0 && tabIndex <= 2;
		Command unmarkCommand = new UnmarkCommand(parsedUserCommand, model, tabIndex);
		String feedback = unmarkCommand.execute();

		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_UNMARK)) {
			commandHistory.updateCommand((TwoWayCommand) unmarkCommand, isAfterSearch);
			storeTask();
			if(view != null) {
				executeShowCommand();
			}
		}
		return feedback;
	}
	
	/**
	 * HELP command execution
	 */
	private String executeHelpCommand() {
		Command helpCommand = new HelpCommand(model, view);
		return helpCommand.execute();
	}
	
	/**
	 * SETTINGS command execution
	 */
	private String executeSettingsCommand(String origin) throws IOException{
		view.getStage().hide();
		String previousTheme = model.getThemeMode();
		if(syncTimer != null)
			syncTimer.cancel();
		
		Command settingsCommand = new SettingsCommand(model, view, origin);
		String feedback = settingsCommand.execute();
		
		if (feedback.equals(Common.MESSAGE_SUCCESSFUL_SETTINGS)) {
			settingStore.updateToFile();
			updateGUI(previousTheme);
			initializeAutoSync();
			updateTimeFormat();
		}
		view.getStage().toFront();
		view.getStage().show();
		return feedback;
	}
	
	//@author A0098077N
	/*
	 * Update the time format display after changing settings
	 */
	private void updateTimeFormat() {
		CustomDate.setDisplayRemaining(model.doDisplayRemaining());
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				updateAllTasks();
			}
		});
	}
		
	/*
	 * Update the interface including theme mode and color scheme after changing settings
	 */
	private void updateGUI(String previousTheme) {
		boolean hasThemeChanged = !previousTheme.equals(model.getThemeMode());
		if(hasThemeChanged)
				view.customizeGUI();
		view.setColourScheme(model.getColourScheme());
		setupChangeListener();
	}
	
	//@author A0105667B
	/*
	 * Reset the sync timer each time after changing settings or at the start when open application
	 */
	private void setupAutoSyncTimer() {
		syncTimer = new Timer();
		syncTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						try {
							syncThread = new SyncCommand(model, sync, view,
									taskFile);
							if (syncThread.getFeedback() != null
									&& syncThread.getFeedback()
											.equals(Common.MESSAGE_SYNC_INVALID_USERNAME_PASSWORD)) {
								executeSettingsCommand(
										CALL_SETTINGS_FROM_SYNC);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});

			}
		}, 0, model.getSyncPeriod() * Common.MINUTE_IN_MILLIS);
	}
	
	//@author A0105523U
	/**
	 * SYNC command execution
	 */
	private String executeSyncCommand() throws IOException {
		// Check whether there is already a sync thread
		if (!isUnderSyncingProcess()){
			syncThread = new SyncCommand(model, sync, view,
					taskFile);
			if(syncThread.getFeedback() != null && syncThread.getFeedback().equals(Common.MESSAGE_SYNC_INVALID_USERNAME_PASSWORD)){
				executeSettingsCommand(CALL_SETTINGS_FROM_SYNC);
			}
		}
		
		clearCommandLine();
		return Common.MESSAGE_REQUEST_COMMAND;
	}
	
	//@author A0098077N
	private void clearCommandLine() {
		view.getCommandLine().setText("");
	}
	
	/**
	 * EXIT command execution
	 */
	private String executeExitCommand() {
		int tabIndex = getTabIndex();
		assert tabIndex >= 0 && tabIndex <= 2;
		Command exitCommand = new ExitCommand(model, tabIndex);
		if(isUnderSyncingProcess()){
			return Common.MESSAGE_EXIT_RESTRICTION;
		}
		
		return exitCommand.execute();
	}
	
	//@author A0105667B
	// Indicator whether the application is under syncing process
	private boolean isUnderSyncingProcess() {
		return syncThread != null && syncThread.isRunning();
	}
	
	//@author A0098077N
	/**
	 * SHOW command execution
	 */
	private String executeShowCommand() {
		Command showCommand = new ShowAllCommand(model, view);
		int tabIndex = getTabIndex();
		assert tabIndex >= 0 && tabIndex <= 2;
		updateOverdueLineForSpecificTab(tabIndex);
		
		return showCommand.execute();
	}
	
	/**
	 * Update the last overdue task in a specific tab
	 * 
	 * @param tab
	 *            the given tab
	 */
	private void updateOverdueLineForSpecificTab(int tab) {
		if(tab == Common.PENDING_TAB){
			updateOverdueLine(model.getPendingList());
		} else if(tab == Common.COMPLETE_TAB){
			updateOverdueLine(model.getCompleteList());
		} else if(tab == Common.TRASH_TAB){
			updateOverdueLine(model.getTrashList());
		}
	}
	
	// Check whether the feedback is a signal of successful execution
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
	
	/*****************************Timer and update control of system*************************************************/
	
	/**
	 * Initialize the auto sync and update timer 
	 */
	private void loadTimer() {
		initializeUpdateTimer();
		initializeAutoSync();
	}
	
	// Setup the update timer for task every 1 minute
	private void initializeUpdateTimer() {
		Timer updateTimer = new Timer();
		updateTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				updateAllTasks();
				checkDisplayMessages();
			}
		}, 0, Common.MINUTE_IN_MILLIS);
	}
	
	//@author A0105667B
	// Setup the auto sync timer with assigned period
	private void initializeAutoSync() {
		if(model.hasAutoSync()){
			setupAutoSyncTimer();
		}
	}
	
	//@author A0098077N
	/**
	 * This function checks which tasks in pending list need to be reminded
	 */
	private void checkDisplayMessages() {
		ObservableList<Task> list = model.getPendingList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isImportantTask()) {
				displayMessageForImportantTask(list.get(i));
			}
		}
	}
	
	// Check whether a task should be reminded
	private void displayMessageForImportantTask(Task task) {
		int remainingTimeForStartDate = task.getStartDate().getRemainingTime();
		int remainingTimeForEndDate = task.getEndDate().getRemainingTime();
		if (isTimeForReminding(remainingTimeForStartDate)) {
			displayMessageForStartDate(task.getWorkInfo(),remainingTimeForStartDate);
		} else if (isTimeForReminding(remainingTimeForEndDate)) {
			displayMessageForEndDate(task.getWorkInfo(), remainingTimeForEndDate);
		}
	}
	
	// Indicator that the remaining time is valid to be reminded
	private boolean isTimeForReminding(int remainingTime) {
		return remainingTime <= 60 && remainingTime > 0 && remainingTime % 15 == 0;
	}
	
	// Reminded a task as the task is about to end
	private void displayMessageForEndDate(String taskInfo,
			int remainingTimeForEndDate) {
		view.getTrayIcon().displayMessage("Reminder", String.format(Common.POPUP_MESSAGE_END_DATE,  taskInfo, remainingTimeForEndDate),
				MessageType.INFO);
	}
	
	// Reminded a task as the task is about to start
	private void displayMessageForStartDate(String taskInfo,
			int remainingTimeForStartDate) {
		view.getTrayIcon().displayMessage("Reminder", String.format(Common.POPUP_MESSAGE_START_DATE, taskInfo, remainingTimeForStartDate),
				MessageType.INFO);
	}
	
	// Update the time of all tasks currently in the application
	private void updateAllTasks() {
		CustomDate.updateCurrentDate();
		updateList(model.getPendingList());
		updateList(model.getCompleteList());
		updateList(model.getTrashList());
	}
	
	/**
	 * This function updates the string displays for dates of all tasks in the
	 * given list. If this is a recurring task and falls behind the current time,
	 * the time will be updated
	 * 
	 * @param list
	 *            the given list
	 */
	private static void updateList(ObservableList<Task> list) {
		for (int i = 0; i < list.size(); i++) {
			list.get(i).updateDateString();
			if (list.get(i).isRecurringTask()) {
				list.get(i).updateDateForRepetitiveTask();
			}
		}
		Common.sortList(list);
	}
	
	/**
	 * Update the last overdue task in a given list
	 * 
	 * @param list
	 *            the given list
	 */
	private static void updateOverdueLine(ObservableList<Task> list) {
		boolean hasLastOverdueTask = false;
		for (int i = list.size() - 1; i >= 0; i--) {
			list.get(i).setIsLastOverdue(false);
			boolean isLastOverdue = !hasLastOverdueTask && list.get(i).isOverdueTask();
			if (isLastOverdue) {
				list.get(i).setIsLastOverdue(true);
				hasLastOverdueTask = true;
			}
		}
	}
	
	//@author A0105667B
	public Model getModel() {
		return model;
	}
	
	public Storage getTaskFile() {
		return taskFile;
	}
	
	public Storage getSettingsFile() {
		return settingStore;
	}
}
