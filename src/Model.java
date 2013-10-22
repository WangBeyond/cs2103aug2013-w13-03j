import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
	final static int PENDING_LIST = 0;
	final static int COMPLETE_LIST = 1;
	final static int TRASH_LIST = 2;

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

	/*
	 * public ArrayList<Integer> getDeletedIndexList() { return deletedIndices;
	 * }
	 * 
	 * public ArrayList<Integer> getAddedIndexList() { return newlyAddedIndices;
	 * }
	 * 
	 * public void loadIndicesToDeletedList(String indices) {
	 * deletedIndices.clear(); if(!indices.trim().equals("")) { String[]
	 * indexStrList = indices.split(" "); for(String indexStr : indexStrList) {
	 * deletedIndices.add(Integer.parseInt(indexStr)); } } }
	 * 
	 * public void loadIndicesToAddedList(String indices) {
	 * newlyAddedIndices.clear(); if(!indices.trim().equals("")) { String[]
	 * indexStrList = indices.split(" "); for(String indexStr : indexStrList) {
	 * newlyAddedIndices.add(Integer.parseInt(indexStr)); } } }
	 * 
	 * public void addIndicesToDeleted(int index) { deletedIndices.add(index); }
	 * 
	 * public void addIndicesToAdded(int index) { newlyAddedIndices.add(index);
	 * }
	 */
}