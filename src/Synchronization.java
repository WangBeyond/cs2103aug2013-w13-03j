import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javafx.collections.ObservableList;

import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.calendar.HiddenProperty;
import com.google.gdata.data.calendar.TimeZoneProperty;
import com.google.gdata.data.extensions.Recurrence;
import com.google.gdata.data.extensions.Reminder;
import com.google.gdata.data.extensions.Reminder.Method;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class Synchronization  {

	/* messages */
	private static final String CALENDAR_TITLE = "iDo";
	private static final String CALENDAR_SUMMARY = "This calendar synchronizes with iDo Task Manager.";
	private static final String SERVICE_NAME = "sg.edu.nus.cs2103aug2013-w13-03j";
	private static final int REMINDER_MINUTES = 30;

	/* use username and password to login */
	String username = null;
	String password = null;

	/* model */
	Model model;

	/* sync store */
	SettingsStorage syncStore;

	/* calendar id */
	String calendarId = null;

	/* calendar service */
	CalendarService service;

	/* The base URL for a user's calendar metafeed (needs a username appended). */
	private static final String METAFEED_URL_BASE = "https://www.google.com/calendar/feeds/";

	// The string to add to the user's metafeedUrl to access the owncalendars
	// feed.
	private static final String OWNCALENDARS_FEED_URL_SUFFIX = "/owncalendars/full";

	private static final String EVENT_FEED_URL_SUFFIX = "/private/full";

	URL owncalendarsFeedUrl = null;
	URL eventFeedUrl = null;

	/* a list of event entry from Google calendar */
	private List<CalendarEventEntry> eventEntry = new ArrayList<CalendarEventEntry>();

	public Synchronization(Model m) {
		model = m;
	}

	public void setUsernameAndPassword(String n, String p) {
		username = n;
		password = p;
	}

	public void setSyncStore(SettingsStorage s) {
		syncStore = s;
	}

	public String execute() {

		try {
			initService();
		} catch (AuthenticationException e) {
			return Command.MESSAGE_SYNC_INVALID_USERNAME_PASSWORD;
		}

		// form feed url
		if (eventFeedUrl == null) {
			try {
				eventFeedUrl = formEventFeedUrl(service);
			} catch (IOException | ServiceException e) {
				return Command.MESSAGE_SYNC_FAIL_TO_CREATE_CALENDAR;
			}
		}

		try {
			// get all events from Google calendar
			eventEntry = getEventsFromGCal(service, eventFeedUrl);

			// sync newly added tasks to GCal
			syncNewTasksToGCal(service, model, eventFeedUrl);
		} catch (ServiceException | IOException e) {
			e.printStackTrace();
		}

		// update Task
		try {
			updateUnchangedTasks(service, model, eventEntry, eventFeedUrl);
		} catch (ServiceException e) {
			System.out.println("fail to update");
		} catch (IOException e) {
			System.out.println("fail to update");
		}
		System.out.println("updated unchanged task.");

		try {
			// delete events on GCal which have been deleted locally
			syncDeletedTasksToGCal(service, model, eventEntry, eventFeedUrl);
		} catch (ServiceException | IOException e) {
			return Command.MESSAGE_SYNC_SERVICE_STOPPED;
		}

		// get all events from Google calendar
		try {
			eventEntry = getEventsFromGCal(service, eventFeedUrl);
		} catch (IOException | ServiceException e) {
			return Command.MESSAGE_SYNC_SERVICE_STOPPED;
		}

		// delete tasks locally which have been deleted on GCal
		deleteTasksLocally(eventEntry, model);

		// add tasks locally which have been added on GCal
		addEventsLocally(eventEntry, model);
		return Command.MESSAGE_SYNC_SUCCESSFUL;
	}

	private void initService() throws AuthenticationException {
		// create a new service
		service = new CalendarService(SERVICE_NAME);

		// authenticate using ClientLogin
		service.setUserCredentials(username, password);
	}

	URL formEventFeedUrl(CalendarService service) throws ServiceException,
			IOException {
		URL owncalUrl = new URL(METAFEED_URL_BASE + username
				+ OWNCALENDARS_FEED_URL_SUFFIX);
		String calId = isCalendarExist(service, owncalUrl);
		if (calId == null) {
			CalendarEntry calendar = createCalendar(service, owncalUrl);
			calId = trimId(calendar.getId());
		}
		return new URL(METAFEED_URL_BASE + calId + EVENT_FEED_URL_SUFFIX);
	}

	private String trimId(String id) {
		String[] temp = id.trim().split("/");
		return temp[temp.length - 1].toString();
	}

	void syncNewTasksToGCal(CalendarService service, Model model, URL feedUrl)
			throws ServiceException, IOException {
		ObservableList<Task> pendingList = model.getPendingList();
		List<CalendarEventEntry> eventsToAdd = new ArrayList<CalendarEventEntry>();
		for (int i = 0; i < pendingList.size(); i++) {
			Task task = pendingList.get(i);
			if (task.getStatus() == Task.Status.NEWLY_ADDED) {
				CalendarEventEntry e;
				if (task.isRecurringTask()) {
					e = createRecurringEvent(service, task.getWorkInfo(), task
							.getStartDate().returnInRecurringFormat(), task
							.getEndDate().returnInRecurringFormat(), task
							.getTag().getRepetition(), null,
							task.getNumOccurrences(), task.getTag()
									.getInterval(), task.getIsImportant(),
							feedUrl);
				} else {
					if (task.getStartDate() != null
							&& task.getEndDate() != null) {// has start date and
															// end date
						e = createSingleEvent(service, task.getWorkInfo(),
								task.getStartDate(), task.getEndDate(),
								task.getIsImportant(), feedUrl);
					} else if (task.getStartDate() == null
							&& task.getEndDate() == null) {// no start date and
															// no end date
						CustomDate cd = new CustomDate();
						CustomDate cd1 = new CustomDate();
						cd1.setTimeInMillis(cd.getTimeInMillis() + 24 * 60 * 60
								* 1000);
						e = createRecurringEvent(service, task.getWorkInfo(),
								cd.returnInRecurringFormat().substring(0, 8),
								cd1.returnInRecurringFormat().substring(0, 8),
								"daily", null, 0, task.getTag().getInterval(),
								task.getIsImportant(), feedUrl);
					} else {
						throw new Error("date format error");
					}
				}
				eventsToAdd.add(e);
				task.setIndexId(e.getId());
				task.setStatus(Task.Status.UNCHANGED);
			}
		}
		addEvents(service, eventsToAdd, feedUrl);
	}

	private void updateUnchangedTasks(CalendarService service, Model model,
			List<CalendarEventEntry> entries, URL feedURL)
			throws ServiceException, IOException {
		List<CalendarEventEntry> toBeUpdatedOnGCal = new ArrayList<CalendarEventEntry>();
		List<Task> pendingList = model.getPendingList();
		for (int i = 0; i < pendingList.size(); i++) {
			if (pendingList.get(i).getStatus() == Task.Status.UNCHANGED) {
				for (int j = 0; j < entries.size(); j++) {
					if (pendingList.get(i).getIndexId()
							.equals(entries.get(j).getId())) {
						DateTime updated = entries.get(j).getUpdated();
						updated.setTzShift(8 * 60);
						if (CustomDate.compare(pendingList.get(i)
								.getLatestModifiedDate(), new CustomDate(
								updated)) > 0) {
							entries.get(j).setTitle(
									new PlainTextConstruct(pendingList.get(i)
											.getWorkInfo()));

							if (pendingList.get(i).getTag().getRepetition()
									.equals("null")
									&& pendingList.get(i).getStartDate() != null) {
								if (entries.get(j).getRecurrence() == null) {
									entries.get(j)
											.getTimes()
											.get(0)
											.setStartTime(
													pendingList
															.get(i)
															.getStartDate()
															.returnInDateTimeFormat());
									entries.get(j)
											.getTimes()
											.get(0)
											.setEndTime(
													pendingList
															.get(i)
															.getEndDate()
															.returnInDateTimeFormat());
									toBeUpdatedOnGCal.add(entries.get(j));
								} else {
									entries.get(j).delete();
									CalendarEventEntry replace = new CalendarEventEntry();
									replace.setTitle(new PlainTextConstruct(
											pendingList.get(i).getWorkInfo()));
									DateTime startTime = pendingList.get(i)
											.getStartDate()
											.returnInDateTimeFormat();
									DateTime endTime = pendingList.get(i)
											.getEndDate()
											.returnInDateTimeFormat();
									When eventTimes = new When();
									eventTimes.setStartTime(startTime);
									eventTimes.setEndTime(endTime);
									replace.addTime(eventTimes);
									CalendarEventEntry insertedEntry = service
											.insert(feedURL, replace);
									entries.set(j, insertedEntry);
									pendingList.get(i).setIndexId(
											insertedEntry.getId());
								}
							} else {
								entries.get(j).getTimes().clear();
								if (pendingList.get(i).getStartDate() != null
										&& pendingList.get(i).getEndDate() != null) {
									String startDate = pendingList.get(i)
											.getStartDate()
											.returnInRecurringFormat();
									String endDate = pendingList.get(i)
											.getEndDate()
											.returnInRecurringFormat();
									String freq = pendingList.get(i).getTag()
											.getRepetition();
									String recurData = "DTSTART;TZID="
											+ TimeZone.getDefault().getID()
											+ ":" + startDate + "\r\n"
											+ "DTEND;TZID="
											+ TimeZone.getDefault().getID()
											+ ":" + endDate + "\r\n";

									if (freq.contains("day") || freq.contains("daily")) {
										recurData = recurData + "RRULE:FREQ="
												+ "DAILY";
									} else if (freq.contains("week")) {
										recurData = recurData + "RRULE:FREQ="
												+ "WEEKLY";
									} else if (freq.contains("month")) {
										recurData = recurData + "RRULE:FREQ="
												+ "MONTHLY";
									} else if (freq.contains("year")) {
										recurData = recurData + "RRULE:FREQ="
												+ "MONTHLY";
									}
									int interval = pendingList.get(i).getTag()
											.getInterval();
									if (interval > 0) {
										recurData = recurData + ";INTERVAL="
												+ interval;
									}

									int count = pendingList.get(i)
											.getNumOccurrences();
									if (count > 0) {
										recurData = recurData + ";COUNT="
												+ count;
									}
									recurData += "\r\n";
									Recurrence rec = new Recurrence();
									System.out.println("test");
									rec.setValue(recurData);
									entries.get(j).setRecurrence(rec);
								}
								if (pendingList.get(i).getIsImportant() == true) {
									setReminder(entries.get(j));
								}
								toBeUpdatedOnGCal.add(entries.get(j));
							}
						} else {
							pendingList.get(i).setWorkInfo(
									entries.get(j).getTitle().getPlainText());
							try {
								pendingList.get(i).setStartDate(
										new CustomDate(entries.get(j)
												.getTimes().get(0)
												.getStartTime()));
								pendingList.get(i)
										.setEndDate(
												new CustomDate(entries.get(j)
														.getTimes().get(0)
														.getEndTime()));
								pendingList.get(i).setTag(
										new Tag(pendingList.get(i).getTag()
												.getTag(), "null"));
							} catch (IndexOutOfBoundsException e) {
								String recurData = entries.get(j)
										.getRecurrence().getValue();
								if (recurData.contains("VALUE=DATE:")) {
									System.out.println("length: "
											+ recurData.length());
									int startDateIndex = recurData.indexOf(":");
									System.out.println("start index: "
											+ startDateIndex);
									String startDateString = recurData
											.substring(startDateIndex + 1,
													startDateIndex + 9);
									System.out.println("start: "
											+ startDateString);
									CustomDate cd1 = new CustomDate(
											startDateString.substring(6, 8)
													+ "/"
													+ startDateString
															.substring(4, 6)
													+ "/"
													+ startDateString
															.substring(0, 4));

									int endDateIndex = recurData.indexOf(":",
											startDateIndex + 1);
									String endDateString = recurData.substring(
											endDateIndex + 1, endDateIndex + 9);
									System.out.println("end: " + endDateString);
									CustomDate cd2 = new CustomDate(
											endDateString.substring(6, 8)
													+ "/"
													+ endDateString.substring(
															4, 6)
													+ "/"
													+ endDateString.substring(
															0, 4));

									pendingList.get(i).setStartDate(cd1);
									pendingList.get(i).setEndDate(cd2);

									int freqStartIndex = recurData
											.indexOf("FREQ=");
									int freqEndIndex;

									if (recurData.contains("BYDAY")
											|| recurData.contains("BYMONTHDAY")
											|| recurData.contains("COUNT")
											|| recurData.contains("INTERVAL")) {
										freqEndIndex = recurData.indexOf(";",
												freqStartIndex);
									} else {
										freqEndIndex = recurData.indexOf("\n",
												freqStartIndex);
										if (freqEndIndex == -1)
											freqEndIndex = recurData.length();
									}
									String freq = recurData.substring(
											freqStartIndex + 5, freqEndIndex);
									if (recurData.contains("INTERVAL=")) {
										int startIndex = recurData
												.indexOf("INTERVAL=") + 9;
										int endIndex = recurData.indexOf(";",
												startIndex);
										if (endIndex < 0) {
											endIndex = recurData.indexOf("\n",
													startIndex);
										}
										int interval = Integer
												.parseInt(recurData.substring(
														startIndex, endIndex));
										String suffix = "";
										if (freq.equalsIgnoreCase("daily")) {
											suffix = "days";
										} else if (freq
												.equalsIgnoreCase("weekly")) {
											suffix = "weeks";
										} else if (freq
												.equalsIgnoreCase("monthly")) {
											suffix = "months";
										} else {
											suffix = "years";
										}
										freq = "every" + interval + suffix;
									}
									System.out.println("freq: " + freq);
									pendingList.get(i).setTag(
											new Tag(pendingList.get(i).getTag()
													.getTag(), freq
													.toLowerCase()));

									if (recurData.contains("COUNT=")) {
										int startIndex = recurData
												.indexOf("COUNT=") + 6;
										int endIndex = recurData.indexOf(";",
												startIndex);
										if (endIndex < 0) {
											endIndex = recurData.indexOf("\n",
													startIndex);
										}
										int count = Integer
												.parseInt(recurData.substring(
														startIndex, endIndex));
										pendingList.get(i).setNumOccurrences(
												count);
									} else if (freq.equals("DAILY")) {
										pendingList.get(i).setStartDate(null);
										pendingList.get(i).setEndDate(null);
										pendingList.get(i).setTag(
												new Tag(pendingList.get(i)
														.getTag().getTag(),
														"null"));
									}

								} else {// timed recurring event
									int startDateIndex = recurData.indexOf(":");
									System.out.println("start index: "
											+ startDateIndex);
									String startDateString = recurData
											.substring(startDateIndex + 1,
													startDateIndex + 16);
									System.out.println("start: "
											+ startDateString);
									CustomDate cd1 = new CustomDate(
											startDateString.substring(6, 8)
													+ "/"
													+ startDateString
															.substring(4, 6)
													+ "/"
													+ startDateString
															.substring(0, 4)
													+ " "
													+ startDateString
															.substring(9, 11)
													+ ":"
													+ startDateString
															.substring(11, 13));

									int endDateIndex = recurData.indexOf(":",
											startDateIndex + 1);
									String endDateString = recurData
											.substring(endDateIndex + 1,
													endDateIndex + 16);
									System.out.println("end: " + endDateString);
									CustomDate cd2 = new CustomDate(
											endDateString.substring(6, 8)
													+ "/"
													+ endDateString.substring(
															4, 6)
													+ "/"
													+ endDateString.substring(
															0, 4)
													+ " "
													+ endDateString.substring(
															9, 11)
													+ ":"
													+ endDateString.substring(
															11, 13));

									pendingList.get(i).setStartDate(cd1);
									pendingList.get(i).setEndDate(cd2);

									int freqStartIndex = recurData
											.indexOf("FREQ=");
									int freqEndIndex;
									if (recurData.contains("BYDAY")
											|| recurData.contains("BYMONTHDAY")
											|| recurData.contains("COUNT")
											|| recurData.contains("INTERVAL")) {
										freqEndIndex = recurData.indexOf(";",
												freqStartIndex);
									} else {
										freqEndIndex = recurData.indexOf("\n",
												freqStartIndex);
										if (freqEndIndex == -1)
											freqEndIndex = recurData.length();
									}
									String freq = recurData.substring(
											freqStartIndex + 5, freqEndIndex);
									if (recurData.contains("INTERVAL=")) {
										int startIndex = recurData
												.indexOf("INTERVAL=") + 9;
										int endIndex = recurData.indexOf(";",
												startIndex);
										if (endIndex < 0) {
											endIndex = recurData.indexOf("\n",
													startIndex);
										}
										int interval = Integer
												.parseInt(recurData.substring(
														startIndex, endIndex));
										String suffix = "";
										if (freq.equalsIgnoreCase("daily")) {
											suffix = "days";
										} else if (freq
												.equalsIgnoreCase("weekly")) {
											suffix = "weeks";
										} else if (freq
												.equalsIgnoreCase("monthly")) {
											suffix = "months";
										} else {
											suffix = "years";
										}
										freq = "every" + interval + suffix;
									}
									System.out.println("freq: " + freq);

									pendingList.get(i).setTag(
											new Tag(pendingList.get(i).getTag()
													.getTag(), freq
													.toLowerCase()));
									if (recurData.contains("COUNT=")) {
										int startIndex = recurData
												.indexOf("COUNT=") + 6;
										int endIndex = recurData.indexOf(";",
												startIndex);
										if (endIndex < 0) {
											endIndex = recurData.indexOf("\n",
													startIndex);
										}
										int count = Integer
												.parseInt(recurData.substring(
														startIndex, endIndex));
										pendingList.get(i).setNumOccurrences(
												count);
									}
								}

							}
							if (!entries.get(j).getReminder().isEmpty())
								pendingList.get(i).setIsImportant(true);
							pendingList.get(i).updateLatestModifiedDate();
						}
						break;
					}
				}
			}
		}
		updateEvents(service, toBeUpdatedOnGCal, feedURL);
	}

	private void syncDeletedTasksToGCal(CalendarService service, Model model,
			List<CalendarEventEntry> entries, URL feedUrl)
			throws ServiceException, IOException {
		List<CalendarEventEntry> tobeDelete = new ArrayList<CalendarEventEntry>();
		ObservableList<Task> completedTasks = model.getCompleteList();
		ObservableList<Task> deletedTasks = model.getTrashList();
		for (int i = 0; i < completedTasks.size(); i++) {
			if (completedTasks.get(i).getStatus() == Task.Status.DELETED) {
				for (int j = 0; j < entries.size(); j++) {
					if (completedTasks.get(i).getIndexId()
							.equals(entries.get(j).getId())) {
						tobeDelete.add(entries.get(j));
						break;
					}
				}
				completedTasks.get(i).setStatus(Task.Status.UNCHANGED);
			}
		}
		for (int i = 0; i < deletedTasks.size(); i++) {
			if (deletedTasks.get(i).getStatus() == Task.Status.DELETED) {
				for (int j = 0; j < entries.size(); j++) {
					if (deletedTasks.get(i).getIndexId()
							.equals(entries.get(j).getId())) {
						tobeDelete.add(entries.get(j));
						break;
					}
				}
				deletedTasks.get(i).setStatus(Task.Status.UNCHANGED);
			}
		}
		deleteEvents(service, tobeDelete, feedUrl);
	}

	private void deleteTasksLocally(List<CalendarEventEntry> entries,
			Model model) {
		ArrayList<String> entryIds = new ArrayList<String>();
		List<Task> pendingList = model.getPendingList();
		for (int i = 0; i < entries.size(); i++) {
			entryIds.add(entries.get(i).getId());
		}
		for (int i = 0; i < pendingList.size(); i++) {
			if (!entryIds.contains(pendingList.get(i).getIndexId()) && pendingList.get(i).getStatus() != Task.Status.ADDED_WHEN_SYNC) {
				Task t = pendingList.remove(i);
				t.setStatus(Task.Status.UNCHANGED);
				model.getTrashList().add(t);
				i--;
			}
		}
	}

	private void addEventsLocally(List<CalendarEventEntry> entries, Model model) {
		ArrayList<String> taskIds = new ArrayList<String>();
		List<Task> pendingList = model.getPendingList();
		for (int i = 0; i < pendingList.size(); i++) {
			taskIds.add(pendingList.get(i).getIndexId());
		}
		System.out.println(taskIds.size());
		taskIds.addAll(model.getRemovedIdDuringSync());
		System.out.println(taskIds.size());
		for (int i = 0; i < entries.size(); i++) {
			CalendarEventEntry e = entries.get(i);
			if (!taskIds.contains(e.getId())) {
				System.out.println("new: " + e.getTitle().getPlainText());
				Task t = new Task();
				t.setWorkInfo(e.getTitle().getPlainText());

				try {
					DateTime start = entries.get(i).getTimes().get(0)
							.getStartTime();
					DateTime end = entries.get(i).getTimes().get(0)
							.getEndTime();
					CustomDate cd1 = new CustomDate(start);
					System.out.println("start date: " + start.toString());
					CustomDate cd2 = new CustomDate(end);
					System.out.println("end date: " + end.toString());
					t.setStartDate(cd1);
					t.setEndDate(cd2);

				} catch (IndexOutOfBoundsException | NullPointerException exc) {
					String recurData = entries.get(i).getRecurrence()
							.getValue();
					System.out.println(recurData);

					if (recurData.contains("VALUE=DATE:")) {// all day recurring
															// event
						System.out.println("length: " + recurData.length());
						int startDateIndex = recurData.indexOf(":");
						System.out.println("start index: " + startDateIndex);
						String startDateString = recurData.substring(
								startDateIndex + 1, startDateIndex + 9);
						System.out.println("start: " + startDateString);
						CustomDate cd1 = new CustomDate(
								startDateString.substring(6, 8) + "/"
										+ startDateString.substring(4, 6) + "/"
										+ startDateString.substring(0, 4));

						int endDateIndex = recurData.indexOf(":",
								startDateIndex + 1);
						String endDateString = recurData.substring(
								endDateIndex + 1, endDateIndex + 9);
						System.out.println("end: " + endDateString);
						CustomDate cd2 = new CustomDate(
								endDateString.substring(6, 8) + "/"
										+ endDateString.substring(4, 6) + "/"
										+ endDateString.substring(0, 4));

						t.setStartDate(cd1);
						t.setEndDate(cd2);

						int freqStartIndex = recurData.indexOf("FREQ=");
						int freqEndIndex;

						if (recurData.contains("BYDAY")
								|| recurData.contains("BYMONTHDAY")
								|| recurData.contains("COUNT")
								|| recurData.contains("INTERVAL")) {
							freqEndIndex = recurData.indexOf(";",
									freqStartIndex);
						} else {
							freqEndIndex = recurData.indexOf("\n",
									freqStartIndex);
							if (freqEndIndex == -1)
								freqEndIndex = recurData.length();
						}
						String freq = recurData.substring(freqStartIndex + 5,
								freqEndIndex);
						if (recurData.contains("INTERVAL=")) {
							int startIndex = recurData.indexOf("INTERVAL=") + 9;
							int endIndex = recurData.indexOf(";", startIndex);
							if (endIndex < 0) {
								endIndex = recurData.indexOf("\n", startIndex);
							}
							int interval = Integer.parseInt(recurData
									.substring(startIndex, endIndex));
							String suffix = "";
							if (freq.equalsIgnoreCase("daily")) {
								suffix = "days";
							} else if (freq.equalsIgnoreCase("weekly")) {
								suffix = "weeks";
							} else if (freq.equalsIgnoreCase("monthly")) {
								suffix = "months";
							} else {
								suffix = "years";
							}
							freq = "every" + interval + suffix;
						}
						System.out.println("freq: " + freq);
						t.setTag(new Tag(Common.HYPHEN, freq.toLowerCase()));

						if (recurData.contains("COUNT=")) {
							int startIndex = recurData.indexOf("COUNT=") + 6;
							int endIndex = recurData.indexOf(";", startIndex);
							if (endIndex < 0) {
								endIndex = recurData.indexOf("\n", startIndex);
							}
							int count = Integer.parseInt(recurData.substring(
									startIndex, endIndex));
							t.setNumOccurrences(count);
						} else if (freq.equals("DAILY")) {
							t.setStartDate(null);
							t.setEndDate(null);
							t.setTag(new Tag(Common.HYPHEN, "null"));
						}

					} else {// timed recurring event
						int startDateIndex = recurData.indexOf(":");
						System.out.println("start index: " + startDateIndex);
						String startDateString = recurData.substring(
								startDateIndex + 1, startDateIndex + 16);
						System.out.println("start: " + startDateString);
						CustomDate cd1 = new CustomDate(
								startDateString.substring(6, 8) + "/"
										+ startDateString.substring(4, 6) + "/"
										+ startDateString.substring(0, 4) + " "
										+ startDateString.substring(9, 11)
										+ ":"
										+ startDateString.substring(11, 13));

						int endDateIndex = recurData.indexOf(":",
								startDateIndex + 1);
						String endDateString = recurData.substring(
								endDateIndex + 1, endDateIndex + 16);
						System.out.println("end: " + endDateString);
						CustomDate cd2 = new CustomDate(
								endDateString.substring(6, 8) + "/"
										+ endDateString.substring(4, 6) + "/"
										+ endDateString.substring(0, 4) + " "
										+ endDateString.substring(9, 11) + ":"
										+ endDateString.substring(11, 13));

						t.setStartDate(cd1);
						t.setEndDate(cd2);

						int freqStartIndex = recurData.indexOf("FREQ=");
						int freqEndIndex;
						if (recurData.contains("BYDAY")
								|| recurData.contains("BYMONTHDAY")
								|| recurData.contains("COUNT")
								|| recurData.contains("INTERVAL")) {
							freqEndIndex = recurData.indexOf(";",
									freqStartIndex);
						} else {
							freqEndIndex = recurData.indexOf("\n",
									freqStartIndex);
							if (freqEndIndex == -1)
								freqEndIndex = recurData.length();
						}
						String freq = recurData.substring(freqStartIndex + 5,
								freqEndIndex);
						if (recurData.contains("INTERVAL=")) {
							int startIndex = recurData.indexOf("INTERVAL=") + 9;
							int endIndex = recurData.indexOf(";", startIndex);
							if (endIndex < 0) {
								endIndex = recurData.indexOf("\n", startIndex);
							}
							int interval = Integer.parseInt(recurData
									.substring(startIndex, endIndex));
							String suffix = "";
							if (freq.equalsIgnoreCase("daily")) {
								suffix = "days";
							} else if (freq.equalsIgnoreCase("weekly")) {
								suffix = "weeks";
							} else if (freq.equalsIgnoreCase("monthly")) {
								suffix = "months";
							} else {
								suffix = "years";
							}
							freq = "every" + interval + suffix;
						}
						System.out.println("freq: " + freq);

						t.setTag(new Tag(Common.HYPHEN, freq.toLowerCase()));
						if (recurData.contains("COUNT=")) {
							int startIndex = recurData.indexOf("COUNT=") + 6;
							int endIndex = recurData.indexOf(";", startIndex);
							if (endIndex < 0) {
								endIndex = recurData.indexOf("\n", startIndex);
							}
							int count = Integer.parseInt(recurData.substring(
									startIndex, endIndex));
							t.setNumOccurrences(count);
						}
					}

				}
				t.setIndexId(e.getId());
				t.setStatus(Task.Status.UNCHANGED);
				if (!e.getReminder().isEmpty())
					t.setIsImportant(true);
				pendingList.add(t);
				System.out.println("done");
			}
		}
	}

	private List<CalendarEventEntry> getEventsFromGCal(CalendarService service,
			URL feedUrl) throws IOException, ServiceException {
		// Send the request and receive the response:
		CalendarEventFeed eventFeed = service.getFeed(feedUrl,
				CalendarEventFeed.class);

		List<CalendarEventEntry> entry = eventFeed.getEntries();
		return entry;
	}

	/**
	 * Creates a new secondary calendar using the owncalendars feed.
	 * 
	 * @param service
	 *            An authenticated CalendarService object.
	 * @return The newly created calendar entry.
	 * @throws IOException
	 *             If there is a problem communicating with the server.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 */
	CalendarEntry createCalendar(CalendarService service,
			URL owncalendarsFeedUrl) throws IOException, ServiceException {

		Calendar cal = new GregorianCalendar();
		String timeZone = cal.getTimeZone().getID();
		// Create the calendar
		CalendarEntry calendar = new CalendarEntry();
		calendar.setTitle(new PlainTextConstruct(CALENDAR_TITLE));
		calendar.setSummary(new PlainTextConstruct(CALENDAR_SUMMARY));
		calendar.setHidden(HiddenProperty.FALSE);
		calendar.setTimeZone(new TimeZoneProperty(timeZone));

		// Insert the calendar
		return service.insert(owncalendarsFeedUrl, calendar);
	}

	/**
	 * Helper method to create either single-instance or recurring events. For
	 * simplicity, some values that might normally be passed as parameters (such
	 * as author name, email, etc.) are hard-coded.
	 * 
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param eventTitle
	 *            Title of the event to create.
	 * @param eventContent
	 *            Text content of the event to create.
	 * @param recurData
	 *            Recurrence value for the event, or null for single-instance
	 *            events.
	 * @param isQuickAdd
	 *            True if eventContent should be interpreted as the text of a
	 *            quick add event.
	 * @param wc
	 *            A WebContent object, or null if this is not a web content
	 *            event.
	 * @return The newly-created CalendarEventEntry.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	CalendarEventEntry createEvent(CalendarService service, String title,
			CustomDate startDate, CustomDate endDate, String recurData,
			boolean isImport, boolean isQuickAdd, URL feedUrl)
			throws ServiceException, IOException {
		CalendarEventEntry myEntry = new CalendarEventEntry();

		myEntry.setTitle(new PlainTextConstruct(title));
		myEntry.setQuickAdd(isQuickAdd);

		if (recurData == null) {
			DateTime startTime = startDate.returnInDateTimeFormat();
			DateTime endTime = endDate.returnInDateTimeFormat();
			When eventTimes = new When();
			eventTimes.setStartTime(startTime);
			eventTimes.setEndTime(endTime);
			myEntry.addTime(eventTimes);
		} else {
			Recurrence recur = new Recurrence();
			recur.setValue(recurData);
			myEntry.setRecurrence(recur);
		}

		if (isImport) {
			setReminder(myEntry);
		}

		// Send the request and receive the response:
		return myEntry;
	}

	/**
	 * Creates a single-occurrence event.
	 * 
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param eventTitle
	 *            Title of the event to create.
	 * @param eventContent
	 *            Text content of the event to create.
	 * @return The newly-created CalendarEventEntry.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	CalendarEventEntry createSingleEvent(CalendarService service,
			String eventContent, CustomDate startDate, CustomDate endDate,
			boolean isImport, URL feedUrl) throws ServiceException, IOException {
		return createEvent(service, eventContent, startDate, endDate, null,
				isImport, false, feedUrl);
	}

	/**
	 * Creates a new recurring event.
	 * 
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param eventTitle
	 *            Title of the event to create.
	 * @param eventContent
	 *            Text content of the event to create.
	 * @return The newly-created CalendarEventEntry.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	private CalendarEventEntry createRecurringEvent(CalendarService service,
			String eventContent, String startDate, String endDate, String freq,
			String until, int count, int interval, boolean isImport, URL feedUrl)
			throws ServiceException, IOException {

		String recurData = "DTSTART;TZID=" + TimeZone.getDefault().getID()
				+ ":" + startDate + "\r\n" + "DTEND;TZID="
				+ TimeZone.getDefault().getID() + ":" + endDate + "\r\n";

		if (freq.contains("day") || freq.contains("daily")) {
			recurData = recurData + "RRULE:FREQ=" + "DAILY";
		} else if (freq.contains("week")) {
			recurData = recurData + "RRULE:FREQ=" + "WEEKLY";
		} else if (freq.contains("month")) {
			recurData = recurData + "RRULE:FREQ=" + "MONTHLY";
		} else if (freq.contains("year") || freq.contains("annually")) {
			recurData = recurData + "RRULE:FREQ=" + "YEARLY";
		}
		if (interval > 0) {
			recurData = recurData + ";INTERVAL=" + interval;
		}
		if (count > 0) {
			recurData = recurData + ";COUNT=" + count;
		}
		if (until != null) {
			recurData = recurData + ";UNTIL=" + until;
		}
		recurData += "\r\n";

		System.out.println(recurData);
		return createEvent(service, eventContent, null, null, recurData,
				isImport, false, feedUrl);
	}

	/**
	 * Makes a batch request to delete all the events in the given list. If any
	 * of the operations fails, the errors returned from the server are
	 * displayed. The CalendarEntry objects in the list given as a parameters
	 * must be entries returned from the server that contain valid edit links
	 * (for optimistic concurrency to work). Note: You can add entries to a
	 * batch request for the other operation types (INSERT, QUERY, and UPDATE)
	 * in the same manner as shown below for DELETE operations.
	 * 
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param eventsToDelete
	 *            A list of CalendarEventEntry objects to delete.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	private static void deleteEvents(CalendarService service,
			List<CalendarEventEntry> eventsToDelete, URL feedUrl)
			throws ServiceException, IOException {

		// Add each item in eventsToDelete to the batch request.
		CalendarEventFeed batchRequest = new CalendarEventFeed();
		for (int i = 0; i < eventsToDelete.size(); i++) {
			CalendarEventEntry toDelete = eventsToDelete.get(i);
			// Modify the entry toDelete with batch ID and operation type.
			BatchUtils.setBatchId(toDelete, String.valueOf(i));
			BatchUtils.setBatchOperationType(toDelete,
					BatchOperationType.DELETE);
			batchRequest.getEntries().add(toDelete);
		}

		// Get the URL to make batch requests to
		CalendarEventFeed feed = service.getFeed(feedUrl,
				CalendarEventFeed.class);
		Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
		URL batchUrl = new URL(batchLink.getHref());

		// Submit the batch request
		CalendarEventFeed batchResponse = service.batch(batchUrl, batchRequest);

		// Ensure that all the operations were successful.
		boolean isSuccess = true;
		for (CalendarEventEntry entry : batchResponse.getEntries()) {
			String batchId = BatchUtils.getBatchId(entry);
			if (!BatchUtils.isSuccess(entry)) {
				isSuccess = false;
				BatchStatus status = BatchUtils.getBatchStatus(entry);
				System.out.println("\n" + batchId + " failed ("
						+ status.getReason() + ") " + status.getContent());
			}
		}
		if (isSuccess) {
			System.out
					.println("Successfully deleted all events via batch request.");
		}
	}

	private void updateEvents(CalendarService service,
			List<CalendarEventEntry> eventsToUpdate, URL feedUrl)
			throws ServiceException, IOException {
		// Add each item in eventsToDelete to the batch request.
		CalendarEventFeed batchRequest = new CalendarEventFeed();
		for (int i = 0; i < eventsToUpdate.size(); i++) {
			CalendarEventEntry toUpdate = eventsToUpdate.get(i);
			// Modify the entry toDelete with batch ID and operation type.
			BatchUtils.setBatchId(toUpdate, String.valueOf(i));
			BatchUtils.setBatchOperationType(toUpdate,
					BatchOperationType.UPDATE);
			batchRequest.getEntries().add(toUpdate);
		}

		// Get the URL to make batch requests to
		CalendarEventFeed feed = service.getFeed(feedUrl,
				CalendarEventFeed.class);
		Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
		URL batchUrl = new URL(batchLink.getHref());

		// Submit the batch request
		CalendarEventFeed batchResponse = service.batch(batchUrl, batchRequest);

		// Ensure that all the operations were successful.
		boolean isSuccess = true;
		for (CalendarEventEntry entry : batchResponse.getEntries()) {
			String batchId = BatchUtils.getBatchId(entry);
			if (!BatchUtils.isSuccess(entry)) {
				isSuccess = false;
				BatchStatus status = BatchUtils.getBatchStatus(entry);
				System.out.println("\n" + batchId + " failed ("
						+ status.getReason() + ") " + status.getContent());
			}
		}
		if (isSuccess) {
			System.out
					.println("Successfully updated all events via batch request.");
		}
	}

	private void setReminder(CalendarEventEntry event) {
		Reminder r = new Reminder();
		Method m = Method.ALERT;

		r.setMinutes(REMINDER_MINUTES);
		r.setMethod(m);
		event.getReminder().add(r);
	}

	private String isCalendarExist(CalendarService service, URL feedUrl) {
		String calendarId = null;
		CalendarFeed resultFeed = null;
		List<CalendarEntry> entries = null;
		try {
			resultFeed = service.getFeed(feedUrl, CalendarFeed.class);
			entries = resultFeed.getEntries();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (entries != null) {
			for (int i = 0; i < entries.size(); i++) {
				if (entries.get(i).getTitle().getPlainText()
						.equals(CALENDAR_TITLE)) {
					calendarId = trimId(entries.get(i).getId());
					break;
				}
			}
		}
		return calendarId;
	}
	
	private void addEvents(CalendarService service, List<CalendarEventEntry> eventsToAdd, URL feedUrl) throws IOException, ServiceException{
		// Add each item in eventsToDelete to the batch request.
		CalendarEventFeed batchRequest = new CalendarEventFeed();
		for (int i = 0; i < eventsToAdd.size(); i++) {
			CalendarEventEntry toAdd = eventsToAdd.get(i);
			// Modify the entry toDelete with batch ID and operation type.
			BatchUtils.setBatchId(toAdd, String.valueOf(i));
			BatchUtils.setBatchOperationType(toAdd,
					BatchOperationType.INSERT);
			batchRequest.getEntries().add(toAdd);
		}

		// Get the URL to make batch requests to
		CalendarEventFeed feed = service.getFeed(feedUrl,
				CalendarEventFeed.class);
		Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
		URL batchUrl = new URL(batchLink.getHref());

		// Submit the batch request
		CalendarEventFeed batchResponse = service.batch(batchUrl, batchRequest);

		// Ensure that all the operations were successful.
		boolean isSuccess = true;
		for (CalendarEventEntry entry : batchResponse.getEntries()) {
			String batchId = BatchUtils.getBatchId(entry);
			if (!BatchUtils.isSuccess(entry)) {
				isSuccess = false;
				BatchStatus status = BatchUtils.getBatchStatus(entry);
				System.out.println("\n" + batchId + " failed ("
						+ status.getReason() + ") " + status.getContent());
			}
		}
		if (isSuccess) {
			System.out
					.println("Successfully updated all events via batch request.");
		}
	}
}
