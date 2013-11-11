import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;

import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
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

/**
 * This class is the main class for processing synchronization
 *
 */

//@author A0105523U
public class Synchronization  {
	// Logger of this class
	private static Logger logger = Logger.getLogger("Sync");
	/* messages */
	private static final String CALENDAR_TITLE = "iDo";
	private static final String CALENDAR_SUMMARY = "This calendar synchronizes with iDo Task Manager.";
	private static final String SERVICE_NAME = "sg.edu.nus.cs2103aug2013-w13-03j";
	
	/* Default time for reminders*/
	private static final int REMINDER_MINUTES = 30;

	/* Username and password to login */
	String username = null;
	String password = null;
	boolean isValid = false;
	/* Model */
	Model model;
	History commandHistory;
	/* calendar service */
	CalendarService service;

	/* The base URL for a user's calendar metafeed (needs a username appended). */
	private static final String METAFEED_URL_BASE = "https://www.google.com/calendar/feeds/";

	/*The string to add to the user's metafeedUrl to access the owncalendars feed.*/
	private static final String OWNCALENDARS_FEED_URL_SUFFIX = "/owncalendars/full";

	/*The string to add to the user's eventfeedUrl to access the private feed.*/
	private static final String EVENT_FEED_URL_SUFFIX = "/private/full";
	
	/* Event feed URL */
	private URL eventFeedUrl = null;

	/* a list of event entry from Google calendar */
	private List<CalendarEventEntry> eventEntry = new ArrayList<CalendarEventEntry>();
	
	/**
	 * Constructor of this class
	 * 
	 * @param m
	 *            model of this application
	 */
	public Synchronization(Model m, History c) {
		model = m;
		commandHistory = c;
	}
	
	/*************** start of public methods ****************/
	/**
	 * Sets user account and password for login.
	 * @param n - username.
	 * @param p - password.
	 */
	public void setUsernameAndPassword(String n, String p) {
		username = n;
		password = p;
	}

	/**
	 * Starts the synchronization with Google Calendar.
	 * @return feedback
	 */
	public String execute() {
		try {
			eventFeedUrl = formEventFeedUrl(service);
		} catch (IOException | ServiceException e) {
			return Common.MESSAGE_SYNC_FAIL_TO_CREATE_CALENDAR;
		}
		
		// Sync new task to Google Calendar
		try {
			eventEntry = getEventsFromGCal(service, eventFeedUrl);
			syncNewTasksToGCal(service, model, eventFeedUrl);
		} catch (ServiceException | IOException e) {
			logger.log(Level.INFO, "Cannot get events from service");
		}

		// Update task due to changes for existiting tasks in both GCal and iDo
		try {
			updateModifiedTasks(service, model, eventEntry, eventFeedUrl);
		} catch (ServiceException|IOException e) {
			logger.log(Level.INFO, "Fail to update changes between GCal and iDo");
		}
		
		// Deelete events on GCal which have been deleted locally
		try {
			syncDeletedTasksToGCal(service, model, eventEntry, eventFeedUrl);
		} catch (ServiceException | IOException e) {
			return Common.MESSAGE_SYNC_SERVICE_STOPPED;
		}

		// Get all events from Google calendar
		try {
			eventEntry = getEventsFromGCal(service, eventFeedUrl);
		} catch (IOException | ServiceException e) {
			return Common.MESSAGE_SYNC_SERVICE_STOPPED;
		}

		// Delete tasks locally which have been deleted on GCal
		deleteTasksLocally(eventEntry, model);

		// Add tasks locally which have been added on GCal
		addEventsLocally(eventEntry, model); 
		
		commandHistory.clearUndoStack();
		commandHistory.clearRedoStack();
		return Common.MESSAGE_SYNC_SUCCESSFUL;
	}
	
	/**
	 * Initialize the service with the stored username and password
	 * 
	 * @throws AuthenticationException
	 *             this exception will be thrown if the Google account is
	 *             invalid
	 */
	public void initService() throws AuthenticationException {
		// Create a new service
		service = new CalendarService(SERVICE_NAME);
		// Authenticate using ClientLogin
		service.setUserCredentials(username, password);
	}
	
	/**
	 * This function is used to create the new calendar "iDo" if it does not
	 * exist in the corresponding Google account
	 * 
	 * @param service
	 *            the currently connected service
	 * @return the URL of the calendar
	 */
	private URL formEventFeedUrl(CalendarService service) throws ServiceException, IOException {
		URL owncalUrl = new URL(METAFEED_URL_BASE + username
				+ OWNCALENDARS_FEED_URL_SUFFIX);
		String calId = isCalendarExist(service, owncalUrl);
		if (calId == null) {
			CalendarEntry calendar = createCalendar(service, owncalUrl);
			calId = trimId(calendar.getId());
			logger.log(Level.INFO, "Successfully create a new calendar");
		}
		return new URL(METAFEED_URL_BASE + calId + EVENT_FEED_URL_SUFFIX);
	}
	
