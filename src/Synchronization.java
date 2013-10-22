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
import com.google.gdata.data.calendar.HiddenProperty;
import com.google.gdata.data.calendar.TimeZoneProperty;
import com.google.gdata.data.extensions.Recurrence;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class Synchronization {

	/* messages */
	private static final String CALENDAR_TITLE = "iDo";
	private static final String CALENDAR_SUMMARY = "This calendar synchronizes with iDo Task Manager.";
	private static final String SERVICE_NAME = "sg.edu.nus.cs2103aug2013-w13-03j";

	/* use username and password to login */
	String username = null;
	String password = null;

	/* model */
	Model model;

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

	public void execute() {

		try {
			initService();
		} catch (AuthenticationException e1) {
			System.out.println("fail to init");
		}

		// form feed url
		if(eventFeedUrl == null) {
			try {
				eventFeedUrl = formEventFeedUrl(service);
			} catch (IOException | ServiceException e) {
				System.out.println("fail to form event url");
			}
		}

		// sync newly added tasks to GCal
		try {
			syncNewTasksToGCal(service, model, eventFeedUrl);
		} catch (ServiceException e) {
			System.out.println("fail to sync new");
		} catch (IOException e) {
			System.out.println("fail to sync new");
		}
		
		System.out.println("1");
		// get all events from Google calendar
		try {
			eventEntry = getEventsFromGCal(service, eventFeedUrl);
		} catch (IOException e) {
			System.out.println("fail to pull: io");
		} catch (ServiceException e) {
			System.out.println("fail to pull: service");
		}
		
		System.out.println("1");
		/*
		// update Task
		try {
			updateUnchangedTasks(service, model, eventEntry, eventFeedUrl);
		} catch (ServiceException e) {
			System.out.println("fail to update");
		} catch (IOException e) {
			System.out.println("fail to update");
		}
		System.out.println("tototot");
		System.out.println("1");
		*/
		try {
			// delete events on GCal which have been deleted locally
			syncDeletedTasksToGCal(service, model, eventEntry, eventFeedUrl);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("1");

		// get all events from Google calendar
		try {
			eventEntry = getEventsFromGCal(service, eventFeedUrl);
		} catch (IOException e) {
			System.out.println("fail to pull: io");
		} catch (ServiceException e) {
			System.out.println("fail to pull: service");
		}

		System.out.println("1");
		// delete tasks locally which have been deleted on GCal
		deleteTasksLocally(eventEntry, model);

		System.out.println("1");
		// add tasks locally which have been added on GCal
		addEventsLocally(eventEntry, model);
		System.out.println("Done!");
		System.out.println("1");
	}

	private void initService() throws AuthenticationException {
		// create a new service
		service = new CalendarService(SERVICE_NAME);

		// authenticate using ClientLogin
		service.setUserCredentials(username, password);
	}
	
	void setCalendarID(String calID) {
		try {
		eventFeedUrl = new URL(calID);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	URL formEventFeedUrl(CalendarService service) throws IOException,
			ServiceException {
		if (calendarId == null) {
			URL owncalUrl = new URL(METAFEED_URL_BASE + username
					+ OWNCALENDARS_FEED_URL_SUFFIX);
			CalendarEntry calendar = createCalendar(service, owncalUrl);
			calendarId = trimId(calendar.getId());
		}
		return new URL(METAFEED_URL_BASE + calendarId + EVENT_FEED_URL_SUFFIX);
	}

	private String trimId(String id) {
		String[] temp = id.trim().split("/");
		return temp[temp.length - 1].toString();
	}

	void syncNewTasksToGCal(CalendarService service, Model model, URL feedUrl)
			throws ServiceException, IOException {
		ObservableList<Task> pendingList = model.getPendingList();
		for (int i = 0; i < pendingList.size(); i++) {
			Task task = pendingList.get(i);
			if (task.getStatus() == Task.Status.NEWLY_ADDED) {
				CalendarEventEntry e;
				 if(task.isRecurringTask()){
					 e = createRecurringEvent(service, task.getWorkInfo(),
					 task.getStartDate(), task.getEndDate(), task.getTag().getRepetition(), feedUrl);
				 } else {
					 e = createSingleEvent(service, task.getWorkInfo(),
					 task.getStartDate(), task.getEndDate(), feedUrl);
				 }
				task.setIndexId(e.getId());
				task.setStatus(Task.Status.UNCHANGED);
			}
		}
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
							entries.get(j)
									.getTimes()
									.get(0)
									.setStartTime(
											pendingList.get(i).getStartDate()
													.returnInDateTimeFormat());
							entries.get(j)
									.getTimes()
									.get(0)
									.setEndTime(
											pendingList.get(i).getEndDate()
													.returnInDateTimeFormat());
							toBeUpdatedOnGCal.add(entries.get(j));
						} else {
							pendingList.get(i).setWorkInfo(
									entries.get(j).getTitle().getPlainText());
							pendingList.get(i).setStartDate(
									new CustomDate(entries.get(j).getTimes()
											.get(0).getStartTime()));
							pendingList.get(i).setEndDate(
									new CustomDate(entries.get(j).getTimes()
											.get(0).getEndTime()));
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
			if (!entryIds.contains(pendingList.get(i).getIndexId())) {
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
		for (int i = 0; i < entries.size(); i++) {
			CalendarEventEntry e = entries.get(i);
			if (!taskIds.contains(e.getId())) {
				System.out.println("new: " + e.getTitle().getPlainText());
				Task t = new Task();
				t.setWorkInfo(e.getTitle().getPlainText());

				System.out.println("stime: "
						+ e.getTimes().get(0).getStartTime().toString());

				t.setStartDate(new CustomDate(e.getTimes().get(0)
						.getStartTime()));
				System.out.println("etime: "
						+ e.getTimes().get(0).getEndTime().toString());

				t.setEndDate(new CustomDate(e.getTimes().get(0).getEndTime()));
				t.setIndexId(e.getId());
				t.setStatus(Task.Status.UNCHANGED);
				/*
				 * if(e.getRecurrence().getValue() != null){ t.setTag(new
				 * Tag(null, e.getRecurrence().getValue())); }
				 */
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
			boolean isQuickAdd, URL feedUrl) throws ServiceException,
			IOException {
		CalendarEventEntry myEntry = new CalendarEventEntry();

		myEntry.setTitle(new PlainTextConstruct(title));
		myEntry.setQuickAdd(isQuickAdd);

		// If a recurrence was requested, add it. Otherwise, set the
		// time (the current date and time) and duration (30 minutes)
		// of the event.
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

		// Send the request and receive the response:
		return service.insert(feedUrl, myEntry);
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
			URL feedUrl) throws ServiceException, IOException {
		return createEvent(service, eventContent, startDate, endDate, null,
				false, feedUrl);
	}

	/**
	 * Creates a quick add event.
	 * 
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param quickAddContent
	 *            The quick add text, including the event title, date and time.
	 * @return The newly-created CalendarEventEntry.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	CalendarEventEntry createQuickAddEvent(CalendarService service,
			String quickAddContent, URL feedUrl) throws ServiceException,
			IOException {
		return createEvent(service, quickAddContent, null, null, null, true,
				feedUrl);
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
			String eventContent, CustomDate startDate, CustomDate endDate,
			String freq, URL feedUrl) throws ServiceException, IOException {
		
		String recurData = "DTSTART;TZID="+TimeZone.getDefault().getID()+":"
				+ startDate.returnInRecurringFormat() + "\r\n"
				+ "DTEND;TZID="+TimeZone.getDefault().getID()+":"
				+ endDate.returnInRecurringFormat() + "\r\n"
				+ "RRULE:FREQ=" + freq.toUpperCase() + "\r\n";

		System.out.println(recurData);
		return createEvent(service, eventContent, null, null, recurData, false,
				feedUrl);
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
}
