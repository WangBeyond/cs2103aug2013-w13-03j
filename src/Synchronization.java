import java.io.IOException;
import java.net.MalformedURLException;
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
import com.google.gdata.data.calendar.ColorProperty;
import com.google.gdata.data.calendar.HiddenProperty;
import com.google.gdata.data.calendar.TimeZoneProperty;
import com.google.gdata.data.calendar.WebContent;
import com.google.gdata.data.extensions.Recurrence;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class Synchronization {
	
	/*messages*/
	private static final String CALENDAR_TITLE = "iDo";
	private static final String CALENDAR_SUMMARY = "This calendar synchronizes with iDo Task Manager.";
	private static final String SERVICE_NAME = "sg.edu.nus.cs2103aug2013-w13-03j";
	
	/*use username and password to login*/
	 String username = null;
	 String password = null;
	 
	/*model*/
	Model model;
	
	/*calendar id*/
	 String calendarId = null;
	
	/*calendar service*/
	CalendarService service;
	
	/*The base URL for a user's calendar metafeed (needs a username appended).*/
	private static final String METAFEED_URL_BASE = "https://www.google.com/calendar/feeds/";
	
	// The string to add to the user's metafeedUrl to access the owncalendars feed.
	private static final String OWNCALENDARS_FEED_URL_SUFFIX = "/owncalendars/full";
	
	private static final String EVENT_FEED_URL_SUFFIX = "/private/full";
	
	URL owncalendarsFeedUrl = null;
	URL eventFeedUrl = null;
	
	/*a list of event entry from Google calendar*/
	private List<CalendarEventEntry> eventEntry = new ArrayList<CalendarEventEntry>();
	
	/*a list of tasks which have been deleted locally since last synchronization*/
	private List<Task> deletedTasks;
	
	/*a list of tasks which have been added locally since last synchronization*/
	private List<Task> newlyAddedTasks;
	
	private List<Task> unchangedTasks;
	
	public Synchronization(String n, String p, Model model){
		username = n;
		password = p;
		this.model = model;
	}
	
	public void execute() {
		//initialize
		try {
			init();
		} catch (AuthenticationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//process model
		processModel(model);
		
		//form feed url
		try {
			eventFeedUrl = formEventFeedUrl(service);
		} catch (IOException | ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//get all events from Google calendar
		try {
			eventEntry = getEventsFromGCal(service, eventFeedUrl);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//sync newly added tasks to GCal
		try {
			syncNewTasksToGCal(service, newlyAddedTasks, eventFeedUrl);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//delete events on GCal which have been deleted locally
		snycDeletedTasksToGCal(service, deletedTasks, eventEntry, eventFeedUrl);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//delete tasks locally which have been deleted on GCal
		deleteTasksLocally(unchangedTasks, eventEntry);
		
		//add tasks locally which have been added on GCal
		addEventsLocally(unchangedTasks, eventEntry);
	}
	
	void processModel(Model model){
		List<Task> temp = model.getPendingList();
		for(int i=0;i<temp.size();i++){
			if(temp.get(i).getStatus().equals(Task.Status.UNCHANGED)){
				unchangedTasks.add(temp.get(i));
			} else if(temp.get(i).getStatus().equals(Task.Status.NEWLY_ADDED)){
				newlyAddedTasks.add(temp.get(i));
			} else {
				throw new Error("something");
			}
		}
		
		deletedTasks = model.getCompleteList();
		deletedTasks.addAll(model.getTrashList());
	}
	
	void init() throws AuthenticationException{
		//create a new service
		service = new CalendarService(SERVICE_NAME);
		
		//authenticate using ClientLogin
		service.setUserCredentials(username, password);
	}
	
	void loadLocalData(Model model){
		
	}
	
	URL formEventFeedUrl(CalendarService service) throws IOException, ServiceException{
		if(calendarId == null){
			URL owncalUrl = new URL(METAFEED_URL_BASE + username + OWNCALENDARS_FEED_URL_SUFFIX);
			CalendarEntry calendar = createCalendar(service, owncalUrl);
			calendarId = trimId(calendar.getId());
		}
		return new URL(METAFEED_URL_BASE + calendarId + EVENT_FEED_URL_SUFFIX);	
	}
	
	private String trimId(String id){
		String[] temp = id.trim().split("/");
		return temp[temp.length-1].toString();
	}
	
	void syncNewTasksToGCal(CalendarService service, List<Task> newlyAddedTasks, URL feedUrl) throws ServiceException, IOException{
		for(int i=0; i<newlyAddedTasks.size(); i++){
			Task task = newlyAddedTasks.get(i);
			if(task.isRecurringTask()){
				createRecurringEvent(service, task.getWorkInfo(), task.getStartDate(), task.getEndDate(), "WEEKLY", feedUrl);
			} else {
				createSingleEvent(service, task.getWorkInfo(), task.getStartDate(), task.getEndDate(),feedUrl);
			}
		}
	}
	
	private void snycDeletedTasksToGCal(CalendarService service, List<Task> tasks, List<CalendarEventEntry> entries, URL feedUrl) throws ServiceException, IOException{
		List<CalendarEventEntry> tobeDelete = new ArrayList<CalendarEventEntry>();
		for(int i=0; i<tasks.size(); i++){
			for(int j=0; j<entries.size();j++){
				if(tasks.get(i).equals(entries.get(j).getId())){
					tobeDelete.add(entries.get(j));
					break;
				}
			}
		}
		deleteEvents(service, tobeDelete, feedUrl);
	}
	
	private void deleteTasksLocally(List<Task> remainingTasks, List<CalendarEventEntry> entries){
		ArrayList<String> entryId = new ArrayList<String>();
		for(int i=0;i<entries.size();i++){
			entryId.add(entries.get(i).getId());
		}
		for(int i=0;i<remainingTasks.size();i++){
			if(!entryId.contains(remainingTasks.get(i).getIndexId())){
				/*delete this task on the disk. to be implemented.*/
			}
		}
	}
	
	private void addEventsLocally(List<Task> remainingTasks, List<CalendarEventEntry> entries){
		ArrayList<String> entryId = new ArrayList<String>();
		ArrayList<String> taskId = new ArrayList<String>();
		for(int i=0;i<entries.size();i++){
			entryId.add(entries.get(i).getId());
		}
		for(int i=0;i<entryId.size();i++){
			if(!taskId.contains(entryId.get(i))){
				/*add this event on the disk. to be implemented.*/
			}
		}
	}
	
	  private List<CalendarEventEntry> getEventsFromGCal(CalendarService service, URL feedUrl)
		      throws IOException, ServiceException {
		    // Send the request and receive the response:
		    CalendarEventFeed eventFeed = service.getFeed(feedUrl, CalendarEventFeed.class);
		    
		    List<CalendarEventEntry> entry = eventFeed.getEntries();
		    return entry;
	  }
	
	  /**
	   * Creates a new secondary calendar using the owncalendars feed.
	   * 
	   * @param service An authenticated CalendarService object.
	   * @return The newly created calendar entry.
	   * @throws IOException If there is a problem communicating with the server.
	   * @throws ServiceException If the service is unable to handle the request.
	   */
	  CalendarEntry createCalendar(CalendarService service, URL owncalendarsFeedUrl)
	      throws IOException, ServiceException {

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
	   * @param service An authenticated CalendarService object.
	   * @param eventTitle Title of the event to create.
	   * @param eventContent Text content of the event to create.
	   * @param recurData Recurrence value for the event, or null for
	   *        single-instance events.
	   * @param isQuickAdd True if eventContent should be interpreted as the text of
	   *        a quick add event.
	   * @param wc A WebContent object, or null if this is not a web content event.
	   * @return The newly-created CalendarEventEntry.
	   * @throws ServiceException If the service is unable to handle the request.
	   * @throws IOException Error communicating with the server.
	   */
	  CalendarEventEntry createEvent(CalendarService service,
	      String title, CustomDate startDate, CustomDate endDate, String recurData,
	      boolean isQuickAdd, URL feedUrl) throws ServiceException, IOException {
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
	   * @param service An authenticated CalendarService object.
	   * @param eventTitle Title of the event to create.
	   * @param eventContent Text content of the event to create.
	   * @return The newly-created CalendarEventEntry.
	   * @throws ServiceException If the service is unable to handle the request.
	   * @throws IOException Error communicating with the server.
	   */
	  CalendarEventEntry createSingleEvent(CalendarService service,
	      String eventContent, CustomDate startDate, CustomDate endDate, URL feedUrl) throws ServiceException,
	      IOException {
	    return createEvent(service, eventContent, startDate, endDate, null, false, feedUrl);
	  }

	  /**
	   * Creates a quick add event.
	   * 
	   * @param service An authenticated CalendarService object.
	   * @param quickAddContent The quick add text, including the event title, date
	   *        and time.
	   * @return The newly-created CalendarEventEntry.
	   * @throws ServiceException If the service is unable to handle the request.
	   * @throws IOException Error communicating with the server.
	   */
	  CalendarEventEntry createQuickAddEvent(
	      CalendarService service, String quickAddContent, URL feedUrl) throws ServiceException,
	      IOException {
	    return createEvent(service, quickAddContent, null, null, null, true, feedUrl);
	  }
	  
	  /**
	   * Creates a new recurring event.
	   * 
	   * @param service An authenticated CalendarService object.
	   * @param eventTitle Title of the event to create.
	   * @param eventContent Text content of the event to create.
	   * @return The newly-created CalendarEventEntry.
	   * @throws ServiceException If the service is unable to handle the request.
	   * @throws IOException Error communicating with the server.
	   */
	  private CalendarEventEntry createRecurringEvent(
	      CalendarService service, String eventContent, CustomDate startDate, CustomDate endDate, String freq, URL feedUrl)
	      throws ServiceException, IOException {
	   
	    String recurData = "DTSTART;VALUE=DATE:"+ startDate.returnInDateTimeFormat().getValue() +"\r\n"
	        + "DTEND;VALUE=DATE:"+ endDate.returnInDateTimeFormat().getValue() +"\r\n"
	        + "RRULE:FREQ=" + freq.toUpperCase() + ";UNTIL=20200101\r\n";

	    return createEvent(service, eventContent, null, null, recurData, false, feedUrl);
	  }
	  
	  /**
	   * Makes a batch request to delete all the events in the given list. If any of
	   * the operations fails, the errors returned from the server are displayed.
	   * The CalendarEntry objects in the list given as a parameters must be entries
	   * returned from the server that contain valid edit links (for optimistic
	   * concurrency to work). Note: You can add entries to a batch request for the
	   * other operation types (INSERT, QUERY, and UPDATE) in the same manner as
	   * shown below for DELETE operations.
	   * 
	   * @param service An authenticated CalendarService object.
	   * @param eventsToDelete A list of CalendarEventEntry objects to delete.
	   * @throws ServiceException If the service is unable to handle the request.
	   * @throws IOException Error communicating with the server.
	   */
	  private static void deleteEvents(CalendarService service,
	      List<CalendarEventEntry> eventsToDelete, URL feedUrl) throws ServiceException,
	      IOException {

	    // Add each item in eventsToDelete to the batch request.
	    CalendarEventFeed batchRequest = new CalendarEventFeed();
	    for (int i = 0; i < eventsToDelete.size(); i++) {
	      CalendarEventEntry toDelete = eventsToDelete.get(i);
	      // Modify the entry toDelete with batch ID and operation type.
	      BatchUtils.setBatchId(toDelete, String.valueOf(i));
	      BatchUtils.setBatchOperationType(toDelete, BatchOperationType.DELETE);
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
	        System.out.println("\n" + batchId + " failed (" + status.getReason()
	            + ") " + status.getContent());
	      }
	    }
	    if (isSuccess) {
	      System.out.println("Successfully deleted all events via batch request.");
	    }
	  }
}