	/**
	 * This function is used to get the calendar id
	 * 
	 * @param id
	 *            the full content of the ID
	 * @return the cropped ID
	 */
	private String trimId(String id) {
		String[] temp = id.trim().split("/");
		return temp[temp.length - 1].toString();
	}
	
	/**
	 * This function is the main function to sync new local tasks to Google
	 * Calendar
	 * 
	 * @param service
	 *            the currently connected service
	 * @param model
	 *            model of tasks from the application
	 * @param feedUrl
	 *            the URL of the calendar
	 */
	private void syncNewTasksToGCal(CalendarService service, Model model, URL feedUrl)
			throws ServiceException, IOException {
		ObservableList<Task> pendingList = model.getPendingList();
		for (int i = 0; i < pendingList.size(); i++) {
			Task task = pendingList.get(i);
			if (task.getStatus() == Task.Status.NEWLY_ADDED) {
				CalendarEventEntry event = addEvent(service, feedUrl, task);
				task.setIndexId(event.getId());
				task.setStatus(Task.Status.UNCHANGED);
			}
		}
	}

	/**
	 * This function is used to add an event to the Google Calendar
	 * @param service the currently connected service
	 * @param feedUrl the URL of the calendar
	 * @param task the task which has to be added
	 * @return the added event entry on Google Calendar
	 */
	private CalendarEventEntry addEvent(CalendarService service, URL feedUrl,
			Task task) throws ServiceException, IOException {
		CalendarEventEntry event;
		if (task.isRecurringTask()) {
			event = createRecurringEvent(service, task.getWorkInfo(), task
					.getStartDate().returnInRecurringFormat(), task
					.getEndDate().returnInRecurringFormat(), task
					.getTag().getRepetition(), null,
					task.getNumOccurrences(), task.getTag()
							.getInterval(), task.isImportantTask(),
					feedUrl);
		} else {
			if (!task.isFloatingTask()) {// has start date and end date
				event = createSingleEvent(service, task.getWorkInfo(),
						task.getStartDate(), task.getEndDate(),
						task.isImportantTask(), feedUrl);
			} else{// no start date and no end date
				CustomDate startDate = new CustomDate();
				CustomDate endDate = new CustomDate();
				endDate.setTimeInMillis(startDate.getTimeInMillis() + CustomDate.DAY_IN_MILLIS);
				event = createRecurringEvent(service, task.getWorkInfo(),
						startDate.returnInRecurringFormat().substring(0, 8),
						endDate.returnInRecurringFormat().substring(0, 8),
						"daily", null, 0, task.getTag().getInterval(),
						task.isImportantTask(), feedUrl);
			}
		}
		return event;
	}
	
	//@author A0098077N
	/**
	 * This function is used to update both the modified tasks on iDo and
	 * modified events on Google Calendar to be the same with each other
	 * 
	 * @param service
	 *            the currently connected service
	 * @param model
	 *            the model of the application
	 * @param entries
	 *            the entries in Google Calendar
	 * @param feedURL
	 *            the URL of the calendar
	 */
	private void updateModifiedTasks(CalendarService service, Model model,
			List<CalendarEventEntry> entries, URL feedURL)
			throws ServiceException, IOException {
		List<CalendarEventEntry> toBeUpdatedOnGCal = new ArrayList<CalendarEventEntry>();
		List<Task> pendingList = model.getPendingList();
		for (int i = 0; i < pendingList.size(); i++) {
			if (pendingList.get(i).getStatus() == Task.Status.UNCHANGED) {
				checkEntriesForUpdate(service, entries, feedURL,
						toBeUpdatedOnGCal, pendingList, i);
			}
		}
		updateEvents(service, toBeUpdatedOnGCal, feedURL);
	}
	
