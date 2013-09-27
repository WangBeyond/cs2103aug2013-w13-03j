import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Control extends Application {
	private static final String MESSAGE_INVALID_COMMAND_TYPE = "Invalid Command Type!";
	private static final String MESSAGE_EMPTY_COMMAND = "Empty Command!";
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
				CustomDate.updateCurrentDate();
				updateList(modelHandler.getPendingList());
				updateList(modelHandler.getCompleteList());
				updateList(modelHandler.getTrashList());
			}
		}, 0, 60000);
	}

	public static Model getModel() {
		return modelHandler;
	}

	private static void updateList(ObservableList<Task> list) {
		for (int i = 0; i < list.size(); i++) {
			list.get(i).updateDateString();
			if (!list.get(i).getTag().getRepetition().equals("-"))
				list.get(i).updateDate();
		}
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
							if (commandType == Parser.COMMAND_TYPES.INVALID) {
								view.feedback.setFill(Color.WHITE);
								view.feedback.setText(MESSAGE_REQUEST_COMMAND);
							} else {
								view.feedback.setFill(Color.rgb(130, 255, 121));
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
									view.feedback
											.setText(MESSAGE_INCOMPLETE_TIP);
									break;
								case TODAY:
									view.feedback.setText(MESSAGE_TODAY_TIP);
									break;
								case CLEAR_ALL:
									view.feedback
											.setText(MESSAGE_CLEAR_ALL_TIP);
									break;
								case EXIT:
									view.feedback.setText(MESSAGE_EXIT_TIP);
									break;
								}
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
					if (successfulExecution(feedback))
						view.commandLine.setText("");
					view.feedback.setFill(Color.WHITE);
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

	private boolean successfulExecution(String feedback) {
		return feedback.equals(Command.MESSAGE_NO_RESULTS)
				|| feedback.equals(Command.MESSAGE_SUCCESFUL_REMOVE)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_ADD)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_CLEAR_ALL)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_COMPLETE)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_EDIT)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_INCOMPLETE)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_MARK)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_SEARCH)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_SHOW_ALL)
				|| feedback.equals(Command.MESSAGE_SUCCESSFUL_UNMARK);
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

	public static void sortList(ObservableList<Task> list) {
		Collections.sort(list);
	}
}
