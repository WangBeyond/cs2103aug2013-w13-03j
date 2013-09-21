import java.util.Scanner;

public class Parser {
	static enum COMMAND_TYPES {
		ADD, REMOVE, SEARCH, EDIT, COMPLETE, INCOMPLETE, UNDO, REDO, CLEAR_ALL, TODAY, SHOW_ALL, SYNC, SETTINGS, HELP, EXIT, INVALID, MARK, UNMARK
	}

	private static String[] startDateKeys = { "start from", "start at",
			"start on", "begin from", "begin at", "begin on", "from" };
	private static String[] endDateKeys = { "end on", "end at", "end by",
			"end before", "to", "till", "until", "by", "due" };

	public static final int INDEX_IS_MATCH = 0;
	public static final int INDEX_WORK_INFO = 1;
	public static final int INDEX_START_DATE = 2;
	public static final int INDEX_END_DATE = 3;

	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NULL = "null";

	/*Test Parse Function
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String s;
		while (sc.hasNextLine()) {
			s = sc.nextLine();
			try {
				String[] result = parseEditCommand(s);
				for (String u : result)
					System.out.println(u);
			} catch (Exception e) {
				System.out.println("Invalid Command");
			}
		}
	}
	*/

	public static String[] parseAddCommand(String content) {
		if (content.isEmpty()) {
			String[] s = { null, null, null, null, null };
			return s;
		}

		String[] tempResult = new String[4];
		String workInfo = "";
		String startDateString = "";
		String endDateString = "";
		String tag = "";
		String isImpt = "";

		content = content.trim();

		if (isImptTask(content)) {
			isImpt = TRUE;
			content = removeImptMark(content);
		}
		if (hasTag(content)) {
			tag = getTagName(content);
			content = removeTag(content);
		}

		String commandString = content;

		tempResult = matchDatePattern1(commandString);
		if (tempResult[INDEX_IS_MATCH] == TRUE) {
			workInfo = tempResult[INDEX_WORK_INFO];
			startDateString = tempResult[INDEX_START_DATE];
			endDateString = tempResult[INDEX_END_DATE];
			String[] result = new String[5];
			result[0] = workInfo;
			result[1] = startDateString;
			result[2] = endDateString;
			result[3] = tag;
			result[4] = isImpt;
			return result;
		}

		tempResult = matchDatePattern2(commandString);
		if (tempResult[INDEX_IS_MATCH] == TRUE) {
			workInfo = tempResult[INDEX_WORK_INFO];
			startDateString = tempResult[INDEX_START_DATE];
			endDateString = tempResult[INDEX_END_DATE];
			String[] result = new String[5];
			result[0] = workInfo;
			result[1] = startDateString;
			result[2] = endDateString;
			result[3] = tag;
			result[4] = isImpt;
			return result;
		}

		tempResult = matchDatePattern3(commandString);
		if (tempResult[INDEX_IS_MATCH] == TRUE) {
			workInfo = tempResult[INDEX_WORK_INFO];
			startDateString = tempResult[INDEX_START_DATE];
			endDateString = tempResult[INDEX_END_DATE];
			String[] result = new String[5];
			result[0] = workInfo;
			result[1] = startDateString;
			result[2] = endDateString;
			result[3] = tag;
			result[4] = isImpt;
			return result;
		}

		tempResult = matchDatePattern4(commandString);
		if (tempResult[INDEX_IS_MATCH] == TRUE) {
			workInfo = tempResult[INDEX_WORK_INFO];
			startDateString = tempResult[INDEX_START_DATE];
			endDateString = tempResult[INDEX_END_DATE];
			String[] result = new String[5];
			result[0] = workInfo;
			result[1] = startDateString;
			result[2] = endDateString;
			result[3] = tag;
			result[4] = isImpt;
			return result;
		}

		tempResult = matchDatePattern5(commandString);
		if (tempResult[INDEX_IS_MATCH] == TRUE) {
			workInfo = tempResult[INDEX_WORK_INFO];
			startDateString = tempResult[INDEX_START_DATE];
			endDateString = tempResult[INDEX_END_DATE];
			String[] result = new String[5];
			result[0] = workInfo;
			result[1] = startDateString;
			result[2] = endDateString;
			result[3] = tag;
			result[4] = isImpt;
			return result;
		}

		String[] result = new String[5];
		result[0] = commandString;
		result[1] = "";
		result[2] = "";
		result[3] = tag;
		result[4] = isImpt;

		return result;
	}

