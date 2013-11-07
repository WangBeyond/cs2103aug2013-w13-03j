import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

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

public class Control extends Application {
	static final String UNDO_COMMAND = "undo";
	static final String REDO_COMMAND = "redo";

	public Model modelHandler = new Model();
	public History commandHistory = new History();
	public View view;
	private Storage taskFile;
	private Storage settingStore;
	public Synchronization sync = new Synchronization(modelHandler);
	static public SyncCommand syncThread;
	private Timer syncTimer;
	static boolean isRealTimeSearch = false;
	static final boolean SEARCHED = true;
	static final boolean SHOWN = false;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		loadData();
		loadGUI(primaryStage);
		loadUpdateTimer();
	}

	public void loadData() {
		try {
			taskFile = new TaskStorage("task_storage.xml", modelHandler);
			taskFile.loadFromFile();
			settingStore = new SettingsStorage("setting_storage.xml", modelHandler);
			settingStore.loadFromFile();
			CustomDate.setDisplayRemaining(modelHandler.doDisplayRemaining());
			
		} catch (IOException e) {
			System.out.println("Cannot read the given file");
		}
	}

	private void loadGUI(Stage primaryStage) {
		view = new View(modelHandler, primaryStage, settingStore);
		addListenerForPreferences();
		handleEventForCommandLine();
		updateOverdueLine(modelHandler.getPendingList());
		updateOverdueLine(modelHandler.getCompleteList());
		updateOverdueLine(modelHandler.getTrashList());
		try{
			settingStore.storeToFile();
		} catch(IOException io) {
			view.setFeedback(io.getMessage());
		}
		
	}

	private void handleEventForCommandLine() {
		handleListener();
		handleKeyPressEvent();
	}

	private void handleListener() {
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

			private void realTimeUpdate() {
				String command = view.txt.getText();
				realTimeFeedback(command);
				if (Parser.determineCommandType(command) == Common.COMMAND_TYPES.SEARCH)
					realTimeSearch(command);
				else if (Parser.determineCommandType(command) == Common.COMMAND_TYPES.REMOVE) {
					String content = removeCommandTypeString(command);
					if (!content.matches("\\s*")) {
						if (!content.matches("\\s*\\d+.*"))
							realTimeSearch("search" + content);
					}
				}
				if (isRealTimeSearch && !command.contains("search")
						&& !command.contains("remove")&&!command.contains("rm")) {
					isRealTimeSearch = false;
					executeShowCommand();
				}
			}
			
			private boolean checkRemoveIndex(String command) {
				String content = Common.removeFirstWord(command);
				String[] splitContent = Common.splitBySpace(content);
				try {
					Integer.valueOf(splitContent[0]);
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			}

			private void realTimeFeedback(String command) {

				if (Parser.checkEmptyCommand(command)) {
					view.setFeedback(Common.MESSAGE_REQUEST_COMMAND);
				} else {
					Common.COMMAND_TYPES commandType = Parser
							.determineCommandType(command);
					if (commandType == Common.COMMAND_TYPES.INVALID) {
						view.setFeedback(command);
					} else {
						switch (commandType) {
						case ADD:
							view.setFeedback(Common.MESSAGE_ADD_TIP);
							break;
						case EDIT:
							view.setFeedback(Common.MESSAGE_EDIT_TIP);
							break;
						case REMOVE:
							if (checkRemoveIndex(command))
								view.setFeedback(Common.MESSAGE_REMOVE_INDEX_TIP);
							else
								view.setFeedback(Common.MESSAGE_REMOVE_INFO_TIP);
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
						}
					}
				}
			}
		});
	}

	private void handleKeyPressEvent() {
		view.generalBase.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				if (Common.undo_hot_key.match(e)) {
					isRealTimeSearch = false;
					String feedback = executeCommand("undo");
					updateFeedback(feedback);
				} else if (Common.redo_hot_key.match(e)) {
					isRealTimeSearch = false;
					String feedback = executeCommand("redo");
					updateFeedback(feedback);
				} else if (e.getCode() == KeyCode.F1) {
					isRealTimeSearch = false;
					String feedback = executeCommand("help");
					updateFeedback(feedback);
				}
			}
		});

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
		InputMap map = view.txt.getInputMap();
		map.put(enterKey, enterAction);

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
	
	private void addListenerForPreferences(){
		view.getSettingsItem().addActionListener(createPreferencesListener());
	}
	
	private ActionListener createPreferencesListener() {
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
	
	private String removeCommandTypeString(String command) {
		String commandTypeStr = Common.getFirstWord(command);
		return command.substring(commandTypeStr.length());
	}

	private void updateFeedback(String feedback) {
		if (successfulExecution(feedback)) {
			view.txt.setText("");
			view.txt.setCaretPosition(0);
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
