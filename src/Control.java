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

			switch (commandType) {
			case ADD:
				executeShowAllCommand();
				return executeAddCommand(parsedUserCommand);
			case EDIT:
				executeShowAllCommand();
				return executeEditCommand(parsedUserCommand);
			case REMOVE:
				executeShowAllCommand();
				return executeRemoveCommand(parsedUserCommand);
				// case UNDO:
				// return executeUndoCommand(parsedUserCommand);
				// case REDO:
				// return executeRedoCommand(parsedUserCommand);
			case SEARCH:
				executeShowAllCommand();
				return executeSearchCommand(parsedUserCommand);
			case TODAY:
				return executeTodayCommand();
			case SHOW_ALL:
				return executeShowAllCommand();
			case CLEAR_ALL:
				executeShowAllCommand();
				return executeClearAllCommand();
				// case COMPLETE:
				// return executeCompleteCommand(parsedUserCommand);
				// case INCOMPLETE:
				// return executeIncompleteCommand(parsedUserCommand);
				// case MARK:
				// return executeMarkCommand(parsedUserCommand);
				// case UNMARK:
				// return executeUnmarkCommand(parsedUserCommand);
				// case SETTINGS:
				// return executeSettingsCommand(parsedUserCommand);
				// case HELP:
				// return executeHelpCommand(parsedUserCommand);
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

	public static String executeTodayCommand() {
		return executeSearchCommand(new String[] { Parser.NULL, Parser.NULL,
				Parser.NULL, "today", Parser.FALSE });
	}

	public static String executeShowAllCommand() {
		int tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
		if (tabIndex == 0)
			view.taskPendingList.setItems(modelHandler.getPendingList());
		else if (tabIndex == 1)
			view.taskCompleteList.setItems(modelHandler.getCompleteList());
		else
			view.taskTrashList.setItems(modelHandler.getTrashList());
		return MESSAGE_SHOW_ALL;
	}

	public static String executeClearAllCommand() {
		int tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
		ObservableList<Task> modifiedList = FXCollections.observableArrayList();
		if (tabIndex == 0)
			modifiedList = modelHandler.getPendingList();
		else if (tabIndex == 1)
			modifiedList = modelHandler.getCompleteList();
		else
			modifiedList = modelHandler.getTrashList();

		for (int i = 0; i < modifiedList.size(); i++)
			modelHandler.removeTask(i, tabIndex);

		if (tabIndex == 1 || tabIndex == 0)
			sortList(modelHandler.getTrashList());

		return MESSAGE_CLEAR_ALL;

	}

	public static String executeAddCommand(String[] parsedUserCommand) {
		String workInfo = parsedUserCommand[0];
		String tag = parsedUserCommand[1];
		String startDateString = parsedUserCommand[2];
		String endDateString = parsedUserCommand[3];
		boolean isImptTask = false;

		if (parsedUserCommand[4].equals(Parser.TRUE)) {
			isImptTask = true;
		}

		Task task = new Task();

		task.setWorkInfo(workInfo);

		if (!startDateString.equals(Parser.NULL)) {
			CustomDate startDate = new CustomDate(startDateString);
			task.setStartDate(startDate);
		}

		if (!endDateString.equals(Parser.NULL)) {
			CustomDate endDate = new CustomDate(endDateString);
			if (endDate != null && endDate.getHour() == 0
					&& endDate.getMinute() == 0) {
				endDate.setHour(23);
				endDate.setMinute(59);
			}
			task.setEndDate(endDate);
		}

		if (!startDateString.equals(Parser.NULL)
				&& !endDateString.equals(Parser.NULL)) {
			if (CustomDate.compare(task.getEndDate(), task.getStartDate()) < 0)
				return MESSAGE_INVALID_DATE_RANGE;
		}

		if (!tag.equals(Parser.NULL)) {
			task.setTag(tag);
		}else{
			task.setTag("-");
		}
		task.setIsImportant(isImptTask);

		modelHandler.addTaskToPending(task);

		sortList(modelHandler.getPendingList());

		return MESSAGE_SUCCESSFUL_ADD;
	}

	// The user can edit the starting time, ending time, tag and isImportant of
	// existing tasks
	public static String executeEditCommand(String[] parsedUserCommand) {
		int tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
		ObservableList<Task> modifiedList = FXCollections.observableArrayList();
		if (tabIndex == 0)
			modifiedList = modelHandler.getPendingList();
		else if (tabIndex == 1)
			modifiedList = modelHandler.getCompleteList();
		else
			modifiedList = modelHandler.getTrashList();

		int index = Integer.parseInt(parsedUserCommand[0]);
		if (index > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		Task targetTask = modifiedList.get(index - 1);

		String workInfo = parsedUserCommand[1];
		String tag = parsedUserCommand[2];
		String startDateString = parsedUserCommand[3];
		String endDateString = parsedUserCommand[4];
		boolean hasImptTaskToggle = (parsedUserCommand[5].equals(Parser.TRUE)) ? true
				: false;
		CustomDate startDate, endDate;
		startDate = endDate = null;

		if (!startDateString.equals(Parser.NULL)) {
			startDate = new CustomDate(startDateString);
		}

		if (!endDateString.equals(Parser.NULL)) {
			endDate = new CustomDate(endDateString);
			if (endDate.getHour() == 0 && endDate.getMinute() == 0) {
				endDate.setHour(23);
				endDate.setMinute(59);
			}
		}

		if (startDate != null && endDate != null
				&& CustomDate.compare(endDate, startDate) < 0)
			return MESSAGE_INVALID_DATE_RANGE;

		if (!workInfo.equals(Parser.NULL))
			targetTask.setWorkInfo(workInfo);

		if (startDate != null)
			targetTask.setStartDate(startDate);

		if (endDate != null)
			targetTask.setEndDate(endDate);

		if (tag != Parser.NULL)
			targetTask.setTag(tag);
		

		if (hasImptTaskToggle)
			targetTask.setIsImportant(!targetTask.getIsImportant());

		sortList(modifiedList);
		return MESSAGE_SUCCESSFUL_EDIT;
	}

	public static String executeRemoveCommand(String[] splittedUserCommand) {
		int tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
		ObservableList<Task> modifiedList = FXCollections.observableArrayList();
		if (tabIndex == 0)
			modifiedList = modelHandler.getPendingList();
		else if (tabIndex == 1)
			modifiedList = modelHandler.getCompleteList();
		else
			modifiedList = modelHandler.getTrashList();

		int indexCount = splittedUserCommand.length;
		int[] indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(splittedUserCommand[i]);
		}

		Arrays.sort(indexList);

		for (int i = 0; i < indexCount - 1; i++)
			if (indexList[i] == indexList[i + 1])
				return MESSAGE_DUPLICATE_INDEXES;

		if (indexList[indexCount - 1] > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		for (int i = indexCount - 1; i >= 0; i--) {
			modelHandler.removeTask(indexList[i] - 1, tabIndex);
		}

		if (tabIndex == 1 || tabIndex == 0)
			sortList(modelHandler.getTrashList());

		return MESSAGE_SUCCESFUL_REMOVE;
	}

	private static String executeExitCommand() {
		primaryStage.close();
		System.exit(0);
		return null;
	}

	private static String executeSearchCommand(String[] splittedUserCommand) {
		int index = view.tabPane.getSelectionModel().getSelectedIndex();
		ObservableList<Task> initialList;

		if (index == 0)
			initialList = modelHandler.getPendingList();
		else if (index == 1)
			initialList = modelHandler.getCompleteList();
		else
			initialList = modelHandler.getTrashList();

		ObservableList<Task> searchList = FXCollections.observableArrayList();

		boolean isFirstTimeSearch = true;

		if (splittedUserCommand[0] != Parser.NULL) {
			String workInfo = splittedUserCommand[0];
			searchList = searchWorkInfo((isFirstTimeSearch) ? initialList
					: searchList, workInfo);
			isFirstTimeSearch = false;
		}

		if (splittedUserCommand[1] != Parser.NULL) {
			String tag = splittedUserCommand[3];
			searchList = searchTag((isFirstTimeSearch) ? initialList
					: searchList, tag);
			isFirstTimeSearch = false;
		}

		if (splittedUserCommand[2] != Parser.NULL) {
			String date = splittedUserCommand[1];
			CustomDate startDate = new CustomDate(date);
			searchList = searchStartDate((isFirstTimeSearch) ? initialList
					: searchList, startDate);
			isFirstTimeSearch = false;
		}

		if (splittedUserCommand[3] != Parser.NULL) {
			String date = splittedUserCommand[2];
			CustomDate endDate = new CustomDate(date);
			searchList = searchEndDate((isFirstTimeSearch) ? initialList
					: searchList, endDate);
			isFirstTimeSearch = false;
		}

		if (splittedUserCommand[4] == Parser.TRUE) {
			searchList = searchImportantTask((isFirstTimeSearch) ? initialList
					: searchList);
		}

		if (searchList.isEmpty())
			return MESSAGE_NO_RESULTS;

		if (index == 0) {
			modelHandler.setSearchPendingList(searchList);
			view.taskPendingList.setItems(modelHandler.getSearchPendingList());
		} else if (index == 1) {
			modelHandler.setSearchCompleteList(searchList);
			view.taskCompleteList
					.setItems(modelHandler.getSearchCompleteList());
		} else {
			modelHandler.setSearchTrashList(searchList);
			view.taskTrashList.setItems(modelHandler.getSearchTrashList());
		}
		return MESSAGE_SUCCESSFUL_SEARCH;
	}

	public static ObservableList<Task> searchImportantTask(
			ObservableList<Task> list) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getIsImportant())
				result.add(list.get(i));
		}
		return result;
	}

	public static ObservableList<Task> searchTag(ObservableList<Task> list,
			String tagName) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			String tag = list.get(i).getTag();
			if (tag.toLowerCase().contains(tagName.toLowerCase()))
				result.add(list.get(i));
		}
		return result;
	}

	public static ObservableList<Task> searchStartDate(
			ObservableList<Task> list, CustomDate date) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		if (date.getHour() != 0 || date.getMinute() != 0) {
			for (int i = 0; i < list.size(); i++) {
				CustomDate startDate = list.get(i).getStartDate();
				if (startDate != null
						&& CustomDate.compare(startDate, date) == 0)
					result.add(list.get(i));
			}
		} else {
			for (int i = 0; i < list.size(); i++) {
				CustomDate startDate = list.get(i).getStartDate();
				if (startDate != null && startDate.dateEqual(date))
					result.add(list.get(i));
			}
		}
		return result;
	}

	public static ObservableList<Task> searchEndDate(ObservableList<Task> list,
			CustomDate date) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		if (date.getHour() == 0 && date.getMinute() == 0) {
			date.setHour(23);
			date.setMinute(59);
		}

		if (date.getHour() != 23 && date.getMinute() != 59) {
			for (int i = 0; i < list.size(); i++) {
				CustomDate endDate = list.get(i).getEndDate();
				if (endDate != null && CustomDate.compare(endDate, date) == 0)
					result.add(list.get(i));
			}
		} else {
			for (int i = 0; i < list.size(); i++) {
				CustomDate endDate = list.get(i).getEndDate();
				if (endDate != null && endDate.dateEqual(date))
					result.add(list.get(i));
			}
		}
		return result;
	}

	public static ObservableList<Task> searchWorkInfo(
			ObservableList<Task> list, String workInfo) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getWorkInfo().toLowerCase()
					.contains(workInfo.toLowerCase()))
				result.add(list.get(i));
		}
		return result;
	}

	private static void sortList(ObservableList<Task> list) {
		Collections.sort(list);
	}
	
	public static String executeClearAllCommand(String[] clearCommand){
		String clearMessage = "";
		
		if (clearCommand[0].equals("")){
			/** if (getTab().equals("pending"){
			 * 		modelHandler.clearPendingTasks();
			 * 		clearMessage = "pending";
			 *	} else if(getTab().equals("complete"){
			 *		modelHandler.clearCompleteTasks();
			 *		clearMessage = "complete";
			 *	} else {
			 *		modelHandler.clearTrash();
			 *		clearMessage = "trash";
			 *	}
			 */
		}
		else if (clearCommand[0].equals("all")){
			modelHandler.clearPendingTasks();
			modelHandler.clearCompleteTasks();
			modelHandler.clearTrash();
			clearMessage = "pending, complete and trash";
		}
		else if (clearCommand[0].equals("pending") || clearCommand[0].equals("p")){
			modelHandler.clearPendingTasks();
			clearMessage = "pending";
		}
		else if (clearCommand[0].equals("complete") || clearCommand[0].equals("c")){
			modelHandler.clearCompleteTasks();
			clearMessage = "complete";
		}
		else if(clearCommand[0].equals("trash") || clearCommand[0].equals("t")){
			modelHandler.clearTrash();
			clearMessage = "trash";
		}
		return String.format(MESSAGE_SUCCESSFUL_CLEAR, clearMessage);
	}

	public static String executeMarkCommand(String[] splittedUserCommand){
		int numPendingTasks = modelHandler.getPendingList().size();
		int indexCount = splittedUserCommand.length;
		String successfulMark = "", failedMark = "";

		for (int i = 0; i < indexCount; i++){
			if (i >= numPendingTasks || i < 0){
				failedMark += i + " ";			
			}
			else{
				Task targetTask = modelHandler.getTaskFromPending(i);
				targetTask.setIsImportant(true);
				successfulMark += i + " ";
			}
		}

		if (failedMark.equals(""))
			return String.format(MESSAGE_SUCCESSFUL_MARK, successfulMark);
		else if (successfulMark.equals(""))
			return String.format(MESSAGE_FAILED_MARK, failedMark);
		else
			return String.format(MESSAGE_SUCCESSFUL_MARK, successfulMark) 
					+ String.format(MESSAGE_FAILED_MARK, failedMark);
	}

	public static String executeUnmarkCommand(String[] splittedUserCommand){
		int numPendingTasks = modelHandler.getPendingList().size();
		int indexCount = splittedUserCommand.length;
		String successfulUnmark = "", failedUnmark = "";

		for (int i = 0; i < indexCount; i++){
			if (i >= numPendingTasks || i < 0){
				failedUnmark += i + " ";			
			}
			else{
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

	public static String executeCompleteCommand(String[] splittedUserCommand){
		int numPendingTasks = modelHandler.getPendingList().size();
		String successfulComplete = "", failedComplete = "";
		
		for (int i = 0; i < splittedUserCommand.length; i++){
			if (Integer.parseInt(splittedUserCommand[i]) < numPendingTasks 
					&& Integer.parseInt(splittedUserCommand[i]) >= 0){
				Task toComplete = modelHandler.getTaskFromPending(i);
				modelHandler.removeTaskFromPending(i);
				modelHandler.addTaskToComplete(toComplete);
				successfulComplete += i + " ";
			}
			else{
				failedComplete += i + " ";
			}
		}
		
		if (failedComplete.equals(""))
			return String.format(MESSAGE_SUCCESSFUL_COMPLETE, successfulComplete);
		else if (successfulComplete.equals(""))
			return String.format(MESSAGE_FAILED_COMPLETE, failedComplete);
		else
			return String.format(MESSAGE_SUCCESSFUL_COMPLETE, successfulComplete)
					+ String.format(MESSAGE_FAILED_COMPLETE, failedComplete);
	}
	
	public static String executeIncompleteCommand(String[] splittedUserCommand){
		int numCompleteTasks = modelHandler.getCompleteList().size();
		String successfulIncomplete = "", failedIncomplete = "";
		
		for (int i = 0; i < splittedUserCommand.length; i++){
			if (Integer.parseInt(splittedUserCommand[i]) < numCompleteTasks 
					&& Integer.parseInt(splittedUserCommand[i]) >= 0){
				Task toIncomplete = modelHandler.getTaskFromComplete(i);
				modelHandler.removeTaskFromComplete(i);
				modelHandler.addTaskToPending(toIncomplete);
				// sort pending?
				successfulIncomplete += i + " ";
			}
			else{
				failedIncomplete += i + " ";
			}
		}
		
		if (successfulIncomplete.equals(""))
			return String.format(MESSAGE_SUCCESSFUL_INCOMPLETE, successfulIncomplete);
		else if (successfulIncomplete.equals(""))
			return String.format(MESSAGE_FAILED_INCOMPLETE, failedIncomplete);
		else
			return String.format(MESSAGE_SUCCESSFUL_INCOMPLETE, successfulIncomplete)
					+ String.format(MESSAGE_FAILED_INCOMPLETE, failedIncomplete);
	}
	
}
