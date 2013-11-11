import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.ArrayList;


import com.google.gdata.util.AuthenticationException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

//@author A0098077N
/****************************************** Abstract Class Command ***************************/
public abstract class Command {
	protected static final String SPARSE_DAY_RECURRING_REGEX = "(every)\\s*(\\d+)\\s*(mondays?|tuesdays?|wednesdays?|thursdays?|fridays?|saturdays?|sundays?)";
	protected static final String FREQUENT_DAY_RECURRING_REGEX = "(every\\s*1?\\s*)(monday|tuesday|wednesday|thursday|friday|saturday|sunday)(\\s?)";
	protected static final String SPARSE_RECURRING_REGEX = "every\\s*\\d+\\s*(days?|weeks?|months?|years?)";
	protected static final String FREQUENT_RECURRING_REGEX = "(every\\s*1?\\s*)(day|week|month|year)(\\s?)";
	protected static final String OCCURRENCE_PATTERN = "(.*)(\\s+)(\\d+)(\\s+times?.*)";
	protected static final String HAVING_START_DATE = "having start date";
	protected static final String HAVING_END_DATE = "having end date";

	// Model containing lists of tasks to process
	protected Model model;
	// Current tab when the command is processed
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
	
	/**
	 * This function is used to get the corresponding search list basing on the
	 * current tab
	 * 
	 * @param tabIndex
	 *            the index of the current tab
	 * @return the corresponding search list
	 */
	protected ObservableList<Task> getSearchList(int tabIndex) {
		if (isPendingTab()) {
			return model.getSearchPendingList();
		} else if (isCompleteTab()) {
			return model.getSearchCompleteList();
		} else {
			return model.getSearchTrashList();
		}
	}
	
	/**
	 * This function is used to check for invalid dates from the task which the
	 * command is currently working on
	 * 
	 * @param isRepetitive
	 *            Indicator whether the task is a recurring task or not
	 * @param hasStartDate
	 *            Indicator whether the task has a start date
	 * @param hasEndDate
	 *            Indicator whether the task has an end date
	 * @param startDate
	 *            the start date of the task if it has
	 * @param endDate
	 *            the end date of the task if it has
	 * @param repeatingType
	 *            the type of repetition of the task if it is a recurring task
	 */
	protected void checkInvalidDates(boolean isRepetitive,
			boolean hasStartDate, boolean hasEndDate, CustomDate startDate,
			CustomDate endDate, String repeatingType) {
		boolean isTimedTask = hasStartDate && hasEndDate;
		if (isTimedTask) {
			boolean hasEndDateBeforeStartDate = CustomDate.compare(endDate, startDate) < 0;
			if (hasEndDateBeforeStartDate) {
				throw new IllegalArgumentException(Common.MESSAGE_INVALID_DATE_RANGE);
			}
		}
		
		if (isRepetitive && (!hasStartDate || !hasEndDate)) { // Recurring task without indicating dates
			throw new IllegalArgumentException(Common.MESSAGE_INVALID_START_END_DATES);
		}
		
		if (isRepetitive) { // recurring task
			checkDifference(startDate, endDate, repeatingType);
		}
	}
	
	/**
	 * This function is used to check whether the difference between start date
	 * and end date of a task is valid for the corresponding type of repetition
	 * 
	 * @param startDate
	 *            the start date of the task
	 * @param endDate
	 *            the end date of the task
	 * @param repeatingType
	 *            the type of repetition
	 */
	private void checkDifference(CustomDate startDate, CustomDate endDate,
			String repeatingType) {
		long expectedDifference = CustomDate
				.getUpdateDistance(repeatingType);
		long actualDifference = endDate.getTimeInMillis()
				- startDate.getTimeInMillis();
		if (actualDifference > expectedDifference) {
			throw new IllegalArgumentException(Common.MESSAGE_INVALID_TIME_REPETITIVE);
		}
	}
	
	/**
	 * This function is used to modify the end date of the task the command is
	 * currently working on to be suitable to the standard in the application
	 * 
	 * @param startDate
	 *            the start date of the task
	 * @param endDate
	 *            the end date of the task
	 */
	protected void updateTimeForEndDate(CustomDate startDate, CustomDate endDate){
		boolean isMidnight = endDate != null && endDate.getHour() == 0
				&& endDate.getMinute() == 0;
		if (isMidnight) {
			endDate.setHour(23);
			endDate.setMinute(59);
		}
		
		boolean hasNoIndicatedDate = endDate.hasIndicatedDate() == false
				&& startDate != null;
		if (hasNoIndicatedDate) {
			endDate.setYear(startDate.getYear());
			endDate.setMonth(startDate.getMonth());
			endDate.setDate(startDate.getDate());
		}
	}
	
	// Check if the current tab is pending tab
	protected boolean isPendingTab(){
		return tabIndex == Common.PENDING_TAB;
	}
	
	// Check if the current tab is complete tab
	protected boolean isCompleteTab(){
		return tabIndex == Common.COMPLETE_TAB;
	}
	
	// Check if the current tab is trash tab
	protected boolean isTrashTab(){
		return tabIndex == Common.TRASH_TAB;
	}
	
	/**
	 * This function is used to get the modified list basing on the current tab
	 * @param tabIndex the index of the current tab
	 * @return the corresponding list
	 */
	protected ObservableList<Task> getModifiedList(int tabIndex){
		if (isPendingTab()) {
			return model.getPendingList();
		} else if (isCompleteTab()) {
			return model.getCompleteList();
		} else {
			return model.getTrashList();
		}
	}
	
	//@author A0105667B
	// Set the type of repetition
		protected String setRepeatingType(String repeatingType) {
			if(repeatingType.matches(FREQUENT_RECURRING_REGEX)) {
				repeatingType = repeatingType.replaceAll(FREQUENT_RECURRING_REGEX,"$2");
					if(repeatingType.equals("day")){
						repeatingType = "daily"; 
					}else{	
						repeatingType = repeatingType+"ly";
					}
			} else if (repeatingType.matches(FREQUENT_DAY_RECURRING_REGEX)) {
				repeatingType = "weekly";
			} else if(repeatingType.matches(SPARSE_RECURRING_REGEX)) {
				repeatingType = repeatingType.replaceAll("\\s+", "");
			} else if(repeatingType.matches(SPARSE_DAY_RECURRING_REGEX)){
				repeatingType = repeatingType.replaceAll(SPARSE_DAY_RECURRING_REGEX, "$1$2weeks");
			}
			
			return repeatingType;
		}
}

//@author A0098077N
/****************************************************************************************
 * Abstract class TwoWayCommand extended from class Command to support undo and redo	*
 *																						*
 ****************************************************************************************/

abstract class TwoWayCommand extends Command {
	protected static final boolean SEARCHED = true;
	protected static final boolean SHOWN = false;
	protected static final int INVALID = -1;
	
	// The current type of index in the list, whether it is the original or the results from search
	protected static boolean listedIndexType;
	// The modified list that the command will work on
	protected ObservableList<Task> modifiedList;

	public TwoWayCommand(Model model, int tabIndex) {
		super(model, tabIndex);
	}
	
	// Abstract function undoing command to be implemented in extended classes
	public abstract String undo();
	// Abstract function redoing command to be implemented in extended classes
	public abstract String redo();
	
	//@author A0105667B
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
		if (isSearchedResults()) {
			return getOriginalIndexFromSearchList(prevIndex);
		} else {
			return getOriginalIndexFromOriginalList(prevIndex);
		}
	}
	
	
	/**
	 * This function is used to get the true index of a task from the given
	 * original index This will first check if the index is out of bounds or
	 * not.
	 * 
	 * @param prevIndex
	 *            the requested index
	 * @return INVALID if index is out of bounds, else itself
	 */
	private int getOriginalIndexFromOriginalList(int prevIndex) {	
		boolean isOutOfBounds = prevIndex < 0
				|| prevIndex >= modifiedList.size();
		if (isOutOfBounds) {
			return INVALID;
		}
		return prevIndex;
	}
	
	/**
	 * This function is used to get the true index of a task from the given
	 * index in search result This will first check if this index in the search
	 * list is out of bounds or not
	 * 
	 * @param prevIndex
	 *            the requested index
	 * @return INVALID if index is out of bounds, else its corresponding
	 *         original index
	 */
	private int getOriginalIndexFromSearchList(int prevIndex) {
		ObservableList<Task> searchList;
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

//@author A0098077N
/************************************************************************************************************
 * Abstract class IndexCommand extended from class TwoWayCommand to work more specifically on indices		*
 *																											*
 ************************************************************************************************************/
abstract class IndexCommand extends TwoWayCommand{
	// The list of indices which the command will work on
	protected int[] indexList;
	// The number of indices
	protected int indexCount;
	
	public IndexCommand(Model model, int tabIndex){
		super(model, tabIndex);
	}
	
	/**
	 * This function is used to check if the list of indices is valid or not
	 */
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
	
	/**
	 * This function is used to get list of indices
	 * @param parsedUserCommand
	 */
	protected void getListOfIndices(String[] parsedUserCommand) {
		indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(parsedUserCommand[i]);
		}
	}
}

