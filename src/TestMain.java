import org.junit.Test;

// This test file is used to test all available test cases in this application
//@author A0098077N
public class TestMain {
	TestHistory historyTest = new TestHistory();
	TestTask taskTest = new TestTask();
	TestCustomDate customDateTest = new TestCustomDate();
	TestParser parserTest = new TestParser();
	TestCommand commandTest = new TestCommand();
	TestTaskStorage taskStorageTest = new TestTaskStorage();
	TestSettingsStorage settingsStorageTest = new TestSettingsStorage();
	TestEncryptor encryptorTest = new TestEncryptor();
	TestIntegrated integratedTest = new TestIntegrated();
	TestSynchronization synchronizationTest = new TestSynchronization();
		
	@Test
	public void test() {
		// Logic test
		historyTest.test();
		taskTest.test();
		customDateTest.test();
		parserTest.test();
		commandTest.test();
		
		// Storage test
		TestTaskStorage.testSetup();
		taskStorageTest.test();
		TestSettingsStorage.testSetup();
		settingsStorageTest.test();
		TestEncryptor.testSetup();
		encryptorTest.test();
		
		// Integrated testing
		TestIntegrated.testSetup();
		integratedTest.test();
		
		synchronizationTest.test();
	}

}
