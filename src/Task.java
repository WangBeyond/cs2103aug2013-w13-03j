import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 * 
 * This class is used to store the information of a task
 *
 */
public class Task implements Comparable<Task> {
	private static final String OCCURRENCE_PROPERTY_ID = "occurrenceProperty";
	private static final String END_STRING_PROPERTY_ID = "endDateString";
	private static final String START_STRING_PROPERTY_ID = "startDateString";
	private static final String ENDDATE_PROPERTY_ID = "enddate";
	private static final String STARTDATE_PROPERTY_ID = "startdate";
	private static final String TAG_PROPERTY_ID = "tag";
	private static final String WORKINFO_PROPERTY_ID = "workinfo";
	private static final String ROWSTATUS_PROPERTY_ID = "rowstatus";
	private static final String EMPTY = "-";
	private final static boolean IS_START_DATE = true;
	private final static boolean IS_END_DATE = false;

	// Enumeration Class representing status of a task
	public static enum Status {
		UNCHANGED, NEWLY_ADDED, DELETED, ADDED_WHEN_SYNC, DELETED_WHEN_SYNC
	}

	// Property indicating the status of the row, whether it is the last overdue task in list or important task
	private ObjectProperty<RowStatus> rowStatus;
	
	// Properties of the start date and its string
	private ObjectProperty<CustomDate> startDate;
	private StringProperty startDateString;
	
	// Properties of the end date and its string
	private ObjectProperty<CustomDate> endDate;
	private StringProperty endDateString;
	
	// Property of the work info
	private StringProperty workInfo;
	
	// Property of the tag containing category and repetitive tag
	private ObjectProperty<Tag> tag;
	
	// Property of number of occurrences and its current occurrence of the task
	private int num_occurrences;
	private int current_occurrence;
	private StringProperty occurrenceString;
	
	// Index ID of this task in Google Calendar
	private String indexId;
	
	// Index in the list containing the task
	private int indexInList;
	
	// Current status of the task
	private Status status;
	
	// The latest date when the task was modified
	private CustomDate latestModifiedDate;
	
	//@author A0098077N
	// Default constructor
	public Task() {
		checkProperty();
		defaultInitialization();
	}
	
	/*
	 * Default initialization for all properties 
	 */
	private void defaultInitialization() {
		setRowStatus(new RowStatus(false, false));
		setStartDate(null);
		setStartDateString(EMPTY);
		setEndDate(null);
		setEndDateString(EMPTY);
		setWorkInfo("");
		setTag(new Tag(EMPTY, "null"));
		indexId = "";
		indexInList = 0;
		if(Control.syncThread!=null && Control.syncThread.isRunning()){
			setStatus(Status.ADDED_WHEN_SYNC);
		}else{
			setStatus(Status.NEWLY_ADDED);
		}
		updateLatestModifiedDate();
		initOccurrence(1);
	}

	/**
	 * This function is used to check whether any property has not been
	 * initialized. If there are, it will initialize these properties.
	 */
	private void checkProperty() {
		rowStatusProperty();
		workInfoProperty();
		tagProperty();
		startDateProperty();
		startDateStringProperty();
		endDateStringProperty();
		endDateProperty();
		occurrenceProperty();
	}

	/**
	 * This function is the implemented method for interface Comparable. It is
	 * used to compare this task with another task. The order of comparison is
	 * first end date, start date and work info respectively.
	 */
	public int compareTo(Task other) {
		int compareEndDates = CustomDate.compare(getEndDate(),
				other.getEndDate());
		int compareStartDates = CustomDate.compare(getStartDate(),
				other.getStartDate());

		boolean equalEndDate = (compareEndDates == 0);
		boolean equalStartAndEndDate = (compareEndDates == 0)
				&& (compareStartDates == 0);

		if (equalStartAndEndDate) {
			return getWorkInfo().compareToIgnoreCase(other.getWorkInfo());
		} else if (equalEndDate) {
			return compareStartDates;
		} else {
			return compareEndDates;
		}
	}

