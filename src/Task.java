import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Task implements Comparable<Task> {

	private BooleanProperty isImportant;
	private ObjectProperty<CustomDate> startDate;
	private StringProperty startDateString;
	private ObjectProperty<CustomDate> endDate;
	private StringProperty endDateString;
	private StringProperty workInfo;
	private ObjectProperty<Tag> tag;
	private int indexId;

	// default constructor
	public Task() {
		isImportantProperty();
		workInfoProperty();
		tagProperty();
		startDateProperty();
		startDateStringProperty();
		endDateStringProperty();
		endDateProperty();

		setIsImportant(false);
		setStartDate(null);
		setStartDateString("-");
		setEndDate(null);
		setStartDateString("-");
		setWorkInfo("");
		setTag(new Tag("", ""));
		indexId = 0;
	}

	public Task(boolean isImportant, CustomDate startDate, CustomDate endDate,
			String workInfo, Tag tag, int indexId) {
		isImportantProperty();
		workInfoProperty();
		tagProperty();
		startDateProperty();
		startDateStringProperty();
		endDateStringProperty();
		endDateProperty();

		setIsImportant(isImportant);
		setStartDate(startDate);
		if (startDate != null)
			setStartDateString(startDate.toString(true));
		else
			setStartDateString("-");
		setEndDate(endDate);
		if (endDate != null)
			setEndDateString(endDate.toString(false));
		else
			setEndDateString("-");
		setWorkInfo(workInfo);
		setTag(tag);
		this.indexId = indexId;
	}

	public int compareTo(Task other) {
		int compareEndDate = CustomDate.compare(getEndDate(), other.getEndDate());
		int compareStartDate = CustomDate.compare(getStartDate(), other.getStartDate());
		
		if (compareEndDate == 0 && compareStartDate == 0)
			return getWorkInfo().compareToIgnoreCase(other.getWorkInfo());
		else if(compareEndDate == 0)
			return compareStartDate;
		else
			return compareEndDate;
	}

	// get Property
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

	// get functions
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

	// set functions
	public void setIsImportant(boolean isImportant) {
		this.isImportant.set(isImportant);
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

	public void updateDateString() {
		if (getStartDate() != null)
			setStartDateString(getStartDate().toString(true));
		if (getEndDate() != null)
			setEndDateString(getEndDate().toString(false));
	}
	
	public void updateDate(){
		if(getEndDate().beforeCurrentTime()){
			long difference = getUpdateDifference(getTag().getRepetition());
			
			CustomDate startDate = getStartDate();
			startDate.setTimeInMillis(startDate.getTimeInMillis() + difference);
			setStartDate(startDate);
			setStartDateString(getStartDate().toString(true));
			
			CustomDate endDate = getEndDate();
			endDate.setTimeInMillis(endDate.getTimeInMillis() + difference);
			setEndDate(endDate);
			setEndDateString(getEndDate().toString(false));
		}
	}
	
	public static long getUpdateDifference(String repetition){
		if(repetition.equals("daily") || repetition.equals("everyday"))
			return (long)24*60*60*1000;
		else if(repetition.equals("weekly") || repetition.startsWith("every"))
			return (long)7*24*60*60*1000;
		else if(repetition.equals("monthly"))
			return (long)30*24*60*60*1000;
		else if(repetition.equals("yearly") || repetition.equals("annually"))
			return (long)365*24*60*60*1000;
		else
			throw new IllegalArgumentException("Invalid repetitive tag");
	}
}

class Tag{
	private String tag;
	private String repetition;
	
	public Tag(String tag, String repetition){
		setTag(tag);
		setRepetition(repetition);
	}
	
	public void setTag(String tag){
		this.tag = tag;
	}
	
	public void setRepetition(String repetition){
		this.repetition = repetition;
	}
	
	public String getTag(){
		return this.tag;
	}
	
	public String getRepetition(){
		return this.repetition;
	}
}