	/**
	 * This function find the correct entry from list of entries in Google
	 * Calendar with the same ID from the task on iDo It then compares the
	 * latest modified date between the event and the task to determine which
	 * latest version to update
	 * 
	 * @param service
	 *            the currently connected service
	 * @param entries
	 *            list of event entries on Google Calendar
	 * @param feedURL
	 *            the URL of the calendar
	 * @param toBeUpdatedOnGCal
	 *            list of events to be updated on Google Calendar
	 * @param pendingList
	 *            the list of pending tasks
	 * @param i
	 *            the index of the task
	 */
	private void checkEntriesForUpdate(CalendarService service,
			List<CalendarEventEntry> entries, URL feedURL,
			List<CalendarEventEntry> toBeUpdatedOnGCal, List<Task> pendingList,
			int i) throws IOException, ServiceException {
		for (int j = 0; j < entries.size(); j++) {
			boolean isSameId = pendingList.get(i).getIndexId().equals(entries.get(j).getId());
			if (isSameId) {
				DateTime updated = entries.get(j).getUpdated();
				updated.setTzShift(8 * 60);
				boolean hasLatestModificationFromiDo = CustomDate.compare(pendingList.get(i)
						.getLatestModifiedDate(), new CustomDate(
						updated)) > 0;
				if (hasLatestModificationFromiDo) {
					updateModifiedEventOnGCal(service, entries,feedURL, toBeUpdatedOnGCal, pendingList, i, j);
				} else {
					updateModifiedTaskOniDo(entries, pendingList, i, j);
				}
				break;
			}
		}
	}

	/**
	 * This function is used to update a task on iDo to be the same with an
	 * event entry on Google Calendar
	 * 
	 * @param entries
	 *            the list of entries on Google Calendar
	 * @param pendingList
	 *            the list of pending tasks
	 * @param i
	 *            the index of the task
	 * @param j
	 *            the index of the entry
	 */
	private void updateModifiedTaskOniDo(List<CalendarEventEntry> entries,
			List<Task> pendingList, int i, int j) {
		
		Task editedTask = pendingList.get(i);
		editedTask.setWorkInfo(
				entries.get(j).getTitle().getPlainText());
		try {
			setupDateForTimedTask(entries, j, editedTask);
		} catch (IndexOutOfBoundsException e) {
			String recurData = entries.get(j).getRecurrence().getValue();
			if (recurData.contains("VALUE=DATE:")) {
				setupDateForAllDayRecurringTask(editedTask, recurData);
			} else {// timed recurring event
				setupDateForTimedRecurringTask(editedTask, recurData);
			}

		}

		if (!entries.get(j).getReminder().isEmpty()) {
			editedTask.setIsImportant(true);
		} else {
			editedTask.setIsImportant(false);
		}
		editedTask.updateLatestModifiedDate();
	}
	
	/**
	 * This function is used to update an event on Google Calendar to be the
	 * same with a task on iDo
	 * 
	 * @param service
	 *            the currently connected service
	 * @param entries
	 *            the list of entries on Google Calendar
	 * @param feedURL
	 *            the URl of the calendar
	 * @param toBeUpdatedOnGCal
	 *            the list of events to be updated on Google Calendar
	 * @param pendingList
	 *            the list of pending tasks
	 * @param i
	 *            the index of the task
	 * @param j
	 *            the index of the event entry
	 */
	private void updateModifiedEventOnGCal(CalendarService service,
			List<CalendarEventEntry> entries, URL feedURL,
			List<CalendarEventEntry> toBeUpdatedOnGCal, List<Task> pendingList,
			int i, int j) throws IOException, ServiceException {
		entries.get(j).setTitle(
				new PlainTextConstruct(pendingList.get(i)
						.getWorkInfo()));
		
		if (isNormalTask(pendingList, i)) {
			updateToNormalEntry(service, entries, feedURL, toBeUpdatedOnGCal, pendingList, i, j);
		} else {
			updateToRecurrenceEntry(entries, toBeUpdatedOnGCal, pendingList, i, j);
		}
		
		if (pendingList.get(i).isImportantTask() == true) {
			setReminder(entries.get(j));
		} else {
			entries.get(j).getReminder().clear();
		}
	}
	
	/**
	 * This function is used to update an event entry to a normal entry
	 * 
	 * @param service
	 *            the currently connected service
	 * @param entries
	 *            the list of entries on Google Calendar
	 * @param feedURL
	 *            the URL of the calendar
	 * @param toBeUpdatedOnGCal
	 *            the list of entries to be updated on Google Calendar
	 * @param pendingList
	 *            the list of pending tasks in iDo
	 * @param i
	 *            the index of task
	 * @param j
	 *            the index of entry
	 */
	private void updateToNormalEntry(CalendarService service,
			List<CalendarEventEntry> entries, URL feedURL,
			List<CalendarEventEntry> toBeUpdatedOnGCal, List<Task> pendingList,
			int i, int j) throws IOException, ServiceException {
		if (isNormalEntry(entries, j)) {
			entries.get(j).getTimes().get(0).setStartTime(
							pendingList.get(i).getStartDate().returnInDateTimeFormat());
			entries.get(j).getTimes().get(0).setEndTime(
							pendingList.get(i).getEndDate().returnInDateTimeFormat());
			toBeUpdatedOnGCal.add(entries.get(j));
		} else {
			convertFromRecurringToNormalEntry(service, entries, feedURL, pendingList, i, j);
		}
	}
	