	/* pattern: <start key> <date> <end key> <date> */
	private static String[] matchDatePattern1(String commandString) {
		String[] result = { FALSE, NULL, NULL, NULL };
		String workInfo = "";
		String unmatchedString = commandString;
		boolean isStartDateMatch = false;
		// find last occurrence of a <start key>
		for (int i = 0; i < startDateKeys.length; i++) {
			int index = unmatchedString.lastIndexOf(startDateKeys[i]);
			if (index < 0) {
				continue;
			} else {
				int length = startDateKeys[i].length();
				isStartDateMatch = true;
				workInfo = unmatchedString.substring(0, index - 1).trim();
				unmatchedString = unmatchedString.substring(index + length,
						unmatchedString.length());
				break;
			}
		}
		if (isStartDateMatch) {
			String startDateString = "";
			String endDateString = "";
			boolean isEndDateMatch = false;
			// find last occurrence of a <end key>
			for (int i = 0; i < endDateKeys.length; i++) {
				int index = unmatchedString.lastIndexOf(endDateKeys[i]);
				if (index < 0) {
					continue;
				} else {
					int length = endDateKeys[i].length();
					isEndDateMatch = true;
					startDateString = unmatchedString.substring(0, index - 1)
							.trim();
					endDateString = unmatchedString.substring(index + length,
							unmatchedString.length()).trim();
					break;
				}
			}
			if (isEndDateMatch) {
				CustomDate DateTester = new CustomDate();
				if (DateTester.convert(startDateString) == 1
						&& DateTester.convert(endDateString) == 1) {
					result[INDEX_IS_MATCH] = TRUE;
					result[INDEX_WORK_INFO] = workInfo;
					result[INDEX_START_DATE] = startDateString;
					result[INDEX_END_DATE] = endDateString;
					return result;
				}
			}
		}
		return result;
	}

	/* pattern: <date> <end key> <date> */
	private static String[] matchDatePattern2(String commandString) {
		String[] result = { FALSE, NULL, NULL, NULL };
		String unmatchedString = commandString;
		String workInfo = "";
		String startDateString = "";
		String endDateString = "";
		boolean isEndDateMatch = false;
		boolean isStartDateMatch = false;
		// find last occurrence of a <end key>
		for (int i = 0; i < endDateKeys.length; i++) {
			int index = unmatchedString.lastIndexOf(endDateKeys[i]);
			if (index < 0) {
				continue;
			} else {
				int length = endDateKeys[i].length();
				isEndDateMatch = true;
				endDateString = unmatchedString.substring(index + length,
						unmatchedString.length()).trim();
				unmatchedString = unmatchedString.substring(0, index - 1)
						.trim();
				break;
			}
		}
		if (isEndDateMatch) {
			CustomDate dateTester = new CustomDate();
			if (dateTester.convert(endDateString) == 1) {
				while (!unmatchedString.isEmpty()) {
					if (dateTester.convert(getLastWord(unmatchedString)) == 1) {
						isStartDateMatch = true;
						if (dateTester.convert(getLastWord(unmatchedString)
								+ " " + startDateString) == 1) {
							startDateString = getLastWord(unmatchedString)
									+ " " + startDateString;
							unmatchedString = removeLastWord(unmatchedString);
							workInfo = unmatchedString;
						} else {
							break;
						}
					} else {
						break;
					}
				}
			}
		}
		if (isStartDateMatch) {
			result[INDEX_IS_MATCH] = TRUE;
			result[INDEX_WORK_INFO] = workInfo;
			result[INDEX_START_DATE] = startDateString.trim();
			result[INDEX_END_DATE] = endDateString;
		}
		return result;
	}