//@author A0105523U
/********************************subclass of TwoWayCommand*************************************************/
/**
 * 
 * Class AddCommand. This command create a new task for the user, the new task will be created in the pending list
 * 
 */
class AddCommand extends TwoWayCommand {
	// Work info
	private String workInfo;
	// Tag
	private String tag;
	// Start date
	private String startDateString;
	// End date
	private String endDateString;
	// Indicator whether this is an important task
	private boolean isImptTask;
	// Type of repetition
	private String repeatingType;
	// The newly created task
	private Task createdTask;
	
	/**
	 * The constructor of this class
	 * 
	 * @param parsedUserCommand
	 *            the array of info from the command parsed by Parser class
	 * @param model
	 *            the model in the application
	 * @param tabIndex
	 *            the current tab index
	 *
	 */
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
	
	/**
	 * Execute the ADD command
	 */
	public String execute() {
		createdTask = new Task();
		updateTask();
		// Add the task to the pending list
		model.addTaskToPending(createdTask);
		Common.sortList(model.getPendingList());
		return Common.MESSAGE_SUCCESSFUL_ADD;
	}
	
	/**
	 * This function is used to update all the information for the created task basing on the command input
	 */
	private void updateTask() {
		createdTask.setWorkInfo(workInfo);
		
		boolean isRepetitive = !repeatingType.equals(Common.NULL);
		boolean hasStartDate = !startDateString.equals(Common.NULL);
		boolean hasEndDate = !endDateString.equals(Common.NULL);
		
		setDates(hasStartDate, hasEndDate); 
		
		if(isRepetitive) {
			splitRepeatingInfo();
		}
		checkInvalidDates(isRepetitive, hasStartDate, hasEndDate, 
				createdTask.getStartDate(), createdTask.getEndDate(), repeatingType);
		
		setTag();
		if (isRepetitive) {
			createdTask.updateDateForRepetitiveTask();
		}
		
		createdTask.setIsImportant(isImptTask);
	}
	
	/**
	 * Set dates for the added task
	 * @param hasStartDate indicator if the input command has start date
	 * @param hasEndDate indicator if the input command has end date
	 */
	private void setDates(boolean hasStartDate, boolean hasEndDate) {
		if (hasStartDate && hasEndDate) {
			setDateForTaskWithBothDates();
		} else if(hasStartDate){
			setDateForTaskWithStartDate();
		} else if(hasEndDate){
			setDateForTaskWithEndDate();
		}
	}
	
	/**
	 * Set dates when the input command has only end date
	 */
	private void setDateForTaskWithEndDate() {
		CustomDate endDate = new CustomDate(endDateString);
		CustomDate cur = new CustomDate();
		cur.setHour(0);
		cur.setMinute(0);
		if(endDate.beforeCurrentTime()){
			cur.setYear(endDate.getYear());
			cur.setMonth(endDate.getMonth());
			cur.setDate(endDate.getDate());
			createdTask.setStartDate(cur);
			createdTask.setEndDate(endDate);
			updateTimeForEndDate(createdTask.getStartDate(), endDate);
		} else {
			createdTask.setStartDate(cur);
			createdTask.setEndDate(endDate);
			updateTimeForEndDate(createdTask.getStartDate(), endDate);
		}
	}
	
	/**
	 * Set dates when the input command has only start date
	 */
	private void setDateForTaskWithStartDate() {
		CustomDate startDate = new CustomDate(startDateString);
		createdTask.setStartDate(startDate);
		CustomDate cd = new CustomDate();
		cd.setYear(createdTask.getStartDate().getYear());
		cd.setMonth(createdTask.getStartDate().getMonth());
		cd.setDate(createdTask.getStartDate().getDate());
		cd.setHour(23);
		cd.setMinute(59);
		createdTask.setEndDate(cd);
	}

	/**
	 * Set dates when the input command has both start and end dates
	 */
	private void setDateForTaskWithBothDates() {
		CustomDate startDate = new CustomDate(startDateString);
		createdTask.setStartDate(startDate);
		CustomDate endDate = new CustomDate(endDateString);
		updateTimeForEndDate(createdTask.getStartDate(), endDate);
		createdTask.setEndDate(endDate);
	}
	
	//@author A0100927M
	/**
	 * Undo the ADD command
	 */
	public String undo() {
		int index = model.getIndexFromPending(createdTask);
		model.removeTaskFromPendingNoTrash(index);
		assert model.getTaskFromPending(index).equals(createdTask);
		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	//@author A0098077N
	/**
	 * Redo the ADD command
	 */
	public String redo(){
		model.addTaskToPending(createdTask);
		createdTask.setStatus(Task.Status.NEWLY_ADDED);
		Common.sortList(model.getPendingList());
		return Common.MESSAGE_SUCCESSFUL_REDO;
	}
	
	//@author A0105667B
	/**
	 * This function is used to split the recurring info from the command From
	 * this function, we can determine the number of occurrences and type of
	 * repetition for a task
	 */
	private void splitRepeatingInfo() {
		setOccurrences();
		repeatingType = setRepeatingType(repeatingType);
	}
	
	
	
	// Set number of occurrences
	private void setOccurrences() {
		if(repeatingType.matches(OCCURRENCE_PATTERN)) {
			int num = Integer.valueOf(repeatingType.replaceAll(OCCURRENCE_PATTERN,"$3"));
			createdTask.setNumOccurrences(num);
			repeatingType = repeatingType.replaceAll(OCCURRENCE_PATTERN, "$1");
		} else {
			createdTask.setNumOccurrences(0);
		}
	}
	
	/**
	 * Set the tag for display of this task
	 */
	private void setTag(){
		if (tag.equals(Common.NULL) || tag.equals(Common.HASH_TAG)) {
				createdTask.setTag(new Tag(Common.HYPHEN, repeatingType));
		} else {
				createdTask.setTag(new Tag(tag, repeatingType));
		}
	}
}

//@author A0105667B
/**
 * 
 * Class Edit Command. This command edits an existing task in the model
 * 
 */
class EditCommand extends TwoWayCommand {
	// Original index of the edited task
	int index; 
	// Work info of the target task 
	String workInfo;
	// Tag of the target task
	String tag;
	// Start date in string format of the target task
	String startDateString;
	// End date in string format of the target task
	String endDateString;
	// Indicator whether the target task is important or not
	boolean hasImptTaskToggle;
	// Type of repetition of the target task
	String repeatingType;
	// The edited task whose infos will change from the originalTask to targetTask
	Task editedTask;
	// Original task
	Task originalTask;
	// Target task
	Task targetTask;
	// The start date and end date of the target task
	CustomDate startDate, endDate;
	
	/**
	 * Constructor of this command
	 * 
	 * @param parsedUserCommand
	 *            array of info from the command parsed by the Parser class
	 * @param model
	 *            the model of tasks in the application
	 * @param tabIndex
	 *            the current tab index
	 */
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
	
	/**
	 * Execute the EDIT command
	 */
	public String execute() {
		if (convertIndex(index - 1) == INVALID) {
			return Common.MESSAGE_INDEX_OUT_OF_BOUNDS;
		}
		editedTask = modifiedList.get(convertIndex(index - 1));
		// Start editing
		setOriginalTask();
		processEditing();
		setTargetTask();
		editedTask.updateLatestModifiedDate();
		Common.sortList(modifiedList);
		return Common.MESSAGE_SUCCESSFUL_EDIT;
	}
	
	/**
	 * This is the main function for the editing process
	 */
	private void processEditing() {
		startDate = endDate = null;
		boolean hasRepetitiveKey = !repeatingType.equals(Common.NULL);
		boolean hasWorkInfoKey = !workInfo.equals(Common.NULL);
		boolean hasStartDateKey = !startDateString.equals(Common.NULL);
		boolean hasEndDateKey = !endDateString.equals(Common.NULL);
		
		determineDateInfoOfTargetTask(hasRepetitiveKey, hasWorkInfoKey,
				hasStartDateKey, hasEndDateKey);
		
		boolean isRepetitive = !repeatingType.equals(Common.NULL);
		boolean hasStartDate = startDate != null;
		boolean hasEndDate = endDate != null;
		checkRecurringInfo(isRepetitive, hasStartDate, hasEndDate);
		
		updateEditedTask(hasWorkInfoKey, isRepetitive, hasStartDate, hasEndDate);
	}
	
