import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/****************************************** Abstract Class Command ***************************/
public abstract class Command {
	protected static final String MESSAGE_SUCCESSFUL_SHOW_ALL = "Show all the tasks";
	protected static final String MESSAGE_SUCCESSFUL_CLEAR_ALL = "Clear all the tasks";
	protected static final String MESSAGE_SUCCESSFUL_SEARCH = "Successful Search!";
	protected static final String MESSAGE_NO_RESULTS = "Search no results!";
	protected static final String MESSAGE_SUCCESSFUL_ADD = "One task has been added successfully";
	protected static final String MESSAGE_INVALID_START_END_DATES = "There must be both start and end dates for repetitive task";
	protected static final String MESSAGE_INVALID_TIME_REPETITIVE = "Invalid command: The difference between times is larger than the limit of repetitive period";
	protected static final String MESSAGE_SUCCESSFUL_EDIT = "Indicated task has been edited successfully";
	protected static final String MESSAGE_SUCCESSFUL_REMOVE = "Indicated tasks has/have been removed";
	protected static final String MESSAGE_INVALID_DATE_RANGE = "Invalid date range as start date is after end date";
	protected static final String MESSAGE_DUPLICATE_INDEXES = "There are duplicate indexes";
	protected static final String MESSAGE_INDEX_OUT_OF_BOUNDS = "There is an index outside the range of the list";
	protected static final String MESSAGE_SUCCESSFUL_MARK = "Indicated task(s) has/have been marked successfully.";
	protected static final String MESSAGE_SUCCESSFUL_UNMARK = "Indicated task(s) has/have been unmarked successfully.";
	protected static final String MESSAGE_SUCCESSFUL_COMPLETE = "Indicated task(s) has/have been marked as complete.";
	protected static final String MESSAGE_SUCCESSFUL_INCOMPLETE = "Indicated task(s) has/have been marked as incomplete.";
	protected static final String MESSAGE_SUCCESSFUL_UNDO = "Undo was successful.";
	protected static final String MESSAGE_WRONG_COMPLETE_TABS = "Cannot complete the tasks in this current tab.";
	protected static final String MESSAGE_WRONG_INCOMPLETE_TABS = "Cannot incomplete the tasks in this current tab.";
	protected static final String MESSAGE_HELP = "Opening the Help window...";
	protected static final String MESSAGE_SYNCING = "synchronizating...";

	protected static final String HAVING_START_DATE = "having start date";
	protected static final String HAVING_END_DATE = "having end date";
	
	protected static final String HASH_TAG = "#";
	protected static final String HYPHEN = "-";
	
	static final int PENDING_TAB = 0;
	static final int COMPLETE_TAB = 1;

	// Model containing lists of tasks to process
	protected Model model;
	// Current tab
	protected int tabIndex;

	public Command(Model model){
		this.model = model;
	}
	
	public Command(Model model, int tabIndex) {
		this.model = model;
		this.tabIndex = tabIndex;
	}

	// Abstract function executing command to be implemented in extended classes
	public abstract String execute();
	
	protected ObservableList<Task> getSearchList(int tabIndex) {
		if (tabIndex == PENDING_TAB) {
			return model.getSearchPendingList();
		} else if (tabIndex == COMPLETE_TAB) {
			return model.getSearchCompleteList();
		} else {
			return model.getSearchTrashList();
		}
	}
	
	protected void checkInvalidDates(boolean isRepetitive, boolean hasStartDate, boolean hasEndDate, CustomDate startDate, CustomDate endDate, String repeatingType){
		if (hasStartDate && hasEndDate) {
			boolean hasEndDateBeforeStartDate = CustomDate.compare(endDate, startDate) < 0;
			if (hasEndDateBeforeStartDate) {
				throw new IllegalArgumentException(MESSAGE_INVALID_DATE_RANGE);
			}
		}
		if (isRepetitive && (!hasStartDate || !hasEndDate)) {
			throw new IllegalArgumentException(MESSAGE_INVALID_START_END_DATES);
		}
		
		if (isRepetitive) {
			long expectedDifference = CustomDate
					.getUpdateDifference(repeatingType);
			long actualDifference = endDate.getTimeInMillis()
					- startDate.getTimeInMillis();
			if (actualDifference > expectedDifference) {
				throw new IllegalArgumentException(MESSAGE_INVALID_TIME_REPETITIVE);
			}
		}
	}
	
	protected void updateTimeForEndDate(CustomDate startDate, CustomDate endDate){
		if (endDate != null && endDate.getHour() == 0
				&& endDate.getMinute() == 0) {
			endDate.setHour(23);
			endDate.setMinute(59);
		}
		
		if (endDate.hasIndicatedDate() == false
				&& startDate != null) {
			endDate.setYear(startDate.getYear());
			endDate.setMonth(startDate.getMonth());
			endDate.setDate(startDate.getDate());
		}
	}
}

/********************************** Abstract class TwoWayCommand extended from class Command ***********************/
abstract class TwoWayCommand extends Command {
	protected static final boolean SEARCHED = true;
	protected static final boolean SHOWN = false;
	protected static final int INVALID = -1;

	protected static boolean listedIndexType;
	protected ObservableList<Task> modifiedList;

