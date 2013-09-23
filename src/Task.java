class Task implements Comparable<Task> {

	private boolean isImportant;
	private CustomDate startDate;
	private CustomDate endDate;
	private String workInfo;
	private String tag;
	private int indexId;

	// default constructor
	public Task() {
		isImportant = false;
		startDate = null;
		endDate = null;
		workInfo = "";
		tag = "";
		indexId = 0;
	}

	public Task(boolean isImportant, CustomDate startDate, CustomDate endDate,
			String workInfo, String tag, int indexId) {
		this.isImportant = isImportant;
		this.startDate = startDate;
		this.endDate = endDate;
		this.workInfo = workInfo;
		this.tag = tag;
		this.indexId = indexId;
	}

	public int compareTo(Task other){
		if(endDate.compareTo(other.endDate) == 0)
			return workInfo.compareToIgnoreCase(other.workInfo);
		else
			return endDate.compareTo(other.endDate);
	}
	
	
	// get functions
	public boolean getIsImportant() {
		return isImportant;
	}

	public CustomDate getStartDate() {
		return startDate;
	}

	public CustomDate getEndDate() {
		return endDate;
	}

	public String getWorkInfo() {
		return workInfo;
	}

	public String getTag() {
		return tag;
	}

	public int getIndexId() {
		return indexId;
	}

	// set functions
	public void setIsImportant(boolean isImportant) {
		this.isImportant = isImportant;
	}

	public void setStartDate(CustomDate startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(CustomDate endDate) {
		if(endDate.getHour() == 0 && endDate.getMinute() == 0){
			endDate.setHour(23);
			endDate.setMinute(59);
		}
		this.endDate = endDate;
	}

	public void setWorkInfo(String workInfo) {
		this.workInfo = workInfo;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setIndexId(int indexId) {
		this.indexId = indexId;
	}
}