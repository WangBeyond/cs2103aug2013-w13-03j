import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

//@author A0100927M
/**
 * 
 * History class: This class stores and retrieves past commands.
 * 
 */
class History {
	private static Logger log = Logger.getLogger("History");
	private Stack<TwoWayCommand> prevCommandsForUndo;
	private Stack<TwoWayCommand> prevCommandsForRedo;
	private boolean undoable;
	private boolean redoable;
	private boolean isOperatedAfterSearch;

	/**
	 * This is the constructor for class History. 
	 */
	public History() {
		prevCommandsForUndo = new Stack<TwoWayCommand>();
		prevCommandsForRedo = new Stack<TwoWayCommand>();
		undoable = false;
		redoable = false;
		isOperatedAfterSearch = false;
	}
	
	/************************ GET Functions **********************************/
	public boolean isUndoable() {
		return undoable;
	}
	
	public boolean isRedoable() {
		return redoable;
	}
	
	//@author A0105667B
	public boolean isAfterSearch() {
		return isOperatedAfterSearch;
	}
	
	//@author A0100927M
	public TwoWayCommand getPrevCommandForUndo() {
		assert undoable == true;
		TwoWayCommand previousCommand = prevCommandsForUndo.pop();
		prevCommandsForRedo.push(previousCommand);
		log.log(Level.INFO, "Moved one previous command from undo to redo.");
		redoable = true;
		if (prevCommandsForUndo.empty()){
			undoable = false;
		}
		return previousCommand;
	}
	
	public TwoWayCommand getPrevCommandForRedo(){
		assert redoable == true;
		TwoWayCommand previousCommand = prevCommandsForRedo.pop();
		prevCommandsForUndo.push(previousCommand);
		log.log(Level.INFO, "Moved one previous command from redo to undo.");
		undoable = true;
		if (prevCommandsForRedo.empty()){
			redoable = false;
		}
		return previousCommand;
	}
	
	/************************ SET Functions **********************************/
	public void setUndoable(boolean undoable) {
		this.undoable = undoable;
	}

	public void setRedoable(boolean redoable) {
		this.redoable = redoable;
	}
	
	public void clearUndoStack(){
		prevCommandsForUndo.clear();
		undoable = false;
	}
	
	public void clearRedoStack(){
		prevCommandsForRedo.clear();
		redoable = false;
	}
	
	//@author A0105667B
	public void setIsAfterSearch(boolean isAfter) {
		isOperatedAfterSearch = isAfter;
	}
	
	//@author A0100927M
	/************************ UPDATE Functions **********************************/
	// update with most recent TwoWayCommand without prior searching
	public void updateCommand(TwoWayCommand newCommand) {
		prevCommandsForUndo.push(newCommand);
		clearRedoStack();
		log.log(Level.INFO, "Cleared redo stack and added new command to undo stack.");
		undoable = true;
	}
	
	//@author A0105667B
	// update with most recent TwoWayCommand with prior searching
	public void updateCommand(TwoWayCommand newCommand,boolean isAfter) {
		prevCommandsForUndo.push(newCommand);
		clearRedoStack();
		log.log(Level.INFO, "Cleared redo stack and added new command to undo stack.");
		undoable = true;
		isOperatedAfterSearch = isAfter;
	}
}
