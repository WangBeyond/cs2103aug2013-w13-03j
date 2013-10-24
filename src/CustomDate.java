import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.google.gdata.data.DateTime;

public class CustomDate {
	final public static int VALID = 1;
	final public static int INVALID = -1;
	final public static int OUT_OF_BOUNDS = 0;

	final public static int MAXIMUM_LENGTH_OF_DATE_INFO = 4;
	final public static int MAXIMUM_LENGTH_FOR_DATE_PART = 2;
	final public static int MAXIMUM_LENGTH_FOR_TIME_PART = 2;
	final public static int EMPTY_DATE_INFO = 0;
	final public static int SUNDAY = 8;
	final public static int MINUTE_IN_MILLIS = 60 * 1000;
	final public static long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
	final public static long SIX_HOURS_IN_MILLIS = 6 * 60 * 60 * 1000;
	final public static long WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000;
	final public static long MONTH_IN_MILLIS = 30 * 24 * 60 * 60 * 1000;
	final public static long YEAR_IN_MILLIS = 365 * 24 * 60 * 60 * 1000;
	final public static long FIRST_TWELVE_HOURS = 12 * 60;
	final public static int HOUR_IN_MINUTES = 60;
	
	final public static int LARGER = 1;
	final public static int SMALLER = -1;
	final public static int EQUAL = 0;

	// The target date of CustomDate object
	private GregorianCalendar targetDate;
	// The date info of this target date
	private String dateInfo;
	boolean hasIndicatedDate;

	// The common current date among CustomDate object
	private static GregorianCalendar currentDate;

	// Constructors
	public CustomDate() {
		targetDate = new GregorianCalendar();
	}
	
	public CustomDate(DateTime dateTime){
		targetDate = new GregorianCalendar();
		String str = dateTime.toString();
		String date = str.substring(8, 10);
		String month = str.substring(5,7);
		String year = str.substring(0, 4);
		String time = "00:00";
		String second = "00";
		if(str.length()>10){
			time = str.substring(11, 16);
			second = str.substring(17, 19);			
		}
		String customDateFormat = date+"/"+month+"/"+year+ " " + time;
		convert(customDateFormat);
		setSecond(Integer.parseInt(second));
	}
	
	public CustomDate(String s) {
		targetDate = new GregorianCalendar();
		convert(s);
	}

	/*************** Get functions to get corresponding elements in the CustomDate *****************/
	public int getYear() {
		return targetDate.get(Calendar.YEAR);
	}

	public int getMonth() {
		return targetDate.get(Calendar.MONTH);
	}

	public int getDate() {
		return targetDate.get(Calendar.DATE);
	}

	public int getHour() {
		return targetDate.get(Calendar.HOUR_OF_DAY);
	}

	public int getMinute() {
		return targetDate.get(Calendar.MINUTE);
	}
	
	public int getSecond(){
		return targetDate.get(Calendar.SECOND);
	}
	
	public boolean hasIndicatedDate(){
		return hasIndicatedDate;
	}

	/************************** Set methods to set hour and minute **************/
	public void setHour(int hour) {
		targetDate.set(Calendar.HOUR_OF_DAY, hour);
	}

	public void setMinute(int minute) {
		targetDate.set(Calendar.MINUTE, minute);
	}
	
	public void setDate(int date){
		targetDate.set(Calendar.DATE, date);
	}
	
	public void setMonth(int month){
		targetDate.set(Calendar.MONTH, month);
	}
	
	public void setYear(int year){
		targetDate.set(Calendar.YEAR, year);
	}
	
	public void setSecond(int second){
		targetDate.set(Calendar.SECOND, second);
	}
	

	/**************** Get the current time in milliseconds ********/
	public long getTimeInMillis() {
		return targetDate.getTimeInMillis();
	}

	/************** Set the current time from given time in milliseconds **************/
	public void setTimeInMillis(long millis) {
		targetDate.setTimeInMillis(millis);
	}

