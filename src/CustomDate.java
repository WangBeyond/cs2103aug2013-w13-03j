import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

// Note: 3 public functions are getTime(), constructor and convert()
public class CustomDate {
	final public static int VALID = 1;
	final public static int INVALID = -1;
	final public static int OUT_OF_BOUNDS = 0;

	private GregorianCalendar targetDate;
	private String dateInfo;
	private static GregorianCalendar currentDate;

	// Constructor
	public CustomDate() {
		targetDate = new GregorianCalendar();
	}

	public CustomDate(String s) {
		targetDate = new GregorianCalendar();
		convert(s);
	}

	// compareTo Function
	public static int compare(CustomDate date1, CustomDate date2) {
		int difference = 0;
		if (date1 == null && date2 == null)
			return 0;
		if (date1 == null && date2 != null)
			return -1;
		if (date1 != null && date2 == null)
			return 1;

		for (int i = 0; i < 5; i++) {
			if (i == 0)
				difference = date1.getYear() - date2.getYear();
			else if (i == 1)
				difference = date1.getMonth() - date2.getMonth();
			else if (i == 2)
				difference = date1.getDate() - date2.getDate();
			else if (i == 3)
				difference = date1.getHour() - date2.getHour();
			else
				difference = date1.getMinute() - date2.getMinute();
			if (difference != 0)
				return difference;
		}
		return difference;
	}

	public boolean dateEqual(CustomDate other) {
		return this.getYear() == other.getYear()
				&& this.getMonth() == other.getMonth()
				&& this.getDate() == other.getDate();
	}

	// GET methods
	private int getYear() {
		return targetDate.get(Calendar.YEAR);
	}

	private int getMonth() {
		return targetDate.get(Calendar.MONTH);
	}

	private int getDate() {
		return targetDate.get(Calendar.DATE);
	}

	public int getHour() {
		return targetDate.get(Calendar.HOUR_OF_DAY);
	}

	public void setHour(int hour) {
		targetDate.set(Calendar.HOUR_OF_DAY, hour);
	}

	public int getMinute() {
		return targetDate.get(Calendar.MINUTE);
	}

	public void setMinute(int minute) {
		targetDate.set(Calendar.MINUTE, minute);
	}

	public long getTimeInMillis() {
		return targetDate.getTimeInMillis();
	}

	public Date getTime() {
		return targetDate.getTime();
	}

	/* Convert Function */
	public int convert(String s) {
		dateInfo = s.toLowerCase();
		String[] infos = dateInfo.split(" ");

		updateCurrentDate();

		boolean invalidLength = infos.length > 4 || infos.length == 0;
		if (invalidLength) {
			return INVALID;
		}

		try {
			int numElements = infos.length;
			GregorianCalendar tempDate = new GregorianCalendar();
			tempDate.setLenient(false);

			if (hasDateFormat()) {
				numElements = updateDate(infos, tempDate, numElements);
			} else if (hasDayFormat()) {
				numElements = updateDay(infos, tempDate, numElements);
			} else if (infos.length > 2) {
				return INVALID;
			}

			if (hasTimeFormat()) {
				numElements = updateTime(infos, tempDate, numElements);
			} else {
				if (infos.length > 2)
					return INVALID;
				resetTime(tempDate);
			}

			if (numElements > 0) {
				return INVALID;
			}

			targetDate = tempDate;
			return VALID;
		} catch (Exception e) {
			if (e.getMessage().equals("Out of bounds"))
				return OUT_OF_BOUNDS;
			return INVALID;
		}
	}

	public static void updateCurrentDate() {
		currentDate = new GregorianCalendar();
	}

	private int updateDate(String[] infos, GregorianCalendar targetDate,
			int numElements) {
		int startIndex = getStartIndexOfDate(infos);
		if (hasMonthWord()) {
			numElements = updateDateWithMonth(infos, targetDate, numElements,
					startIndex);
		} else if (dateInfo.contains("/")) {
			numElements = updateDateWithSlash(infos, targetDate, numElements,
					startIndex);
		} else {
			numElements = updateDateWithDash(infos, targetDate, numElements,
					startIndex);
		}

		return numElements;

	}