	public TwoWayCommand(Model model, int tabIndex) {
		super(model, tabIndex);
	}

	public abstract String undo();

	/**
	 * This function is used to set the current indexes as indexes after search
	 * or original ones.
	 * 
	 * @param type
	 *            type of indexes: SEARCH or SHOWN
	 */
	public static void setIndexType(boolean type) {
		listedIndexType = type;
	}

	/**
	 * This function is used to return the original index of a task in the
	 * modifiedList
	 * 
	 * @param prevIndex
	 *            the required index in the current list
	 * @return the original index. INVALID if the index is out of bounds.
	 */
	public int convertIndex(int prevIndex) {
		if (listedIndexType == SEARCHED) {
			return getIndexAfterSearch(prevIndex);
		} else {
			return getIndexBeforeSearch(prevIndex);
		}
	}

	private int getIndexBeforeSearch(int prevIndex) {
		boolean isOutOfBounds = prevIndex < 0
				|| prevIndex >= modifiedList.size();
		if (isOutOfBounds) {
			return INVALID;
		}
		return prevIndex;
	}

	private int getIndexAfterSearch(int prevIndex) {
		ObservableList<Task> searchList;
		TwoWayCommand.setIndexType(TwoWayCommand.SEARCHED);
		searchList = getSearchList(tabIndex);
		boolean isOutOfBounds = prevIndex < 0 || prevIndex >= searchList.size();
		if (isOutOfBounds) {
			return INVALID;
		}
		return searchList.get(prevIndex).getIndexInList();
	}
	
	protected ObservableList<Task> getModifiedList(int tabIndex){
		if (tabIndex == PENDING_TAB) {
			return model.getPendingList();
		} else if (tabIndex == COMPLETE_TAB) {
			return model.getCompleteList();
		} else {
			return model.getTrashList();
		}
	}
}

/**
 * 
 * Class AddCommand
 * 
 */
class AddCommand extends TwoWayCommand {
	private String workInfo;
	private String tag;
	private String startDateString;
	private String endDateString;
	private boolean isImptTask;
	private String repeatingType;
	private Task task;

	public AddCommand(String[] parsedUserCommand, Model model, int tabIndex) throws IllegalArgumentException {
		super(model, tabIndex);
		boolean isImportant = parsedUserCommand[4].equals(Parser.TRUE);

		workInfo = parsedUserCommand[0];
		tag = parsedUserCommand[1];
		startDateString = parsedUserCommand[2];
		endDateString = parsedUserCommand[3];
		isImptTask = isImportant ? true : false;
		repeatingType = parsedUserCommand[5];
	}

	public String execute() {
		task = new Task();
		task.setWorkInfo(workInfo);
		boolean isRepetitive = !repeatingType.equals(Parser.NULL);
		boolean hasStartDate = !startDateString.equals(Parser.NULL);
		boolean hasEndDate = !endDateString.equals(Parser.NULL);
		
		if (hasStartDate) {
			CustomDate startDate = new CustomDate(startDateString);
			task.setStartDate(startDate);
		}
		if (hasEndDate) {
			CustomDate endDate = new CustomDate(endDateString);
			updateTimeForEndDate(task.getStartDate(), endDate);
			task.setEndDate(endDate);
		}
		
		checkInvalidDates(isRepetitive, hasStartDate, hasEndDate, task.getStartDate(), task.getEndDate(), repeatingType);
		
		setTag(isRepetitive);
		if (isRepetitive) {
			task.updateDateForRepetitiveTask();
		}
		
		task.setIsImportant(isImptTask);
		
		model.addTaskToPending(task);
		Control.sortList(model.getPendingList());

		return MESSAGE_SUCCESSFUL_ADD;
	}

	public String undo() {
		int index = model.getIndexFromPending(task);
		model.removeTaskFromPendingNoTrash(index);
		Control.sortList(model.getPendingList());
		return MESSAGE_SUCCESSFUL_UNDO;
	}
	
	private void setTag(boolean isRepetitive){
		if (tag.equals(Parser.NULL) || tag.equals(HASH_TAG)) {
			if (isRepetitive) {
				task.setTag(new Tag(Parser.HYPHEN, repeatingType));
			} else {
				task.setTag(new Tag(Parser.HYPHEN, Parser.NULL));
			}
		} else {
			if (isRepetitive) {
				task.setTag(new Tag(Parser.HYPHEN, Parser.NULL));
			} else {
				task.setTag(new Tag(tag, Parser.NULL));
			}
		}
	}
}

/**
 * 
 * Class Edit Command
 * 
 */
class EditCommand extends TwoWayCommand {
	int index;
	String workInfo;
	String tag;
	String startDateString;
	String endDateString;
	boolean hasImptTaskToggle;
	String repeatingType;
	Task targetTask;
	Task originalTask;

	public EditCommand(String[] parsedUserCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		modifiedList = getModifiedList(tabIndex);
		
		index = Integer.parseInt(parsedUserCommand[0]);
		workInfo = parsedUserCommand[1];
		tag = parsedUserCommand[2];
		startDateString = parsedUserCommand[3];
		endDateString = parsedUserCommand[4];
		hasImptTaskToggle = (parsedUserCommand[5].equals(Parser.TRUE)) ? true: false;
		repeatingType = parsedUserCommand[6];
	}

