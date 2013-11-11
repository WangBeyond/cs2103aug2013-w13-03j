import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.google.gdata.data.DateTime;

//@author A0098077N
/**
 *
 * This class is used to store the start date and end date in iDo
 * It provides the conversions for various type of inputs or class objects
 *
 */
public class CustomDate {
	final private static int VALID = 1;
	final private static int INVALID = -1;
	final public static int OUT_OF_BOUNDS = 0;
	final private static int MAXIMUM_LENGTH_OF_DATE_INFO = 4;
	final private static int MAXIMUM_LENGTH_FOR_DATE_PART = 2;
	final private static int MAXIMUM_LENGTH_FOR_TIME_PART = 2;
	final private static int EMPTY_DATE_INFO = 0;
	final private static int SUNDAY = 8;
	final private static int MINUTE_IN_MILLIS = 60 * 1000;
	final public static long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
	final private static long SIX_HOURS_IN_MILLIS = 6 * 60 * 60 * 1000;
	final private static long WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000;
	final private static long MONTH_IN_MILLIS = 30L * 24 * 60 * 60 * 1000;
	final private static long YEAR_IN_MILLIS = 365 * 24 * 60 * 60 * 1000;
	final private static long FIRST_TWELVE_HOURS = 12 * 60;
	final private static int HOUR_IN_MINUTES = 60;
	final private static String DAILY_REGEX = "(every)(\\d+)(days?)";
	final private static String WEEKLY_REGEX = "(every)(\\d+)(weeks?)";
	final private static String MONTHLY_REGEX = "(every)(\\d+)(months?)";
	final private static String YEARLY_REGEX = "(every)(\\d+)(years?)";
	final private static int LARGER = 1;
	final private static int SMALLER = -1;
	final private static int EQUAL = 0;

	// The common current date among CustomDate object
	private static GregorianCalendar currentDate;

	// The option whether to display remaining time according to the current time
	private static boolean displayRemaining = true;

	// The target date of CustomDate object
	private GregorianCalendar sourceDate;

	// The date info of this target date
	private String dateInfo;
	// Indicator whether the dateInfo has indicated the specific date
	boolean hasIndicatedDate;
	
	// Default constructors
	public CustomDate() {
		sourceDate = new GregorianCalendar();
	}
	
	/**
	 *  Constructor from a DateTime object by converting its string format into the desired format
	 *  DateTime format: YYYY/MM/DDTHH:MM:SS
	 * @param dateTime the DateTime object 
	 */
	public CustomDate(DateTime dateTime){
		String dateTimeString = dateTime.toString();
		String date = dateTimeString.substring(8, 10);
		String month = dateTimeString.substring(5, 7);
		String year = dateTimeString.substring(0, 4);
		String time = "00:00";
		String second = "00";
		
		boolean hasTime = dateTimeString.length() > 10;
		if (hasTime) {
			time = dateTimeString.substring(11, 16);
			second = dateTimeString.substring(17, 19);
		}
		String customDateFormat = date + "/" + month + "/" + year + " " + time;
		
		sourceDate = new GregorianCalendar();
		convert(customDateFormat);
		setSecond(Integer.parseInt(second));
	}
	
	/**
	 * Constructor from an input string
	 * @param inputDateString the input of date
	 */
	public CustomDate(String inputDateString) {
		sourceDate = new GregorianCalendar();
		convert(inputDateString);
	}
	
	// Set whether to display the remaining time or not
	public static void setDisplayRemaining(boolean displayRemaining){
		CustomDate.displayRemaining = displayRemaining;
	}
	
	/*************** GET functions to get corresponding time elements in CustomDate *****************/
	public int getYear() {
		return sourceDate.get(Calendar.YEAR);
	}

	public int getMonth() {
		return sourceDate.get(Calendar.MONTH);
	}

	public int getDate() {
		return sourceDate.get(Calendar.DATE);
	}

	public int getHour() {
		return sourceDate.get(Calendar.HOUR_OF_DAY);
	}

	public int getMinute() {
		return sourceDate.get(Calendar.MINUTE);
	}
	
	public int getSecond(){
		return sourceDate.get(Calendar.SECOND);
	}
	
	public long getTimeInMillis() {
		return sourceDate.getTimeInMillis();
	}
	
	// Check whether the dateInfo has indicated an exact date
	public boolean hasIndicatedDate(){
		return hasIndicatedDate;
	}

	/************************** SET methods to set the time elements in CustomDate **************/
	public void setHour(int hour) {
		sourceDate.set(Calendar.HOUR_OF_DAY, hour);
	}

	public void setMinute(int minute) {
		sourceDate.set(Calendar.MINUTE, minute);
	}
	
	public void setDate(int date){
		sourceDate.set(Calendar.DATE, date);
	}
	
	public void setMonth(int month){
		sourceDate.set(Calendar.MONTH, month);
	}
	
	public void setYear(int year){
		sourceDate.set(Calendar.YEAR, year);
	}
	
	public void setSecond(int second){
		sourceDate.set(Calendar.SECOND, second);
	}
	
	public void setTimeInMillis(long millis) {
		sourceDate.setTimeInMillis(millis);
	}

	/**
	 * Convert the CustomDate object into DateTime object from Google Library
	 * 
	 * @return the DateTime object
	 */
	public DateTime returnInDateTimeFormat() {
		return new DateTime(sourceDate.getTime(), TimeZone.getDefault());
	}
	
