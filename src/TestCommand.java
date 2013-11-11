import static org.junit.Assert.assertEquals;
import org.junit.Test;

//@author A0098077N
public class TestCommand {
	Model testModel = new Model();
	Command testCommand;
	String[] parsedInfo;

	@Test
	public void test() {
		// ADD command
		testAddCommand();
		// MARK command
		testMarkCommand();
		// UNMARK command
		testUnmarkCommand();
		// EDIT command
		testEditCommand();
		// REMOVE command
		testRemoveCommand();
		// RECOVER command
		testRecoverCommand();
		// COMPLETE command
		testCompleteCommand();
		// INCOMPLETE command
		testIncompleteCommand();
	}

	private void testIncompleteCommand() {
		parsedInfo = Parser.parseCommand("incomplete 1", Common.COMMAND_TYPES.INCOMPLETE, testModel, 1);
		testCommand = new IncompleteCommand(parsedInfo, testModel, 1);
		assertEquals("Test INCOMPLETE command", "Indicated task(s) has/have been marked as incomplete.",testCommand.execute());
		
		try{
			parsedInfo = Parser.parseCommand("incomplete 1", Common.COMMAND_TYPES.COMPLETE, testModel, 0);
			testCommand = new IncompleteCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test INCOMPLETE command","Cannot incomplete the tasks in this current tab.", e.getMessage());
		}
	}

	private void testCompleteCommand() {
		parsedInfo = Parser.parseCommand("complete 1", Common.COMMAND_TYPES.COMPLETE, testModel, 0);
		testCommand = new CompleteCommand(parsedInfo, testModel, 0);
		assertEquals("Test COMPLETE command", "Indicated task(s) has/have been marked as complete.",testCommand.execute());
		
		try{
			parsedInfo = Parser.parseCommand("complete 1", Common.COMMAND_TYPES.COMPLETE, testModel, 1);
			testCommand = new CompleteCommand(parsedInfo, testModel, 1);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test COMPLETE command","Cannot complete the tasks in this current tab.", e.getMessage());
		}
	}

	private void testRecoverCommand() {
		parsedInfo = Parser.parseCommand("recover 1", Common.COMMAND_TYPES.RECOVER, testModel, 2);
		testCommand = new RecoverCommand(parsedInfo, testModel, 2);
		assertEquals("Test RECOVER command", "Indicated task(s) has/have been recovered successfully.",testCommand.execute());
		
		try{
			parsedInfo = Parser.parseCommand("recover 1", Common.COMMAND_TYPES.RECOVER, testModel, 1);
			testCommand = new RecoverCommand(parsedInfo, testModel, 1);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test REMOVE command","Cannot recover the tasks in this current tab.", e.getMessage());
		}
	}

	private void testRemoveCommand() {
		parsedInfo = Parser.parseCommand("remove 1", Common.COMMAND_TYPES.REMOVE, testModel, 0);
		testCommand = new RemoveCommand(parsedInfo, testModel, 0);
		assertEquals("Test REMOVE command", "Indicated tasks has/have been removed.",testCommand.execute());
		
		try{
			parsedInfo = Parser.parseCommand("remove 1-5", Common.COMMAND_TYPES.REMOVE, testModel, 1);
			testCommand = new RemoveCommand(parsedInfo, testModel, 1);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test REMOVE command","There is an index outside the range of the list.", e.getMessage());
		}
	}

