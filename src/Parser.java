import java.util.ArrayList;
import java.util.Collections;


public class Parser {
	static enum COMMAND_TYPES {
		ADD, REMOVE, SEARCH, EDIT, COMPLETE, INCOMPLETE, UNDO, REDO, CLEAR_ALL, TODAY, SHOW_ALL, SYNC, SETTINGS, HELP, EXIT, INVALID, MARK, UNMARK
	}

	/* start date keys */
	private static String[] startDateKeys = { "start from", "start at",
			"start on", "begin from", "begin at", "begin on", "from", "on",
			"at" };

	/* end date keys */
	private static String[] endDateKeys = { "end on", "end at", "end by",
			"end before", "to", "till", "until", "by", "due" };

	private static String[] repeatingKeys = { "daily", "weekly", "monthly",
			"yearly", "annually", "every monday", "every tuesday",
			"every wednesday", "every thursday", "every friday",
			"every saturday", "every sunday", "every day", "every month",
			"every year", "everyday", "every week" };

	public static final int INDEX_WORK_INFO = 0;
	public static final int INDEX_TAG = 1;
	public static final int INDEX_START_DATE = 2;
	public static final int INDEX_END_DATE = 3;
	public static final int INDEX_IS_IMPT = 4;
	public static final int INDEX_REPEATING = 5;

	/* maximum length of a date. Example: next Monday 4pm */
	public static final int MAX_DATE_LENGTH = 4;

	public static final String IMPT_MARK = "*";
	public static final String HASH_TAG = "#";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NULL = "null";
	public static final String START_KEY = "start key";
	public static final String END_KEY = "end key";
	
