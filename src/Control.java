import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collections;
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
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Control extends Application {
	static final int MINUTE_IN_MILLIS = 60000;
	static final String MESSAGE_INVALID_COMMAND_TYPE = "Invalid Command Type!";
	static final String MESSAGE_EMPTY_COMMAND = "Empty Command!";
	static final String MESSAGE_INVALID_UNDO = "You cannot undo anymore!";
	static final String MESSAGE_INVALID_REDO = "You cannot redo anymore!";
	static final String MESSAGE_SUCCESSFUL_REDO = "Redo was successful.";
	static final String MESSAGE_ADD_TIP = "<add> <task info 1> <task info 2> <task info 3> <task info 4> ...";
	static final String MESSAGE_EDIT_TIP = "<edit/mod/modify> <index> <task info 1> <task info 2> <task info 3> ...";
	static final String MESSAGE_REMOVE_INDEX_TIP = "<delete/del/remove/rm> <index 1> <index 2> <index 3> ...";
	static final String MESSAGE_REMOVE_INFO_TIP = "<remove> <task info 1> <task info 2> <task info 3> <task info 4> ...";
	static final String MESSAGE_SEARCH_TIP = "<search/find> <task info 1> <task info 2> <task info 3> ...";
	static final String MESSAGE_TODAY_TIP = "<today>";
	static final String MESSAGE_SHOW_ALL_TIP = "<show/all/list/ls>";
	static final String MESSAGE_CLEAR_ALL_TIP = "<clear/clr>";
	static final String MESSAGE_UNDO_TIP = "<undo>";
	static final String MESSAGE_REDO_TIP = "<redo>";
	static final String MESSAGE_MARK_TIP = "<mark> <index 1> <index 2> <index 3> ...";
	static final String MESSAGE_UNMARK_TIP = "<unmark> <index 1> <index 2> <index 3> ...";
	static final String MESSAGE_COMPLETE_TIP = "<complete/done> <index 1> <index 2> <index 3> ...";
	static final String MESSAGE_INCOMPLETE_TIP = "<incomplete/undone> <index 1> <index 2> <index 3> ...";
	static final String MESSAGE_HELP_TIP = "<help>";
	static final String MESSAGE_SETTINGS_TIP = "<settings>";
	static final String MESSAGE_EXIT_TIP = "<exit>";
	static final String MESSAGE_REQUEST_COMMAND = "Please enter a command or type help to view commands.";

	static final String UNDO_COMMAND = "undo";
	static final String REDO_COMMAND = "redo";
	static final KeyCombination undo_hot_key = new KeyCodeCombination(
			KeyCode.Z, KeyCodeCombination.CONTROL_DOWN);
	static final KeyCombination redo_hot_key = new KeyCodeCombination(
			KeyCode.Y, KeyCodeCombination.CONTROL_DOWN);

	public Model modelHandler = new Model();
	public History commandHistory = new History();
	public View view;
	private Store dataFile;
	private SyncStore syncStore;
	public Synchronization sync = new Synchronization(modelHandler);

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
			dataFile = new DataStorage("dataStorage.txt", modelHandler);
			dataFile.loadFromFile();
			syncStore = new SyncStore("sync.xml", modelHandler);
			syncStore.retrieveAccount();
			syncStore.retrieveCalID();
		} catch (IOException e) {
			System.out.println("Cannot read the given file");
		}
	}

	private void loadGUI(Stage primaryStage) {
		view = new View(modelHandler, primaryStage, syncStore);
		handleEventForCommandLine();
		updateOverdueLine(modelHandler.getPendingList());
		updateOverdueLine(modelHandler.getCompleteList());
		updateOverdueLine(modelHandler.getTrashList());
		syncStore.storeAccount();
	}

	private void handleEventForCommandLine() {
		handleListener();
		handleKeyPressEvent();
	}

	private void handleListener() {
		view.txt.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				String command = view.txt.getText();
				realTimeFeedback(command);
				if (Parser.determineCommandType(command) == Parser.COMMAND_TYPES.SEARCH)
					realTimeSearch(command);
				else if (Parser.determineCommandType(command) == Parser.COMMAND_TYPES.REMOVE) {
					String content = removeCommandTypeString(command);
					try {
						Integer.parseInt(Parser.getFirstWord(content));
					} catch (NumberFormatException ex) {
						realTimeSearch("search" + content);
					}
				}
				if (isRealTimeSearch && !command.contains("search")
						&& !command.contains("remove")) {
					isRealTimeSearch = false;
					executeShowCommand();
				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				String command = view.txt.getText();
				realTimeFeedback(command);
				if (Parser.determineCommandType(command) == Parser.COMMAND_TYPES.SEARCH)
					realTimeSearch(command);
				else if (Parser.determineCommandType(command) == Parser.COMMAND_TYPES.REMOVE) {
					String content = removeCommandTypeString(command);
					try {
						Integer.parseInt(Parser.getFirstWord(content));
					} catch (NumberFormatException ex) {
						realTimeSearch("search" + content);
					}
				}
				if (isRealTimeSearch && !command.contains("search")
						&& !command.contains("remove")) {
					isRealTimeSearch = false;
					executeShowCommand();
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				String command = view.txt.getText();

				if (Parser.determineCommandType(command) == Parser.COMMAND_TYPES.SEARCH)
					realTimeSearch(command);
				else if (Parser.determineCommandType(command) == Parser.COMMAND_TYPES.REMOVE) {
					String content = removeCommandTypeString(command);
					try {
						Integer.parseInt(Parser.getFirstWord(content));
					} catch (NumberFormatException ex) {
						realTimeSearch("search" + content);
					}
				}
				if (isRealTimeSearch && !command.contains("search")
						&& !command.contains("remove")) {
					isRealTimeSearch = false;
					executeShowCommand();
				}
			}

			private boolean checkRemoveIndex(String command) {
				String content = Parser.removeFirstWord(command);
				String[] splitContent = Parser.splitBySpace(content);
				try {
					Integer.valueOf(splitContent[0]);
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			}

			private void realTimeFeedback(String command) {

				if (Parser.checkEmptyCommand(command)) {
					view.setFeedback(MESSAGE_REQUEST_COMMAND);
				} else {
					Parser.COMMAND_TYPES commandType = Parser
							.determineCommandType(command);
					if (commandType == Parser.COMMAND_TYPES.INVALID) {
						view.setFeedback(command);
					} else {
						switch (commandType) {
						case ADD:
							view.setFeedback(MESSAGE_ADD_TIP);
							break;
						case EDIT:
							view.setFeedback(MESSAGE_EDIT_TIP);
							break;
						case REMOVE:
							if (checkRemoveIndex(command))
								view.setFeedback(MESSAGE_REMOVE_INDEX_TIP);
							else
								view.setFeedback(MESSAGE_REMOVE_INFO_TIP);
							break;
						case SEARCH:
							view.setFeedback(MESSAGE_SEARCH_TIP);
							break;
						case SHOW_ALL:
							view.setFeedback(MESSAGE_SHOW_ALL_TIP);
							break;
						case UNDO:
							view.setFeedback(MESSAGE_UNDO_TIP);
							break;
						case REDO:
							view.setFeedback(MESSAGE_REDO_TIP);
							break;
						case MARK:
							view.setFeedback(MESSAGE_MARK_TIP);
							break;
						case UNMARK:
							view.setFeedback(MESSAGE_UNMARK_TIP);
							break;
						case COMPLETE:
							view.setFeedback(MESSAGE_COMPLETE_TIP);
							break;
						case INCOMPLETE:
							view.setFeedback(MESSAGE_INCOMPLETE_TIP);
							break;
						case TODAY:
							view.setFeedback(MESSAGE_TODAY_TIP);
							break;
						case CLEAR_ALL:
							view.setFeedback(MESSAGE_CLEAR_ALL_TIP);
							break;
						case HELP:
							view.setFeedback(MESSAGE_HELP_TIP);
							break;
						case SETTINGS:
							view.setFeedback(MESSAGE_SETTINGS_TIP);
							break;
						case EXIT:
							view.setFeedback(MESSAGE_EXIT_TIP);
							break;
						}
					}
				}
			}
		});
	}

	private void handleKeyPressEvent() {
		view.root2.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				if (undo_hot_key.match(e)) {
					isRealTimeSearch = false;
					String feedback = executeCommand("undo");
					updateFeedback(feedback);
				} else if (redo_hot_key.match(e)) {
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

	private String removeCommandTypeString(String command) {
		String commandTypeStr = Parser.getFirstWord(command);
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
			return MESSAGE_EMPTY_COMMAND;
		}
		try {
			Parser.COMMAND_TYPES commandType = Parser
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
			Parser.COMMAND_TYPES commandType) throws IllegalArgumentException,
			IOException {
		switch (commandType) {
		case ADD:
			return executeAddCommand(parsedUserCommand);
		case EDIT:
			return executeEditCommand(parsedUserCommand);
		case REMOVE:
			return executeRemoveCommand(parsedUserCommand);
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
			return MESSAGE_INVALID_COMMAND_TYPE;
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
		if (feedback.equals(Command.MESSAGE_SUCCESSFUL_ADD)) {
			commandHistory.updateCommand((TwoWayCommand) s);
			dataFile.storeToFile();
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
		if (feedback.equals(Command.MESSAGE_SUCCESSFUL_EDIT)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			dataFile.storeToFile();
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
		if (feedback.equals(Command.MESSAGE_SUCCESSFUL_REMOVE)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			dataFile.storeToFile();
			// syncFile.storeToFile();
			executeShowCommand();
		}
		return feedback;
	}

	private String executeUndoCommand() throws IOException {
		if (commandHistory.isUndoable()) {
			executeShowCommand();
			TwoWayCommand undoCommand = commandHistory.getPrevCommandForUndo();
			String feedback = undoCommand.undo();
			dataFile.storeToFile();
			return feedback;
		} else
			return MESSAGE_INVALID_UNDO;
	}

	private String executeRedoCommand() throws IOException {
		if (commandHistory.isRedoable()) {
			if (commandHistory.isAfterSearch()) {
				TwoWayCommand.setIndexType(TwoWayCommand.SEARCHED);
			}
			executeShowCommand();
			TwoWayCommand redoCommand = commandHistory.getPrevCommandForRedo();
			redoCommand.execute();
			dataFile.storeToFile();
			return MESSAGE_SUCCESSFUL_REDO;
		} else
			return MESSAGE_INVALID_REDO;
	}

	private String executeSearchCommand(String[] parsedUserCommand,
			boolean isRealTimeSearch) {
		Command s = new SearchCommand(parsedUserCommand, modelHandler, view,
				isRealTimeSearch);
		return s.execute();
	}

	private String executeTodayCommand(boolean isRealTimeSearch) {
		Command s = new TodayCommand(modelHandler, view, isRealTimeSearch);
		return s.execute();
	}

	private String executeClearCommand() throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		int tabIndex = view.getTabIndex();
		Command s = new ClearAllCommand(modelHandler, tabIndex);
		String feedback = s.execute();
		if (feedback.equals(Command.MESSAGE_SUCCESSFUL_CLEAR_ALL)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			dataFile.storeToFile();
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

		if (feedback.equals(Command.MESSAGE_SUCCESSFUL_COMPLETE)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			dataFile.storeToFile();
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

		if (feedback.equals(Command.MESSAGE_SUCCESSFUL_INCOMPLETE)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			dataFile.storeToFile();
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

		if (feedback.equals(Command.MESSAGE_SUCCESSFUL_MARK)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			dataFile.storeToFile();
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

		if (feedback.equals(Command.MESSAGE_SUCCESSFUL_UNMARK)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			dataFile.storeToFile();
			executeShowCommand();
		}
		return feedback;
	}

	private String executeHelpCommand() {
		Command s = new HelpCommand(modelHandler, view);
		return s.execute();
	}

	private String executeSettingsCommand(String[] parsedUserCommand) {
		Command s = new SettingsCommand(modelHandler, view, parsedUserCommand);
		String feedback = s.execute();
		if (feedback.equals(Command.MESSAGE_SUCCESSFUL_SETTINGS)) {
			syncStore.storeAccount();
			view.customizeGUI();
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

	private String executeSyncCommand(String[] parsedUserCommand)
			throws IOException {

		Command s = new SyncCommand(parsedUserCommand, modelHandler, sync);
		String feedback = s.execute();
		if (feedback.equals(Command.MESSAGE_SYNC_SUCCESSFUL)) {
			dataFile.storeToFile();
			syncStore.storeCalendarID();
			view.setTab(0);
			executeShowCommand();
		} /*
		 * else if
		 * (feedback.equals(Command.MESSAGE_SYNC_INVALID_USERNAME_PASSWORD)){
		 * view.showSettingsPage(); }
		 */
		return feedback;
	}

	private String executeExitCommand() {
		int tabIndex = view.getTabIndex();
		Command s = new ExitCommand(modelHandler, tabIndex);
		return s.execute();
	}

	private String executeShowCommand() {
		Command showCommand = new ShowAllCommand(modelHandler, view);
		return showCommand.execute();
	}

	private boolean successfulExecution(String feedback) {
		return feedback.equals(Command.MESSAGE_NO_RESULTS)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_REMOVE)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_ADD)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_CLEAR_ALL)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_COMPLETE)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_EDIT)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_INCOMPLETE)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_MARK)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_SEARCH)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_SHOW_ALL)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_UNMARK)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_UNDO)
				|| feedback.equals(MESSAGE_SUCCESSFUL_REDO)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_HELP)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_SETTINGS)
				|| feedback.equals(Command.MESSAGE_SYNC_SUCCESSFUL);
	}

	private void loadUpdateTimer() {
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("eeee");
				CustomDate.updateCurrentDate();
				updateList(modelHandler.getPendingList());
				updateList(modelHandler.getCompleteList());
				updateList(modelHandler.getTrashList());
			}
		}, 0, MINUTE_IN_MILLIS);
	}

	private static void updateList(ObservableList<Task> list) {
		for (int i = 0; i < list.size(); i++) {
			list.get(i).updateDateString();
			if (list.get(i).isRecurringTask()) {
				list.get(i).updateDateForRepetitiveTask();
			}
		}
		sortList(list);
	}

	public static void sortList(ObservableList<Task> list) {
		Collections.sort(list);
		updateIndexInList(list);
	}

	private static void updateIndexInList(ObservableList<Task> list) {
		boolean hasLastOverdue = false;
		for (int i = list.size() - 1; i >= 0; i--) {
			list.get(i).setIndexInList(i);
			list.get(i).setIsLastOverdue(false);
			if (!hasLastOverdue && list.get(i).isOverdueTask()) {
				list.get(i).setIsLastOverdue(true);
				hasLastOverdue = true;
			}
		}
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
