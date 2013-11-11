import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import org.junit.BeforeClass;
import org.junit.Test;

//@author A0105667B
public class TestTaskStorage {

	static Control controlTest;
	static Storage dataFile;

	@BeforeClass
	public static void testSetup() {
		controlTest = new Control();
		//Common.changeTaskFile("testTaskFile.xml");
		controlTest.loadData();
		dataFile = controlTest.getTaskFile();
	}
	
	@Test
	public void test() {
		controlTest.executeCommand("add watch football game from 5pm to 7pm");
		try {
			dataFile.storeToFile();
			assertTrue("Model are not stored successfully", dataFile.compareModelAndFileForTest());
		} catch(Exception e) {
			fail("Some exception thrown "+e.getMessage());
		}
		
		controlTest.executeCommand("remove 3");
		try {
			dataFile.storeToFile();
			assertTrue("Model are not stored successfully", dataFile.compareModelAndFileForTest());
		} catch(Exception e) {
			fail("Some exception thrown "+e.getMessage());
		}
		controlTest.executeCommand("undo");
		controlTest.executeCommand("undo");
	}
	

}
