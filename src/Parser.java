import java.util.Scanner;

public class Parser {
	static enum COMMAND_TYPES {
		ADD, REMOVE, SEARCH, EDIT, COMPLETE, INCOMPLETE, UNDO, REDO, CLEAR_ALL, TODAY, SHOW_ALL, SYNC, SETTINGS, HELP, EXIT, INVALID, MARK, UNMARK
	}

	/*start date keys*/
	private static String[] startDateKeys = {
		"start from", "start at", "start on", "begin from", "begin at", "begin on", "from", "frm"
	};
	
	/*end date keys*/
	private static String[] endDateKeys = {
		"end on", "end at", "end by", "end before", "to", "till", "until", "by", "due"
	};

	public static final int INDEX_IS_MATCH = 0;
	public static final int INDEX_WORK_INFO = 0;
	public static final int INDEX_STRING_BEFORE = 1;
	public static final int INDEX_TAG = 1;
	public static final int INDEX_START_DATE = 2;
	public static final int INDEX_END_DATE = 3;
	public static final int INDEX_STRING_AFTER = 4;
	public static final int INDEX_IS_IMPT = 4;
	
	/*maximum length of a date. Example: next Monday 4pm*/
	public static final int MAX_DATE_LENGTH = 3;
	
	public static final String IMPT_MARK = "*";
	public static final String HASH_TAG = "#";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NULL = "null";
	public static final String START_KEY = "start key";
	public static final String END_KEY = "end key";

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
/*******************************************************************************************************************/
	/**
	 * Parse add command string into an array of strings.
	 * @param content - a add command string with "add" key word being removed
	 * @return an array of strings where  
	 *         the first field contains workInfo, NULL if it is empty;
	 *         the second field contains startDateString, NULL if it is empty;
	 *         the third field contains endDateString, NULL if it is empty;
	 *         the fourth field contains tag, NULL if it is empty;
	 *         the fifth field contains isImpt (TRUE or FALSE).
	 */
	public static String[] parseAddCommand(String content){
		String commandString = content.trim();
		
		String[] result = null;
		
		String workInfo = NULL;
		String startDateString = NULL;
		String endDateString = NULL;
		String tag = NULL;
		String isImpt = FALSE;
		
		if(hasMultipleTags(commandString)){//if contains multiple hash tags
			throw new IllegalArgumentException("Invalid Command: multiple hash tags(#).");
		} else {
			tag = getTagName(commandString);
			commandString = removeTag(commandString);
		}
		
		if(hasMultipleImptMarks(commandString)){//if contains multiple important marks
			throw new IllegalArgumentException("Invalid Command: multiple important marks(*).");
		} else {
			isImpt = isImptTask(commandString);
			commandString = removeImptMark(commandString);
		}
		
		//split command string into three parts: string before dates, dates, and string after dates.
		result = splitByDate(commandString);
		String stringBeforeDate = result[INDEX_STRING_BEFORE];
		String stringAfterDate = result[INDEX_STRING_AFTER];
		
		startDateString = result[INDEX_START_DATE];
		endDateString = result[INDEX_END_DATE];
		
		if(result[INDEX_START_DATE] != NULL || result[INDEX_END_DATE] != NULL){//contains at one date
			//check if contains multiple dates
			if(splitByDate(result[4])[0] == TRUE){//contains multiple dates
				throw new IllegalArgumentException("Invalid Command: multiple date inputs.");
			} else {//contains only one date
				if(stringBeforeDate == NULL && stringAfterDate == NULL){//empty work information
					throw new IllegalArgumentException("Invalid command: work information can't be empty.");					
				} else {
					if(stringBeforeDate == NULL){//case 1: date is at the begin
						workInfo = stringAfterDate;
					} else if(stringAfterDate == NULL){//case 2: date is at the end
						workInfo = stringBeforeDate;
					} else {//case 3: date is at the middle
						workInfo = stringBeforeDate + " " + stringAfterDate;
					}
				}
			}
		} else {//contains no date
			workInfo = stringBeforeDate;
		}
		
		String[] parsedCommand = new String[]{FALSE, NULL, NULL, NULL, NULL};
		
		parsedCommand[INDEX_WORK_INFO] = workInfo;
		parsedCommand[INDEX_START_DATE] = startDateString;
		parsedCommand[INDEX_END_DATE] = endDateString;
		parsedCommand[INDEX_TAG] = tag;
		parsedCommand[INDEX_IS_IMPT] = isImpt;
	
		return parsedCommand;
	}
	
