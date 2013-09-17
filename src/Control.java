public class Control {
	enum COMMAND_TYPES {
		ADD, REMOVE, SEARCH, EDIT, COMPLETE, INCOMPLETE, UNDO, REDO, CLEAR_ALL, TODAY, SHOW_ALL, SYNC, SETTINGS, HELP, EXIT, INVALID, MARK, UNMARK
	}

	private static String executeCommand(String userCommandString) {
		boolean isEmptyCommand = userCommandString.trim().equals("");

		if (isEmptyCommand) {
			return String.format("INVALID_COMMAND_FORMAT", userCommandString);
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

	private static String executeAddCommand(String[] splittedUserCommand) {
		if (splitedUserCommand.length < 1) {
			return String.format("Invalid command");
		}
		String startTime = null;
		String endTime = null;
		String tag = null;
		boolean hasTag = false;
		boolean isImptTask = false;
		String workInfo = splittedUserCommand[0];
		for (int i = 1; i < splittedUserCommand.length; i++) {
			if (isStartTime(splittedUserCommand[i])) {
				startTime = splittedUserCommand[i];
			} else if (isEndTime(splittedUserCommand[i])) {
				endTime = splittedUserCommand[i];
			} else if (hasTag(splittedUserCommand[i])) {
				hasTag = true;
				tag = splittedUserCommand[i];
			} else if (isImportantTask(splittedUserCommand[i])) {
				isImptTask = true;
			}
		}

		Task task = new Task(workInfo);

		if (startTime != null) {
			task.setStartTime(new CustomDate(removeKeyWords(startTime));
		}
		if (endTime != null) {
			task.setEndTime(new CustomDate(endTime);
		}
		if (hasTag) {
			task.setTag(tag);
		}
		if (isImptTask) {
			task.setImptTask();
		}
	}

	private static String[] splitCommandString(String userCommand) {
		String content = removeFirstWord(userCommand);

		if (content.contains(",")) {
			return splitCommandStringByComma(content);
		} else {
			return splitCommandStringBySpace(content);
		}
	}

	private static String[] splitCommandStringByComma(String content) {
		return content.trim().split(",");
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

	private static boolean isStartTime(String content) {
		if (content.contains("start") || content.contains("begin")
				|| content.contains("from")) {
			return true;
		}
		return false;
	}

	private static boolean isEndTime(String content) {
		if (content.contains("end") || content.contains("to")
				|| content.contains("till") || content.contains("until")
				|| content.contains("by") || content.contains("due")) {
			return true;
		}
		return false;
	}

	private static boolean hasTag(String content) {
		return content.contains("#");
	}

	private static boolean isImportantTask(String content) {
		return content.contains("*");
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
