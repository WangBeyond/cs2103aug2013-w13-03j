
import static org.junit.Assert.assertEquals;

import org.junit.Test;

//@author A0100927M
/**
 * 
 * HistoryTest class: This class tests the functionality of History class.
 * 
 */
public class TestHistory {

	@Test
	public void test() {
		History testHistory = new History();
		
		// boundary case where the user's first command is undo (no commands before that)
		assertEquals(false, testHistory.isUndoable());
		
		// boundary case where the user's first command is redo (no commands before that)
		assertEquals(false, testHistory.isRedoable());
		
		// boundary case where the user's first command is redo (no commands before that)
		assertEquals(false, testHistory.isAfterSearch());
		
		// valid case where a command was executed
		String[] addInfo = new String[] {"task", "", "", "", "", ""};
		AddCommand add = new AddCommand(addInfo, new Model(), 1);
		testHistory.updateCommand(add);
		assertEquals(true, testHistory.isUndoable());
		assertEquals(false, testHistory.isRedoable());
		
		// valid case where an undo command was executed
		testHistory.getPrevCommandForUndo();
		assertEquals(true, testHistory.isRedoable());
		
		// valid case where there is more than one command
		testHistory.getPrevCommandForRedo();
		testHistory.updateCommand(add, true);
		testHistory.getPrevCommandForUndo();
		assertEquals(true, testHistory.isUndoable());
		assertEquals(true, testHistory.isRedoable());	
		assertEquals(true, testHistory.isAfterSearch());	
	}

}