	/**
	 * This function is used to determine the command type of the command input
	 * from the user
	 * 
	 * @param userCommand
	 *            - the command input read from the user
	 * @return the corresponding command type
	 */
	public static COMMAND_TYPES determineCommandType(String userCommand) {
		String commandTypeString = getFirstWord(userCommand);

		if (commandTypeString == null)
			throw new IllegalArgumentException(
					"Command type string cannot be null!");

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

	/**
	 * This method is used to check whether a command is empty or not
	 */
	public static boolean checkEmptyCommand(String userCommand) {
		return userCommand.trim().equals("");
	}

	/**
	 * This method is used to parse from a string of command input into a string
	 * array of necessary info for a specific command
	 * 
	 * @param userCommand
	 *            command input from the user
	 * @param commandType
	 *            command type of the command input
	 * @return the array of infos necessary for each command
	 */
	public static String[] parseCommand(String userCommand,
			COMMAND_TYPES commandType) {
		String content = removeFirstWord(userCommand);
		content = removeUnneededSpaces(content);
		if (commandType == COMMAND_TYPES.ADD)
			return parseCommandWithInfo(content, COMMAND_TYPES.ADD);
		else if (commandType == COMMAND_TYPES.SEARCH)
			return parseCommandWithInfo(content, COMMAND_TYPES.SEARCH);
		else if (commandType == COMMAND_TYPES.EDIT)
			return parseEditCommand(content);
		else if (commandType == COMMAND_TYPES.REMOVE
				|| commandType == COMMAND_TYPES.COMPLETE
				|| commandType == COMMAND_TYPES.INCOMPLETE
				|| commandType == COMMAND_TYPES.MARK
				|| commandType == COMMAND_TYPES.UNMARK)
			return parseCommandWithIndex(content);
		else
			return null;
	}

	/**
	 * This method is used to parse from the content of the EDIT command to
	 * necessary infos
	 * 
	 * @param content
	 *            command string after removing the command type
	 * @return string array. First field is taskIndex. Second field is workInfo.
	 *         Third field is tag. Fourth field is startDateString. Fifth field
	 *         is endDateString. Sixth field is isImpt.
	 */
	private static String[] parseEditCommand(String content) {
		String[] splittedUserCommand = splitBySpace(content);
		int index;
		try {
			index = Integer.parseInt(getFirstWord(content));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid index");
		}

		if (splittedUserCommand.length < 2)
			throw new IllegalArgumentException("No infos for editing");

		String[] temp = parseCommandWithInfo(removeFirstWord(content),
				COMMAND_TYPES.EDIT);

		String[] parsedCommand = new String[7];
		parsedCommand[0] = String.valueOf(index);
		for (int i = 1; i < parsedCommand.length; i++) {
			parsedCommand[i] = temp[i - 1];
		}
		return parsedCommand;
	}

	/*******************************************************************************************************************/
	/**
	 * This method is used to parse from content of an ADD or SEARCH command to
	 * necessary infos
	 * 
	 * @param content
	 *            command string after removing the command type
	 * @return an array of strings where the first field contains workInfo, NULL
	 *         if it is empty; the second field contains tag, NULL if it is
	 *         empty; the third field contains startDateString, NULL if it is
	 *         empty; the fourth field contains endDateString, NULL if it is
	 *         empty; the fifth field contains isImpt (TRUE or FALSE).
	 */
	private static String[] parseCommandWithInfo(String content,
			COMMAND_TYPES commandType) {
		String commandString = content.trim();

		String workInfo = NULL;
		String startDateString = NULL;
		String endDateString = NULL;
		String tag = NULL;
		String isImpt = FALSE;
		String repeatingType = NULL;

		String[] result = getRepeatingType(commandString);
		commandString = result[0];
		repeatingType = result[1];

		if (hasMultipleTags(commandString)) {// if contains multiple hash tags
			throw new IllegalArgumentException(
					"Invalid Command: multiple hash tags(#).");
		} else {
			tag = getTagName(commandString);
			commandString = (!tag.equals(NULL)) ? removeTag(commandString)
					: commandString;
		}

		if (hasMultipleImptMarks(commandString)) {// if contains multiple
													// important marks
			throw new IllegalArgumentException(
					"Invalid Command: multiple important marks(*).");
		} else {
			isImpt = isImptTask(commandString);
			commandString = (isImpt.equals(TRUE)) ? removeImptMark(commandString)
					: commandString;
		}

		result = checkDate(commandString, startDateKeys, START_KEY);
		commandString = result[0];
		startDateString = result[1];

		result = checkDate(commandString, endDateKeys, END_KEY);
		commandString = result[0];
		endDateString = result[1];

		if (commandString.trim().equals("")) {
			if (commandType == COMMAND_TYPES.ADD)
				throw new IllegalArgumentException(
						"Invalid command: work information cannot be empty");
			else
				workInfo = Parser.NULL;
		} else
			workInfo = commandString.trim();

		String[] parsedCommand = new String[] { NULL, NULL, NULL, NULL, FALSE,
				NULL };

		parsedCommand[INDEX_WORK_INFO] = workInfo.equals(NULL) ? NULL
				: workInfo;
		parsedCommand[INDEX_START_DATE] = startDateString.equals(NULL) ? NULL
				: startDateString;
		parsedCommand[INDEX_END_DATE] = endDateString.equals(NULL) ? NULL
				: endDateString;
		parsedCommand[INDEX_TAG] = tag.equals(NULL) ? NULL : tag;
		parsedCommand[INDEX_IS_IMPT] = isImpt;
		parsedCommand[INDEX_REPEATING] = repeatingType.equals(NULL) ? NULL
				: repeatingType;

		return parsedCommand;
	}
	
	/*public static void main(String args[]) {
		ArrayList<InfoWithIndex> result = parseForView("add playing football from 1:00 to 2:00 always");
		for(int i=0;i<result.size();i++)
			System.out.println(result.get(i));
	}*/
	
	public static ArrayList<InfoWithIndex> parseForView(String command) {
		COMMAND_TYPES commandType = determineCommandType(command);
		String[] result = parseCommand(command,commandType);
		ArrayList<InfoWithIndex> infoList = new ArrayList<InfoWithIndex>();
		ArrayList<String> splittedCommand = new ArrayList<String>();
		for(int i =0; i<result.length;i++)	{
			String info = result[i];
			if(command.contains(info)) {
				int startIndex = command.indexOf(info);
				InfoWithIndex ci = new InfoWithIndex(info,startIndex, true);
				infoList.add(ci);
			}
		}
		Collections.sort(infoList);
		
		int keyInfoCount = infoList.size();
		//Add the command type
		if(infoList.get(0).getStartIndex()>0)
			infoList.add(new InfoWithIndex(command.substring(0, infoList.get(0).getStartIndex()),0,true));
		System.out.println(infoList.get(infoList.size()-1));
		for(int i=0;i<keyInfoCount;i++) {
			int startIndex = infoList.get(i).getEndIndex();
			int endIndex;
			if(i!=(keyInfoCount-1))
				endIndex = infoList.get(i+1).getStartIndex();
			else 
				endIndex = command.length();
			if(startIndex < endIndex)
				infoList.add(new InfoWithIndex(command.substring(startIndex,endIndex),startIndex,false));
		}
		Collections.sort(infoList);
		/*for(int i = 0;i<infoList.size();i++) {
			splittedCommand.add(infoList.get(i).getInfo());
		}*/
		return infoList;
	}

	
	private static String[] getRepeatingType(String commandString) {
		String repeatingKey = null;
		for (int i = 0; i < repeatingKeys.length; i++) {
			if (commandString.toLowerCase().contains(repeatingKeys[i])) {
				if (repeatingKey == null)
					repeatingKey = repeatingKeys[i];
				else
					throw new IllegalArgumentException(
							"Invalid Command: More than 1 repetitive signals");
				commandString = commandString.replace(repeatingKey, "");
				break;
			}
		}
		return new String[] { commandString, getRepeatingTag(repeatingKey) };
	}

	private static String getRepeatingTag(String key) {
		if (key == null)
			return Parser.NULL;
		if (key.equals("every day") || key.equals("daily")
				|| key.equals("everyday"))
			return "daily";
		else if (key.equals("monthly") || key.equals("every month"))
			return "monthly";
		else if (key.equals("yearly") || key.equals("every year")
				|| key.equals("annually"))
			return "yearly";
		else
			return "weekly";
	}

	/**
	 * This method is used to parse content of commands filled with indexes to
	 * necessary infos
	 * 
	 * @param content
	 *            command string after removing the command type
	 * @return array of indexes in form of strings.
	 */
	private static String[] parseCommandWithIndex(String content) {
		String[] splittedUserCommand = splitBySpace(content);
		if (splittedUserCommand.length < 1)
			throw new IllegalArgumentException("No indexes");

		try {
			for (String s : splittedUserCommand)
				Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Index is invalid");
		}
		return splittedUserCommand;
	}

	/**
	 * This method is used to check a command string for valid date and remove
	 * the valid date from this command string. If the command string contains
	 * more than 2 valid dates, it will throw an exception message.
	 * 
	 * @param commandString
	 *            command string at the moment
	 * @param keys
	 *            list of key words corresponding to the key type
	 * @param keyType
	 *            key type of the date (START or END)
	 * @return array of strings. First field is the commandString after remove
	 *         the valid date. Second field is the string of the date.
	 */
	private static String[] checkDate(String commandString, String[] keys,
			String keyType) {
		String temp = commandString + "";
		boolean hasDate = false;
		String dateString = NULL;
		for (int i = 0; i < keys.length; i++) {
			// find first occurrence of a <key>
			int keyIndex = temp.toLowerCase().indexOf(keys[i]);
			int keyLength = keys[i].length();
			while (keyIndex >= 0) {
				// get string before the date key
				String stringBeforeKey = temp.substring(0, keyIndex).trim();
				// get string after the date key
				String stringAfterKey = temp.substring(keyIndex + keyLength)
						.trim();

				int dateIndex = isValidDate(stringAfterKey);
				if (dateIndex == Control.INVALID) {
					keyIndex = temp.indexOf(keys[i], keyIndex + keyLength);
				} else {
					if (hasDate) {
						throw new IllegalArgumentException(
								"Invalid Command: Multiple Dates");
					}
					dateString = stringAfterKey.substring(0, dateIndex).trim();
					hasDate = true;
					temp = stringBeforeKey.trim() + " "
							+ stringAfterKey.substring(dateIndex).trim();
					keyIndex = temp.indexOf(keys[i]);

				}
			}
		}

		if (hasDate) {
			return new String[] { temp, dateString };
		} else
			return new String[] { commandString, dateString };
	}

	/**
	 * Checks whether a string is a valid date or not.
	 * 
	 * @param dateString
	 *            - the string which is being checked.
	 * @return number of words in the date string if the string contains a date;
	 *         otherwise, returns INVALID(-1).
	 */
	private static int isValidDate(String dateString) {
		CustomDate dateTester = new CustomDate();
		String[] dateStringArray = splitBySpace(dateString);
		int length = 0;

		if (dateStringArray.length >= MAX_DATE_LENGTH) {
			length = MAX_DATE_LENGTH;
		} else {
			length = dateStringArray.length;
		}

		String tester = "";

		// construct the date tester string.
		for (int i = 0; i < length; i++) {
			tester = (tester + " " + dateStringArray[i]);
		}

		tester = tester.trim();

		while (!tester.isEmpty()) {
			int result = dateTester.convert(tester);
			if (result == Control.VALID) {
				return tester.length();
			} else if (result == CustomDate.OUT_OF_BOUNDS) {
				throw new IllegalArgumentException(
						"The time is out of bounds. Please recheck!");
			}
			tester = removeLastWord(tester);
		}
		return Control.INVALID;
	}

	/**
	 * Checks whether the specified command string has more than one hash tags.
	 * 
	 * @param commandString
	 *            - the string which is being checked.
	 * @return true if it contains more than one hash tag; false otherwise.
	 */
	private static boolean hasMultipleTags(String commandString) {
		String[] words = splitBySpace(commandString);
		boolean hasTag = false;

		for (int i = 0; i < words.length; i++) {
			if (words[i].startsWith(HASH_TAG)) {
				if (hasTag) {
					return true;
				}
				hasTag = true;
			}
		}
		return false;
	}

	/**
	 * Removes the first hash tag from the specified string.
	 * 
	 * @param commandString
	 *            - the string from which the hash tag is being removed.
	 * @return the string with hash tag being removed if it contains a hash tag;
	 *         otherwise, returns the same string.
	 */
	private static String removeTag(String commandString) {
		String[] words = splitBySpace(commandString);
		String result = "";
		int index = indexOfTag(commandString);
		if (index >= 0) {
			for (int i = 0; i < words.length; i++) {
				if (i != index) {
					result = result + " " + words[i];
				}
			}
			return result.trim();
		}
		return commandString;
	}

	/**
	 * Returns content of the hash tag in the specified command string.
	 * 
	 * @param commandString
	 *            - the string which may contains a hash tag.
	 * @return the content of the hash tag with '#' being removed if the string
	 *         contains a hash tag; the string NULL otherwise.
	 */
	private static String getTagName(String commandString) {
		String[] words = splitBySpace(commandString);
		for (int i = 0; i < words.length; i++) {
			if (words[i].startsWith(HASH_TAG)) {
				return words[i];
			}
		}
		return NULL;
	}

	/**
	 * Returns the index of the first hash tag in the specified command string.
	 * 
	 * @param commandString
	 *            - the string which is being checked.
	 * @return the starting index if the command string contains a hag tag; -1
	 *         otherwise.
	 */
	private static int indexOfTag(String commandString) {
		String[] words = splitBySpace(commandString);
		for (int i = 0; i < words.length; i++) {
			if (words[i].startsWith(HASH_TAG)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Checks whether the specified command string contains more than one
	 * important marks.
	 * 
	 * @param commandString
	 *            - the string which is being checked.
	 * @return true if it contains more than one important marks; false,
	 *         otherwise.
	 */
	private static boolean hasMultipleImptMarks(String commandString) {
		String[] words = splitBySpace(commandString);
		boolean isImpt = false;

		for (int i = 0; i < words.length; i++) {
			if (words[i].equals(IMPT_MARK)) {
				if (isImpt) {
					return true;
				}
				isImpt = true;
			}
		}

		return false;
	}

	/**
	 * Checks whether the specified command string is important.
	 * 
	 * @param commandString
	 *            - the string which is being checked.
	 * @return the string TRUE if it is a important task; otherwise, returns the
	 *         string FALSE.
	 */
	private static String isImptTask(String commandString) {
		String[] words = splitBySpace(commandString);

		for (int i = 0; i < words.length; i++) {
			if (words[i].equals(IMPT_MARK)) {
				return TRUE;
			}
		}

		return FALSE;
	}

	/**
	 * Removes the important mark from the specified command string.
	 * 
	 * @param commandString
	 *            - the string from which the important mark is being removed.
	 * @return the string after removing the first important mark if it contains
	 *         important mark; otherwise the same string is returned.
	 */
	private static String removeImptMark(String commandString) {
		String[] words = splitBySpace(commandString);
		String result = "";
		for (int i = 0; i < words.length; i++) {
			if (!words[i].equals(IMPT_MARK)) {
				result = result + " " + words[i];
			}
		}
		return result.trim();
	}

	private static String removeUnneededSpaces(String content) {
		String[] words = splitBySpace(content);
		String result = "";
		for (int i = 0; i < words.length; i++)
			result += words[i] + " ";

		return result.trim();
	}

	private static String getLastWord(String commandString) {
		String[] stringArray = commandString.trim().split("\\s+");
		return stringArray[stringArray.length - 1];
	}

	private static String removeLastWord(String commandString) {
		String lastWord = getLastWord(commandString);
		return commandString.substring(0,
				commandString.length() - lastWord.length()).trim();
	}

	private static String getFirstWord(String commandString) {
		String[] stringArray = splitBySpace(commandString);
		return stringArray[0];
	}

	private static String removeFirstWord(String commandString) {
		return commandString.replaceFirst(getFirstWord(commandString), "")
				.trim();
	}

	private static String[] splitBySpace(String content) {
		return content.trim().split("\\s+");
	}

	/******************************** Determine command type Section ***************************************************************/

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
		return commandTypeString.equalsIgnoreCase("done")
				|| commandTypeString.equalsIgnoreCase("complete");
	}

	private static boolean isIncompleteCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("undone")
				|| commandTypeString.equalsIgnoreCase("incomplete");
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


class InfoWithIndex implements Comparable<InfoWithIndex> {
	String info;
	int startIndex;
	int endIndex;
	boolean isKeyInfo;
	
	InfoWithIndex(String s, int ind, boolean isKeyInfo) {
		info = s;
		startIndex = ind;
		endIndex = startIndex+info.length();
		this.isKeyInfo = isKeyInfo;
	}
	
	public String getInfo() {
		return info;
	}
	
	public int getStartIndex() {
		return startIndex;
	}
	
	public int getEndIndex() {
		return endIndex;
	}
	
	public boolean getIsKeyInfo() {
		return isKeyInfo;
	}
	
	public int compareTo(InfoWithIndex c) {
		if (startIndex > c.startIndex)
			return 1;
		else if (startIndex == c.startIndex)
			return 0;
		else 
			return -1;
	}
	
	public String toString() {
		return info;
	}
}