	/**
	 * Update the string representing the date for display
	 */
	public void updateDateString() {
		boolean hasStartDate = getStartDate() != null;
		boolean hasEndDate = getEndDate() != null;
		if (hasStartDate) {
			setStartDateString(getStartDate().toString(IS_START_DATE));
		}

		if (hasEndDate) {
			setEndDateString(getEndDate().toString(IS_END_DATE));
		}
	}
	
	//@author A0105667B
	/**
	 * This function is used to update the start time and end time for a
	 * repetitive task when the end time is behind the current time
	 */
	public void updateDateForRepetitiveTask() {
		long difference = CustomDate.getUpdateDistance(getTag()
				.getRepetition());
		while (getEndDate().beforeCurrentTime()) {
			if(current_occurrence == num_occurrences)
				break;
			// Update the occurrences
			current_occurrence++;
			updateOccurrenceString();
			// Update the start date and end date
			updateStartDate(difference);
			updateEndDate(difference);
		}
	}
	
	//@author A0098077N
	/**
	 * Update the start date to new date with given difference in milliseconds
	 * between the new and the old ones
	 * 
	 * @param difference
	 *            time between 2 dates
	 */
	private void updateStartDate(long difference) {
		CustomDate startDate = getStartDate();
		startDate.setTimeInMillis(startDate.getTimeInMillis() + difference);
		setStartDate(startDate);
		setStartDateString(getStartDate().toString(IS_START_DATE));
	}

	/**
	 * Update the end date to new date with given difference in milliseconds
	 * between the new and the old ones
	 * 
	 * @param difference
	 *            time between 2 dates
	 */
	private void updateEndDate(long difference) {
		CustomDate endDate = getEndDate();
		endDate.setTimeInMillis(endDate.getTimeInMillis() + difference);
		setEndDate(endDate);
		setEndDateString(getEndDate().toString(IS_END_DATE));
	}

	/**
	 * This function is used to check whether this task is a repetitive task
	 * 
	 * @return true if this is indeed a recurring task, vice versa
	 */
	public boolean isRecurringTask() {
		return !tag.get().getRepetition().equals(Common.NULL);
	}
	
	/**
	 * This function is used to check whether this task is an overdue task
	 * @return true if this is indeed an overdue task, vice versa
	 */
	public boolean isOverdueTask() {
		if (getEndDate() != null){
			return getEndDate().beforeCurrentTime();
		}
		return false;
	}
	
	//@author A0105667B
	/**
	 * This function is used to update the occurrences of a task for display
	 */
	private void updateOccurrenceString() {
		if (num_occurrences <= 1){
			occurrenceString.set("");
		}else{
			occurrenceString.set(current_occurrence + "/" + num_occurrences);
		}
	}
	
	//@author A0098077N
	/************************ GET Property Functions **********************************/
	public ObjectProperty<RowStatus> rowStatusProperty() {
		if (rowStatus == null) {
			rowStatus = new SimpleObjectProperty<RowStatus>(this, ROWSTATUS_PROPERTY_ID);
		}
		return rowStatus;
	}

	public StringProperty workInfoProperty() {
		if (workInfo == null) {
			workInfo = new SimpleStringProperty(this, WORKINFO_PROPERTY_ID);
		}
		return workInfo;
	}

	public ObjectProperty<Tag> tagProperty() {
		if (tag == null) {
			tag = new SimpleObjectProperty<Tag>(this, TAG_PROPERTY_ID);
		}
		return tag;
	}

	public ObjectProperty<CustomDate> startDateProperty() {
		if (startDate == null) {
			startDate = new SimpleObjectProperty<CustomDate>(this, STARTDATE_PROPERTY_ID);
		}
		return startDate;
	}

	public ObjectProperty<CustomDate> endDateProperty() {
		if (endDate == null) {
			endDate = new SimpleObjectProperty<CustomDate>(this, ENDDATE_PROPERTY_ID);
		}
		return endDate;
	}

