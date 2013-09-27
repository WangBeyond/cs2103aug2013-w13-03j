import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public abstract class Command {
	protected static final String MESSAGE_SUCCESSFUL_SHOW_ALL = "Show all the tasks";
	protected static final String MESSAGE_SUCCESSFUL_CLEAR_ALL = "Clear all the tasks";
	protected static final String MESSAGE_SUCCESSFUL_SEARCH = "Successful Search!";
	protected static final String MESSAGE_NO_RESULTS = "Search no results!";
	protected static final String MESSAGE_SUCCESSFUL_ADD = "Task is added successfully";
	protected static final String MESSAGE_SUCCESSFUL_EDIT = "Task is edited successfully";
	protected static final String MESSAGE_SUCCESFUL_REMOVE = "Indicated tasks are removed";
	protected static final String MESSAGE_INVALID_DATE_RANGE = "Invalid date range as start date is after end date";
	protected static final String MESSAGE_DUPLICATE_INDEXES = "There are duplicate indexes";
	protected static final String MESSAGE_INDEX_OUT_OF_BOUNDS = "There is an index outside the range of the list";
	protected static final String MESSAGE_SUCCESSFUL_MARK = "Task(s) %1$shas/have been marked successfully.";
	protected static final String MESSAGE_SUCCESSFUL_UNMARK = "Task(s) %1$shas/have been unmarked successfully.";
	protected static final String MESSAGE_SUCCESSFUL_COMPLETE = "Task(s) %1$shas/have been marked as complete.";
	protected static final String MESSAGE_SUCCESSFUL_INCOMPLETE = "Task(s) %1$shas/have been marked as incomplete.";

	protected Model model;
	protected View view;

	public abstract String execute();
}

abstract class TwoWayCommand extends Command {
	public abstract String undo();
}

class AddCommand extends TwoWayCommand {
	String workInfo;
	String tag;
	String startDateString;
	String endDateString;
	boolean isImptTask;
	String repeatingType;

	public AddCommand(String[] parsedUserCommand, Model model, View view) {
		workInfo = parsedUserCommand[0];
		tag = parsedUserCommand[1];
		startDateString = parsedUserCommand[2];
		endDateString = parsedUserCommand[3];
		isImptTask = false;
		repeatingType = parsedUserCommand[5];
		this.model = model;
		this.view = view;

		if (parsedUserCommand[4].equals(Parser.TRUE)) {
			isImptTask = true;
		}
	}

	public String execute() {
		Task task = new Task();

		task.setWorkInfo(workInfo);

		if (!repeatingType.equals(Parser.NULL)
				&& (startDateString.equals(Parser.NULL) || endDateString
						.equals(Parser.NULL)))
			throw new IllegalArgumentException(
					"There must be both start and end dates for repetitive task");

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

		if (!repeatingType.equals(Parser.NULL)) {
			long expectedDifference = Task.getUpdateDifference(repeatingType);
			long actualDifference = task.getEndDate().getTimeInMillis()
					- task.getStartDate().getTimeInMillis();
			if (actualDifference > expectedDifference)
				throw new IllegalArgumentException(
						"Invalid command: The difference between times is larger than the limit of repetitive period");
		}

		task.setTag(new Tag(tag.equals(Parser.NULL) ? "-" : tag, repeatingType
				.equals(Parser.NULL) ? "-" : repeatingType));
		task.setIsImportant(isImptTask);

		model.addTaskToPending(task);

		Control.sortList(model.getPendingList());

		return MESSAGE_SUCCESSFUL_ADD;
	}

	public String undo() {
		return null;
	}
}

class EditCommand extends TwoWayCommand {
	int index;
	String workInfo;
	String tag;
	String startDateString;
	String endDateString;
	boolean hasImptTaskToggle;
	String repeatingType;
	ObservableList<Task> modifiedList;

	public EditCommand(String[] parsedUserCommand, Model model, View view) {
		int tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
		modifiedList = FXCollections.observableArrayList();
		this.model = model;
		this.view = view;
		if (tabIndex == 0)
			modifiedList = this.model.getPendingList();
		else if (tabIndex == 1)
			modifiedList = this.model.getCompleteList();
		else
			modifiedList = this.model.getTrashList();
		workInfo = parsedUserCommand[1];
		tag = parsedUserCommand[2];
		startDateString = parsedUserCommand[3];
		endDateString = parsedUserCommand[4];
		hasImptTaskToggle = (parsedUserCommand[5].equals(Parser.TRUE)) ? true
				: false;
		repeatingType = parsedUserCommand[6];
		index = Integer.parseInt(parsedUserCommand[0]);
	}

