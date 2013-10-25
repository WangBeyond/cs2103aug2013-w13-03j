import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import javafx.collections.ObservableList;

public class Parser {
	static enum COMMAND_TYPES {
		ADD, REMOVE, SEARCH, EDIT, COMPLETE, INCOMPLETE, UNDO, REDO, CLEAR_ALL, TODAY, SHOW_ALL, SYNC, SETTINGS, HELP, EXIT, INVALID, MARK, UNMARK
	}

	/* start date keys */
	static String[] startDateKeys = { "start from", "start at",
			"start on", "begin from", "begin at", "begin on", "from", "after", "on",
			"at"};

	/* end date keys */
	static String[] endDateKeys = { "end on", "end at", "end by",
			"end before", "to", "till", "until", "by", "due", "before", "next", "today",
			"tonight" };

	static String[] repeatingKeys = { "daily", "weekly", "monthly",
			"yearly", "annually", "every monday", "every tuesday",
			"every wednesday", "every thursday", "every friday",
			"every saturday", "every sunday", "every day", "every month",
			"every year", "everyday", "every week" };
	
	private static String[] occurenceKeys = { "time", "times" };
	
	public static final int INDEX_USELESS_INFO = -2;
	public static final int INDEX_COMMAND_TYPE = -1;
	public static final int INDEX_WORK_INFO = 0;
	public static final int INDEX_TAG = 1;
	public static final int INDEX_START_DATE = 2;
	public static final int INDEX_END_DATE = 3;
	public static final int INDEX_IS_IMPT = 4;
	public static final int INDEX_REPEATING = 5;
	public static final int INDEX_INDEX_INFO = 6;

	/* maximum length of a date. Example: next Monday 4pm */
	public static final int MAX_DATE_LENGTH = 4;

	public static final String IMPT_MARK = "*";
	public static final String HASH_TAG = "#";
	public static final String HYPHEN = "-";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NULL = "null";
	public static final String START_KEY = "start key";
	public static final String END_KEY = "end key";

	private static final String NO_EDITING_INFO = "No infos for editing";
	private static final String INVALID_INDEX = "Invalid index";
	private static final String INVALID_RANGE_END_SMALLER = "Invalid range as end point is smaller than start point";
	private static final String INVALID_RANGE = "Invalid Range";

	private static final int START_DATE = 0;
	private static final int END_DATE = 1;

	private static final int VALID = 1;
	private static final int INVALID = -1;
	
