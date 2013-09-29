class History {
	private TwoWayCommand prevCommand;
	private boolean undoable;
	private boolean redoable;

	public History() {
		prevCommand = null;
		undoable = false;
		redoable = false;
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
}