	public String execute() {
		if (index > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		Task targetTask = modifiedList.get(index - 1);

		CustomDate startDate, endDate;
		startDate = endDate = null;
		
		if(repeatingType.equals(Parser.NULL))
			repeatingType = targetTask.getTag().getRepetition();
		
		if (!startDateString.equals(Parser.NULL)) {
			startDate = new CustomDate(startDateString);
		} else
			startDate = targetTask.getStartDate();

		if (!endDateString.equals(Parser.NULL)) {
			endDate = new CustomDate(endDateString);
			if (endDate.getHour() == 0 && endDate.getMinute() == 0) {
				endDate.setHour(23);
				endDate.setMinute(59);
			}
		} else
			endDate = targetTask.getEndDate();

		if (startDate != null && endDate != null
				&& CustomDate.compare(endDate, startDate) < 0)
			return MESSAGE_INVALID_DATE_RANGE;

		if (!repeatingType.equals(Parser.NULL)
				&& (startDate == null || endDate == null))
			throw new IllegalArgumentException(
					"There must be both start and end dates for repetitive task");

		if (!repeatingType.equals(Parser.NULL)) {
			long expectedDifference = Task.getUpdateDifference(repeatingType);
			long actualDifference = endDate.getTimeInMillis()
					- startDate.getTimeInMillis();
			if (actualDifference > expectedDifference)
				throw new IllegalArgumentException(
						"Invalid command: The difference between times is larger than the limit of repetitive period");
		}

		if (!workInfo.equals(Parser.NULL))
			targetTask.setWorkInfo(workInfo);

		if (startDate != null)
			targetTask.setStartDate(startDate);

		if (endDate != null)
			targetTask.setEndDate(endDate);

		if(tag != Parser.NULL)
			targetTask.setTag(new Tag(tag, repeatingType));
		else
			targetTask.setTag(new Tag(targetTask.getTag().getTag(), repeatingType));
		
		if (hasImptTaskToggle)
			targetTask.setIsImportant(!targetTask.getIsImportant());

		Control.sortList(modifiedList);
		return MESSAGE_SUCCESSFUL_EDIT;
	}

	public String undo() {
		return null;
	}
}

class RemoveCommand extends TwoWayCommand {
	ObservableList<Task> modifiedList;
	int[] indexList;
	int tabIndex;

	public RemoveCommand(String[] userParsedCommand, Model model, View view) {
		this.model = model;
		this.view = view;
		tabIndex = this.view.tabPane.getSelectionModel().getSelectedIndex();
		modifiedList = FXCollections.observableArrayList();
		if (tabIndex == 0)
			modifiedList = this.model.getPendingList();
		else if (tabIndex == 1)
			modifiedList = this.model.getCompleteList();
		else
			modifiedList = this.model.getTrashList();

		int indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		int indexCount = indexList.length;
		for (int i = 0; i < indexCount - 1; i++)
			if (indexList[i] == indexList[i + 1])
				return MESSAGE_DUPLICATE_INDEXES;

		if (indexList[indexCount - 1] > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		for (int i = indexCount - 1; i >= 0; i--) {
			model.removeTask(indexList[i] - 1, tabIndex);
		}

		if (tabIndex == 1 || tabIndex == 0)
			Control.sortList(model.getTrashList());

		return MESSAGE_SUCCESFUL_REMOVE;
	}

	public String undo() {
		return null;
	}
}

class ClearAllCommand extends TwoWayCommand {
	public ClearAllCommand(Model model, View view) {
		this.model = model;
		this.view = view;
	}

	public String execute() {
		int tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
		ObservableList<Task> modifiedList = FXCollections.observableArrayList();
		if (tabIndex == 0)
			modifiedList = model.getPendingList();
		else if (tabIndex == 1)
			modifiedList = model.getCompleteList();
		else
			modifiedList = model.getTrashList();

		for (int i = modifiedList.size() - 1; i >= 0; i--)
			model.removeTask(i, tabIndex);

		if (tabIndex == 1 || tabIndex == 0)
			Control.sortList(model.getTrashList());

		return MESSAGE_SUCCESSFUL_CLEAR_ALL;
	}