	public String execute() {
		if (convertIndex(index - 1) == INVALID) {
			return MESSAGE_INDEX_OUT_OF_BOUNDS;
		}
		targetTask = modifiedList.get(convertIndex(index - 1));
		setOriginalTask();
		
		CustomDate startDate, endDate;
		startDate = endDate = null;
		boolean hasRepetitiveKey = !repeatingType.equals(Parser.NULL);
		boolean hasWorkInfoKey = !workInfo.equals(Parser.NULL);
		boolean hasStartDateKey = !startDateString.equals(Parser.NULL);
		boolean hasEndDateKey = !endDateString.equals(Parser.NULL);
		
		if (!hasRepetitiveKey && !hasWorkInfoKey ) {
			repeatingType = targetTask.getTag().getRepetition();
		}
		if (hasStartDateKey) {
			startDate = new CustomDate(startDateString);
		} else {
			startDate = targetTask.getStartDate();
		}
		if (hasEndDateKey) {
			endDate = new CustomDate(endDateString);
			updateTimeForEndDate(startDate, endDate);
		} else {
			endDate = targetTask.getEndDate();
		}
		
		boolean isRepetitive = !repeatingType.equals(Parser.NULL);
		boolean hasStartDate = startDate != null;
		boolean hasEndDate = endDate != null;
		checkInvalidDates(isRepetitive, hasStartDate, hasEndDate, startDate, endDate, repeatingType);
		
		if (hasWorkInfoKey) {
			targetTask.setWorkInfo(workInfo);
		}
		if (hasStartDate) {
			targetTask.setStartDate(startDate);
		}
		if (hasEndDate) {
			targetTask.setEndDate(endDate);
		}
		
		setTag();
		
		if (isRepetitive) {
			targetTask.updateDateForRepetitiveTask();
		}
		
		if (hasImptTaskToggle) {
			targetTask.setIsImportant(!targetTask.getIsImportant());
		}

		targetTask.updateLatestModifiedDate();
		Control.sortList(modifiedList);
		return MESSAGE_SUCCESSFUL_EDIT;
	}
	
	private void setTag(){
		if (tag != Parser.NULL) {
			targetTask.setTag(new Tag(tag, repeatingType));
			if (tag.equals(HASH_TAG))
				targetTask.getTag().setTag(HYPHEN);
		} else {
			targetTask.setTag(new Tag("-", repeatingType));
		}
	}
	
	private void setOriginalTask() {
		originalTask = new Task();
		originalTask.setIsImportant(targetTask.getIsImportant());
		originalTask.setStartDate(targetTask.getStartDate());
		originalTask.setEndDate(targetTask.getEndDate());
		originalTask.setStartDateString(targetTask.getStartDateString());
		originalTask.setEndDateString(targetTask.getEndDateString());
		originalTask.setWorkInfo(targetTask.getWorkInfo());
		originalTask.setTag(targetTask.getTag());
		originalTask.setIndexId(targetTask.getIndexId());
		originalTask.setLatestModifiedDate(targetTask.getLatestModifiedDate());
	}