	/**
	 * Splits the specified command string into an array of strings based on dates.
	 * @param commandString - a command string
	 * @return an array of string where the first field contains is_match(TRUE or FALSE); 
	 *         the second field contains stringBeforeStartDate, NULL if it is empty;
	 *         the third field contains startDateString, NULL if it is empty;
	 *         the fourth field contains endDateString, NULL if it is empty;
	 *         the fifth field contains stringAfterEndDate, NULL if it is empty.
	 */
	private static String[] splitByDate(String commandString){
		String[] r = new String[] {TRUE, NULL, NULL, NULL, NULL};
		
		//try to match date pattern1: <start date key> <date> <end date key> <date>
		r = matchDatePattern1(commandString.trim());
		if(r[0] == TRUE){//if it matches pattern1
			return r;
		}
		
		//try to match date pattern2: <start date key> <date> 
		r = matchDatePattern2(commandString.trim());
		if(r[0] == TRUE){//if it matches pattern2.
			return r;
		}
		
		//try to match date pattern3: <end date key> <date>
		r = matchDatePattern3(commandString.trim());
		if(r[0] == TRUE){//if it matches pattern3.
			return r;
		}
		
		//no dates found in command string
		r[INDEX_STRING_BEFORE] = commandString;
		
		return r;
	}
	
	/**
	 * Matches a date pattern: <start date key> <date> <end date key> <date> within the specified string.
	 * @param commandString - a command string.
	 * @return an array of string where the first field contains is_match(TRUE or FALSE); 
	 *         the second field contains stringBeforeStartDate, NULL if it is empty;
	 *         the third field contains startDateString, NULL if it is empty;
	 *         the fourth field contains endDateString, NULL if it is empty;
	 *         the fifth field contains stringAfterEndDate, NULL if it is empty.
	 */
	private static String[] matchDatePattern1(String commandString){
		String[] result = {FALSE, NULL, NULL, NULL, NULL};
		
		for(int i = 0; i < startDateKeys.length; i++){
			//find first occurrence of a <start key>
			int startKeyIndex =  indexOf(commandString, startDateKeys[i], 0);
			while(startKeyIndex >= 0){
				int startKeyLength = startDateKeys[i].length();
				String stringBeforeStartKey = commandString.substring(0, startKeyIndex).trim();
				String stringAfterStartKey = commandString.substring(startKeyIndex+startKeyLength).trim();
				
				//find first occurrence of a <end key> after start key
				for(int j = 0; j < endDateKeys.length; j++){
					int endKeyIndex = stringAfterStartKey.indexOf(endDateKeys[j]);
					if(endKeyIndex < 0){//only has a start key
						continue;
					} else {
						int endKeyLength = endDateKeys[j].length();
						CustomDate dateTester = new CustomDate();
						String startDateString = stringAfterStartKey.substring(0, endKeyIndex).trim();
						if(dateTester.convert(startDateString)==Control.INVALID){
							break;
						}
						//get end date string
						String stringAfterEndKey = stringAfterStartKey.substring(endKeyLength+endKeyIndex).trim();
						int endDateLength = isValidDate(stringAfterEndKey);
						if(endDateLength == Control.INVALID){
							break;
						} else {
							String endDateString = stringAfterEndKey.substring(0, endDateLength);
							String remainingString = stringAfterEndKey.substring(endDateLength).trim();
							
							result[INDEX_IS_MATCH] = TRUE;
							if(!stringBeforeStartKey.isEmpty()){
								result[INDEX_STRING_BEFORE] = stringBeforeStartKey;								
							}
							if(!remainingString.isEmpty()){
								result[INDEX_STRING_AFTER] = remainingString;
							}
							result[INDEX_START_DATE] = startDateString;
							result[INDEX_END_DATE] = endDateString;
							
							return result;
						}
					}
				}
				startKeyIndex = indexOf(commandString, startDateKeys[i], startKeyIndex+startKeyLength);
			}	
		}
		return result;
	}
	
	/**
	 * Matches a date pattern: <start date key> <date> within the specified string.
	 * @param commandString - a command string.
	 * @return an array of string where the first field contains is_match(TRUE or FALSE); 
	 *         the second field contains stringBeforeStartDate, NULL if it is empty;
	 *         the third field contains startDateString, NULL if it is empty;
	 *         the fourth field contains endDateString, NULL if it is empty;
	 *         the fifth field contains stringAfterEndDate, NULL if it is empty.
	 */
	private static String[] matchDatePattern2(String commandString){
		return matchDatePatternWith(commandString, startDateKeys, START_KEY);
	}
	
	/**
	 * Matches a date pattern: <end date key> <date> within the specified string.
	 * @param commandString - a command string.
	 * @return an array of string where the first field contains is_match(TRUE or FALSE); 
	 *         the second field contains stringBeforeStartDate, NULL if it is empty;
	 *         the third field contains startDateString, NULL if it is empty;
	 *         the fourth field contains endDateString, NULL if it is empty;
	 *         the fifth field contains stringAfterEndDate, NULL if it is empty.
	 */
	private static String[] matchDatePattern3(String commandString){
		return matchDatePatternWith(commandString, endDateKeys, END_KEY);
	}
	