	public String undo() {
		return null;
	}
}

class CompleteCommand extends TwoWayCommand {
	ObservableList<Task> modifiedList;
	int[] indexList;
	int tabIndex;

	public CompleteCommand(String[] userParsedCommand, Model model, View view) {
		this.model = model;
		this.view = view;
		modifiedList = this.model.getPendingList();

		int indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		int indexCount = indexList.length;
		for (int i = 0; i < indexCount - 1; i++)
			if (indexList[i] == indexList[i + 1])
				return MESSAGE_DUPLICATE_INDEXES;

		if (indexList[indexCount - 1] > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		String successfulComplete = "";

		for (int i = indexCount - 1; i >= 0; i--) {
			Task toComplete = model.getTaskFromPending(indexList[i] - 1);
			model.getPendingList().remove(indexList[i] - 1);
			model.addTaskToComplete(toComplete);
			successfulComplete += i + " ";
		}

		Control.sortList(model.getCompleteList());

		return String.format(MESSAGE_SUCCESSFUL_COMPLETE, successfulComplete);
	}

	public String undo() {
		return null;
	}
}

class IncompleteCommand extends TwoWayCommand {
	ObservableList<Task> modifiedList;
	int[] indexList;
	int tabIndex;

	public IncompleteCommand(String[] userParsedCommand, Model model, View view) {
		this.model = model;
		this.view = view;
		modifiedList = this.model.getCompleteList();

		int indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		int indexCount = indexList.length;
		for (int i = 0; i < indexCount - 1; i++)
			if (indexList[i] == indexList[i + 1])
				return MESSAGE_DUPLICATE_INDEXES;

		if (indexList[indexCount - 1] > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		String successfulIncomplete = "";

		for (int i = indexCount - 1; i >= 0; i--) {
			Task toPending = model.getTaskFromComplete(indexList[i] - 1);
			model.getCompleteList().remove(indexList[i] - 1);
			model.addTaskToPending(toPending);
			successfulIncomplete += i + " ";
		}

		Control.sortList(model.getPendingList());

		return String.format(MESSAGE_SUCCESSFUL_INCOMPLETE,
				successfulIncomplete);
	}

	public String undo() {
		return null;
	}
}

class MarkCommand extends TwoWayCommand {
	ObservableList<Task> modifiedList;
	int[] indexList;
	int tabIndex;

	public MarkCommand(String[] userParsedCommand, Model model, View view) {
		this.model = model;
		this.view = view;
		tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
		modifiedList = FXCollections.observableArrayList();
		if (tabIndex == 0)
			modifiedList = this.model.getPendingList();
		else if (tabIndex == 1)
			modifiedList = this.model.getCompleteList();
		else
			modifiedList = this.model.getTrashList();

		int indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		int indexCount = indexList.length;
		for (int i = 0; i < indexCount - 1; i++)
			if (indexList[i] == indexList[i + 1])
				return MESSAGE_DUPLICATE_INDEXES;

		if (indexList[indexCount - 1] > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		String successfulMark = "";

		for (int i = 0; i < indexCount; i++) {
			Task targetTask = modifiedList.get(indexList[i] - 1);
			targetTask.setIsImportant(true);
			successfulMark += indexList[i] + " ";
		}

		return String.format(MESSAGE_SUCCESSFUL_MARK, successfulMark);
	}

	public String undo() {
		return null;
	}
}

class UnmarkCommand extends TwoWayCommand {
	ObservableList<Task> modifiedList;
	int[] indexList;
	int tabIndex;