	public String undo() {
		targetTask.setIsImportant(originalTask.getIsImportant());
		targetTask.setStartDate(originalTask.getStartDate());
		targetTask.setEndDate(originalTask.getEndDate());
		targetTask.setStartDateString(originalTask.getStartDateString());
		targetTask.setEndDateString(originalTask.getEndDateString());
		targetTask.setWorkInfo(originalTask.getWorkInfo());
		targetTask.setTag(originalTask.getTag());
		targetTask.setIndexId(originalTask.getIndexId());
		targetTask.setLatestModifiedDate(originalTask.getLatestModifiedDate());
		Control.sortList(modifiedList);

		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

/**
 * Class RemoveCommand
 * 
 * 
 */
class RemoveCommand extends TwoWayCommand {
	int[] indexList;
	int indexCount;
	ArrayList<Task> removedTaskInfo;

	public RemoveCommand(String[] userParsedCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		removedTaskInfo = new ArrayList<Task>();
		this.tabIndex = tabIndex;
		modifiedList = FXCollections.observableArrayList();
		if (tabIndex == PENDING_TAB) {
			modifiedList = this.model.getPendingList();
		} else if (tabIndex == COMPLETE_TAB) {
			modifiedList = this.model.getCompleteList();
		} else {
			modifiedList = this.model.getTrashList();
		}
		indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		indexCount = indexList.length;

		for (int i = 0; i < indexCount - 1; i++) {
			if (indexList[i] == indexList[i + 1]) {
				return MESSAGE_DUPLICATE_INDEXES;
			}
		}
		if (convertIndex(indexList[indexCount - 1] - 1) == INVALID
				|| convertIndex(indexList[0] - 1) == INVALID) {
			return MESSAGE_INDEX_OUT_OF_BOUNDS;
		}

		for (int i = indexCount - 1; i >= 0; i--) {
			removedTaskInfo.add(modifiedList
					.get(convertIndex(indexList[i] - 1)));
			Task removedTask = modifiedList.get(convertIndex(indexList[i] - 1));
			model.removeTask(convertIndex(indexList[i] - 1), tabIndex);
			if (tabIndex == 0
					&& removedTask.getStatus() == Task.Status.NEWLY_ADDED)
				removedTask.setStatus(Task.Status.UNCHANGED);
			else if (tabIndex == 0
					&& removedTask.getStatus() == Task.Status.UNCHANGED)
				removedTask.setStatus(Task.Status.DELETED);
		}
		if (tabIndex == PENDING_TAB) {
			Control.sortList(model.getPendingList());
		} else if (tabIndex == COMPLETE_TAB) {
			Control.sortList(model.getCompleteList());
		}
		Control.sortList(model.getTrashList());

		return MESSAGE_SUCCESSFUL_REMOVE;
	}

	public String undo() {
		int index;
		for (int i = 0; i < removedTaskInfo.size(); i++) {
			Task removedTask = removedTaskInfo.get(i);
			if (tabIndex == 0
					&& removedTask.getStatus() == Task.Status.UNCHANGED)
				removedTask.setStatus(Task.Status.NEWLY_ADDED);
			else if (tabIndex == 0
					&& removedTask.getStatus() == Task.Status.DELETED)
				removedTask.setStatus(Task.Status.UNCHANGED);
			modifiedList.add(removedTask);
			if (tabIndex == PENDING_TAB || tabIndex == COMPLETE_TAB) {
				index = model.getIndexFromTrash(removedTaskInfo.get(i));
				model.removeTask(index, 2);
			}
		}
		removedTaskInfo.clear();
		Control.sortList(modifiedList);

		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

/**
 * 
 * Class ClearAllCommand
 * 
 */
class ClearAllCommand extends TwoWayCommand {
	Task[] clearedTasks;
	Task[] originalTrashTasks;

	public ClearAllCommand(Model model, int tabIndex) {
		super(model, tabIndex);
	}

	public String execute() {
		originalTrashTasks = new Task[model.getTrashList().size()];
		for (int i = 0; i < model.getTrashList().size(); i++) {
			originalTrashTasks[i] = model.getTaskFromTrash(i);
		}
		// If the operation is after search, should delete list of tasks in the
		// searched result
		if (listedIndexType == TwoWayCommand.SEARCHED) {
			if (tabIndex == PENDING_TAB) {
				modifiedList = model.getSearchPendingList();
			} else if (tabIndex == COMPLETE_TAB) {
				modifiedList = model.getSearchCompleteList();
			} else {
				modifiedList = model.getSearchTrashList();
			}
		} else {
			if (tabIndex == PENDING_TAB) {
				modifiedList = model.getPendingList();
			} else if (tabIndex == COMPLETE_TAB) {
				modifiedList = model.getCompleteList();
			} else {
				modifiedList = model.getTrashList();
			}
		}
		clearedTasks = new Task[modifiedList.size()];
		for (int i = modifiedList.size() - 1; i >= 0; i--) {
			if (tabIndex == PENDING_TAB) {
				clearedTasks[i] = model.getTaskFromPending(convertIndex(i));
				if (clearedTasks[i].getStatus() == Task.Status.NEWLY_ADDED)
					clearedTasks[i].setStatus(Task.Status.UNCHANGED);
				else if (clearedTasks[i].getStatus() == Task.Status.UNCHANGED)
					clearedTasks[i].setStatus(Task.Status.DELETED);
			} else if (tabIndex == COMPLETE_TAB) {
				clearedTasks[i] = model.getTaskFromComplete(convertIndex(i));
			}
			model.removeTask(convertIndex(i), tabIndex);
		}
		if (tabIndex == PENDING_TAB || tabIndex == COMPLETE_TAB) {
			Control.sortList(model.getTrashList());
		}
		return MESSAGE_SUCCESSFUL_CLEAR_ALL;
	}

	public String undo() {
		if (tabIndex == PENDING_TAB) {
			for (int i = 0; i < clearedTasks.length; i++) {
				model.addTaskToPending(clearedTasks[i]);
				if (clearedTasks[i].getStatus() == Task.Status.UNCHANGED)
					clearedTasks[i].setStatus(Task.Status.NEWLY_ADDED);
				else if (clearedTasks[i].getStatus() == Task.Status.DELETED)
					clearedTasks[i].setStatus(Task.Status.UNCHANGED);
			}
			Control.sortList(model.getPendingList());
		} else if (tabIndex == COMPLETE_TAB) {
			for (int i = 0; i < clearedTasks.length; i++) {
				model.addTaskToComplete(clearedTasks[i]);
			}
			Control.sortList(model.getCompleteList());
		}
		model.getTrashList().clear();
		for (int i = 0; i < originalTrashTasks.length; i++) {
			model.addTaskToTrash(originalTrashTasks[i]);
		}
		Control.sortList(model.getTrashList());

		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

/**
 * 
 * Class CompleteCommand
 * 
 */
class CompleteCommand extends TwoWayCommand {
	Task[] toCompleteTasks;
	int[] indexList;
	int[] indexInCompleteList;
	int indexCount;

	public CompleteCommand(String[] userParsedCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		modifiedList = this.model.getPendingList();
		indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);

		if (tabIndex != PENDING_TAB) {
			return MESSAGE_WRONG_COMPLETE_TABS;
		}
		for (int i = 0; i < indexCount - 1; i++) {
			if (indexList[i] == indexList[i + 1]) {
				return MESSAGE_DUPLICATE_INDEXES;
			}
		}
		if (indexList[indexCount - 1] > modifiedList.size()
				|| indexList[0] <= 0) {
			return MESSAGE_INDEX_OUT_OF_BOUNDS;
		}

		indexInCompleteList = new int[indexCount];
		toCompleteTasks = new Task[indexCount];

		for (int i = indexCount - 1; i >= 0; i--) {
			Task toComplete = model
					.getTaskFromPending(convertIndex(indexList[i] - 1));
			toCompleteTasks[i] = toComplete;
			if (toComplete.getStatus() == Task.Status.NEWLY_ADDED)
				toComplete.setStatus(Task.Status.UNCHANGED);
			else if (toComplete.getStatus() == Task.Status.UNCHANGED)
				toComplete.setStatus(Task.Status.DELETED);
			model.getPendingList().remove(convertIndex(indexList[i] - 1));
			model.addTaskToComplete(toComplete);
		}
		Control.sortList(model.getPendingList());
		Control.sortList(model.getCompleteList());

		for (int i = 0; i < indexCount; i++) {
			indexInCompleteList[i] = model
					.getIndexFromComplete(toCompleteTasks[i]);
		}
		Arrays.sort(indexInCompleteList);

		return MESSAGE_SUCCESSFUL_COMPLETE;
	}

	public String undo() {
		for (int i = indexCount - 1; i >= 0; i--) {
			Task toPending = model.getTaskFromComplete(indexInCompleteList[i]);
			if (toPending.getStatus() == Task.Status.DELETED)
				toPending.setStatus(Task.Status.UNCHANGED);
			else if (toPending.getStatus() == Task.Status.UNCHANGED)
				toPending.setStatus(Task.Status.NEWLY_ADDED);
			model.removeTaskFromCompleteNoTrash(indexInCompleteList[i]);
			model.addTaskToPending(toPending);
		}
		Control.sortList(model.getPendingList());
		Control.sortList(model.getCompleteList());

		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

/**
 * 
 * Class IncompleteCommand
 * 
 */
class IncompleteCommand extends TwoWayCommand {
	int[] indexList;
	Task[] toIncompleteTasks;
	int[] indexInIncompleteList;
	int indexCount;

	public IncompleteCommand(String[] userParsedCommand, Model model,
			int tabIndex) {
		super(model, tabIndex);
		modifiedList = this.model.getCompleteList();
		indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);

		if (tabIndex != COMPLETE_TAB) {
			return MESSAGE_WRONG_INCOMPLETE_TABS;
		}

		indexCount = indexList.length;
		for (int i = 0; i < indexCount - 1; i++) {
			if (indexList[i] == indexList[i + 1]) {
				return MESSAGE_DUPLICATE_INDEXES;
			}
		}
		if (indexList[indexCount - 1] > modifiedList.size()
				|| indexList[0] <= 0) {
			return MESSAGE_INDEX_OUT_OF_BOUNDS;
		}
		indexInIncompleteList = new int[indexCount];
		toIncompleteTasks = new Task[indexCount];

		for (int i = indexCount - 1; i >= 0; i--) {
			Task toPending = model
					.getTaskFromComplete(convertIndex(indexList[i] - 1));
			if (toPending.getStatus() == Task.Status.DELETED)
				toPending.setStatus(Task.Status.UNCHANGED);
			else if (toPending.getStatus() == Task.Status.UNCHANGED)
				toPending.setStatus(Task.Status.NEWLY_ADDED);
			toIncompleteTasks[i] = toPending;
			model.getCompleteList().remove(convertIndex(indexList[i] - 1));
			model.addTaskToPending(toPending);
		}
		Control.sortList(model.getPendingList());
		Control.sortList(model.getCompleteList());

		for (int i = 0; i < indexCount; i++) {
			indexInIncompleteList[i] = model
					.getIndexFromPending(toIncompleteTasks[i]);
		}
		Arrays.sort(indexInIncompleteList);

		return MESSAGE_SUCCESSFUL_INCOMPLETE;
	}

	public String undo() {
		for (int i = indexCount - 1; i >= 0; i--) {
			Task toComplete = model
					.getTaskFromPending(indexInIncompleteList[i]);
			if (toComplete.getStatus() == Task.Status.NEWLY_ADDED)
				toComplete.setStatus(Task.Status.UNCHANGED);
			else if (toComplete.getStatus() == Task.Status.UNCHANGED)
				toComplete.setStatus(Task.Status.DELETED);
			toIncompleteTasks[i] = toComplete;
			model.getPendingList().remove(indexInIncompleteList[i]);
			model.addTaskToComplete(toComplete);
		}
		Control.sortList(model.getCompleteList());
		Control.sortList(model.getPendingList());

		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

/**
 * 
 * Class MarkCommand
 * 
 */
class MarkCommand extends TwoWayCommand {
	int[] indexList;
	int indexCount;

	public MarkCommand(String[] userParsedCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		modifiedList = FXCollections.observableArrayList();
		if (tabIndex == PENDING_TAB) {
			modifiedList = this.model.getPendingList();
		} else if (tabIndex == COMPLETE_TAB) {
			modifiedList = this.model.getCompleteList();
		} else {
			modifiedList = this.model.getTrashList();
		}

		int indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		indexCount = indexList.length;
		for (int i = 0; i < indexCount - 1; i++) {
			if (indexList[i] == indexList[i + 1]) {
				return MESSAGE_DUPLICATE_INDEXES;
			}
		}
		if (indexList[indexCount - 1] > modifiedList.size()
				|| indexList[0] <= 0) {
			return MESSAGE_INDEX_OUT_OF_BOUNDS;
		}
		for (int i = 0; i < indexCount; i++) {
			Task targetTask = modifiedList.get(convertIndex(indexList[i] - 1));
			targetTask.setIsImportant(true);
		}

		return MESSAGE_SUCCESSFUL_MARK;
	}

	public String undo() {
		for (int i = 0; i < indexCount; i++) {
			Task targetTask = modifiedList.get(convertIndex(indexList[i] - 1));
			targetTask.setIsImportant(false);
		}
		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

/**
 * 
 * Class UnmarkCommand
 * 
 */
class UnmarkCommand extends TwoWayCommand {
	int[] indexList;
	int indexCount;

	public UnmarkCommand(String[] userParsedCommand, Model model, int tabIndex) {
		super(model, tabIndex);

		modifiedList = FXCollections.observableArrayList();
		if (tabIndex == PENDING_TAB) {
			modifiedList = this.model.getPendingList();
		} else if (tabIndex == COMPLETE_TAB) {
			modifiedList = this.model.getCompleteList();
		} else {
			modifiedList = this.model.getTrashList();
		}

		int indexCount = userParsedCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(userParsedCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		indexCount = indexList.length;
		for (int i = 0; i < indexCount - 1; i++) {
			if (indexList[i] == indexList[i + 1]) {
				return MESSAGE_DUPLICATE_INDEXES;
			}
		}
		if (indexList[indexCount - 1] > modifiedList.size()
				|| indexList[0] <= 0) {
			return MESSAGE_INDEX_OUT_OF_BOUNDS;
		}
		for (int i = 0; i < indexCount; i++) {
			Task targetTask = modifiedList.get(convertIndex(indexList[i] - 1));
			targetTask.setIsImportant(false);
		}

		return MESSAGE_SUCCESSFUL_UNMARK;
	}

	public String undo() {
		for (int i = 0; i < indexCount; i++) {
			Task targetTask = modifiedList.get(convertIndex(indexList[i] - 1));
			targetTask.setIsImportant(true);
		}
		return MESSAGE_SUCCESSFUL_UNDO;
	}
}

/**
 * 
 * Class SearchCommand
 * 
 */
class SearchCommand extends Command {
	String workInfo;
	String tag;
	String startDateString;
	String endDateString;
	String repeatingType;
	String isImpt;
	int tabIndex;
	View view;
	ObservableList<Task> initialList;
	ObservableList<Task> searchList;
	ObservableList<Task> secondSearchList;
	CustomDate startDate, endDate;
	boolean isFirstTimeSearch;

	public SearchCommand(String[] parsedUserCommand, Model model, View view) {
		super(model, view.getTabIndex());
		this.view = view;
		workInfo = parsedUserCommand[0];
		tag = parsedUserCommand[1];
		startDateString = parsedUserCommand[2];
		endDateString = parsedUserCommand[3];
		isImpt = parsedUserCommand[4];
		repeatingType = parsedUserCommand[5];

		if (tabIndex == PENDING_TAB) {
			initialList = model.getPendingList();
		} else if (tabIndex == COMPLETE_TAB) {
			initialList = model.getCompleteList();
		} else {
			initialList = model.getTrashList();
		}

		searchList = FXCollections.observableArrayList();
		secondSearchList = FXCollections.observableArrayList();
		isFirstTimeSearch = true;
	}

	public String execute() {
		continuouslySearch();
		// copy searchList to finalSearchList
		for (int i = 0; i < searchList.size(); i++) {
			secondSearchList.add(searchList.get(i));
		}
		if (tabIndex == PENDING_TAB) {
			initialList = model.getPendingList();
		} else if (tabIndex == COMPLETE_TAB) {
			initialList = model.getCompleteList();
		} else {
			initialList = model.getTrashList();
		}
		searchList.clear();
		isFirstTimeSearch = true;
		if (searchDateKey() > 0) {
			Control.setDateKeySearched(true);
			continuouslySearch();
		} else
			Control.setDateKeySearched(false);
		searchList = mergeLists(secondSearchList, searchList);
		if (!Control.isRealTime && searchList.isEmpty()) {
			return MESSAGE_NO_RESULTS;
		}
		TwoWayCommand.setIndexType(TwoWayCommand.SEARCHED);
		if (tabIndex == PENDING_TAB) {
			model.setSearchPendingList(searchList);
			view.taskPendingList.setItems(model.getSearchPendingList());
		} else if (tabIndex == COMPLETE_TAB) {
			model.setSearchCompleteList(searchList);
			view.taskCompleteList.setItems(model.getSearchCompleteList());
		} else {
			model.setSearchTrashList(searchList);
			view.taskTrashList.setItems(model.getSearchTrashList());
		}
		return MESSAGE_SUCCESSFUL_SEARCH;
	}

	private ObservableList<Task> mergeLists(ObservableList<Task> list1,
			ObservableList<Task> list2) {
		FXCollections.sort(list1);
		FXCollections.sort(list2);
		ObservableList<Task> mergedList = FXCollections.observableArrayList();
		int index1 = 0;
		int index2 = 0;
		while (index1 < list1.size() && index2 < list2.size()) {
			if (list1.get(index1).compareTo(list2.get(index2)) > 0) {
				mergedList.add(list2.get(index2));
				index2++;
			} else if (list1.get(index1).compareTo(list2.get(index2)) < 0) {
				mergedList.add(list1.get(index1));
				index1++;
			} else if (list1.get(index1).compareTo(list2.get(index2)) == 0) {
				if (list1.get(index1).equals(list2.get(index2))) {
					mergedList.add(list1.get(index1));
					index1++;
					index2++;
				} else {
					mergedList.add(list1.get(index1));
					mergedList.add(list2.get(index2));
					index1++;
					index2++;
				}
			}
		}
		if (index1 >= list1.size()) {
			for (int i = index2; i < list2.size(); i++)
				mergedList.add(list2.get(i));
		} else {
			for (int i = index1; i < list1.size(); i++)
				mergedList.add(list1.get(i));
		}
		return mergedList;
	}

	public void continuouslySearch() {
		if (!workInfo.equals(Parser.NULL)) {
			if (isFirstTimeSearch) {
				searchList = searchWorkInfo(initialList, workInfo);
			} else {
				searchList = searchWorkInfo(searchList, workInfo);
			}
			isFirstTimeSearch = false;
		}
		if (!tag.equals(Parser.NULL)) {
			if (isFirstTimeSearch) {
				searchList = searchTag(initialList, tag);
			} else {
				searchList = searchTag(searchList, tag);
			}
			isFirstTimeSearch = false;
		}
		if (startDateString.equals(HAVING_START_DATE)
				&& endDateString.equals(HAVING_END_DATE)) {
			System.out.println("havingbothDate");
			if (isFirstTimeSearch) {
				searchList = searchHavingtDate(initialList);
			} else {
				searchList = searchHavingtDate(searchList);
			}
		} else {
			if (!startDateString.equals(Parser.NULL)) {
				if (startDateString.equals(HAVING_START_DATE)) {
					System.out.println("havingStartDate");
					if (isFirstTimeSearch) {
						searchList = searchHavingStartDate(initialList);
					} else {
						searchList = searchHavingStartDate(searchList);
					}
				} else {
					startDate = new CustomDate(startDateString);
					if (isFirstTimeSearch) {
						searchList = searchStartDate(initialList, startDate);
					} else {
						searchList = searchStartDate(searchList, startDate);
					}
				}
				isFirstTimeSearch = false;
			}

			if (!endDateString.equals(Parser.NULL)) {
				if (endDateString.equals(HAVING_END_DATE)) {
					System.out.println("havingEndDate");
					if (isFirstTimeSearch) {
						searchList = searchHavingEndDate(initialList);
					} else {
						searchList = searchHavingEndDate(searchList);
					}
				} else {
					endDate = new CustomDate(endDateString);
					if (startDate != null
							&& endDate.hasIndicatedDate() == false) {
						endDate.setYear(startDate.getYear());
						endDate.setMonth(startDate.getMonth());
						endDate.setDate(startDate.getDate());
					}
					if (isFirstTimeSearch) {
						searchList = searchEndDate(initialList, endDate);
					} else {
						searchList = searchEndDate(searchList, endDate);
					}
				}
				isFirstTimeSearch = false;
			}
		}
		if (isImpt.equals(Parser.TRUE)) {
			if (isFirstTimeSearch) {
				searchList = searchImportantTask(initialList);
			} else {
				searchList = searchImportantTask(searchList);
			}
		}
		if (!repeatingType.equals(Parser.NULL)) {
			if (isFirstTimeSearch) {
				searchList = searchRepeatingType(initialList, repeatingType);
			} else {
				searchList = searchRepeatingType(searchList, repeatingType);
			}
			isFirstTimeSearch = false;
		}
	}

	public int searchDateKey() {
		String[] splittedWorkInfo = Parser.splitBySpace(workInfo);
		int startWordNum = 0;
		int endWordNum = 0;
		int maxWordNum;
		String lastWords;
		if (splittedWorkInfo.length >= 1) {
			if (splittedWorkInfo.length == 2)
				lastWords = splittedWorkInfo[splittedWorkInfo.length - 2] + " "
						+ splittedWorkInfo[splittedWorkInfo.length - 1];
			else
				lastWords = splittedWorkInfo[splittedWorkInfo.length - 1];
			lastWords = lastWords.toLowerCase();
			if (startDateString == Parser.NULL) {
				startWordNum = doesArrayWeaklyContain(Parser.startDateKeys,
						lastWords);
				if (startWordNum > 0)
					startDateString = HAVING_START_DATE;
			}
			if (endDateString == Parser.NULL) {
				endWordNum = doesArrayWeaklyContain(Parser.endDateKeys,
						lastWords);
				if (endWordNum > 0)
					endDateString = HAVING_END_DATE;
			}
		}
		maxWordNum = startWordNum > endWordNum ? startWordNum : endWordNum;
		for (int i = 0; i < maxWordNum; i++)
			workInfo = Parser.removeLastWord(workInfo);
		return maxWordNum;
	}

	private int doesArrayWeaklyContain(String[] array, String wordInfo) {
		int wordNum = Parser.splitBySpace(wordInfo).length;
		if (wordNum == 2) {
			for (String str : array) {
				if (str.indexOf(wordInfo) == 0)
					return 2;
			}
		}
		String oneWordInfo = Parser.getLastWord(wordInfo);
		for (String str : array) {
			if (str.indexOf(oneWordInfo) == 0)
				return 1;
		}
		return 0;
	}

	public static ObservableList<Task> searchImportantTask(
			ObservableList<Task> list) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getIsImportant()) {
				result.add(list.get(i));
			}
		}
		return result;
	}

	public static ObservableList<Task> searchTag(ObservableList<Task> list,
			String tagName) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			String tag = list.get(i).getTag().getTag();
			if (tag.toLowerCase().contains(tagName.toLowerCase())) {
				result.add(list.get(i));
			}
		}
		return result;
	}

	public static ObservableList<Task> searchRepeatingType(
			ObservableList<Task> list, String repeatingType) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			String repetition = list.get(i).getTag().getRepetition();
			if (repetition.equalsIgnoreCase(repeatingType)) {
				result.add(list.get(i));
			}
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
						&& CustomDate.compare(startDate, date) == 0) {
					result.add(list.get(i));
				}
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
				if (endDate != null && CustomDate.compare(endDate, date) == 0) {
					result.add(list.get(i));
				}
			}
		} else {
			for (int i = 0; i < list.size(); i++) {
				CustomDate endDate = list.get(i).getEndDate();
				if (endDate != null && endDate.dateEqual(date)) {
					result.add(list.get(i));
				}
			}
		}
		return result;
	}

	public static ObservableList<Task> searchHavingtDate(
			ObservableList<Task> list) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getStartDate() != null
					|| list.get(i).getEndDate() != null)
				result.add(list.get(i));
		}
		return result;
	}

	public static ObservableList<Task> searchHavingStartDate(
			ObservableList<Task> list) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getStartDate() != null)
				result.add(list.get(i));
		}
		return result;
	}

	public static ObservableList<Task> searchHavingEndDate(
			ObservableList<Task> list) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getEndDate() != null)
				result.add(list.get(i));
		}
		return result;
	}

	public static ObservableList<Task> searchWorkInfo(
			ObservableList<Task> list, String workInfo) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			String searchedWorkInfo = list.get(i).getWorkInfo().toLowerCase();
			if (searchedWorkInfo.contains(workInfo.toLowerCase())) {
				result.add(list.get(i));
			}
		}
		return result;
	}
}