	/**
	 * Matches the specified command string with a date pattern.
	 * @param commandString - a command string.
	 * @param keys - an array of string which contains a set of keys.
	 * @param keyType - a string of key type. It should be neither START_KEY or END_KEY.
	 * @return an array of string where the first field contains is_match(TRUE or FALSE); 
	 *         the second field contains stringBeforeStartDate, NULL if it is empty;
	 *         the third field contains startDateString, NULL if it is empty;
	 *         the fourth field contains endDateString, NULL if it is empty;
	 *         the fifth field contains stringAfterEndDate, NULL if it is empty.
	 */
	private static String[] matchDatePatternWith(String commandString, String[] keys, String keyType){
		String[] result = {FALSE, NULL, NULL, NULL, NULL};
		
		for(int i = 0; i < keys.length; i++){
			//find first occurrence of a <key>
			int keyIndex =  indexOf(commandString, keys[i], 0);
			while(keyIndex >= 0){
				int keyLength = keys[i].length();
				//get string before the date key
				String stringBeforeKey = commandString.substring(0, keyIndex).trim();
				//get string after the date key
				String stringAfterKey = commandString.substring(keyIndex+keyLength).trim();
				
				int dateIndex = isValidDate(stringAfterKey);
				if(dateIndex == Control.INVALID){
					keyIndex = indexOf(commandString, keys[i], keyIndex+keyLength);
				} else {
					String dateString = stringAfterKey.substring(0, dateIndex);
					String remainingString = stringAfterKey.substring(dateIndex, stringAfterKey.length()).trim();
					result[INDEX_IS_MATCH] = TRUE;
					if(!stringBeforeKey.isEmpty()){
						result[INDEX_STRING_BEFORE] = stringBeforeKey;
					}
					if(!remainingString.isEmpty()){
						result[INDEX_STRING_AFTER] = remainingString;
					}
					if(keyType.equals(START_KEY)){
						result[INDEX_START_DATE] = dateString;						
						result[INDEX_END_DATE] = NULL;
					} else {
						result[INDEX_START_DATE] = NULL;
						result[INDEX_END_DATE] = dateString;
					}
					return result;
				}
			}	
		}
		return result;
	}
	
	/**
	 * Returns the index within this string of the first occurrence of the specified substring after a specified index. 
	 * @param s - the string within witch str is being searched.
	 * @param str - a string.
	 * @param fromIndex - the index to search from.
	 * @return the index of the first occurrence of the specified substring within this string; 
	 *         if no such value of k exists, then -1 is returned.
	 */
	private static int indexOf(String s, String str, int fromIndex){
		int index = s.substring(fromIndex, s.length()).indexOf(str);
		if(index < 0){
			return -1;
		} else {
			return index+fromIndex;
		}
	}
	
	/*
	private static int isValidStartDate(String startDateString){
		CustomDate dateTester = new CustomDate();
		String[] startDateStringArray = startDateString.trim().split("\\s+");
		int length = 0;
		if(startDateStringArray.length >= 4){
			length = 4;
		} else {
			length = startDateStringArray.length;
		}
		String tester = "";
		for(int i=0;i<length;i++){
			tester = (tester+" "+startDateStringArray[i]);
		}
		tester = tester.trim();
		while(!tester.isEmpty()){
			if(dateTester.convert(tester) == Control.VALID){
				return tester.length();
			}
			tester = removeLastWord(tester);
		}
		return Control.INVALID;
	}
	
	private static int isValidEndDate(String endDateString){
		CustomDate dateTester = new CustomDate();
		String[] endDateStringArray = endDateString.trim().split("\\s+");
		int length = 0;
		if(endDateStringArray.length >= 4){
			length = 4;
		} else {
			length = endDateStringArray.length;
		}
		String tester = "";
		for(int i=0;i<length;i++){
			tester = (tester+" "+endDateStringArray[i]);
		}
		tester = tester.trim();
		while(!tester.isEmpty()){
			if(dateTester.convert(tester) == Control.VALID){
				return tester.length();
			}
			tester = removeLastWord(tester);
		}
		return Control.INVALID;
	}
	*/
	
	/**
	 * Checks whether a string is a valid date or not.
	 * @param dateString - the string which is being checked.
	 * @return number of words in the date string if the string contains a date;
	 *         otherwise, returns INVALID(-1).
	 */
	private static int isValidDate(String dateString){
		CustomDate dateTester = new CustomDate();
		String[] dateStringArray = dateString.trim().split("\\s+");
		int length = 0;
		
		if(dateStringArray.length >= MAX_DATE_LENGTH){
			length = MAX_DATE_LENGTH;
		} else {
			length = dateStringArray.length;
		}
		
		String tester = "";
		
		//construct the date tester string.
		for(int i = 0; i < length; i++){
			tester = (tester + " " + dateStringArray[i]);
		}
		
		tester = tester.trim();
		
		while(!tester.isEmpty()){
			if(dateTester.convert(tester) == Control.VALID){
				return tester.length();
			}
			tester = removeLastWord(tester);
		}
		return Control.INVALID;
	}
	
