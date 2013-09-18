import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

// Note: 3 public functions are getTime(), constructor and convert()
public class CustomDate implements Comparable<CustomDate>{
	final public static int VALID = 1;
	final public static int INVALID = -1;

	private GregorianCalendar targetDate;
	private String dateInfo;
	private GregorianCalendar currentDate;

	public CustomDate() {
		targetDate = new GregorianCalendar();
		currentDate = new GregorianCalendar();
	}
	
	public int compareTo(CustomDate other){
		int difference = 0;
		for(int i = 0; i < 5; i++){
			if(i == 0)
				difference = this.getYear() - other.getYear();
			else if(i == 1)
				difference = this.getMonth() - other.getMonth();
			else if(i == 2)
				difference = this.getDate() - other.getDate();
			else if(i == 3)
				difference = this.getHour() - other.getHour();
			else
				difference = this.getMinute() - other.getMinute();
			if(difference != 0)
				return difference;
		}
		return difference;
		
	}
	
	private int getYear(){
		return targetDate.get(Calendar.YEAR);
	}
	
	private int getMonth(){
		return targetDate.get(Calendar.MONTH);
	}
	
	private int getDate(){
		return targetDate.get(Calendar.DATE);
	}
	
	private int getHour(){
		return targetDate.get(Calendar.HOUR);
	}
	
	private int getMinute(){
		return targetDate.get(Calendar.MINUTE);
	}
	
	
	public Date getTime() {
		return targetDate.getTime();
	}
	
	public int convert(String s) {
		dateInfo = s.toLowerCase();
		currentDate = new GregorianCalendar();

		String[] infos = dateInfo.split(" ");

		if (infos.length > 4 || infos.length == 0)
			return INVALID;

		try {
			Integer n = new Integer(infos.length);
			GregorianCalendar tempDate = new GregorianCalendar();
			if (hasDateFormat()) {
				n = updateDate(infos, tempDate, n);
			} else if (hasDayFormat()) {
				n = updateDay(infos, tempDate, n);
			} else if (infos.length > 2)
				return INVALID;

			if (hasTimeFormat())
				n = updateTime(infos, tempDate, n);
			else {
				if (infos.length > 2)
					return INVALID;
				resetTime(tempDate);
			}

			if (n > 0)
				return INVALID;
			targetDate = tempDate;
			return VALID;
		} catch (IllegalArgumentException e) {
			return INVALID;
		}
	}