	/**
	 * Convert into CustomDate object from the string format of recurring date from Google Library
	 * Recurring date format: DDMMYYTHHMMSS
	 * @param recurrenceDateString the string with format from Google Library
	 * @return the converted CustomDate object
	 */
	public static CustomDate convertFromRecurringDateString(String recurrenceDateString){
		String year = recurrenceDateString.substring(0, 4);
		String month = recurrenceDateString.substring(4, 6);
		String date = recurrenceDateString.substring(6, 8);
		String hour = recurrenceDateString.substring(9, 11);
		String minute = recurrenceDateString.substring(11, 13);
		
		return new CustomDate(date+"/" + Integer.parseInt(month) + "/" + year + " " + hour + ":" + minute);
	}
	
	/**
	 * Get the recurring format from Google Library from the CustomDate object
	 * @return the string in required format
	 */
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
	 * This function is used to convert this CustomDate object to string with
	 * format for display in GUI
	 * 
	 * @param isStartDate
	 *            check whether this is a start or end date
	 * @return the required String displayed on GUI
	 */
	public String toString(boolean isStartDate) {
		updateCurrentDate();
		boolean hasTime = hasTime(isStartDate);
	
		// Check the capability to display remaining time
		if (displayRemaining && !beforeCurrentTime() && lessThan6Hours()) {
			return getRemainingTimeString();
		}
	
		// Check if the time is tonight
		if (isTonight()) {
			return "Tonight";
		}
	
		// Otherwise, display the standard format
		String result = "";
		result += getDateString();
		result += getTimeString(hasTime);
		return result;
	}

	/**
	 * Get the remaining time relatively to the current time in String
	 * @return the required string with correct format for display
	 */
	public String getRemainingTimeString() {
		int remainingTime = (int) (sourceDate.getTimeInMillis() - currentDate.getTimeInMillis()) / MINUTE_IN_MILLIS;
		int remainingHours = remainingTime / HOUR_IN_MINUTES;
		int remainingMinutes = remainingTime % HOUR_IN_MINUTES;
		return remainingHours + "h " + remainingMinutes + "m";
	}
	
	public int getRemainingTime(){
		int remainingTime = (int) (sourceDate.getTimeInMillis() - currentDate.getTimeInMillis()) / MINUTE_IN_MILLIS;
		return remainingTime;
	}

	/**
	 * Get the string of the date in CustomDate object
	 * 
	 * @return the required string with correct format
	 */
	private String getDateString() {
		if (isToday()) {
			return "Today";
		} else {
			String dateString;
			dateString = sourceDate.get(Calendar.DATE) + " " + getMonthString(sourceDate.get(Calendar.MONTH));
			if (!isCurrentYear()) {
				dateString += " " + sourceDate.get(Calendar.YEAR);
			}
			return dateString;
		}
	}
	
	/**
	 * Get the string of the time in CustomDate object
	 * @param hasTime to check whether the dateInfo string does indicate the time
	 * @return the required string with correct format
	 */
	private String getTimeString(boolean hasTime) {
		if (hasTime) {
			DecimalFormat df = new DecimalFormat("00");
			return "\n " + sourceDate.get(Calendar.HOUR_OF_DAY) + ":" + df.format(sourceDate.get(Calendar.MINUTE));
		}
		return "";
	}
	
