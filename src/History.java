import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

class History {
	private static Logger log = Logger.getLogger("History");
	private Stack<TwoWayCommand> prevCommandsForUndo;
	private Stack<TwoWayCommand> prevCommandsForRedo;
	private boolean undoable;
	private boolean redoable;
	private boolean isOperatedAfterSearch;

	public History() {
		prevCommandsForUndo = new Stack<TwoWayCommand>();
		prevCommandsForRedo = new Stack<TwoWayCommand>();
		undoable = false;
		redoable = false;
		isOperatedAfterSearch = false;
	}

	public boolean isUndoable() {
		return undoable;
	}

	public void setUndoable(boolean undoable) {
		this.undoable = undoable;
	}

	public void setRedoable(boolean redoable) {
		this.redoable = redoable;
	}
	
	public boolean isRedoable() {
		return redoable;
	}
	
	public void setIsAfterSearch(boolean isAfter) {
		isOperatedAfterSearch = isAfter;
	}
	
	public boolean isAfterSearch() {
		return isOperatedAfterSearch;
	}
	
	public TwoWayCommand getPrevCommandForUndo() {
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
		TwoWayCommand previousCommand = prevCommandsForRedo.pop();
		prevCommandsForUndo.push(previousCommand);
		log.log(Level.INFO, "Moved one previous command from redo to undo.");
		undoable = true;
		if (prevCommandsForRedo.empty()){
			redoable = false;
		}
		return previousCommand;
	}

	public void updateCommand(TwoWayCommand newCommand) {
		prevCommandsForUndo.push(newCommand);
		if (!prevCommandsForRedo.empty()){
			prevCommandsForRedo.clear();
		}
		log.log(Level.INFO, "Cleared redo stack and added new command to undo stack.");
		undoable = true;
		redoable = false;
	}
	
	public void updateCommand(TwoWayCommand newCommand,boolean isAfter) {
		prevCommandsForUndo.push(newCommand);
		if (!prevCommandsForRedo.empty()){
			prevCommandsForRedo.clear();
		}
		log.log(Level.INFO, "Cleared redo stack and added new command to undo stack.");
		undoable = true;
		redoable = false;
		isOperatedAfterSearch = isAfter;
	}
}