	/**
	 * This function is used to update an event entry to a recurrence entry
	 * 
	 * @param entries
	 *            the list of entries in Google Calendar
	 * @param toBeUpdatedOnGCal
	 *            the list of entries to be updated on Google Calendar
	 * @param pendingList
	 *            the list of pending tasks in iDo
	 * @param i
	 *            the index of the task
	 * @param j
	 *            the index of the entry
	 */
	private void updateToRecurrenceEntry(List<CalendarEventEntry> entries,
			List<CalendarEventEntry> toBeUpdatedOnGCal, List<Task> pendingList,
			int i, int j) {
		entries.get(j).getTimes().clear();
		if (!pendingList.get(i).isFloatingTask()) {
			String startDate = pendingList.get(i)
					.getStartDate()
					.returnInRecurringFormat();
			String endDate = pendingList.get(i)
					.getEndDate()
					.returnInRecurringFormat();
			String freq = pendingList.get(i).getTag()
					.getRepetition();
			int interval = pendingList.get(i).getTag()
					.getInterval();
			int count = pendingList.get(i)
					.getNumOccurrences();
			
			String recurData = setRecurrenceData(startDate, endDate, freq, null, count, interval);
			Recurrence rec = new Recurrence();
			rec.setValue(recurData);
			entries.get(j).setRecurrence(rec);
		}
		toBeUpdatedOnGCal.add(entries.get(j));
	}
	
	//@author A0105523U
	/**
	 * This function is used to set the number of occurences for a recurring
	 * data to be passed to the constructor of Recurrence object
	 * 
	 * @param occurrence
	 *            the number of occurrences
	 * @param recurData
	 *            the recurring data in String format
	 * @return the modified recurring data
	 */
	private String setOccurrenceForEntry(int occurrence,
			String recurData) {
		
		if (occurrence > 0) {
			recurData = recurData + ";COUNT=" + occurrence;
		}
		return recurData;
	}
	
	/**
	 * This function is used to set the interval for a recurring data to be
	 * passed to the constructor of Recurrence object
	 * 
	 * @param interval
	 *            the interval of the repetition
	 * @param recurData
	 *            the recurring data in String format
	 * @return the modified recurring data
	 */
	private String setIntervalForEntry(int interval,
			String recurData) {
		
		if (interval > 0) {
			recurData = recurData + ";INTERVAL=" + interval;
		}
		return recurData;
	}
	
