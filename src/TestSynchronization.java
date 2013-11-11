import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.gdata.util.AuthenticationException;

//@author A0105523U
public class TestSynchronization {

	@Test
	public void test() {
		//history object
		History history = new History();
		//model object
		Model model = new Model();
	
		Synchronization sync = new Synchronization(model, history);
		
		//set a valid username and password
		sync.setUsernameAndPassword("jimcs2103iDo@gmail.com", "cs2103cs2101");
		try {
			sync.initService();
		} catch (AuthenticationException e) {
			assert(false);
		}
		
		//test case for empty tasks
		assertEquals(Common.MESSAGE_SYNC_SUCCESSFUL, sync.execute());
		
		//test case for timed tasks
		Task task = new Task();
		task.setWorkInfo("timed task");
		task.setStartDate(new CustomDate("6pm"));
		task.setEndDate(new CustomDate("7pm"));
		model.addTaskToPending(task);
		
		assertEquals(Common.MESSAGE_SYNC_SUCCESSFUL, sync.execute());
		
		//test case for floating tasks
		Task task1 = new Task();
		task1.setWorkInfo("floating task");
		model.getPendingList().clear();
		model.addTaskToPending(task1);
		
		assertEquals(Common.MESSAGE_SYNC_SUCCESSFUL, sync.execute());
		
		//test case for recurring tasks
		Task task2 = new Task();
		task2.setWorkInfo("timed task");
		task2.setStartDate(new CustomDate("4pm"));
		task2.setEndDate(new CustomDate("6pm"));
		task2.setTag(new Tag(Common.HYPHEN, "weekly"));
		model.getPendingList().clear();
		model.addTaskToPending(task2);
		
		assertEquals(Common.MESSAGE_SYNC_SUCCESSFUL, sync.execute());
	}

}
