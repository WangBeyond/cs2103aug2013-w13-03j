import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
	final static int PENDING_LIST = 0;
	final static int COMPLETE_LIST = 1;
	final static int TRASH_LIST = 2;
	final static String DAY_MODE = "day";
	final static String NIGHT_MODE = "night";
	
	private ObservableList<Task> pending;
	private ObservableList<Task> complete;
	private ObservableList<Task> trash;
	private ObservableList<Task> searchPending;
	private ObservableList<Task> searchComplete;
	private ObservableList<Task> searchTrash;

	// Constructor
	public Model() {
		pending = FXCollections.observableArrayList();
		complete = FXCollections.observableArrayList();
		trash = FXCollections.observableArrayList();
		searchPending = FXCollections.observableArrayList();
		searchComplete = FXCollections.observableArrayList();
		searchTrash = FXCollections.observableArrayList();
		displayRemaining = true;
		themeMode = DAY_MODE;
	}

	/************************** Get a task from given index ************************************/
	public Task getTaskFromPending(int index) {
		return pending.get(index);
	}

	public Task getTaskFromComplete(int index) {
		return complete.get(index);
	}

	public Task getTaskFromTrash(int index) {
		return trash.get(index);
	}

	/****************************** Get the required list of tasks *****************************/
	public ObservableList<Task> getPendingList() {
		return pending;
	}

	public ObservableList<Task> getCompleteList() {
		return complete;
	}

	public ObservableList<Task> getTrashList() {
		return trash;
	}

	public ObservableList<Task> getSearchPendingList() {
		return searchPending;
	}

	public ObservableList<Task> getSearchCompleteList() {
		return searchComplete;
	}

	public ObservableList<Task> getSearchTrashList() {
		return searchTrash;
	}

	/********************************** Get the index from given index ******************************/
	public int getIndexFromPending(Task task) {
		return pending.indexOf(task);
	}

	public int getIndexFromComplete(Task task) {
		return complete.indexOf(task);
	}

	public int getIndexFromTrash(Task task) {
		return trash.indexOf(task);
	}

	/****************************** Add a task to the list *******************************/
	public void addTaskToPending(Task newPendingTask) {
		pending.add(newPendingTask);
	}

	public void addTaskToComplete(Task newCompleteTask) {
		complete.add(newCompleteTask);
	}

	public void addTaskToTrash(Task newTrashTask) {
		trash.add(newTrashTask);
	}

	/******************** Remove a task with indicated index *******************************/
	public void removeTask(int index, int listType) {
		if (listType == PENDING_LIST) {
			removeTaskFromPending(index);
		} else if (listType == COMPLETE_LIST) {
			removeTaskFromComplete(index);
		} else if (listType == TRASH_LIST) {
			removeTaskFromTrash(index);
		}
	}

	private void removeTaskFromPending(int index) {
		Task t = pending.remove(index);
		trash.add(t);
	}

	private void removeTaskFromComplete(int index) {
		Task t = complete.remove(index);
		trash.add(t);
	}

	private void removeTaskFromTrash(int index) {
		trash.remove(index);
	}

	/***************************** Remove a task with indicated index permanently *******************/
	public void removeTaskFromPendingNoTrash(int index) {
		pending.remove(index);
	}

	public void removeTaskFromCompleteNoTrash(int index) {
		complete.remove(index);
	}

	/************************************** Set a specific searchList *********************************/
	public void setSearchPendingList(ObservableList<Task> searchList) {
		searchPending = searchList;
	}

	public void setSearchCompleteList(ObservableList<Task> searchList) {
		searchComplete = searchList;
	}

	public void setSearchTrashList(ObservableList<Task> searchList) {
		searchTrash = searchList;
	}
	
	/************************************ Settings Model *************************************************/
	private String username;
	private String password;
	private String calendarID;
	private boolean displayRemaining;
	private String themeMode;
	
	public boolean doDisplayRemaining(){
		return displayRemaining;
	}
	
	public String getThemeMode(){
		return themeMode;
	}
	
	public void setThemeMode(String themeMode){
		this.themeMode = themeMode;
	}
	
	
	public boolean getDisplayRemaining() {
		return displayRemaining;
	}
	public void setDisplayRemaining(boolean displayRemaining){
		this.displayRemaining = displayRemaining;
	}
	
	
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public String getCalendarID(){
		return calendarID;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public void setCalendarID(String calendarID){
		this.calendarID = calendarID;
	}
}