	/**
	 * Start updating the new info for edited task
	 * 
	 * @param hasWorkInfoKey
	 *            indicator whether the command input has a new work info
	 * @param isRepetitive
	 *            indicator whether the target task is recurring task
	 * @param hasStartDate
	 *            indicator whether the target task has start date
	 * @param hasEndDate
	 *            indicator whether the target task has end date
	 */
	private void updateEditedTask(boolean hasWorkInfoKey, boolean isRepetitive,
			boolean hasStartDate, boolean hasEndDate) {
		if (hasWorkInfoKey) {
			editedTask.setWorkInfo(workInfo);
		}
		
		updateDateInfos(hasStartDate, hasEndDate); 
		setTag();
		if (isRepetitive) {
			editedTask.updateDateForRepetitiveTask();
		}
		
		if (hasImptTaskToggle) {
			editedTask.setIsImportant(!editedTask.isImportantTask());
		}
	}
	
	/**
	 * Update the date info for the edited task
	 * 
	 * @param hasStartDate
	 *            indicator whether target task has start date
	 * @param hasEndDate
	 *            indicator whether target task has end date
	 */
	private void updateDateInfos(boolean hasStartDate, boolean hasEndDate) {
		if (hasStartDate && hasEndDate) {
			updateDatesWithBothDates();
		} else if(hasStartDate){
			updateDateWithStartDate();
		} else if(hasEndDate){
			updateDateWithEndDate();
		}
	}
	
	/**
	 * Update date when target task has both start date and end date
	 */
	private void updateDatesWithBothDates() {
		editedTask.setStartDate(startDate);
		editedTask.setEndDate(endDate);
	}
	
	/**
	 * Update date when target task has only end date
	 */
	private void updateDateWithEndDate() {
		CustomDate cur = new CustomDate();
		cur.setHour(0);
		cur.setMinute(0);
		if(endDate.beforeCurrentTime()){
			cur.setYear(endDate.getYear());
			cur.setMonth(endDate.getMonth());
			cur.setDate(endDate.getDate());
			editedTask.setStartDate(cur);
			editedTask.setEndDate(endDate);
			updateTimeForEndDate(editedTask.getStartDate(), endDate);
		} else {
			editedTask.setStartDate(cur);
			editedTask.setEndDate(endDate);
			updateTimeForEndDate(editedTask.getStartDate(), endDate);
		}
	}
	
	/**
	 * Update date when target task has only start date
	 */
	private void updateDateWithStartDate() {
		editedTask.setStartDate(startDate);
		CustomDate cd = new CustomDate();
		cd.setYear(editedTask.getStartDate().getYear());
		cd.setMonth(editedTask.getStartDate().getMonth());
		cd.setDate(editedTask.getStartDate().getDate());
		cd.setHour(23);
		cd.setMinute(59);
		editedTask.setEndDate(cd);
	}
	
	/**
	 * This function is used to get the repeating info of the target task and
	 * check whether it is valid or not
	 * 
	 * @param isRepetitive
	 *            indicator whether the target task is a recurring task or not
	 * @param hasStartDate
	 *            indicator whether the target task has start date
	 * @param hasEndDate
	 *            indicator whether the target task has end date
	 */
	private void checkRecurringInfo(boolean isRepetitive, boolean hasStartDate,
			boolean hasEndDate) {
		if (isRepetitive) {
			splitRepeatingInfo();
		}
		checkInvalidDates(isRepetitive, hasStartDate, hasEndDate, startDate, endDate, repeatingType);
	}
	
	//@author A0098077N
	/**
	 * Determine the target date info from the command input
	 * 
	 * @param hasRepetitiveKey
	 *            indicator whether the command has a new repetitive key
	 * @param hasWorkInfoKey
	 *            indicator whether the command has a new work info key
	 * @param hasStartDateKey
	 *            indicator whether the command has a new start date key
	 * @param hasEndDateKey
	 *            indicator whether the command has a new end date key
	 */
	private void determineDateInfoOfTargetTask(boolean hasRepetitiveKey,
			boolean hasWorkInfoKey, boolean hasStartDateKey,
			boolean hasEndDateKey) {
		if (!hasRepetitiveKey && !hasWorkInfoKey) {
			repeatingType = editedTask.getTag().getRepetition();
		}
		
		if (hasStartDateKey) {
			startDate = new CustomDate(startDateString);
		} else {
			startDate = editedTask.getStartDate();
		}
		
		if (hasEndDateKey) {
			endDate = new CustomDate(endDateString);
			updateTimeForEndDate(startDate, endDate);
		} else {
			endDate = editedTask.getEndDate();
		}
		
		if(hasStartDateKey || hasEndDateKey || hasRepetitiveKey){
			editedTask.setCurrentOccurrence(1);
		} 
		
		if(hasRepetitiveKey){
			editedTask.setNumOccurrences(0);
		}
		if(hasWorkInfoKey){
			editedTask.setNumOccurrences(0);
			editedTask.setCurrentOccurrence(1);
		}
	}
	
	/**
	 * This function is used to modify the tag for the editedTask
	 */
	private void setTag() {
		if (!tag.equals(Common.NULL)) {
			editedTask.setTag(new Tag(tag, repeatingType));
			if (tag.equals(Common.HASH_TAG)) {
				editedTask.getTag().setTag(Common.HYPHEN);
			}
		} else {
			editedTask.setTag(new Tag(editedTask.getTag().getTag(), repeatingType));
		}
	}
	
	//@author A0100927M
	/**
	 * Memorize the initial state of the edited task
	 */
	private void setOriginalTask() {
		originalTask = new Task();
		originalTask.setIsImportant(editedTask.isImportantTask());
		originalTask.setStartDate(editedTask.getStartDate());
		originalTask.setEndDate(editedTask.getEndDate());
		originalTask.setStartDateString(editedTask.getStartDateString());
		originalTask.setEndDateString(editedTask.getEndDateString());
		originalTask.setWorkInfo(editedTask.getWorkInfo());
		originalTask.setTag(editedTask.getTag());
		originalTask.setIndexId(editedTask.getIndexId());
		originalTask.setLatestModifiedDate(editedTask.getLatestModifiedDate());
		originalTask.setOccurrence(editedTask.getNumOccurrences(), editedTask.getCurrentOccurrence());
	}
	
	/**
	 * Memorize the aimed state of the edited task
	 */
	private void setTargetTask(){
		targetTask = new Task();
		targetTask.setIsImportant(editedTask.isImportantTask());
		targetTask.setStartDate(editedTask.getStartDate());
		targetTask.setEndDate(editedTask.getEndDate());
		targetTask.setStartDateString(editedTask.getStartDateString());
		targetTask.setEndDateString(editedTask.getEndDateString());
		targetTask.setWorkInfo(editedTask.getWorkInfo());
		targetTask.setTag(editedTask.getTag());
		targetTask.setIndexId(editedTask.getIndexId());
		targetTask.setLatestModifiedDate(editedTask.getLatestModifiedDate());
		targetTask.setOccurrence(editedTask.getNumOccurrences(), editedTask.getCurrentOccurrence());
	}
	
	//@author A0105667B
	/**
	 * This function is used to process the repeating info of target task.
	 * Then it updates these infos for the edited task.
	 */
	private void splitRepeatingInfo() {
		updateOccurrences();
		repeatingType = setRepeatingType(repeatingType);
	}
	
	// Update the number of occurrences for the edited task
	private void updateOccurrences() {
		String pattern = "(.*)(\\s+)(\\d+)(\\s+times?.*)";
		if(repeatingType.matches(pattern)) {
			int num = Integer.valueOf(repeatingType.replaceAll(pattern,"$3"));
			editedTask.setNumOccurrences(num);
			repeatingType = repeatingType.replaceAll(pattern, "$1");

		} else{
			
		}
	}
	
