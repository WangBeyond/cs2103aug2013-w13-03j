import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

//@author A0105667B
public class TestSettingsStorage {
	static Control controlTest;
	static Storage settingStore;
	static Model model;

	@BeforeClass
	public static void testSetup() {
		controlTest = new Control();
		//Common.changeTaskFile("testTaskFile.xml");
		controlTest.loadData();
		settingStore = controlTest.getSettingsFile();
		model = controlTest.getModel();
	}
	
	@Test
	public void test() {
		model.setColourScheme(Common.BRIGHT);
		model.setAutoSync(false);
		try {
			settingStore.storeToFile();
			assertTrue("Settings are not stored successfully", settingStore.compareModelAndFileForTest());
		} catch(Exception e) {
			fail("Some exception thrown "+e.getMessage());
		}
		
		model.setColourScheme(Common.GOLDFISH);
		model.setAutoSync(true);
		try {
			settingStore.storeToFile();
			assertTrue("Settings are not stored successfully", settingStore.compareModelAndFileForTest());
		} catch(Exception e) {
			fail("Some exception thrown "+e.getMessage());
		}
	}

}