	private static int updateDateWithMonth(String[] infos,
			GregorianCalendar targetDate, int numElements, int startIndex) {
		int date = Integer.parseInt(infos[startIndex]);
		int month = getMonth(infos[startIndex + 1]);

		targetDate.set(Calendar.MONTH, month);
		targetDate.set(Calendar.DATE, date);
		checkDateBound(targetDate);

		return numElements - 2;
	}

	private static int updateDateWithSlash(String[] infos,
			GregorianCalendar targetDate, int numElements, int startIndex) {
		String[] numbers = infos[startIndex].split("/");
		boolean invalidLength = numbers.length != 3 && numbers.length != 2;
		if (invalidLength)
			throw new IllegalArgumentException("Invalid length in slash format");

		int month = getMonth(numbers[1]);
		targetDate.set(Calendar.MONTH, month);

		int date = Integer.parseInt(numbers[0]);
		targetDate.set(Calendar.DATE, date);

		checkDateBound(targetDate);
		int year = (numbers.length == 3) ? Integer.parseInt(numbers[2])
				: currentDate.get(Calendar.YEAR);

		targetDate.set(Calendar.YEAR, year);

		return numElements - 1;
	}

	private static int updateDateWithDash(String[] infos,
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
		int year = (numbers.length == 3) ? Integer.parseInt(numbers[2])
				: currentDate.get(Calendar.YEAR);

		targetDate.set(Calendar.YEAR, year);

		return numElements - 1;
	}

