import java.util.Collections;

import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;


public class Common {
	static enum COMMAND_TYPES {
		ADD, REMOVE, RECOVER, SEARCH, EDIT, COMPLETE, INCOMPLETE, UNDO, REDO, CLEAR_ALL, TODAY, SHOW_ALL, SYNC, SETTINGS, HELP, EXIT, INVALID, MARK, UNMARK
	}
	
	static String[] COMMAND_TYPES_STR = { "add", "remove", "delete", "edit",
		"modify", "search", "find", "clear", "mark", "unmark", "complete",
		"incomplete", "all", "list", "today", "help", "del", "exit", "rm",
		"show", "ls", "clr", "done", "undone", "settings", "sync", "recover", "rec" };
	
	/* start date keys */
	static String[] startDateKeys = { "start from", "start at",
			"start on", "begin from", "begin at", "begin on", "from", "after", "on",
			"at"};

	/* end date keys */
	static String[] endDateKeys = { "end on", "end at", "end by",
			"end before", "to", "till", "until", "by", "due", "before", "next", "today",
			"tonight" };
	
	/************************************* Feedback Messsage  ******************************/
	static final String WELCOME_MESSAGE = "Welcome back, %s";
	
	static final String MESSAGE_INVALID_COMMAND_TYPE = "Invalid Command Type!";
	static final String MESSAGE_EMPTY_COMMAND = "Empty Command!";
	static final String MESSAGE_INVALID_UNDO = "You cannot undo anymore!";
	static final String MESSAGE_INVALID_REDO = "You cannot redo anymore!";
	static final String MESSAGE_SUCCESSFUL_REDO = "Redo was successful.";
	static final String MESSAGE_ADD_TIP = "Tip for ADD command";
	static final String MESSAGE_EDIT_TIP = "Tip for EDIT command";
	static final String MESSAGE_RECOVER_INDEX_TIP = "Tip for RECOVER with index command";
	static final String MESSAGE_RECOVER_INFO_TIP = "Tip for RECOVER with info command";
	static final String MESSAGE_REMOVE_INDEX_TIP = "Tip for REMOVE with index command";
	static final String MESSAGE_REMOVE_INFO_TIP = "Tip for REMOVE with info command";
	static final String MESSAGE_SEARCH_TIP = "Tip for SEARCH command";
	static final String MESSAGE_TODAY_TIP = "Tip for TODAY command";
	static final String MESSAGE_SHOW_ALL_TIP = "Tip for SHOW command";
	static final String MESSAGE_CLEAR_ALL_TIP = "Tip for CLEAR command";
	static final String MESSAGE_UNDO_TIP = "Tip for UNDO command";
	static final String MESSAGE_REDO_TIP = "Tip for REDO command";
	static final String MESSAGE_MARK_INDEX_TIP = "Tip for MARK with index command";
	static final String MESSAGE_MARK_INFO_TIP = "Tip for MARK with info command";
	static final String MESSAGE_UNMARK_INDEX_TIP = "Tip for UNMARK with index command";
	static final String MESSAGE_UNMARK_INFO_TIP = "Tip for UNMAR with info command";
	static final String MESSAGE_COMPLETE_INDEX_TIP = "Tip for COMPLETE with index command";
	static final String MESSAGE_COMPLETE_INFO_TIP = "Tip for COMPLETE with info command";
	static final String MESSAGE_INCOMPLETE_INDEX_TIP = "Tip for INCOMPLETE with index command";
	static final String MESSAGE_INCOMPLETE_INFO_TIP = "Tip for INCOMPLETE with info command";
	static final String MESSAGE_SYNC_TIP = "Tip for SYNC command";
	static final String MESSAGE_HELP_TIP = "Tip for HELP command";
	static final String MESSAGE_SETTINGS_TIP = "Tip for SETTINGS command";
	static final String MESSAGE_EXIT_TIP = "Tip for EXIT command";
	static final String MESSAGE_REQUEST_COMMAND = "Please enter a command or type help to view commands.";
	
