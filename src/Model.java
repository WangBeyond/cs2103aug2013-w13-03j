import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 
 * This class is used to store all the data while the application is working.
 * It comprises data in settings, list of tasks for display on the interface.
 *
 */
public class Model {
	private static final String MESSAGE_OUT_OF_BOUNDS_INDEX = "Out of bounds index";
	// Logger
	private static Logger logger = Logger.getLogger("Model");
	
	// Index of tabs
	private final static int PENDING_TAB = 0;
	private final static int COMPLETE_TAB = 1;
	private final static int TRASH_TAB = 2;

	/*
	 * List of all tasks in corresponding tabs
	 */
	private ObservableList<Task> pending;
	private ObservableList<Task> complete;
	private ObservableList<Task> trash;
	
	
	/*
	 * List of searched tasks in corresponding tabs
	 */
	private ObservableList<Task> searchPending;
	private ObservableList<Task> searchComplete;
	private ObservableList<Task> searchTrash;
	
	/*
	 * List of IDs from tasks which were removed during synchronization
	 */
	private ObservableList<String> removedIdDuringSync;

	//@author A0105523U
	/*
	 * Default constructor
	 */
	public Model() {
		pending = FXCollections.observableArrayList();
		complete = FXCollections.observableArrayList();
		trash = FXCollections.observableArrayList();
		searchPending = FXCollections.observableArrayList();
		searchComplete = FXCollections.observableArrayList();
		searchTrash = FXCollections.observableArrayList();
		removedIdDuringSync = FXCollections.observableArrayList();
		displayRemaining = true;
		themeMode = Common.DAY_MODE;
		colourScheme = Common.DAY_MODE;
		syncPeriod = 1;
	}
	
	/**************************************** TASK Section **********************************/
	
	/************************** GET a task from given index ************************************/
	public Task getTaskFromPending(int index) {
		try{
			Task temp = pending.get(index);
			return temp;
		} catch (IndexOutOfBoundsException e){
			logger.log(Level.WARNING, MESSAGE_OUT_OF_BOUNDS_INDEX);
			return null;
		}
	}

	public Task getTaskFromComplete(int index) {
		try{
			Task temp = complete.get(index);
			return temp;
		} catch (IndexOutOfBoundsException e){
			logger.log(Level.WARNING, MESSAGE_OUT_OF_BOUNDS_INDEX);
			return null;
		}
	}

	public Task getTaskFromTrash(int index) {
		try{
			Task temp = trash.get(index);
			return temp;
		} catch (IndexOutOfBoundsException e){
			logger.log(Level.WARNING, MESSAGE_OUT_OF_BOUNDS_INDEX);
			return null;
		}
	}

	/****************************** GET the required list of tasks *****************************/
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
	
	//@author A0105667B
	/******************************** GET or MODIFY the index IDs of deleted-during-sync tasks ******************/
	public ObservableList<String> getRemovedIdDuringSync() {
		return removedIdDuringSync;
	}
	
	public void clearSyncInfo() {
		removedIdDuringSync.clear();
		
		modifyStatusForAddedTasks();
		modifyStatusForCompletedTasks();
		modifyStatusForRemovedTasks();
	}

	private void modifyStatusForRemovedTasks() {
		for(Task deletedTask : trash) {
			if (deletedTask.getStatus() == Task.Status.DELETED_WHEN_SYNC){
			deletedTask.setStatus(Task.Status.DELETED);
			}
		}
	}

	private void modifyStatusForCompletedTasks() {
		for(Task deletedTask : complete) {
			if (deletedTask.getStatus() == Task.Status.DELETED_WHEN_SYNC){
			deletedTask.setStatus(Task.Status.DELETED);
			}
		}
	}

	private void modifyStatusForAddedTasks() {
		for(Task addedTask : pending) {
			if(addedTask.getStatus() == Task.Status.ADDED_WHEN_SYNC){
				addedTask.setStatus(Task.Status.NEWLY_ADDED);
			}
		}
	}

	//@author A0100927M
	/********************************** GET the index from given task ******************************/
	public int getIndexFromPending(Task task) {
		return pending.indexOf(task);
	}

	public int getIndexFromComplete(Task task) {
		return complete.indexOf(task);
	}

	public int getIndexFromTrash(Task task) {
		return trash.indexOf(task);
	}
	
	
	/****************************** ADD a task to the list *******************************/
	public void addTaskToPending(Task newPendingTask) {
		pending.add(newPendingTask);
	}

	public void addTaskToComplete(Task newCompleteTask) {
		complete.add(newCompleteTask);
	}

