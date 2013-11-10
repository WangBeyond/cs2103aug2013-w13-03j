
import org.junit.Test;


public class TestMain {
	HistoryTest historyTest = new HistoryTest();
	TaskTest taskTest = new TaskTest();
	CustomDateTest customDateTest = new CustomDateTest();
	ParserTest parserTest = new ParserTest();
	CommandTest commandTest = new CommandTest();
	
	
	@Test
	public void test() {
		historyTest.test();
		taskTest.test();
		customDateTest.test();
		parserTest.test();
		commandTest.test();
	}

}
