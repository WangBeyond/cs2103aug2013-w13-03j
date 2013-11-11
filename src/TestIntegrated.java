import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import static org.junit.Assert.fail;
import org.junit.Test;

//@author A0105667B
public class TestIntegrated  {
	
	static Control controlTest;
	static Storage taskFile;
	static Storage settingFile;
	static Model model;

	@BeforeClass
	public static void testSetup() {
		controlTest = new Control();
		Common.changeTaskFile("testTaskFile.xml");
		Common.changeSettingsFile("testSettingStorage.xml");
		controlTest.loadData();
		taskFile = controlTest.getTaskFile();
		settingFile = controlTest.getSettingsFile();
		model = controlTest.getModel();
		model.setAutoSync(false);
		try {
			settingFile.storeToFile();
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		
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
		test_Complete_REMOVE_RECOVER();
	}
	
	public void testClear() {
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("clear");
		try {
			assertTrue("Task not removed successful", taskFile.checkTaskListEmptyForTest(TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.setTabForTest(Common.COMPLETE_TAB);
		controlTest.executeCommand("clear");
		try {
			assertTrue("Task not removed successful", taskFile.checkTaskListEmptyForTest(TaskStorage.COMPLETE));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.setTabForTest(Common.TRASH_TAB);
		controlTest.executeCommand("clear");
		try {
			assertTrue("Task not removed successful", taskFile.checkTaskListEmptyForTest(TaskStorage.TRASH));
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
			assertTrue("Task not added successful",taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("undo");
		try {
			assertTrue("Task not add-undo successful", taskFile.checkTaskListEmptyForTest(TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("redo");
		try {
			assertTrue("Task not add-redo successful",taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	public void testRemove() {
		clearAll();
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("add watch football game from 5pm to 7pm *");
		Task newTask = new Task();
		newTask.setWorkInfo("watch football game");
		newTask.setStartDate(new CustomDate("5pm"));
		newTask.setEndDate(new CustomDate("7pm"));
		newTask.setIsImportant(true);
		controlTest.executeCommand("remove 1");
		try {
			assertTrue("Task not removed from pending successfully", taskFile.checkTaskListEmptyForTest(TaskStorage.PENDING));
			assertTrue("Task not moved to trash successfully", taskFile.searchTaskInFileForTest(newTask, TaskStorage.TRASH));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("undo");
		try {
			assertTrue("Task not remove-undo successfully",taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
			assertTrue("Task not removed from trash successfully", taskFile.checkTaskListEmptyForTest(TaskStorage.TRASH));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("redo");
		try {
			assertTrue("Task not remove-undo successfully", taskFile.checkTaskListEmptyForTest(TaskStorage.PENDING));
			assertTrue("Task not moved to trash successfully", taskFile.searchTaskInFileForTest(newTask, TaskStorage.TRASH));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.setTabForTest(Common.TRASH_TAB);
		controlTest.executeCommand("remove 1");
		try {
			assertTrue("Task not removed from trash successfully", taskFile.checkTaskListEmptyForTest(TaskStorage.TRASH));
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
			assertTrue("Task not edited successfully 1",taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("undo");
		newTask.setStartDate(new CustomDate("5pm"));
		newTask.setEndDate(new CustomDate("7pm"));
		newTask.setIsImportant(false);
		try {
			assertTrue("Task not edit-undo successfully",taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("redo");
		newTask.setStartDate(new CustomDate("6pm"));
		newTask.setEndDate(new CustomDate("11pm"));
		newTask.setIsImportant(true);
		try {
			assertTrue("Task not edit-redo successfully",taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		//More edit case
		controlTest.executeCommand("edit 1 #UEFAchampion");
		newTask.setTag(new Tag("#UEFAchampion",Common.NULL));
		try {
			assertTrue("Task not edited successfully 2",taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
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
			assertTrue("Task not marked successfully", taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		newTask.setIsImportant(false);
		controlTest.executeCommand("undo");
		try {
			assertTrue("Task not mark-undo successfully", taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}	
		newTask.setIsImportant(true);
		controlTest.executeCommand("redo");
		try {
			assertTrue("Task not mark-redo successfully", taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("unmark 1");
		newTask.setIsImportant(false);
		try {
			assertTrue("Task not unmarked successfully", taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}	
	}
	
	public void testComplete() {
		clearAll();
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("add do project #Computing");
		controlTest.executeCommand("add watch football game from 5pm to 7pm");
		controlTest.executeCommand("complete 1 2");
		Task newTask1 = new Task();
		newTask1.setWorkInfo("do project");
		newTask1.setTag(new Tag("#Computing", Common.NULL));
		Task newTask2 = new Task();
		newTask2.setWorkInfo("watch football game");
		newTask2.setStartDate(new CustomDate("5pm"));
		newTask2.setEndDate(new CustomDate("7pm"));
		try {
			assertTrue("Task not removed from pending",!taskFile.searchTaskInFileForTest(newTask1, TaskStorage.PENDING));
			assertTrue("Task not moved to complete",taskFile.searchTaskInFileForTest(newTask1, TaskStorage.COMPLETE));
			assertTrue("Task not removed from pending",!taskFile.searchTaskInFileForTest(newTask2, TaskStorage.PENDING));
			assertTrue("Task not moved to complete",taskFile.searchTaskInFileForTest(newTask2, TaskStorage.COMPLETE));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("undo");
		try {
			assertTrue("Task not remvoed from complete",!taskFile.searchTaskInFileForTest(newTask1, TaskStorage.COMPLETE));
			assertTrue("Task not moved to pending",taskFile.searchTaskInFileForTest(newTask1, TaskStorage.PENDING));
			assertTrue("Task not remvoed from complete",!taskFile.searchTaskInFileForTest(newTask2, TaskStorage.COMPLETE));
			assertTrue("Task not moved to pending",taskFile.searchTaskInFileForTest(newTask2, TaskStorage.PENDING));
			
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("redo");
		try {
			assertTrue("Task not removed from pending",!taskFile.searchTaskInFileForTest(newTask1, TaskStorage.PENDING));
			assertTrue("Task not moved to complete",taskFile.searchTaskInFileForTest(newTask1, TaskStorage.COMPLETE));
			assertTrue("Task not removed from pending",!taskFile.searchTaskInFileForTest(newTask2, TaskStorage.PENDING));
			assertTrue("Task not moved to complete",taskFile.searchTaskInFileForTest(newTask2, TaskStorage.COMPLETE));
			
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.setTabForTest(Common.COMPLETE_TAB);
		controlTest.executeCommand("undone 1-2");
		try {
			assertTrue("Task not remvoed from complete",!taskFile.searchTaskInFileForTest(newTask1, TaskStorage.COMPLETE));
			assertTrue("Task not moved to pending",taskFile.searchTaskInFileForTest(newTask1, TaskStorage.PENDING));
			assertTrue("Task not remvoed from complete",!taskFile.searchTaskInFileForTest(newTask2, TaskStorage.COMPLETE));
			assertTrue("Task not moved to pending",taskFile.searchTaskInFileForTest(newTask2, TaskStorage.PENDING));
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
		newTask.setTag(new Tag("#artCenter",Common.NULL));
		try {
			taskFile.storeToFile();
			assertTrue("Task not recovered to pending successfully", taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
			assertTrue("Task not recmoved from trash successfully", !taskFile.searchTaskInFileForTest(newTask, TaskStorage.TRASH));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("undo");
		try {
			taskFile.storeToFile();
			assertTrue("Task not recovere-undo successfully", !taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
			assertTrue("Task not moved to trash again", taskFile.searchTaskInFileForTest(newTask, TaskStorage.TRASH));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("redo");
		try {
			taskFile.storeToFile();
			assertTrue("Task not recover-redo to pending successfully", taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
			assertTrue("Task not recmoved from trash successfully", !taskFile.searchTaskInFileForTest(newTask, TaskStorage.TRASH));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	public void test_Complete_REMOVE_RECOVER() {
		clearAll();
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("add go to music concert #artCenter");
		controlTest.executeCommand("complete 1");
		Task newTask = new Task();
		newTask.setWorkInfo("go to music concert");
		newTask.setTag(new Tag("#artCenter",Common.NULL));
		try {
			assertTrue("Task not removed from pending",!taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
			assertTrue("Task not completed to complete",taskFile.searchTaskInFileForTest(newTask, TaskStorage.COMPLETE));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.setTabForTest(Common.COMPLETE_TAB);
		controlTest.executeCommand("remove 1");
		try {
			assertTrue("Task not removed successful", taskFile.checkTaskListEmptyForTest(TaskStorage.COMPLETE));
			assertTrue("Task not moved to trash",taskFile.searchTaskInFileForTest(newTask, TaskStorage.TRASH));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.setTabForTest(Common.TRASH_TAB);
		controlTest.executeCommand("recover 1");
		try {
			assertTrue("Task not recovered from trash successful", taskFile.checkTaskListEmptyForTest(TaskStorage.TRASH));
			assertTrue("Task not recovered from trash to pending",taskFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
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
