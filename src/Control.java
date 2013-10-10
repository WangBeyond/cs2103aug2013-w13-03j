import java.io.IOException;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Control extends Application {
	static final String MESSAGE_INVALID_COMMAND_TYPE = "Invalid Command Type!";
	static final String MESSAGE_EMPTY_COMMAND = "Empty Command!";
	static final String MESSAGE_INVALID_UNDO = "You cannot undo anymore!";
	static final String MESSAGE_INVALID_REDO = "You cannot redo anymore!";
	static final String MESSAGE_SUCCESSFUL_REDO = "Redo was successful.";
	static final String MESSAGE_ADD_TIP = "<add> <task info 1> <task info 2> <task info 3> <task info 4> ...";
	static final String MESSAGE_EDIT_TIP = "<edit/mod/modify> <index> <task info 1> <task info 2> <task info 3> ...";
	static final String MESSAGE_REMOVE_TIP = "<delete/del/remove/rm> <index 1> <index 2> <index 3> ...";
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
	static final String MESSAGE_EXIT_TIP = "<exit>";
	static final String MESSAGE_REQUEST_COMMAND = "Please enter a command";
	static final KeyCombination undo_hot_key = new KeyCodeCombination(
			KeyCode.Z, KeyCodeCombination.CONTROL_DOWN,
			KeyCodeCombination.ALT_DOWN);
	static final KeyCombination redo_hot_key = new KeyCodeCombination(
			KeyCode.Y, KeyCodeCombination.CONTROL_DOWN,
			KeyCodeCombination.ALT_DOWN);

	private Model modelHandler = new Model();
	private History commandHistory = new History();
	private View view;
	private Store dataFile;

	static boolean listedIndexType;
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

	private void loadData() {
		try {
			dataFile = new DataStorage("dataStorage.txt", modelHandler);
			dataFile.loadFromFile();
		} catch (IOException e) {
			System.out.println("Cannot read the given file");
		}
	}

	private void loadGUI(Stage primaryStage) {
		view = new View(modelHandler, primaryStage);
		handleEventForCommandLine();
	}

	private void handleEventForCommandLine() {
		handleListener();
		handleKeyPressEvent();
	}

	private void handleListener() {
		view.commandLine.textProperty().addListener(
				new ChangeListener<String>() {
					public void changed(ObservableValue<? extends String> ov,
							String oldValue, String newValue) {
						String command = view.commandLine.getText();
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
									view.setFeedback(MESSAGE_REMOVE_TIP);
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
		view.commandLine.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				if (e.getCode() == KeyCode.ENTER) {
					String feedback = executeCommand(view.commandLine.getText());
					updateFeedback(feedback);
				} else if (undo_hot_key.match(e)) {
					String feedback = executeCommand("undo");
					updateFeedback(feedback);

					e.consume();
				} else if (redo_hot_key.match(e)) {
					String feedback = executeCommand("redo");
					updateFeedback(feedback);
					e.consume();
				}
			}
		});
	}

	private void updateFeedback(String feedback) {
		if (successfulExecution(feedback))
			view.commandLine.setText("");
		view.emptyFeedback(0);
		view.setFeedbackStyle(0, feedback, Color.WHITE);
	}

	private String executeCommand(String userCommand) {
		boolean isEmptyCommand = Parser.checkEmptyCommand(userCommand);
		if (isEmptyCommand) {
			return MESSAGE_EMPTY_COMMAND;
		}

		try {
			Parser.COMMAND_TYPES commandType = Parser
					.determineCommandType(userCommand);

			String[] parsedUserCommand = Parser.parseCommand(userCommand,
					commandType);

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
				return executeSearchCommand(parsedUserCommand);
			case TODAY:
				return executeTodayCommand();
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
				// case SETTINGS:
				// return executeSettingsCommand(parsedUserCommand);
			case HELP:
				return executeHelpCommand();
				// case SYNC:
				// return executeSyncCommand(parsedUserCommand);
			case EXIT:
				return executeExitCommand();
			case INVALID:
				return MESSAGE_INVALID_COMMAND_TYPE;
			default:
				throw new Error("Unrecognised command type.");
			}
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private String executeAddCommand(String[] parsedUserCommand)
			throws IOException {
		Command s = new AddCommand(parsedUserCommand, modelHandler, view);
		String feedback = s.execute();
		if (feedback.equals(Command.MESSAGE_SUCCESSFUL_ADD)) {
			commandHistory.updateCommand((TwoWayCommand) s);
			dataFile.storeToFile();
			executeShowCommand();
		}
		return feedback;
	}

	private String executeEditCommand(String[] parsedUserCommand)
			throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;
		Command s = new EditCommand(parsedUserCommand, modelHandler, view);
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
		Command s = new RemoveCommand(parsedUserCommand, modelHandler, view);
		String feedback = s.execute();
		if (feedback.equals(Command.MESSAGE_SUCCESSFUL_REMOVE)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			dataFile.storeToFile();
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

	private String executeSearchCommand(String[] parsedUserCommand) {
		Command s = new SearchCommand(parsedUserCommand, modelHandler, view);
		return s.execute();
	}

	private String executeTodayCommand() {
		Command s = new TodayCommand(modelHandler, view);
		return s.execute();
	}

	private String executeClearCommand() throws IOException {
		boolean isAfterSearch = TwoWayCommand.listedIndexType;

		Command s = new ClearAllCommand(modelHandler, view);
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

		Command s = new CompleteCommand(parsedUserCommand, modelHandler, view);
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

		Command s = new IncompleteCommand(parsedUserCommand, modelHandler, view);
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

		Command s = new MarkCommand(parsedUserCommand, modelHandler, view);
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

		Command s = new UnmarkCommand(parsedUserCommand, modelHandler, view);
		String feedback = s.execute();

		if (feedback.equals(Command.MESSAGE_SUCCESSFUL_UNMARK)) {
			commandHistory.updateCommand((TwoWayCommand) s, isAfterSearch);
			dataFile.storeToFile();
			executeShowCommand();
		}
		return feedback;
	}
	
	private String executeHelpCommand(){
		Command s = new HelpCommand(modelHandler, view);
		return s.execute();
	}

	private String executeExitCommand() {
		Command s = new ExitCommand(modelHandler, view);
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
				|| feedback.equals(MESSAGE_SUCCESSFUL_REDO);
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
			}
		}, 0, 60000);
	}

	private static void updateList(ObservableList<Task> list) {
		for (int i = 0; i < list.size(); i++) {
			list.get(i).updateDateString();
			if (!list.get(i).getTag().getRepetition().equals(Parser.NULL)) {
				list.get(i).updateDate();
			}
		}
	}

	public static void sortList(ObservableList<Task> list) {
		Collections.sort(list);
		for (int i = 0; i < list.size(); i++) {
			list.get(i).setIndexInList(i);
		}
	}
}