	public static final String MESSAGE_SUCCESSFUL_SHOW_ALL = "Show all the tasks";
	public static final String MESSAGE_SUCCESSFUL_CLEAR_ALL = "Clear all the tasks";
	public static final String MESSAGE_SUCCESSFUL_SEARCH = "Successful search!";
	public static final String MESSAGE_NO_RESULTS = "No results found!";
	public static final String MESSAGE_SUCCESSFUL_ADD = "One task has been added successfully.";
	public static final String MESSAGE_INVALID_START_END_DATES = "There must be both start and end dates for repetitive task.";
	public static final String MESSAGE_INVALID_TIME_REPETITIVE = "The difference is larger than the limit of repetitive period.";
	public static final String MESSAGE_SUCCESSFUL_EDIT = "Indicated task has been edited successfully.";
	public static final String MESSAGE_SUCCESSFUL_REMOVE = "Indicated tasks has/have been removed.";
	public static final String MESSAGE_INVALID_DATE_RANGE = "Invalid date range as start date is after end date.";
	public static final String MESSAGE_DUPLICATE_INDEXES = "There are duplicate indexes!";
	public static final String MESSAGE_INDEX_OUT_OF_BOUNDS = "There is an index outside the range of the list.";
	public static final String MESSAGE_SUCCESSFUL_MARK = "Indicated task(s) has/have been marked successfully.";
	public static final String MESSAGE_SUCCESSFUL_UNMARK = "Indicated task(s) has/have been unmarked successfully.";
	public static final String MESSAGE_SUCCESSFUL_COMPLETE = "Indicated task(s) has/have been marked as complete.";
	public static final String MESSAGE_SUCCESSFUL_INCOMPLETE = "Indicated task(s) has/have been marked as incomplete.";
	public static final String MESSAGE_SUCCESSFUL_RECOVER = "Indicated task(s) has/have been recovered successfully.";
	public static final String MESSAGE_SUCCESSFUL_UNDO = "Undo was successful.";
	public static final String MESSAGE_WRONG_COMPLETE_TABS = "Cannot complete the tasks in this current tab.";
	public static final String MESSAGE_WRONG_INCOMPLETE_TABS = "Cannot incomplete the tasks in this current tab.";
	public static final String MESSAGE_WRONG_RECOVER_TABS = "Cannot recover the tasks in this current tab.";
	public static final String MESSAGE_SUCCESSFUL_HELP = "Help window opened.";
	public static final String MESSAGE_SUCCESSFUL_SETTINGS = "Settings window opened.";
	public static final String MESSAGE_SYNC_SUCCESSFUL = "Successful synchronized.";
	public static final String MESSAGE_SYNC_INVALID_USERNAME_PASSWORD = "Synchronization failed: Invalid username and password.";
	public static final String MESSAGE_SYNC_SERVICE_STOPPED = "Synchronization service has stopped working.";
	public static final String MESSAGE_SYNC_FAIL_TO_CREATE_CALENDAR = "Fail to create a calendar.";
	
	public static final String NO_EDITING_INFO = "No infos for editing";
	public static final String INVALID_INDEX = "Invalid index";
	public static final String INVALID_RANGE_END_SMALLER = "Invalid range as end point is smaller than start point";
	public static final String INVALID_RANGE = "Invalid Range";
	
	/******************************symbols *****************************************/
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NULL = "null";	
	public static final String IMPT_MARK = "*";
	public static final String HASH_TAG = "#";
	public static final String HYPHEN = "-";
	
	/******************************key combination*************************************/
	static final KeyCombination undo_hot_key = new KeyCodeCombination(
			KeyCode.Z, KeyCodeCombination.CONTROL_DOWN);
	static final KeyCombination redo_hot_key = new KeyCodeCombination(
			KeyCode.Y, KeyCodeCombination.CONTROL_DOWN);
	static final KeyCombination esc = new KeyCodeCombination(KeyCode.ESCAPE);
	static final KeyCombination saveSettings = new KeyCodeCombination(
			KeyCode.S, KeyCombination.CONTROL_DOWN);
	static final KeyCombination collapseWindow = new KeyCodeCombination(KeyCode.UP,
			KeyCombination.CONTROL_DOWN);
	static final KeyCombination expandWindow = new KeyCodeCombination(KeyCode.DOWN,
			KeyCombination.CONTROL_DOWN);
	static final KeyCombination hideWindow = new KeyCodeCombination(KeyCode.H,
			KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
	static final KeyCombination changeTab = new KeyCodeCombination(KeyCode.TAB,
			KeyCombination.CONTROL_DOWN);
	
	/****************************** tab index **********************************************/
	static final int PENDING_TAB = 0;
	static final int COMPLETE_TAB = 1;
	static final int TRASH_TAB = 2;
	
	/******************************index of command info ***************************************/
	public static final int INDEX_TYPING_INFO = -2;
	public static final int INDEX_COMMAND_TYPE = -1;
	public static final int INDEX_WORK_INFO = 0;
	public static final int INDEX_TAG = 1;
	public static final int INDEX_START_DATE = 2;
	public static final int INDEX_END_DATE = 3;
	public static final int INDEX_IS_IMPT = 4;
	public static final int INDEX_REPEATING = 5;
	public static final int INDEX_INDEX_INFO = 6;
	
	public static final int MINUTE_IN_MILLIS = 60000;
	
	/***********************************theme mode ************************************************/
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
