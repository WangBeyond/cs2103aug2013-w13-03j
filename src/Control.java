import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import sun.security.krb5.internal.PAData;

public class Control {

	private static final String MESSAGE_INVALID_COMMAND_TYPE = "Invalid Command Type!";
	private static final String MESSAGE_EMPTY_COMMAND = "Empty Command!";
	private static final String MESSAGE_SUCCESSFUL_SEARCH = "Successful Search!";
	private static final String MESSAGE_NO_RESULTS = "Search no results!";
	private static final String MESSAGE_SUCCESSFUL_ADD = "Task is added successfully";
	private static final String MESSAGE_SUCCESSFUL_EDIT = "Task is edited successfully";
	private static final String MESSAGE_SUCCESFUL_REMOVE = "Indicated tasks are removed";
	private static final String MESSAGE_INVALID_DATE_RANGE = "Invalid date range as start date is after end date";
	private static final String MESSAGE_DUPLICATE_INDEXES = "There are duplicate indexes";
	private static final String MESSAGE_INDEX_OUT_OF_BOUNDS = "There is an index outside the range of the list";

	public static final int VALID = 1;
	public static final int INVALID = -1;

	static Model modelHandler = new Model();

	public static String executeCommand(String userCommand) {
		boolean isEmptyCommand = Parser.checkEmptyCommand(userCommand);
		if (isEmptyCommand) {
			return MESSAGE_EMPTY_COMMAND;
		}
		try {
			Parser.COMMAND_TYPES commandType = Parser
					.determineCommandType(userCommand);

			String[] parsedUserCommand = Parser.parseCommand(userCommand,
					commandType);

			switch (commandType) {
			case ADD:
				return executeAddCommand(parsedUserCommand);
			case EDIT:
				return executeEditCommand(parsedUserCommand);
			case REMOVE:
				return executeRemoveCommand(parsedUserCommand);
				// case UNDO:
				// return executeUndoCommand(parsedUserCommand);
				// case REDO:
				// return executeRedoCommand(parsedUserCommand);
			case SEARCH:
				return executeSearchCommand(parsedUserCommand);
				// case TODAY:
				// return executeTodayCommand(parsedUserCommand);
				// case SHOW_ALL:
				// return executeShowAllCommand(parsedUserCommand);
				// case CLEAR_ALL:
				// return executeClearAllCommand(parsedUserCommand);
				// case COMPLETE:
				// return executeCompleteCommand(parsedUserCommand);
				// case INCOMPLETE:
				// return executeIncompleteCommand(parsedUserCommand);
				// case MARK:
				// return executeMarkCommand(parsedUserCommand);
				// case UNMARK:
				// return executeUnmarkCommand(parsedUserCommand);
				// case SETTINGS:
				// return executeSettingsCommand(parsedUserCommand);
				// case HELP:
				// return executeHelpCommand(parsedUserCommand);
				// case SYNC:
				// return executeSyncCommand(parsedUserCommand);
				// case EXIT:
				// return executeExitCommand(parsedUserCommand);
			case INVALID:
				return MESSAGE_INVALID_COMMAND_TYPE;
			default:
				throw new Error("Unrecognised command type.");
			}
		} catch (IllegalArgumentException e) {
			return e.getMessage();
		}
	}

	public static String executeAddCommand(String[] parsedUserCommand) {
		String workInfo = parsedUserCommand[0];
		String tag = parsedUserCommand[1];
		String startDateString = parsedUserCommand[2];
		String endDateString = parsedUserCommand[3];
		boolean isImptTask = false;

		if (parsedUserCommand[4].equals(Parser.TRUE)) {
			isImptTask = true;
		}

		Task task = new Task();

		task.setWorkInfo(workInfo);

		if (!startDateString.equals(Parser.NULL)) {
			CustomDate startDate = new CustomDate(startDateString);
			task.setStartDate(startDate);
		}

		if (!endDateString.equals(Parser.NULL)) {
			CustomDate endDate = new CustomDate(endDateString);
			task.setEndDate(endDate);
		}

		if (!startDateString.equals(Parser.NULL)
				&& !endDateString.equals(Parser.NULL)) {
			if (task.getEndDate().compareTo(task.getStartDate()) < 0)
				return MESSAGE_INVALID_DATE_RANGE;
		}

		if (!tag.equals(Parser.NULL)) {
			task.setTag(tag);
		}

		task.setIsImportant(isImptTask);

		modelHandler.addTaskToPending(task);

		sortList(modelHandler.getPendingList());

		return MESSAGE_SUCCESSFUL_ADD;
	}

	// The user can edit the starting time, ending time, tag and isImportant of
	// existing tasks
	public static String executeEditCommand(String[] parsedUserCommand) {
		int index = Integer.parseInt(parsedUserCommand[0]);

		Task targetTask = modelHandler.getTaskFromPending(index);

		String workInfo = parsedUserCommand[1];
		String tag = parsedUserCommand[2];
		String startDateString = parsedUserCommand[3];
		String endDateString = parsedUserCommand[4];
		boolean hasImptTaskToggle = (parsedUserCommand[5].equals(Parser.TRUE)) ? true
				: false;
		CustomDate startDate, endDate;
		startDate = endDate = null;

		if (!startDateString.equals(Parser.NULL)) {
			startDate = new CustomDate(startDateString);
		}

		if (!endDateString.equals(Parser.NULL)) {
			endDate = new CustomDate(endDateString);
			if (endDate.getHour() == 0 && endDate.getMinute() == 0) {
				endDate.setHour(23);
				endDate.setMinute(59);
			}
		}

		if (startDate != null && endDate != null
				&& endDate.compareTo(startDate) < 0)
			return MESSAGE_INVALID_DATE_RANGE;

		if (!workInfo.equals(Parser.NULL))
			targetTask.setWorkInfo(workInfo);

		if (startDate != null)
			targetTask.setStartDate(startDate);

		if (endDate != null)
			targetTask.setEndDate(endDate);

		if (tag != null)
			targetTask.setTag(tag);

		if (hasImptTaskToggle)
			targetTask.setIsImportant(!targetTask.getIsImportant());

		sortList(modelHandler.getPendingList());
		return MESSAGE_SUCCESSFUL_EDIT;
	}

