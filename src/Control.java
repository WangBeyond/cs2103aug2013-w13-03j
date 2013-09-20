import java.util.ArrayList;

public class Control {

	private static final String MESSAGE_INVALID_COMMAND = "Invalid Command!";
	
  public static final int VALID = 1;
  public static final int INVALID = -1;
  
  static Model modelHandler = new Model();
  
	private int executeCommand(String userCommand) {
		boolean isEmptyCommand = Parser.checkEmptyCommand(userCommand);
		if (isEmptyCommand) {
			return INVALID;
		}
		
		Parser.COMMAND_TYPES commandType = Parser.determineCommandType(userCommand);
		
		String[] parsedUserCommand = Parser.parseCommand(userCommand, commandType);

		switch (commandType) {
		case ADD:
			return executeAddCommand(parsedUserCommand);
		case EDIT:
			return executeEditCommand(parsedUserCommand);
		case REMOVE:
			return executeRemoveCommand(parsedUserCommand);
		case UNDO:
			return executeUndoCommand(parsedUserCommand);
		case REDO:
			return executeRedoCommand(parsedUserCommand);
		case SEARCH:
			return executeSearchCommand(parsedUserCommand);
		case TODAY:
			return executeTodayCommand(parsedUserCommand);
		case SHOW_ALL:
			return executeShowAllCommand(parsedUserCommand);
		case CLEAR_ALL:
			return executeClearAllCommand(parsedUserCommand);
		case COMPLETE:
			return executeCompleteCommand(parsedUserCommand);
		case INCOMPLETE:
			return executeIncompleteCommand(parsedUserCommand);
		case MARK:
			return executeMarkCommand(parsedUserCommand);
		case UNMARK:
			return executeUnmarkCommand(parsedUserCommand);
		case SETTINGS:
			return executeSettingsCommand(parsedUserCommand);
		case HELP:
			return executeHelpCommand(parsedUserCommand);
		case SYNC:
			return executeSyncCommand(parsedUserCommand);
		case EXIT:
			return executeExitCommand(parsedUserCommand);
		case INVALID:
			return executeInvalidCommand(userCommandString);
		default:
			throw new Error("Unrecognised command type.");
		}
	}

	

	public static int executeAddCommand(String[] splittedUserCommand) {
		if (splittedUserCommand.length < 1) {
			return INVALID;
		}
		CustomDate startDate = null;
		CustomDate endDate = null;
		String workInfo = "";
		String tag = null;
		boolean hasTag = false;
		boolean isImptTask = false;
		int workInfoIndex = splittedUserCommand.length-1;

		for(int i=splittedUserCommand.length-1;i>=0;i--){
			if(isImptTask == false && isImportantTask(splittedUserCommand[i])){
				isImptTask = true;
				--workInfoIndex;
			} else if(hasTag == false && hasTag(splittedUserCommand[i])){
				hasTag = true;
				tag = getTagName(splittedUserCommand[i]);
				--workInfoIndex;
			} else if(endDate == null && isEndDate(splittedUserCommand[i]) && (endDate = getEndDate(splittedUserCommand[i])) != null ){
				--workInfoIndex;
			} else if(startDate == null && isStartDate(splittedUserCommand[i]) && (startDate = getStartDate(splittedUSerCommand[i])) != null){
				--workInfoIndex;
			}
		}
		
		if(workInfoIndex < 0){
			System.out.println("Invalid workInfo");
		}
		
		for(int i=0;i<=workInfoIndex;i++){
			workInfo += splittedUserCommand[i];
			if(i != workInfoIndex){
				workInfo += ", ";
			}
		}
		
		Task task = new Task();
		task.setWorkInfo(workInfo);
		
		if (startDate != null) {
			task.setStartDate(startDate);
		}
		if (endDate != null) {
			task.setEndDate(endDate);
		}
		if (hasTag) {
			task.setTag(tag);
		}
		if (isImptTask) {
			task.setIsImportant(isImptTask);
		}
		System.out.println("workInfo: "+workInfo);
		System.out.println("startDate: "+startDate);
		System.out.println("endDate: "+endDate);
		System.out.println("tag: "+tag);
		System.out.println("impt: "+isImptTask);
		
		//modelHandler.getPending().add(task);
		
		return VALID;
	}
  

        //The user can edit the starting time, ending time, tag and isImportant of existing tasks 
	public static void  executeEditCommand(String[] parsedUserCommand){       
		int index = Integer.parseInt(parsedUserCommand[0]);
		Task targetTask = modelHandler.getTaskFromPending(index);
		if(!parsedUserCommand[1].equals(Parser.NULL)) {
			String startTime = parsedUserCommand[1];
			int feedback = updateStartDate(startTime, targetTask);
		} 
		if(!parsedUserCommand[2].equals(Parser.NULL)) {
			String endTime = parsedUserCommand[2];
			int feedback = updateEndDate(endTime, targetTask);
		}
		if(!parsedUserCommand[3].equals(Parser.NULL)) {
			String tagKey = parsedUserCommand[3];
			targetTask.setTag(tagKey);
		}
		if(parsedUserCommand[4].equals(Parser.TRUE)) {
			targetTask.setIsImportant(true);
		}
    }


        //return the result whether the update is successful
        private static int updateStartDate(String dateInfo,Task task){
            CustomDate startDate = new CustomDate();
            if(startDate.convert(dateInfo)==-INVALID){
                return INVALID;
            }
            task.setStartDate(startDate);
            return VALID;
       }

