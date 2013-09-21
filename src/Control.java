import java.util.ArrayList;
import java.util.Scanner;

public class Control {

	private static final String MESSAGE_INVALID_COMMAND = "Invalid Command!";

	public static final int VALID = 1;
	public static final int INVALID = -1;

  static Model modelHandler = new Model();
  	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String s;
		while (sc.hasNextLine()) {
			s = sc.nextLine();
			try {
				 int feedback = executeCommand(s);
				 System.out.println(feedback);
			} catch (Exception e) {
				System.out.println("Invalid Command");
			}
		}
	}
  	
	private static int executeCommand(String userCommand) {
		boolean isEmptyCommand = Parser.checkEmptyCommand(userCommand);
		if (isEmptyCommand) {
			return INVALID;
		}

		Parser.COMMAND_TYPES commandType = Parser.determineCommandType(userCommand);
		System.out.println(commandType);

		String[] parsedUserCommand = Parser.parseCommand(userCommand,
				commandType);

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
	/*
	public static int executeAddCommand(String[] parsedUserCommand) {
		if (parsedUserCommand.length < 1) {
			return INVALID;
		}
		CustomDate startDate = null;
		CustomDate endDate = null;
		String workInfo = "";
		String tag = null;
		boolean hasTag = false;
		boolean isImptTask = false;
		int workInfoIndex = parsedUserCommand.length - 1;

		for (int i = parsedUserCommand.length - 1; i >= 0; i--) {
			if (isImptTask == false && isImportantTask(parsedUserCommand[i])) {
				isImptTask = true;
				--workInfoIndex;
			} else if (hasTag == false && hasTag(parsedUserCommand[i])) {
				hasTag = true;
				tag = getTagName(parsedUserCommand[i]);
				--workInfoIndex;
			} else if (endDate == null && isEndDate(parsedUserCommand[i])
					&& (endDate = getEndDate(parsedUserCommand[i])) != null) {
				--workInfoIndex;
			} else if (startDate == null
					&& isStartDate(parsedUserCommand[i])
					&& (startDate = getStartDate(parsedUSerCommand[i])) != null) {
				--workInfoIndex;
			}
		}

		if (workInfoIndex < 0) {
			System.out.println("Invalid workInfo");
		}

		for (int i = 0; i <= workInfoIndex; i++) {
			workInfo += splittedUserCommand[i];
			if (i != workInfoIndex) {
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
		System.out.println("workInfo: " + workInfo);
		System.out.println("startDate: " + startDate);
		System.out.println("endDate: " + endDate);
		System.out.println("tag: " + tag);
		System.out.println("impt: " + isImptTask);

		// modelHandler.getPending().add(task);

		return VALID;
	}
	*/
	
	public static int executeAddCommand(String[] parsedUserCommand) {
		String workInfo = parsedUserCommand[0];
		String startDate = parsedUserCommand[1];
		String endDate = parsedUserCommand[2];
		String tag = parsedUserCommand[3];
		boolean isImptTask = false;
		if (parsedUserCommand[4].equals(Parser.TRUE)){
			isImptTask = true;
		}
		
		Task task = new Task();
		task.setWorkInfo(workInfo);

		if (!startDate.equals("")) {
			if (updateStartDate(endDate,task) == INVALID) {
				return INVALID;		
			}
		}
		if (!endDate.equals("")) {
			if (updateStartDate(endDate,task) == INVALID) {
				return INVALID;		
			}
		}
		if (tag != null) {
			task.setTag(tag);
		}
		task.setIsImportant(isImptTask);
		modelHandler.addTaskToPending(task);
		System.out.println("workInfo: " + workInfo);
		System.out.println("startDate: " + startDate);
		System.out.println("endDate: " + endDate);
		System.out.println("tag: " + tag);
		System.out.println("impt: " + isImptTask);
		return VALID;
	}
	
	// The user can edit the starting time, ending time, tag and isImportant of
	// existing tasks
	public static int  executeEditCommand(String[] parsedUserCommand){       
		int index = Integer.parseInt(parsedUserCommand[0]);

		Task targetTask = modelHandler.getTaskFromPending(index);

		String workInfo = targetTask.getWorkInfo();
		String startTime =  targetTask.getStartDate()==null?"":targetTask.getStartDate().toString();

		String endTime = targetTask.getEndDate()==null?"":targetTask.getEndDate().toString();
		
		String tag = targetTask.getTag();

		String isImptTask =targetTask.getIsImportant()==true?Parser.TRUE:Parser.FALSE;
		
		if(parsedUserCommand[1] != Parser.NULL) {
			startTime = parsedUserCommand[1];
			if (updateStartDate(startTime, targetTask) == INVALID) {
				return INVALID;
			}
		} 
		if (parsedUserCommand[2] != Parser.NULL) {
			endTime = parsedUserCommand[2];
			if (updateEndDate(endTime, targetTask) == INVALID) {
				return INVALID;
			}
		}
		if (parsedUserCommand[3] != Parser.NULL) {
			tag = parsedUserCommand[3];
			targetTask.setTag(tag);
		}
		if (parsedUserCommand[4].equals(Parser.TRUE)) {
			isImptTask = Parser.TRUE;
			targetTask.setIsImportant(true);
		}
		System.out.println("workInfo: " + workInfo);
		System.out.println("startDate: " + startTime);
		System.out.println("endDate: " + endTime);
		System.out.println("tag: " + tag);
		System.out.println("impt: " + isImptTask);
		return VALID;
    }

	// return the result whether the update is successful
	private static int updateStartDate(String dateInfo, Task task) {
		CustomDate startDate = new CustomDate();
		if (startDate.convert(dateInfo) == INVALID) {
			return INVALID;
            }
			task.setStartDate(startDate);
			return VALID;
	}

	private static int updateEndDate(String dateInfo, Task task) {
		CustomDate endDate = new CustomDate();
		if (endDate.convert(dateInfo) == INVALID) {
			return INVALID;
            }
			task.setEndDate(endDate);
			return VALID;
		}

    public static int executeRemoveCommand(String[] splittedUserCommand){
        int indexCount = splittedUserCommand.length;
		int[] indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
            indexList[i] = Integer.valueOf(splittedUserCommand[i]);
		}

		int i = indexCount - 1;
        int prevIndex=-1;
		while (i >= 0) {
			if (indexList[i] != prevIndex) {
                if (modelHandler.removeTaskFromPending(i) == INVALID) {
                	return INVALID;
                }
			}
			prevIndex = indexList[i];
		}
		return VALID;
	}

	private static int executeSearchCommand(String[] splittedUserCommand) {
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

		return INVALID;
	}

	public static ArrayList<Task> searchImportantTask(ArrayList<Task> list) {
		ArrayList<Task> result = new ArrayList<Task>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getIsImportant())
				result.add(list.get(i));
		}
		return result;
	}

	public static ArrayList<Task> searchTag(ArrayList<Task> list, String tagName) {
		ArrayList<Task> result = new ArrayList<Task>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getTag().equals(tagName))
				result.add(list.get(i));
		}
		return result;
	}

	public static ArrayList<Task> searchStartDate(ArrayList<Task> list, CustomDate date) {
		ArrayList<Task> result = new ArrayList<Task>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getStartDate().compareTo(date) <= 0)
				result.add(list.get(i));
		}
		return result;
	}

	public static ArrayList<Task> searchEndDate(ArrayList<Task> list, CustomDate date) {
		ArrayList<Task> result = new ArrayList<Task>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getEndDate().compareTo(date) <= 0)
				result.add(list.get(i));
		}
		return result;
	}

	public static ArrayList<Task> searchWorkInfo(ArrayList<Task> list, String workInfo) {
		ArrayList<Task> result = new ArrayList<Task>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getWorkInfo().equals(workInfo))
				result.add(list.get(i));
		}
		return result;
	}
	

}
