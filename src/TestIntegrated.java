import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;

import static org.junit.Assert.fail;

import org.junit.Test;


public class TestIntegrated  {
	
	static Control controlTest;
	static Storage dataFile;
	static Model model;

	@BeforeClass
	public static void testSetup() {
		controlTest = new Control();
		Common.changeTaskFile("testTaskFile.xml");
		controlTest.loadData();
		dataFile = controlTest.getTaskFile();
		model = controlTest.getModel();
	}
	
	@Test
	public void test(){
		testClear();
		testAdd();
		testRemove();
		testEdit();
		testMark();
		testComplete();
		testRecover();
		
	}
	
	public void testClear() {
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("clear");
		try {
			assertTrue("Task not removed successful", dataFile.checkTaskListEmptyForTest("pending"));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.setTabForTest(Common.COMPLETE_TAB);
		controlTest.executeCommand("clear");
		try {
			assertTrue("Task not removed successful", dataFile.checkTaskListEmptyForTest("complete"));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.setTabForTest(Common.TRASH_TAB);
		controlTest.executeCommand("clear");
		try {
			assertTrue("Task not removed successful", dataFile.checkTaskListEmptyForTest("trash"));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
	}
	

	public void testAdd() {
		clearAll();
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("add go to gym from 9pm to 10pm every 3 days 3 times");
		Task newTask = new Task();
		newTask.setWorkInfo("go to gym");
		newTask.setStartDate(new CustomDate("9pm"));
		newTask.setEndDate(new CustomDate("10pm"));
		newTask.setTag(new Tag(Common.HYPHEN,"every3days"));
		newTask.setNumOccurrences(3);
		newTask.setCurrentOccurrence(1);
		try {
			assertTrue("Task not stored successful",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("undo");
		try {
			assertTrue("Task not removed successful", dataFile.checkTaskListEmptyForTest("pending"));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("redo");
		try {
			assertTrue("Task not stored successful",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	public void testRemove() {
		clearAll();
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("add watch football game from 5pm to 7pm");
		Task newTask = new Task();
		newTask.setWorkInfo("watch football game");
		newTask.setStartDate(new CustomDate("5pm"));
		newTask.setEndDate(new CustomDate("7pm"));
		controlTest.executeCommand("remove 1");
		try {
			assertTrue("Task not removed successful", dataFile.checkTaskListEmptyForTest("pending"));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("undo");
		try {
			assertTrue("Task not stored successful",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("redo");
		try {
			assertTrue("Task not removed successful", dataFile.checkTaskListEmptyForTest("pending"));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	public void testEdit() {
		clearAll();
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("add watch football game from 5pm to 7pm");
		controlTest.executeCommand("edit 1 from 6pm to 11pm *");
		Task newTask = new Task();
		newTask.setWorkInfo("watch football game");
		newTask.setStartDate(new CustomDate("6pm"));
		newTask.setEndDate(new CustomDate("11pm"));
		newTask.setIsImportant(true);
		try {
			assertTrue("Task not edited successfully",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("undo");
		newTask.setStartDate(new CustomDate("5pm"));
		newTask.setEndDate(new CustomDate("7pm"));
		newTask.setIsImportant(false);
		try {
			assertTrue("Task not edit-undo successfully",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("redo");
		newTask.setStartDate(new CustomDate("6pm"));
		newTask.setEndDate(new CustomDate("11pm"));
		newTask.setIsImportant(true);
		try {
			assertTrue("Task not edit-redo successfully",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	public void testMark() {
		clearAll();
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("add prepare to final exam from monday 10am to sunday 10pm");
		controlTest.executeCommand("mark 1");
		Task newTask = new Task();
		newTask.setWorkInfo("prepare to final exam");
		newTask.setStartDate(new CustomDate("monday 10am"));
		newTask.setEndDate(new CustomDate("sunday 10pm"));
		newTask.setIsImportant(true);
		try {
			assertTrue("Task not stored successful",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("unmark 1");
		newTask.setIsImportant(false);
		try {
			assertTrue("Task not stored successful",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}	
	}
	
	public void testComplete() {
		clearAll();
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("add do project #Computing");
		controlTest.executeCommand("complete 1");
		Task newTask = new Task();
		newTask.setWorkInfo("do project");
		newTask.setTag(new Tag("#Computing", Common.NULL));
		try {
			assertTrue("Task not removed from pending",!dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
			assertTrue("Task not moved to complete",dataFile.searchTaskInFileForTest(newTask, TaskStorage.COMPLETE));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("undo");
		try {
			assertTrue("Task not remvoed from complete",!dataFile.searchTaskInFileForTest(newTask, TaskStorage.COMPLETE));
			assertTrue("Task not moved to pending",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("redo");
		try {
			assertTrue("Task not removed from pending",!dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
			assertTrue("Task not moved to complete",dataFile.searchTaskInFileForTest(newTask, TaskStorage.COMPLETE));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.setTabForTest(Common.COMPLETE_TAB);
		controlTest.executeCommand("undone 1");
		try {
			assertTrue("Task not remvoed from complete",!dataFile.searchTaskInFileForTest(newTask, TaskStorage.COMPLETE));
			assertTrue("Task not moved to pending",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}

	}
	
	public void testRecover() {
		clearAll();
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("add go to music concert #artCenter");
		controlTest.executeCommand("remove 1");
		controlTest.setTabForTest(Common.TRASH_TAB);
		controlTest.executeCommand("recover 1");
		
		Task newTask = new Task();
		newTask.setWorkInfo("go to music concert");
		newTask.setTag(new Tag("#artCenter","null"));
		model.addTaskToPending(newTask);
		try {
			dataFile.storeToFile();
			assertTrue("Task not recovered to pending successfully", dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
			assertTrue("Task not recmoved from trash successfully", !dataFile.searchTaskInFileForTest(newTask, TaskStorage.TRASH));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	private void clearAll() {
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("clear");
		controlTest.setTabForTest(Common.COMPLETE_TAB);
		controlTest.executeCommand("clear");
		controlTest.setTabForTest(Common.TRASH_TAB);
		controlTest.executeCommand("clear");
	}
	
}