	//@author A0100927M
	/**
	 * Undo EDIT command
	 */
	public String undo() {
		editedTask.setIsImportant(originalTask.isImportantTask());
		editedTask.setStartDate(originalTask.getStartDate());
		editedTask.setEndDate(originalTask.getEndDate());
		editedTask.setStartDateString(originalTask.getStartDateString());
		editedTask.setEndDateString(originalTask.getEndDateString());
		editedTask.setWorkInfo(originalTask.getWorkInfo());
		editedTask.setTag(originalTask.getTag());
		editedTask.setIndexId(originalTask.getIndexId());
		editedTask.setLatestModifiedDate(originalTask.getLatestModifiedDate());
		editedTask.setOccurrence(originalTask.getNumOccurrences(), originalTask.getCurrentOccurrence());
		editedTask.updateLatestModifiedDate();
		Common.sortList(modifiedList);

		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	//@author A0098077N
	/**
	 * Redo EDIT command
	 */
	public String redo() {
		editedTask.setIsImportant(targetTask.isImportantTask());
		editedTask.setStartDate(targetTask.getStartDate());
		editedTask.setEndDate(targetTask.getEndDate());
		editedTask.setStartDateString(targetTask.getStartDateString());
		editedTask.setEndDateString(targetTask.getEndDateString());
		editedTask.setWorkInfo(targetTask.getWorkInfo());
		editedTask.setTag(targetTask.getTag());
		editedTask.setIndexId(targetTask.getIndexId());
		editedTask.setLatestModifiedDate(targetTask.getLatestModifiedDate());
		editedTask.setOccurrence(targetTask.getNumOccurrences(), targetTask.getCurrentOccurrence());
		editedTask.updateLatestModifiedDate();
		Common.sortList(modifiedList);
		
		return Common.MESSAGE_SUCCESSFUL_REDO;
	}
}

/*****************************Subclass of IndexCommand************************************************/
//@author A0105667B
/**
 * Class RemoveCommand. This class executes a command to remove a list of given indices by the user.
 * 
 */
class RemoveCommand extends IndexCommand {
	// List of removed tasks
	private ArrayList<Task> removedTaskInfo;
	
	/**
	 * Constructor of this class
	 * 
	 * @param parsedUserCommand
	 *            the array of indices in string format from the command parsed
	 *            by Parser clas
	 * @param model
	 *            model of tasks in the application
	 * @param tabIndex
	 *            the current tab
	 */
	public RemoveCommand(String[] parsedUserCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		removedTaskInfo = new ArrayList<Task>();
		modifiedList = getModifiedList(tabIndex);
		indexCount = parsedUserCommand.length;
		getListOfIndices(parsedUserCommand);
	}
	
	/**
	 * Execute the REMOVE command
	 */
	public String execute() {
		Arrays.sort(indexList);
		checkValidIndexes();
		processRemove();
		sortInvolvedLists();
		return Common.MESSAGE_SUCCESSFUL_REMOVE;
	}
	
	/**
	 * This is the main function for the removing process
	 */
	private void processRemove(){
		for (int i = indexCount - 1; i >= 0; i--) {
			int removedIndex = convertIndex(indexList[i] - 1);
			Task removedTask = modifiedList.get(removedIndex);
			removedTaskInfo.add(removedTask);
			model.removeTask(removedIndex, tabIndex);
			modifyStatus(removedTask);
		}
	}
	
	/**
	 * This function sort all lists involved in the removing process
	 */
	private void sortInvolvedLists(){
		if (isPendingTab()) {
			Common.sortList(model.getPendingList());
		} else if (isCompleteTab()) {
			Common.sortList(model.getCompleteList());
		}
		
		Common.sortList(model.getTrashList());
	}
	
	//@author A0100927M
	/**
	 * Undo the REMOVE command
	 */
	public String undo() {
		for (int i = 0; i < removedTaskInfo.size(); i++) {
			Task removedTask = removedTaskInfo.get(i);
			reverseStatus(removedTask);
			modifiedList.add(removedTask);
			removeTaskFromTrash(i);
		}
		
		removedTaskInfo.clear();
		Common.sortList(modifiedList);

		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}

	private void removeTaskFromTrash(int i) {
		if (isPendingTab() || isCompleteTab()) {
			int index = model.getIndexFromTrash(removedTaskInfo.get(i));
			model.removeTask(index, Common.TRASH_TAB);
		}
	}
	
	//@author A0098077N
	/**
	 * Redo the REMOVE command
	 */
	public String redo(){
		processRemove();
		sortInvolvedLists();

		return Common.MESSAGE_SUCCESSFUL_REDO;
	}
	
	/**
	 * This function is used to modify the status of a task when the a certain
	 * command is working on it
	 * 
	 * @param modifiedTask
	 *            the task which will have it status changed
	 */
	protected void modifyStatus(Task modifiedTask){
		if (isPendingTab() && modifiedTask.hasNewlyAddedStatus()) {
			modifiedTask.setStatus(Task.Status.UNCHANGED);
		} else if (isPendingTab() && modifiedTask.hasUnchangedStatus()) {
			modifyStatusForUnchangedTask(modifiedTask);
		}
	}

	private void modifyStatusForUnchangedTask(Task modifiedTask) {
		if (Control.syncThread == null || !Control.syncThread.isRunning()) {
			modifiedTask.setStatus(Task.Status.DELETED);
		} else {
			modifiedTask.setStatus(Task.Status.DELETED_WHEN_SYNC);
		}
	}
	
	/**
	 * This function is used to reverse the status of a task when a certain
	 * command is working on it
	 * 
	 * @param reversedTask
	 *            the task which will have its status reversed
	 */
	protected void reverseStatus(Task reversedTask){
		if (isPendingTab() && reversedTask.hasUnchangedStatus()) {
			reverseStatusForUnchangedTask(reversedTask);
		} else if (isPendingTab() && reversedTask.hasDeletedStatus()) {
			reversedTask.setStatus(Task.Status.UNCHANGED);
		}
	}

	private void reverseStatusForUnchangedTask(Task reversedTask) {
		if (Control.syncThread == null || !Control.syncThread.isRunning()) {
			reversedTask.setStatus(Task.Status.NEWLY_ADDED);
		} else {
			reversedTask.setStatus(Task.Status.ADDED_WHEN_SYNC);
		}
	}
}

//@author A0100927M
/**
 * 
 * Class ClearAllCommand. This class executes command to clear all tasks in a list.
 * 
 */
class ClearAllCommand extends IndexCommand {
	// List of tasks which will be cleared
	private Task[] clearedTasks;
	// List of original tasks in trash
	private Task[] originalTrashTasks;
	
	/**
	 * Constructor
	 * 
	 * @param model
	 *            model of tasks in the application
	 * @param tabIndex
	 *            the current tab
	 */
	public ClearAllCommand(Model model, int tabIndex) {
		super(model, tabIndex);
	}
	
	/**
	 * Execute the CLEAR command
	 */
	public String execute() {
		setOriginalTasksInTrash();
		setModifiedList();
		processClear();
		return Common.MESSAGE_SUCCESSFUL_CLEAR_ALL;
	}
	
	/**
	 * This function determine which will be the modified list for this command
	 */
	private void setModifiedList() {
		if (isSearchedResults()) {
			modifiedList = getSearchList(tabIndex);
		} else {
			modifiedList = getModifiedList(tabIndex);
		}
	}
	
	/**
	 * This function saves the original state list of tasks in trash list
	 */
	private void setOriginalTasksInTrash() {
		originalTrashTasks = new Task[model.getTrashList().size()];
		for (int i = 0; i < model.getTrashList().size(); i++) {
			originalTrashTasks[i] = model.getTaskFromTrash(i);
		}
	}

	/**
	 * This is the main function for the clearing process
	 */
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
	
	/**
	 * Undo CLEAR command
	 */
	public String undo() {
		if (isPendingTab()) {
			recoverTasksForPendingTab();
		} else if (isCompleteTab()) {
			recoverTasksForCompleteTab();
		}
		
		resetTasksInTrash();
		Common.sortList(model.getTrashList());
		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}

	/**
	 * This function resets the list of tasks in trash tab back to its original state
	 */
	private void resetTasksInTrash() {
		model.getTrashList().clear();
		for (int i = 0; i < originalTrashTasks.length; i++) {
			model.addTaskToTrash(originalTrashTasks[i]);
		}
	}
	
	/**
	 * This function recover cleared tasks for complete tab
	 */
	private void recoverTasksForCompleteTab() {
		for (int i = 0; i < clearedTasks.length; i++) {
			model.addTaskToComplete(clearedTasks[i]);
		}
		Common.sortList(model.getCompleteList());
	}
	
	/**
	 * This function recover cleared tasks for pending tab
	 */
	private void recoverTasksForPendingTab() {
		for (int i = 0; i < clearedTasks.length; i++) {
			model.addTaskToPending(clearedTasks[i]);
			reverseStatus(clearedTasks[i]);
		}
		Common.sortList(model.getPendingList());
	}
	
	//@author A0098077N
	/**
	 * Redo CLEAR command
	 */
	public String redo(){
		processClear();
		return Common.MESSAGE_SUCCESSFUL_REDO;
	}
	
	/**
	 * This function is used to modify the status of a task when the a certain
	 * command is working on it
	 * 
	 * @param modifiedTask
	 *            the task which will have it status changed
	 */
	protected void modifyStatus(Task modifiedTask){
		if (isPendingTab() && modifiedTask.hasNewlyAddedStatus()) {
			modifiedTask.setStatus(Task.Status.UNCHANGED);
		} else if (isPendingTab() && modifiedTask.hasUnchangedStatus()) {
			modifyStatusForUnchangedTask(modifiedTask);
		}
	}

