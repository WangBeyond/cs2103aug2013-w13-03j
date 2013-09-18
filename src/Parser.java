
public class Parser {
	private static String[] startDateKeys = {
		"start from", "start at", "start on", "begin from", "begin at", "begin on", "from"
	};
	private static String[] endDateKeys = {
		"end on", "end at", "end by", "end before", "to", "till", "until", "by", "due"
	};
	
	public static final int INDEX_IS_MATCH = 0;
	public static final int INDEX_WORK_INFO = 1;
	public static final int INDEX_START_DATE = 2;
	public static final int INDEX_END_DATE = 3;
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NULL = "null";	
	
	
	public static String[] parseAddCommand(String content){
		if(content.isEmpty()){
			String[] s = {null, null, null ,null ,null};
			return s;
		}
		
		String[] tempResult = new String[4];
		String workInfo = "";
		String startDateString = "";
		String endDateString = "";
		String tag = "";
		String isImpt = "";
		
		content = content.trim();
		
		if(isImptTask(content)){
			isImpt = TRUE;
			content = removeImptMark(content);
		}
		if(hasTag(content)){
			tag = getTagName(content);
			content = removeTag(content);
		}
		
		String commandString = content;
		
		tempResult = matchDatePattern1(commandString);
		if(tempResult[INDEX_IS_MATCH] == TRUE){
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
		if(tempResult[INDEX_IS_MATCH] == TRUE){
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
		if(tempResult[INDEX_IS_MATCH] == TRUE){
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
		if(tempResult[INDEX_IS_MATCH] == TRUE){
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
		if(tempResult[INDEX_IS_MATCH] == TRUE){
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
	
	/*pattern: <start key> <date> <end key> <date>*/
	private static String[] matchDatePattern1(String commandString){
		String[] result = {FALSE, NULL, NULL, NULL};
		String workInfo = "";
		String unmatchedString = commandString;
		boolean isStartDateMatch = false;
		//find last occurrence of a <start key>
		for(int i=0;i<startDateKeys.length;i++){
			int index = unmatchedString.lastIndexOf(startDateKeys[i]);
			if(index < 0){
				continue;
			} else {
				int length = startDateKeys[i].length();
				isStartDateMatch = true;
				workInfo = unmatchedString.substring(0, index-1).trim();
				unmatchedString = unmatchedString.substring(index+length, unmatchedString.length());
				break;
			}
		}
		if(isStartDateMatch){
			String startDateString = "";
			String endDateString = "";
			boolean isEndDateMatch = false;
			//find last occurrence of a <end key>
			for(int i=0;i<endDateKeys.length;i++){
				int index = unmatchedString.lastIndexOf(endDateKeys[i]);
				if(index < 0){
					continue;
				} else {
					int length = endDateKeys[i].length();
					isEndDateMatch = true;
					startDateString = unmatchedString.substring(0, index-1).trim();
					endDateString = unmatchedString.substring(index+length, unmatchedString.length()).trim();
					break;
				}
			}
			if(isEndDateMatch){
				CustomDate DateTester = new CustomDate();
				if(DateTester.convert(startDateString)==Control.VALID && DateTester.convert(endDateString)==Control.VALID){
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
	
	/*pattern: <date> <end key> <date>*/
	private static String[] matchDatePattern2(String commandString){
		String[] result = {FALSE, NULL, NULL, NULL};
		String unmatchedString = commandString;
		String workInfo = "";
		String startDateString = "";
		String endDateString = "";
		boolean isEndDateMatch = false;
		boolean isStartDateMatch = false;
		//find last occurrence of a <end key>
		for(int i=0;i<endDateKeys.length;i++){
			int index = unmatchedString.lastIndexOf(endDateKeys[i]);
			if(index < 0){
				continue;
			} else {
				int length = endDateKeys[i].length();
				isEndDateMatch = true;
				endDateString = unmatchedString.substring(index+length, unmatchedString.length()).trim();
				unmatchedString = unmatchedString.substring(0, index-1).trim();
				break;
			}
		}
		if(isEndDateMatch){
			CustomDate dateTester = new CustomDate();
			if(dateTester.convert(endDateString)==Control.VALID){
				while(!unmatchedString.isEmpty()){
					if(dateTester.convert(getLastWord(unmatchedString))==Control.VALID){
						isStartDateMatch = true;
						if(dateTester.convert(getLastWord(unmatchedString)+" "+startDateString)==Control.VALID){
							startDateString = getLastWord(unmatchedString)+" "+startDateString;
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
		if(isStartDateMatch){
			result[INDEX_IS_MATCH] = TRUE;
			result[INDEX_WORK_INFO] = workInfo;
			result[INDEX_START_DATE] = startDateString.trim();
			result[INDEX_END_DATE] = endDateString;
		}
		return result;
	}
	
	/*pattern: <start key> <date>*/
	private static String[] matchDatePattern3(String commandString){
		String[] result = {FALSE, NULL, NULL, NULL};
		String workInfo = "";
		String unmatchedString = commandString;
		String startDateString = "";
		boolean isStartDateMatch = false;
		CustomDate dateTester = new CustomDate();
		//find last occurrence of a <start key>
		for(int i=0;i<startDateKeys.length;i++){
			int index = unmatchedString.lastIndexOf(startDateKeys[i]);
			if(index < 0){
				continue;
			} else {
				int length = startDateKeys[i].length();
				isStartDateMatch = true;
				workInfo = unmatchedString.substring(0, index-1).trim();
				startDateString = unmatchedString.substring(index+length, unmatchedString.length()).trim();
				break;
			}
		}
		if(isStartDateMatch){
			if(dateTester.convert(startDateString)==Control.VALID){
				result[INDEX_IS_MATCH] = TRUE;
				result[INDEX_WORK_INFO] = workInfo;
				result[INDEX_START_DATE] = startDateString;
			}
		}
		return result;
	}
	
	/*pattern: <end key> <date>*/
	private static String[] matchDatePattern4(String commandString){
		String[] result = {FALSE, NULL, NULL, NULL};
		String workInfo = "";
		String unmatchedString = commandString;
		String endDateString = "";
		boolean isEndDateMatch = false;
		CustomDate dateTester = new CustomDate();
		//find last occurrence of a <end key>
		for(int i=0;i<endDateKeys.length;i++){
			int index = unmatchedString.lastIndexOf(endDateKeys[i]);
			if(index < 0){
				continue;
			} else {
				int length = endDateKeys[i].length();
				isEndDateMatch = true;
				workInfo = unmatchedString.substring(0, index-1).trim();
				endDateString = unmatchedString.substring(index+length, unmatchedString.length()).trim();
				break;
			}
		}
		if(isEndDateMatch){
			if(dateTester.convert(endDateString)==Control.VALID){
				result[INDEX_IS_MATCH] = TRUE;
				result[INDEX_WORK_INFO] = workInfo;
				result[INDEX_END_DATE] = endDateString;
			}
		}
		return result;
	}	
	
	/*pattern: <date>*/
	private static String[] matchDatePattern5(String commandString){
		String[] result = {FALSE, NULL, NULL, NULL};
		String workInfo = "";
		String startDateString = "";
		boolean isStartDate = false;
		String unmatchedString = commandString;
		CustomDate dateTester = new CustomDate();
		while(!unmatchedString.isEmpty()){
			if(dateTester.convert(getLastWord(unmatchedString))==Control.VALID){
				isStartDate = true;
				startDateString = getLastWord(unmatchedString)+" "+startDateString;
				unmatchedString = removeLastWord(unmatchedString);
				workInfo = unmatchedString;
			} else {
				break;
			}
		}
		if(isStartDate){
			result[INDEX_IS_MATCH] = TRUE;
			result[INDEX_WORK_INFO] = workInfo;
			result[INDEX_START_DATE] = startDateString;
		}
		return result;
	}
	
	
	private static String getFirstWord(String userCommand) {
		String commandTypeString = userCommand.trim().split("\\s+")[0];
		return commandTypeString;
	}
	
	private static String removeFirstWord(String userCommand) {
		return userCommand.replace(getFirstWord(userCommand), "").trim();
	}
	
	private static String getLastWord(String commandString){
		String[] stringArray = commandString.trim().split("\\s+");
		return stringArray[stringArray.length-1];
	}
	
	private static String removeLastWord(String commandString){
		String lastWord = getLastWord(commandString);
		return commandString.substring(0, commandString.length()-lastWord.length()).trim();
	}
	
	public static String getStartDateString(String s) {
		String[] keys = {"from", "start from", "start at", "start on", "begin at", "begin from", "begin on"};
		return removeInputKeys(s, keys);
	}
	
	public static String getEndDateString(String s) {
		String[] keys = {"to", "till", "until", "by", "end at", "begin by", "end on", "end before", "due"};
		return removeInputKeys(s, keys);
	}
	
	private static String removeInputKeys(String s, String[] keys) {
		String StringRemovedKey = null;
		for(int i=0; i<keys.length; i++){
			if(s.contains(keys[i])){
				StringRemovedKey = s.replace(keys[i], " ").trim();
				break;
			}
		}
		return StringRemovedKey;
	}
	
	private static String getTagName(String s) {
		return getLastWord(s).replace("#", "").trim();
	}
	public static boolean isStartDate(String content) {
		String[] inputKeys = {"start", "begin", "from"};
		for(int i=0;i<inputKeys.length;i++){
			if(content.contains(inputKeys[i])){
				return true;
			}
		}
		return false;
	}
	
	public static CustomDate getStartDate(String content){
			CustomDate tempDate = new CustomDate();
			String DateString = getStartDateString(content);
			if(tempDate.convert(DateString) == Control.VALID)
				return tempDate;
			else
				return null;
	}

	public static boolean isEndDate(String content) {
		String[] inputKeys = {"end", "to", "till", "until", "by", "due"};
		for(int i=0;i<inputKeys.length;i++){
			if(content.contains(inputKeys[i])){
				return true;
			}
		}
		return false;
	}
	
	public static CustomDate getEndDate(String content){
			CustomDate tempDate = new CustomDate();
			String DateString = getEndDateString(content);
			if(tempDate.convert(DateString) == Control.VALID)		
				return tempDate;
			else
				return null;
	}
	
	private static boolean hasTag(String commandString) {
		String lastWord = getLastWord(commandString);
		return lastWord.startsWith("#");
	}
	
	private static String removeTag(String commandString){
		if(hasTag(commandString)){
			return removeLastWord(commandString);
		}
		return commandString;
	}
	
	private static boolean isImptTask(String commandString) {
		return commandString.charAt(commandString.length()-1)=='*';
	}
	
	private static String removeImptMark(String commandString){
		if(isImptTask(commandString)){
			return commandString.substring(0, commandString.length()-1).trim();
		}
		return commandString;
	}
}