	// Check whether the target year is the current year
	private boolean isCurrentYear() {
		return sourceDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR);
	}
	
	/**
	 * Convert a CustomDate object into string with required format from DataStorage class
	 * 
	 * @param storedDate
	 *            the object needed to be converted
	 * @return the String format of this object
	 */
	public static String convertString(CustomDate storedDate) {
		if (storedDate == null)
			return "-";
		
		int date = storedDate.sourceDate.get(Calendar.DATE);
		int month = storedDate.sourceDate.get(Calendar.MONTH);
		int year = storedDate.sourceDate.get(Calendar.YEAR);
		int hour = storedDate.sourceDate.get(Calendar.HOUR_OF_DAY);
		int minute = storedDate.sourceDate.get(Calendar.MINUTE);
		
		return date + "/" + (month + 1) + "/" + year + " " + hour + ":" + minute;
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


	/**
	 * This function is used to compare 2 CustomDate objects only according to
	 * their dates, not their times
	 * 
	 * @param date1
	 *            the first date
	 * @param date2
	 *            the second date
	 * @return value indicating the result of comparison
	 */
	public static int dateCompare(CustomDate date1, CustomDate date2) {
		int difference = 0;
		if (date1 == null && date2 == null) {
			return EQUAL;
		} else if (date1 == null && date2 != null) {
			return LARGER;
		} else if (date1 != null && date2 == null) {
			return SMALLER;
		}
		
		for (int i = 0; i < 3; i++) {
			if (i == 0) {
				difference = date1.getYear() - date2.getYear();
			} else if (i == 1) {
				difference = date1.getMonth() - date2.getMonth();
			} else if (i == 2) {
				difference = date1.getDate() - date2.getDate();
			}
			if (difference != 0) {
				return difference;
			}
		}
		
		return difference;
	}
	
	// Check whether the sourceDate is before current time
	public boolean beforeCurrentTime() {
		return (currentDate.getTimeInMillis() - sourceDate.getTimeInMillis()) > 0;
	}

	// Refresh the current time
	public static void updateCurrentDate() {
		currentDate = new GregorianCalendar();
	}
	
	//@author A0105667B
	/**
	 * Get the distance between 2 periods in recurring rule for updating
	 * 
	 * @param repetition
	 *            the string format of the recurring rule
	 * @return the required distance
	 */
	public static long getUpdateDistance(String repetition) {
		if (checkDailyRoutine(repetition)>0) {
			return DAY_IN_MILLIS * checkDailyRoutine(repetition);
		} else if (checkWeeklyRoutine(repetition)>0) {
			return WEEK_IN_MILLIS * checkWeeklyRoutine(repetition);
		} else if (checkMonthlyRoutine(repetition)>0) {
			return MONTH_IN_MILLIS * checkMonthlyRoutine(repetition);
		} else if (checkYearlyRoutine(repetition)>0) {
			return YEAR_IN_MILLIS * checkYearlyRoutine(repetition);
		} else {
			return 0;
		}
	}

	/**
	 * This function is used to check whether the recurring rule is daily
	 * routine. If yes, get the distance between each period.
	 * 
	 * @param repetition
	 *            the string format of the recurring rule
	 * @return 0 if this is not a daily routine. Otherwise, return the distance.
	 */
	private static int checkDailyRoutine(String repetition) {
		boolean isFrequentDailyRoutine = repetition.equals("daily");
		if (isFrequentDailyRoutine) {
			return 1;
		} else if (repetition.matches(DAILY_REGEX)) {
			return Integer.valueOf(repetition.replaceAll(DAILY_REGEX, "$2"));
		} 
		
		return 0;
	}
	
	/**
	 * This function is used to check whether the recurring rule is weekly
	 * routine. If yes, get the distance between each period.
	 * 
	 * @param repetition
	 *            the string format of the recurring rule
	 * @return 0 if this is not a weekly routine. Otherwise, return the
	 *         distance.
	 */
	private static int checkWeeklyRoutine(String repetition) {
		if (isFrequentWeeklyRoutine(repetition)) {
			return 1;
		} else if(repetition.matches(WEEKLY_REGEX)) {
			int weekNum = Integer.valueOf(repetition.replaceAll(WEEKLY_REGEX, "$2"));
			return weekNum;
		}

		return 0;
	}
	
	private static boolean isFrequentWeeklyRoutine(String repetition){
		boolean isFrequentWeeklyRoutine = repetition.equals("weekly");
		return isFrequentWeeklyRoutine;
	}

	/**
	 * This function is used to check whether the recurring rule is monthly
	 * routine. If yes, get the distance between each period.
	 * 
	 * @param repetition
	 *            the string format of the recurring rule
	 * @return 0 if this is not a monthly routine. Otherwise, return the
	 *         distance.
	 */
	private static int checkMonthlyRoutine(String repetition) {
		boolean isFrequentMonthlyRoutine = repetition.equals("monthly");
		if(isFrequentMonthlyRoutine){
			return 1;
		} else if(repetition.matches(MONTHLY_REGEX)) {
			int monthNum = Integer.valueOf(repetition.replaceAll(MONTHLY_REGEX, "$2"));
			return monthNum;
		} 
		
		return 0;
	}
	
	/**
	 * This function is used to check whether the recurring rule is yearly
	 * routine. If yes, get the distance between each period.
	 * 
	 * @param repetition
	 *            the string format of the recurring rule
	 * @return 0 if this is not a yearly routine. Otherwise, return the
	 *         distance.
	 */
	private static int checkYearlyRoutine(String repetition) {
		boolean isFrequentYearlyRoutine = repetition.equals("yearly") || repetition.equals("annually");
		if (isFrequentYearlyRoutine) {
			return 1;
		} else if (repetition.matches(YEARLY_REGEX)) {
			int yearNum = Integer.valueOf(repetition.replaceAll(YEARLY_REGEX, "$2"));
			return yearNum;
		}
		
		return 0;
	}
	
	//@author A0098077N
	/**
	 * This function is used to set the date from given info string
	 * This is the main function for conversion for CustomDate class.
	 * @param dateString
	 *            the given info of the date
	 * @return value indicating the conversion is successful or not
	 */
	public int convert(String dateString) {
		dateInfo = dateString.toLowerCase();
		String[] infos = dateInfo.split("\\s+");

		updateCurrentDate();

		boolean invalidLength = infos.length > MAXIMUM_LENGTH_OF_DATE_INFO || infos.length == EMPTY_DATE_INFO;
		if (invalidLength) {
			return INVALID;
		}
		
		// Start the process of conversion
		try {
			int numElements = infos.length; // get the current number of elements in the String array
			
			GregorianCalendar tempDate = new GregorianCalendar();
			tempDate.setLenient(false); // prevent Out of Bounds date
			
			// Date Processing
			numElements = processDate(infos, tempDate, numElements);
			if (numElements == INVALID) {
				return INVALID;
			}
			if (numElements != infos.length) { // check if the dateInfo did indicating the date
				hasIndicatedDate = true;
			} else {
				hasIndicatedDate = false;
			}
			
			// Time Processing
			numElements = processTime(infos, tempDate, numElements);
			if (numElements > 0 || numElements == INVALID) {
				return INVALID;
			}

			sourceDate = tempDate;
			return VALID;
		} catch (Exception e) {
			if (e.getMessage().equals("Out of bounds")) { // case when the date is out of bounds
				return OUT_OF_BOUNDS;
			}
			return INVALID;
		}
	}
	
	/**
	 * This function is used to process the date conversion
	 * 
	 * @param infos
	 *            the String array containing infos of target of conversion
	 * @param tempDate
	 *            the modified GregorianCalendar temporary object
	 * @param numElements
	 *            number of unprocessed elements left in the array
	 * @return the number of elements left unprocessed in the array
	 */
	private int processDate(String[] infos, GregorianCalendar tempDate, int numElements) {
		if (hasDateFormat()) { // has date format
			return updateDate(infos, tempDate, numElements);
		} else if (hasDayFormat()) { // has day format, i.e Monday
			return updateDay(infos, tempDate, numElements);
		} else if (infos.length > MAXIMUM_LENGTH_FOR_TIME_PART) { // no indication of date and exceed length for time part 
			return INVALID;
		}
		
		return numElements;
	}
	
	/**
	 * This function is used to update the date in date format
	 * 
	 * @param infos
	 *            the String array containing infos of target of conversion
	 * @param tempDate
	 *            the modified GregorianCalendar temporary object
	 * @param numElements
	 *            number of unprocessed elements left in the array
	 * @return the number of elements left unprocessed in the array
	 */
	private int updateDate(String[] infos, GregorianCalendar tempDate, int numElements) {
		int startIndex = getStartIndexOfDate(infos);

		if (hasMonthWord()) { // containing month String i.e Jan
			numElements = updateDateWithMonth(infos, tempDate, numElements, startIndex);
		} else if (hasSlashFormat(dateInfo)) { 
			numElements = updateDateWithSlash(infos, tempDate, numElements, startIndex);
		} else if(hasDashFormat(dateInfo)) {
			numElements = updateDateWithDash(infos, tempDate, numElements, startIndex);
		}

		return numElements;
	}
	
	private static boolean hasSlashFormat(String dateInfo){
		return dateInfo.contains("/");
	}
	
	private static boolean hasDashFormat(String dateInfo){
		return dateInfo.contains("-");
	}
	
	/**
	 * This function is used to update the date in date format with indicated
	 * month word
	 * 
	 * @param infos
	 *            the String array containing infos of target of conversion
	 * @param tempDate
	 *            the modified GregorianCalendar temporary object
	 * @param numElements
	 *            number of unprocessed elements left in the array
	 * @param startIndex
	 *             the start index of the date in the array
	 * @return the number of elements left unprocessed in the array
	 */
	private int updateDateWithMonth(String[] infos, GregorianCalendar tempDate, int numElements, int startIndex) {
		int date = Integer.parseInt(infos[startIndex]);
		int month = getMonth(infos[startIndex + 1]);

		tempDate.set(Calendar.MONTH, month);
		tempDate.set(Calendar.DATE, date);
		checkDateBound(tempDate); 

		return numElements - 2;
	}
	
	/**
	 * This function is used to update the date in date format with slash
	 * 
	 * @param infos
	 *            the String array containing infos of target of conversion
	 * @param tempDate
	 *            the modified GregorianCalendar temporary object
	 * @param numElements
	 *            number of unprocessed elements left in the array
	 * @param startIndex
	 *            the start index of the date in the array
	 * @return the number of elements left unprocessed in the array
	 */
	private int updateDateWithSlash(String[] infos,
			GregorianCalendar tempDate, int numElements, int startIndex) {
		String[] numbers = infos[startIndex].split("/");
		boolean invalidLength = numbers.length != 3 && numbers.length != 2;
		if (invalidLength) {
			throw new IllegalArgumentException("Invalid length in slash format");
		}

		int month = getMonth(numbers[1]);
		tempDate.set(Calendar.MONTH, month);
		int date = Integer.parseInt(numbers[0]);
		tempDate.set(Calendar.DATE, date);
		int year = (numbers.length == 3) ? Integer.parseInt(numbers[2]) : currentDate.get(Calendar.YEAR);
		tempDate.set(Calendar.YEAR, year);
		checkDateBound(tempDate); // check whether the date is out of bounds

		return numElements - 1;
	}
	
	/**
	 * This function is used to update the date in date format with dash
	 * 
	 * @param infos
	 *            the String array containing infos of target of conversion
	 * @param tempDate
	 *            the modified GregorianCalendar temporary object
	 * @param numElements
	 *            number of unprocessed elements left in the array
	 * @param startIndex
	 *            the start index of the date in the array
	 * @return the number of elements left unprocessed in the array
	 */
	private int updateDateWithDash(String[] infos,
			GregorianCalendar tempDate, int numElements, int startIndex) {
		String[] numbers = infos[startIndex].split("-");
		boolean invalidLength = numbers.length != 3 && numbers.length != 2;
		if (invalidLength)
			throw new IllegalArgumentException("Invalid length in dash format");

		int month = getMonth(numbers[1]);
		tempDate.set(Calendar.MONTH, month);
		int date = Integer.parseInt(numbers[0]);
		tempDate.set(Calendar.DATE, date);
		int year = (numbers.length == 3) ? Integer.parseInt(numbers[2]) : currentDate.get(Calendar.YEAR);
		tempDate.set(Calendar.YEAR, year);
		checkDateBound(tempDate); // check whether the date is out of bounds

		return numElements - 1;
	}
	
	/**
	 * This function is used to check whether the date is out of bounds
	 * If yes, it will throw an IllegalArgumentException with indicated message
	 */
	private void checkDateBound(GregorianCalendar tempDate) {
		try {
			tempDate.get(Calendar.DATE);
			tempDate.get(Calendar.MONTH);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Out of bounds");
		}
	}
	
	/**
	 * Get the starting index of the DATE part in the string array
	 */
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
	
	/**
	 * This function is used to update the date in day format
	 * 
	 * @param infos
	 *            the String array containing infos of target of conversion
	 * @param tempDate
	 *            the modified GregorianCalendar temporary object
	 * @param numElements
	 *            number of unprocessed elements left in the array
	 * @return the number of elements left unprocessed in the array
	 */
	private int updateDay(String[] infos, GregorianCalendar tempDate,
			int numElements) {
		int startIndex = getStartIndexOfDate(infos);
		
		if (isTomorrowKeyWord(infos[startIndex])) {
			numElements = updateTomorrow(tempDate, numElements);
		} else if (isTodayKeyWord(infos[startIndex])) {
			numElements--; // no need to update
		} else if (isTonightKeyWord(infos[startIndex])) {
			numElements--; // no need to update
		} else if (hasDayWord()) {
			numElements = updateDateWithDay(infos, tempDate, numElements, startIndex);
		}
		
		return numElements;
	}
	
	// Update the target date as tomorrow
	private int updateTomorrow(GregorianCalendar tempDate, int numElements) {
		tempDate.setTimeInMillis(currentDate.getTimeInMillis() + DAY_IN_MILLIS);
		return numElements - 1;
	}
	
	/**
	 * This function is used to update the date in date format with day i.e Monday
	 * 
	 * @param infos
	 *            the String array containing infos of target of conversion
	 * @param tempDate
	 *            the modified GregorianCalendar temporary object
	 * @param numElements
	 *            number of unprocessed elements left in the array
	 * @param startIndex
	 *            the start index of the date in the array
	 * @return the number of elements left unprocessed in the array
	 */
	private int updateDateWithDay(String[] infos, GregorianCalendar tempDate,
			int numElements, int startIndex) {
		boolean hasNext = infos[startIndex].equals("next");
		int currentDay = currentDate.get(Calendar.DAY_OF_WEEK);
		int targetDay = (hasNext == true) ? getDay(infos[startIndex + 1]) : getDay(infos[startIndex]);
		if (targetDay == 1) { // value of Sunday in GregorianCalendar is 1
			targetDay = SUNDAY; // change to 8
		}
		if (currentDay == 1) {
			currentDay = SUNDAY;
		}

		long difference = targetDay - currentDay + ((hasNext || targetDay < currentDay) ? 7 : 0);
		tempDate.setTimeInMillis(currentDate.getTimeInMillis() + difference * DAY_IN_MILLIS);
		if (hasNext == true) {
			numElements -= 2;
		} else {
			numElements--;
		}
		
		return numElements;
	}
	
	// Check if the day String is tomorrow
	private boolean isTomorrowKeyWord(String day) {
		return day.equals("tomorrow") || day.equals("tmr");
	}
	
	// Check if the day String is today
	private boolean isTodayKeyWord(String day) {
		return day.equals("today");
	}

	// Check if the day String is tonight
	private boolean isTonightKeyWord(String day) {
		return day.equals("tonight");
	}
	
	// Check if the dateInfo String may have day format
	private boolean hasDayFormat() {
		boolean hasToday = dateInfo.contains("today");
		boolean hasTomorrow = dateInfo.contains("tomorrow") || dateInfo.contains("tmr");
		boolean hasTonight = dateInfo.contains("tonight");

		return hasToday || hasTomorrow || hasDayWord() || hasTonight;
	}
	
	// Check if the dateInfo String may contain days in a week
	private boolean hasDayWord() {
		return dateInfo.contains("mon") || dateInfo.contains("tue")
				|| dateInfo.contains("wed") || dateInfo.contains("thu")
				|| dateInfo.contains("fri") || dateInfo.contains("sat")
				|| dateInfo.contains("sun");
	}
	
	// Check if the dateInfo String may have date format
	private boolean hasDateFormat() {
		boolean hasSlash = dateInfo.contains("/");
		boolean hasDash = dateInfo.contains("-");

		return hasSlash || hasDash || hasMonthWord();
	}	

	// Check if the dateInfo String may contain month in a year
	private boolean hasMonthWord() {
		return dateInfo.contains("jan") || dateInfo.contains("feb")
				|| dateInfo.contains("mar") || dateInfo.contains("apr")
				|| dateInfo.contains("may") || dateInfo.contains("june")
				|| dateInfo.contains("jul") || dateInfo.contains("aug")
				|| dateInfo.contains("sep") || dateInfo.contains("oct")
				|| dateInfo.contains("nov") || dateInfo.contains("dec");
	}
	
	/**
	 * This function is used to get the corresponding Integer value of the day string 
	 */
	private int getDay(String day) {
		if (isMonday(day)) {
			return Calendar.MONDAY;
		} else if (isTuesday(day)) {
			return Calendar.TUESDAY;
		} else if (isWednesday(day)) {
			return Calendar.WEDNESDAY;
		} else if (isThursday(day)) {
			return Calendar.THURSDAY;
		} else if (isFriday(day)) {
			return Calendar.FRIDAY;
		} else if (isSaturday(day)) {
			return Calendar.SATURDAY;
		} else if (isSunday(day)) {
			return Calendar.SUNDAY;
		} else {
			throw new IllegalArgumentException("Invalid Day");
		}
	}
	
	/********************************************* Determine the day in the week  **************************************/
	private boolean isMonday(String day) {
		return day.equals("monday") || day.equals("mon");
	}

	private boolean isTuesday(String day) {
		return day.equals("tuesday") || day.equals("tue");
	}

	private boolean isWednesday(String day) {
		return day.equals("wednesday") || day.equals("wed");
	}

	private boolean isThursday(String day) {
		return day.equals("thursday") || day.equals("thu");
	}

	private boolean isFriday(String day) {
		return day.equals("friday") || day.equals("fri");
	}

	private boolean isSaturday(String day) {
		return day.equals("saturday") || day.equals("sat");
	}

	private boolean isSunday(String day) {
		return day.equals("sunday") || day.equals("sun");
	}
	
	/**
	 * This function is used to get the Integer value of the month String
	 */
	private int getMonth(String month) {
		if (isJanuary(month)) {
			return Calendar.JANUARY;
		} else if (isFebruary(month)) {
			return Calendar.FEBRUARY;
		} else if (isMarch(month)) {
			return Calendar.MARCH;
		} else if (isApril(month)) {
			return Calendar.APRIL;
		} else if (isMay(month)) {
			return Calendar.MAY;
		} else if (isJune(month)) {
			return Calendar.JUNE;
		} else if (isJuly(month)) {
			return Calendar.JULY;
		} else if (isAugust(month)) {
			return Calendar.AUGUST;
		} else if (isSeptember(month)) {
			return Calendar.SEPTEMBER;
		} else if (isOctober(month)) {
			return Calendar.OCTOBER;
		} else if (isNovember(month)) {
			return Calendar.NOVEMBER;
		} else if (isDecember(month)) {
			return Calendar.DECEMBER;
		} else if (isInteger(month)) {
			throw new IllegalArgumentException("Out of bounds");
		} else {
			throw new IllegalArgumentException("Invalid Month");
		}
	}
	
	/**
	 * This function is used to get the string of the month for display
	 */
	private String getMonthString(int month) {
		if (month == Calendar.JANUARY) {
			return "Jan";
		} else if (month == Calendar.FEBRUARY) {
			return "Feb";
		} else if (month == Calendar.MARCH) {
			return "Mar";
		} else if (month == Calendar.APRIL) {
			return "Apr";
		} else if (month == Calendar.MAY) {
			return "May";
		} else if (month == Calendar.JUNE) {
			return "June";
		} else if (month == Calendar.JULY) {
			return "July";
		} else if (month == Calendar.AUGUST) {
			return "Aug";
		} else if (month == Calendar.SEPTEMBER) {
			return "Sep";
		} else if (month == Calendar.OCTOBER) {
			return "Oct";
		} else if (month == Calendar.NOVEMBER) {
			return "Nov";
		} else if (month == Calendar.DECEMBER) {
			return "Dec";
		} else {
			return "Invalid";
		}
	}
	/********************************* Determine the month *********************************************/
	private boolean isJanuary(String month) {
		return month.equals("jan") || (isInteger(month) && String.valueOf(Integer.parseInt(month)).equals("1")) || month.equals("january");
	}

	private boolean isFebruary(String month) {
		return month.equals("feb") || (isInteger(month) && String.valueOf(Integer.parseInt(month)).equals("2") )|| month.equals("february");
	}

	private boolean isMarch(String month) {
		return month.equals("mar") ||  (isInteger(month) &&String.valueOf(Integer.parseInt(month)).equals("3") )|| month.equals("march");
	}

	private boolean isApril(String month) {
		return month.equals("apr") ||  (isInteger(month) &&String.valueOf(Integer.parseInt(month)).equals("4")) || month.equals("april");
	}

	private boolean isMay(String month) {
		return month.equals("may") || (isInteger(month) && String.valueOf(Integer.parseInt(month)).equals("5"));
	}

	private boolean isJune(String month) {
		return month.equals("june") ||  (isInteger(month) && String.valueOf(Integer.parseInt(month)).equals("6"));
	}

	private boolean isJuly(String month) {
		return month.equals("july") ||  (isInteger(month) && String.valueOf(Integer.parseInt(month)).equals("7"));
	}

	private boolean isAugust(String month) {
		return month.equals("aug") || (isInteger(month) && String.valueOf(Integer.parseInt(month)).equals("8"))|| month.equals("august");
	}

	private boolean isSeptember(String month) {
		return month.equals("sep") || (isInteger(month) && String.valueOf(Integer.parseInt(month)).equals("9")) || month.equals("september");
	}

	private boolean isOctober(String month) {
		return month.equals("oct") || (isInteger(month) && String.valueOf(Integer.parseInt(month)).equals("10")) || month.equals("october");
	}

	private boolean isNovember(String month) {
		return month.equals("nov") ||  (isInteger(month) && String.valueOf(Integer.parseInt(month)).equals("11")) || month.equals("november");
	}

	private boolean isDecember(String month) {
		return month.equals("dec") ||  (isInteger(month) && String.valueOf(Integer.parseInt(month)).equals("12")) || month.equals("december");
	}
	
	/**
	 * This function is used to process the time conversion
	 * 
	 * @param infos
	 *            the String array containing infos of target of conversion
	 * @param tempDate
	 *            the modified GregorianCalendar temporary object
	 * @param numElements
	 *            number of unprocessed elements left in the array
	 * @return the number of elements left unprocessed in the array
	 */
	private int processTime(String[] infos, GregorianCalendar tempDate, int numElements) {
		if (hasTimeFormat()) {
			return updateTime(infos, tempDate, numElements);
		} else {
			if (infos.length > MAXIMUM_LENGTH_FOR_DATE_PART) { // no indication of date and exceed length for DATE part 
				return INVALID;
			}
			resetTime(tempDate); 
		}
		
		return numElements;
	}
	
	// Get the start index of TIME with 'am' or 'pm' in the String array
	private int getIndexOfTime(String[] infos) {
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].contains("am") || infos[i].contains("pm")) {
				return i;
			}
		}
		
		return INVALID;
	}
	
	// Get the start index of TIME with colon in the String array
	private int getIndexOfColon(String[] infos) {
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].contains(":")) {
				return i;
			}
		}
		return INVALID;
	}

	/**
	 * This function is used to update the time
	 * 
	 * @param infos
	 *            the String array containing infos of target of conversion
	 * @param tempDate
	 *            the modified GregorianCalendar temporary object
	 * @param numElements
	 *            number of unprocessed elements left in the array
	 * @return the number of elements left unprocessed in the array
	 */
	private int updateTime(String[] infos, GregorianCalendar tempDate,
			int numElements) {
		int index = getIndexOfTime(infos);
		if (index != INVALID) { // has time with 12 Format
			numElements = updateTimeWith12Format(infos, tempDate, index, numElements);
		} else {
			numElements = updateTimeWith24Format(infos, tempDate, numElements);
		}
		
		// Conversion of time for 'tonight'
		if (dateInfo.contains("tonight")) {
			if (tempDate.get(Calendar.HOUR_OF_DAY) * HOUR_IN_MINUTES + tempDate.get(Calendar.MINUTE) < FIRST_TWELVE_HOURS) {
				tempDate.setTimeInMillis(tempDate.getTimeInMillis() + DAY_IN_MILLIS);
			}
		}
		
		tempDate.set(Calendar.SECOND, 0); // default second for time
		return numElements;
	}
	
	/**
	 * This function is used to update the time in 12-hour format
	 * 
	 * @param infos
	 *            the String array containing infos of target of conversion
	 * @param tempDate
	 *            the modified GregorianCalendar temporary object
	 * @param numElements
	 *            number of unprocessed elements left in the array
	 * @param startIndex
	 *            the start index of the time in the array
	 * @return the number of elements left unprocessed in the array
	 */
	private int updateTimeWith12Format(String[] infos,
			GregorianCalendar tempDate, int index, int numElements) {
		boolean isDay;
		String timeInfo;
		boolean hasSpace;
		
		// Check whether the 'am' or 'pm' is attached to the time info(i.e 6pm or 6 pm)
		if (infos[index].equals("am") || infos[index].equals("pm")) {
			isDay = infos[index].equals("am");
			timeInfo = infos[index - 1];
			hasSpace = true;
		} else {
			isDay = infos[index].substring(infos[index].length() - 2).equals("am");
			timeInfo = infos[index].substring(0, infos[index].length() - 2);
			hasSpace = false;
		}
		
		// Start updating
		boolean hasColon = timeInfo.contains(":");
		boolean hasDot = timeInfo.contains(".");
		if (hasColon) {
			updateTimeWithColon(timeInfo, isDay, tempDate);
		} else if (hasDot) {
			updateTimeWithDot(timeInfo, isDay, tempDate);
		} else { // no signs
			updateTimeWithoutSign(timeInfo, isDay, tempDate);
		}

		if (hasSpace) {
			return numElements - 2;
		} else {
			return numElements - 1;
		}
	}
	
	/**
	 * This function is used to update time in 12-hour format with colon
	 * 
	 * @param timeInfo
	 *            the time info String
	 * @param isDay
	 *            indicator whether it is 'am' or 'pm'
	 * @param modifiedDate
	 *            the modified GregorianCalendar temporary object
	 */
	private void updateTimeWithColon(String timeInfo, boolean isDay,
			GregorianCalendar modifiedDate) {
		String[] time = timeInfo.split(":");
		boolean invalidLength = time.length > 2;
		if (invalidLength) {
			throw new IllegalArgumentException("Invalid length in colon time format");
		}
		
		int hour = Integer.parseInt(time[0]) + (isDay ? 0 : 12);
		if (hour == 24 || hour == 12)
			hour -= 12;
		int minute = Integer.parseInt(time[1]);
		
		modifiedDate.set(Calendar.HOUR_OF_DAY, hour);
		modifiedDate.set(Calendar.MINUTE, minute);
		checkTimeBound(modifiedDate);
	}
	
	/**
	 * This function is used to update time in 12-hour format with dot
	 * 
	 * @param timeInfo
	 *            the time info String
	 * @param isDay
	 *            indicator whether it is 'am' or 'pm'
	 * @param modifiedDate
	 *            the modified GregorianCalendar temporary object
	 */
	private void updateTimeWithDot(String timeInfo, boolean isDay,
			GregorianCalendar tempDate) {
		String[] time = timeInfo.split("\\.");
		boolean invalidLength = time.length > 2;
		if (invalidLength) {
			throw new IllegalArgumentException("Invalid length in dot time format");
		}
		
		int hour = Integer.parseInt(time[0]) + (isDay ? 0 : 12);
		if (hour == 24 || hour == 12) {
			hour -= 12;
		}
		int minute = Integer.parseInt(time[1]);
		
		tempDate.set(Calendar.HOUR_OF_DAY, hour);
		tempDate.set(Calendar.MINUTE, minute);
		checkTimeBound(tempDate);
	}
	
	/**
	 * This function is used to update time in 12-hour format without any signs
	 * 
	 * @param timeInfo
	 *            the time info String
	 * @param isDay
	 *            indicator whether it is 'am' or 'pm'
	 * @param modifiedDate
	 *            the modified GregorianCalendar temporary object
	 */
	private void updateTimeWithoutSign(String timeInfo, boolean isDay,
			GregorianCalendar tempDate) {
		int timeInNumberFormat = Integer.parseInt(timeInfo);
		
		boolean hasOnlyHour = timeInfo.length() < 3;
		boolean hasHourAndMinute = timeInfo.length() == 3 || timeInfo.length() == 4;
		if (hasOnlyHour) {
			int hour = timeInNumberFormat + (isDay ? 0 : 12);
			if (hour == 24 || hour == 12){
				hour -= 12;
			}
			
			tempDate.set(Calendar.HOUR_OF_DAY, hour);
			tempDate.set(Calendar.MINUTE, 0);
			checkTimeBound(tempDate);
		} else if (hasHourAndMinute) {
			int hour = timeInNumberFormat / 100 + (isDay ? 0 : 12);
			if (hour == 24 || hour == 12){
				hour -= 12;
			}
			int minute = timeInNumberFormat % 100;
			
			tempDate.set(Calendar.HOUR_OF_DAY, hour);
			tempDate.set(Calendar.MINUTE, minute);
			checkTimeBound(tempDate);
		} else {
			throw new IllegalArgumentException("Invalid length in time without sign format");
		}
	}
	
	/**
	 * This function is used to update the time in 24-hour format
	 * 
	 * @param infos
	 *            the String array containing infos of target of conversion
	 * @param tempDate
	 *            the modified GregorianCalendar temporary object
	 * @param numElements
	 *            number of unprocessed elements left in the array
	 * @return the number of elements left unprocessed in the array
	 */
	private int updateTimeWith24Format(String[] infos,
			GregorianCalendar tempDate, int numElements) {
		int index = getIndexOfColon(infos);
		String[] time = infos[index].split(":");
		boolean invalidLength = time.length > 2;
		if (invalidLength){
			throw new IllegalArgumentException("Invalid in time with colon format");
		}
		
		int hour = Integer.parseInt(time[0]);
		int minute = Integer.parseInt(time[1]);

		tempDate.set(Calendar.HOUR_OF_DAY, hour);
		tempDate.set(Calendar.MINUTE, minute);
		checkTimeBound(tempDate);
		
		return numElements - 1;
	}
	
	/**
	 * This function is used to reset the hour, minute and second back to 0
	 * But if the time is 'tonight', hour and minute will be reseted back to 23 and 59 respectively
	 */
	private void resetTime(GregorianCalendar targetDate) {
		targetDate.set(Calendar.HOUR_OF_DAY, 0);
		targetDate.set(Calendar.MINUTE, 0);
		targetDate.set(Calendar.SECOND, 0);
		
		boolean isTonight = dateInfo.contains("tonight");
		if (isTonight) {
			targetDate.set(Calendar.HOUR_OF_DAY, 23);
			targetDate.set(Calendar.MINUTE, 59);
		}
	}

	/**
	 * This function is used to check whether the time is out of bounds
	 * If yes, it will throw an IllegalArgumentException with indicated message
	 */
	private void checkTimeBound(GregorianCalendar tempDate) {
		try {
			tempDate.get(Calendar.HOUR_OF_DAY);
			tempDate.get(Calendar.MINUTE);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Out of bounds");
		}
	}

	// Check whether the dateInfo may contain TIME format
	private boolean hasTimeFormat() {
		return dateInfo.contains(":") || dateInfo.contains("am")|| dateInfo.contains("pm");
	}
	
	// Check if the string can be converted into an integer
	private boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	// Check whether the time of sourceDate is just less than 6 hours before the current time
	private boolean lessThan6Hours() {
		return (sourceDate.getTimeInMillis() - currentDate.getTimeInMillis()) <= SIX_HOURS_IN_MILLIS;
	}
	
	/**
	 * This function is used to check whether the user did input the time
	 */
	private boolean hasTime(boolean isStartDate) {
		boolean isMidnight = sourceDate.get(Calendar.HOUR_OF_DAY) == 23
				&& sourceDate.get(Calendar.MINUTE) == 59;
		boolean isNewDay = sourceDate.get(Calendar.HOUR_OF_DAY) == 0
				&& sourceDate.get(Calendar.MINUTE) == 0;
		
		if (isStartDate && isNewDay) {
			return false;
		} else if (!isStartDate && isMidnight) {
			return false;
		}

		return true;
	}
	
	/**
	 * This function is used to check whether the date is today
	 */
	private boolean isToday() {
		boolean isCurrentYear = sourceDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR);
		boolean isCurrentMonth = sourceDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH);
		boolean isCurrentDate = sourceDate.get(Calendar.DATE) == currentDate.get(Calendar.DATE);
		
		return isCurrentYear && isCurrentMonth && isCurrentDate;
	}
	
	/**
	 * This function is used to check whether the time is tonight
	 */
	private boolean isTonight() {
		boolean isCurrentYear = sourceDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR);
		boolean isCurrentMonth = sourceDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH);
		boolean isCurrentDate = sourceDate.get(Calendar.DATE) == currentDate.get(Calendar.DATE);
		boolean isMidnight = (sourceDate.get(Calendar.HOUR_OF_DAY) == 23 && sourceDate.get(Calendar.MINUTE) == 59);
		
		return isCurrentYear && isCurrentMonth && isCurrentDate && isMidnight;
	}
}
