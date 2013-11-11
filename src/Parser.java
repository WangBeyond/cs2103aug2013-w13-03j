
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.Vector;

import javafx.collections.ObservableList;

import java.util.regex.*;

public class Parser {
	// Keys for recurring task
	private static String[] repeatingKeys = {
			"daily",
			"weekly",
			"monthly",
			"yearly",
			"annually",
			"every monday",
			"every tuesday",
			"every wednesday",
			"every thursday",
			"every friday",
			"every saturday",
			"every sunday",
			"every\\s*\\d*\\s*(days?|weeks?|months?|years?"
					+ "|mondays?|tuesdays?|wednesdays?|thursdays?"
						+ "|fridays?|saturdays?|sundays?)" };


	/* maximum length of a date. Example: next Monday 4pm */
	private static final int MAX_DATE_LENGTH = 4;
	// Signal for keywords of start date
	private static final String START_KEY = "start key";
	// Signal for keywords of end date
	private static final String END_KEY = "end key";
	// Signal for date as start date
	private static final int START_DATE = 0;
	// Signal for date as end date
	private static final int END_DATE = 1;
	// Valid Indicator
	private static final int VALID = 1;
	// Invalid Indicator
	private static final int INVALID = -1;
	// Model of the application
	private static Model model;
	// Logger of Parser class
	private static Logger log = Logger.getLogger("ParserStorage");
	
	//@author A0105523U
	/**
	 * This function is used to determine the command type of the command input
	 * from the user
	 * 
	 * @param userCommand
	 *            - the command input read from the user
	 * @return the corresponding command type
	 */
	public static Common.COMMAND_TYPES determineCommandType(String userCommand) {
		String commandTypeString = Common.getFirstWord(userCommand);

		if (commandTypeString == null)
			throw new IllegalArgumentException("Command type string cannot be null!");

		if (isAddCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.ADD;
		} else if (isEditCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.EDIT;
		} else if (isRemoveCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.REMOVE;
		} else if(isRecoverCommand(commandTypeString)){ 
			return Common.COMMAND_TYPES.RECOVER;
		}	else if (isUndoCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.UNDO;
		} else if (isRedoCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.REDO;
		} else if (isSearchCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.SEARCH;
		} else if (isTodayCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.TODAY;
		} else if (isShowAllCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.SHOW_ALL;
		} else if (isClearAllCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.CLEAR_ALL;
		} else if (isCompleteCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.COMPLETE;
		} else if (isIncompleteCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.INCOMPLETE;
		} else if (isMarkCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.MARK;
		} else if (isUnmarkCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.UNMARK;
		} else if (isSettingsCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.SETTINGS;
		} else if (isHelpCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.HELP;
		} else if (isSyncCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.SYNC;
		} else if (isExitCommand(commandTypeString)) {
			return Common.COMMAND_TYPES.EXIT;
		} else {
			return Common.COMMAND_TYPES.INVALID;
		}
	}

	/**
	 * This method is used to check whether a command is empty or not
	 */
	public static boolean checkEmptyCommand(String userCommand) {
		return userCommand.trim().equals("");
	}
	
