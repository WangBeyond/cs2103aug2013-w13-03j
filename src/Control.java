public class Control {

  public static final int VALID = 1;
  public static final int INVALID = -1;
  
  
  private Model modelHandler = new Model();
  
	enum COMMAND_TYPES {
		ADD, REMOVE, SEARCH, EDIT, COMPLETE, INCOMPLETE, UNDO, REDO, CLEAR_ALL, TODAY, SHOW_ALL, SYNC, SETTINGS, HELP, EXIT, INVALID, MARK, UNMARK
	}

	private int executeCommand(String userCommandString) {
		boolean isEmptyCommand = userCommandString.trim().equals("");

		if (isEmptyCommand) {
			return INVALID;
		}

		String commandTypeString = getFirstWord(userCommandString);

		String[] splittedUserCommand = splitCommandString(userCommandString);

		COMMAND_TYPES commandType = determineCommandType(commandTypeString);

		switch (commandType) {
		case ADD:
			return executeAddCommand(splittedUserCommand);
		case EDIT:
			return executeEditCommand(splittedUserCommand);
		case REMOVE:
			return executeRemoveCommand(splittedUserCommand);
		case UNDO:
			return executeUndoCommand(splittedUserCommand);
		case REDO:
			return executeRedoCommand(splittedUserCommand);
		case SEARCH:
			return executeSearchCommand(splittedUserCommand);
		case TODAY:
			return executeTodayCommand(splittedUserCommand);
		case SHOW_ALL:
			return executeShowAllCommand(splittedUserCommand);
		case CLEAR_ALL:
			return executeClearAllCommand(splittedUserCommand);
		case COMPLETE:
			return executeCompleteCommand(splittedUserCommand);
		case INCOMPLETE:
			return executeIncompleteCommand(splittedUserCommand);
		case MARK:
			return executeMarkCommand(splittedUserCommand);
		case UNMARK:
			return executeUnmarkCommand(splittedUserCommand);
		case SETTINGS:
			return executeSettingsCommand(splittedUserCommand);
		case HELP:
			return executeHelpCommand(splittedUserCommand);
		case SYNC:
			return executeSyncCommand(splittedUserCommand);
		case EXIT:
			return executeExitCommand(splittedUserCommand);
		case INVALID:
			return executeInvalidCommand(splittedUserCommand);
		default:
			throw new Error("Unrecognised command type.");
		}
	}

	private static COMMAND_TYPES determineCommandType(String commandTypeString) {
		if (commandTypeString == null)
			throw new Error("Command type string cannot be null!");

		if (isAddCommand(commandTypeString)) {
			return COMMAND_TYPES.ADD;
		} else if (isEditCommand(commandTypeString)) {
			return COMMAND_TYPES.EDIT;
		} else if (isRemoveCommand(commandTypeString)) {
			return COMMAND_TYPES.REMOVE;
		} else if (isUndoCommand(commandTypeString)) {
			return COMMAND_TYPES.UNDO;
		} else if (isRedoCommand(commandTypeString)) {
			return COMMAND_TYPES.REDO;
		} else if (isSearchCommand(commandTypeString)) {
			return COMMAND_TYPES.SEARCH;
		} else if (isTodayCommand(commandTypeString)) {
			return COMMAND_TYPES.TODAY;
		} else if (isShowAllCommand(commandTypeString)) {
			return COMMAND_TYPES.SHOW_ALL;
		} else if (isClearAllCommand(commandTypeString)) {
			return COMMAND_TYPES.CLEAR_ALL;
		} else if (isCompleteCommand(commandTypeString)) {
			return COMMAND_TYPES.COMPLETE;
		} else if (isIncompleteCommand(commandTypeString)) {
			return COMMAND_TYPES.INCOMPLETE;
		} else if(isMarkCommand(commandTypeString)){
			return COMMAND_TYPES.MARK;
		} else if(isUnmarkCommand(commandTypeString)){
			return COMMAND_TYPES.UNMARK;
		} else if (isSettingsCommand(commandTypeString)) {
			return COMMAND_TYPES.SETTINGS;
		} else if (isHelpCommand(commandTypeString)) {
			return COMMAND_TYPES.HELP;
		} else if (isSyncCommand(commandTypeString)) {
			return COMMAND_TYPES.SYNC;
		} else if (isExitCommand(commandTypeString)) {
			return COMMAND_TYPES.EXIT;
		} else {
			return COMMAND_TYPES.INVALID;
		}
	}

	public static int executeAddCommand(String[] splittedUserCommand) {
		String[] startDateKeys = {"start", "begin", "from"};
		String[] endDateKeys = {"end", "to", "till", "until", "by", "due"};
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
			} else if(endDate == null && isEndDate(splittedUserCommand[i])){
				endDate = new CustomDate();
				endDate.convert(removeInputKeys(splittedUserCommand[i], endDateKeys));
				--workInfoIndex;
			} else if(startDate == null && isStartDate(splittedUserCommand[i])){
				startDate = new CustomDate();
				startDate.convert(removeInputKeys(splittedUserCommand[i], startDateKeys));
				--workInfoIndex;
			}
		}
		
		if(workInfoIndex < 0){
			System.out.println("Invalid workInfo");
		}
		
		for(int i=0;i<=workInfoIndex;i++){
			workInfo += splittedUserCommand[i];
			if(i != workInfoIndex){
				workInfo += ",";
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
    executeEditCommand(String[] splittedUserCommand){            
        int updateNum = splittedUserCommand.size()-1;
        int taskIndex = getFirstWord(splittedUserCommand[0]);
        splittedUserCommand[0] = removeFirstWord(splittedUserCommand[0]);
        Task targettask = getTaskFromPending(taskIndex);
        //Retrive the edited infos one by one
        for(int i=0;i<updateNum;i++){
            String updateInfo = splittedUserCommand[i].trim();
            String[] splittedUpdateInfo = updateInfo.split(" ");

            //The case that the update info 1 is new starting date
            if(splittedUpdateInfo[0].equalsTo("from"){
                String dateInfo = removeFirstWord(updateInfo);
                if(updateStartDate(dateInfo,targetTask)==INVALID){
                    //feedback about dateUpdateFailure, waiting to implement
                    feedbackStartDateUpdateFail("Invalid input");
                } 
            } else if((splittedUpdateInfo[0].equalsTo("start")||splittedUpdateInfo[0].equalsTo("begin"))
                && (splittedUpdateInfo[1].equalsTo("at")||splittedUpdateInfo[1].equalsTo("from")||splittedUpdateInfo[1].equalsTo("on")){
                String dateInfo =removeFirstWord(removeFirstWord(updateInfo));
                if(updateStartDate(dateInfo,targetTask)==INVALID){
                    //feedback about dateUpdateFailure, waiting to implement
                    feedbackStartDateUpdateFail("Invalid input");
                }
            } 
            //The case that the update info 1 is new ending date
              else if(splittedUpdateInfo[0].equalsTo("to")||splittedUpdateInfo[0].equalsTo("till")
                ||splittedUpdateInfo[0].equalsTo("until")||splittedUpdateInfo[0].equalsTo("by")
                ||splittedUpdateInfo[0].equalsTo("due")){
                String dateInfo = removeFirstWord(UpdateInfo);
                if(updateEndDate(dateInfo,targetTask)==INVALID){
                    //feedback about dateUpdateFailure, waiting to implement
                    feedbackEndDateUpdateFail("Invalid input");
                } 
            } else if(splittedUpdateInfo[0].equalsTo("end")&&(splittedUpdateInfo[1].equalsTo("on")||
                splittedUpdateInfo[1].equalsTo("at")||splittedUpdateInfo[1].equalsTo("by")|splittedUpdateInfo[1].equalsTo("before"))){
                String dateInfo = removeFirstWord(removeFirstWord(updateInfo));
                if(updateEndDate(dateInfo,targetTask)==INVALID){
                    //feedback about dateUpdateFailure, waiting to implement
                    feedbackEndDateUpdateFail("Invalid input");
                } 
            } 
               //The case that the update info 1 is a tag
               else if(hasTag(updateInfo)){
                        targettask.setTag(updateInfo.trim());
            } 
              //The case that the update info 1 is about importance*
              else if(isImportantTask(updateInfo)){
                        targettask.setIsImportant(true);
            } else if(updateInfo.equalsTo("~")||updateInfo.toLowerCase().equalsTo("not important")){
                        targettask.setIsImportant(false)
            }
        }
    }


        //return the result whether the update is successful
        private static int updateStartDate(String dateInfo,Task task){
            CustomDate startDate = new CustomDate();
            if(startDate.convert(dateInfo)==-INVALID){
                return INVALID;
            task.setStartDate(startDate);
            return VALID
        }

        private static int updateEndDate(String dateInfo,Task task){
            CustomDate endDate = new CustomDate();
            if(endDate.convert(dateInfo)==-INVALID){
                return INVALID;
            task.setEndDate(endDate);
            return VALID
        }



    private public executeRemoveCommand(String[] splittedUserCommand){
        indexCount = splittedUserCommand.size();
        int[] indexList = new int[indexCount]
        for(int=0; i<indexCount;i++){
            indexList[i] = parseInt(splittedUserCommand[i]);
        }
        java.util.Arrays.sort(indexList)
        int i=indexCount-1;
        prevIndex=-1
        while(i>=0){
            if(indexList[i]!=prevIndex){
                model.reomvoeTaskFromComplete(i);
            }
            prevIndex = indexList[i];
        }        
    }
  
	public static String[] splitCommandString(String userCommand) {
		String content = removeFirstWord(userCommand);

		if (content.contains(",")) {
			return splitCommandStringByComma(content);
		} else {
			return splitCommandStringBySpace(content);
		}
	}

	private static String[] splitCommandStringByComma(String content) {
		String[] splittedContent = content.split(",");
		for( String s : splittedContent){
			s=s.trim();
		}
    return splittedContent;
	}

	private static String[] splitCommandStringBySpace(String content) {
		return content.trim().split(" ");
	}

	private static String getFirstWord(String userCommand) {
		String commandTypeString = userCommand.trim().split("\\s+")[0];
		return commandTypeString;
	}

	private static String removeFirstWord(String userCommand) {
		return userCommand.replace(getFirstWord(userCommand), "").trim();
	}

	public static String getStartDateString(String s) {
		String[] keys = {"from", "start from", "start at", "start on", "begin at", "begin from", "begin on"};
		return removeInputKeys(s, keys);
	}
	public static String getEndDateString(String s) {
		String[] keys = {"to", "till", "untill", "by", "end at", "begin by", "end on", "end before", "due"};
		return removeInputKeys(s, keys);
	}
	private static String removeInputKeys(String s, String[] keys) {
		String StringRemovedKey = null;
		for(int i=0; i<keys.length; i++){
			if(s.contains(keys[i])){
				StringRemovedKey = s.replace(keys[i], " ").trim();
			}
		}
		return StringRemovedKey;
	}
	private static String getTagName(String s) {
		return s.replace("#", " ").trim();
	}
	public static boolean isStartDate(String content) {
		String[] inputKeys = {"start", "begin", "from"};
		boolean containsKey = false;
		for(int i=0;i<inputKeys.length;i++){
			if(content.contains(inputKeys[i])){
				containsKey = true;
			}
		}
		if(containsKey){
			CustomDate tempDate = new CustomDate();
			String DateString = getStartDateString(content);
			return tempDate.convert(DateString) == VALID;			
		}
		return false;
	}

	public static boolean isEndDate(String content) {
		String[] inputKeys = {"end", "to", "till", "until", "by", "due"};
		boolean containsKey = false;
		for(int i=0;i<inputKeys.length;i++){
			if(content.contains(inputKeys[i])){
				containsKey = true;
			}
		}
		if(containsKey){
			CustomDate tempDate = new CustomDate();
			String DateString = getEndDateString(content);
			return tempDate.convert(DateString) == VALID;			
		}
		return false;
	}

	private static boolean hasTag(String content) {
		return content.contains("#");
	}

	private static boolean isImportantTask(String content) {
		return content.trim().equals("*");
	}

	private static boolean isAddCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("add");
	}

	private static boolean isEditCommand(String commandTypeString) {
		boolean isEdit = commandTypeString.equalsIgnoreCase("edit")
				|| commandTypeString.equalsIgnoreCase("modify")
				|| commandTypeString.equalsIgnoreCase("mod");
		return isEdit;
	}

	private static boolean isRemoveCommand(String commandTypeString) {
		boolean isRemove = commandTypeString.equalsIgnoreCase("remove")
				|| commandTypeString.equalsIgnoreCase("rm")
				|| commandTypeString.equalsIgnoreCase("delete")
				|| commandTypeString.equalsIgnoreCase("del");
		return isRemove;
	}

	private static boolean isUndoCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("undo");
	}

	private static boolean isRedoCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("redo");
	}

	private static boolean isSearchCommand(String commandTypeString) {
		boolean isSearch = commandTypeString.equalsIgnoreCase("search")
				|| commandTypeString.equalsIgnoreCase("find");
		return isSearch;
	}

	private static boolean isTodayCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("today");
	}

	private static boolean isShowAllCommand(String commandTypeString) {
		boolean isShowAll = commandTypeString.equalsIgnoreCase("all")
				|| commandTypeString.equalsIgnoreCase("show")
				|| commandTypeString.equalsIgnoreCase("list")
				|| commandTypeString.equalsIgnoreCase("ls");
		return isShowAll;
	}

	private static boolean isClearAllCommand(String commandTypeString) {
		boolean isClearAll = commandTypeString.equalsIgnoreCase("clear")
				|| commandTypeString.equalsIgnoreCase("clr");
		return isClearAll;
	}

	private static boolean isCompleteCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("done");
	}

	private static boolean isIncompleteCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("undone");
	}
	
	private static boolean isMarkCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("mark");
	}
	
	private static boolean isUnmarkCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("unmark");
	}
	
	private static boolean isSettingsCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("settings");
	}

	private static boolean isHelpCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("help");
	}

	private static boolean isSyncCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("sync");
	}

	private static boolean isExitCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("exit");
	}
}