	public StringProperty startDateStringProperty() {
		if (startDateString == null) {
			startDateString = new SimpleStringProperty(this, START_STRING_PROPERTY_ID);
		}
		return startDateString;
	}

	public StringProperty endDateStringProperty() {
		if (endDateString == null) {
			endDateString = new SimpleStringProperty(this, END_STRING_PROPERTY_ID);
		}
		return endDateString;
	}

	public StringProperty occurrenceProperty() {
		if (occurrenceString == null) {
			occurrenceString = new SimpleStringProperty(this, OCCURRENCE_PROPERTY_ID);
		}
		return occurrenceString;
	}

	/********************************* GET Value Functions ***********************************/
	public boolean isLastOverdueTask() {
		return rowStatus.get().getIsLastOverdue();
	}

	public boolean isImportantTask() {
		return rowStatus.get().getIsImportant();
	}
	
	public boolean isFloatingTask(){
		return getStartDate() == null && getEndDate() == null;
	}
	
	public RowStatus getRowStatus(){
		return rowStatus.get();
	}

	public String getStartDateString() {
		return startDateString.get();
	}

	public String getEndDateString() {
		return endDateString.get();
	}

	public CustomDate getStartDate() {
		return startDate.get();
	}

	public CustomDate getEndDate() {
		return endDate.get();
	}

	public String getWorkInfo() {
		return workInfo.get();
	}

	public Tag getTag() {
		return tag.get();
	}

	public String getIndexId() {
		return indexId;
	}

	public int getIndexInList() {
		return indexInList;
	}

	public Status getStatus() {
		return status;
	}

	public CustomDate getLatestModifiedDate() {
		return latestModifiedDate;
	}

	public boolean hasNewlyAddedStatus() {
		return status == Status.NEWLY_ADDED || status == Status.ADDED_WHEN_SYNC;
	}

	public boolean hasDeletedStatus() {
		return status == Status.DELETED || status == Status.DELETED_WHEN_SYNC;
	}

	public boolean hasUnchangedStatus() {
		return status == Status.UNCHANGED;
	}
	
	//@author A0105667B
	public int getNumOccurrences() {
		return num_occurrences;
	}

	public int getCurrentOccurrence() {
		return current_occurrence;
	}
	
	//@author A0098077N
	/*************************************** SET Value Functions ****************************************/
	public void setIsLastOverdue(boolean isLastOverdue) {
		rowStatus.set(new RowStatus(rowStatus.get().getIsImportant(), isLastOverdue));
	}

	public void setIsImportant(boolean isImportant) {
		rowStatus.set(new RowStatus(isImportant, rowStatus.get().getIsLastOverdue()));
	}
	
