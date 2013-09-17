import java.util.ArrayList;

class Model{
	private ArrayList<Task> pending;
	private ArrayList<Task> complete;
	private ArrayList<Task> trash;
	
	// constructor
	public Model(){
		pending = new ArrayList<Task>();
		complete = new ArrayList<Task>();
		trash = new ArrayList<Task>();
	}
	
	// get functions
	public Task getTaskFromPending(int index){ return pending.get(index); }
	public Task getTaskFromComplete(int index){ return complete.get(index); }
	public Task getTaskFromTrash(int index){ return trash.get(index); }
	public ArrayList<Task> getPending(){ return pending; }
	public ArrayList<Task> getComplete(){ return complete; }
	public ArrayList<Task> getTrash(){ return trash; }
	
	// add new task functions
	public void addTaskToPending(Task newPendingTask){ pending.add(newPendingTask); }
	public void addTaskToComplete(Task newCompleteTask){ complete.add(newCompleteTask); }
	public void addTaskToTrash(Task newTrashTask){ trash.add(newTrashTask); }
   
  // remove task functions return INVALID or VALID when remove a task
  public int removeTaskFromPending(int index) {
    if(index>pending.size()) return INVALID 
     else {pending.remove(index);
       return VALID;}
    }
  public int removeTaskFromComplete (int index) {
      if(index>pending.size()) return INVALID 
     else {complete.remove(index);
       return VALID;}
    }
  public int removeTaskFromTrash (int index) {   
      if(index>pending.size()) return INVALID 
     else {trash.remove(index);
       return VALID;}
    }
}