	private void modifyStatusForUnchangedTask(Task modifiedTask) {
		if (Control.syncThread == null || !Control.syncThread.isRunning()) {
			modifiedTask.setStatus(Task.Status.DELETED);
		} else {
			modifiedTask.setStatus(Task.Status.DELETED_WHEN_SYNC);
		}
	}
	
	/**
	 * This function is used to reverse the status of a task when a certain
	 * command is working on it
	 * 
	 * @param reversedTask
	 *            the task which will have its status reversed
	 */
	protected void reverseStatus(Task reversedTask){
		if (isPendingTab() && reversedTask.hasUnchangedStatus()) {
			reverseStatusForUnchangedTask(reversedTask);
		} else if (isPendingTab() && reversedTask.hasDeletedStatus()) {
			reversedTask.setStatus(Task.Status.UNCHANGED);
		}
	}

	private void reverseStatusForUnchangedTask(Task reversedTask) {
		if (Control.syncThread == null || !Control.syncThread.isRunning()) {
			reversedTask.setStatus(Task.Status.NEWLY_ADDED);
		} else {
			reversedTask.setStatus(Task.Status.ADDED_WHEN_SYNC);
		}
	}
}

//@author A0100927M
/**
 * 
 * Class CompleteCommand. This class executes command to complete a list of given indices.
 * 
 */
class CompleteCommand extends IndexCommand {
	// List of tasks to complete
	private Task[] toCompleteTasks;
	// Indices of these tasks in the complete list
	private int[] indexInCompleteList;
	
	/**
	 * Constructor of this class
	 * 
	 * @param parsedUserCommand
	 *            the array of indices from the command parsed by Parser class
	 * @param model
	 *            model of tasks in the application
	 * @param tabIndex
	 *            the current tab
	 */
	public CompleteCommand(String[] parsedUserCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		modifiedList = this.model.getPendingList();
		indexCount = parsedUserCommand.length;
		indexList = new int[indexCount];
		indexInCompleteList = new int[indexCount];
		toCompleteTasks = new Task[indexCount];
		getListOfIndices(parsedUserCommand);
	}
	
	/**
	 * Execute COMPLETE command
	 */
	public String execute() {
		Arrays.sort(indexList);
		checkSuitableTab();
		checkValidIndexes();
		processComplete();
		retrieveIndexesAfterProcessing();

		return Common.MESSAGE_SUCCESSFUL_COMPLETE;
	}
		
	/**
	 * This is the main function for the completing process
	 */
	private void processComplete(){
		for (int i = indexCount - 1; i >= 0; i--) {
			int completeIndex = convertIndex(indexList[i] - 1);
			Task toComplete = model.getTaskFromPending(completeIndex);
			toCompleteTasks[i] = toComplete;
			modifyStatus(toComplete);
			model.completeTaskFromPending(completeIndex);
		}
		sortInvolvedLists();
	}
	
	//@author A0098077N
	/**
	 * Sort the 2 lists which are involved in this process
	 */
	private void sortInvolvedLists() {
		Common.sortList(model.getPendingList());
		Common.sortList(model.getCompleteList());
	}
	
	//@author A0100927M
	/**
	 * Get the indices of these tasks after they move to the complete list
	 */
	private void retrieveIndexesAfterProcessing(){
		for (int i = 0; i < indexCount; i++) {
			indexInCompleteList[i] = model.getIndexFromComplete(toCompleteTasks[i]);
		}
		Arrays.sort(indexInCompleteList);
	}
	
	//@author A0098077N
	/**
	 * Check whether the current tab is suitable for the COMPLETE command
	 */
	private void checkSuitableTab(){
		if (tabIndex != Common.PENDING_TAB) {
			throw new IllegalArgumentException(Common.MESSAGE_WRONG_COMPLETE_TABS);
		}
	}
	
	//@author A0100927M
	/**
	 * Undo COMPLETE command
	 */
	public String undo() {
		for (int i = indexCount - 1; i >= 0; i--) {
			Task toPending = model.getTaskFromComplete(indexInCompleteList[i]);
			reverseStatus(toPending);
			model.removeTaskFromCompleteNoTrash(indexInCompleteList[i]);
			model.addTaskToPending(toPending);
		}
		sortInvolvedLists();

		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	//@author A0098077N
	/**
	 * Redo COMPLETE command
	 */
	public String redo(){
		processComplete();
		retrieveIndexesAfterProcessing();

		return Common.MESSAGE_SUCCESSFUL_REDO;
	}
	
	/**
	 * This function is used to modify the status of a task when the a certain
	 * command is working on it
	 * 
	 * @param modifiedTask
	 *            the task which will have it status changed
	 */
	protected void modifyStatus(Task modifiedTask){
		if (isPendingTab() && modifiedTask.hasNewlyAddedStatus()) {
			modifiedTask.setStatus(Task.Status.UNCHANGED);
		} else if (isPendingTab() && modifiedTask.hasUnchangedStatus()) {
			modifyStatusForUnchangedTask(modifiedTask);
		}
	}

	private void modifyStatusForUnchangedTask(Task modifiedTask) {
		if (Control.syncThread == null || !Control.syncThread.isRunning()) {
			modifiedTask.setStatus(Task.Status.DELETED);
		} else {
			modifiedTask.setStatus(Task.Status.DELETED_WHEN_SYNC);
		}
	}
	
	/**
	 * This function is used to reverse the status of a task when a certain
	 * command is working on it
	 * 
	 * @param reversedTask
	 *            the task which will have its status reversed
	 */
	protected void reverseStatus(Task reversedTask){
		if (isPendingTab() && reversedTask.hasUnchangedStatus()) {
			reverseStatusForUnchangedTask(reversedTask);
		} else if (isPendingTab() && reversedTask.hasDeletedStatus()) {
			reversedTask.setStatus(Task.Status.UNCHANGED);
		}
	}

	private void reverseStatusForUnchangedTask(Task reversedTask) {
		if (Control.syncThread == null || !Control.syncThread.isRunning()) {
			reversedTask.setStatus(Task.Status.NEWLY_ADDED);
		} else {
			reversedTask.setStatus(Task.Status.ADDED_WHEN_SYNC);
		}
	}
}

//@author A0100927M
/**
 * 
 * Class IncompleteCommand
 * 
 */
class IncompleteCommand extends IndexCommand {
	// List of tasks to be incompleted
	private Task[] toIncompleteTasks;
	// Indices of these tasks in pending list
	private int[] indexInIncompleteList;

	/**
	 * Constructor of this class
	 * 
	 * @param parsedUserCommand
	 *            array of indices from the command parsed by Parser class
	 * @param model
	 *            model of tasks in the application
	 * @param tabIndex
	 *            the current tab
	 */
	public IncompleteCommand(String[] parsedUserCommand, Model model,
			int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		modifiedList = this.model.getCompleteList();
		indexCount = parsedUserCommand.length;
		indexInIncompleteList = new int[indexCount];
		toIncompleteTasks = new Task[indexCount];

		getListOfIndices(parsedUserCommand);
	}

	/**
	 * Execute INCOMPLETE command
	 */
	public String execute() {
		Arrays.sort(indexList);
		checkSuitableTab();
		checkValidIndexes();
		
		processIncomplete();
		retrieveIndexesAfterProcessing();
		
		return Common.MESSAGE_SUCCESSFUL_INCOMPLETE;
	}
	
	/** 
	 * This is the main function for incompleting process
	 */
	private void processIncomplete(){
		for (int i = indexCount - 1; i >= 0; i--) {
			int incompleteIndex = convertIndex(indexList[i] - 1);
			Task toPending = model.getTaskFromComplete(incompleteIndex);
			modifyStatus(toPending);
			toIncompleteTasks[i] = toPending;
			model.getCompleteList().remove(incompleteIndex);
			model.addTaskToPending(toPending);
		}
		sortInvolvedLists();
	}
	
	//@author A0098077N
	/**
	 * This function is used to sort the involved lists in the process
	 */
	private void sortInvolvedLists() {
		Common.sortList(model.getPendingList());
		Common.sortList(model.getCompleteList());
	}
	
	//@author A0100927M
	/**
	 * This function is used to get the indices of these tasks in pending list after processing
	 */
	private void retrieveIndexesAfterProcessing(){
		for (int i = 0; i < indexCount; i++) {
			indexInIncompleteList[i] = model
					.getIndexFromPending(toIncompleteTasks[i]);
		}
		Arrays.sort(indexInIncompleteList);
	}
	
	//@author A0098077N
	/**
	 * Check whether the current tab is suitable for executing INCOMPLETE commmand
	 */
	private void checkSuitableTab(){
		if (tabIndex != Common.COMPLETE_TAB) {
			throw new IllegalArgumentException(Common.MESSAGE_WRONG_INCOMPLETE_TABS);
		}
	}
	