	/* pattern: <start key> <date> */
	private static String[] matchDatePattern3(String commandString) {
		String[] result = { FALSE, NULL, NULL, NULL };
		String workInfo = "";
		String unmatchedString = commandString;
		String startDateString = "";
		boolean isStartDateMatch = false;
		CustomDate dateTester = new CustomDate();
		// find last occurrence of a <start key>
		for (int i = 0; i < startDateKeys.length; i++) {
			int index = unmatchedString.lastIndexOf(startDateKeys[i]);
			if (index < 0) {
				continue;
			} else {
				int length = startDateKeys[i].length();
				isStartDateMatch = true;
				workInfo = unmatchedString.substring(0, index - 1).trim();
				startDateString = unmatchedString.substring(index + length,
						unmatchedString.length()).trim();
				break;
			}
		}
		if (isStartDateMatch) {
			if (dateTester.convert(startDateString) == 1) {
				result[INDEX_IS_MATCH] = TRUE;
				result[INDEX_WORK_INFO] = workInfo;
				result[INDEX_START_DATE] = startDateString;
			}
		}
		return result;
	}

	/* pattern: <end key> <date> */
	private static String[] matchDatePattern4(String commandString) {
		String[] result = { FALSE, NULL, NULL, NULL };
		String workInfo = "";
		String unmatchedString = commandString;
		String endDateString = "";
		boolean isEndDateMatch = false;
		CustomDate dateTester = new CustomDate();
		// find last occurrence of a <end key>
		for (int i = 0; i < endDateKeys.length; i++) {
			int index = unmatchedString.lastIndexOf(endDateKeys[i]);
			if (index < 0) {
				continue;
			} else {
				int length = endDateKeys[i].length();
				isEndDateMatch = true;
				workInfo = unmatchedString.substring(0, index - 1).trim();
				endDateString = unmatchedString.substring(index + length,
						unmatchedString.length()).trim();
				break;
			}
		}
		if (isEndDateMatch) {
			if (dateTester.convert(endDateString) == 1) {
				result[INDEX_IS_MATCH] = TRUE;
				result[INDEX_WORK_INFO] = workInfo;
				result[INDEX_END_DATE] = endDateString;
			}
		}
		return result;
	}

	/* pattern: <date> */
	private static String[] matchDatePattern5(String commandString) {
		String[] result = { FALSE, NULL, NULL, NULL };
		String workInfo = "";
		String startDateString = "";
		boolean isStartDate = false;
		String unmatchedString = commandString;
		CustomDate dateTester = new CustomDate();
		while (!unmatchedString.isEmpty()) {
			if (dateTester.convert(getLastWord(unmatchedString)) == 1) {
				isStartDate = true;
				startDateString = getLastWord(unmatchedString) + " "
						+ startDateString;
				unmatchedString = removeLastWord(unmatchedString);
				workInfo = unmatchedString;
			} else {
				break;
			}
		}
		if (isStartDate) {
			result[INDEX_IS_MATCH] = TRUE;
			result[INDEX_WORK_INFO] = workInfo;
			result[INDEX_START_DATE] = startDateString;
		}
		return result;
	}

	public static boolean checkEmptyCommand(String userCommand) {
		return userCommand.trim().equals("");
	}

