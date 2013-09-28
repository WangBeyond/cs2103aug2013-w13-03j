class History {

	private TwoWayCommand prevCommand;
	private boolean undoOnce;
	private boolean redoOnce;
	
	public History(){
		prevCommand = null;
		undoOnce = false;
		redoOnce = false;
	}
	
	public boolean getUndoOnce(){
		return undoOnce;
	}
	
	public boolean getRedoOnce(){
		return redoOnce;
	}
	
	public TwoWayCommand getPrevCommand(){
		undoOnce = false;
		redoOnce = true;
		return prevCommand;

	}
	
	public void updateCommand(TwoWayCommand newCommand){
		prevCommand = newCommand;
		undoOnce = true;
		redoOnce = false;
	}
}