        private static int updateEndDate(String dateInfo,Task task){
            CustomDate endDate = new CustomDate();
            if(endDate.convert(dateInfo)==-INVALID){
                return INVALID;
            }
            task.setEndDate(endDate);
            return VALID;
        }



    public static void executeRemoveCommand(String[] splittedUserCommand){
        int indexCount = splittedUserCommand.length;
        int[] indexList = new int[indexCount];
        for(int i=0; i<indexCount;i++){
            indexList[i] = Integer.valueOf(splittedUserCommand[i]);
        }
        
        int i=indexCount-1;
        int prevIndex=-1;
        while(i>=0){
            if(indexList[i]!=prevIndex){
                modelHandler.removeTaskFromPending(i);
            }
            prevIndex = indexList[i];
        }        
    }
    
   private String executeSearchCommand(String[] splittedUserCommand) {
		ArrayList<Task> searchList = modelHandler.getSearchList();
		searchList.clear();

		boolean isFirstTimeSearch = true;

		if (splittedUserCommand[0] != Parser.NULL) {
			String workInfo = splittedUserCommand[0];
			searchList = searchWorkInfo(
					(isFirstTimeSearch) ? modelHandler.getPendingList()
							: searchList, workInfo);
			isFirstTimeSearch = false;
		}

		if (splittedUserCommand[1] != Parser.NULL) {
			String date = splittedUserCommand[1];
			searchList = searchStartDate(
					(isFirstTimeSearch) ? modelHandler.getPendingList()
							: searchList, Parser.getStartDate(date));
			isFirstTimeSearch = false;
		}

		if (splittedUserCommand[2] != Parser.NULL) {
			String date = splittedUserCommand[2];
			searchList = searchStartDate(
					(isFirstTimeSearch) ? modelHandler.getPendingList()
							: searchList, Parser.getEndDate(date));
			isFirstTimeSearch = false;
		}

		if (splittedUserCommand[3] != Parser.NULL) {
			String tag = splittedUserCommand[3];
			searchList = searchTag(
					(isFirstTimeSearch) ? modelHandler.getPendingList()
							: searchList, tag);
			isFirstTimeSearch = false;
		}

		if (splittedUserCommand[4] == Parser.TRUE) {
			searchList = searchImportantTask((isFirstTimeSearch) ? modelHandler
					.getPendingList() : searchList);
		}

		return "Search Completed!";
	}
    
    public ArrayList<Task> searchImportantTask(ArrayList<Task> list){
    	ArrayList<Task> result = new ArrayList<Task>();
    	for(int i = 0; i < list.size(); i++){
    		if(list.get(i).getIsImportant())
    			result.add(list.get(i));
    	}
    	return result;
    }
    
    public ArrayList<Task> searchTag(ArrayList<Task> list, String tagName){
    	ArrayList<Task> result = new ArrayList<Task>();
    	for(int i = 0; i < list.size(); i++){
    		if(list.get(i).getTag().equals(tagName))
    			result.add(list.get(i));
    	}
    	return result;
    }
    
    public ArrayList<Task> searchStartDate(ArrayList<Task> list, CustomDate date){
    	ArrayList<Task> result = new ArrayList<Task>();
    	for(int i = 0; i < list.size(); i++){
    		if(list.get(i).getStartDate().compareTo(date) <= 0)
    			result.add(list.get(i));
    	}
    	return result;
    }
    
    public ArrayList<Task> searchEndDate(ArrayList<Task> list, CustomDate date){
    	ArrayList<Task> result = new ArrayList<Task>();
    	for(int i = 0; i < list.size(); i++){
    		if(list.get(i).getEndDate().compareTo(date) <= 0)
    			result.add(list.get(i));
    	}
    	return result;
    }
    
    public ArrayList<Task> searchWorkInfo(ArrayList<Task> list, String workInfo){
    	ArrayList<Task> result = new ArrayList<Task>();
    	for(int i = 0; i < list.size(); i++){
    		if(list.get(i).getWorkInfo().equals(workInfo))
    			result.add(list.get(i));
    	}
    	return result;
    }
    
    
	public static String[] splitCommandString(String userCommand, COMMAND_TYPES commandType) {
		String content = removeFirstWord(userCommand);

		if (commandType == COMMAND_TYPES.ADD || commandType == COMMAND_TYPES.EDIT || commandType == COMMAND_TYPES.SEARCH) {
			return splitCommandStringByComma(content);
		} else {
			return splitCommandStringBySpace(content);
		}
	}
	

/*
				lst.add(temp[i]);
				content = content.substring(0, content.lastIndexOf(","));
			} else if(hasTag(temp[i])&&!lst.contains(temp[i])){
				lst.add(temp[i]);
				content = content.substring(0, content.lastIndexOf(","));
			} else if(isEndTime(temp[i])&&!lst.contains(temp[i])){
				lst.add(temp[i]);
				content = content.substring(0, content.lastIndexOf(","));
			} else if(isStartTime(temp[i])&&!lst.contains(temp[i])){
				lst.add(temp[i]);
				content = content.substring(0, content.lastIndexOf(","));
			}
		}
		lst.add(content);
		String[] splittedCommand = new String[lst.size()];
		for(int i=0; i<lst.size();i++){
			splittedCommand[i] = lst.get(lst.size()-1-i);
		}
		return splittedCommand;
	}
*/

	
}