	public void setRowStatus(RowStatus rowStatus){
		this.rowStatus.set(rowStatus);
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setStartDate(CustomDate startDate) {
		this.startDate.set(startDate);
		if (startDate != null) {
			setStartDateString(startDate.toString(true));
		} else {
			setStartDateString(EMPTY);
		}
	}

	public void setEndDate(CustomDate endDate) {
		this.endDate.set(endDate);
		if (endDate != null) {
			setEndDateString(endDate.toString(false));
		} else {
			setEndDateString(EMPTY);
		}
	}

	public void setStartDateString(String dateString) {
		startDateString.set(dateString);
	}

	public void setEndDateString(String dateString) {
		endDateString.set(dateString);
	}

	public void setWorkInfo(String workInfo) {
		this.workInfo.set(workInfo);
	}

	public void setTag(Tag tag) {
		this.tag.set(tag);
	}

	public void setIndexId(String indexId) {
		this.indexId = indexId;
	}

	public void setIndexInList(int index) {
		indexInList = index;
	}

	public void setLatestModifiedDate(CustomDate modifiedDate) {
		latestModifiedDate = modifiedDate;
	}

	public void updateLatestModifiedDate() {
		latestModifiedDate = new CustomDate();
	}
	
	//@author A0105667B
	public void initOccurrence(int num_occurrences) {
		this.num_occurrences = num_occurrences;
		current_occurrence = 1;
		updateOccurrenceString();
	}
	
	public void setNumOccurrences(int num_occurrences) {
		this.num_occurrences = num_occurrences;
		updateOccurrenceString();
	}

	public void setCurrentOccurrence(int current) {
		current_occurrence = current;
		updateOccurrenceString();
	}

	public void setOccurrence(int occurNum, int curOccur) {
		num_occurrences = occurNum;
		current_occurrence = curOccur;
		updateOccurrenceString();
	}
	
	public static boolean equalTask(Task task1, Task task2) {
		if (!task1.getIndexId().equals(task2.getIndexId())) {
			return false;
		} else if (!task1.getWorkInfo().equals(task2.getWorkInfo())) {
			return false;
		} else if (CustomDate.compare(task1.getStartDate(), task2.getStartDate()) != 0) {
			return false;
		} else if (CustomDate.compare(task1.getEndDate(), task2.getEndDate()) != 0) {
			return false;
		} else if (!task1.getTag().getTag().equals(task2.getTag().getTag())) {
			return false;
		} else if (!task1.getTag().getRepetition().equals(task2.getTag().getRepetition())) {
			return false;
		} else if (task1.isImportantTask() != task2.isImportantTask()) {
			return false;
		} else if (task1.getNumOccurrences() != task2.getNumOccurrences()) {
			return false;
		} else if (task1.getCurrentOccurrence() != task2.getCurrentOccurrence()) {
			return false;
		} else {
			return true;
		}
	}
}

//@author A0098077N
/**
 * 
 * This class is used to stored the information of tag of a task
 * A tag comprises the information of the recurring and category tags.
 *
 */
class Tag {
	// The default category tag
	private String tag;
	// The recurring tag
	private String repetition;
	
	
	/*
	 * Constructor
	 */
	public Tag(String tag, String repetition) {
		setTag(tag);
		setRepetition(repetition);
	}
	
	/********************************************** SET functions ******************************************/
	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setRepetition(String repetition) {
		this.repetition = repetition;
	}
	
	/************************************************ GET functions ******************************************/
	public String getTag() {
		return this.tag;
	}

	public String getRepetition() {
		return this.repetition;
	}

	/**
	 * This function is used to get the interval in the recurrence tag For
	 * example, "every 3 weeks" will return 3 while "every day" will return -1
	 * as there is no intervals.
	 * 
	 * @return the required interval
	 */
	public int getInterval() {
		int startIndex = repetition.indexOf("every");
		if(startIndex != -1){
			int endIndex;
			if(repetition.contains("day")){
				endIndex = repetition.indexOf("day");
			} else if(repetition.contains("week")){
				endIndex = repetition.indexOf("week");				
			} else if(repetition.contains("month")){
				endIndex = repetition.indexOf("month");
			} else{
				endIndex = repetition.indexOf("year");				
			}
			int interval = Integer.parseInt(repetition.substring(startIndex+5, endIndex).trim());
			return interval;
		}
		return -1;
	}
}

//@author A0098077N
/**
 * 
 * This class is used to store the current status of a task whether it is
 * important or the last overdue task in the list
 * 
 */
class RowStatus{
	// Important indicator
	private boolean isImportant;
	// Last overdue indicator
	private boolean isLastOverdue;
	
	/*
	 * Constructor
	 */
	public RowStatus(boolean isImportant, boolean isLastOverdue){
		this.isImportant = isImportant;
		this.isLastOverdue = isLastOverdue;
	}
	
	/**************************************** SET functions ******************************************/
	public void setIsImportant(boolean isImportant){
		this.isImportant = isImportant;
	}
	
	public void setIsLastOverdue(boolean isLastOverdue){
		this.isLastOverdue = isLastOverdue;
	}
	
	/****************************************** GET functions *********************************************/
	public boolean getIsImportant(){
		return this.isImportant;
	}
	
	public boolean getIsLastOverdue(){
		return this.isLastOverdue;
	}
}