	private int updateDate(String[] infos, GregorianCalendar targetDate,
			Integer n) {
		int startIndex = 0;
		if (hasTimeFormat()) {
			int temp = indexOfTime(infos);
			if (temp != -1)
				startIndex = (temp == 1) ? temp + 1 : 0;
			else {
				temp = indexOfColon(infos);
				startIndex = (temp == 0) ? temp + 1 : 0;
			}
		}

		try {
			if (hasMonthWord()) {
				targetDate.set(Calendar.MONTH, getMonth(infos[startIndex + 1]));
				targetDate.set(Calendar.DATE,
						Integer.parseInt(infos[startIndex]));
				if (targetDate.get(Calendar.MONTH) < currentDate
						.get(Calendar.MONTH)
						|| (targetDate.get(Calendar.MONTH) == currentDate
								.get(Calendar.MONTH) && targetDate
								.get(Calendar.DATE) < currentDate
								.get(Calendar.DATE)))
					targetDate.set(Calendar.YEAR,
							currentDate.get(Calendar.YEAR) + 1);
				n -= 2;
			} else if (dateInfo.contains("/")) {
				String[] date = infos[startIndex].split("/");
				if (date.length == 3)
					targetDate.set(Integer.parseInt(date[2]),
							getMonth(date[1]), Integer.parseInt(date[0]));
				else if (date.length == 2) {
					targetDate.set(Calendar.DATE, Integer.parseInt(date[0]));
					targetDate.set(Calendar.MONTH, getMonth(date[1]));
					if (targetDate.get(Calendar.MONTH) < currentDate
							.get(Calendar.MONTH)
							|| (targetDate.get(Calendar.MONTH) == currentDate
									.get(Calendar.MONTH) && targetDate
									.get(Calendar.DATE) < currentDate
									.get(Calendar.DATE)))
						targetDate.set(Calendar.YEAR,
								currentDate.get(Calendar.YEAR) + 1);
				} else
					throw new IllegalArgumentException();
				n--;
			} else {
				String[] date = infos[0].split("-");
				if (date.length == 3)
					targetDate.set(Integer.parseInt(date[2]),
							getMonth(date[1]), Integer.parseInt(date[0]));
				else if (date.length == 2) {
					targetDate.set(Calendar.DATE, Integer.parseInt(date[0]));
					targetDate.set(Calendar.MONTH, getMonth(date[1]));
					if (targetDate.get(Calendar.MONTH) < currentDate
							.get(Calendar.MONTH)
							|| (targetDate.get(Calendar.MONTH) == currentDate
									.get(Calendar.MONTH) && targetDate
									.get(Calendar.DATE) < currentDate
									.get(Calendar.DATE)))
						targetDate.set(Calendar.YEAR,
								currentDate.get(Calendar.YEAR) + 1);
				} else
					throw new IllegalArgumentException();
				n--;

			}
			return n;
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
	}

	private int indexOfTime(String[] infos) {
		for (int i = 1; i < infos.length; i++)
			if (infos[i].equals("am") || infos[i].equals("pm"))
				return i;
		return INVALID;
	}

	private int indexOfColon(String[] infos) {
		for (int i = 0; i < infos.length; i++)
			if (infos[i].contains(":"))
				return i;
		return INVALID;
	}

	private int updateDay(String[] infos, GregorianCalendar targetDate,
			Integer n) {

		int currentDay = currentDate.get(Calendar.DAY_OF_WEEK);

		int startIndex = 0;
		if (hasTimeFormat()) {
			int temp = indexOfTime(infos);
			if (temp != -1)
				startIndex = (temp == 1) ? temp + 1 : 0;
			else {
				temp = indexOfColon(infos);
				startIndex = (temp == 0) ? temp + 1 : 0;
			}
		}

		if (infos[startIndex].equals("tomorrow")
				|| infos[startIndex].equals("tmr")) {
			targetDate.set(currentDate.get(Calendar.YEAR),
					currentDate.get(Calendar.MONTH),
					currentDate.get(Calendar.DATE) + 1);
			n--;
		} else if (infos[startIndex].equals("today")) {
			targetDate.set(currentDate.get(Calendar.YEAR),
					currentDate.get(Calendar.MONTH),
					currentDate.get(Calendar.DATE));
			n--;
		} else if (hasDayWord()) {
			boolean hasNext = infos[startIndex].equals("next");
			int targetDay = (hasNext == true) ? getDay(infos[startIndex + 1])
					: getDay(infos[startIndex]);
			targetDate.set(currentDate.get(Calendar.YEAR),
					currentDate.get(Calendar.MONTH),
					currentDate.get(Calendar.DATE) + targetDay - currentDay
							+ ((hasNext || targetDay < currentDay) ? 7 : 0));
			if (hasNext == true)
				n -= 2;
			else
				n--;
		}
		return n;
	}

	private int updateTime(String[] infos, GregorianCalendar targetDate,
			Integer n) {
		int index = -1;
		for (int i = 1; i < infos.length; i++) {
			if (infos[i].equals("am") || infos[i].equals("pm")) {
				index = i;
				break;
			}
		}

		if (index != -1) {
			String[] time;
			if (infos[index - 1].contains(":")) {
				time = infos[index - 1].split(":");
				targetDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0])
						+ (infos[index].equals("pm") ? 12 : 0));
				targetDate.set(Calendar.MINUTE, Integer.parseInt(time[1]));
			} else if (infos[index - 1].contains(".")) {
				time = infos[index - 1].split("\\.");
				targetDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0])
						+ (infos[index].equals("pm") ? 12 : 0));
				targetDate.set(Calendar.MINUTE, Integer.parseInt(time[1]));
			} else {
				try {
					int s = Integer.parseInt(infos[index - 1]);
					if (infos[index - 1].length() < 3) {
						targetDate.set(Calendar.HOUR_OF_DAY,
								s + (infos[index].equals("pm") ? 12 : 0));
						targetDate.set(Calendar.MINUTE, 0);
					} else if (infos[index - 1].length() == 3
							|| infos[index - 1].length() == 4) {
						targetDate.set(Calendar.HOUR_OF_DAY, s / 100
								+ (infos[index].equals("pm") ? 12 : 0));
						targetDate.set(Calendar.MINUTE, s % 100);
					}
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException();
				}
			}
			n -= 2;
		} else {
			String[] time = new String[2];
			for (int i = 0; i < infos.length; i++) {
				if (infos[i].contains(":")) {
					time = infos[i].split(":");
					break;
				}
			}
			targetDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
			targetDate.set(Calendar.MINUTE, Integer.parseInt(time[1]));
			n--;
		}

		targetDate.set(Calendar.SECOND, 0);
		return n;
	}

	private void resetTime(GregorianCalendar targetDate) {
		targetDate.set(Calendar.HOUR_OF_DAY, 0);
		targetDate.set(Calendar.MINUTE, 0);
		targetDate.set(Calendar.SECOND, 0);
	}

	private int getDay(String s) {
		if (s.equals("monday") || s.equals("mon"))
			return Calendar.MONDAY;
		else if (s.equals("tuesday") || s.equals("tue"))
			return Calendar.TUESDAY;
		else if (s.equals("wednesday") || s.equals("wed"))
			return Calendar.WEDNESDAY;
		else if (s.equals("thursday") || s.equals("thu"))
			return Calendar.THURSDAY;
		else if (s.equals("friday") || s.equals("fri"))
			return Calendar.FRIDAY;
		else if (s.equals("saturday") || s.equals("sat"))
			return Calendar.SATURDAY;
		else if (s.equals("sunday") || s.equals("sun"))
			return Calendar.SUNDAY;
		else
			throw new IllegalArgumentException("Wrong Format");
	}

	private boolean hasTimeFormat() {
		return dateInfo.contains(":") || dateInfo.contains("am")
				|| dateInfo.contains("pm");
	}

	private boolean hasDayFormat() {
		boolean hasToday = dateInfo.contains("today");
		boolean hasTomorrow = dateInfo.contains("tomorrow")
				|| dateInfo.contains("tmr");
		boolean hasDayWord = hasDayWord();
		return hasToday || hasTomorrow || hasDayWord;
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
		boolean hasMonthWord = hasMonthWord();

		return hasSlash || hasDash || hasMonthWord;
	}

	private boolean hasMonthWord() {
		return dateInfo.contains("jan") || dateInfo.contains("feb")
				|| dateInfo.contains("mar") || dateInfo.contains("apr")
				|| dateInfo.contains("may") || dateInfo.contains("june")
				|| dateInfo.contains("jul") || dateInfo.contains("aug")
				|| dateInfo.contains("sep") || dateInfo.contains("oct")
				|| dateInfo.contains("nov") || dateInfo.contains("dec");
	}

	private int getMonth(String s) {
		if (s.equals("jan") || s.equals("1") || s.equals("january"))
			return Calendar.JANUARY;
		else if (s.equals("feb") || s.equals("2") || s.equals("february"))
			return Calendar.FEBRUARY;
		else if (s.equals("mar") || s.equals("3") || s.equals("march"))
			return Calendar.MARCH;
		else if (s.equals("apr") || s.equals("4") || s.equals("april"))
			return Calendar.APRIL;
		else if (s.equals("may") || s.equals("5"))
			return Calendar.MAY;
		else if (s.equals("june") || s.equals("6"))
			return Calendar.JUNE;
		else if (s.equals("july") || s.equals("7"))
			return Calendar.JULY;
		else if (s.equals("aug") || s.equals("8") || s.equals("august"))
			return Calendar.AUGUST;
		else if (s.equals("sep") || s.equals("9") || s.equals("september"))
			return Calendar.SEPTEMBER;
		else if (s.equals("oct") || s.equals("10") || s.equals("october"))
			return Calendar.OCTOBER;
		else if (s.equals("nov") || s.equals("11") || s.equals("november"))
			return Calendar.NOVEMBER;
		else if (s.equals("dec") || s.equals("12") || s.equals("december"))
			return Calendar.DECEMBER;
		else {
			throw new IllegalArgumentException("Wrong Format");
		}
	}
}