/**
 * 
 * Class Today Command
 * 
 */
class TodayCommand extends Command {
	View view;

	public TodayCommand(Model model, View view) {
		super(model, view.getTabIndex());
		this.view = view;
	}

	public String execute() {
		Command s = new SearchCommand(new String[] { "null", "null", "null",
				"today", "false" }, model, view);
		TwoWayCommand.setIndexType(TwoWayCommand.SHOWN);
		return s.execute();
	}
}

/**
 * 
 * Class ShowAllCommand
 * 
 */
class ShowAllCommand extends Command {
	View view;

	public ShowAllCommand(Model model, View view) {
		super(model, view.getTabIndex());
		this.view = view;
	}

	public String execute() {
		TwoWayCommand.setIndexType(TwoWayCommand.SHOWN);
		int tabIndex = view.getTabIndex();
		if (tabIndex == PENDING_TAB) {
			view.taskPendingList.setItems(model.getPendingList());
		} else if (tabIndex == COMPLETE_TAB) {
			view.taskCompleteList.setItems(model.getCompleteList());
		} else {
			view.taskTrashList.setItems(model.getTrashList());
		}
		return MESSAGE_SUCCESSFUL_SHOW_ALL;
	}
}

/**
 * 
 * Class HelpCommand
 * 
 */
class HelpCommand extends Command {
	View view;

	public HelpCommand(Model model, View view) {
		super(model, view.getTabIndex());
		this.view = view;
	}

	public String execute() {
		view.showHelpPage();
		return MESSAGE_HELP;
	}
}

class SyncCommand extends Command {
	String username = null;
	String password = null;
	
	Synchronization sync;
	
	public SyncCommand(String[] parsedUserCommand, Model model, Synchronization sync) {
		super(model);
		this.sync = sync;
		int size = parsedUserCommand.length;
		if(size == 2){
			username = parsedUserCommand[0];
			password = parsedUserCommand[1];
		}
	}

	@Override
	public String execute() {
		sync.execute();
		return MESSAGE_SYNCING;
	}
}

/**
 * 
 * Class ExitComand
 * 
 */
class ExitCommand extends Command {
	public ExitCommand(Model model, int tabIndex) {
		super(model, tabIndex);
	}

	public String execute() {
		System.exit(0);
		return null;
	}
}