	/**
	 * Convert the CustomDate object into DateTime object from Google Library
	 * 
	 * @return the DateTime object
	 */
	public DateTime returnInDateTimeFormat() {
		return new DateTime(targetDate.getTime(), TimeZone.getTimeZone("Asia/Singapore"));
	}
	
	public static CustomDate convertFromRecurringDateString(String recurrenceDateString){
		String year = recurrenceDateString.substring(0, 4);
		String month = recurrenceDateString.substring(4, 6);
		String date = recurrenceDateString.substring(6, 8);
		String hour = recurrenceDateString.substring(9, 11);
		String minute = recurrenceDateString.substring(11, 13);
		return new CustomDate(date+"/" + Integer.parseInt(month) + "/" + year + " " + hour + ":" + minute);
	}
	
	
	public String returnInRecurringFormat(){
		DecimalFormat df = new DecimalFormat("00");
		String year = String.valueOf(getYear());
		String month = df.format(getMonth()+1);
		String date = df.format(getDate());
		String hour = df.format(getHour());
		String minute = df.format(getMinute());
		String second = "00";
		return year + month + date + "T" + hour + minute+second;
	}

	/**
	 * Convert a CustomDate object into String format to store in storage
	 * 
	 * @param v
	 *            the object needed to be converted
	 * @return the String format of this object
	 */
	public static String convertString(CustomDate v) {
		if (v == null)
			return "-";
		GregorianCalendar target = v.targetDate;
		int date = target.get(Calendar.DATE);
		int month = target.get(Calendar.MONTH);
		int year = target.get(Calendar.YEAR);
		int hour = target.get(Calendar.HOUR_OF_DAY);
		int minute = target.get(Calendar.MINUTE);
		return date + "/" + (month + 1) + "/" + year + " " + hour + ":" + minute;
	}

	/**
	 * This function is used to convert this CustomDate object to String format
	 * to display in GUI
	 * 
	 * @param isStartDate
	 *            whether this is a start or end date
	 * @return the required String displayed on GUI
	 */
	public String toString(boolean isStartDate) {
		updateCurrentDate();
		boolean hasTime = hasTime(isStartDate);

		if (!beforeCurrentTime() && lessThan6Hours()) {
			return getRemainingTime();
		}
		if (isTonight()) {
			return "Tonight";
		}

		String result = "";
		result += getDateString();
		result += getTimeString(hasTime, result.equals("Tomorrow"));
		return result;
	}

	private String getDateString() {
		if (isToday()) {
			return "Today";
		} else {
			String str;
			str = targetDate.get(Calendar.DATE) + " " + getMonthString(targetDate.get(Calendar.MONTH));
			if (!isCurrentYear()) {
				str += " " + targetDate.get(Calendar.YEAR);
			}
			return str;
		}
	}

	private String getTimeString(boolean hasTime, boolean isTomorrow) {
		if (hasTime) {
			DecimalFormat df = new DecimalFormat("00");
			return "\n " + (isTomorrow ? "    " : "") + targetDate.get(Calendar.HOUR_OF_DAY) + ":"
					+ df.format(targetDate.get(Calendar.MINUTE));
		}
		return "";
	}

