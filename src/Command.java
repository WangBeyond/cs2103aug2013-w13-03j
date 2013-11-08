import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/****************************************** Abstract Class Command ***************************/
public abstract class Command {

	
	protected static final String HAVING_START_DATE = "having start date";
	protected static final String HAVING_END_DATE = "having end date";


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
		if (tabIndex == Common.PENDING_TAB) {
			return model.getSearchPendingList();
		} else if (tabIndex == Common.COMPLETE_TAB) {
			return model.getSearchCompleteList();
		} else {
			return model.getSearchTrashList();
		}
	}
	
	protected void checkInvalidDates(boolean isRepetitive, boolean hasStartDate, boolean hasEndDate, CustomDate startDate, CustomDate endDate, String repeatingType){
		System.out.println(repeatingType);
		if (hasStartDate && hasEndDate) {
			boolean hasEndDateBeforeStartDate = CustomDate.compare(endDate, startDate) < 0;
			if (hasEndDateBeforeStartDate) {
				throw new IllegalArgumentException(Common.MESSAGE_INVALID_DATE_RANGE);
			}
		}
		if (isRepetitive && (!hasStartDate || !hasEndDate)) {
			throw new IllegalArgumentException(Common.MESSAGE_INVALID_START_END_DATES);
		}
		
		if (isRepetitive) {
			long expectedDifference = CustomDate
					.getUpdateDistance(repeatingType);
			long actualDifference = endDate.getTimeInMillis()
					- startDate.getTimeInMillis();
			if (actualDifference > expectedDifference) {
				throw new IllegalArgumentException(Common.MESSAGE_INVALID_TIME_REPETITIVE);
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
		return tabIndex == Common.PENDING_TAB;
	}
	
	protected boolean isCompleteTab(){
		return tabIndex == Common.COMPLETE_TAB;
	}
	
	protected boolean isTrashTab(){
		return tabIndex == Common.TRASH_TAB;
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
	public abstract String redo();
	
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
			System.out.println("lele");
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
		searchList = getSearchList(tabIndex);
		boolean isOutOfBounds = prevIndex < 0 || prevIndex >= searchList.size();
		if (isOutOfBounds) {
			return INVALID;
		}
		
		System.out.println(searchList.get(prevIndex).getIndexInList());
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
			if(Control.syncThread == null || !Control.syncThread.isRunning() )
				modifiedTask.setStatus(Task.Status.DELETED);
			else 
				modifiedTask.setStatus(Task.Status.DELETED_WHEN_SYNC);
		}
	}
	
	protected void reverseStatus(Task modifiedTask){
		if (isPendingTab() && modifiedTask.hasUnchangedStatus()) {
			if(Control.syncThread == null || !Control.syncThread.isRunning() )
				modifiedTask.setStatus(Task.Status.NEWLY_ADDED);
			else
				modifiedTask.setStatus(Task.Status.ADDED_WHEN_SYNC);
		} else if (isPendingTab() && modifiedTask.hasDeletedStatus()) {
			modifiedTask.setStatus(Task.Status.UNCHANGED);
		}
	}
	
	protected void checkValidIndexes(){
		for (int i = 0; i < indexCount - 1; i++) {
			if (indexList[i] == indexList[i + 1]) {
				throw new IllegalArgumentException(Common.MESSAGE_DUPLICATE_INDEXES);
			}
		}
		
		int MAX_INDEX = indexCount - 1;
		int MIN_INDEX = 0;
		
		if (convertIndex(indexList[MAX_INDEX] - 1) == INVALID
				|| convertIndex(indexList[MIN_INDEX] - 1) == INVALID) {
			throw new IllegalArgumentException(Common.MESSAGE_INDEX_OUT_OF_BOUNDS);
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
		isImptTask =  parsedUserCommand[4].equals(Common.TRUE);
		repeatingType = parsedUserCommand[5];
	}

	public String execute() {
		task = new Task();
		task.setWorkInfo(workInfo);
		
		boolean isRepetitive = !repeatingType.equals(Common.NULL);
		boolean hasStartDate = !startDateString.equals(Common.NULL);
		boolean hasEndDate = !endDateString.equals(Common.NULL);
		
		if (hasStartDate && hasEndDate) {
			CustomDate startDate = new CustomDate(startDateString);
			task.setStartDate(startDate);
			
			CustomDate endDate = new CustomDate(endDateString);
			updateTimeForEndDate(task.getStartDate(), endDate);
			task.setEndDate(endDate);
		} else if(hasStartDate){
			CustomDate startDate = new CustomDate(startDateString);
			task.setStartDate(startDate);
			
			CustomDate cd = new CustomDate();
			cd.setYear(task.getStartDate().getYear());
			cd.setMonth(task.getStartDate().getMonth());
			cd.setDate(task.getStartDate().getDate());
			cd.setHour(23);
			cd.setMinute(59);
			
			task.setEndDate(cd);
		} else if(hasEndDate){
			CustomDate endDate = new CustomDate(endDateString);
			CustomDate cur = new CustomDate();
			cur.setHour(0);
			cur.setMinute(0);
			if(endDate.beforeCurrentTime()){
				return "Invalid as end time is before current time";
			} else {
				task.setStartDate(cur);
				task.setEndDate(endDate);
				updateTimeForEndDate(task.getStartDate(), endDate);
			}
		} 
		if(isRepetitive) {
			splitRepeatingInfo();
		}
		checkInvalidDates(isRepetitive, hasStartDate, hasEndDate, 
				task.getStartDate(), task.getEndDate(), repeatingType);
		
		setTag();
		if (isRepetitive) {
			task.updateDateForRepetitiveTask();
		}
		
		task.setIsImportant(isImptTask);
		
		model.addTaskToPending(task);
		Common.sortList(model.getPendingList());

		return Common.MESSAGE_SUCCESSFUL_ADD;
	}

	public String undo() {
		int index = model.getIndexFromPending(task);
		model.removeTaskFromPendingNoTrash(index);
		assert model.getTaskFromPending(index).equals(task);
		if(task.getStatus() == Task.Status.UNCHANGED)
			model.getUndoTaskBuffer().add(task);
		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	public String redo(){
		model.addTaskToPending(task);
		task.setStatus(Task.Status.NEWLY_ADDED);
		for(int i = 0; i < model.getUndoTaskBuffer().size(); i++){
			if(model.getUndoTaskBuffer().get(i) == task)
				task.setStatus(Task.Status.UNCHANGED);
		}
		
		Common.sortList(model.getPendingList());
		return Common.MESSAGE_SUCCESSFUL_REDO;
	}
	
	private void splitRepeatingInfo() {
		String pattern = "(.*)(\\s+)(\\d+)(\\s+times?.*)";
		if(repeatingType.matches(pattern)) {
			int num = Integer.valueOf(repeatingType.replaceAll(pattern,"$3"));
			task.setNumOccurrences(num);
			repeatingType = repeatingType.replaceAll(pattern, "$1");

		} else 
			task.setNumOccurrences(0);
		String regex = "(every\\s*1?\\s*)(day|week|month|year)(\\s?)";
		String frequentDayRegex = "(every\\s*1?\\s*)(monday|tuesday|wednesday|thursday|friday|saturday|sunday)(\\s?)";
		String dayRegex = "(every)\\s*(\\d+)\\s*(mondays?|tuesdays?|wednesdays?|thursdays?|fridays?|saturdays?|sundays?)";
		if(repeatingType.matches(regex)) {
			repeatingType = repeatingType.replaceAll(regex,"$2");
				if(repeatingType.equals("day"))
					repeatingType = "daily"; 
				else	
					repeatingType = repeatingType+"ly";
		} else if (repeatingType.matches(frequentDayRegex)) {
			repeatingType = "weekly";
		} else if(repeatingType.matches("every\\s*\\d+\\s*(days?|weeks?|months?|years?)")) {
			repeatingType = repeatingType.replaceAll("\\s+", "");
		} else if(repeatingType.matches(dayRegex)){
			repeatingType = repeatingType.replaceAll(dayRegex, "$1$2weeks");
		}
	}
	
	private void setTag(){
		if (tag.equals(Common.NULL) || tag.equals(Common.HASH_TAG)) {
				task.setTag(new Tag(Common.HYPHEN, repeatingType));
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
	Task modifiedTask;
	Task originalTask;
	Task targetTask;
	
	public EditCommand(String[] parsedUserCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		modifiedList = getModifiedList(tabIndex);
		
		index = Integer.parseInt(parsedUserCommand[0]);
		workInfo = parsedUserCommand[1];
		tag = parsedUserCommand[2];
		startDateString = parsedUserCommand[3];
		endDateString = parsedUserCommand[4];
		hasImptTaskToggle = (parsedUserCommand[5].equals(Common.TRUE)) ? true: false;
		repeatingType = parsedUserCommand[6];
	}

	public String execute() {
		if (convertIndex(index - 1) == INVALID) {
			return Common.MESSAGE_INDEX_OUT_OF_BOUNDS;
		}
		modifiedTask = modifiedList.get(convertIndex(index - 1));
		setOriginalTask();
		
		CustomDate startDate, endDate;
		startDate = endDate = null;
		boolean hasRepetitiveKey = !repeatingType.equals(Common.NULL);
		boolean hasWorkInfoKey = !workInfo.equals(Common.NULL);
		boolean hasStartDateKey = !startDateString.equals(Common.NULL);
		boolean hasEndDateKey = !endDateString.equals(Common.NULL);
		
		if (!hasRepetitiveKey && !hasWorkInfoKey) {
			repeatingType = modifiedTask.getTag().getRepetition();
		}
		if (hasStartDateKey) {
			startDate = new CustomDate(startDateString);
		} else {
			startDate = modifiedTask.getStartDate();
		}
		if (hasEndDateKey) {
			endDate = new CustomDate(endDateString);
			updateTimeForEndDate(startDate, endDate);
		} else {
			endDate = modifiedTask.getEndDate();
		}
		
		boolean isRepetitive = !repeatingType.equals(Common.NULL);
		boolean hasStartDate = startDate != null;
		boolean hasEndDate = endDate != null;
		if(isRepetitive)
			splitRepeatingInfo();
		checkInvalidDates(isRepetitive, hasStartDate, hasEndDate, startDate, endDate, repeatingType);
		
		if (hasWorkInfoKey) {
			modifiedTask.setWorkInfo(workInfo);
		}
		if (hasStartDate && hasEndDate) {
			modifiedTask.setStartDate(startDate);
			modifiedTask.setEndDate(endDate);
		} else if(hasStartDate){
			modifiedTask.setStartDate(startDate);
			
			CustomDate cd = new CustomDate();
			cd.setYear(modifiedTask.getStartDate().getYear());
			cd.setMonth(modifiedTask.getStartDate().getMonth());
			cd.setDate(modifiedTask.getStartDate().getDate());
			cd.setHour(23);
			cd.setMinute(59);
			
			modifiedTask.setEndDate(cd);
		} else if(hasEndDate){
			CustomDate cur = new CustomDate();
			cur.setHour(0);
			cur.setMinute(0);
			if(endDate.beforeCurrentTime()){
				return "Invalid as end time is before current time";
			} else {
				modifiedTask.setStartDate(cur);
				modifiedTask.setEndDate(endDate);
				updateTimeForEndDate(modifiedTask.getStartDate(), endDate);
			}
		} 
		setTag();
		if (isRepetitive) {
			modifiedTask.updateDateForRepetitiveTask();
		}
		
		if (hasImptTaskToggle) {
			modifiedTask.setIsImportant(!modifiedTask.isImportantTask());
		}
		
		setTargetTask();
		
		modifiedTask.updateLatestModifiedDate();
		Common.sortList(modifiedList);
		return Common.MESSAGE_SUCCESSFUL_EDIT;
	}
	
	private void setTag() {
		if (tag != Common.NULL) {
			modifiedTask.setTag(new Tag(tag, repeatingType));
			if (tag.equals(Common.HASH_TAG)) {
				modifiedTask.getTag().setTag(Common.HYPHEN);
			}
		} else {
			modifiedTask.setTag(new Tag(modifiedTask.getTag().getTag(), repeatingType));
		}
	}
	
	private void setOriginalTask() {
		originalTask = new Task();
		originalTask.setIsImportant(modifiedTask.isImportantTask());
		originalTask.setStartDate(modifiedTask.getStartDate());
		originalTask.setEndDate(modifiedTask.getEndDate());
		originalTask.setStartDateString(modifiedTask.getStartDateString());
		originalTask.setEndDateString(modifiedTask.getEndDateString());
		originalTask.setWorkInfo(modifiedTask.getWorkInfo());
		originalTask.setTag(modifiedTask.getTag());
		originalTask.setIndexId(modifiedTask.getIndexId());
		originalTask.setLatestModifiedDate(modifiedTask.getLatestModifiedDate());
		originalTask.setOccurrence(modifiedTask.getNumOccurrences(), modifiedTask.getCurrentOccurrence());
	}
	
	private void setTargetTask(){
		targetTask = new Task();
		targetTask.setIsImportant(modifiedTask.isImportantTask());
		targetTask.setStartDate(modifiedTask.getStartDate());
		targetTask.setEndDate(modifiedTask.getEndDate());
		targetTask.setStartDateString(modifiedTask.getStartDateString());
		targetTask.setEndDateString(modifiedTask.getEndDateString());
		targetTask.setWorkInfo(modifiedTask.getWorkInfo());
		targetTask.setTag(modifiedTask.getTag());
		targetTask.setIndexId(modifiedTask.getIndexId());
		targetTask.setLatestModifiedDate(modifiedTask.getLatestModifiedDate());
		targetTask.setOccurrence(modifiedTask.getNumOccurrences(), modifiedTask.getCurrentOccurrence());
	}
	
	private void splitRepeatingInfo() {
		String pattern = "(.*)(\\s+)(\\d+)(\\s+times?.*)";
		if(repeatingType.matches(pattern)) {
			int num = Integer.valueOf(repeatingType.replaceAll(pattern,"$3"));
			modifiedTask.setNumOccurrences(num);
			repeatingType = repeatingType.replaceAll(pattern, "$1");

		} else 
			modifiedTask.setNumOccurrences(0);
		String regex = "(every\\s*1?\\s*)(day|week|month|year)(\\s?)";
		String frequentDayRegex = "(every\\s*1?\\s*)(monday|tuesday|wednesday|thursday|friday|saturday|sunday)(\\s?)";
		String dayRegex = "(every)\\s*(\\d+)\\s*(mondays?|tuesdays?|wednesdays?|thursdays?|fridays?|saturdays?|sundays?)";
		if(repeatingType.matches(regex)) {
				repeatingType = repeatingType.replaceAll(regex,"$2");
				if(repeatingType.equals("day"))
					repeatingType = "daily"; 
				else	
					repeatingType = repeatingType+"ly";
		} else if (repeatingType.matches(frequentDayRegex)) {
			repeatingType = "weekly";
		}	else if(repeatingType.matches("every\\s*\\d+\\s*(days?|weeks?|months?|years?)")) {
			repeatingType = repeatingType.replaceAll("\\s+", "");
		} else if(repeatingType.matches(dayRegex)){
			repeatingType = repeatingType.replaceAll(dayRegex, "$1$2weeks");
		}
	}

	public String undo() {
		modifiedTask.setIsImportant(originalTask.isImportantTask());
		modifiedTask.setStartDate(originalTask.getStartDate());
		modifiedTask.setEndDate(originalTask.getEndDate());
		modifiedTask.setStartDateString(originalTask.getStartDateString());
		modifiedTask.setEndDateString(originalTask.getEndDateString());
		modifiedTask.setWorkInfo(originalTask.getWorkInfo());
		modifiedTask.setTag(originalTask.getTag());
		modifiedTask.setIndexId(originalTask.getIndexId());
		modifiedTask.setLatestModifiedDate(originalTask.getLatestModifiedDate());
		modifiedTask.setOccurrence(originalTask.getNumOccurrences(), originalTask.getCurrentOccurrence());
		Common.sortList(modifiedList);

		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	public String redo() {
		modifiedTask.setIsImportant(targetTask.isImportantTask());
		modifiedTask.setStartDate(targetTask.getStartDate());
		modifiedTask.setEndDate(targetTask.getEndDate());
		modifiedTask.setStartDateString(targetTask.getStartDateString());
		modifiedTask.setEndDateString(targetTask.getEndDateString());
		modifiedTask.setWorkInfo(targetTask.getWorkInfo());
		modifiedTask.setTag(targetTask.getTag());
		modifiedTask.setIndexId(targetTask.getIndexId());
		modifiedTask.setLatestModifiedDate(targetTask.getLatestModifiedDate());
		modifiedTask.setOccurrence(targetTask.getNumOccurrences(), targetTask.getCurrentOccurrence());
		Common.sortList(modifiedList);
		
		return Common.MESSAGE_SUCCESSFUL_REDO;
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

		return Common.MESSAGE_SUCCESSFUL_REMOVE;
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
			Common.sortList(model.getPendingList());
		} else if (isCompleteTab()) {
			Common.sortList(model.getCompleteList());
		}
		Common.sortList(model.getTrashList());
	}
	
	public String undo() {
		for (int i = 0; i < removedTaskInfo.size(); i++) {
			Task removedTask = removedTaskInfo.get(i);
			reverseStatus(removedTask);
			modifiedList.add(removedTask);
			if (isPendingTab() || isCompleteTab()) {
				int index = model.getIndexFromTrash(removedTaskInfo.get(i));
				model.removeTask(index, Common.TRASH_TAB);
			}
		}
		
		removedTaskInfo.clear();
		Common.sortList(modifiedList);

		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	public String redo(){
		processRemove();
		sortInvolvedLists();

		return Common.MESSAGE_SUCCESSFUL_REDO;
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
		return Common.MESSAGE_SUCCESSFUL_CLEAR_ALL;
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
			Common.sortList(model.getTrashList());
		}
	}

	public String undo() {
		if (isPendingTab()) {
			for (int i = 0; i < clearedTasks.length; i++) {
				model.addTaskToPending(clearedTasks[i]);
				reverseStatus(clearedTasks[i]);
			}
			Common.sortList(model.getPendingList());
		} else if (isCompleteTab()) {
			for (int i = 0; i < clearedTasks.length; i++) {
				model.addTaskToComplete(clearedTasks[i]);
			}
			Common.sortList(model.getCompleteList());
		}
		
		model.getTrashList().clear();
		for (int i = 0; i < originalTrashTasks.length; i++) {
			model.addTaskToTrash(originalTrashTasks[i]);
		}
		
		Common.sortList(model.getTrashList());

		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	public String redo(){
		processClear();
		return Common.MESSAGE_SUCCESSFUL_REDO;
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

		return Common.MESSAGE_SUCCESSFUL_COMPLETE;
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
		Common.sortList(model.getPendingList());
		Common.sortList(model.getCompleteList());
	}
	
	private void retrieveIndexesAfterProcessing(){
		for (int i = 0; i < indexCount; i++) {
			indexInCompleteList[i] = model.getIndexFromComplete(toCompleteTasks[i]);
		}
		Arrays.sort(indexInCompleteList);
	}
	
	
	private void checkSuitableTab(){
		if (tabIndex != Common.PENDING_TAB) {
			throw new IllegalArgumentException(Common.MESSAGE_WRONG_COMPLETE_TABS);
		}
	}

	public String undo() {
		for (int i = indexCount - 1; i >= 0; i--) {
			Task toPending = model.getTaskFromComplete(indexInCompleteList[i]);
			reverseStatus(toPending);
			model.removeTaskFromCompleteNoTrash(indexInCompleteList[i]);
			model.addTaskToPending(toPending);
		}
		Common.sortList(model.getPendingList());
		Common.sortList(model.getCompleteList());

		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	public String redo(){
		processComplete();
		retrieveIndexesAfterProcessing();

		return Common.MESSAGE_SUCCESSFUL_REDO;
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
		
		return Common.MESSAGE_SUCCESSFUL_INCOMPLETE;
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
		Common.sortList(model.getPendingList());
		Common.sortList(model.getCompleteList());
	}
	
	private void retrieveIndexesAfterProcessing(){
		for (int i = 0; i < indexCount; i++) {
			indexInIncompleteList[i] = model
					.getIndexFromPending(toIncompleteTasks[i]);
		}
		Arrays.sort(indexInIncompleteList);
	}
	
	private void checkSuitableTab(){
		if (tabIndex != Common.COMPLETE_TAB) {
			throw new IllegalArgumentException(Common.MESSAGE_WRONG_INCOMPLETE_TABS);
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
		Common.sortList(model.getCompleteList());
		Common.sortList(model.getPendingList());

		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	public String redo(){
		processIncomplete();
		retrieveIndexesAfterProcessing();
		
		return Common.MESSAGE_SUCCESSFUL_REDO;
	}
}

/**
 * 
 * Class RecoverCommand
 * 
 */
class RecoverCommand extends IndexCommand {
	Task[] toRecoverTasks;
	int[] indexInPendingList;


	public RecoverCommand(String[] parsedUserCommand, Model model,
			int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		modifiedList = this.model.getTrashList();
		indexCount = parsedUserCommand.length;
		indexInPendingList = new int[indexCount];
		toRecoverTasks = new Task[indexCount];

		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(parsedUserCommand[i]);
		}
	}

	public String execute() {
		Arrays.sort(indexList);
		checkSuitableTab();
		checkValidIndexes();
		
		processRecover();
		retrieveIndexesAfterProcessing();
		
		return Common.MESSAGE_SUCCESSFUL_RECOVER;
	}
	
	private void processRecover(){
		for (int i = indexCount - 1; i >= 0; i--) {
			int recoverIndex = convertIndex(indexList[i] - 1);
			Task toPending = model.getTaskFromTrash(recoverIndex);
			reverseStatus(toPending);
			toRecoverTasks[i] = toPending;
			model.getTrashList().remove(recoverIndex);
			model.addTaskToPending(toPending);
		}
		Common.sortList(model.getPendingList());
		Common.sortList(model.getTrashList());
	}
	
	private void retrieveIndexesAfterProcessing(){
		for (int i = 0; i < indexCount; i++) {
			indexInPendingList[i] = model
					.getIndexFromPending(toRecoverTasks[i]);
		}
		Arrays.sort(indexInPendingList);
	}
	
	private void checkSuitableTab(){
		if (tabIndex != Common.TRASH_TAB) {
			throw new IllegalArgumentException(Common.MESSAGE_WRONG_RECOVER_TABS);
		}
	}

	public String undo() {
		for (int i = indexCount - 1; i >= 0; i--) {
			Task toTrash = model.getTaskFromPending(indexInPendingList[i]);
			modifyStatus(toTrash);
			toRecoverTasks[i] = toTrash;
			model.getPendingList().remove(indexInPendingList[i]);
			model.addTaskToTrash(toTrash);
		}
		Common.sortList(model.getTrashList());
		Common.sortList(model.getPendingList());

		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	public String redo(){
		processRecover();
		retrieveIndexesAfterProcessing();
		
		return Common.MESSAGE_SUCCESSFUL_REDO;
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

		return Common.MESSAGE_SUCCESSFUL_MARK;
	}

	public String undo() {
		for (int i = 0; i < indexCount; i++) {
			int unmarkIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(unmarkIndex);
			targetTask.setIsImportant(false);
		}
		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	public String redo(){
		for (int i = 0; i < indexCount; i++) {
			int markIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(markIndex);
			targetTask.setIsImportant(true);
		}

		return Common.MESSAGE_SUCCESSFUL_REDO;
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

		return Common.MESSAGE_SUCCESSFUL_UNMARK;
	}

	public String undo() {
		for (int i = 0; i < indexCount; i++) {
			int markIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(markIndex);
			targetTask.setIsImportant(true);
		}
		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	public String redo(){
		for (int i = 0; i < indexCount; i++) {
			int unmarkIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(unmarkIndex);
			targetTask.setIsImportant(false);
		}

		return Common.MESSAGE_SUCCESSFUL_REDO;
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
	int numOccurrences = 0;
	int currentOccurrence;
	
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
		splitRepeatingInfo();
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
		
		/*searchList.clear();
		
		isFirstTimeSearch = true;
		if (searchForDateKey()) {
			processSearch();
		} 
		
		searchList = mergeLists(tempSearchList, searchList);*/
		if (!isRealTimeSearch && searchList.isEmpty()) {
			return Common.MESSAGE_NO_RESULTS;
		}
		
		TwoWayCommand.setIndexType(TwoWayCommand.SEARCHED);
		if (tabIndex == Common.PENDING_TAB) {
			model.setSearchPendingList(searchList);
			view.taskPendingList.setItems(model.getSearchPendingList());
		} else if (tabIndex == Common.COMPLETE_TAB) {
			model.setSearchCompleteList(searchList);
			view.taskCompleteList.setItems(model.getSearchCompleteList());
		} else {
			model.setSearchTrashList(searchList);
			view.taskTrashList.setItems(model.getSearchTrashList());
		}
		return Common.MESSAGE_SUCCESSFUL_SEARCH;
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
		processNumOccurrences();
	}
	
	private void splitRepeatingInfo() {
		String pattern = "(.*)(\\s+)(\\d+)(\\s+times?.*)";
		if(repeatingType.matches(pattern)) {
			int num = Integer.valueOf(repeatingType.replaceAll(pattern,"$3"));
			numOccurrences = num;
			repeatingType = repeatingType.replaceAll(pattern, "$1");

		} else 
			numOccurrences = 0;
		String regex = "(every\\s*1?\\s*)(day|week|month|year)(\\s?)";
		String frequentDayRegex = "(every\\s*1?\\s*)(monday|tuesday|wednesday|thursday|friday|saturday|sunday)(\\s?)";
		String dayRegex = "(every)\\s*(\\d+)\\s*(mondays?|tuesdays?|wednesdays?|thursdays?|fridays?|saturdays?|sundays?)";
		if(repeatingType.matches(regex)) {
				repeatingType = repeatingType.replaceAll(regex,"$2");
				if(repeatingType.equals("day"))
					repeatingType = "daily"; 
				else	
					repeatingType = repeatingType+"ly";
		} else if (repeatingType.matches(frequentDayRegex)) {
			repeatingType = "weekly";
		}	else if(repeatingType.matches("every\\s*\\d+\\s*(days?|weeks?|months?|years?)")) {
			repeatingType = repeatingType.replaceAll("\\s+", "");
		} else if(repeatingType.matches(dayRegex)){
			repeatingType = repeatingType.replaceAll(dayRegex, "$1$2weeks");
		}
	}
	
	private void processStartDate(){
		if (!startDateString.equals(Common.NULL)) {
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
		if (!endDateString.equals(Common.NULL)) {
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
		if (isImpt.equals(Common.TRUE)) {
			if (isFirstTimeSearch) {
				searchList = searchImportantTask(initialList);
			} else {
				searchList = searchImportantTask(searchList);
			}
		}
	}
	
	private void processNumOccurrences(){
		if (numOccurrences != 0) {
			if (isFirstTimeSearch) {
				searchList = searchOccurrenceNum(initialList, numOccurrences);
			} else {
				searchList = searchOccurrenceNum(searchList, numOccurrences);
			}
			isFirstTimeSearch = false;
		}
	}
	
	private void processRepeatingType(){
		if (!repeatingType.equals(Common.NULL)) {
			if (isFirstTimeSearch) {
				searchList = searchRepeatingType(initialList, repeatingType);
			} else {
				searchList = searchRepeatingType(searchList, repeatingType);
			}
			isFirstTimeSearch = false;
		}
	}
	
	private void processTag(){
		if (!tag.equals(Common.NULL)) {
			if (isFirstTimeSearch) {
				searchList = searchTag(initialList, tag);
			} else {
				searchList = searchTag(searchList, tag);
			}
			isFirstTimeSearch = false;
		}
	}
	
	private void processWorkInfo(){
		if (!workInfo.equals(Common.NULL)) {
			if (isFirstTimeSearch) {
				searchList = searchWorkInfo(initialList, workInfo);
			} else {
				searchList = searchWorkInfo(searchList, workInfo);
			}
			isFirstTimeSearch = false;
		}
	}
	

	private boolean searchForDateKey() {
		String[] splittedWorkInfo = Common.splitBySpace(workInfo);
		String lastTwoWords, lastWord;
		if (splittedWorkInfo.length >= 1) {
				if(splittedWorkInfo.length == 1)
					lastTwoWords = "";
				else
					lastTwoWords = splittedWorkInfo[splittedWorkInfo.length - 2] + " "
							+ splittedWorkInfo[splittedWorkInfo.length - 1];
				lastWord = splittedWorkInfo[splittedWorkInfo.length - 1];
				if(splittedWorkInfo.length == 1)
					lastTwoWords = lastWord;
			if (startDateString == Common.NULL) {
				if (doesArrayWeaklyContain(Common.startDateKeys, lastTwoWords, lastWord)) {
					startDateString = HAVING_START_DATE;
					return true;
				}
			}

			if (endDateString == Common.NULL) {
				if (doesArrayWeaklyContain(Common.endDateKeys, lastTwoWords, lastWord)) {
					endDateString = HAVING_END_DATE;
					return true;
				}
			}
		}
		
		return false;
	}

	private boolean doesArrayWeaklyContain(String[] array, String wordInfo1, String wordInfo2) {
		for (String str : array) {
			if (str.equals(wordInfo1.toLowerCase())){
				int lastIndex = workInfo.lastIndexOf(wordInfo1);
				workInfo = workInfo.substring(0, lastIndex).trim();
				return true;
			}
			if(str.equals(wordInfo2.toLowerCase())){
				int lastIndex = workInfo.lastIndexOf(wordInfo2);
				workInfo = workInfo.substring(0, lastIndex).trim();
				return true;
			}
			/*
			if (str.indexOf(wordInfo1.toLowerCase()) == 0){
				int lastIndex = workInfo.lastIndexOf(wordInfo1);
				workInfo = workInfo.substring(0, lastIndex).trim();
				return true;
			}
			if(str.indexOf(wordInfo2.toLowerCase()) == 0){
				int lastIndex = workInfo.lastIndexOf(wordInfo2);
				workInfo = workInfo.substring(0, lastIndex).trim();
				return true;
			}*/
		}
		return false;
	}
	
	private static ObservableList<Task> searchOccurrenceNum(ObservableList<Task> list, int occurNum) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getNumOccurrences() == occurNum) {
				result.add(list.get(i));
			}
		}
		return result;
	}
	
	private static ObservableList<Task> searchImportantTask(
			ObservableList<Task> list) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isImportantTask()) {
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
						&& CustomDate.compare(startDate, date) >= 0) {
					result.add(list.get(i));
				}
			}
		} else {
			for (int i = 0; i < list.size(); i++) {
				CustomDate startDate = list.get(i).getStartDate();
				if (startDate != null && CustomDate.compare(startDate, date) >= 0)
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
				if (endDate != null && CustomDate.compare(endDate, date) <= 0) {
					result.add(list.get(i));
				}
			}
		} else {
			for (int i = 0; i < list.size(); i++) {
				CustomDate endDate = list.get(i).getEndDate();
				if (endDate != null && CustomDate.compare(endDate, date) <= 0) {
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
			String tag = list.get(i).getTag().getTag().toLowerCase().substring(1);
			if (searchedWorkInfo.contains(workInfo.toLowerCase()) || (!tag.equals("") && tag.contains(workInfo.toLowerCase()))) {
				result.add(list.get(i));
			}
		}
		return result;
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
		if (tabIndex == Common.PENDING_TAB) {
			view.taskPendingList.setItems(model.getPendingList());
		} else if (tabIndex == Common.COMPLETE_TAB) {
			view.taskCompleteList.setItems(model.getCompleteList());
		} else {
			view.taskTrashList.setItems(model.getTrashList());
		}
		return Common.MESSAGE_SUCCESSFUL_SHOW_ALL;
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
		return Common.MESSAGE_SUCCESSFUL_HELP;
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
		view.showSettingsPage(null);
		return Common.MESSAGE_SUCCESSFUL_SETTINGS;
	}
}

/**
 * 
 * Class SyncCommand
 * 
 */
class SyncCommand extends Command implements Runnable {
	String username = null;
	String password = null;
	String feedback = null;
	boolean isValidUserPass = true;
	boolean isRunning = false;
	
	Synchronization sync;
	View view;
	Storage taskFile;
	Thread t;
	
	public SyncCommand(Model model, Synchronization sync, View view, Storage taskFile) {
		super(model);
		this.sync = sync;
		this.view = view;
		this.taskFile = taskFile;
		username = model.getUsername();
		password = model.getPassword();
		
	    t = new Thread(this, "Sync Thread");  
	  
	    t.start(); // Start the thread

	    try{
	  	  t.join();
	    } catch (InterruptedException e){
	  	  System.out.println("Failed to join back to main thread.");
	
	    }
		
	}
	
	private boolean checkInternetAccess(){
            try {
                //make a URL to a known source
                URL url = new URL("http://www.google.com");

                //open a connection to that source
                HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();

                //trying to retrieve data from the source. If there
                //is no connection, this line will fail
                urlConnect.getContent();

            } catch (UnknownHostException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
            return true;
    }
	
	

	public void run() {
		if (checkInternetAccess()) {
			isRunning = true;
			view.setSyncProgressVisible(true);
			feedback = execute();
			if (feedback.equals(Common.MESSAGE_SYNC_INVALID_USERNAME_PASSWORD)){
				t.interrupt();
			}
			view.setSyncProgressVisible(false);
			isRunning = false;
			model.clearSyncInfo();
			try {
				taskFile.storeToFile();
			} catch (IOException io) {
				System.out.println(io.getMessage());
			}
		} else {
			view.showNoInternetConnection();
		}
	}

	@Override
	public String execute() {
		try{
			view.setSyncProgressVisible(true);
			sync.setUsernameAndPassword(username, password);
			feedback = sync.execute();
			Common.sortList(model.getPendingList());
			return feedback;
		} catch(Exception e){
			view.setSyncProgressVisible(false);
			e.printStackTrace();
			feedback = "There is currently some problem in syncing.";
			return feedback;
		}
	}
	
	public String getFeedback() {
		return feedback;
	}
	
	public boolean isRunning() {
		return isRunning;
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
