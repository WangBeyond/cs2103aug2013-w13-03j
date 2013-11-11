
import static org.junit.Assert.assertEquals;

import org.junit.Test;

//@author A0098077N
// This class contains test cases for class CustomDate
public class TestCustomDate {
	CustomDate currentTime;
	CustomDate testTime;
	
	@Test
	public void test() {
		currentTime = new CustomDate();
		
		testTime = new CustomDate();
		testTime.convert("21 Oct 12:30");
		assertEquals("CustomDate with normal valid date info", "21/10/2013 12:30", CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("21/12 13:29");
		assertEquals("CustomDate with date in slash format", "21/12/2013 13:29", CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("3/1/2014 6:47");
		assertEquals("CustomDate with date in next year", "3/1/2014 6:47", CustomDate.convertString(testTime));

		testTime = new CustomDate();
		testTime.convert("8-6-2013");
		assertEquals("CustomDate with date in dash format without indicating time","8/6/2013 0:0", CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("15/10/2013 630am");
		assertEquals("CustomDate with date in slash format and time in integer format", "15/10/2013 6:30", CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("32 October");
		assertEquals("CustomDate with invalid date", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("21/13");
		assertEquals("CustomDate with invalid month", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("21/-1");
		assertEquals("CustomDate with invalid negative month", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("-32 October");
		assertEquals("CustomDate with invalid negative date", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("21 October 25:30");
		assertEquals("CustomDate with invalid hour", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("32 October 4:80");
		assertEquals("CustomDate with invalid minute", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("21/12/2013/5");
		assertEquals("CustomDate with excessive slashses", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("32 October 12305");
		assertEquals("CustomDate with invalid hour in integer format", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("32 October -1:59");
		assertEquals("CustomDate with invalid negative hour", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("21/5/-2013 4:80");
		assertEquals("CustomDate with invalid negative year", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("25 Feb 4:-34");
		assertEquals("CustomDate with invalid negative minute", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("3 Maru 4:50");
		assertEquals("CustomDate with invalid month due to typo", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
		
		testTime = new CustomDate();
		testTime.convert("35/15 34:80");
		assertEquals("CustomDate with more than 1 invalid fields", CustomDate.convertString(currentTime), CustomDate.convertString(testTime));
	}

}