	public static COMMAND_TYPES determineCommandType(String userCommand) {
		String commandTypeString = getFirstWord(userCommand);

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
		} else if (isMarkCommand(commandTypeString)) {
			return COMMAND_TYPES.MARK;
		} else if (isUnmarkCommand(commandTypeString)) {
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

	/* Determine COMMAND TYPE Section */
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

	public static String[] parseCommand(String userCommand,
			COMMAND_TYPES commandType) {
		String content = removeFirstWord(userCommand);
		if (commandType == COMMAND_TYPES.ADD)
			return parseAddCommand(content);
		else if (commandType == COMMAND_TYPES.SEARCH)
			return parseSearchCommand(content);
		else if (commandType == COMMAND_TYPES.EDIT)
			return parseEditCommand(content);
		else if (commandType == COMMAND_TYPES.REMOVE
				|| commandType == COMMAND_TYPES.COMPLETE
				|| commandType == COMMAND_TYPES.INCOMPLETE
				|| commandType == COMMAND_TYPES.MARK
				|| commandType == COMMAND_TYPES.UNMARK)
			return parseIndexCommand(content);
		else
			return null;
	}

	private static String[] parseIndexCommand(String content) {
		String[] splittedUserCommand = splitCommandBySpace(content);
		for (String s : splittedUserCommand)
			Integer.parseInt(s);

		return splittedUserCommand;
	}

	private static String[] splitCommandBySpace(String content) {
		return content.split(" ");
	}

	private static String[] splitCommandByComma(String content) {
		String[] splittedContent = content.split(",");
		for (int i = 0; i < splittedContent.length; i++) {
			splittedContent[i] = splittedContent[i].trim();
		}
		return splittedContent;
	}

	private static String[] parseEditCommand(String content) {
		String[] splittedUserCommand = splitCommandBySpace(content);
		if (splittedUserCommand.length < 2)
			throw new IllegalArgumentException();

		int index = Integer.parseInt(getFirstWord(content));

		String[] temp = parseSearchCommand(removeFirstWord(content));

		String[] parsedCommand = new String[5];
		parsedCommand[0] = String.valueOf(index);
		for (int i = 1; i < parsedCommand.length; i++) {
			parsedCommand[i] = temp[i];
		}
		return parsedCommand;
	}

	private static String[] parseSearchCommand(String content) {
		String[] splittedUserCommand = splitCommandByComma(content);
		if (splittedUserCommand.length == 0)
			throw new IllegalArgumentException();

		String workInfo = "";
		String tagKey = NULL;
		String startDate = NULL;
		String endDate = NULL;
		int startWorkInfoIndex = -1;
		int endWorkInfoIndex = -1;
		boolean hasStartDate = false;
		boolean hasEndDate = false;
		boolean hasImportantTaskKey = false;
		boolean hasTagKey = false;
		boolean hasWorkInfo = false;

		for (int i = splittedUserCommand.length - 1; i >= 0; i--) {
			if (isImptTask(splittedUserCommand[i])) {
				if (hasImportantTaskKey)
					throw new IllegalArgumentException();
				hasImportantTaskKey = true;
				if (endWorkInfoIndex != -1 && startWorkInfoIndex == -1) {
					startWorkInfoIndex = i + 1;
					hasWorkInfo = true;
				}
			} else if (hasTag(splittedUserCommand[i])) {
				if (hasTagKey)
					throw new IllegalArgumentException();
				tagKey = getTagName(splittedUserCommand[i]);
				hasTagKey = true;
				if (endWorkInfoIndex != -1 && startWorkInfoIndex == -1) {
					startWorkInfoIndex = i + 1;
					hasWorkInfo = true;
				}
			} else if (isStartDate(splittedUserCommand[i])
					&& (getStartDate(splittedUserCommand[i]) != null)) {
				if (hasStartDate)
					throw new IllegalArgumentException();
				startDate = getStartDateString(splittedUserCommand[i]);
				hasStartDate = true;
				if (endWorkInfoIndex != -1 && startWorkInfoIndex == -1) {
					startWorkInfoIndex = i + 1;
					hasWorkInfo = true;
				}
			} else if (isEndDate(splittedUserCommand[i])
					&& (getEndDate(splittedUserCommand[i]) != null)) {
				if (hasEndDate)
					throw new IllegalArgumentException();
				endDate = getEndDateString(splittedUserCommand[i]);
				hasEndDate = true;
				if (endWorkInfoIndex != -1 && startWorkInfoIndex == -1) {
					startWorkInfoIndex = i + 1;
					hasWorkInfo = true;
				}
			} else if (!hasWorkInfo) {
				if (endWorkInfoIndex == -1)
					endWorkInfoIndex = i;
			} else
				throw new IllegalArgumentException();
		}

		if (endWorkInfoIndex != -1) {
			startWorkInfoIndex = (startWorkInfoIndex == -1) ? 0
					: startWorkInfoIndex;
			for (int i = startWorkInfoIndex; i <= endWorkInfoIndex; i++) {
				workInfo += splittedUserCommand[i]
						+ (i == endWorkInfoIndex ? "" : ", ");
			}
		}

		String[] parsedCommand = new String[5];
		parsedCommand[0] = workInfo.equals("") ? NULL : workInfo;
		parsedCommand[1] = startDate;
		parsedCommand[2] = endDate;
		parsedCommand[3] = tagKey;
		parsedCommand[4] = (hasImportantTaskKey == true) ? TRUE : FALSE;
		return parsedCommand;
	}

	/* String Modifying Section */
	static String getFirstWord(String userCommand) {
		String commandTypeString = userCommand.trim().split("\\s+")[0];
		return commandTypeString;
	}

	static String removeFirstWord(String userCommand) {
		return userCommand.replaceFirst(getFirstWord(userCommand), "").trim();
	}

	private static String removeLastWord(String commandString) {
		String lastWord = getLastWord(commandString);
		return commandString.substring(0,
				commandString.length() - lastWord.length()).trim();
	}

	private static String getLastWord(String commandString) {
		String[] stringArray = commandString.trim().split("\\s+");
		return stringArray[stringArray.length - 1];
	}

	public static String getStartDateString(String s) {
		return removeInputKeys(s, startDateKeys);
	}

	public static String getEndDateString(String s) {
		return removeInputKeys(s, endDateKeys);
	}

	private static String removeInputKeys(String s, String[] keys) {
		String StringRemovedKey = null;
		for (int i = 0; i < keys.length; i++) {
			if (s.startsWith(keys[i])) {
				StringRemovedKey = s.replaceFirst(keys[i], " ").trim();
				break;
			}
		}
		return StringRemovedKey;
	}

	private static String getTagName(String s) {
		return getLastWord(s).replace("#", "").trim();
	}

	public static boolean isStartDate(String content) {
		for (int i = 0; i < startDateKeys.length; i++) {
			if (content.startsWith(startDateKeys[i])) {
				return true;
			}
		}
		return false;
	}

	public static CustomDate getStartDate(String content) {
		CustomDate tempDate = new CustomDate();
		String DateString = getStartDateString(content);
		if (tempDate.convert(DateString) == 1)
			return tempDate;
		else
			return null;
	}

	public static boolean isEndDate(String content) {
		for (int i = 0; i < endDateKeys.length; i++) {
			if (content.startsWith(endDateKeys[i])) {
				return true;
			}
		}
		return false;
	}

	public static CustomDate getEndDate(String content) {
		CustomDate tempDate = new CustomDate();
		String DateString = getEndDateString(content);
		if (tempDate.convert(DateString) == 1)
			return tempDate;
		else
			return null;
	}

	private static boolean hasTag(String commandString) {
		String lastWord = getLastWord(commandString);
		return lastWord.startsWith("#");
	}

	private static String removeTag(String commandString) {
		if (hasTag(commandString)) {
			return removeLastWord(commandString);
		}
		return commandString;
	}

	private static boolean isImptTask(String commandString) {
		return commandString.charAt(commandString.length() - 1) == '*';
	}

	private static String removeImptMark(String commandString) {
		if (isImptTask(commandString)) {
			return commandString.substring(0, commandString.length() - 1)
					.trim();
		}
		return commandString;
	}
}
