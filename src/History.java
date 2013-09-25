class History {
	
	private final static int BEFORE = 0;	
	private final static int AFTER = 1;
	private final static int NUM_CHANGE_HISTORY = 2;
	
	private String[] changeHistory;

	// default constructor
	public History(){
		changeHistory = new String[NUM_CHANGE_HISTORY];
	}
	
	public String getAfter(){
		return changeHistory[AFTER];
	}
	
	public String getBefore(){
		return changeHistory[BEFORE];
	}
	
	public void updateChangeHistory(String newChange){
		if (changeHistory.length < 1)
			changeHistory[BEFORE] = newChange;
		else if (changeHistory.length == 1)
			changeHistory[AFTER] = newChange;
		else{
			changeHistory[BEFORE] = changeHistory[AFTER];
			changeHistory[AFTER] = newChange;
		}
	}
}
