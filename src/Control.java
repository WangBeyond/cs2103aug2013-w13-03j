import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Control extends Application {
	private static final String MESSAGE_INVALID_COMMAND_TYPE = "Invalid Command Type!";
	private static final String MESSAGE_EMPTY_COMMAND = "Empty Command!";
	private static final String MESSAGE_SHOW_ALL = "Show all the tasks";
	private static final String MESSAGE_CLEAR_ALL = "Clear all the tasks";
	private static final String MESSAGE_SUCCESSFUL_SEARCH = "Successful Search!";
	private static final String MESSAGE_NO_RESULTS = "Search no results!";
	private static final String MESSAGE_SUCCESSFUL_ADD = "Task is added successfully";
	private static final String MESSAGE_SUCCESSFUL_EDIT = "Task is edited successfully";
	private static final String MESSAGE_SUCCESFUL_REMOVE = "Indicated tasks are removed";
	private static final String MESSAGE_INVALID_DATE_RANGE = "Invalid date range as start date is after end date";
	private static final String MESSAGE_DUPLICATE_INDEXES = "There are duplicate indexes";
	private static final String MESSAGE_INDEX_OUT_OF_BOUNDS = "There is an index outside the range of the list";
	private static final String MESSAGE_ADD_TIP = "<add> <task info 1> <task info 2> <task info 3> <task info 4> ...";
	private static final String MESSAGE_EDIT_TIP = "<edit/mod/modify> <index> <task info 1> <task info 2> <task info 3> ...";
	private static final String MESSAGE_REMOVE_TIP = "<delete/del/remove/rm> <index 1> <index 2> <index 3> ...";
	private static final String MESSAGE_SEARCH_TIP = "<search/find> <task info 1> <task info 2> <task info 3> ...";
	private static final String MESSAGE_TODAY_TIP = "<today>";
	private static final String MESSAGE_SHOW_ALL_TIP = "<show/all/list/ls>";
	private static final String MESSAGE_CLEAR_ALL_TIP = "<clear/clr>";
	private static final String MESSAGE_MARK_TIP = "<mark> <index 1> <index 2> <index 3> ...";
	private static final String MESSAGE_UNMARK_TIP = "<unmark> <index 1> <index 2> <index 3> ...";
	private static final String MESSAGE_COMPLETE_TIP = "<complete/done> <index 1> <index 2> <index 3> ...";
	private static final String MESSAGE_INCOMPLETE_TIP = "<incomplete/undone> <index 1> <index 2> <index 3> ...";
	private static final String MESSAGE_EXIT_TIP = "<exit>";
	private static final String MESSAGE_REQUEST_COMMAND = "Please enter a command";
	private static final String MESSAGE_SUCCESSFUL_MARK = "Task(s) %1$shas/have been marked successfully.";
	private static final String MESSAGE_FAILED_MARK = "Task(s) %1$shas/have not been successfully marked as index(es) is/are not valid.";
	private static final String MESSAGE_SUCCESSFUL_UNMARK = "Task(s) %1$shas/have been unmarked successfully.";
	private static final String MESSAGE_FAILED_UNMARK = "Task(s) %1$shas/have not been successfully unmarked as index(es) is/are not valid.";
	private static final String MESSAGE_SUCCESSFUL_CLEAR = "All %1$s tasks have been cleared.";
	private static final String MESSAGE_SUCCESSFUL_COMPLETE = "Task(s) %1$shas/have been marked as complete.";
	private static final String MESSAGE_FAILED_COMPLETE = "Task(s) %1$shas/have not been marked complete as index(es) is/are not valid.";
	private static final String MESSAGE_SUCCESSFUL_INCOMPLETE = "Task(s) %1$shas/have been marked as incomplete.";
	private static final String MESSAGE_FAILED_INCOMPLETE = "Task(s) %1$shas/have not been marked incomplete as index(es) is/are not valid.";

	public static final int VALID = 1;
	public static final int INVALID = -1;

	static private Model modelHandler = new Model();
	static private View view;
	static Stage primaryStage;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		Control.primaryStage = primaryStage;
		primaryStage.initStyle(StageStyle.UNDECORATED);
		view = new View(modelHandler, primaryStage);
		primaryStage.setTitle("iDo Prototype");
		hookUpEventForCommandLine();
		primaryStage.setScene(view.scene);
		primaryStage.show();
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				updateList(modelHandler.getPendingList());
				updateList(modelHandler.getCompleteList());
				updateList(modelHandler.getTrashList());
				updateList(modelHandler.getSearchPendingList());
				updateList(modelHandler.getSearchCompleteList());
				updateList(modelHandler.getSearchTrashList());
			}
		}, 0, 60000);
	}
	
	public static Model getModel() {
		return modelHandler;
	}

	private static void updateList(ObservableList<Task> list) {
		for (int i = 0; i < list.size(); i++)
			list.get(i).updateDateString();
	}

	private void hookUpEventForCommandLine() {
		view.commandLine.textProperty().addListener(
				new ChangeListener<String>() {
					public void changed(ObservableValue<? extends String> ov,
							String oldValue, String newValue) {
						String command = view.commandLine.getText();
						if (Parser.checkEmptyCommand(command)) {
							view.feedback.setText(MESSAGE_REQUEST_COMMAND);
						} else {
							Parser.COMMAND_TYPES commandType = Parser
									.determineCommandType(command);
							switch (commandType) {
							case ADD:
								view.feedback.setText(MESSAGE_ADD_TIP);
								break;
							case EDIT:
								view.feedback.setText(MESSAGE_EDIT_TIP);
								break;
							case REMOVE:
								view.feedback.setText(MESSAGE_REMOVE_TIP);
								break;
							case SEARCH:
								view.feedback.setText(MESSAGE_SEARCH_TIP);
								break;
							case SHOW_ALL:
								view.feedback.setText(MESSAGE_SHOW_ALL_TIP);
								break;
							case MARK:
								view.feedback.setText(MESSAGE_MARK_TIP);
								break;
							case UNMARK:
								view.feedback.setText(MESSAGE_UNMARK_TIP);
								break;
							case COMPLETE:
								view.feedback.setText(MESSAGE_COMPLETE_TIP);
								break;
							case INCOMPLETE:
								view.feedback.setText(MESSAGE_INCOMPLETE_TIP);
								break;
							case TODAY:
								view.feedback.setText(MESSAGE_TODAY_TIP);
								break;
							case CLEAR_ALL:
								view.feedback.setText(MESSAGE_CLEAR_ALL_TIP);
								break;
							case EXIT:
								view.feedback.setText(MESSAGE_EXIT_TIP);
								break;
							case INVALID:
								view.feedback.setText(MESSAGE_REQUEST_COMMAND);
								break;
							}
						}
					}
				});
		final KeyCombination changeTab = new KeyCodeCombination(KeyCode.TAB,
				KeyCombination.CONTROL_DOWN);

		view.commandLine.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				if (e.getCode() == KeyCode.ENTER) {
					String feedback = executeCommand(view.commandLine.getText());
					view.commandLine.setText("");
					view.feedback.setText(feedback);
				} else if (changeTab.match(e)) {
					int index = view.tabPane.getSelectionModel()
							.getSelectedIndex();
					if (index != 2)
						view.tabPane.getSelectionModel().selectNext();
					else
						view.tabPane.getSelectionModel().selectFirst();
				}
			}
		});
	}

	private static String executeCommand(String userCommand) {
		boolean isEmptyCommand = Parser.checkEmptyCommand(userCommand);
		if (isEmptyCommand) {
			return MESSAGE_EMPTY_COMMAND;
		}
		try {
			Parser.COMMAND_TYPES commandType = Parser
					.determineCommandType(userCommand);

			String[] parsedUserCommand = Parser.parseCommand(userCommand,
					commandType);
			Command s;
			switch (commandType) {
			case ADD:
				s = new ShowAllCommand(modelHandler, view);
				s.execute();
				s = new AddCommand(parsedUserCommand, modelHandler, view);
				return s.execute();
			case EDIT:
				s = new ShowAllCommand(modelHandler, view);
				s.execute();
				s = new EditCommand(parsedUserCommand, modelHandler, view);
				return s.execute();
			case REMOVE:
				s = new ShowAllCommand(modelHandler, view);
				s.execute();
				s = new RemoveCommand(parsedUserCommand, modelHandler, view);
				return s.execute();
				// case UNDO:
				// return executeUndoCommand(parsedUserCommand);
				// case REDO:
				// return executeRedoCommand(parsedUserCommand);
			case SEARCH:
				s = new SearchCommand(parsedUserCommand, modelHandler, view);
				return s.execute();
			case TODAY:
				s = new TodayCommand(modelHandler, view);
				return s.execute();
			case SHOW_ALL:
				s = new ShowAllCommand(modelHandler, view);
				return s.execute();
			case CLEAR_ALL:
				s = new ClearAllCommand(modelHandler, view);
				return s.execute();
			case COMPLETE:
				s = new ShowAllCommand(modelHandler, view);
				s.execute();
				s = new CompleteCommand(parsedUserCommand, modelHandler, view);
				return s.execute();
			case INCOMPLETE:
				s = new ShowAllCommand(modelHandler, view);
				s.execute();
				s = new IncompleteCommand(parsedUserCommand, modelHandler, view);
				return s.execute();
			case MARK:
				s = new ShowAllCommand(modelHandler, view);
				s.execute();
				s = new MarkCommand(parsedUserCommand, modelHandler, view);
				return s.execute();
			case UNMARK:
				s = new ShowAllCommand(modelHandler, view);
				s.execute();
				s = new UnmarkCommand(parsedUserCommand, modelHandler, view);
				return s.execute();
				// case SETTINGS:
				// return executeSettingsCommand(parsedUserCommand);
				// case HELP:
				// return executeHelpCommand(parsedUserCommand);
				// case SYNC:
				// return executeSyncCommand(parsedUserCommand);
			case EXIT:
				s = new ExitCommand(modelHandler, view, primaryStage);
				return s.execute();
			case INVALID:
				return MESSAGE_INVALID_COMMAND_TYPE;
			default:
				throw new Error("Unrecognised command type.");
			}
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private static String executeExitCommand() {
		primaryStage.close();
		System.exit(0);
		return null;
	}

	public static void sortList(ObservableList<Task> list) {
		Collections.sort(list);
	}

	public static String executeClearAllCommand(String[] clearCommand) {
		String clearMessage = "";

		if (clearCommand[0].equals("")) {
			/**
			 * if (getTab().equals("pending"){ modelHandler.clearPendingTasks();
			 * clearMessage = "pending"; } else if(getTab().equals("complete"){
			 * modelHandler.clearCompleteTasks(); clearMessage = "complete"; }
			 * else { modelHandler.clearTrash(); clearMessage = "trash"; }
			 */
		} else if (clearCommand[0].equals("all")) {
			modelHandler.clearPendingTasks();
			modelHandler.clearCompleteTasks();
			modelHandler.clearTrash();
			clearMessage = "pending, complete and trash";
		} else if (clearCommand[0].equals("pending")
				|| clearCommand[0].equals("p")) {
			modelHandler.clearPendingTasks();
			clearMessage = "pending";
		} else if (clearCommand[0].equals("complete")
				|| clearCommand[0].equals("c")) {
			modelHandler.clearCompleteTasks();
			clearMessage = "complete";
		} else if (clearCommand[0].equals("trash")
				|| clearCommand[0].equals("t")) {
			modelHandler.clearTrash();
			clearMessage = "trash";
		}
		return String.format(MESSAGE_SUCCESSFUL_CLEAR, clearMessage);
	}

	public static String executeUnmarkCommand(String[] splittedUserCommand) {
		int numPendingTasks = modelHandler.getPendingList().size();
		int indexCount = splittedUserCommand.length;
		String successfulUnmark = "", failedUnmark = "";

		for (int i = 0; i < indexCount; i++) {
			if (i >= numPendingTasks || i < 0) {
				failedUnmark += i + " ";
			} else {
				Task targetTask = modelHandler.getTaskFromPending(i);
				targetTask.setIsImportant(false);
				successfulUnmark += i + " ";
			}
		}

		if (failedUnmark.equals(""))
			return String.format(MESSAGE_SUCCESSFUL_UNMARK, successfulUnmark);
		else if (successfulUnmark.equals(""))
			return String.format(MESSAGE_FAILED_UNMARK, failedUnmark);
		else
			return String.format(MESSAGE_SUCCESSFUL_UNMARK, successfulUnmark)
					+ String.format(MESSAGE_FAILED_UNMARK, failedUnmark);
	}

	public static String executeIncompleteCommand(String[] splittedUserCommand) {
		int numCompleteTasks = modelHandler.getCompleteList().size();
		String successfulIncomplete = "", failedIncomplete = "";

		for (int i = 0; i < splittedUserCommand.length; i++) {
			if (Integer.parseInt(splittedUserCommand[i]) < numCompleteTasks
					&& Integer.parseInt(splittedUserCommand[i]) >= 0) {
				Task toIncomplete = modelHandler.getTaskFromComplete(i);
				modelHandler.removeTaskFromComplete(i);
				modelHandler.addTaskToPending(toIncomplete);
				// sort pending?
				successfulIncomplete += i + " ";
			} else {
				failedIncomplete += i + " ";
			}
		}

		if (successfulIncomplete.equals(""))
			return String.format(MESSAGE_SUCCESSFUL_INCOMPLETE,
					successfulIncomplete);
		else if (successfulIncomplete.equals(""))
			return String.format(MESSAGE_FAILED_INCOMPLETE, failedIncomplete);
		else
			return String.format(MESSAGE_SUCCESSFUL_INCOMPLETE,
					successfulIncomplete)
					+ String.format(MESSAGE_FAILED_INCOMPLETE, failedIncomplete);
	}
	
}
