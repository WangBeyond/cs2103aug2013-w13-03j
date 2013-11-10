import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import static org.junit.Assert.fail;

import org.junit.Test;

public class IntegratedTest  {
	
	static Control controlTest;
	static Storage dataFile;

	@BeforeClass
	public static void testSetup() {
		controlTest = new Control();
		Common.changeTaskFile("testTaskFile.xml");
		controlTest.loadData();
		dataFile = controlTest.getTaskFile();
	}
	
	@Test
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
	
	@Test
	public void testAdd() {
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("clear");
		controlTest.executeCommand("add watch football game from 5pm to 7pm");
		Task newTask = new Task();
		newTask.setWorkInfo("watch football game");
		newTask.setStartDate(new CustomDate("5pm"));
		newTask.setEndDate(new CustomDate("7pm"));
		try {
			assertTrue("Task not stored successful",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	@Test
	public void testRemove() {
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("clear");
		controlTest.executeCommand("add watch football game from 5pm to 7pm");
		controlTest.executeCommand("remove 1");
		Task newTask = new Task();
		newTask.setWorkInfo("watch football game");
		newTask.setStartDate(new CustomDate("5pm"));
		newTask.setEndDate(new CustomDate("7pm"));
		try {
			assertTrue("Task not removed successful", !dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	@Test 
	public void testEdit() {
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("clear");
		controlTest.executeCommand("add watch football game from 5pm to 7pm");
		controlTest.executeCommand("edit 1 from 6pm to 11pm *");
		Task newTask = new Task();
		newTask.setWorkInfo("watch football game");
		newTask.setStartDate(new CustomDate("6pm"));
		newTask.setEndDate(new CustomDate("11pm"));
		newTask.setIsImportant(true);
		try {
			assertTrue("Task not stored successful",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	@Test 
	public void testMark() {
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("clear");
		controlTest.executeCommand("add watch football game from 5pm to 7pm");
		controlTest.executeCommand("edit 1 from 6pm to 11pm *");
		Task newTask = new Task();
		newTask.setWorkInfo("watch football game");
		newTask.setStartDate(new CustomDate("6pm"));
		newTask.setEndDate(new CustomDate("11pm"));
		newTask.setIsImportant(true);
		try {
			assertTrue("Task not stored successful",dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	@Test 
	public void testComplete() {
		controlTest.setTabForTest(Common.PENDING_TAB);
		controlTest.executeCommand("clear");
		controlTest.executeCommand("add watch football game from 5pm to 7pm");
		controlTest.executeCommand("complete 1");
		Task newTask = new Task();
		newTask.setWorkInfo("watch football game");
		newTask.setStartDate(new CustomDate("5pm"));
		newTask.setEndDate(new CustomDate("7pm"));
		try {
			assertTrue("Task not stored successful",!dataFile.searchTaskInFileForTest(newTask, TaskStorage.PENDING));
			assertTrue("Task not stored successful",dataFile.searchTaskInFileForTest(newTask, TaskStorage.COMPLETE));
		} catch(Exception e){
			fail("Some exception thrown "+e.getMessage());
		}
	}
	


}
