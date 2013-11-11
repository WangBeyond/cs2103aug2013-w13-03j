
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

//@author A0098077N
public class TestTask {
	Task testTask;
	
	@Test
	public void test() {
		testTask = new Task();
		// Check number of occurrences
		assertEquals("Compare initial occurrence", 1, testTask.getNumOccurrences());
		
		// Check the current occurrence
		assertEquals("Compare the current occurrence", 1, testTask.getCurrentOccurrence());
		
		// Compare the initial task info
		assertEquals("Compare the initial task info", "", testTask.getWorkInfo());
		
		// Update the task info
		testTask.setWorkInfo("Using JUnit test");
		assertEquals("Compare the updated task info", "Using JUnit test", testTask.getWorkInfo());
		
		// Update the start date
		assertNull("Check the initial start date", testTask.getStartDate());
		testTask.setStartDate(new CustomDate("21/10/2013"));
		assertEquals("Compare the updated start date", "21/10/2013 0:0", CustomDate.convertString(testTask.getStartDate()));
		
		// Update the end date
		assertNull("Check the initial end date", testTask.getEndDate());
		testTask.setEndDate(new CustomDate("5/12/2013 12:30"));
		assertEquals("Compare the updated end date", "5/12/2013 12:30" , CustomDate.convertString(testTask.getEndDate()));
		
		// Compare the start date string
		assertEquals("Compare the string of start date", "21 Oct", testTask.getStartDateString());
		// Compare the end date string
		assertEquals("Compare the string of end date", "5 Dec\n 12:30", testTask.getEndDateString());
		
		// Update the tag
		assertEquals("Compare the tag", "-", testTask.getTag().getTag());
		testTask.setTag(new Tag("JUnitTest", "null"));
		assertEquals("Compare the updated tag", "JUnitTest", testTask.getTag().getTag());
		
		// Update the tag and the type of repetition
		testTask.setTag(new Tag("iDo", "weekly"));
		assertEquals("Compare the updated tag", "iDo", testTask.getTag().getTag());
		assertEquals("Compare the updated type of repetition","weekly", testTask.getTag().getRepetition());
		
		// Update only the type of repetition
		testTask.getTag().setRepetition("every3weeks");
		assertEquals("Compare the updated tag", "iDo", testTask.getTag().getTag());
		assertEquals("Compare the updated type of repetition","every3weeks", testTask.getTag().getRepetition());
		
		// Compare the interval
		assertEquals("Compare the interval from the updated type of repetition", 3, testTask.getTag().getInterval());
		
		// Check indicator for overdue task
		assertFalse("Check if it is pending task", testTask.isOverdueTask());
		testTask.setEndDate(new CustomDate("5/11/2013 13:10"));
		assertEquals("Compare the updated end date", "5/11/2013 13:10", CustomDate.convertString(testTask.getEndDate()));
		assertTrue("Check if it becomes overdue task", testTask.isOverdueTask());
		
		// Check indicator for floating task
		assertFalse("Check if it is a timed task", testTask.isFloatingTask());
		testTask.setStartDate(null);
		testTask.setEndDate(null);
		assertTrue("Check if it is a floating task", testTask.isFloatingTask());
		
		// Check indicator for important task
		assertFalse("Check if it is a normal task", testTask.isImportantTask());
		testTask.setIsImportant(true);
		assertTrue("Check if it is an important task", testTask.isImportantTask());
		
		// Check indicator for recurring task
		assertTrue("Check if it is a recurring task", testTask.isRecurringTask());
		testTask.getTag().setRepetition("null");
		assertFalse("Check if it is a timed task", testTask.isRecurringTask());
		
		// Compare status
		assertEquals("Check the initial status", Task.Status.NEWLY_ADDED, testTask.getStatus());
		testTask.setStatus(Task.Status.DELETED);
		assertEquals("Check the updated status", Task.Status.DELETED, testTask.getStatus());
		testTask.setStatus(Task.Status.UNCHANGED);
		assertEquals("Check the updated status", Task.Status.UNCHANGED, testTask.getStatus());
	}	
}