	private static Model model;
	private static View view;

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
			COMMAND_TYPES commandType, Model model, View view) {
		String content = removeFirstWord(userCommand);
		content = removeUnneededSpaces(content);
		if (commandType == COMMAND_TYPES.ADD)
			return parseCommandWithInfo(content, COMMAND_TYPES.ADD);
		else if (commandType == COMMAND_TYPES.SEARCH)
			return parseCommandWithInfo(content, COMMAND_TYPES.SEARCH);
		else if (commandType == COMMAND_TYPES.EDIT)
			return parseEditCommand(content);
		else if (commandType == COMMAND_TYPES.REMOVE)
			return parseRemoveCommand(content, view, model);
		else if (commandType == COMMAND_TYPES.COMPLETE
				|| commandType == COMMAND_TYPES.INCOMPLETE
				|| commandType == COMMAND_TYPES.MARK
				|| commandType == COMMAND_TYPES.UNMARK)
			return parseCommandWithIndex(content);
		else if (commandType == COMMAND_TYPES.SYNC){
			return parseSyncCommand(content);
		} else
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
			throw new IllegalArgumentException(INVALID_INDEX);
		}

		if (splittedUserCommand.length < 2)
			throw new IllegalArgumentException(NO_EDITING_INFO);

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

		parsedCommand[INDEX_WORK_INFO] = workInfo;
		parsedCommand[INDEX_START_DATE] = startDateString;
		parsedCommand[INDEX_END_DATE] = endDateString;
		parsedCommand[INDEX_TAG] = tag;
		parsedCommand[INDEX_IS_IMPT] = isImpt;
		parsedCommand[INDEX_REPEATING] = repeatingType;

		return parsedCommand;
	}

	private static String[] parseRemoveCommand(String content, View view, Model model) {
		try {
			return parseCommandWithIndex(content);
		} catch (Exception e) {
			System.out.println(TwoWayCommand.listedIndexType);
			ObservableList<Task> modifiedList;
			int tabIndex = view.tabPane.getSelectionModel().getSelectedIndex();
			if (tabIndex == Command.PENDING_TAB) {
				modifiedList = model.getSearchPendingList();
			} else if (tabIndex == Command.COMPLETE_TAB) {
				modifiedList = model.getSearchCompleteList();
			} else {
				modifiedList = model.getSearchTrashList();
			}
			String indexRange = "1" + HYPHEN + modifiedList.size();
			return parseCommandWithIndex(indexRange);
		} 
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
		Vector<String> indexList = new Vector<String>();

		if (splittedUserCommand.length < 1)
			throw new IllegalArgumentException("No indexes");

		try {
			for (String s : splittedUserCommand) {
				if (s.contains(HYPHEN))
					processRange(indexList, s);
				else
					indexList.add(String.valueOf(Integer.parseInt(s)));
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Index is invalid");
		}

		splittedUserCommand = indexList.toArray(new String[0]);
		return splittedUserCommand;
	}

	private static String[] parseSyncCommand(String content){
		String[] splittedUserCommand = splitBySpace(content);
		
		if(splittedUserCommand.length != 1 && !splittedUserCommand[0].equals("") && splittedUserCommand.length != 2){
			throw new IllegalArgumentException("Invalid sync command.");
		}
		return splittedUserCommand;
	}
	
	private static void processRange(Vector<String> indexList, String s) {
		String[] limits = s.split(HYPHEN);
		if (limits.length > 2)
			throw new IllegalArgumentException(INVALID_RANGE);
		int startPoint, endPoint;
		startPoint = Integer.parseInt(limits[0]);
		endPoint = Integer.parseInt(limits[1]);
		if (startPoint > endPoint)
			throw new IllegalArgumentException(INVALID_RANGE_END_SMALLER);
		for (int i = startPoint; i <= endPoint; i++)
			indexList.add(String.valueOf(i));
	}

	/**
	 * This method is used to parse the command when any key event occurs and
	 * highlight the command to indicate the understanding of the command by the
	 * program to user to assist user to type more exact command in real-time
	 * before the user presses Enter
	 * 
	 * @param command
	 */
	static ArrayList<InfoWithIndex> parseForView(String command) {
		ArrayList<InfoWithIndex> infoList = new ArrayList<InfoWithIndex>();
		COMMAND_TYPES commandType;
		String commandTypeStr;

		commandType = determineCommandType(command);
		if (commandType == COMMAND_TYPES.INVALID) {
			infoList.add(new InfoWithIndex(command, 0, INDEX_USELESS_INFO));
			return infoList;
		}
		
		int indexCommand = command.indexOf(getFirstWord(command));
		commandTypeStr = completeWithSpace(getFirstWord(command), command, indexCommand);
		while(indexCommand != 0){
			commandTypeStr = (command.charAt(indexCommand) == '\t' ? "\t" : " ") + commandTypeStr;
			indexCommand--;
		}
		
		infoList.add(new InfoWithIndex(commandTypeStr, 0, INDEX_COMMAND_TYPE));
		try {
			String[] result = parseCommand(command, commandType, model, view);
			// Add the commandType first

			if (commandType == COMMAND_TYPES.EDIT) {
				String index = result[0];
				String indexWithSpace = completeWithSpace(index, command,
						command.indexOf(index));
				InfoWithIndex indexInfo = new InfoWithIndex(indexWithSpace,
						command.indexOf(index), INDEX_INDEX_INFO);
				infoList.add(indexInfo);
				for (int i = 0; i < result.length - 1; i++)
					result[i] = result[i + 1];
				result[result.length - 1] = NULL;
			}

			// First consider command with info
			if (commandType == COMMAND_TYPES.ADD
					|| commandType == COMMAND_TYPES.SEARCH
					|| commandType == COMMAND_TYPES.EDIT) {
				for (int infoIndex = 0; infoIndex < result.length; infoIndex++) {
					String info = result[infoIndex];
					// append the preposition or date keys with date info
					if (infoIndex == INDEX_START_DATE && info != NULL)
						info = appendWithDateKey(info, command, START_DATE);
					if (infoIndex == INDEX_END_DATE && info != NULL)
						info = appendWithDateKey(info, command, END_DATE);
					// Add * if command has
					if (infoIndex == INDEX_IS_IMPT && info == TRUE) {
						String markStr = completeWithSpace(IMPT_MARK, command,
								command.indexOf(IMPT_MARK));
						InfoWithIndex imptInfo = new InfoWithIndex(markStr,
								command.indexOf(IMPT_MARK), INDEX_IS_IMPT);
						infoList.add(imptInfo);
					}
					if (command.contains(info)) {
						// To get the index of workflow, we need to get rid of
						// the commandType string first,
						// otherwise some mistakes make occur: eg: add ad will
						// return workflow index 1
						int startIndex;
						if (infoIndex == INDEX_WORK_INFO) {
							String temp = command.substring(commandTypeStr
									.length());
							startIndex = temp.indexOf(info)
									+ commandTypeStr.length();
						} else
							startIndex = command.indexOf(info);
						info = completeWithSpace(info, command, startIndex);
						InfoWithIndex ci = new InfoWithIndex(info, startIndex,
								infoIndex);
						infoList.add(ci);
					}
				}
				infoList = addInUselessInfo(infoList, command);
			} else { // Consider command with index, just add the part except
						// commandType,
						// but if the commandType is invalid, just add them as
						// uselessInfo
				int beginIndex = commandTypeStr.length();
				if (commandType != COMMAND_TYPES.INVALID
						&& commandType.equals(COMMAND_TYPES.REMOVE)
						|| commandType.equals(COMMAND_TYPES.COMPLETE)
						|| commandType.equals(COMMAND_TYPES.INCOMPLETE)
						|| commandType.equals(COMMAND_TYPES.MARK)
						|| commandType.equals(COMMAND_TYPES.UNMARK)) {
					infoList.add(new InfoWithIndex(command
							.substring(beginIndex), beginIndex,
							INDEX_INDEX_INFO));
				} else {
					infoList.add(new InfoWithIndex(command
							.substring(beginIndex), beginIndex,
							INDEX_USELESS_INFO));
				}

			}
			return infoList;
		} catch (Exception e) {
			infoList.clear();
			infoList.add(new InfoWithIndex(commandTypeStr, 0, INDEX_COMMAND_TYPE));
			String remainingInfo = removeFirstWord(command);
			if (e.getMessage()!=null && e.getMessage().equals(NO_EDITING_INFO))
				infoList.add(new InfoWithIndex(remainingInfo, commandTypeStr
						.length(), INDEX_INDEX_INFO));
			else
				infoList.add(new InfoWithIndex(remainingInfo, commandTypeStr.length(),
						INDEX_USELESS_INFO));
			return infoList;
		}
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
				String stringAfterKey;
				if (keys[i].equals("today") || keys[i].equals("tonight")
						|| keys[i].equals("tomorrow") || keys[i].equals("next"))
					stringAfterKey = keys[i] + " "
							+ temp.substring(keyIndex + keyLength).trim();
				else
					stringAfterKey = temp.substring(keyIndex + keyLength)
							.trim();

				int dateIndex = isValidDate(stringAfterKey);
				if (dateIndex == INVALID) {
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

	/*********************** some methods used for parseForView ***********************************************/

	/**
	 * append the date with its front preposition like start from or so on
	 * 
	 * @param date
	 * @param command
	 * @param startOrEnd
	 *            is the date type start date or end date
	 * @return the date with preposition in front
	 */
	private static String appendWithDateKey(String date, String command,
			int startOrEnd) {
		int startIndex = command.indexOf(date);
		int secondSpaceIndex = startIndex - 2;
		String dateWithPreposition = date;
		while (secondSpaceIndex > 0 && command.charAt(secondSpaceIndex) != ' ') {
			secondSpaceIndex--;
		}
		int firstSpaceIndex = secondSpaceIndex - 1;
		while (firstSpaceIndex > 0 && command.charAt(firstSpaceIndex) != ' ') {
			firstSpaceIndex--;
		}
		if (doesArrayContain(startOrEnd == START_DATE ? startDateKeys
				: endDateKeys, command.substring(firstSpaceIndex + 1,
				startIndex)))
			dateWithPreposition = command.substring(firstSpaceIndex + 1,
					startIndex) + date;
		else if (doesArrayContain(startOrEnd == START_DATE ? startDateKeys
				: endDateKeys, command.substring(secondSpaceIndex + 1,
				startIndex)))
			dateWithPreposition = command.substring(secondSpaceIndex + 1,
					startIndex) + date;
		return dateWithPreposition;
	}

	/**
	 * add the remaining info without highlighted by parser to infoList with
	 * InfoType: INDEX_USELESS_INFO
	 * 
	 * @param infoList
	 * @param command
	 * @return complete infoList
	 */
	private static ArrayList<InfoWithIndex> addInUselessInfo(
			ArrayList<InfoWithIndex> infoList, String command) {
		Collections.sort(infoList);
		int keyInfoCount = infoList.size();
		for (int i = 0; i < keyInfoCount; i++) {
			int startIndex = infoList.get(i).getEndIndex();
			int endIndex;
			if (i != (keyInfoCount - 1))
				endIndex = infoList.get(i + 1).getStartIndex();
			else
				endIndex = command.length();
			if (startIndex < endIndex)
				infoList.add(new InfoWithIndex(command.substring(startIndex,
						endIndex), startIndex, INDEX_WORK_INFO));
		}
		Collections.sort(infoList);
		return infoList;
	}

	/**
	 * complete a info with its rear spaces
	 * 
	 * @param info
	 * @param command
	 * @param startIndex
	 * @return info with space
	 */
	private static String completeWithSpace(String info, String command,
			int startIndex) {
		int endIndex = startIndex + info.length();
		int i = 0;
		while ((endIndex + i) < command.length()) {
			if(command.charAt(endIndex + i) == ' '){
				info += " ";
				i++;
			} else if(command.charAt(endIndex + i) == '\t'){
				info += "\t";
				i++;
			} else
				break;
		}
		return info;
	}

	/**
	 * Justify does the array contain one specific string
	 * 
	 * @param array
	 * @param element
	 * @return boolean result
	 */
	static boolean doesArrayContain(String[] array, String element) {
		element = element.trim();
		for (int i = 0; i < array.length; i++)
			if (array[i].equals(element))
				return true;
		return false;
	}

	/************************** assisting methods for parseCommandWithInfo ****************************************/

	/**
	 * retrieve the repeating type from the command string
	 * 
	 * @param commandString
	 * @return a string array including command string and repeating tag
	 */
	private static String[] getRepeatingType(String commandString) {
		String repeatingKey = Parser.NULL;
		boolean isOccurence = false;
		for (int i = 0; i < repeatingKeys.length; i++) {
			if (commandString.toLowerCase().contains(repeatingKeys[i])) {
				if (repeatingKey.equals(Parser.NULL))
					repeatingKey = repeatingKeys[i];
				else
					throw new IllegalArgumentException(
							"Invalid Command: More than 1 repetitive signals");
				break;
			}		
		}
		if(!repeatingKey.equals(Parser.NULL)) {
			int repeatingIndex = commandString.indexOf(repeatingKey);
			int remainIndex = repeatingIndex + repeatingKey.length();
			String remainInfo = commandString.substring(remainIndex);
			String[] splitRemainInfo = splitBySpace(remainInfo);
			try {
				Integer.valueOf(splitRemainInfo[0]);
				for( String key : occurenceKeys ) {
					if(splitRemainInfo[1].equals(key)) {
						int endIndex = remainInfo.indexOf(key) + key.length();
						repeatingKey = commandString.substring(repeatingIndex, remainIndex + endIndex);
						isOccurence = true;
						break;
					}
				}
			} catch (NumberFormatException e) {		
			}
			commandString = commandString.replace(repeatingKey, "");
		}
		return new String[] { commandString, repeatingKey };
	}

	/**
	 * Checks whether a string is a valid date or not.
	 * 
	 * @param dateString
	 *            - the string which is being checked.
	 * @return number of words in the date string if the string contains a date;
	 *         otherwise, returns INVALID(-1).
	 */
	static int isValidDate(String dateString) {
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
			if (result == VALID) {
				return tester.length();
			} else if (result == CustomDate.OUT_OF_BOUNDS) {
				throw new IllegalArgumentException(
						"The time is out of bounds. Please recheck!");
			}
			tester = removeLastWord(tester);
		}
		return INVALID;
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

	static String getLastWord(String commandString) {
		String[] stringArray = commandString.trim().split("\\s+");
		return stringArray[stringArray.length - 1];
	}

	static String removeLastWord(String commandString) {
		String lastWord = getLastWord(commandString);
		return commandString.substring(0,
				commandString.length() - lastWord.length()).trim();
	}

	static String getFirstWord(String commandString) {
		String[] stringArray = splitBySpace(commandString);
		return stringArray[0];
	}

	static String removeFirstWord(String commandString) {
		return commandString.replaceFirst(getFirstWord(commandString), "")
				.trim();
	}

	static String[] splitBySpace(String content) {
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

/**
 * class infoWithIndex is assisting parseForView to install the information of
 * parsed command and reflects on commandLine and feedback
 * 
 * 
 */
class InfoWithIndex implements Comparable<InfoWithIndex> {
	String info;
	int startIndex;
	int endIndex;
	int infoType;

	InfoWithIndex(String s, int ind, int infoType) {
		info = s;
		startIndex = ind;
		endIndex = startIndex + info.length();
		this.infoType = infoType;
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

	public int getInfoType() {
		return infoType;
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
