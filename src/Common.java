import java.util.Collections;

import javafx.collections.ObservableList;


public class Common {
	static enum COMMAND_TYPES {
		ADD, REMOVE, RECOVER, SEARCH, EDIT, COMPLETE, INCOMPLETE, UNDO, REDO, CLEAR_ALL, TODAY, SHOW_ALL, SYNC, SETTINGS, HELP, EXIT, INVALID, MARK, UNMARK
	}
	
	/* start date keys */
	static String[] startDateKeys = { "start from", "start at",
			"start on", "begin from", "begin at", "begin on", "from", "after", "on",
			"at"};

	/* end date keys */
	static String[] endDateKeys = { "end on", "end at", "end by",
			"end before", "to", "till", "until", "by", "due", "before", "next", "today",
			"tonight" };
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NULL = "null";	
	public static final String IMPT_MARK = "*";
	public static final String HASH_TAG = "#";
	public static final String HYPHEN = "-";
	
	public static final int MINUTE_IN_MILLIS = 60000;
	
	public static final String DAY_MODE = "Day mode";
	public static final String NIGHT_MODE = "Night mode";
	
	public static void sortList(ObservableList<Task> list) {
		Collections.sort(list);
		updateIndexInList(list);
	}
	
	private static void updateIndexInList(ObservableList<Task> list) {
		boolean hasLastOverdue = false;
		for (int i = list.size() - 1; i >= 0; i--) {
			list.get(i).setIndexInList(i);
			list.get(i).setIsLastOverdue(false);
			if (!hasLastOverdue && list.get(i).isOverdueTask()) {
				list.get(i).setIsLastOverdue(true);
				hasLastOverdue = true;
			}
		}
	}
	
	/****************** string operation *********************************/
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
}