	//@author A0100927M
	/**
	 * Undo for INCOMPLETE command
	 */
	public String undo() {
		for (int i = indexCount - 1; i >= 0; i--) {
			Task toComplete = model.getTaskFromPending(indexInIncompleteList[i]);
			reverseStatus(toComplete);
			toIncompleteTasks[i] = toComplete;
			model.getPendingList().remove(indexInIncompleteList[i]);
			model.addTaskToComplete(toComplete);
		}
		sortInvolvedLists();
		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	//@author A0098077N
	/**
	 * Redo for INCOMPLETE command
	 */
	public String redo(){
		processIncomplete();
		retrieveIndexesAfterProcessing();
		
		return Common.MESSAGE_SUCCESSFUL_REDO;
	}
	
	/**
	 * This function is used to modify the status of a task when the a certain
	 * command is working on it
	 * 
	 * @param modifiedTask
	 *            the task which will have it status changed
	 */
	protected void modifyStatus(Task modifiedTask){
		if (isCompleteTab() && modifiedTask.hasUnchangedStatus()) {
			modifyStatusForUnchangedTask(modifiedTask);
		} else if (isCompleteTab() && modifiedTask.hasDeletedStatus()) {
			modifiedTask.setStatus(Task.Status.UNCHANGED);
		}
	}

	private void modifyStatusForUnchangedTask(Task modifiedTask) {
		if (Control.syncThread == null || !Control.syncThread.isRunning()) {
			modifiedTask.setStatus(Task.Status.NEWLY_ADDED);
		} else {
			modifiedTask.setStatus(Task.Status.ADDED_WHEN_SYNC);
		}
	}
	
	/**
	 * This function is used to reverse the status of a task when a certain
	 * command is working on it
	 * 
	 * @param reversedTask
	 *            the task which will have its status reversed
	 */
	protected void reverseStatus(Task reversedTask){
		if (isCompleteTab() && reversedTask.hasNewlyAddedStatus()) {
			reversedTask.setStatus(Task.Status.UNCHANGED);
		} else if (isCompleteTab() && reversedTask.hasUnchangedStatus()) {
			reverseStatusForUnchangedTask(reversedTask);
		}
	}

	private void reverseStatusForUnchangedTask(Task reversedTask) {
		if (Control.syncThread == null || !Control.syncThread.isRunning()) {
			reversedTask.setStatus(Task.Status.DELETED);
		} else {
			reversedTask.setStatus(Task.Status.DELETED_WHEN_SYNC);
		}
	}
}

//@author A0098077N
/**
 * 
 * Class RecoverCommand. This class executes command to recover tasks in trash list.
 * 
 */
class RecoverCommand extends IndexCommand {
	// List of tasks to be recovered
	private Task[] toRecoverTasks;
	// Indices of these tasks in the pending list
	private int[] indexInPendingList;

	/**
	 * Constructor of this class
	 * 
	 * @param parsedUserCommand
	 *            array of indices from the command parsed by Parser class
	 * @param model
	 *            model of tasks in the application
	 * @param tabIndex
	 *            the current tab
	 */
	public RecoverCommand(String[] parsedUserCommand, Model model,
			int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		modifiedList = this.model.getTrashList();
		indexCount = parsedUserCommand.length;
		indexInPendingList = new int[indexCount];
		toRecoverTasks = new Task[indexCount];

		getListOfIndices(parsedUserCommand);
	}
	
	/**
	 * Execute RECOVER command
	 */
	public String execute() {
		Arrays.sort(indexList);
		checkSuitableTab();
		checkValidIndexes();
		processRecover();
		retrieveIndexesAfterProcessing();
		
		return Common.MESSAGE_SUCCESSFUL_RECOVER;
	}
	
	/**
	 * This is the main function for recovering process
	 */
	private void processRecover(){
		for (int i = indexCount - 1; i >= 0; i--) {
			int recoverIndex = convertIndex(indexList[i] - 1);
			Task toPending = model.getTaskFromTrash(recoverIndex);
			modifyStatus(toPending);
			toRecoverTasks[i] = toPending;
			model.getTrashList().remove(recoverIndex);
			model.addTaskToPending(toPending);
		}
		sortInvolvedLists();
	}
	
	/**
	 * This function is used to sort involved lists in the process
	 */
	private void sortInvolvedLists() {
		Common.sortList(model.getPendingList());
		Common.sortList(model.getTrashList());
	}
	
	/**
	 * This function is used to get the indices of these tasks in pending list after processing
	 */
	private void retrieveIndexesAfterProcessing(){
		for (int i = 0; i < indexCount; i++) {
			indexInPendingList[i] = model
					.getIndexFromPending(toRecoverTasks[i]);
		}
		Arrays.sort(indexInPendingList);
	}
	
	/**
	 * Check whether the current tab is suitable for executing RECOVER command
	 */
	private void checkSuitableTab(){
		if (tabIndex != Common.TRASH_TAB) {
			throw new IllegalArgumentException(Common.MESSAGE_WRONG_RECOVER_TABS);
		}
	}
	
	/**
	 * Undo the RECOVER command
	 */
	public String undo() {
		for (int i = indexCount - 1; i >= 0; i--) {
			Task toTrash = model.getTaskFromPending(indexInPendingList[i]);
			reverseStatus(toTrash);
			toRecoverTasks[i] = toTrash;
			model.getPendingList().remove(indexInPendingList[i]);
			model.addTaskToTrash(toTrash);
		}
		sortInvolvedLists();

		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	/** 
	 * Redo the RECOVER command
	 */
	public String redo(){
		processRecover();
		retrieveIndexesAfterProcessing();
		
		return Common.MESSAGE_SUCCESSFUL_REDO;
	}
	
	/**
	 * This function is used to modify the status of a task when the a certain
	 * command is working on it
	 * 
	 * @param modifiedTask
	 *            the task which will have it status changed
	 */
	protected void modifyStatus(Task modifiedTask){
		if (isTrashTab() && modifiedTask.hasUnchangedStatus()) {
			modifyStatusForUnchangedTask(modifiedTask);
		} else if (isTrashTab() && modifiedTask.hasDeletedStatus()) {
			modifiedTask.setStatus(Task.Status.UNCHANGED);
		}
	}

	private void modifyStatusForUnchangedTask(Task modifiedTask) {
		if (Control.syncThread == null || !Control.syncThread.isRunning()) {
			modifiedTask.setStatus(Task.Status.NEWLY_ADDED);
		} else {
			modifiedTask.setStatus(Task.Status.ADDED_WHEN_SYNC);
		}
	}
	
	/**
	 * This function is used to reverse the status of a task when a certain
	 * command is working on it
	 * 
	 * @param reversedTask
	 *            the task which will have its status reversed
	 */
	protected void reverseStatus(Task reversedTask){
		if (isTrashTab() && reversedTask.hasNewlyAddedStatus()) {
			reversedTask.setStatus(Task.Status.UNCHANGED);
		} else if (isTrashTab() && reversedTask.hasUnchangedStatus()) {
			reverseStatusForUnchangedTask(reversedTask);
		}
	}

	private void reverseStatusForUnchangedTask(Task reversedTask) {
		if (Control.syncThread == null || !Control.syncThread.isRunning()) {
			reversedTask.setStatus(Task.Status.DELETED);
		} else {
			reversedTask.setStatus(Task.Status.DELETED_WHEN_SYNC);
		}
	}
}

//@author A0100927M
/**
 * 
 * Class MarkCommand. This class executes command to mark a list of indices as important
 * 
 */
class MarkCommand extends IndexCommand {
	
	/**
	 * Constructor of this class
	 * 
	 * @param parsedUserCommand
	 *            array of indices from the command parsed by Parser class
	 * @param model
	 *            model of tasks in the application
	 * @param tabIndex
	 *            the current tab
	 */
	public MarkCommand(String[] parsedUserCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		modifiedList = getModifiedList(tabIndex);
		indexCount = parsedUserCommand.length;
		getListOfIndices(parsedUserCommand);
	}

	/**
	 * Execute MARK command
	 */
	public String execute() {
		Arrays.sort(indexList);
		checkValidIndexes();
		for (int i = 0; i < indexCount; i++) {
			int markIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(markIndex);
			targetTask.setIsImportant(true);
			targetTask.updateLatestModifiedDate();
		}

		return Common.MESSAGE_SUCCESSFUL_MARK;
	}
	