	public void addTaskToTrash(Task newTrashTask) {
		trash.add(newTrashTask);
	}

	/******************** REMOVE a task with indicated index *******************************/
	public void removeTask(int index, int tabIndex) {
		if (tabIndex == PENDING_TAB) {
			removeTaskFromPending(index);
		} else if (tabIndex == COMPLETE_TAB) {
			removeTaskFromComplete(index);
		} else if (tabIndex == TRASH_TAB) {
			removeTaskFromTrash(index);
		}
	}

	private void removeTaskFromPending(int index) {
		try {
			Task t = pending.remove(index);
			if (t.getStatus() != Task.Status.ADDED_WHEN_SYNC) {
				removedIdDuringSync.add(t.getIndexId());
			}
			trash.add(t);
		} catch (IndexOutOfBoundsException e) {
			logger.log(Level.WARNING, MESSAGE_OUT_OF_BOUNDS_INDEX);
		}
	}

	private void removeTaskFromComplete(int index) {
		try {
			Task t = complete.remove(index);
			trash.add(t);
		} catch (IndexOutOfBoundsException e) {
			logger.log(Level.WARNING, MESSAGE_OUT_OF_BOUNDS_INDEX);
		}
	}

	private void removeTaskFromTrash(int index) {
		try {
			trash.remove(index);
		} catch (IndexOutOfBoundsException e) {
			logger.log(Level.WARNING, MESSAGE_OUT_OF_BOUNDS_INDEX);
		}
	}

	/***************************** REMOVE a task with indicated index permanently, not moving to trash *******************/
	public void removeTaskFromPendingNoTrash(int index) {
		try {
			pending.remove(index);
		} catch (IndexOutOfBoundsException e) {
			logger.log(Level.WARNING, MESSAGE_OUT_OF_BOUNDS_INDEX);
		}
	}

	public void removeTaskFromCompleteNoTrash(int index) {
		try {
			complete.remove(index);
		} catch (IndexOutOfBoundsException e) {
			logger.log(Level.WARNING, MESSAGE_OUT_OF_BOUNDS_INDEX);
		}
	}

	/******************************complete a task from pending***************************************/
	public void completeTaskFromPending(int index) {
		try {
			Task t = pending.remove(index);
			if (t.getStatus() != Task.Status.ADDED_WHEN_SYNC) {
				removedIdDuringSync.add(t.getIndexId());
			}
			addTaskToComplete(t);
		} catch (IndexOutOfBoundsException e) {
			logger.log(Level.WARNING, MESSAGE_OUT_OF_BOUNDS_INDEX);
		}
	}
	
	/************************************** SET a specific searchList *********************************/
	public void setSearchPendingList(ObservableList<Task> searchList) {
		searchPending = searchList;
	}

	public void setSearchCompleteList(ObservableList<Task> searchList) {
		searchComplete = searchList;
	}

	public void setSearchTrashList(ObservableList<Task> searchList) {
		searchTrash = searchList;
	}
	
	//@author A0105667B
	/************************************ SETTINGS Section *************************************************/
	// Google Account ID
	private String username;
	// Google Account Password
	private String password;
	// Indicator whether to display the remaining time or not
	private boolean displayRemaining;
	// The theme mode: DAY or NIGHT
	private String themeMode;
	// The color theme
	private String colourScheme;
	// Indicator whether to auto sync or not
	private boolean isAutoSync;
	// Period of syncing if enabling auto sync
	private int syncPeriod;
	
	/*********************************** GET functions ****************************************/
	public boolean doDisplayRemaining(){
		return displayRemaining;
	}
	
	public String getThemeMode(){
		return themeMode;
	}
	
	//@author A0100927M
	public String getColourScheme(){
		return colourScheme;
	}
	
	public boolean getDisplayRemaining() {
		return displayRemaining;
	}
	public boolean hasAutoSync() {
		return isAutoSync;
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public int getSyncPeriod(){
		return syncPeriod;
	}
	
	/***************************************** SET functions ********************************************/
	public void setThemeMode(String themeMode){
		this.themeMode = themeMode;
	}

	public void setDisplayRemaining(boolean displayRemaining){
		this.displayRemaining = displayRemaining;
	}

	public void setAutoSync(boolean isAutoSync) {
		this.isAutoSync = isAutoSync;
	}
	
	public void setColourScheme(String colourScheme){
		this.colourScheme = colourScheme;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public void setSyncPeriod(int syncPeriod){
		this.syncPeriod = syncPeriod;
	}
}