	/**
	 * This function is used to set the frequency for a recurring datat to be
	 * passed to the constructor of Recurrence object
	 * 
	 * @param freq
	 *            the frequency of the repetition
	 * @param recurData
	 *            the recurring data in String format
	 * @return the modified recurring data
	 */
	private String setFrequencyForEntry(String freq, String recurData) {
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
					+ "YEARLY";
		}
		return recurData;
	}
	
	/**
	 * This function is used to convert a recurring event into a normal event on
	 * Google Calendar
	 * 
	 * @param service
	 *            the currently connected service
	 * @param entries
	 *            the list of entries on Google Calendar
	 * @param feedURL
	 *            the URL of the calendar
	 * @param pendingList
	 *            the list of pending tasks on iDo
	 * @param i
	 *            the index of the task
	 * @param j
	 *            the index of the entry
	 */
	private void convertFromRecurringToNormalEntry(CalendarService service,
			List<CalendarEventEntry> entries, URL feedURL,
			List<Task> pendingList, int i, int j) throws IOException,
			ServiceException {
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
	
	/**
	 * This function is used to check if the event entry is a normal entry
	 * 
	 * @param entries
	 *            list of entries on Google Calendar
	 * @param j
	 *            the index of the entry
	 * @return true if the event is indeed a normal event or vice versa
	 */
	private boolean isNormalEntry(List<CalendarEventEntry> entries, int j) {
		return entries.get(j).getRecurrence() == null;
	}
	
	/**
	 * This function is used to check if the task is a normal task
	 * 
	 * @param pendingList
	 *            the list of pending tasks in iDo
	 * @param i
	 *            the index of the task
	 * @return true if the task is indeed a normal task or vice versa
	 */
	private boolean isNormalTask(List<Task> pendingList, int i) {
		return !pendingList.get(i).isRecurringTask()
				&& pendingList.get(i).getStartDate() != null;
	}

	/**
	 * This function is used to sync all deleted tasks in iDo to Google Calendar
	 * so that these corresponding events on Google Calendar are also deleted
	 * 
	 * @param service
	 *            the currently connected service
	 * @param model
	 *            the model of tasks from the application
	 * @param entries
	 *            the list of entries in iDo
	 * @param feedUrl
	 *            the URL of the calendar
	 */
	private void syncDeletedTasksToGCal(CalendarService service, Model model,
			List<CalendarEventEntry> entries, URL feedUrl)
			throws ServiceException, IOException {
		List<CalendarEventEntry> tobeDelete = new ArrayList<CalendarEventEntry>();
		ObservableList<Task> completedTasks = model.getCompleteList();
		ObservableList<Task> deletedTasks = model.getTrashList();
		deleteTasksInOtherTabsOnGCal(entries, tobeDelete, completedTasks);
		deleteTasksInOtherTabsOnGCal(entries, tobeDelete, deletedTasks);
		deleteEvents(service, tobeDelete, feedUrl);
		
	}
	
	/**
	 * Delete list of tasks which are moved from pending tab to complete tab or
	 * trash tab
	 * 
	 * @param entries
	 *            the list of tasks in iDo
	 * @param tobeDelete
	 *            the list of event entries to be deleted on Google Calendar
	 * @param movedTasks
	 *            the list of tasks in complete tab or trash tab
	 */
	private void deleteTasksInOtherTabsOnGCal(List<CalendarEventEntry> entries,
			List<CalendarEventEntry> tobeDelete,
			ObservableList<Task> movedTasks) {
		for (int i = 0; i < movedTasks.size(); i++) {
			if (movedTasks.get(i).getStatus() == Task.Status.DELETED) {
				for (int j = 0; j < entries.size(); j++) {
					if (movedTasks.get(i).getIndexId()
							.equals(entries.get(j).getId())) {
						tobeDelete.add(entries.get(j));
						break;
					}
				}
				movedTasks.get(i).setStatus(Task.Status.UNCHANGED);
			}
		}
	}
	
	/**
	 * This function is used to sync all deleted events on Google Calendar back
	 * to iDo so that the corresponding tasks in iDo are also deleted.
	 * 
	 * @param entries
	 *            the list of entries on Google Calendar
	 * @param model
	 *            the model of tasks from the application
	 */
	private void deleteTasksLocally(List<CalendarEventEntry> entries,
			Model model) {
		List<Task> pendingList = model.getPendingList();
		ArrayList<String> entryIds = getGCalEntriesIdList(entries);
		for (int i = 0; i < pendingList.size(); i++) {
			if (!entryIds.contains(pendingList.get(i).getIndexId()) && pendingList.get(i).getStatus() != Task.Status.ADDED_WHEN_SYNC) {
				Task t = pendingList.remove(i);
				t.setStatus(Task.Status.UNCHANGED);
				model.getTrashList().add(t);
				i--;
			}
		}
	}
	
	/**
	 * This function is used to get the list of entries IDs from iDo
	 * 
	 * @param entries
	 *            the list of entries on Google Calendar
	 * @return the list of IDs
	 */
	private ArrayList<String> getGCalEntriesIdList(List<CalendarEventEntry> entries) {
		ArrayList<String> entryIds = new ArrayList<String>();
		for (int i = 0; i < entries.size(); i++) {
			entryIds.add(entries.get(i).getId());
		}
		return entryIds;
	}
	
	/**
	 * This function is used to sync newly added events on Google Calendar to
	 * the local application iDo
	 * 
	 * @param entries
	 *            the list of entries on Google Calendar
	 * @param model
	 *            the model of tasks from the application
	 */
	private void addEventsLocally(List<CalendarEventEntry> entries, Model model) {
		List<Task> pendingList = model.getPendingList();
		ArrayList<String> taskIds = getLocalEntriesIdList(pendingList);
		taskIds.addAll(model.getRemovedIdDuringSync());
		
		for (int i = 0; i < entries.size(); i++) {
			CalendarEventEntry e = entries.get(i);
			boolean isNewEntry = !taskIds.contains(e.getId());
			if (isNewEntry) {
				processAddingNewTask(entries, pendingList, i, e);
			}
		}
	}
	
	/**
	 * This function is used to add a new task to iDo
	 * 
	 * @param entries
	 *            the list of entries on Google Calendar
	 * @param pendingList
	 *            the list of pending tasks in iDo
	 * @param i
	 *            the index of the entry
	 * @param e
	 *            the actual entry
	 */
	private void processAddingNewTask(List<CalendarEventEntry> entries,
			List<Task> pendingList, int i, CalendarEventEntry e) {
		Task newTask = new Task();
		newTask.setWorkInfo(e.getTitle().getPlainText());
		try {
			setupDateForTimedTask(entries, i, newTask);
		} catch (IndexOutOfBoundsException | NullPointerException exception) { // which means it is a recurring event
			String recurData = e.getRecurrence().getValue();
			if (recurData.contains("VALUE=DATE:")) {// all day recurring event
				setupDateForAllDayRecurringTask(newTask, recurData);
			} else {// timed recurring event
				setupDateForTimedRecurringTask(newTask, recurData);
			}
		}
		newTask.setIndexId(e.getId());
		newTask.setStatus(Task.Status.UNCHANGED);
		if (!e.getReminder().isEmpty()){
			newTask.setIsImportant(true);
		}
		pendingList.add(newTask);
	}
	
	/**
	 * This function is used to set date for a timed task with the given event
	 * entry from Google Calendar
	 * 
	 * @param entries
	 *            the list of entries in Google Calendar
	 * @param i
	 *            the index of the entry
	 * @param newTask
	 *            the modified task
	 */
	private void setupDateForTimedTask(List<CalendarEventEntry> entries, int i,
			Task newTask) {
		DateTime start = entries.get(i).getTimes().get(0)
				.getStartTime();
		DateTime end = entries.get(i).getTimes().get(0)
				.getEndTime();
		CustomDate startDate = new CustomDate(start);
		CustomDate endDate = new CustomDate(end);
		newTask.setStartDate(startDate);
		newTask.setEndDate(endDate);
		newTask.setTag(new Tag(newTask.getTag().getTag(), "null"));
	}
	
	/**
	 * This function is used to set date for a recurring task with the given
	 * recurring event entry from Google Calendar
	 * 
	 * @param newTask
	 *            the modified task
	 * @param recurData
	 *            the recurring data
	 */
	private void setupDateForTimedRecurringTask(Task newTask, String recurData) {
		int startDateIndex = recurData.indexOf(":");
		CustomDate startDate = getDateForTimedRecurringTask(recurData,
				startDateIndex);
		int endDateIndex = recurData.indexOf(":",
				startDateIndex + 1);
		CustomDate endDate = getDateForTimedRecurringTask(recurData,
				endDateIndex);
		newTask.setStartDate(startDate);
		newTask.setEndDate(endDate);

		String freq = getFrequency(recurData);
		freq = checkInterval(recurData, freq);
		newTask.setTag(new Tag(newTask.getTag().getTag(), freq.toLowerCase()));

		checkOccurrencesForTimedRecurringTask(newTask, recurData, freq);
	}
	
	/**
	 * This function is used to update the number of occurrences for a timed
	 * recurring task
	 * 
	 * @param newTask
	 *            the modified task
	 * @param recurData
	 *            the recurring data
	 * @param freq
	 *            the frequency of repetition
	 */
	private void checkOccurrencesForTimedRecurringTask(Task newTask,
			String recurData, String freq) {
		newTask.setTag(new Tag(newTask.getTag().getTag(), freq.toLowerCase()));
		if (recurData.contains("COUNT=")) {
			int startIndex = recurData.indexOf("COUNT=") + 6;
			int endIndex = recurData.indexOf(";", startIndex);
			if (endIndex < 0) {
				endIndex = recurData.indexOf("\n", startIndex);
			}
			int count = Integer.parseInt(recurData.substring(startIndex, endIndex));
			newTask.setNumOccurrences(count);
		}
	}
	
	/**
	 * This function is used to extract the date from the recurring data in
	 * format for timed recurring task
	 * 
	 * @param recurData
	 *            the recurring data
	 * @param dateIndex
	 *            the start index of the date
	 * @return the converted date into CustomDate object
	 */
	private CustomDate getDateForTimedRecurringTask(String recurData,
			int dateIndex) {
		String startDateString = recurData.substring(
				dateIndex + 1, dateIndex + 16);
		CustomDate date = new CustomDate(
				startDateString.substring(6, 8) + "/"
						+ startDateString.substring(4, 6) + "/"
						+ startDateString.substring(0, 4) + " "
						+ startDateString.substring(9, 11)
						+ ":"
						+ startDateString.substring(11, 13));
		return date;
	}
	
	/**
	 * This function is used to update the dates for an all day recurring task
	 * with the given all day recurring event entry from the google Calendar
	 * 
	 * @param recurData
	 *            the recurring data
	 * @param task
	 *            the modified task
	 */
	private void setupDateForAllDayRecurringTask(Task task, String recurData) {
		int startDateIndex = recurData.indexOf(":");
		CustomDate startDate = getDateForAllDayRecurringEvent(recurData, startDateIndex);
		int endDateIndex = recurData.indexOf(":", startDateIndex + 1);
		CustomDate endDate = getDateForAllDayRecurringEvent(recurData, endDateIndex);
		task.setStartDate(startDate);
		task.setEndDate(endDate);

		setupRecurrenceInfoForAllDayRecurringTask(task, recurData);
	}

	/**
	 * This function is used to setup the repetition info for a task from the
	 * given recurring data
	 * 
	 * @param task
	 *            the modified task
	 * @param recurData
	 *            the recurring data
	 */
	private void setupRecurrenceInfoForAllDayRecurringTask(Task task,
			String recurData) {
		String freq = getFrequency(recurData);
		freq = checkInterval(recurData, freq);
		task.setTag(new Tag(task.getTag().getTag(), freq.toLowerCase()));
		checkOccurrencesForAllDayRecurringTask(task, recurData, freq);
	}
	
	/**
	 * This function is used to update the number of occurrences of an all day
	 * recurring task
	 * 
	 * @param task
	 *            the modified task
	 * @param recurData
	 *            the recurring data
	 * @param freq
	 *            the frequency of repetition
	 */
	private void checkOccurrencesForAllDayRecurringTask(Task task, String recurData, String freq) {
		if (recurData.contains("COUNT=")) {
			int startIndex = recurData.indexOf("COUNT=") + 6;
			int endIndex = recurData.indexOf(";", startIndex);
			if (endIndex < 0) {
				endIndex = recurData.indexOf("\n", startIndex);
			}
			int count = Integer.parseInt(recurData.substring(
					startIndex, endIndex));
			task.setNumOccurrences(count);
		} else if (freq.equals("DAILY")) {
			setFloatingTask(task);
		}
	}
	
	/**
	 * This function is used to set a task to be a floating task
	 * @param task
	 */
	private void setFloatingTask(Task task) {
		task.setStartDate(null);
		task.setEndDate(null);
		task.setTag(new Tag(task.getTag().getTag(), "null"));
	}
	
	/**
	 * This function is used to process the interval and update it to the
	 * frequency from the given recurring data
	 * 
	 * @param recurData
	 *            the recurring data
	 * @param freq
	 *            the frequency of repetition
	 * @return the updated frequency
	 */
	private String checkInterval(String recurData, String freq) {
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
		return freq;
	}	
	
	/**
	 * This function is used to get the basic frequency from the recurring data
	 * (does not consider the interval)
	 * 
	 * @param recurData
	 *            the recurring data
	 * @return the basic corresponding frequency
	 */
	private String getFrequency(String recurData) {
		int freqStartIndex = recurData.indexOf("FREQ=");
		int freqEndIndex = getFrequencyEndIndex(recurData, freqStartIndex);
		String freq = recurData.substring(freqStartIndex + 5, freqEndIndex);
		return freq;
	}
	
	// Get the last index of the frequency in the recurring data with the given start index
	private int getFrequencyEndIndex(String recurData, int freqStartIndex) {
		int freqEndIndex;
		if (recurData.contains("BYDAY")
				|| recurData.contains("BYMONTHDAY")
				|| recurData.contains("COUNT")
				|| recurData.contains("INTERVAL")) {
			freqEndIndex = recurData.indexOf(";",
					freqStartIndex);
		} else {
			freqEndIndex = recurData.indexOf("\n", freqStartIndex);
			if (freqEndIndex == -1){
				freqEndIndex = recurData.length();
			}
		}
		return freqEndIndex;
	}
	
	/**
	 * Get date from the recurrence data by indicating the start index of date
	 * @param recurData
	 * @param dateIndex
	 * @return
	 */
	private CustomDate getDateForAllDayRecurringEvent(String recurData,
			int dateIndex) {
		String endDateString = recurData.substring(dateIndex + 1, dateIndex + 9);
		CustomDate date = new CustomDate(endDateString.substring(6, 8) + "/"
						+ endDateString.substring(4, 6) + "/" + endDateString.substring(0, 4));
		return date;
	}

	
	/**
	 * Get all entries ID of events in the local application iDo
	 * 
	 * @param pendingList
	 *            the list to tasks to retrieve IDs
	 * @return the list of entries IDs
	 */
	private ArrayList<String> getLocalEntriesIdList(List<Task> pendingList) {
		ArrayList<String> taskIds = new ArrayList<String>();
		for (int i = 0; i < pendingList.size(); i++) {
			taskIds.add(pendingList.get(i).getIndexId());
		}
		return taskIds;
	}
	
	/**
	 * Retrieve all event entries from calendar "iDo" on Google Calendar
	 *
	 */
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
			boolean isImportant, boolean isQuickAdd, URL feedUrl)
			throws ServiceException, IOException {
		CalendarEventEntry myEntry = new CalendarEventEntry();

		myEntry.setTitle(new PlainTextConstruct(title));
		myEntry.setQuickAdd(isQuickAdd);

		if (recurData == null) {
			setTimeForNormalEvent(startDate, endDate, myEntry);
		} else {
			setTimeForRecurringEvent(recurData, myEntry);
		}

		if (isImportant) {
			setReminder(myEntry);
		}

		// Send the request and receive the response:
		return service.insert(feedUrl, myEntry);
	}
	
	/**
	 * This function is used to set the recurrence data for an event entry
	 * 
	 * @param recurData
	 *            the recurring data
	 * @param myEntry
	 *            the modified event entry on Google Calendar
	 */
	private void setTimeForRecurringEvent(String recurData,
			CalendarEventEntry myEntry) {
		Recurrence recur = new Recurrence();
		recur.setValue(recurData);
		myEntry.setRecurrence(recur);
	}
	
	/**
	 * Set time for normal event
	 * 
	 * @param startDate
	 *            the start date of new task
	 * @param endDate
	 *            the end date of new task
	 * @param myEntry
	 *            the added entry on Google Calendar
	 */
	private void setTimeForNormalEvent(CustomDate startDate,
			CustomDate endDate, CalendarEventEntry myEntry) {
		DateTime startTime = startDate.returnInDateTimeFormat();
		DateTime endTime = endDate.returnInDateTimeFormat();
		
		When eventTimes = new When();
		eventTimes.setStartTime(startTime);
		eventTimes.setEndTime(endTime);
		myEntry.addTime(eventTimes);
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
		return createEvent(service, eventContent, startDate, endDate, null, isImport, false, feedUrl);
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

		String recurData = setRecurrenceData(startDate, endDate, freq, until, count, interval);

		return createEvent(service, eventContent, null, null, recurData, isImport, false, feedUrl);
	}
	
	/**
	 * This function is used to set the recurrence data suitable to pass to the
	 * constructor of Recurrence object
	 * 
	 * @param startDate
	 *            the start date in String format
	 * @param endDate
	 *            the end date in String format
	 * @param freq
	 *            the frequency in String format
	 * @param until
	 *            the until limit in String format
	 * @param count
	 *            the number of occurrences in String format
	 * @param interval
	 *            the interval between period
	 * @return the recurrence info
	 */
	private String setRecurrenceData(String startDate, String endDate,
			String freq, String until, int count, int interval) {
		String recurData = "DTSTART;TZID=" + TimeZone.getDefault().getID()
				+ ":" + startDate + "\r\n" + "DTEND;TZID="
				+ TimeZone.getDefault().getID() + ":" + endDate + "\r\n";
		
		recurData = setFrequencyForEntry(freq, recurData);
		recurData = setIntervalForEntry(interval, recurData);
		recurData = setOccurrenceForEntry(count, recurData);
		recurData += "\r\n";
		return recurData;
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

		service.batch(batchUrl, batchRequest);
	}

	/**
	 * Makes a batch request to update all the events in the given list. If any
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
	 *            A list of CalendarEventEntry objects to update.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	private void updateEvents(CalendarService service,
			List<CalendarEventEntry> eventsToUpdate, URL feedUrl)
			throws ServiceException, IOException {
		// Add each item in eventsToUpdate to the batch request.
		CalendarEventFeed batchRequest = new CalendarEventFeed();
		
		for (int i = 0; i < eventsToUpdate.size(); i++) {
			CalendarEventEntry toUpdate = eventsToUpdate.get(i);
			// Modify the entry toUpdate with batch ID and operation type.
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

		service.batch(batchUrl, batchRequest);
	}
	
	/**
	 * This function is used to set reminder for an event in the calendar
	 * 
	 * @param event
	 *            the modified event
	 */
	private void setReminder(CalendarEventEntry event) {
		Reminder r = new Reminder();
		Method m = Method.ALERT;

		r.setMinutes(REMINDER_MINUTES);
		r.setMethod(m);
		event.getReminder().add(r);
	}
	
	/**
	 * This function is used to check whether the connected service has already
	 * had the calendar "iDo" yet If yes, it will return the calendar ID. Else,
	 * return null
	 * 
	 * @param service
	 *            the currently connected service
	 * @param feedUrl
	 *            the URL of Google Calendar
	 * @return the calendar ID if exists
	 */
	private String isCalendarExist(CalendarService service, URL feedUrl) {
		String calendarId = null;
		CalendarFeed resultFeed = null;
		List<CalendarEntry> entries = null;
		
		try {
			resultFeed = service.getFeed(feedUrl, CalendarFeed.class);
			entries = resultFeed.getEntries();
		} catch (IOException e) {
			logger.log(Level.INFO, "Error in IO when checking calendar");
		} catch (ServiceException e) {
			logger.log(Level.INFO, "Error in connecting to service when checking calendar");
		}
		
		if (entries != null) {
			for (int i = 0; i < entries.size(); i++) {
				if (entries.get(i).getTitle().getPlainText().equals(CALENDAR_TITLE)) {
					calendarId = trimId(entries.get(i).getId());
					break;
				}
			}
		}
		return calendarId;
	}
}