	/**
	 * Undo MARK command
	 */
	public String undo() {
		for (int i = 0; i < indexCount; i++) {
			int unmarkIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(unmarkIndex);
			targetTask.setIsImportant(false);
			targetTask.updateLatestModifiedDate();
		}
		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	//@author A0098077N
	/**
	 * Redo MARK command
	 */
	public String redo(){
		for (int i = 0; i < indexCount; i++) {
			int markIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(markIndex);
			targetTask.setIsImportant(true);
			targetTask.updateLatestModifiedDate();
		}

		return Common.MESSAGE_SUCCESSFUL_REDO;
	}
}

//@author A0100927M
/**
 * 
 * Class UnmarkCommand. This class executes command to unmark a list of indices in the list
 * 
 */
class UnmarkCommand extends IndexCommand {
	/**
	 * Constructor of this class
	 * 
	 * @param parsedUserCommand
	 *            array of indices from the command parsed by Parser class
	 * @param model
	 *            model of tasks in the application
	 * @param tabIndex
	 *            the current tab
	 */
	public UnmarkCommand(String[] parsedUserCommand, Model model, int tabIndex) {
		super(model, tabIndex);
		assert parsedUserCommand != null;
		modifiedList = getModifiedList(tabIndex);
		indexCount = parsedUserCommand.length;
		getListOfIndices(parsedUserCommand);
	}

	/**
	 * Execute UNMARK command
	 */
	public String execute() {
		Arrays.sort(indexList);
		checkValidIndexes();
		for (int i = 0; i < indexCount; i++) {
			int unmarkIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(unmarkIndex);
			targetTask.setIsImportant(false);
			targetTask.updateLatestModifiedDate();
		}

		return Common.MESSAGE_SUCCESSFUL_UNMARK;
	}
	
	/**
	 * Undo UNMARK command
	 */
	public String undo() {
		for (int i = 0; i < indexCount; i++) {
			int markIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(markIndex);
			targetTask.setIsImportant(true);
			targetTask.updateLatestModifiedDate();
		}
		return Common.MESSAGE_SUCCESSFUL_UNDO;
	}
	
	//@author A0098077N
	/**
	 * Redo UNMARK command
	 */
	public String redo(){
		for (int i = 0; i < indexCount; i++) {
			int unmarkIndex = convertIndex(indexList[i] - 1);
			Task targetTask = modifiedList.get(unmarkIndex);
			targetTask.setIsImportant(false);
			targetTask.updateLatestModifiedDate();
		}

		return Common.MESSAGE_SUCCESSFUL_REDO;
	}
}

//@author A0098077N
/**********************************Subclass of Command**********************************************/

/**
 * 
 * Class SearchCommand. This class executes command to search for list of tasks with required infos.
 * 
 */
class SearchCommand extends Command {
	// The searched work info
	private String workInfo;
	// The searched tag
	private String tag;
	// The searched start date string
	private String startDateString;
	// The searched end date string
	private String endDateString;
	// The searched repeating type
	private String repeatingType;
	private int numOccurrences = 0;
	// Search for important task
	private String isImpt;
	// The main interface of the application
	private View view;
	// The initial list for searching
	private ObservableList<Task> initialList;
	// The latest list of searched results
	private ObservableList<Task> searchList;
	// The searched start and end dates
	private CustomDate startDate, endDate;
	// Indicator if this is the first field searched
	private boolean isFirstTimeSearch;
	// Indicator if this is currently under real time search
	private boolean isRealTimeSearch;
	
	/**
	 * 
	 * @param parsedUserCommand
	 * @param model
	 * @param view
	 * @param isRealTimeSearch
	 */
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
		
		isFirstTimeSearch = true;
	}
	
	/**
	 * Execute the SEARCH command
	 */
	public String execute() {
		processSearch();
		if (!isRealTimeSearch && searchList.isEmpty()) {
			return Common.MESSAGE_NO_RESULTS;
		}
		TwoWayCommand.setIndexType(TwoWayCommand.SEARCHED);
		updateSearchResults();
		return Common.MESSAGE_SUCCESSFUL_SEARCH;
	}
	
	/** 
	 * This function is used to update the search results on the GUI
	 */
	private void updateSearchResults() {
		if (isPendingTab()) {
			model.setSearchPendingList(searchList);
			view.getPendingTable().setItems(model.getSearchPendingList());
		} else if (isCompleteTab()) {
			model.setSearchCompleteList(searchList);
			view.getCompleteTable().setItems(model.getSearchCompleteList());
		} else {
			model.setSearchTrashList(searchList);
			view.getTrashTable().setItems(model.getSearchTrashList());
		}
	}
	
	/**
	 * This is the main function for searching process
	 */
	public void processSearch() {
		processWorkInfo();
		processTag();
		processStartDate();
		processEndDate();
		processIsImportant();
		processRepeatingType();
		processNumOccurrences();
	}
	
	/**
	 * This function is used to determine the type of repetition and number of
	 * occurrences for search info
	 */
	private void splitRepeatingInfo() {
		setOccurrences();
		repeatingType = setRepeatingType(repeatingType);
	}
	
	/**
	 * Set the searched number of occurrences
	 */
	private void setOccurrences() {
		if (repeatingType.matches(OCCURRENCE_PATTERN)) {
			int num = Integer.valueOf(repeatingType.replaceAll(
					OCCURRENCE_PATTERN, "$3"));
			numOccurrences = num;
			repeatingType = repeatingType.replaceAll(OCCURRENCE_PATTERN, "$1");
		} else {
			numOccurrences = 0;
		}
	}
	
	// Process searching for start date
	private void processStartDate() {
		if (!startDateString.equals(Common.NULL)) {
			startDate = new CustomDate(startDateString);
			if (isFirstTimeSearch) {
				searchList = searchStartDate(initialList, startDate);
			} else {
				searchList = searchStartDate(searchList, startDate);
			}
			isFirstTimeSearch = false;
		}
	}
	
	// Process searching for end date
	private void processEndDate() {
		if (!endDateString.equals(Common.NULL)) {
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

			isFirstTimeSearch = false;
		}
	}
	
	//Process searching for important task
	private void processIsImportant(){
		if (isImpt.equals(Common.TRUE)) {
			if (isFirstTimeSearch) {
				searchList = searchImportantTask(initialList);
			} else {
				searchList = searchImportantTask(searchList);
			}
			isFirstTimeSearch = false;
		}
	}
	
	// Process searching for number of occurrences
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
	
	// Process searching for type of repetition
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
	
	// Process searching for tag
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
	
	// Process searching for task info
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
	
	/**
	 * This function is used to return the results of task with requested number
	 * of occurrences
	 * 
	 * @param list
	 *            the searched list
	 * @param occurNum
	 *            the request number of occurences
	 * @return the result list
	 */
	private static ObservableList<Task> searchOccurrenceNum(ObservableList<Task> list, int occurNum) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getNumOccurrences() == occurNum) {
				result.add(list.get(i));
			}
		}
		return result;
	}
	
	/**
	 * This function is used to return the results of important tasks
	 * 
	 * @param list
	 *            the searched list
	 * @return the result list
	 */
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

	/**
	 * This function is used to return the results of task with requested tag
	 * 
	 * @param list
	 *            the searched list
	 * @param tagName
	 *            the requested tag
	 * @return the result list
	 */
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
	
	/**
	 * This function is used to return the results of task with requested type
	 * of repetition
	 * 
	 * @param list
	 *            the searched list
	 * @param repeatingType
	 *            the requested type of repetition
	 * @return the result list
	 */
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
	
	/**
	 * This function is used to return the results of task with requested start
	 * date
	 * 
	 * @param list
	 *            the searched list
	 * @param date
	 *            the requested date
	 * @return the result list
	 */
	private static ObservableList<Task> searchStartDate(
			ObservableList<Task> list, CustomDate date) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		boolean hasIndicatedTime = date.getHour() != 0 || date.getMinute() != 0;
		if (hasIndicatedTime) {
			searchAtTimeLevelForStartDate(list, date, result);
		} else {
			searchAtDateLevelForStartDate(list, date, result);
		}
		return result;
	}
	
	// Search by comparing time
	private static void searchAtDateLevelForStartDate(
			ObservableList<Task> list, CustomDate date,
			ObservableList<Task> result) {
		for (int i = 0; i < list.size(); i++) {
			CustomDate startDate = list.get(i).getStartDate();
			if (startDate != null && CustomDate.dateCompare(startDate, date) >= 0)
				result.add(list.get(i));
		}
	}
	
	// Search by just comparing date
	private static void searchAtTimeLevelForStartDate(
			ObservableList<Task> list, CustomDate date,
			ObservableList<Task> result) {
		for (int i = 0; i < list.size(); i++) {
			CustomDate startDate = list.get(i).getStartDate();
			if (startDate != null
					&& CustomDate.compare(startDate, date) >= 0) {
				result.add(list.get(i));
			}
		}
	}
	
