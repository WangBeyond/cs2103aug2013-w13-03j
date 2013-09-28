import java.util.Arrays;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public abstract class Command {
	protected static final String MESSAGE_SUCCESSFUL_SHOW_ALL = "Show all the tasks";
	protected static final String MESSAGE_SUCCESSFUL_CLEAR_ALL = "Clear all the tasks";
	protected static final String MESSAGE_SUCCESSFUL_SEARCH = "Successful Search!";
	protected static final String MESSAGE_NO_RESULTS = "Search no results!";
	protected static final String MESSAGE_SUCCESSFUL_ADD = "One task has been added successfully";
	protected static final String MESSAGE_SUCCESSFUL_EDIT = "Indicated task has been edited successfully";
	protected static final String MESSAGE_SUCCESFUL_REMOVE = "Indicated tasks has/have been removed";
	protected static final String MESSAGE_INVALID_DATE_RANGE = "Invalid date range as start date is after end date";
	protected static final String MESSAGE_DUPLICATE_INDEXES = "There are duplicate indexes";
	protected static final String MESSAGE_INDEX_OUT_OF_BOUNDS = "There is an index outside the range of the list";
	protected static final String MESSAGE_SUCCESSFUL_MARK = "Indicated task(s) has/have been marked successfully.";
	protected static final String MESSAGE_SUCCESSFUL_UNMARK = "Indicated task(s) has/have been unmarked successfully.";
	protected static final String MESSAGE_SUCCESSFUL_COMPLETE = "Indicated task(s) has/have been marked as complete.";
	protected static final String MESSAGE_SUCCESSFUL_INCOMPLETE = "Indicated task(s) has/have been marked as incomplete.";
	protected static final String MESSAGE_SUCCESSFUL_UNDO = "Undo is successful.";
	protected static final String MESSAGE_FAILED_UNDO = "Undo has failed.";

	protected Model model;
	protected View view;

	public abstract String execute();
}

abstract class TwoWayCommand extends Command {
	public abstract String undo(Model m, View v);
	// public abstract String[] getInfoForOppositeCommand();
}

class AddCommand extends TwoWayCommand {
	String workInfo;
	String tag;
	String startDateString;
	String endDateString;
	boolean isImptTask;
	String repeatingType;
	Task task;
	int index;

	public AddCommand(String[] parsedUserCommand, Model model, View view) {
		workInfo = parsedUserCommand[0];
		tag = parsedUserCommand[1];
		startDateString = parsedUserCommand[2];
		endDateString = parsedUserCommand[3];
		isImptTask = false;
		repeatingType = parsedUserCommand[5];
		this.model = model;
		this.view = view;
		// index = new String[1];

		if (parsedUserCommand[4].equals(Parser.TRUE)) {
			isImptTask = true;
		}
	}

	public String execute() {
		task = new Task();
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
		if (!repeatingType.equals(Parser.NULL))
			task.updateDate();
		model.addTaskToPending(task);
		Control.sortList(model.getPendingList());

		return MESSAGE_SUCCESSFUL_ADD;
	}

