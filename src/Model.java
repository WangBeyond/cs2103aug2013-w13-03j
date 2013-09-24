import java.util.ArrayList;

class Model {
	ArrayList<Task> pending;
	ArrayList<Task> complete;
	ArrayList<Task> trash;
	private ArrayList<Task> search;

	// constructor
	public Model() {
		pending = new ArrayList<Task>();
		complete = new ArrayList<Task>();
		trash = new ArrayList<Task>();
		search = new ArrayList<Task>();
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

	public ArrayList<Task> getPendingList() {
		return pending;
	}

	public ArrayList<Task> getCompleteList() {
		return complete;
	}

	public ArrayList<Task> getTrashList() {
		return trash;
	}

	public ArrayList<Task> getSearchList() {
		return search;
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

	public void addTaskToSearch(Task newSearchTask) {
		search.add(newSearchTask);
	}

	// remove task functions return INVALID or VALID when remove a task
	public int removeTaskFromPending(int index) {
		if (index > pending.size()) {
			return Control.INVALID;
		} else {
			pending.remove(index);
			return Control.VALID;
		}
	}

	public int removeTaskFromComplete(int index) {
		if (index > complete.size()) {
			return Control.INVALID;
		} else {
			complete.remove(index);
			return Control.VALID;
		}
	}

	public int removeTaskFromTrash(int index) {
		if (index > trash.size()) {
			return Control.INVALID;
		} else {
			trash.remove(index);
			return Control.VALID;
		}
	}
}