	/*
	 * return the result whether the update is successful private static int
	 * updateStartDate(String dateInfo, Task task) { CustomDate startDate = new
	 * CustomDate(); if (startDate.convert(dateInfo) == INVALID) { return
	 * INVALID; } task.setStartDate(startDate); return VALID; }
	 * 
	 * private static int updateEndDate(String dateInfo, Task task) { CustomDate
	 * endDate = new CustomDate(); if (endDate.convert(dateInfo) == INVALID) {
	 * return INVALID; } task.setEndDate(endDate); return VALID; }
	 */
	public static String executeRemoveCommand(String[] splittedUserCommand) {
		int indexCount = splittedUserCommand.length;
		int[] indexList = new int[indexCount];
		for (int i = 0; i < indexCount; i++) {
			indexList[i] = Integer.valueOf(splittedUserCommand[i]);
		}

		Arrays.sort(indexList);

		for (int i = 0; i < indexCount - 1; i++)
			if (indexList[i] == indexList[i + 1])
				return MESSAGE_DUPLICATE_INDEXES;

		if (indexList[indexCount] > modelHandler.getPendingList().size())
			return MESSAGE_INDEX_OUT_OF_BOUNDS;

		for (int i = 0; i >= 0; i--) {
			modelHandler.removeTaskFromPending(indexList[i] - 1);
		}

		sortList(modelHandler.getPendingList());
		return MESSAGE_SUCCESFUL_REMOVE;
	}

	private static String executeSearchCommand(String[] splittedUserCommand) {
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
			String tag = splittedUserCommand[3];
			searchList = searchTag(
					(isFirstTimeSearch) ? modelHandler.getPendingList()
							: searchList, tag);
			isFirstTimeSearch = false;
		}

		if (splittedUserCommand[2] != Parser.NULL) {
			String date = splittedUserCommand[1];
			CustomDate startDate = new CustomDate(date);
			searchList = searchStartDate(
					(isFirstTimeSearch) ? modelHandler.getPendingList()
							: searchList, startDate);
			isFirstTimeSearch = false;
		}

		if (splittedUserCommand[3] != Parser.NULL) {
			String date = splittedUserCommand[2];
			CustomDate endDate = new CustomDate(date);
			searchList = searchEndDate(
					(isFirstTimeSearch) ? modelHandler.getPendingList()
							: searchList, endDate);
			isFirstTimeSearch = false;
		}

		if (splittedUserCommand[4] == Parser.TRUE) {
			searchList = searchImportantTask((isFirstTimeSearch) ? modelHandler
					.getPendingList() : searchList);
		}

		if (searchList.isEmpty())
			return MESSAGE_NO_RESULTS;

		return MESSAGE_SUCCESSFUL_SEARCH;
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
			String tag = list.get(i).getTag();
			if (tag.equals(tagName))
				result.add(list.get(i));
		}
		return result;
	}

	public static ArrayList<Task> searchStartDate(ArrayList<Task> list,
			CustomDate date) {
		ArrayList<Task> result = new ArrayList<Task>();
		if (date.getHour() != 0 || date.getMinute() != 0) {
			for (int i = 0; i < list.size(); i++) {
				CustomDate startDate = list.get(i).getStartDate();
				if (startDate != null && startDate.compareTo(date) == 0)
					result.add(list.get(i));
			}
		} else {
			for (int i = 0; i < list.size(); i++) {
				CustomDate startDate = list.get(i).getStartDate();
				if (startDate != null && startDate.dateEqual(date))
					result.add(list.get(i));
			}
		}
		return result;
	}

	public static ArrayList<Task> searchEndDate(ArrayList<Task> list,
			CustomDate date) {
		ArrayList<Task> result = new ArrayList<Task>();
		if (date.getHour() != 23 && date.getMinute() != 59) {
			for (int i = 0; i < list.size(); i++) {
				CustomDate endDate = list.get(i).getEndDate();
				if (endDate != null && endDate.compareTo(date) == 0)
					result.add(list.get(i));
			}
		} else {
			for (int i = 0; i < list.size(); i++) {
				CustomDate endDate = list.get(i).getEndDate();
				if (endDate != null && endDate.dateEqual(date))
					result.add(list.get(i));
			}
		}
		return result;
	}

	public static ArrayList<Task> searchWorkInfo(ArrayList<Task> list,
			String workInfo) {
		ArrayList<Task> result = new ArrayList<Task>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getWorkInfo().equals(workInfo))
				result.add(list.get(i));
		}
		return result;
	}

	private static void sortList(ArrayList<Task> list) {
		Collections.sort(list);
	}
}