	public UnmarkCommand(String[] userParsedCommand, Model model, View view) {
		this.model = model;
		this.view = view;
		tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
		modifiedList = FXCollections.observableArrayList();
		if (tabIndex == 0)
			modifiedList = this.model.getPendingList();
		else if (tabIndex == 1)
			modifiedList = this.model.getCompleteList();
		else
			modifiedList = this.model.getTrashList();

		int indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		int indexCount = indexList.length;
		for (int i = 0; i < indexCount - 1; i++)
			if (indexList[i] == indexList[i + 1])
				return MESSAGE_DUPLICATE_INDEXES;

		if (indexList[indexCount - 1] > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		String successfulUnmark = "";

		for (int i = 0; i < indexCount; i++) {
			Task targetTask = modifiedList.get(indexList[i] - 1);
			targetTask.setIsImportant(false);
			successfulUnmark += indexList[i] + " ";
		}

		return String.format(MESSAGE_SUCCESSFUL_UNMARK, successfulUnmark);
	}

	public String undo() {
		return null;
	}
}

class SearchCommand extends Command {
	String workInfo;
	String tag;
	String startDateString;
	String endDateString;
	String isImpt;
	int tabIndex;

	public SearchCommand(String[] parsedUserCommand, Model model, View view) {
		this.model = model;
		this.view = view;
		tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
		workInfo = parsedUserCommand[0];
		tag = parsedUserCommand[1];
		startDateString = parsedUserCommand[2];
		endDateString = parsedUserCommand[3];
		isImpt = parsedUserCommand[4];
	}

	public String execute() {
		ObservableList<Task> initialList;
		if (tabIndex == 0)
			initialList = model.getPendingList();
		else if (tabIndex == 1)
			initialList = model.getCompleteList();
		else
			initialList = model.getTrashList();

		ObservableList<Task> searchList = FXCollections.observableArrayList();

		boolean isFirstTimeSearch = true;

		if (!workInfo.equals(Parser.NULL)) {
			searchList = searchWorkInfo((isFirstTimeSearch) ? initialList
					: searchList, workInfo);
			isFirstTimeSearch = false;
		}

		if (!tag.equals(Parser.NULL)) {
			searchList = searchTag((isFirstTimeSearch) ? initialList
					: searchList, tag);
			isFirstTimeSearch = false;
		}

		if (!startDateString.equals(Parser.NULL)) {
			CustomDate startDate = new CustomDate(startDateString);
			searchList = searchStartDate((isFirstTimeSearch) ? initialList
					: searchList, startDate);
			isFirstTimeSearch = false;
		}

		if (!endDateString.equals(Parser.NULL)) {
			CustomDate endDate = new CustomDate(endDateString);
			searchList = searchEndDate((isFirstTimeSearch) ? initialList
					: searchList, endDate);
			isFirstTimeSearch = false;
		}

		if (isImpt.equals(Parser.TRUE)) {
			searchList = searchImportantTask((isFirstTimeSearch) ? initialList
					: searchList);
		}

		if (searchList.isEmpty())
			return MESSAGE_NO_RESULTS;

		if (tabIndex == 0) {
			model.setSearchPendingList(searchList);
			view.taskPendingList.setItems(model.getSearchPendingList());
		} else if (tabIndex == 1) {
			model.setSearchCompleteList(searchList);
			view.taskCompleteList.setItems(model.getSearchCompleteList());
		} else {
			model.setSearchTrashList(searchList);
			view.taskTrashList.setItems(model.getSearchTrashList());
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
			String tag = list.get(i).getTag().getTag();
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
}

class TodayCommand extends Command {
	public TodayCommand(Model model, View view) {
		this.model = model;
		this.view = view;
	}

	public String execute() {
		Command s = new SearchCommand(new String[] { "null", "null", "null",
				"today", "false" }, model, view);
		return s.execute();
	}
}

class ShowAllCommand extends Command {
	public ShowAllCommand(Model model, View view) {
		this.model = model;
		this.view = view;
	}

	public String execute() {
		int tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
		if (tabIndex == 0)
			view.taskPendingList.setItems(model.getPendingList());
		else if (tabIndex == 1)
			view.taskCompleteList.setItems(model.getCompleteList());
		else
			view.taskTrashList.setItems(model.getTrashList());
		return MESSAGE_SUCCESSFUL_SHOW_ALL;
	}
}

class ExitCommand extends Command {
	Stage stage;

	public ExitCommand(Model model, View view, Stage primaryStage) {
		this.model = model;
		this.view = view;
		this.stage = primaryStage;
	}

	public String execute() {
		stage.close();
		System.exit(0);
		return null;
	}
}