	//@author A0098077N
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
			Common.COMMAND_TYPES commandType, Model model, int tabIndex) {
		String content = Common.removeFirstWord(userCommand);
		content = Common.removeUnneededSpaces(content);
		
		if (isAddCommandType(commandType)) {
			return parseCommandWithInfo(content, Common.COMMAND_TYPES.ADD);
		} else if (isSearchCommandType(commandType)) {
			return parseCommandWithInfo(content, Common.COMMAND_TYPES.SEARCH);
		} else if (isEditCommandType(commandType)) {
			return parseEditCommand(content);
		} else if (isIndexCommandType(commandType)) {
			return parseIndexCommand(content, tabIndex, model);
		} else {
			return null;
		}
	}
	
	// Check if the command type is EDIT type
	private static boolean isEditCommandType(Common.COMMAND_TYPES commandType) {
		return commandType == Common.COMMAND_TYPES.EDIT;
	}
	
	// Check if the command type is SEARCH type
	private static boolean isSearchCommandType(Common.COMMAND_TYPES commandType) {
		return commandType == Common.COMMAND_TYPES.SEARCH;
	}
	
	// Check if the command type is ADD type
	private static boolean isAddCommandType(Common.COMMAND_TYPES commandType) {
		return commandType == Common.COMMAND_TYPES.ADD;
	}
	
	// Check if the command type is INDEX type
	private static boolean isIndexCommandType(Common.COMMAND_TYPES commandType) {
		return commandType == Common.COMMAND_TYPES.COMPLETE
				|| commandType == Common.COMMAND_TYPES.INCOMPLETE
				|| commandType == Common.COMMAND_TYPES.MARK
				|| commandType == Common.COMMAND_TYPES.UNMARK
				|| commandType == Common.COMMAND_TYPES.RECOVER
				|| commandType == Common.COMMAND_TYPES.REMOVE;
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
		String[] splittedUserCommand = Common.splitBySpace(content);
		int modifiedIndex = -1;
		modifiedIndex = checkValidIndexForEditing(content, modifiedIndex);
		checkIfThereExistsTaskInfo(splittedUserCommand);

		String[] parsedCommand = mergeInfoWithIndex(content, modifiedIndex);
		return parsedCommand;
	}
	
	/**
	 * This method is used to merge the index info in EDIT command with the
	 * remaining infos
	 * 
	 * @param content
	 *            conttent of the command
	 * @param modifiedIndex
	 *            the index to be edited
	 * @return the array after merging
	 */
	private static String[] mergeInfoWithIndex(String content, int modifiedIndex) {
		String[] temp = parseCommandWithInfo(Common.removeFirstWord(content),
				Common.COMMAND_TYPES.EDIT);
		String[] parsedCommand = new String[7];
		parsedCommand[0] = String.valueOf(modifiedIndex);
		for (int i = 1; i < parsedCommand.length; i++) {
			parsedCommand[i] = temp[i - 1];
		}
		return parsedCommand;
	}
	
	// Check if the command contains info for task info
	private static void checkIfThereExistsTaskInfo(String[] splittedUserCommand) {
		if (splittedUserCommand.length < 2)
			throw new IllegalArgumentException(Common.NO_EDITING_INFO);
	}
	
	// Check if the first info in the EDIT command is a valid index
	private static int checkValidIndexForEditing(String content,
			int modifiedIndex) {
		try {
			modifiedIndex = Integer.parseInt(Common.getFirstWord(content));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(Common.INVALID_INDEX);
		}
		return modifiedIndex;
	}
	
	//@author A0105523U
	/*******************************************************************************************************************/
	/**
	 * This method is used to parse from content of an ADD or SEARCH command to
	 * necessary infos
	 * 
	 * @param content
	 *            command string after removing the command type
	 * @return an array of strings where the first field contains workInfo, Common.NULL
	 *         if it is empty; the second field contains tag, Common.NULL if it is
	 *         empty; the third field contains startDateString, Common.NULL if it is
	 *         empty; the fourth field contains endDateString, Common.NULL if it is
	 *         empty; the fifth field contains isImpt (Common.TRUE or Common.FALSE).
	 */
	private static String[] parseCommandWithInfo(String content,
			Common.COMMAND_TYPES commandType) {
		String commandString = content.trim();
		String workInfo = Common.NULL;
		String startDateString = Common.NULL;
		String endDateString = Common.NULL;
		String tag = Common.NULL;
		String isImpt = Common.FALSE;
		String repeatingType = Common.NULL;
		
		// Process recurring info
		String[] result = getRepeatingType(commandString);
		commandString = result[0];
		repeatingType = result[1];
		
		// Process tag info
		if (hasMultipleTags(commandString)) {// multiple hash tags
			throw new IllegalArgumentException("Invalid Command: multiple hash tags(#).");
		} else {
			tag = getTagName(commandString);
			commandString = parseTag(commandString, tag);
		}
		
		// Process importance info
		if (hasMultipleImptMarks(commandString)) {//multiple important marks
			throw new IllegalArgumentException("Invalid Command: multiple important marks(*).");
		} else {
			isImpt = isImptTask(commandString);
			commandString = parseImportance(commandString, isImpt);
		}
		
		// Process start date
		result = checkDate(commandString, Common.startDateKeys, START_KEY);
		commandString = result[0];
		startDateString = result[1];
		
		// Process end date
		result = checkDate(commandString, Common.endDateKeys, END_KEY);
		commandString = result[0];
		endDateString = result[1];
		
		// Process work info
		workInfo = parseWorkInfo(commandType, commandString);

		String[] parsedCommand = assignParsedInfos(workInfo, startDateString,
				endDateString, tag, isImpt, repeatingType);

		return parsedCommand;
	}
	
	/**
	 * This function is used to assign the parsed info to the corresponding
	 * array elements to return to its caller
	 * 
	 * @param workInfo
	 *            the info of the task
	 * @param startDateString
	 *            the String format of start date
	 * @param endDateString
	 *            the String format of end date
	 * @param tag
	 *            the tag
	 * @param isImpt
	 *            the indicator whether this task is an important task or not
	 * @param repeatingType
	 *            the type of repetition including also the number of
	 *            occurrences
	 * @return the required array
	 */
	private static String[] assignParsedInfos(String workInfo,
			String startDateString, String endDateString, String tag,
			String isImpt, String repeatingType) {
		String[] parsedCommand = new String[] { Common.NULL, Common.NULL, Common.NULL, Common.NULL, Common.FALSE,
				Common.NULL };
		
		parsedCommand[Common.INDEX_WORK_INFO] = workInfo;
		parsedCommand[Common.INDEX_START_DATE] = startDateString;
		parsedCommand[Common.INDEX_END_DATE] = endDateString;
		parsedCommand[Common.INDEX_TAG] = tag;
		parsedCommand[Common.INDEX_IS_IMPT] = isImpt;
		parsedCommand[Common.INDEX_REPEATING] = repeatingType;
		
		return parsedCommand;
	}
	
	/**
	 * This function is used to parse the work info
	 * 
	 * @param commandType
	 *            the type of the command
	 * @param commandString
	 *            the string content of the comand
	 * @return the corresponding work info
	 */
	private static String parseWorkInfo(Common.COMMAND_TYPES commandType,
			String commandString) {
		String workInfo;
		if (commandString.trim().equals("")) {
			workInfo = checkForAddCommand(commandType);
		} else{
			workInfo = commandString.trim();
		}
		return workInfo;
	}
	
	/**
	 * This function is used to check if this is an ADD command or not when the task info is empty
	 * @param commandType the type of the command
	 * @return NULL if this is not an ADD command. Throw error vice versa
	 */
	private static String checkForAddCommand(Common.COMMAND_TYPES commandType) {
		if (isAddCommandType(commandType)){
			throw new IllegalArgumentException("Invalid command: work information cannot be empty.");
		}else{
			return Common.NULL;
		}
	}	

	/**
	 * This function is used to parse the important indicator for the task
	 * 
	 * @param commandString
	 *            the string content of the command
	 * @param isImpt
	 *            the indicator whether it is important or not
	 * @return the command string after removing the important indicator
	 */
	private static String parseImportance(String commandString, String isImpt) {
		commandString = (isImpt.equals(Common.TRUE)) ? removeImptMark(commandString)
				: commandString;
		return commandString;
	}

	/**
	 * This function is used to parse the tag for the task
	 * 
	 * @param commandString
	 *            the strig content of the command
	 * @param tag
	 *            the tag in the command
	 * @return the command string after removing the tag
	 */
	private static String parseTag(String commandString, String tag) {
		commandString = (!tag.equals(Common.NULL)) ? removeTag(commandString)
				: commandString;
		return commandString;
	}
	
	//@author A0105667B
	/**
	 * This function is used to parse all command working with indices such as
	 * MARK, COMPLETE or REMOVE
	 * 
	 * @param content
	 *            content of the command
	 * @param tabIndex
	 *            the current tab
	 * @param model
	 *            model of the application
	 * @return the array of indices
	 */
	private static String[] parseIndexCommand(String content, int tabIndex, Model model) {
		try {
			return parseCommandWithIndex(content);
		} catch (NumberFormatException e) {
			return convertInfosIntoIndices(tabIndex, model);
		} 
	}
	
	/**
	 * This function is used when the user type infos for these commands working with indices.
	 * This function will convert the infos typed by the user into indices by searching for these corresponding infos.
	 * @param tabIndex the current tab
	 * @param model of the application
	 * @return the array of indices 
	 */
	private static String[] convertInfosIntoIndices(int tabIndex, Model model) {
		ObservableList<Task> modifiedList;
		if (tabIndex == Common.PENDING_TAB) {
			modifiedList = model.getSearchPendingList();
		} else if (tabIndex == Common.COMPLETE_TAB) {
			modifiedList = model.getSearchCompleteList();
		} else {
			modifiedList = model.getSearchTrashList();
		}
		String indexRange = "1" + Common.HYPHEN + modifiedList.size();
		
		return parseCommandWithIndex(indexRange);
	}
	
	//@author A0098077N
	/**
	 * This method is used to parse content of commands filled with indexes to
	 * necessary infos
	 * 
	 * @param content
	 *            command string after removing the command type
	 * @return array of indexes in form of strings.
	 */
	private static String[] parseCommandWithIndex(String content) {
		String[] splittedUserCommand = Common.splitBySpace(content);
		Vector<String> indexList = new Vector<String>();
		checkIfThereExistsIndices(splittedUserCommand);
		parseIndex(splittedUserCommand, indexList);
		splittedUserCommand = indexList.toArray(new String[0]);
		return splittedUserCommand;
	}
	
	
	/**
	 * This function is the main function to parse the indices into the array
	 * 
	 * @param splittedUserCommand
	 *            the user command after splitted by spcaes
	 * @param indexList
	 *            the array vector of indices
	 */
	private static void parseIndex(String[] splittedUserCommand,
			Vector<String> indexList) {
			for (String s : splittedUserCommand) {
				boolean isRangeIndex = s.contains(Common.HYPHEN);
				if (isRangeIndex) {
					parseRange(indexList, s);
				} else {
					indexList.add(String.valueOf(Integer.parseInt(s)));
				}
			}
	}

	/**
	 * Check if there exists indices in the command or not
	 * 
	 * @param splittedUserCommand
	 *            the command after splitted by spaces
	 */
	private static void checkIfThereExistsIndices(String[] splittedUserCommand) {
		if (splittedUserCommand.length < 1)
			throw new IllegalArgumentException("No indexes");
	}
	
	/**
	 * This function is used to parse a range of indices
	 * @param indexList the vector list of indices
	 * @param range the range indicated from the command
	 */
	private static void parseRange(Vector<String> indexList, String range) {
		String[] limits = range.split(Common.HYPHEN);
		checkInvalidRangeFormat(limits);
		
		int startPoint, endPoint;
		startPoint = Integer.parseInt(limits[0]);
		endPoint = Integer.parseInt(limits[1]);
		checkInvalidRange(startPoint, endPoint);
		
		for (int i = startPoint; i <= endPoint; i++){
			indexList.add(String.valueOf(i));
		}
	}
	
	// Check if the content of the rang is valid or not
	private static void checkInvalidRange(int startPoint, int endPoint) {
		if (startPoint > endPoint){
			throw new IllegalArgumentException(Common.INVALID_RANGE_END_SMALLER);
		}
	}
	
	// Check if the format of the range is valid or not
	private static void checkInvalidRangeFormat(String[] limits) {
		if (limits.length > 2) {
			throw new IllegalArgumentException(Common.INVALID_RANGE);
		}
	}
	
	//@author A0105523U
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
		String proccessedString = commandString + "";
		boolean hasDate = false;
		String dateString = Common.NULL;
		for (int i = 0; i < keys.length; i++) {
			// find first occurrence of a <key>
			int keyIndex = proccessedString.toLowerCase().indexOf(keys[i]);
			int keyLength = keys[i].length();
			boolean isValidIndex = keyIndex == 0 || (keyIndex > 0 && proccessedString.charAt(keyIndex-1) == ' ');
			while (isValidIndex) {
				// get string before the date key
				String stringBeforeKey = getStringBeforeIndex(proccessedString, keyIndex);
				// get string after the date key
				String stringAfterKey = getStringAfterKey(keys, proccessedString, i, keyIndex, keyLength);

				int dateLastIndex = isValidDate(stringAfterKey);
				if (dateLastIndex == INVALID) {
					keyIndex = proccessedString.indexOf(keys[i], keyIndex + keyLength);
				} else {
					checkMultipleDates(hasDate);
					dateString = getStringBeforeIndex(stringAfterKey, dateLastIndex);
					hasDate = true;
					proccessedString = stringBeforeKey.trim() + " " + stringAfterKey.substring(dateLastIndex).trim();
					keyIndex = proccessedString.indexOf(keys[i]);
				}
				isValidIndex = keyIndex == 0 || (keyIndex > 0 && proccessedString.charAt(keyIndex-1) == ' ');
			}
		}

		if (hasDate) {
			return new String[] { proccessedString, dateString };
		} else{
			return new String[] { commandString, dateString };
		}
	}
	
	// Check if this command has multiple dates in the same type
	private static void checkMultipleDates(boolean hasDate) {
		if (hasDate) {
			throw new IllegalArgumentException("Invalid Command: Multiple Dates");
		}
	}
	
	// Get the String after a key word
	private static String getStringAfterKey(String[] keys, String temp, int i,
			int keyIndex, int keyLength) {
		String stringAfterKey;
		if (isSpecialEndKeyword(keys, i)) {
			stringAfterKey = keys[i] + " " + temp.substring(keyIndex + keyLength).trim();
		} else {
			stringAfterKey = temp.substring(keyIndex + keyLength).trim();
		}
		return stringAfterKey;
	}
	
	// Check if this is a special key word in the end date kys
	private static boolean isSpecialEndKeyword(String[] keys, int i) {
		return keys[i].equals("today") || keys[i].equals("tonight") || keys[i].equals("tomorrow") || keys[i].equals("next");
	}
	
	// Get the string before a certain index
	private static String getStringBeforeIndex(String temp, int keyIndex) {
		return temp.substring(0, keyIndex).trim();
	}

	//@author A0105667B
	/**
	 * This method is used to parse the command when any key event occurs and
	 * highlight the command to indicate the understanding of the command by the
	 * program to user to assist user to type more exact command in real-time
	 * before the user presses Enter
	 * 
	 * @param command
	 *            the command input
	 * @return the array list of InfoWithIndex object to pass on the View class
	 *         to process
	 */
	static ArrayList<InfoWithIndex> parseForView(String command) {
		ArrayList<InfoWithIndex> infoList = new ArrayList<InfoWithIndex>();
		Common.COMMAND_TYPES commandType;
		String commandTypeStr;
		commandType = determineCommandType(command);
		if (commandType == Common.COMMAND_TYPES.INVALID) {
			infoList.add(new InfoWithIndex(command, 0, Common.INDEX_REDUNDANT_INFO));
			return infoList;
		}	
		commandTypeStr = appendCommandTypeInfo(command, infoList);
		try {
			String[] result = parseCommand(command, commandType, model, 0);
			infoList = appendIndexInfo(result, infoList, command, commandType);
			infoList = decomposeCommand(result, infoList, command, commandType, commandTypeStr);
			return infoList;
		} catch (Exception e) {
			infoList = handleRedundantInfo(e, infoList,command, commandTypeStr);
			return infoList;
		}
	}
	
	/********************************* Some methods used for parseForView ***********************************************/
	
	/**
	 * This function appends the info of command type to the list of
	 * InfoWithIndex object
	 * 
	 * @param command
	 *            the command from the user
	 * @param infoList
	 *            the list of infos
	 * @return the string of command type
	 */
	private static String appendCommandTypeInfo(String command,
			ArrayList<InfoWithIndex> infoList) {
		String commandTypeStr;
		int indexOfCommandType = command.indexOf(Common.getFirstWord(command));
		commandTypeStr = completeWithSpace(Common.getFirstWord(command), command, indexOfCommandType);
		while(indexOfCommandType != 0){
			commandTypeStr = (command.charAt(indexOfCommandType) == '\t' ? "\t" : " ") + commandTypeStr;
			indexOfCommandType--;
		}
		infoList.add(new InfoWithIndex(commandTypeStr, 0, Common.INDEX_COMMAND_TYPE));
		return commandTypeStr;
	}
	
	/**
	 * This function handles the redundant info when the command is invalid
	 * 
	 * @param exception
	 *            exception from the application
	 * @param infoList
	 *            the array list of infos
	 * @param command
	 *            the command from the user
	 * @param commandTypeStr
	 *            the string of command type in the command
	 * @return the modified list of infos
	 */
	private static ArrayList<InfoWithIndex> handleRedundantInfo(Exception exception,
			ArrayList<InfoWithIndex> infoList, String command,
			String commandTypeStr) {
		infoList.clear();
		infoList.add(new InfoWithIndex(commandTypeStr, 0, Common.INDEX_COMMAND_TYPE));
		String remainingInfo = Common.removeFirstWord(command);
		if (exception.getMessage() != null && exception.getMessage().equals(Common.NO_EDITING_INFO)) {
			infoList.add(new InfoWithIndex(remainingInfo, commandTypeStr.length(), Common.INDEX_INDEX_INFO));
		} else {
			infoList.add(new InfoWithIndex(remainingInfo, commandTypeStr.length(), Common.INDEX_REDUNDANT_INFO));
		}
		return infoList;
	}
	
	/**
	 * This function is the main function to parse all important infos in the
	 * command into the list of infos for viewing
	 * 
	 * @param parsedCommand
	 *            the array of infos after being parsed
	 * @param infoList
	 *            the list of infos to be passed to View class
	 * @param command
	 *            the command from the user
	 * @param commandType
	 *            the type of command
	 * @param commandTypeStr
	 *            the command type in String format
	 * @return the list of InfoWithIndex object
	 */
	private static ArrayList<InfoWithIndex> decomposeCommand(String[] parsedCommand,
			ArrayList<InfoWithIndex> infoList, String command,
			Common.COMMAND_TYPES commandType, String commandTypeStr) {
		// First consider command with info
		if (isAddCommandType(commandType) || isSearchCommandType(commandType) || isEditCommandType(commandType)) {
			infoList = decomposeInfoCommand(parsedCommand, infoList, command, commandTypeStr);
			infoList = addRedundantInfo(infoList, command);
		} else {
			int beginIndex = commandTypeStr.length();
			if (isIndexCommandType(commandType)) {
				infoList.add(new InfoWithIndex(command.substring(beginIndex), beginIndex, Common.INDEX_INDEX_INFO));
			} else if(commandType == Common.COMMAND_TYPES.INVALID){
				infoList.add(new InfoWithIndex(command.substring(beginIndex), beginIndex, Common.INDEX_REDUNDANT_INFO));
			} else {
				log.log(Level.WARNING, "Inexisting command type");
			}
		}
		return infoList;
	}
	
	/**
	 * This function is used to parse all the infos in ADD, SEARCH and EDIT
	 * command for viewing
	 * 
	 * @param parsedCommand
	 *            the command after being parsed
	 * @param infoList
	 *            the array list of infos for viewing
	 * @param command
	 *            the command from the user
	 * @param commandTypeStr
	 *            command type in String format
	 * @return the modifed list of infos for viewing
	 */
	private static ArrayList<InfoWithIndex> decomposeInfoCommand(
			String[] parsedCommand, ArrayList<InfoWithIndex> infoList, String command,
			String commandTypeStr) {
		for (int infoIndex = 0; infoIndex < parsedCommand.length; infoIndex++) {
			String info = parsedCommand[infoIndex];
			info = appendDateKeyForStartDate(command, infoIndex, info);
			info = appendDateKeyForEndDate(command, infoIndex, info);
			appendAsteriskForImportantTask(infoList, command, infoIndex, info);
			appendInfoToList(infoList, command, commandTypeStr, infoIndex, info);
		}
		return infoList;
	}
	
	private static void appendInfoToList(ArrayList<InfoWithIndex> infoList, String command, String commandTypeStr, int infoIndex, String info) {
		if (command.contains(info)) {
			int startIndex;
			if (infoIndex == Common.INDEX_WORK_INFO) {
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
	
	// Add the important info in the list of infos for viewing
	private static void appendAsteriskForImportantTask(
			ArrayList<InfoWithIndex> infoList, String command, int infoIndex,
			String info) {
		if (infoIndex == Common.INDEX_IS_IMPT && info == Common.TRUE) {
			String markStr = completeWithSpace(Common.IMPT_MARK, command,
					command.indexOf(Common.IMPT_MARK));
			InfoWithIndex imptInfo = new InfoWithIndex(markStr,
					command.indexOf(Common.IMPT_MARK), Common.INDEX_IS_IMPT);
			infoList.add(imptInfo);
		}
	}
	
	// Append the key word for the end date
	private static String appendDateKeyForEndDate(String command,
			int infoIndex, String info) {
		if (infoIndex == Common.INDEX_END_DATE && info != Common.NULL){
			info = appendWithDateKey(info, command, END_DATE);
		}
		return info;
	}
	
	// Append the key word for the start date
	private static String appendDateKeyForStartDate(String command,
			int infoIndex, String info) {
		if (infoIndex == Common.INDEX_START_DATE && info != Common.NULL){
			info = appendWithDateKey(info, command, START_DATE);
		}
		return info;
	}
	
	/**
	 * This funtionc add the index info into the list of infos for viewing for
	 * EDIT command
	 * 
	 * @param parsedCommand
	 *            the command after being parsed
	 * @param infoList
	 *            the list of InfoWithIndex to be passed to View class
	 * @param command
	 *            the command from the user
	 * @param commandType
	 *            the type of command
	 * @return the list of infos for viewing
	 */
	private static ArrayList<InfoWithIndex> appendIndexInfo(String[] parsedCommand,
			ArrayList<InfoWithIndex> infoList, String command,
			Common.COMMAND_TYPES commandType) {
		if (isEditCommandType(commandType)) {
			String index = parsedCommand[0];
			String indexWithSpace = completeWithSpace(index, command,
					command.indexOf(index));
			InfoWithIndex indexInfo = new InfoWithIndex(indexWithSpace,
					command.indexOf(index), Common.INDEX_INDEX_INFO);
			infoList.add(indexInfo);
			for (int i = 0; i < parsedCommand.length - 1; i++)
				parsedCommand[i] = parsedCommand[i + 1];
			parsedCommand[parsedCommand.length - 1] = Common.NULL;
		}
		return infoList;
	}
	

	/**
	 * Append the date with its front preposition like start from or so on
	 * 
	 * @param date
	 * @param command
	 * @param dateTypeIndicator
	 *            is the date type start date or end date
	 * @return the date with preposition in front
	 */
	private static String appendWithDateKey(String date, String command,
			int dateTypeIndicator) {
		int startIndex = 0;
		String dateWithKeyWord = date;
		while (startIndex != -1) {
			startIndex = command.indexOf(date, startIndex + 1);
			int secondSpaceIndex = getSecondSpaceIndex(command, startIndex);
			int firstSpaceIndex = getFirstSpaceIndex(command, secondSpaceIndex);
			dateWithKeyWord = addKeyWord(date, command, dateTypeIndicator,
					startIndex, dateWithKeyWord, secondSpaceIndex,
					firstSpaceIndex);
			if (!dateWithKeyWord.equals(date) || dateWithKeyWord.startsWith("tomorrow") || dateWithKeyWord.startsWith("today") || dateWithKeyWord.startsWith("tonight") || dateWithKeyWord.startsWith("next")) {
				return dateWithKeyWord;
			}
		}
		return dateWithKeyWord;
	}
	
	// Add key word for the date
	private static String addKeyWord(String date, String command,
			int dateTypeIndicator, int startIndex, String dateWithPreposition,
			int secondSpaceIndex, int firstSpaceIndex) {
		boolean containsKeyWordForStartDate = Common.doesArrayContain(
				dateTypeIndicator == START_DATE ? Common.startDateKeys
						: Common.endDateKeys, command.substring(
						firstSpaceIndex + 1, startIndex));
		boolean containsKeyWordForEndDate = Common.doesArrayContain(
				dateTypeIndicator == START_DATE ? Common.startDateKeys
						: Common.endDateKeys, command.substring(
						secondSpaceIndex + 1, startIndex));
		
		if (containsKeyWordForStartDate) {
			dateWithPreposition = command.substring(firstSpaceIndex + 1,
					startIndex) + date;
		} else if (containsKeyWordForEndDate) {
			dateWithPreposition = command.substring(secondSpaceIndex + 1,
					startIndex) + date;
		}
		return dateWithPreposition;
	}
	
	// Get the index of the first space before the date in the command
	private static int getFirstSpaceIndex(String command, int secondSpaceIndex) {
		int firstSpaceIndex = getCharIndex(command, secondSpaceIndex);
		while (firstSpaceIndex >= 0
				&& command.charAt(firstSpaceIndex) != ' ') {
			firstSpaceIndex--;
		}
		return firstSpaceIndex;
	}
	
	// Get the index of the second space before the date in the command
	private static int getSecondSpaceIndex(String command, int startIndex) {
		int secondSpaceIndex = getCharIndex(command, startIndex-1);

		while (secondSpaceIndex >= 0
				&& command.charAt(secondSpaceIndex) != ' ') {
			secondSpaceIndex--;
		}
		return secondSpaceIndex;
	}
	
	private static int getCharIndex(String command, int index){
		while(index >= 0 && command.charAt(index) == ' ')
			index--;
		
		return index;
	}

	/**
	 * add the remaining info without highlighted by parser to infoList with
	 * InfoType: INDEX_TYPING_INFO
	 * 
	 * @param infoList
	 * @param command
	 * @return complete infoList
	 */
	private static ArrayList<InfoWithIndex> addRedundantInfo(
			ArrayList<InfoWithIndex> infoList, String command) {
		Collections.sort(infoList);
		int keyInfoCount = infoList.size();
		for (int i = 0; i < keyInfoCount; i++) {
			int startIndex = infoList.get(i).getEndIndex();
			int endIndex;
			if (i != (keyInfoCount - 1)) {
				endIndex = infoList.get(i + 1).getStartIndex();
			} else {
				endIndex = command.length();
			}
			if (startIndex < endIndex) {
				infoList.add(new InfoWithIndex(command.substring(startIndex, endIndex), startIndex, Common.INDEX_WORK_INFO));
			}
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
			} else { // no more spaces or tabs
				break;
			}
		}
		return info;
	}



	/************************** assisting methods for parseCommandWithInfo ****************************************/

	/**
	 * retrieve the repeating type from the command string
	 * 
	 * @param commandString
	 * @return a string array including command string and repeating tag
	 */
	private static String[] getRepeatingType(String commandString) {
		String repeatingKey = Common.NULL;
		for (int i = 0; i < repeatingKeys.length; i++) {
			String regex = "(\\s+)?"+repeatingKeys[i]+"(\\s+\\d+\\s+times?)?";
		    Pattern pattern = Pattern.compile(regex);
		    Matcher matcher = pattern.matcher(commandString);
			repeatingKey = processRecurringInfo(repeatingKey, matcher);		
		}
		commandString = extractRecurringInfo(commandString, repeatingKey);
		return new String[] { commandString, repeatingKey.trim() };
	}
	
	// Remove the recurring info from the command
	private static String extractRecurringInfo(String commandString,
			String repeatingKey) {
		if(!repeatingKey.equals(Common.NULL)){
			commandString = commandString.replace(repeatingKey, "");
		}
		return commandString;
	}
	
	// Get the recurring info
	private static String processRecurringInfo(String repeatingKey,
			Matcher matcher) {
		while (matcher.find()) {
			if (repeatingKey.equals(Common.NULL))
				repeatingKey = matcher.group();
			else {
				throw new IllegalArgumentException(
						"Invalid Command: More than 1 repetitive signals");
			}
		}
		return repeatingKey;
	}
	
	//@author A0105523U
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
		String[] dateStringArray = Common.splitBySpace(dateString);
		int length = getMaximumLengthForProcessing(dateStringArray);
		dateString = cropDateString(dateStringArray, length);
		while (!dateString.isEmpty()) {
			int result = dateTester.convert(dateString);
			if (result == VALID) {
				return dateString.length();
			} else if (result == CustomDate.OUT_OF_BOUNDS) {
				throw new IllegalArgumentException("The time is out of bounds. Please recheck!");
			}
			dateString = Common.removeLastWord(dateString);
		}
		return INVALID;
	}
	
	// Crop the possible date string to the maximum length for date string
	private static String cropDateString(String[] dateStringArray, int length) {
		String dateString;
		dateString = "";
		// construct the date tester string.
		for (int i = 0; i < length; i++) {
			dateString = (dateString + " " + dateStringArray[i]).trim();
		}
		return dateString;
	}
	
	// Determine the maximum length for the investigated date string
	private static int getMaximumLengthForProcessing(String[] dateStringArray) {
		int length;
		if (dateStringArray.length >= MAX_DATE_LENGTH) {
			length = MAX_DATE_LENGTH;
		} else {
			length = dateStringArray.length;
		}
		return length;
	}

	/**
	 * Checks whether the specified command string has more than one hash tags.
	 * 
	 * @param commandString
	 *            - the string which is being checked.
	 * @return true if it contains more than one hash tag; false otherwise.
	 */
	private static boolean hasMultipleTags(String commandString) {
		String[] words = Common.splitBySpace(commandString);
		boolean hasTag = false;

		for (int i = 0; i < words.length; i++) {
			if (words[i].startsWith(Common.HASH_TAG)) {
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
		String[] words = Common.splitBySpace(commandString);
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
	 *         contains a hash tag; the string Common.NULL otherwise.
	 */
	private static String getTagName(String commandString) {
		String[] words = Common.splitBySpace(commandString);
		for (int i = 0; i < words.length; i++) {
			if (words[i].startsWith(Common.HASH_TAG)) {
				return words[i];
			}
		}
		return Common.NULL;
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
		String[] words = Common.splitBySpace(commandString);
		for (int i = 0; i < words.length; i++) {
			if (words[i].startsWith(Common.HASH_TAG)) {
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
		String[] words = Common.splitBySpace(commandString);
		boolean isImpt = false;

		for (int i = 0; i < words.length; i++) {
			if (words[i].equals(Common.IMPT_MARK)) {
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
	 * @return the string Common.TRUE if it is a important task; otherwise, returns the
	 *         string Common.FALSE.
	 */
	private static String isImptTask(String commandString) {
		String[] words = Common.splitBySpace(commandString);

		for (int i = 0; i < words.length; i++) {
			if (words[i].equals(Common.IMPT_MARK)) {
				return Common.TRUE;
			}
		}

		return Common.FALSE;
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
		String[] words = Common.splitBySpace(commandString);
		String result = "";
		for (int i = 0; i < words.length; i++) {
			if (!words[i].equals(Common.IMPT_MARK)) {
				result = result + " " + words[i];
			}
		}
		return result.trim();
	}

	
	/******************************** Determine COMMAND_TYPES Section ***************************************************************/

	private static boolean isAddCommand(String commandTypeString) {
		return commandTypeString.equalsIgnoreCase("add")
				||commandTypeString.equalsIgnoreCase("insert");
	}

	private static boolean isEditCommand(String commandTypeString) {
		boolean isEdit = commandTypeString.equalsIgnoreCase("edit")
				|| commandTypeString.equalsIgnoreCase("set")
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
	
	private static boolean isRecoverCommand(String commandTypeString){
		boolean isRecover = commandTypeString.equalsIgnoreCase("recover") || commandTypeString.equalsIgnoreCase("rec");
		return isRecover;
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
				|| commandTypeString.equalsIgnoreCase("display")
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
		boolean isMark = commandTypeString.equalsIgnoreCase("mark") 
				||commandTypeString.equalsIgnoreCase("highlight") ;
		return isMark;
	}

	private static boolean isUnmarkCommand(String commandTypeString) {
		boolean isUnMark = commandTypeString.equalsIgnoreCase("unmark") 
				||commandTypeString.equalsIgnoreCase("unhighlight") ;
		return isUnMark;
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
		return commandTypeString.equalsIgnoreCase("exit") || commandTypeString.equalsIgnoreCase("end");
	}
}

//@author A0105667B
/**
 * Class infoWithIndex is assisting parseForView to install the information of
 * parsed command and reflects on commandLine and feedback
 * 
 * 
 */
class InfoWithIndex implements Comparable<InfoWithIndex> {
	// Info content
	String info;
	// Start index of the info in the command
	int startIndex;
	// End index of the info in the command
	int endIndex;
	// Type of info
	int infoType;

	/**
	 * Constructor of this class
	 * 
	 * @param info
	 *            the info
	 * @param index
	 *            the index of the info found in the command
	 * @param infoType
	 *            the type of info
	 */
	public InfoWithIndex(String info, int index, int infoType) {
		this.info = info;
		startIndex = index;
		endIndex = startIndex + info.length();
		this.infoType = infoType;
	}
	
	// Get info content
	public String getInfo() {
		return info;
	}
	
	// Get start index in the command
	public int getStartIndex() {
		return startIndex;
	}
	
	// Get end index in the command
	public int getEndIndex() {
		return endIndex;
	}
	
	// Get the type of info
	public int getInfoType() {
		return infoType;
	}

	public int compareTo(InfoWithIndex other) {
		if (startIndex > other.startIndex) {
			return 1;
		} else if (startIndex == other.startIndex) {
			return 0;
		} else {
			return -1;
		}
	}
}
