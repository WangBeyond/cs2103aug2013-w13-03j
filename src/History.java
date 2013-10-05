class History {
	private TwoWayCommand prevCommand;
	private boolean undoable;
	private boolean redoable;
	private boolean isOperatedAfterSearch;

	public History() {
		prevCommand = null;
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
	
	public TwoWayCommand getPrevCommand() {
		undoable = !undoable;
		redoable = !redoable;
		return prevCommand;

	}

	public void updateCommand(TwoWayCommand newCommand) {
		prevCommand = newCommand;
		undoable = true;
		redoable = false;
	}
	
	public void updateCommand(TwoWayCommand newCommand,boolean isAfter) {
		prevCommand = newCommand;
		undoable = true;
		redoable = false;
		isOperatedAfterSearch =  isAfter;
	}
}