	public String undo(Model model, View view) {
		this.model = model;
		this.view = view;
		index = model.getIndexFromPending(task);
		model.removeTaskFromPendingNoTrash(index);
		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

class EditCommand extends TwoWayCommand {
	int index;
	int tabIndex;
	String workInfo;
	String tag;
	String startDateString;
	String endDateString;
	boolean hasImptTaskToggle;
	String repeatingType;
	ObservableList<Task> modifiedList;
	Task targetTask;
	Task originalTask;

	public EditCommand(String[] parsedUserCommand, Model model, View view) {
		tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
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

		targetTask = modifiedList.get(index - 1);
		originalTask = targetTask;

		CustomDate startDate, endDate;
		startDate = endDate = null;

		if (repeatingType.equals(Parser.NULL) && workInfo.equals(Parser.NULL))
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

		if (!repeatingType.equals(Parser.NULL))
			targetTask.updateDate();
		if (repeatingType.equals(Parser.NULL))
			repeatingType = "-";

		if (tag != Parser.NULL)
			targetTask.setTag(new Tag(tag, repeatingType));
		else
			targetTask.setTag(new Tag(targetTask.getTag().getTag(),
					repeatingType));

		if (hasImptTaskToggle)
			targetTask.setIsImportant(!targetTask.getIsImportant());

		Control.sortList(modifiedList);

		return MESSAGE_SUCCESSFUL_EDIT;
	}

	//TODO: undo does not work
	public String undo(Model model, View view) {
		this.model = model;
		this.view = view;
		
		if (tabIndex == 0){
			index = model.getIndexFromPending(targetTask);
			model.removeTaskFromPendingNoTrash(index);
			model.addTaskToPending(originalTask);
			Control.sortList(model.getPendingList());
			return String.valueOf(index);
		}
		else if (tabIndex == 1){
			index = model.getIndexFromComplete(targetTask);
			model.removeTaskFromCompleteNoTrash(index);
			model.addTaskToComplete(originalTask);
			Control.sortList(model.getCompleteList());
			return "complete";
		}
		else{
			index = model.getIndexFromTrash(targetTask);
			model.removeTaskFromTrash(index);
			model.addTaskToTrash(originalTask);
			Control.sortList(model.getTrashList());
			return "trash";
		}
//		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

class RemoveCommand extends TwoWayCommand {
	ObservableList<Task> modifiedList;
	int[] indexList;
	int tabIndex;
	int indexCount;
	ArrayList<Task> removedTaskInfo;

	public RemoveCommand(String[] userParsedCommand, Model model, View view) {
		removedTaskInfo = new ArrayList<Task>();
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

		indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		indexCount = indexList.length;

		for (int i = 0; i < indexCount - 1; i++)
			if (indexList[i] == indexList[i + 1])
				return MESSAGE_DUPLICATE_INDEXES;

		if (indexList[indexCount - 1] > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		for (int i = indexCount - 1; i >= 0; i--) {
			removedTaskInfo.add(model.getTaskFromPending(indexList[i] - 1));
			model.removeTask(indexList[i] - 1, tabIndex);
		}

		if (tabIndex == 1 || tabIndex == 0)
			Control.sortList(model.getTrashList());

		return MESSAGE_SUCCESFUL_REMOVE;
	}

	public String undo(Model model, View view) {
		this.model = model;
		this.view = view;
		int index;

		if (tabIndex == 0)
			modifiedList = model.getPendingList();
		else if (tabIndex == 1)
			modifiedList = model.getCompleteList();
		else
			modifiedList = model.getTrashList();

		for (int i = 0; i < removedTaskInfo.size(); i++) {
			modifiedList.add(removedTaskInfo.get(i));
			if (tabIndex == 0 || tabIndex == 1) {
				index = model.getIndexFromTrash(removedTaskInfo.get(i));
				model.removeTaskFromTrash(index);
			}
		}
		Control.sortList(modifiedList);
		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

class ClearAllCommand extends TwoWayCommand {
	int tabIndex;
	Task[] clearedTasks;
	Task[] originalTrashTasks;
	
	public ClearAllCommand(Model model, View view) {
		this.model = model;
		this.view = view;
	}

	public String execute() {
		tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
		ObservableList<Task> modifiedList = FXCollections.observableArrayList();
		
		originalTrashTasks = new Task[model.getTrashList().size()];
		for (int i = 0; i < model.getTrashList().size(); i++)
			originalTrashTasks[i] = model.getTaskFromTrash(i);
		
		if (tabIndex == 0)
			modifiedList = model.getPendingList();
		else if (tabIndex == 1)
			modifiedList = model.getCompleteList();
		else
			modifiedList = model.getTrashList();

		clearedTasks = new Task[modifiedList.size()];
		for (int i = modifiedList.size() - 1; i >= 0; i--){
			if (tabIndex == 0)
				clearedTasks[i] = model.getTaskFromPending(i);
			else if (tabIndex == 1)
				clearedTasks[i] = model.getTaskFromComplete(i);
			model.removeTask(i, tabIndex);
		}
		
		if (tabIndex == 1 || tabIndex == 0)
			Control.sortList(model.getTrashList());

		return MESSAGE_SUCCESSFUL_CLEAR_ALL;
	}

	public String undo(Model model, View view) {
		this.model = model;
		this.view = view;
		
		if (tabIndex == 0){
			for (int i = 0; i < clearedTasks.length; i++)
				model.addTaskToPending(clearedTasks[i]);
			Control.sortList(model.getPendingList());
		}
		else if (tabIndex == 1){
			for (int i = 0; i < clearedTasks.length; i++)
				model.addTaskToComplete(clearedTasks[i]);
			Control.sortList(model.getCompleteList());
		}
		model.getTrashList().clear();
		for (int i = 0; i < originalTrashTasks.length; i++)
			model.addTaskToTrash(originalTrashTasks[i]);
		Control.sortList(model.getTrashList());
		
		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

class CompleteCommand extends TwoWayCommand {
	ObservableList<Task> modifiedList;
	Task[] toCompleteTasks;
	int[] indexList;
	int[] indexInCompleteList;
	int indexCount;

	public CompleteCommand(String[] userParsedCommand, Model model, View view) {
		this.model = model;
		this.view = view;
		modifiedList = this.model.getPendingList();

		indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		for (int i = 0; i < indexCount - 1; i++)
			if (indexList[i] == indexList[i + 1])
				return MESSAGE_DUPLICATE_INDEXES;

		if (indexList[indexCount - 1] > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		indexInCompleteList = new int[indexCount];
		toCompleteTasks = new Task[indexCount];

		for (int i = indexCount - 1; i >= 0; i--) {
			Task toComplete = model.getTaskFromPending(indexList[i] - 1);
			toCompleteTasks[i] = toComplete;
			model.getPendingList().remove(indexList[i] - 1);
			model.addTaskToComplete(toComplete);
		}
		Control.sortList(model.getCompleteList());

		for (int i = 0; i < indexCount; i++) {
			indexInCompleteList[i] = model.getIndexFromComplete(toCompleteTasks[i]);
		}
		Arrays.sort(indexInCompleteList);
		
		return MESSAGE_SUCCESSFUL_COMPLETE;
	}

	public String undo(Model model, View view) {
		this.model = model;
		this.view = view;

		for (int i = indexCount - 1; i >= 0; i--) {
			Task toPending = model.getTaskFromComplete(indexInCompleteList[i]);
			model.removeTaskFromCompleteNoTrash(indexInCompleteList[i]);
			model.addTaskToPending(toPending);
		}
		Control.sortList(model.getPendingList());

		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

class IncompleteCommand extends TwoWayCommand {
	ObservableList<Task> modifiedList;
	int[] indexList;
	Task[] toIncompleteTasks;
	int[] indexInIncompleteList;
	int indexCount;

	public IncompleteCommand(String[] userParsedCommand, Model model, View view) {
		this.model = model;
		this.view = view;
		modifiedList = this.model.getCompleteList();

		indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		indexCount = indexList.length;
		for (int i = 0; i < indexCount - 1; i++)
			if (indexList[i] == indexList[i + 1])
				return MESSAGE_DUPLICATE_INDEXES;

		if (indexList[indexCount - 1] > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;
		
		indexInIncompleteList = new int[indexCount];
		toIncompleteTasks = new Task[indexCount];

		for (int i = indexCount - 1; i >= 0; i--) {
			Task toPending = model.getTaskFromComplete(indexList[i] - 1);
			toIncompleteTasks[i] = toPending;
			model.getCompleteList().remove(indexList[i] - 1);
			model.addTaskToPending(toPending);
		}
		Control.sortList(model.getPendingList());
		
		for (int i = 0; i < indexCount; i++) {
			indexInIncompleteList[i] = model.getIndexFromComplete(toIncompleteTasks[i]);
		}
		Arrays.sort(indexInIncompleteList);

		return MESSAGE_SUCCESSFUL_INCOMPLETE;
	}

	public String undo(Model model, View view) {
		this.model = model;
		this.view = view;

		for (int i = indexCount - 1; i >= 0; i--) {
			Task toComplete = model.getTaskFromPending(indexList[i] - 1);
			toIncompleteTasks[i] = toComplete;
			model.getPendingList().remove(indexList[i] - 1);
			model.addTaskToComplete(toComplete);
		}
		Control.sortList(model.getCompleteList());
		
		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

class MarkCommand extends TwoWayCommand {
	ObservableList<Task> modifiedList;
	int[] indexList;
	int tabIndex;
	int indexCount;

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
		indexCount = indexList.length;
		for (int i = 0; i < indexCount - 1; i++)
			if (indexList[i] == indexList[i + 1])
				return MESSAGE_DUPLICATE_INDEXES;

		if (indexList[indexCount - 1] > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		for (int i = 0; i < indexCount; i++) {
			Task targetTask = modifiedList.get(indexList[i] - 1);
			targetTask.setIsImportant(true);
		}

		return MESSAGE_SUCCESSFUL_MARK;
	}

	public String undo(Model model, View view) {
		this.model = model;
		this.view = view;
		
		if (tabIndex == 0)
			modifiedList = this.model.getPendingList();
		else if (tabIndex == 1)
			modifiedList = this.model.getCompleteList();
		else
			modifiedList = this.model.getTrashList();
		
		for (int i = 0; i < indexCount; i++) {
			Task targetTask = modifiedList.get(indexList[i] - 1);
			targetTask.setIsImportant(false);
		}
		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

class UnmarkCommand extends TwoWayCommand {
	ObservableList<Task> modifiedList;
	int[] indexList;
	int tabIndex;
	int indexCount;

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
		indexCount = indexList.length;
		for (int i = 0; i < indexCount - 1; i++)
			if (indexList[i] == indexList[i + 1])
				return MESSAGE_DUPLICATE_INDEXES;

		if (indexList[indexCount - 1] > modifiedList.size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		for (int i = 0; i < indexCount; i++) {
			Task targetTask = modifiedList.get(indexList[i] - 1);
			targetTask.setIsImportant(false);
		}

		return MESSAGE_SUCCESSFUL_UNMARK;
	}

	public String undo(Model model, View view) {
		this.model = model;
		this.view = view;
		
		if (tabIndex == 0)
			modifiedList = this.model.getPendingList();
		else if (tabIndex == 1)
			modifiedList = this.model.getCompleteList();
		else
			modifiedList = this.model.getTrashList();
		
		for (int i = 0; i < indexCount; i++) {
			Task targetTask = modifiedList.get(indexList[i] - 1);
			targetTask.setIsImportant(true);
		}
		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

class SearchCommand extends Command {
	String workInfo;
	String tag;
	String startDateString;
	String endDateString;
	String repeatingType;
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
		repeatingType = parsedUserCommand[5];
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

		if (!repeatingType.equals(Parser.NULL)) {
			searchList = searchRepeatingType((isFirstTimeSearch) ? initialList
					: searchList, repeatingType);
			isFirstTimeSearch = false;
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
	
	public static ObservableList<Task> searchRepeatingType(ObservableList<Task> list, String repeatingType){
		ObservableList<Task> result = FXCollections.observableArrayList();
		for(int i = 0; i < list.size(); i++){
			String repetition = list.get(i).getTag().getRepetition();
			if(repetition.equalsIgnoreCase(repeatingType))
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
