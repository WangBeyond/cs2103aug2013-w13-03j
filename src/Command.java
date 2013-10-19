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
	protected static final String MESSAGE_HELP = "Help window opened.";
	protected static final String MESSAGE_OPEN_SETTINGS_DIALOG = "Settings window opened.";
	protected static final String MESSAGE_SUCCESSFUL_SYNC = "successful synchronized.";

	protected static final String HAVING_START_DATE = "having start date";
	protected static final String HAVING_END_DATE = "having end date";
	
	protected static final String HASH_TAG = "#";
	protected static final String HYPHEN = "-";
	
	static final int PENDING_TAB = 0;
	static final int COMPLETE_TAB = 1;
	static final int TRASH_TAB = 2;

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
	
	protected boolean isPendingTab(){
		return tabIndex == PENDING_TAB;
	}
	
	protected boolean isCompleteTab(){
		return tabIndex == COMPLETE_TAB;
	}
	
	protected boolean isTrashTab(){
		return tabIndex == TRASH_TAB;
	}
	
	protected ObservableList<Task> getModifiedList(int tabIndex){
		if (isPendingTab()) {
			return model.getPendingList();
		} else if (isCompleteTab()) {
			return model.getCompleteList();
		} else {
			return model.getTrashList();
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
	
	protected boolean isSearchedResults(){
		return listedIndexType == TwoWayCommand.SEARCHED;
	}
	
	protected boolean isAllResults(){
		return listedIndexType == TwoWayCommand.SHOWN;
	}
}

abstract class IndexCommand extends TwoWayCommand{
	int[] indexList;
	int indexCount;
	
	public IndexCommand(Model model, int tabIndex){
		super(model, tabIndex);
	}
	
	protected void modifyStatus(Task modifiedTask){
		if (isPendingTab() && modifiedTask.hasNewlyAddedStatus()) {
			modifiedTask.setStatus(Task.Status.UNCHANGED);
		} else if (isPendingTab() && modifiedTask.hasUnchangedStatus()) {
			modifiedTask.setStatus(Task.Status.DELETED);
		}
	}
	
	protected void reverseStatus(Task modifiedTask){
		if (isPendingTab() && modifiedTask.hasUnchangedStatus()) {
			modifiedTask.setStatus(Task.Status.NEWLY_ADDED);
		} else if (isPendingTab() && modifiedTask.hasDeletedStatus()) {
			modifiedTask.setStatus(Task.Status.UNCHANGED);
		}
	}
	
	protected void checkValidIndexes(){
		for (int i = 0; i < indexCount - 1; i++) {
			if (indexList[i] == indexList[i + 1]) {
				throw new IllegalArgumentException(MESSAGE_DUPLICATE_INDEXES);
			}
		}
		
		int MAX_INDEX = indexCount - 1;
		int MIN_INDEX = 0;
		
		if (convertIndex(indexList[MAX_INDEX] - 1) == INVALID
				|| convertIndex(indexList[MIN_INDEX] - 1) == INVALID) {
			throw new IllegalArgumentException(MESSAGE_INDEX_OUT_OF_BOUNDS);
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
		assert parsedUserCommand != null;

		workInfo = parsedUserCommand[0];
		tag = parsedUserCommand[1];
		startDateString = parsedUserCommand[2];
		endDateString = parsedUserCommand[3];
		isImptTask =  parsedUserCommand[4].equals(Parser.TRUE);
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
		assert model.getTaskFromPending(index).equals(task);
		return MESSAGE_SUCCESSFUL_UNDO;
	}
	
	private void setTag(boolean isRepetitive){
		if (tag.equals(Parser.NULL) || tag.equals(HASH_TAG)) {
				task.setTag(new Tag(Parser.HYPHEN, repeatingType));
		} else {
				task.setTag(new Tag(tag, repeatingType));
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
		assert parsedUserCommand != null;
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
	
	private void setTag() {
		if (tag != Parser.NULL) {
			targetTask.setTag(new Tag(tag, repeatingType));
			if (tag.equals(HASH_TAG)) {
				targetTask.getTag().setTag(HYPHEN);
			}
		} else {
			targetTask.setTag(new Tag(HYPHEN, repeatingType));
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
class RemoveCommand extends IndexCommand {
	ArrayList<Task> removedTaskInfo;

	public RemoveCommand(String[] parsedUserCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		removedTaskInfo = new ArrayList<Task>();
		modifiedList = getModifiedList(tabIndex);
		indexCount = parsedUserCommand.length;
		
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(parsedUserCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);

		checkValidIndexes();
		processRemove();
		sortInvolvedLists();

		return MESSAGE_SUCCESSFUL_REMOVE;
	}
	
	private void processRemove(){
		for (int i = indexCount - 1; i >= 0; i--) {
			int removedIndex = convertIndex(indexList[i] - 1);
			Task removedTask = modifiedList.get(removedIndex);
			removedTaskInfo.add(removedTask);
			model.removeTask(removedIndex, tabIndex);
			modifyStatus(removedTask);
		}
	}
	
	private void sortInvolvedLists(){
		if (isPendingTab()) {
			Control.sortList(model.getPendingList());
		} else if (isCompleteTab()) {
			Control.sortList(model.getCompleteList());
		}
		Control.sortList(model.getTrashList());
	}
	
	public String undo() {
		for (int i = 0; i < removedTaskInfo.size(); i++) {
			Task removedTask = removedTaskInfo.get(i);
			reverseStatus(removedTask);
			modifiedList.add(removedTask);
			if (isPendingTab() || isCompleteTab()) {
				int index = model.getIndexFromTrash(removedTaskInfo.get(i));
				model.removeTask(index, TRASH_TAB);
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
class ClearAllCommand extends IndexCommand {
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
		if (isSearchedResults()) {
			modifiedList = getSearchList(tabIndex);
		} else {
			modifiedList = getModifiedList(tabIndex);
		}
		
		processClear();
		return MESSAGE_SUCCESSFUL_CLEAR_ALL;
	}
	
	private void processClear(){
		clearedTasks = new Task[modifiedList.size()];
		for (int i = modifiedList.size() - 1; i >= 0; i--) {
			if (isPendingTab()) {
				clearedTasks[i] = model.getTaskFromPending(convertIndex(i));
				modifyStatus(clearedTasks[i]);
			} else if (isCompleteTab()) {
				clearedTasks[i] = model.getTaskFromComplete(convertIndex(i));
			}
			model.removeTask(convertIndex(i), tabIndex);
		}
		if (isPendingTab() || isCompleteTab()) {
			Control.sortList(model.getTrashList());
		}
	}

	public String undo() {
		if (isPendingTab()) {
			for (int i = 0; i < clearedTasks.length; i++) {
				model.addTaskToPending(clearedTasks[i]);
				reverseStatus(clearedTasks[i]);
			}
			Control.sortList(model.getPendingList());
		} else if (isCompleteTab()) {
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
class CompleteCommand extends IndexCommand {
	Task[] toCompleteTasks;
	int[] indexInCompleteList;

	public CompleteCommand(String[] parsedUserCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		modifiedList = this.model.getPendingList();
		indexCount = parsedUserCommand.length;
		indexList = new int[indexCount];
		indexInCompleteList = new int[indexCount];
		toCompleteTasks = new Task[indexCount];
		
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(parsedUserCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		checkSuitableTab();
		checkValidIndexes();
		processComplete();
		retrieveIndexesAfterProcessing();

		return MESSAGE_SUCCESSFUL_COMPLETE;
	}
	
	private void processComplete(){
		for (int i = indexCount - 1; i >= 0; i--) {
			int completeIndex = convertIndex(indexList[i] - 1);
			Task toComplete = model.getTaskFromPending(completeIndex);
			toCompleteTasks[i] = toComplete;
			modifyStatus(toComplete);
			model.getPendingList().remove(completeIndex);
			model.addTaskToComplete(toComplete);
		}
		Control.sortList(model.getPendingList());
		Control.sortList(model.getCompleteList());
	}
	
	private void retrieveIndexesAfterProcessing(){
		for (int i = 0; i < indexCount; i++) {
			indexInCompleteList[i] = model.getIndexFromComplete(toCompleteTasks[i]);
		}
		Arrays.sort(indexInCompleteList);
	}
	
	
	private void checkSuitableTab(){
		if (tabIndex != PENDING_TAB) {
			throw new IllegalArgumentException(MESSAGE_WRONG_COMPLETE_TABS);
		}
	}

	public String undo() {
		for (int i = indexCount - 1; i >= 0; i--) {
			Task toPending = model.getTaskFromComplete(indexInCompleteList[i]);
			reverseStatus(toPending);
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
class IncompleteCommand extends IndexCommand {
	Task[] toIncompleteTasks;
	int[] indexInIncompleteList;


	public IncompleteCommand(String[] parsedUserCommand, Model model,
			int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		modifiedList = this.model.getCompleteList();
		indexCount = parsedUserCommand.length;
		indexInIncompleteList = new int[indexCount];
		toIncompleteTasks = new Task[indexCount];

		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(parsedUserCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		checkSuitableTab();
		checkValidIndexes();
		
		processIncomplete();
		retrieveIndexesAfterProcessing();
		
		return MESSAGE_SUCCESSFUL_INCOMPLETE;
	}
	
	private void processIncomplete(){
		for (int i = indexCount - 1; i >= 0; i--) {
			int incompleteIndex = convertIndex(indexList[i] - 1);
			Task toPending = model.getTaskFromComplete(incompleteIndex);
			reverseStatus(toPending);
			toIncompleteTasks[i] = toPending;
			model.getCompleteList().remove(incompleteIndex);
			model.addTaskToPending(toPending);
		}
		Control.sortList(model.getPendingList());
		Control.sortList(model.getCompleteList());
	}
	
	private void retrieveIndexesAfterProcessing(){
		for (int i = 0; i < indexCount; i++) {
			indexInIncompleteList[i] = model
					.getIndexFromPending(toIncompleteTasks[i]);
		}
		Arrays.sort(indexInIncompleteList);
	}
	
	private void checkSuitableTab(){
		if (tabIndex != COMPLETE_TAB) {
			throw new IllegalArgumentException(MESSAGE_WRONG_INCOMPLETE_TABS);
		}
	}

	public String undo() {
		for (int i = indexCount - 1; i >= 0; i--) {
			Task toComplete = model.getTaskFromPending(indexInIncompleteList[i]);
			modifyStatus(toComplete);
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
class MarkCommand extends IndexCommand {
	public MarkCommand(String[] parsedUserCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		modifiedList = getModifiedList(tabIndex);
		indexCount = parsedUserCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(parsedUserCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		checkValidIndexes();
		for (int i = 0; i < indexCount; i++) {
			int markIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(markIndex);
			targetTask.setIsImportant(true);
		}

		return MESSAGE_SUCCESSFUL_MARK;
	}

	public String undo() {
		for (int i = 0; i < indexCount; i++) {
			int unmarkIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(unmarkIndex);
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
class UnmarkCommand extends IndexCommand {
	public UnmarkCommand(String[] parsedUserCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		modifiedList = getModifiedList(tabIndex);
		indexCount = parsedUserCommand.length;
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(parsedUserCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		checkValidIndexes();
		for (int i = 0; i < indexCount; i++) {
			int unmarkIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(unmarkIndex);
			targetTask.setIsImportant(false);
		}

		return MESSAGE_SUCCESSFUL_UNMARK;
	}

	public String undo() {
		for (int i = 0; i < indexCount; i++) {
			int markIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(markIndex);
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
	View view;
	ObservableList<Task> initialList;
	ObservableList<Task> searchList;
	ObservableList<Task> tempSearchList;
	CustomDate startDate, endDate;
	boolean isFirstTimeSearch;
	boolean isRealTimeSearch;

	public SearchCommand(String[] parsedUserCommand, Model model, View view, boolean isRealTimeSearch) {
		super(model, view.getTabIndex());
		assert parsedUserCommand != null;
		this.view = view;
		this.isRealTimeSearch = isRealTimeSearch;
		workInfo = parsedUserCommand[0];
		tag = parsedUserCommand[1];
		startDateString = parsedUserCommand[2];
		endDateString = parsedUserCommand[3];
		isImpt = parsedUserCommand[4];
		repeatingType = parsedUserCommand[5];
		
		initialList = getModifiedList(tabIndex);
		searchList = FXCollections.observableArrayList();
		tempSearchList = FXCollections.observableArrayList();
		
		isFirstTimeSearch = true;
	}

	public String execute() {
		processSearch();
		
		// Store the current searchList to tempSearchList
		for (int i = 0; i < searchList.size(); i++) {
			tempSearchList.add(searchList.get(i));
		}
		
		searchList.clear();
		
		isFirstTimeSearch = true;
		if (searchForDateKey()) {
			processSearch();
		} 
		
		searchList = mergeLists(tempSearchList, searchList);
		if (!isRealTimeSearch && searchList.isEmpty()) {
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

	public void processSearch() {
		processWorkInfo();
		processTag();
		processStartDate();

		processEndDate();

		processIsImportant();
		processRepeatingType();
	}
	
	private void processStartDate(){
		if (!startDateString.equals(Parser.NULL)) {
			if (startDateString.equals(HAVING_START_DATE)) {
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
	}
	
	private void processEndDate(){
		if (!endDateString.equals(Parser.NULL)) {
			if (endDateString.equals(HAVING_END_DATE)) {
				if (isFirstTimeSearch) {
					searchList = searchHavingEndDate(initialList);
				} else {
					searchList = searchHavingEndDate(searchList);
				}
			} else {
				endDate = new CustomDate(endDateString);
				if (startDate != null && endDate.hasIndicatedDate() == false) {
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
	
	private void processIsImportant(){
		if (isImpt.equals(Parser.TRUE)) {
			if (isFirstTimeSearch) {
				searchList = searchImportantTask(initialList);
			} else {
				searchList = searchImportantTask(searchList);
			}
		}
	}
	private void processRepeatingType(){
		if (!repeatingType.equals(Parser.NULL)) {
			if (isFirstTimeSearch) {
				searchList = searchRepeatingType(initialList, repeatingType);
			} else {
				searchList = searchRepeatingType(searchList, repeatingType);
			}
			isFirstTimeSearch = false;
		}
	}
	
	private void processTag(){
		if (!tag.equals(Parser.NULL)) {
			if (isFirstTimeSearch) {
				searchList = searchTag(initialList, tag);
			} else {
				searchList = searchTag(searchList, tag);
			}
			isFirstTimeSearch = false;
		}
	}
	
	private void processWorkInfo(){
		if (!workInfo.equals(Parser.NULL)) {
			if (isFirstTimeSearch) {
				searchList = searchWorkInfo(initialList, workInfo);
			} else {
				searchList = searchWorkInfo(searchList, workInfo);
			}
			isFirstTimeSearch = false;
		}
	}
	

	private boolean searchForDateKey() {
		String[] splittedWorkInfo = Parser.splitBySpace(workInfo);
		String lastWords1, lastWords2;
		if (splittedWorkInfo.length >= 1) {
				if(splittedWorkInfo.length == 1)
					lastWords1 = "";
				else
					lastWords1 = splittedWorkInfo[splittedWorkInfo.length - 2] + " "
							+ splittedWorkInfo[splittedWorkInfo.length - 1];
				lastWords2 = splittedWorkInfo[splittedWorkInfo.length - 1];
				if(splittedWorkInfo.length == 1)
					lastWords1 = lastWords2;
			if (startDateString == Parser.NULL) {
				if (doesArrayWeaklyContain(Parser.startDateKeys, lastWords1, lastWords2)) {
					startDateString = HAVING_START_DATE;
					return true;
				}
			}

			if (endDateString == Parser.NULL) {
				if (doesArrayWeaklyContain(Parser.endDateKeys, lastWords1, lastWords2)) {
					endDateString = HAVING_END_DATE;
					return true;
				}
			}
		}
		
		return false;
	}

	private boolean doesArrayWeaklyContain(String[] array, String wordInfo1, String wordInfo2) {
		for (String str : array) {
			if (str.indexOf(wordInfo1.toLowerCase()) == 0){
				int lastIndex = workInfo.lastIndexOf(wordInfo1);
				workInfo = workInfo.substring(0, lastIndex).trim();
				return true;
			}
			if(str.indexOf(wordInfo2.toLowerCase()) == 0){
				int lastIndex = workInfo.lastIndexOf(wordInfo2);
				workInfo = workInfo.substring(0, lastIndex).trim();
				return true;
			}
		}
		return false;
	}
	
	private static ObservableList<Task> searchImportantTask(
			ObservableList<Task> list) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getIsImportant()) {
				result.add(list.get(i));
			}
		}
		return result;
	}

	private static ObservableList<Task> searchTag(ObservableList<Task> list,
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

	private static ObservableList<Task> searchRepeatingType(
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

	private static ObservableList<Task> searchStartDate(
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

	private static ObservableList<Task> searchEndDate(ObservableList<Task> list,
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

	private static ObservableList<Task> searchHavingStartDate(
			ObservableList<Task> list) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getStartDate() != null)
				result.add(list.get(i));
		}
		return result;
	}

	private static ObservableList<Task> searchHavingEndDate(
			ObservableList<Task> list) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getEndDate() != null)
				result.add(list.get(i));
		}
		return result;
	}

	private static ObservableList<Task> searchWorkInfo(
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
	boolean isRealTimeSearch;
	
	public TodayCommand(Model model, View view, boolean isRealTimeSearch) {
		super(model, view.getTabIndex());
		this.view = view;
		this.isRealTimeSearch = isRealTimeSearch;
	}

	public String execute() {
		Command s = new SearchCommand(new String[] { "null", "null", "null",
				"today", "false" }, model, view, isRealTimeSearch);
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

/**
 * 
 * Class SettingsCommand
 * 
 */
class SettingsCommand extends Command {
	View view;

	public SettingsCommand(Model model, View view, String[] parsedUserCommand) {
		super(model, view.getTabIndex());
		this.view = view;
	}

	public String execute() {
		view.showSettingsPage();
		return MESSAGE_OPEN_SETTINGS_DIALOG;
	}
}

/**
 * 
 * Class SyncCommand
 * 
 */
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
		sync.setUsernameAndPassword(username, password);
		sync.execute();
		return MESSAGE_SUCCESSFUL_SYNC;
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