	public void testAddCommand() {
		parsedInfo = Parser.parseCommand("add JUnit Test from 6pm to 8pm", Common.COMMAND_TYPES.ADD, testModel, 0);
		testCommand = new AddCommand(parsedInfo, testModel, 0);
		assertEquals("Test ADD command", "One task has been added successfully.",testCommand.execute());
		
		parsedInfo = Parser.parseCommand("add CS2103 demo from 13/11 1:30pm to 2pm", Common.COMMAND_TYPES.ADD, testModel, 0);
		testCommand = new AddCommand(parsedInfo, testModel, 0);
		assertEquals("Test ADD command", "One task has been added successfully.",testCommand.execute());
		
		parsedInfo = Parser.parseCommand("add CS2105 last lecture from 11/11 2pm to 4pm", Common.COMMAND_TYPES.ADD, testModel, 0);
		testCommand = new AddCommand(parsedInfo, testModel, 0);
		assertEquals("Test ADD command", "One task has been added successfully.",testCommand.execute());
		
		parsedInfo = Parser.parseCommand("add CS2101 Written Test from 11/11 10am to 12pm", Common.COMMAND_TYPES.ADD, testModel, 0);
		testCommand = new AddCommand(parsedInfo, testModel, 0);
		assertEquals("Test ADD command", "One task has been added successfully.",testCommand.execute());
		
		try{
			parsedInfo = Parser.parseCommand("add from 6pm to 8pm", Common.COMMAND_TYPES.ADD, testModel, 0);
			testCommand = new AddCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test ADD command", "Invalid command: work information cannot be empty.", e.getMessage());
		}
		
		try{
			parsedInfo = Parser.parseCommand("add from 6pm from 7pm", Common.COMMAND_TYPES.ADD, testModel, 0);
			testCommand = new AddCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test ADD command", "Invalid Command: Multiple Dates", e.getMessage());
		}
		
		try{
			parsedInfo = Parser.parseCommand("add from 6pm from 7pm", Common.COMMAND_TYPES.ADD, testModel, 0);
			testCommand = new AddCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test ADD command", "Invalid Command: Multiple Dates", e.getMessage());
		}
		
		try{
			parsedInfo = Parser.parseCommand("add multiple asterisks * *", Common.COMMAND_TYPES.ADD, testModel, 0);
			testCommand = new AddCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test ADD command", "Invalid Command: multiple important marks(*).", e.getMessage());
		}
		
		try{
			parsedInfo = Parser.parseCommand("add multiple hastags # #", Common.COMMAND_TYPES.ADD, testModel, 0);
			testCommand = new AddCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test ADD command", "Invalid Command: multiple hash tags(#).", e.getMessage());
		}
		
		try{
			parsedInfo = Parser.parseCommand("add end time before start time from 6pm to 5pm", Common.COMMAND_TYPES.ADD, testModel, 0);
			testCommand = new AddCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test ADD command", "Invalid date range as start date is after end date.", e.getMessage());
		}
		
		try{
			parsedInfo = Parser.parseCommand("add end time before current time by 9/11", Common.COMMAND_TYPES.ADD, testModel, 0);
			testCommand = new AddCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test ADD command", "Invalid date as end time is before the current time", e.getMessage());
		}
		
		try{
			parsedInfo = Parser.parseCommand("add recurring task from 2/11 weekly", Common.COMMAND_TYPES.ADD, testModel, 0);
			testCommand = new AddCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test ADD command", "There must be both start and end dates for repetitive task.", e.getMessage());
		}
		
		try{
			parsedInfo = Parser.parseCommand("add recurring task from 2/11 to 13/11 weekly", Common.COMMAND_TYPES.ADD, testModel, 0);
			testCommand = new AddCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test ADD command","The difference is larger than the limit of repetitive period.", e.getMessage());
		}
	}
	
	
	public void testMarkCommand(){
		parsedInfo = Parser.parseCommand("mark 1 2", Common.COMMAND_TYPES.MARK, testModel, 0);
		testCommand = new MarkCommand(parsedInfo, testModel, 0);
		assertEquals("Test MARK command", "Indicated task(s) has/have been marked successfully.",testCommand.execute());
		
		try{
			parsedInfo = Parser.parseCommand("mark 1 6", Common.COMMAND_TYPES.MARK, testModel, 0);
			testCommand = new MarkCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test MARK command","There is an index outside the range of the list.", e.getMessage());
		}
		
		try{
			parsedInfo = Parser.parseCommand("mark 3-1", Common.COMMAND_TYPES.MARK, testModel, 0);
			testCommand = new MarkCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test MARK command","Invalid range as end point is smaller than start point", e.getMessage());
		}
	}
	
	public void testUnmarkCommand(){
		parsedInfo = Parser.parseCommand("unmark 1", Common.COMMAND_TYPES.UNMARK, testModel, 0);
		testCommand = new UnmarkCommand(parsedInfo, testModel, 0);
		assertEquals("Test UNMARK command", "Indicated task(s) has/have been unmarked successfully.",testCommand.execute());
		
		try{
			parsedInfo = Parser.parseCommand("unmark 3 3", Common.COMMAND_TYPES.UNMARK, testModel, 0);
			testCommand = new UnmarkCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test UNMARK command","There are duplicate indexes!", e.getMessage());
		}
	}
	
	public void testEditCommand() {
		try{
			parsedInfo = Parser.parseCommand("edit 1 from 7pm to 9pm", Common.COMMAND_TYPES.EDIT, testModel, 0);
			testCommand = new EditCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test EDIT command", "Indicated task has been edited successfully.", e.getMessage());
		}
		
		try{
			parsedInfo = Parser.parseCommand("edit 3 #cs2103 weekly", Common.COMMAND_TYPES.EDIT, testModel, 0);
			testCommand = new EditCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test EDIT command", "Indicated task has been edited successfully.", e.getMessage());
		}
		
		try{
			parsedInfo = Parser.parseCommand("edit no index", Common.COMMAND_TYPES.EDIT, testModel, 0);
			testCommand = new EditCommand(parsedInfo, testModel, 0);
			testCommand.execute();
		} catch(IllegalArgumentException e){
			assertEquals("Test EDIT command", "Invalid index", e.getMessage());
		}
	}
}
