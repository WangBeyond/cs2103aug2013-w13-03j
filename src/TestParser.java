import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

//@author A0098077N
public class TestParser {
	String[] parsedInfo;
	
	@Test
	public void test() {
		// Check empty command
		assertTrue("Check empty command", Parser.checkEmptyCommand("         "));
		assertFalse("Check non empty command", Parser.checkEmptyCommand("non empty command"));
		
		// Check ADD command type
		testParsingAdd();
		
		// Check REMOVE command type
		testParsingRemove();
		
		// Check SEARCH command type
		testParsingSearch();
		
		// Check RECOVER command type
		testParsingRecover();
		
		// Check EDIT command type
		testParsingEdit();
		
		// Check COMPLETE command type
		testParsingComplete();
		
		// Check INCOMPLETE command type
		testParsingIncomplete();
			
		// Check MARK command type 
		testParsingMark();
		
		// Check UNMARK command type
		testParsingUnmark();
		
		// Check SHOW_ALL command type
		testParsingShow();
		
		// Check EXIT command type
		testParsingExit();
		
		// Check HELP command type
		testParsingHelp();
		
		// Check SETTINGS command type
		testParsingSettings();
		
		// Check SYNC command type
		testParsingSync();
		
		// Check TODAY command type
		testParsingToday();
		
		// Check UNDO command type
		testParsingUndo();
		
		// Check REDO command type
		testParsingRedo();
		
		// Check CLEAR command type
		testParsingClear();
		
	}
	
	public void testParsingClear() {
		assertEquals("Check CLEAR command type", Common.COMMAND_TYPES.CLEAR_ALL, Parser.determineCommandType("clear JUnit Test"));
		assertEquals("Check CLEAR command type", Common.COMMAND_TYPES.CLEAR_ALL, Parser.determineCommandType("clr JUnit Test"));
		assertNull(Parser.parseCommand("clear JUnit Test", Common.COMMAND_TYPES.CLEAR_ALL, null, 0));
		assertNull(Parser.parseCommand("clr", Common.COMMAND_TYPES.CLEAR_ALL, null, 0));
	}
	
	
	public void testParsingRedo() {
		assertEquals("Check REDO command type", Common.COMMAND_TYPES.REDO, Parser.determineCommandType("redo JUnit Test"));
		assertNull(Parser.parseCommand("redo JUnit Test", Common.COMMAND_TYPES.REDO, null, 0));
	}
	
	
	public void testParsingUndo() {
		assertEquals("Check UNDO command type", Common.COMMAND_TYPES.UNDO, Parser.determineCommandType("undo JUnit Test"));
		assertNull(Parser.parseCommand("undo", Common.COMMAND_TYPES.UNDO, null, 0));
	}
	
	
	public void testParsingToday() {
		assertEquals("Check TODAY command type", Common.COMMAND_TYPES.TODAY, Parser.determineCommandType("today JUnit Test"));
		assertNull(Parser.parseCommand("today", Common.COMMAND_TYPES.TODAY, null, 0));
	}
	
	
	public void testParsingSync() {
		assertEquals("Check SYNC command type", Common.COMMAND_TYPES.SYNC, Parser.determineCommandType("sYnc JUnit Test"));
		assertNull(Parser.parseCommand("sync JUnit Test", Common.COMMAND_TYPES.SYNC, null, 0));
	}
	

	public void testParsingSettings() {
		assertEquals("Check SETTINGS command type", Common.COMMAND_TYPES.SETTINGS, Parser.determineCommandType("settings JUnit Test"));
		assertNull(Parser.parseCommand("settings", Common.COMMAND_TYPES.SETTINGS, null, 0));
	}