	private static void checkDateBound(GregorianCalendar targetDate) {
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
			if (temp != -1) {
				if (infos[temp].equals("am") || infos[temp].equals("pm"))
					return (temp <= 1) ? temp + 1 : 0;
				else
					return (temp >= 1) ? 0 : temp + 1;
			} else {
				temp = getIndexOfColon(infos);
				return (temp == 0) ? temp + 1 : 0;
			}
		}
		return 0;
	}

	private static int getIndexOfTime(String[] infos) {
		for (int i = 0; i < infos.length; i++)
			if (infos[i].contains("am") || infos[i].contains("pm"))
				return i;
		return INVALID;
	}

	private static int getIndexOfColon(String[] infos) {
		for (int i = 0; i < infos.length; i++)
			if (infos[i].contains(":"))
				return i;
		return INVALID;
	}

	private int updateDay(String[] infos, GregorianCalendar targetDate,
			int numElements) {
		int startIndex = getStartIndexOfDate(infos);
		if (isTomorrowKeyWord(infos[startIndex])) {
			numElements = updateTomorrow(targetDate, numElements);
		} else if (isTodayKeyWord(infos[startIndex])) {
			numElements--;
		} else if (hasDayWord()) {
			numElements = updateDateWithDay(infos, targetDate, numElements,
					startIndex);
		}
		return numElements;
	}

	private static int updateTomorrow(GregorianCalendar targetDate,
			int numElements) {
		targetDate.set(Calendar.DATE, targetDate.get(Calendar.DATE) + 1);
		return numElements - 1;
	}

	private static int updateDateWithDay(String[] infos,
			GregorianCalendar targetDate, int numElements, int startIndex) {
		boolean hasNext = infos[startIndex].equals("next");
		int currentDay = currentDate.get(Calendar.DAY_OF_WEEK);
		int targetDay = (hasNext == true) ? getDay(infos[startIndex + 1])
				: getDay(infos[startIndex]);
		if (targetDay == 1)
			targetDay += 7;
		if (currentDay == 1)
			currentDay += 7;

		long difference = targetDay - currentDay
				+ ((hasNext || targetDay < currentDay) ? 7 : 0);
		targetDate.setTimeInMillis(currentDate.getTimeInMillis() + difference
				* 24 * 60 * 60 * 1000);

		if (hasNext == true) {
			numElements -= 2;
		} else {
			numElements--;
		}
		return numElements;
	}

	private int updateTime(String[] infos, GregorianCalendar targetDate,
			int numElements) {
		int index = getIndexOfTime(infos);

		if (index != INVALID) {
			numElements = updateTimeWith12Format(infos, targetDate, index,
					numElements);
		} else {
			numElements = updateTimeWith24Format(infos, targetDate, numElements);
		}

		targetDate.set(Calendar.SECOND, 0);
		return numElements;
	}

	private static int updateTimeWith12Format(String[] infos,
			GregorianCalendar targetDate, int index, int numElements) {
		boolean isDay;
		String timeInfo;
		boolean hasSpace;

		if (infos[index].equals("am") || infos[index].equals("pm")) {
			isDay = infos[index].equals("am");
			timeInfo = infos[index - 1];
			hasSpace = true;
		} else {
			isDay = infos[index].substring(infos[index].length() - 2).equals(
					"am");
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

		if (hasSpace)
			return numElements - 2;
		else
			return numElements - 1;
	}

	private static void updateTimeWithColon(String timeInfo, boolean isDay,
			GregorianCalendar targetDate) {
		String[] time = timeInfo.split(":");
		if (time.length > 2)
			throw new IllegalArgumentException(
					"Invalid length in colon time format");

		int hour = Integer.parseInt(time[0]) + (isDay ? 0 : 12);
		if (hour == 24 || hour == 12)
			hour -= 12;
		int minute = Integer.parseInt(time[1]);
		targetDate.set(Calendar.HOUR_OF_DAY, hour);
		targetDate.set(Calendar.MINUTE, minute);
		checkTimeBound(targetDate);
	}

	private static void updateTimeWithDot(String timeInfo, boolean isDay,
			GregorianCalendar targetDate) {
		String[] time = timeInfo.split("\\.");
		if (time.length > 2)
			throw new IllegalArgumentException(
					"Invalid length in dot time format");

		int hour = Integer.parseInt(time[0]) + (isDay ? 0 : 12);
		if (hour == 24 || hour == 12)
			hour -= 12;
		int minute = Integer.parseInt(time[1]);
		targetDate.set(Calendar.HOUR_OF_DAY, hour);
		targetDate.set(Calendar.MINUTE, minute);
		checkTimeBound(targetDate);
	}

	private static void updateTimeWithoutSign(String timeInfo, boolean isDay,
			GregorianCalendar targetDate) {
		int s = Integer.parseInt(timeInfo);
		if (timeInfo.length() < 3) {
			int hour = s + (isDay ? 0 : 12);
			if (hour == 24 || hour == 12)
				hour -= 12;
			targetDate.set(Calendar.HOUR_OF_DAY, hour);
			targetDate.set(Calendar.MINUTE, 0);
			checkTimeBound(targetDate);
		} else if (timeInfo.length() == 3 || timeInfo.length() == 4) {
			int hour = s / 100 + (isDay ? 0 : 12);
			if (hour == 24 || hour == 12)
				hour -= 12;
			int minute = s % 100;
			targetDate.set(Calendar.HOUR_OF_DAY, hour);
			targetDate.set(Calendar.MINUTE, minute);
			checkTimeBound(targetDate);
		} else {
			throw new IllegalArgumentException(
					"Invalid length in time without sign format");
		}
	}

	private static void checkTimeBound(GregorianCalendar targetDate) {
		try {
			targetDate.get(Calendar.HOUR_OF_DAY);
			targetDate.get(Calendar.MINUTE);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Out of bounds");
		}
	}

	private static int updateTimeWith24Format(String[] infos,
			GregorianCalendar targetDate, int numElements) {
		int index = getIndexOfColon(infos);
		String[] time = infos[index].split(":");
		if (time.length > 2)
			throw new IllegalArgumentException(
					"Invalid in time with colon format");

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
	}

	private static boolean isTomorrowKeyWord(String s) {
		return s.equals("tomorrow") || s.equals("tmr");
	}

	private static boolean isTodayKeyWord(String s) {
		return s.equals("today");
	}

	private boolean hasTimeFormat() {
		return dateInfo.contains(":") || dateInfo.contains("am")
				|| dateInfo.contains("pm");
	}

	private boolean hasDayFormat() {
		boolean hasToday = dateInfo.contains("today");
		boolean hasTomorrow = dateInfo.contains("tomorrow")
				|| dateInfo.contains("tmr");

		return hasToday || hasTomorrow || hasDayWord();
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

	private static int getDay(String s) {
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

	private static boolean isMonday(String s) {
		return s.equals("monday") || s.equals("mon");
	}

	private static boolean isTuesday(String s) {
		return s.equals("tuesday") || s.equals("tue");
	}

	private static boolean isWednesday(String s) {
		return s.equals("wednesday") || s.equals("wed");
	}

	private static boolean isThursday(String s) {
		return s.equals("thursday") || s.equals("thu");
	}

	private static boolean isFriday(String s) {
		return s.equals("friday") || s.equals("fri");
	}

	private static boolean isSaturday(String s) {
		return s.equals("saturday") || s.equals("sat");
	}

	private static boolean isSunday(String s) {
		return s.equals("sunday") || s.equals("sun");
	}

	private static int getMonth(String s) {
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

	private static String getMonthString(int i) {
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

	private static boolean isJanuary(String s) {
		return s.equals("jan") || s.equals("1") || s.equals("january");
	}

	private static boolean isFebruary(String s) {
		return s.equals("feb") || s.equals("2") || s.equals("february");
	}

	private static boolean isMarch(String s) {
		return s.equals("mar") || s.equals("3") || s.equals("march");
	}

	private static boolean isApril(String s) {
		return s.equals("apr") || s.equals("4") || s.equals("april");
	}

	private static boolean isMay(String s) {
		return s.equals("may") || s.equals("5");
	}

	private static boolean isJune(String s) {
		return s.equals("june") || s.equals("6");
	}

	private static boolean isJuly(String s) {
		return s.equals("july") || s.equals("7");
	}

	private static boolean isAugust(String s) {
		return s.equals("aug") || s.equals("8") || s.equals("august");
	}

	private static boolean isSeptember(String s) {
		return s.equals("sep") || s.equals("9") || s.equals("september");
	}

	private static boolean isOctober(String s) {
		return s.equals("oct") || s.equals("10") || s.equals("october");
	}

	private static boolean isNovember(String s) {
		return s.equals("nov") || s.equals("11") || s.equals("november");
	}

	private static boolean isDecember(String s) {
		return s.equals("dec") || s.equals("12") || s.equals("december");
	}

	private static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public boolean lessThan6Hours() {
		return (targetDate.getTimeInMillis() - currentDate.getTimeInMillis()) <= 21600000;
	}

	public boolean beforeCurrentTime() {
		return (currentDate.getTimeInMillis() - targetDate.getTimeInMillis()) > 0;
	}

	public boolean hasTime(boolean isStartDate) {
		if (isStartDate && targetDate.get(Calendar.HOUR_OF_DAY) == 0
				&& targetDate.get(Calendar.MINUTE) == 0)
			return false;
		else if (!isStartDate && targetDate.get(Calendar.HOUR_OF_DAY) == 23
				&& targetDate.get(Calendar.MINUTE) == 59)
			return false;

		return true;
	}

	public boolean isToday() {
		return targetDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
				&& targetDate.get(Calendar.MONTH) == currentDate
						.get(Calendar.MONTH)
				&& targetDate.get(Calendar.DATE) == currentDate
						.get(Calendar.DATE);
	}

	public String toString(boolean isStartDate) {
		updateCurrentDate();
		boolean hasTime = hasTime(isStartDate);
		if (beforeCurrentTime()) {
			if (!isStartDate)
				return "OVERDUE";
		} else {
			if (lessThan6Hours()) {
				int remainingTime = (int) (targetDate.getTimeInMillis() - currentDate
						.getTimeInMillis()) / (1000 * 60);
				int remainingHours = remainingTime / 60;
				int remainingMinutes = remainingTime % 60;
				return remainingHours + "h " + remainingMinutes + "m";
			}
		}
		String result = "";
		if (isToday())
			result += "Today";
		else {
			result += targetDate.get(Calendar.DATE) + " "
					+ getMonthString(targetDate.get(Calendar.MONTH));
			if (targetDate.get(Calendar.YEAR) != currentDate.get(Calendar.YEAR))
				result += " " + targetDate.get(Calendar.YEAR);
		}
		if (hasTime) {
			DecimalFormat df = new DecimalFormat("00");
			result += "\n " + targetDate.get(Calendar.HOUR_OF_DAY) + ":"
					+ df.format(targetDate.get(Calendar.MINUTE));
		}
		return result;
	}
}
