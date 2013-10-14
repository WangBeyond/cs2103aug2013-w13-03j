import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Task implements Comparable<Task> {
	final static boolean IS_START_DATE = true;
	final static boolean IS_END_DATE = false;

	// Enum Class representing status of a task
	public static enum Status {
		UNCHANGED, NEWLY_ADDED, DELETED
	}

	// Property indicating a task is important or not
	private BooleanProperty isImportant;
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

	// Index ID of this task in Google Calendar
	private int indexId;
	// Index in the list containing the task
	private int indexInList;
	// Current status of the task
	private Status status;
	private CustomDate latestModifiedDate;

	// Default constructor
	public Task() {
		checkProperty();
		defaultInitialization();
	}

	public Task(boolean isImportant, CustomDate startDate, CustomDate endDate,
			String workInfo, Tag tag, int indexId, Status status) {
		checkProperty();

		setIsImportant(isImportant);
		setStartDate(startDate);
		setEndDate(endDate);
		setWorkInfo(workInfo);
		setTag(tag);

		this.indexId = indexId;
		setStatus(status);
		updateLatestModifiedDate();
	}

	private void defaultInitialization() {
		setIsImportant(false);
		setStartDate(null);
		setStartDateString("-");
		setEndDate(null);
		setStartDateString("-");
		setWorkInfo("");
		setTag(new Tag("", ""));
		indexId = 0;
		indexInList = 0;
		setStatus(Status.NEWLY_ADDED);
		updateLatestModifiedDate();
	}

	/**
	 * This function is used to check whether any property has not been
	 * initialized If there are, it will initialize these properties.
	 */
	private void checkProperty() {
		isImportantProperty();
		workInfoProperty();
		tagProperty();
		startDateProperty();
		startDateStringProperty();
		endDateStringProperty();
		endDateProperty();
	}

	/**
	 * This function is the implemented method for interface Comparable. It is
	 * used to compare this task with another task
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
	 * Update the string representing the date
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

	/**
	 * This function is used to update the start time and end time for a
	 * repetitive task when the end time is behind the current time
	 */
	public void updateDateForRepetitiveTask() {
		long difference = CustomDate.getUpdateDifference(getTag()
				.getRepetition());
		while (getEndDate().beforeCurrentTime()) {
			updateStartDate(difference);
			updateEndDate(difference);

		}
	}

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
		return tag.get().getRepetition().equals(Parser.NULL);
	}

	/************************ Get Property Functions **********************************/
	public BooleanProperty isImportantProperty() {
		if (isImportant == null)
			isImportant = new SimpleBooleanProperty(this, "isimportant");
		return isImportant;
	}

	public StringProperty workInfoProperty() {
		if (workInfo == null)
			workInfo = new SimpleStringProperty(this, "workinfo");
		return workInfo;
	}

	public ObjectProperty<Tag> tagProperty() {
		if (tag == null)
			tag = new SimpleObjectProperty<Tag>(this, "tag");
		return tag;
	}

	public ObjectProperty<CustomDate> startDateProperty() {
		if (startDate == null)
			startDate = new SimpleObjectProperty<CustomDate>(this, "startdate");
		return startDate;
	}

	public ObjectProperty<CustomDate> endDateProperty() {
		if (endDate == null)
			endDate = new SimpleObjectProperty<CustomDate>(this, "enddate");
		return endDate;
	}

	public StringProperty startDateStringProperty() {
		if (startDateString == null)
			startDateString = new SimpleStringProperty(this, "startDateString");
		return startDateString;
	}

	public StringProperty endDateStringProperty() {
		if (endDateString == null)
			endDateString = new SimpleStringProperty(this, "endDateString");
		return endDateString;
	}

	/********************************* Get Value Functions ***********************************/
	public boolean getIsImportant() {
		return isImportant.get();
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

	public int getIndexId() {
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

	/*************************************** Set Value Functions ****************************************/
	public void setIsImportant(boolean isImportant) {
		this.isImportant.set(isImportant);
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setStartDate(CustomDate startDate) {
		this.startDate.set(startDate);
		if (startDate != null)
			setStartDateString(startDate.toString(true));
		else
			setStartDateString("-");
	}

	public void setEndDate(CustomDate endDate) {
		this.endDate.set(endDate);
		if (endDate != null)
			setEndDateString(endDate.toString(false));
		else
			setEndDateString("-");
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

	public void setIndexId(int indexId) {
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
}

/************************************* Class to store repetitive and category tag **********************************/
class Tag {
	private String tag;
	private String repetition;

	public Tag(String tag, String repetition) {
		setTag(tag);
		setRepetition(repetition);
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setRepetition(String repetition) {
		this.repetition = repetition;
	}

	public String getTag() {
		return this.tag;
	}

	public String getRepetition() {
		return this.repetition;
	}
}