	public void testParsingHelp() {
		assertEquals("Check HELP command type", Common.COMMAND_TYPES.HELP, Parser.determineCommandType("help JUnit Test"));
		assertNull(Parser.parseCommand("help JUnit Test", Common.COMMAND_TYPES.HELP, null, 0));
	}
	
	
	public void testParsingExit() {
		assertEquals("Check EXIT command type", Common.COMMAND_TYPES.EXIT, Parser.determineCommandType("eXit JUnit Test"));
		assertEquals("Check EXIT command type", Common.COMMAND_TYPES.EXIT, Parser.determineCommandType("end JUnit Test"));
		assertNull(Parser.parseCommand("exit", Common.COMMAND_TYPES.EXIT, null, 0));
		assertNull(Parser.parseCommand("End JUnit Test", Common.COMMAND_TYPES.EXIT, null, 0));
	}

	
	public void testParsingShow() {
		assertEquals("Check SHOW_ALL command type", Common.COMMAND_TYPES.SHOW_ALL, Parser.determineCommandType("show JUnit Test"));
		assertEquals("Check SHOW_ALL command type", Common.COMMAND_TYPES.SHOW_ALL, Parser.determineCommandType("aLL JUnit Test"));
		assertEquals("Check SHOW_ALL command type", Common.COMMAND_TYPES.SHOW_ALL, Parser.determineCommandType("diSplay JUnit Test"));
		assertEquals("Check SHOW_ALL command type", Common.COMMAND_TYPES.SHOW_ALL, Parser.determineCommandType("liSt JUnit Test"));
		assertEquals("Check SHOW_ALL command type", Common.COMMAND_TYPES.SHOW_ALL, Parser.determineCommandType("ls JUnit Test"));
		assertNull(Parser.parseCommand("show", Common.COMMAND_TYPES.SHOW_ALL, null, 0));
		assertNull(Parser.parseCommand("all JUnit Test", Common.COMMAND_TYPES.SHOW_ALL, null, 0));
	}
	
	
	public void testParsingUnmark() throws ArrayComparisonFailure {
		assertEquals("Check UNMARK command type", Common.COMMAND_TYPES.UNMARK, Parser.determineCommandType("unmaRk JUnit Test"));
		assertEquals("Check UNMARK command type", Common.COMMAND_TYPES.UNMARK, Parser.determineCommandType("unhiGhLight JUnit Test"));
		parsedInfo = new String[] {"2", "8"};
		assertArrayEquals("Compare UNMARK command parsed result", parsedInfo, Parser.parseCommand("unmark 2 8", Common.COMMAND_TYPES.UNMARK, null, 0));
		parsedInfo = new String[] {"1", "2", "3", "4", "8", "9"};
		assertArrayEquals("Compare MARK command parsed result", parsedInfo, Parser.parseCommand("unmark 1-4 8 9", Common.COMMAND_TYPES.UNMARK, null, 0));
	}
	
	
	public void testParsingMark() throws ArrayComparisonFailure {
		assertEquals("Check MARK command type", Common.COMMAND_TYPES.MARK, Parser.determineCommandType("mark JUnit Test"));
		assertEquals("Check MARK command type", Common.COMMAND_TYPES.MARK, Parser.determineCommandType("highLight JUnit Test"));
		parsedInfo = new String[] {"3", "9"};
		assertArrayEquals("Compare MARK command parsed result", parsedInfo, Parser.parseCommand("mark 3 9", Common.COMMAND_TYPES.MARK, null, 0));
		parsedInfo = new String[] {"2", "3", "4", "5", "6"};
		assertArrayEquals("Compare MARK command parsed result", parsedInfo, Parser.parseCommand("mark 2-6", Common.COMMAND_TYPES.MARK, null, 0));
	}
	
	
	public void testParsingIncomplete() throws ArrayComparisonFailure {
		assertEquals("Check INCOMPLETE command type", Common.COMMAND_TYPES.INCOMPLETE, Parser.determineCommandType("Incomplete JUnit Test"));
		assertEquals("Check INCOMPLETE command type", Common.COMMAND_TYPES.INCOMPLETE, Parser.determineCommandType("uNdone JUnit Test"));
		parsedInfo = new String[] {"12", "4"};
		assertArrayEquals("Compare INCOMPLETE command parsed result", parsedInfo, Parser.parseCommand("incomplete 12 4", Common.COMMAND_TYPES.INCOMPLETE, null, 0));
		parsedInfo = new String[] {"2", "3", "4", "5", "1", "2", "3", "4"};
		assertArrayEquals("Compare INCOMPLETE command parsed result", parsedInfo, Parser.parseCommand("incomplete 2-5 1-4", Common.COMMAND_TYPES.INCOMPLETE, null, 0));
	}
	
	
	public void testParsingComplete() throws ArrayComparisonFailure {
		assertEquals("Check COMPLETE command type", Common.COMMAND_TYPES.COMPLETE, Parser.determineCommandType("complete JUnit Test"));
		assertEquals("Check COMPLETE command type", Common.COMMAND_TYPES.COMPLETE, Parser.determineCommandType("doNe JUnit Test"));
		parsedInfo = new String[] {"1", "3"};
		assertArrayEquals("Compare COMPLETE command parsed result", parsedInfo, Parser.parseCommand("complete 1 3", Common.COMMAND_TYPES.COMPLETE, null, 0));
		parsedInfo = new String[] {"2", "3", "6", "7"};
		assertArrayEquals("Compare COMPLETE command parsed result", parsedInfo, Parser.parseCommand("complete 2-3 6-7", Common.COMMAND_TYPES.COMPLETE, null, 0));
	}
	
	
	public void testParsingEdit() throws ArrayComparisonFailure {
		assertEquals("Check EDIT command type", Common.COMMAND_TYPES.EDIT, Parser.determineCommandType("Edit JUnit Test"));
		assertEquals("Check EDIT command type", Common.COMMAND_TYPES.EDIT, Parser.determineCommandType("mod JUnit Test"));
		assertEquals("Check EDIT command type", Common.COMMAND_TYPES.EDIT, Parser.determineCommandType("mOdify JUnit Test"));
		assertEquals("Check EDIT command type", Common.COMMAND_TYPES.EDIT, Parser.determineCommandType("set JUnit Test"));
		parsedInfo = new String[] {"2", "null", "#cs2103", "5 Aug", "28 Dec",
				"true", "null"};
		assertArrayEquals("Compare EDIT command parsed result", parsedInfo,
				Parser.parseCommand(
						"edit 2 from 5 Aug to 28 Dec #cs2103 *",
						Common.COMMAND_TYPES.EDIT, null, 0));
		parsedInfo = new String[] {"5", "JUnit Test", "null", "null", "null",
				"false", "weekly 3 times"};
		assertArrayEquals("Compare EDIT command parsed result", parsedInfo,
				Parser.parseCommand(
						"edit 5 JUnit Test weekly 3 times",
						Common.COMMAND_TYPES.EDIT, null, 0));
	}
	
	
	public void testParsingRecover() throws ArrayComparisonFailure {
		assertEquals("Check RECOVER command type", Common.COMMAND_TYPES.RECOVER, Parser.determineCommandType("recover JUnit Test"));
		assertEquals("Check RECOVER command type", Common.COMMAND_TYPES.RECOVER, Parser.determineCommandType("rEc JUnit Test"));
		parsedInfo = new String[] {"2", "1", "7"};
		assertArrayEquals("Compare RECOVER command parsed result", parsedInfo, Parser.parseCommand("recover 2 1 7", Common.COMMAND_TYPES.RECOVER, null, 0));
		parsedInfo = new String[] {"5", "6", "7", "8"};
		assertArrayEquals("Compare RECOVER command parsed result", parsedInfo, Parser.parseCommand("recover 5-8", Common.COMMAND_TYPES.RECOVER, null, 0));
	}
	