	private boolean isCurrentYear() {
		return targetDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR);
	}

	private String getRemainingTime() {
		int remainingTime = (int) (targetDate.getTimeInMillis() - currentDate.getTimeInMillis()) / MINUTE_IN_MILLIS;
		int remainingHours = remainingTime / HOUR_IN_MINUTES;
		int remainingMinutes = remainingTime % HOUR_IN_MINUTES;
		return remainingHours + "h " + remainingMinutes + "m";
	}

	/**
	 * This function is used to compare 2 CustomDate objects
	 * 
	 * @param date1
	 *            the first date
	 * @param date2
	 *            the second date
	 * @return value indicating the result of comparison
	 */
	public static int compare(CustomDate date1, CustomDate date2) {
		int difference = 0;
		if (date1 == null && date2 == null) {
			return EQUAL;
		} else if (date1 == null && date2 != null) {
			return LARGER;
		} else if (date1 != null && date2 == null) {
			return SMALLER;
		}

		for (int i = 0; i < 6; i++) {
			if (i == 0) {
				difference = date1.getYear() - date2.getYear();
			} else if (i == 1) {
				difference = date1.getMonth() - date2.getMonth();
			} else if (i == 2) {
				difference = date1.getDate() - date2.getDate();
			} else if (i == 3) {
				difference = date1.getHour() - date2.getHour();
			} else if(i == 4) {
				difference = date1.getMinute() - date2.getMinute();
			} else{
				difference = date1.getSecond() - date2.getSecond();
			}
			if (difference != 0) {
				return difference;
			}
		}

		return difference;
	}

	// Compare the 2 dates according to their dates, not specific times.
	public boolean dateEqual(CustomDate other) {
		return this.getYear() == other.getYear()
				&& this.getMonth() == other.getMonth()
				&& this.getDate() == other.getDate();
	}

	public boolean beforeCurrentTime() {
		return (currentDate.getTimeInMillis() - targetDate.getTimeInMillis()) > 0;
	}

	public static void updateCurrentDate() {
		currentDate = new GregorianCalendar();
	}

	/******** Get the difference between periods according to the type of repetition ***********/
	public static long getUpdateDifference(String repetition) {
		if (isDailyRoutine(repetition)) {
			return DAY_IN_MILLIS;
		} else if (isWeeklyRoutine(repetition)) {
			return WEEK_IN_MILLIS;
		} else if (isMonthlyRoutine(repetition)) {
			return MONTH_IN_MILLIS;
		} else if (isYearlyRoutine(repetition)) {
			return YEAR_IN_MILLIS;
		} else {
			return 0;
		}
	}

	private static boolean isDailyRoutine(String repetition) {
		return repetition.equals("daily");
	}

	private static boolean isWeeklyRoutine(String repetition) {
		return repetition.equals("weekly");
	}

	private static boolean isMonthlyRoutine(String repetition) {
		return repetition.equals("monthly");
	}

	private static boolean isYearlyRoutine(String repetition) {
		return repetition.equals("yearly");
	}

	/**
	 * This function is used to set the date from given info of String s
	 * 
	 * @param s
	 *            the given info of the date
	 * @return value indicating the conversion is successful or not
	 */
	public int convert(String s) {
		dateInfo = s.toLowerCase();
		String[] infos = dateInfo.split(" ");

		updateCurrentDate();

		boolean invalidLength = infos.length > MAXIMUM_LENGTH_OF_DATE_INFO
				|| infos.length == EMPTY_DATE_INFO;
		if (invalidLength) {
			return INVALID;
		}

		try {
			int numElements = infos.length;
			GregorianCalendar tempDate = new GregorianCalendar();
			tempDate.setLenient(false);

			numElements = processDate(infos, tempDate, numElements);
			if (numElements == INVALID) {
				return INVALID;
			}
			
			if (numElements != infos.length) {
				hasIndicatedDate = true;
			} else {
				hasIndicatedDate = false;
			}
			
			numElements = processTime(infos, tempDate, numElements);
			if (numElements > 0 || numElements == INVALID) {
				return INVALID;
			}

			targetDate = tempDate;
			return VALID;
		} catch (Exception e) {
			if (e.getMessage().equals("Out of bounds")) {
				return OUT_OF_BOUNDS;
			}
			return INVALID;
		}
	}

	private int processDate(String[] infos, GregorianCalendar tempDate,
			int numElements) {
		if (hasDateFormat()) {
			return updateDate(infos, tempDate, numElements);
		} else if (hasDayFormat()) {
			return updateDay(infos, tempDate, numElements);
		} else if (infos.length > MAXIMUM_LENGTH_FOR_TIME_PART) {
			return INVALID;
		}

		return numElements;
	}

	private int updateDate(String[] infos, GregorianCalendar targetDate,
			int numElements) {
		int startIndex = getStartIndexOfDate(infos);
		assert startIndex >= 0;
		if (hasMonthWord()) {
			numElements = updateDateWithMonth(infos, targetDate, numElements, startIndex);
		} else if (dateInfo.contains("/")) {
			numElements = updateDateWithSlash(infos, targetDate, numElements, startIndex);
		} else {
			numElements = updateDateWithDash(infos, targetDate, numElements, startIndex);
		}

		return numElements;
	}

	private int updateDateWithMonth(String[] infos,
			GregorianCalendar targetDate, int numElements, int startIndex) {
		int date = Integer.parseInt(infos[startIndex]);
		int month = getMonth(infos[startIndex + 1]);

		targetDate.set(Calendar.MONTH, month);
		targetDate.set(Calendar.DATE, date);
		checkDateBound(targetDate);

		return numElements - 2;
	}

	private int updateDateWithSlash(String[] infos,
			GregorianCalendar targetDate, int numElements, int startIndex) {
		String[] numbers = infos[startIndex].split("/");

		boolean invalidLength = numbers.length != 3 && numbers.length != 2;
		if (invalidLength) {
			throw new IllegalArgumentException("Invalid length in slash format");
		}

		int month = getMonth(numbers[1]);
		targetDate.set(Calendar.MONTH, month);

		int date = Integer.parseInt(numbers[0]);
		targetDate.set(Calendar.DATE, date);

		checkDateBound(targetDate);
		int year = (numbers.length == 3) ? Integer.parseInt(numbers[2]) : currentDate.get(Calendar.YEAR);
		targetDate.set(Calendar.YEAR, year);

		return numElements - 1;
	}

	private int updateDateWithDash(String[] infos,
			GregorianCalendar targetDate, int numElements, int startIndex) {
		String[] numbers = infos[0].split("-");

		boolean invalidLength = numbers.length != 3 && numbers.length != 2;
		if (invalidLength)
			throw new IllegalArgumentException("Invalid length in dash format");

		int date = Integer.parseInt(numbers[0]);
		targetDate.set(Calendar.DATE, date);

		int month = getMonth(numbers[1]);
		targetDate.set(Calendar.MONTH, month);

		checkDateBound(targetDate);
		int year = (numbers.length == 3) ? Integer.parseInt(numbers[2]) : currentDate.get(Calendar.YEAR);
		targetDate.set(Calendar.YEAR, year);

		return numElements - 1;
	}

	private void checkDateBound(GregorianCalendar targetDate) {
		try {
			targetDate.get(Calendar.DATE);
			targetDate.get(Calendar.MONTH);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Out of bounds");
		}
	}

	private int getStartIndexOfDate(String[] infos) {
		if (hasTimeFormat()) {
			int temp = getIndexOfTime(infos);
			if (temp != INVALID) {
				if (infos[temp].equals("am") || infos[temp].equals("pm")) {
					return (temp <= 1) ? temp + 1 : 0;
				} else {
					return (temp >= 1) ? 0 : temp + 1;
				}
			} else {
				temp = getIndexOfColon(infos);
				return (temp == 0) ? temp + 1 : 0;
			}
		}
		return 0;
	}

	private int updateDay(String[] infos, GregorianCalendar targetDate,
			int numElements) {
		int startIndex = getStartIndexOfDate(infos);
		if (isTomorrowKeyWord(infos[startIndex])) {
			numElements = updateTomorrow(targetDate, numElements);
		} else if (isTodayKeyWord(infos[startIndex])) {
			numElements--;
		} else if (isTonightKeyWord(infos[startIndex])) {
			numElements--;
		} else if (hasDayWord()) {
			numElements = updateDateWithDay(infos, targetDate, numElements,	startIndex);
		}
		
		return numElements;
	}

	private int updateTomorrow(GregorianCalendar targetDate, int numElements) {
		targetDate.set(Calendar.DATE, targetDate.get(Calendar.DATE) + 1);
		return numElements - 1;
	}

	private int updateDateWithDay(String[] infos, GregorianCalendar targetDate,
			int numElements, int startIndex) {
		boolean hasNext = infos[startIndex].equals("next");
		int currentDay = currentDate.get(Calendar.DAY_OF_WEEK);
		int targetDay = (hasNext == true) ? getDay(infos[startIndex + 1]) : getDay(infos[startIndex]);
		if (targetDay == 1) {
			targetDay = SUNDAY;
		}
		if (currentDay == 1) {
			currentDay = SUNDAY;
		}

		long difference = targetDay - currentDay + ((hasNext || targetDay < currentDay) ? 7 : 0);
		targetDate.setTimeInMillis(currentDate.getTimeInMillis() + difference * DAY_IN_MILLIS);

		if (hasNext == true) {
			numElements -= 2;
		} else {
			numElements--;
		}
		return numElements;
	}

	private boolean isTomorrowKeyWord(String s) {
		return s.equals("tomorrow") || s.equals("tmr");
	}

	private boolean isTodayKeyWord(String s) {
		return s.equals("today");
	}

	private boolean isTonightKeyWord(String s) {
		return s.equals("tonight");
	}

	private boolean hasDayFormat() {
		boolean hasToday = dateInfo.contains("today");
		boolean hasTomorrow = dateInfo.contains("tomorrow") || dateInfo.contains("tmr");
		boolean hasTonight = dateInfo.contains("tonight");

		return hasToday || hasTomorrow || hasDayWord() || hasTonight;
	}

	private boolean hasDayWord() {
		return dateInfo.contains("mon") || dateInfo.contains("tue")
				|| dateInfo.contains("wed") || dateInfo.contains("thu")
				|| dateInfo.contains("fri") || dateInfo.contains("sat")
				|| dateInfo.contains("sun");
	}

	private boolean hasDateFormat() {
		boolean hasSlash = dateInfo.contains("/");
		boolean hasDash = dateInfo.contains("-");

		return hasSlash || hasDash || hasMonthWord();
	}

	private boolean hasMonthWord() {
		return dateInfo.contains("jan") || dateInfo.contains("feb")
				|| dateInfo.contains("mar") || dateInfo.contains("apr")
				|| dateInfo.contains("may") || dateInfo.contains("june")
				|| dateInfo.contains("jul") || dateInfo.contains("aug")
				|| dateInfo.contains("sep") || dateInfo.contains("oct")
				|| dateInfo.contains("nov") || dateInfo.contains("dec");
	}

	private int getDay(String s) {
		if (isMonday(s)) {
			return Calendar.MONDAY;
		} else if (isTuesday(s)) {
			return Calendar.TUESDAY;
		} else if (isWednesday(s)) {
			return Calendar.WEDNESDAY;
		} else if (isThursday(s)) {
			return Calendar.THURSDAY;
		} else if (isFriday(s)) {
			return Calendar.FRIDAY;
		} else if (isSaturday(s)) {
			return Calendar.SATURDAY;
		} else if (isSunday(s)) {
			return Calendar.SUNDAY;
		} else {
			throw new IllegalArgumentException("Invalid Day");
		}
	}

	private boolean isMonday(String s) {
		return s.equals("monday") || s.equals("mon");
	}

	private boolean isTuesday(String s) {
		return s.equals("tuesday") || s.equals("tue");
	}

	private boolean isWednesday(String s) {
		return s.equals("wednesday") || s.equals("wed");
	}

	private boolean isThursday(String s) {
		return s.equals("thursday") || s.equals("thu");
	}

	private boolean isFriday(String s) {
		return s.equals("friday") || s.equals("fri");
	}

	private boolean isSaturday(String s) {
		return s.equals("saturday") || s.equals("sat");
	}

	private boolean isSunday(String s) {
		return s.equals("sunday") || s.equals("sun");
	}

	private int getMonth(String s) {
		if (isJanuary(s)) {
			return Calendar.JANUARY;
		} else if (isFebruary(s)) {
			return Calendar.FEBRUARY;
		} else if (isMarch(s)) {
			return Calendar.MARCH;
		} else if (isApril(s)) {
			return Calendar.APRIL;
		} else if (isMay(s)) {
			return Calendar.MAY;
		} else if (isJune(s)) {
			return Calendar.JUNE;
		} else if (isJuly(s)) {
			return Calendar.JULY;
		} else if (isAugust(s)) {
			return Calendar.AUGUST;
		} else if (isSeptember(s)) {
			return Calendar.SEPTEMBER;
		} else if (isOctober(s)) {
			return Calendar.OCTOBER;
		} else if (isNovember(s)) {
			return Calendar.NOVEMBER;
		} else if (isDecember(s)) {
			return Calendar.DECEMBER;
		} else if (isInteger(s)) {
			throw new IllegalArgumentException("Out of bounds");
		} else {
			throw new IllegalArgumentException("Invalid Month");
		}
	}

	private String getMonthString(int i) {
		if (i == Calendar.JANUARY)
			return "Jan";
		else if (i == Calendar.FEBRUARY)
			return "Feb";
		else if (i == Calendar.MARCH)
			return "Mar";
		else if (i == Calendar.APRIL)
			return "Apr";
		else if (i == Calendar.MAY)
			return "May";
		else if (i == Calendar.JUNE)
			return "June";
		else if (i == Calendar.JULY)
			return "July";
		else if (i == Calendar.AUGUST)
			return "Aug";
		else if (i == Calendar.SEPTEMBER)
			return "Sep";
		else if (i == Calendar.OCTOBER)
			return "Oct";
		else if (i == Calendar.NOVEMBER)
			return "Nov";
		else if (i == Calendar.DECEMBER)
			return "Dec";
		else
			return "Invalid";
	}

	private boolean isJanuary(String s) {
		return s.equals("jan") || s.equals("1") || s.equals("january");
	}

	private boolean isFebruary(String s) {
		return s.equals("feb") || s.equals("2") || s.equals("february");
	}

	private boolean isMarch(String s) {
		return s.equals("mar") || s.equals("3") || s.equals("march");
	}

	private boolean isApril(String s) {
		return s.equals("apr") || s.equals("4") || s.equals("april");
	}

	private boolean isMay(String s) {
		return s.equals("may") || s.equals("5");
	}

	private boolean isJune(String s) {
		return s.equals("june") || s.equals("6");
	}

	private boolean isJuly(String s) {
		return s.equals("july") || s.equals("7");
	}

	private boolean isAugust(String s) {
		return s.equals("aug") || s.equals("8") || s.equals("august");
	}

	private boolean isSeptember(String s) {
		return s.equals("sep") || s.equals("9") || s.equals("september");
	}

	private boolean isOctober(String s) {
		return s.equals("oct") || s.equals("10") || s.equals("october");
	}

	private boolean isNovember(String s) {
		return s.equals("nov") || s.equals("11") || s.equals("november");
	}

	private boolean isDecember(String s) {
		return s.equals("dec") || s.equals("12") || s.equals("december");
	}

	private int processTime(String[] infos, GregorianCalendar tempDate, int numElements) {
		if (hasTimeFormat()) {
			return updateTime(infos, tempDate, numElements);
		} else {
			if (infos.length > MAXIMUM_LENGTH_FOR_DATE_PART) {
				return INVALID;
			}
			resetTime(tempDate);
		}
		return numElements;
	}

	private int getIndexOfTime(String[] infos) {
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].contains("am") || infos[i].contains("pm")) {
				return i;
			}
		}
		return INVALID;
	}

	private int getIndexOfColon(String[] infos) {
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].contains(":")) {
				return i;
			}
		}
		return INVALID;
	}

	private int updateTime(String[] infos, GregorianCalendar targetDate,
			int numElements) {
		int index = getIndexOfTime(infos);

		if (index != INVALID) {
			numElements = updateTimeWith12Format(infos, targetDate, index, numElements);
		} else {
			numElements = updateTimeWith24Format(infos, targetDate, numElements);
		}
		
		if (dateInfo.contains("tonight")) {
			if (targetDate.get(Calendar.HOUR_OF_DAY) * HOUR_IN_MINUTES + targetDate.get(Calendar.MINUTE) < FIRST_TWELVE_HOURS) {
				targetDate.setTimeInMillis(targetDate.getTimeInMillis() + DAY_IN_MILLIS);
			}
		}
		targetDate.set(Calendar.SECOND, 0);
		
		return numElements;
	}

	private int updateTimeWith12Format(String[] infos,
			GregorianCalendar targetDate, int index, int numElements) {
		boolean isDay;
		String timeInfo;
		boolean hasSpace;

		if (infos[index].equals("am") || infos[index].equals("pm")) {
			isDay = infos[index].equals("am");
			timeInfo = infos[index - 1];
			hasSpace = true;
		} else {
			isDay = infos[index].substring(infos[index].length() - 2).equals("am");
			timeInfo = infos[index].substring(0, infos[index].length() - 2);
			hasSpace = false;
		}

		if (timeInfo.contains(":")) {
			updateTimeWithColon(timeInfo, isDay, targetDate);
		} else if (timeInfo.contains(".")) {
			updateTimeWithDot(timeInfo, isDay, targetDate);
		} else {
			updateTimeWithoutSign(timeInfo, isDay, targetDate);
		}

		if (hasSpace) {
			return numElements - 2;
		} else {
			return numElements - 1;
		}
	}

	private void updateTimeWithColon(String timeInfo, boolean isDay,
			GregorianCalendar targetDate) {
		String[] time = timeInfo.split(":");
		if (time.length > 2) {
			throw new IllegalArgumentException("Invalid length in colon time format");
		}
		int hour = Integer.parseInt(time[0]) + (isDay ? 0 : 12);
		if (hour == 24 || hour == 12)
			hour -= 12;
		int minute = Integer.parseInt(time[1]);
		targetDate.set(Calendar.HOUR_OF_DAY, hour);
		targetDate.set(Calendar.MINUTE, minute);
		checkTimeBound(targetDate);
	}

	private void updateTimeWithDot(String timeInfo, boolean isDay,
			GregorianCalendar targetDate) {
		String[] time = timeInfo.split("\\.");
		if (time.length > 2) {
			throw new IllegalArgumentException("Invalid length in dot time format");
		}
		int hour = Integer.parseInt(time[0]) + (isDay ? 0 : 12);
		if (hour == 24 || hour == 12) {
			hour -= 12;
		}
		int minute = Integer.parseInt(time[1]);
		targetDate.set(Calendar.HOUR_OF_DAY, hour);
		targetDate.set(Calendar.MINUTE, minute);
		checkTimeBound(targetDate);
	}

	private void updateTimeWithoutSign(String timeInfo, boolean isDay,
			GregorianCalendar targetDate) {
		int s = Integer.parseInt(timeInfo);
		if (timeInfo.length() < 3) {
			int hour = s + (isDay ? 0 : 12);
			if (hour == 24 || hour == 12){
				hour -= 12;
			}
			targetDate.set(Calendar.HOUR_OF_DAY, hour);
			targetDate.set(Calendar.MINUTE, 0);
			checkTimeBound(targetDate);
		} else if (timeInfo.length() == 3 || timeInfo.length() == 4) {
			int hour = s / 100 + (isDay ? 0 : 12);
			if (hour == 24 || hour == 12){
				hour -= 12;
			}
			int minute = s % 100;
			targetDate.set(Calendar.HOUR_OF_DAY, hour);
			targetDate.set(Calendar.MINUTE, minute);
			checkTimeBound(targetDate);
		} else {
			throw new IllegalArgumentException("Invalid length in time without sign format");
		}
	}

	private void checkTimeBound(GregorianCalendar targetDate) {
		try {
			targetDate.get(Calendar.HOUR_OF_DAY);
			targetDate.get(Calendar.MINUTE);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Out of bounds");
		}
	}

	private int updateTimeWith24Format(String[] infos,
			GregorianCalendar targetDate, int numElements) {
		int index = getIndexOfColon(infos);
		String[] time = infos[index].split(":");
		if (time.length > 2){
			throw new IllegalArgumentException("Invalid in time with colon format");
		}
		int hour = Integer.parseInt(time[0]);
		int minute = Integer.parseInt(time[1]);

		targetDate.set(Calendar.HOUR_OF_DAY, hour);
		targetDate.set(Calendar.MINUTE, minute);
		checkTimeBound(targetDate);
		
		return numElements - 1;
	}

	private void resetTime(GregorianCalendar targetDate) {
		targetDate.set(Calendar.HOUR_OF_DAY, 0);
		targetDate.set(Calendar.MINUTE, 0);
		targetDate.set(Calendar.SECOND, 0);
		
		if (dateInfo.contains("tonight")) {
			targetDate.set(Calendar.HOUR_OF_DAY, 23);
			targetDate.set(Calendar.MINUTE, 59);
		}
	}

	private boolean hasTimeFormat() {
		return dateInfo.contains(":") || dateInfo.contains("am")|| dateInfo.contains("pm");
	}

	private boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean lessThan6Hours() {
		return (targetDate.getTimeInMillis() - currentDate.getTimeInMillis()) <= SIX_HOURS_IN_MILLIS;
	}

	private boolean hasTime(boolean isStartDate) {
		boolean isMidnight = targetDate.get(Calendar.HOUR_OF_DAY) == 23
				&& targetDate.get(Calendar.MINUTE) == 59;
		boolean isNewDay = targetDate.get(Calendar.HOUR_OF_DAY) == 0
				&& targetDate.get(Calendar.MINUTE) == 0;
		if (isStartDate && isNewDay) {
			return false;
		} else if (!isStartDate && isMidnight) {
			return false;
		}

		return true;
	}

	private boolean isToday() {
		boolean isCurrentYear = targetDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR);
		boolean isCurrentMonth = targetDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH);
		boolean isCurrentDate = targetDate.get(Calendar.DATE) == currentDate.get(Calendar.DATE);
		
		return isCurrentYear && isCurrentMonth && isCurrentDate;
	}

	private boolean isTomorrow() {
		long targetTime = targetDate.getTimeInMillis();
		long currentTime = currentDate.getTimeInMillis();
		long nearestDayTime = ((currentTime / DAY_IN_MILLIS) + 1) * DAY_IN_MILLIS;
		long nextNearestDayTime = nearestDayTime + DAY_IN_MILLIS;
		
		return targetTime >= nearestDayTime && targetTime <= nextNearestDayTime;
	}

	private boolean isTonight() {
		boolean isCurrentYear = targetDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR);
		boolean isCurrentMonth = targetDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH);
		boolean isCurrentDate = targetDate.get(Calendar.DATE) == currentDate.get(Calendar.DATE);
		boolean isMidnight = targetDate.get(Calendar.HOUR_OF_DAY) == 23 && targetDate.get(Calendar.MINUTE) == 59;
		
		return isCurrentYear && isCurrentMonth && isCurrentDate && isMidnight;
	}
}