	/*
	private static String getFirstWord(String userCommand) {
		String commandTypeString = userCommand.trim().split("\\s+")[0];
		return commandTypeString;
	}
	
	private static String removeFirstWord(String userCommand) {
		return userCommand.replace(getFirstWord(userCommand), "").trim();
	}
	*/
	
	private static String getLastWord(String commandString){
		String[] stringArray = commandString.trim().split("\\s+");
		return stringArray[stringArray.length-1];
	}
	
	private static String removeLastWord(String commandString){
		String lastWord = getLastWord(commandString);
		return commandString.substring(0, commandString.length()-lastWord.length()).trim();
	}
	
	/**
	 * Checks whether the specified command string has more than one hash tags.
	 * @param commandString - the string which is being checked.
	 * @return true if it contains more than one hash tag; false otherwise.
	 */
	private static boolean hasMultipleTags(String commandString){
		String[] words = commandString.trim().split("\\s+");
		boolean hasTag = false;
		
		for(int i = 0;i < words.length; i++){
			if(words[i].startsWith(HASH_TAG)){
				if(hasTag){
					return true;
				}
				hasTag = true;
			}
		}
		return false;
	}
	
	/**
	 * Removes the first hash tag from the specified string.
	 * @param commandString - the string from which the hash tag is being removed.
	 * @return the string with hash tag being removed if it contains a hash tag;
	 *         otherwise, returns the same string.
	 */
	private static String removeTag(String commandString){
		String[] words = commandString.trim().split("\\s+");
		String result = "";
		int index = indexOfTag(commandString);
		if(index >= 0){
			for(int i = 0; i < words.length; i++){
				if(i != index){
					result = result + " " + words[i];
				}
			}
			return result.trim();
		}
		return commandString;
	}
	
	/**
	 * Returns content of the hash tag in the specified command string.
	 * @param commandString - the string which may contains a hash tag.
	 * @return the content of the hash tag with '#' being removed if the string contains a hash tag;
	 *         the string NULL otherwise.
	 */
	private static String getTagName(String commandString){
		String[] words = commandString.trim().split("\\s+");
		for(int i = 0; i < words.length; i++){
			if(words[i].startsWith(HASH_TAG)){
				return words[i].replaceFirst(HASH_TAG, " ").trim();
			}
		}
		return NULL;
	}
	
	/**
	 * Returns the index of the first hash tag in the specified command string.
	 * @param commandString - the string which is being checked.
	 * @return the starting index if the command string contains a hag tag; -1 otherwise.
	 */
	private static int indexOfTag(String commandString) {
		String[] words = commandString.trim().split("\\s+");
		for(int i = 0; i < words.length; i++){
			if(words[i].startsWith(HASH_TAG)){
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Checks whether the specified command string contains more than one important marks.
	 * @param commandString - the string which is being checked.
	 * @return true if it contains more than one important marks; false, otherwise.
	 */
	private static boolean hasMultipleImptMarks(String commandString){
		String[] words = commandString.trim().split("\\s+");
		boolean isImpt = false;
		
		for(int i = 0; i < words.length; i++){
			if(words[i].equals(IMPT_MARK)){
				if(isImpt){
					return true;
				}
				isImpt = true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks whether the specified command string is important.
	 * @param commandString - the string which is being checked.
	 * @return the string TRUE if it is a important task; otherwise, returns the string FALSE.
	 */
	private static String isImptTask(String commandString) {
		String[] words = commandString.trim().split("\\s+");
		
		for(int i = 0; i < words.length; i++){
			if(words[i].equals(IMPT_MARK)){
				return TRUE;
			}
		}
		
		return FALSE;
	}
	
	/**
	 * Removes the important mark from the specified command string.
	 * @param commandString - the string from which the important mark is being removed.
	 * @return the string after removing the first important mark if it contains important mark;
	 *         otherwise the same string is returned.
	 */
	private static String removeImptMark(String commandString){
		if(isImptTask(commandString) == TRUE){
			String[] words = commandString.trim().split("\\s+");
			String result = "";
			for(int i = 0; i < words.length; i++){
				if(!words[i].equals(IMPT_MARK)){
					result = result + " " + words[i];
				}
			}
			return result.trim();
		}
		
		return commandString;
	}
/********************************************************************************************************************/
	
	
	
	
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
}