	public void testParsingSearch() throws ArrayComparisonFailure {
		assertEquals("Check SEARCH command type", Common.COMMAND_TYPES.SEARCH, Parser.determineCommandType("search JUnit Test"));
		assertEquals("Check SEARCH command type", Common.COMMAND_TYPES.SEARCH, Parser.determineCommandType("finD JUnit Test"));
		parsedInfo = new String[] { "null", "#cs2103", "21/12", "23 Dec",
				"true", "null" };
		assertArrayEquals("Compare SEARCH command parsed result", parsedInfo,
				Parser.parseCommand(
						"search from 21/12 to 23 Dec #cs2103 *",
						Common.COMMAND_TYPES.SEARCH, null, 0));
		parsedInfo = new String[] { "JUnit test", "null", "3 Sep 1230am", "null",
				"false", "null" };
		assertArrayEquals("Compare SEARCH command parsed result", parsedInfo,
				Parser.parseCommand(
						"search JUnit test from 3 Sep 1230am",
						Common.COMMAND_TYPES.SEARCH, null, 0));
		parsedInfo = new String[] { "null", "#cs2103t", "null", "2 Nov",
				"true", "null" };
		assertArrayEquals("Compare SEARCH command parsed result", parsedInfo,
				Parser.parseCommand(
						"search * by 2 Nov #cs2103t",
						Common.COMMAND_TYPES.SEARCH, null, 0));
	}
	
	
	public void testParsingRemove() throws ArrayComparisonFailure {
		assertEquals("Check REMOVE command type", Common.COMMAND_TYPES.REMOVE, Parser.determineCommandType("remove JUnit test"));
		assertEquals("Check REMOVE command type", Common.COMMAND_TYPES.REMOVE, Parser.determineCommandType("Delete JUnit test"));
		assertEquals("Check REMOVE command type", Common.COMMAND_TYPES.REMOVE, Parser.determineCommandType("rM JUnit test"));
		assertEquals("Check REMOVE command type", Common.COMMAND_TYPES.REMOVE, Parser.determineCommandType("del JUnit test"));
		parsedInfo = new String[] {"2", "4", "8"};
		assertArrayEquals("Compare REMOVE command parsed result", parsedInfo, Parser.parseCommand("remove 2 4 8", Common.COMMAND_TYPES.REMOVE, null, 0));
	}
	
	
	public void testParsingAdd() throws ArrayComparisonFailure {
		assertEquals("Check ADD command type", Common.COMMAND_TYPES.ADD, Parser.determineCommandType("add JUnit test"));
		assertEquals("Check ADD command type", Common.COMMAND_TYPES.ADD, Parser.determineCommandType("inserT JUnit test"));
		parsedInfo = new String[] { "JUnit test", "#cs2103", "21/12", "23 Dec",
				"true", "null" };
		assertArrayEquals("Compare ADD command parsed result", parsedInfo,
				Parser.parseCommand(
						"add JUnit test from 21/12 to 23 Dec #cs2103 *",
						Common.COMMAND_TYPES.ADD, null, 0));
		parsedInfo = new String[] { "JUnit test", "#cs2101", "11-9 3:30",
				"21/10 6:30am", "false", "null" };
		assertArrayEquals(
				"Compare ADD command parsed result",
				parsedInfo,
				Parser.parseCommand(
						"insert JUnit test from 11-9 3:30 by 21/10 6:30am #cs2101",
						Common.COMMAND_TYPES.ADD, null, 0));
		try{
			Parser.parseCommand("add # #", Common.COMMAND_TYPES.ADD, null, 0);
			Parser.parseCommand("add from 21/10 to 23/12", Common.COMMAND_TYPES.ADD, null, 0);
			Parser.parseCommand("add * *", Common.COMMAND_TYPES.ADD, null, 0);
			assertTrue(false);
		} catch(IllegalArgumentException e){
			assertTrue(true);
		}
	}
}