	/**
	 * This function is used to return the results of tasks with requested end
	 * date
	 * 
	 * @param list
	 *            the searched list
	 * @param date
	 *            the requested date
	 * @return the result list
	 */
	private static ObservableList<Task> searchEndDate(ObservableList<Task> list,
			CustomDate date) {
		ObservableList<Task> result = FXCollections.observableArrayList();
		updateDateForEndDate(date);
		
		boolean hasIndicatedTime = date.getHour() != 23 && date.getMinute() != 59;
		if (hasIndicatedTime) {
			searchAtTimeLevelForEndDate(list, date, result);
		} else {
			searchAtDateLevelForEndDate(list, date, result);
		}
		return result;
	}

	// Search by comparing date
	private static void searchAtDateLevelForEndDate(ObservableList<Task> list,
			CustomDate date, ObservableList<Task> result) {
		for (int i = 0; i < list.size(); i++) {
			CustomDate endDate = list.get(i).getEndDate();
			if (endDate != null && CustomDate.dateCompare(endDate, date) <= 0) {
				result.add(list.get(i));
			}
		}
	}
	
	// Search by comparing time
	private static void searchAtTimeLevelForEndDate(ObservableList<Task> list,
			CustomDate date, ObservableList<Task> result) {
		for (int i = 0; i < list.size(); i++) {
			CustomDate endDate = list.get(i).getEndDate();
			if (endDate != null && CustomDate.compare(endDate, date) <= 0) {
				result.add(list.get(i));
			}
		}
	}
	
	// Update time for end date if it is midnight
	private static void updateDateForEndDate(CustomDate date) {
		boolean isMidnight = date.getHour() == 0 && date.getMinute() == 0;
		if (isMidnight) {
			date.setHour(23);
			date.setMinute(59);
		}
	}
	
	/**
	 * This function is used to return the results of task containing requested
	 * work info
	 * 
	 * @param list
	 *            the searched list
	 * @param workInfo
	 *            the requested work info
	 * @return the result list
	 */
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

//@author A0098077N
/**
 * 
 * Class ShowAllCommand. This class executes command to show all tasks in the list
 * 
 */
class ShowAllCommand extends Command {
	// The main interface of the application
	private View view;
	
	/**
	 * Constructor
	 * 
	 * @param model
	 *            model of tasks in the application
	 * @param view
	 *            the interface of the application
	 */
	public ShowAllCommand(Model model, View view) {
		super(model, view.getTabIndex());
		this.view = view;
	}
	
	/**
	 * Execute SHOW command
	 */
	public String execute() {
		TwoWayCommand.setIndexType(TwoWayCommand.SHOWN);
		setContent();
		return Common.MESSAGE_SUCCESSFUL_SHOW_ALL;
	}
	
	/**
	 * Set the content of the corresponding list back to the full list of tasks
	 */
	private void setContent() {
		if (isPendingTab()) {
			view.getPendingTable().setItems(model.getPendingList());
		} else if (isCompleteTab()) {
			view.getCompleteTable().setItems(model.getCompleteList());
		} else {
			view.getTrashTable().setItems(model.getTrashList());
		}
	}
}

//@author A0100927M
/**
 * 
 * Class HelpCommand. This class execute command to show the help window.
 * 
 */
class HelpCommand extends Command {
	// The main interface of the application
	private View view;
	
	/**
	 * Constructor of this class
	 * 
	 * @param model
	 *            model of tasks in application
	 * @param view
	 *            main interface
	 */
	public HelpCommand(Model model, View view) {
		super(model, view.getTabIndex());
		this.view = view;
	}
	
	/**
	 * Execute HELP command
	 */
	public String execute() {
		view.showHelpPage();
		return Common.MESSAGE_SUCCESSFUL_HELP;
	}
}

//@author A0100927M
/**
 * 
 * Class SettingsCommand. This class executes command to show SETTINGS dialog
 * 
 */
class SettingsCommand extends Command {
	// Main interface of the application
	private View view;
	// Indicator what calls the settings command
	private String origin;
	
	/**
	 * Constructor of this class
	 * 
	 * @param model
	 *            model of tasks in the application
	 * @param view
	 *            main interface
	 * @param origin
	 *            indicator what calls this command
	 */
	public SettingsCommand(Model model, View view, String origin) {
		super(model, view.getTabIndex());
		this.view = view;
		this.origin = origin;
	}
	
	/**
	 * Execute SETTINGS command
	 */
	public String execute() {
		view.showSettingsPage(origin);
		return Common.MESSAGE_SUCCESSFUL_SETTINGS;
	}
}


//@author A0105523U
/**
 * 
 * Class SyncCommand. This class executes command to sync with the Google Calendar in a concurrent thread.
 * 
 */
class SyncCommand extends Command implements Runnable {
	// Username of Google Account
	private String username = null;
	// Password of Google Account
	private String password = null;
	// Feedback from the sync
	private String feedback = null;
	// Indicator whether it is under syncing process
	private boolean isRunning = false;
	// The sync class
	private Synchronization sync;
	// Main interface
	private View view;
	// The storage file of tasks
	private Storage taskFile;
	// The thread that will run the syncing process
	private Thread syncingThread;
	
	/**
	 * Constructor of this class
	 * @param model model of tasks in the application
	 * @param sync the sync object
	 * @param view the main interface
	 * @param taskFile the task file to store
	 */
	public SyncCommand(Model model, Synchronization sync, View view, Storage taskFile) {
		super(model);
		this.sync = sync;
		this.view = view;
		this.taskFile = taskFile;
		
		loadAccount(model);
		processSyncing(sync, view);
	}

	/**
	 * This function is the main function for syncing process
	 * 
	 * @param sync
	 *            sync object
	 * @param view
	 *            the main interface
	 */
	private void processSyncing(Synchronization sync, View view) {
		if(checkInternetAccess()){
			try {
				checkAccount(sync);
				sync.isValid = true;
				startSyncing(); 
			} catch (AuthenticationException e) {
				feedback = Common.MESSAGE_SYNC_INVALID_USERNAME_PASSWORD;
				sync.isValid = false;
			}
		} else {
			view.showNoInternetConnection();
		}
	}
	
	// Begin the syncing phase
	private void startSyncing() {
		syncingThread = new Thread(this, "Sync Thread");
		syncingThread.start();
	}
	
	// Check if this google account is valid or not
	private void checkAccount(Synchronization sync)
			throws AuthenticationException {
		if(!sync.isValid || !username.equals(sync.username) || !password.equals(sync.password)){
			sync.setUsernameAndPassword(username, password);
			sync.initService();
		}
	}
	
	// Load the account user input in the application
	private void loadAccount(Model model) {
		username = model.getUsername();
		password = model.getPassword();
	}
	
	/**
	 * This function is used to check whether there is currently internet access in the system or not
	 * @return true if there is indeed internet access, or vice versa
	 */
	private boolean checkInternetAccess(){
            try {
                //URL of Google
                URL url = new URL("http://www.google.com");
                //Open a connection to Google
                HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();
                // Test connection
                urlConnect.getContent();
            } catch (UnknownHostException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
            return true;
    }
	
	/**
	 * The syncing thread running content
	 */
	@Override
	public void run() {
			isRunning = true;
			view.setSyncProgressVisible(true);
			execute();
			view.setSyncProgressVisible(false);
			isRunning = false;
			model.clearSyncInfo();
			storeSyncData();
	}
	
	/**
	 * Store data after finishing synchronization
	 */
	private void storeSyncData() {
		try {
			taskFile.storeToFile();
		} catch (IOException io) {
			System.out.println(io.getMessage());
		}
	}
	
	/**
	 * Execute SYNC command
	 */
	@Override
	public String execute() {
		try{
			sync.execute();
			Common.sortList(model.getPendingList());
		} catch(Exception e){
			if(e instanceof AuthenticationException){
				showSettingsPage();
			}
		}
		return null;
	}
	
	// Show settings window to require reinput google account from the user as it is invalid
	private void showSettingsPage() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				view.showSettingsPage(Common.MESSAGE_SYNC_INVALID_USERNAME_PASSWORD);
			}
		});
	}
	
	// Check whether it is currently under syncing process or not
	public boolean isRunning() {
		return isRunning;
	}
	
	public String getFeedback() {
		return feedback;
	}
}

//@author A0098077N
/**
 * 
 * Class ExitCommand. This class executes command to exit the application
 * 
 */
class ExitCommand extends Command {
	/**
	 * Constructor of this class
	 * 
	 * @param model
	 *            model of tasks in the application
	 * @param tabIndex
	 *            the current tab
	 */
	public ExitCommand(Model model, int tabIndex) {
		super(model, tabIndex);
	}
	
	/**
	 * Execute EXIT command
	 */
	public String execute() {
		System.exit(0);
		return null;
	}
}
