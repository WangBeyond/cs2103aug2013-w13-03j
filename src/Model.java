import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
	private ObservableList<Task> pending;
	private ObservableList<Task> complete;
	private ObservableList<Task> trash;
	private ObservableList<Task> searchPending;
	private ObservableList<Task> searchComplete;
	private ObservableList<Task> searchTrash;

	// constructor
	public Model() {
		pending = FXCollections.observableArrayList();
		complete = FXCollections.observableArrayList();
		trash = FXCollections.observableArrayList();
		searchPending = FXCollections.observableArrayList();
		searchComplete = FXCollections.observableArrayList();
		searchTrash = FXCollections.observableArrayList();
	}

	// get functions
	public Task getTaskFromPending(int index) {
		return pending.get(index);
	}

	public Task getTaskFromComplete(int index) {
		return complete.get(index);
	}

	public Task getTaskFromTrash(int index) {
		return trash.get(index);
	}

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
	
	public int getIndexFromPending(Task task){
		return pending.indexOf(task);
	}

	public int getIndexFromComplete(Task task){
		return complete.indexOf(task);
	}
	
	public int getIndexFromTrash(Task task){
		return trash.indexOf(task);
	}
	
	// add new task functions
	public void addTaskToPending(Task newPendingTask) {
		pending.add(newPendingTask);
	}

	public void addTaskToComplete(Task newCompleteTask) {
		complete.add(newCompleteTask);
	}

	public void addTaskToTrash(Task newTrashTask) {
		trash.add(newTrashTask);
	}

	// remove task functions return INVALID or VALID when remove a task
	public void removeTask(int index, int listType){
		if(listType == 0)
			removeTaskFromPending(index);
		else if(listType == 1)
			removeTaskFromComplete(index);
		else
			removeTaskFromTrash(index);
	}
	
	public void removeTaskFromPending(int index) {
		Task t = pending.remove(index);
		trash.add(t);
	}

	public void removeTaskFromComplete(int index) {
		Task t = complete.remove(index);
		trash.add(t);

	}

	public void removeTaskFromTrash(int index) {
		trash.remove(index);
	}

	public void removeTaskFromPendingNoTrash(int index) {
		pending.remove(index);
	}

	public void removeTaskFromCompleteNoTrash(int index) {
		complete.remove(index);
	}

	// set Search List
	public void setSearchPendingList(ObservableList<Task> searchList) {
		searchPending = searchList;
	}

	public void setSearchCompleteList(ObservableList<Task> searchList) {
		searchComplete = searchList;
	}

	public void setSearchTrashList(ObservableList<Task> searchList) {
		searchTrash = searchList;
	}
}