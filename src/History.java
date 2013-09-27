import java.util.ArrayList;

class History {

	private TwoWayCommand prevCommand;
	private ArrayList<Task> pendingHistory;
	private ArrayList<Task> completeHistory;
	private ArrayList<Task> trashHistory;
	private boolean undoOnce;
	
	public History(){
		prevCommand = null;
		undoOnce = false;
	}
	
	public boolean getUndoOnce(){
		return undoOnce;
	}
	
	public TwoWayCommand getPrevCommand(){
		undoOnce = false;		
		return prevCommand;

	}
	
	public void updateCommand(TwoWayCommand newCommand){
		prevCommand = newCommand;
		undoOnce = true;
	}
}
