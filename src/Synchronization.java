import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

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
import com.google.gdata.data.calendar.WebContent;
import com.google.gdata.data.extensions.Recurrence;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;



public class Synchronization {
	
	/*messages*/
	private static final String CALENDAR_TITLE = "iDo";
	private static final String CALENDAR_SUMMARY = "this calendar synchronizes with iDo task Manager.";
	private static final String SERVICE_NAME = "sg.nus.edu.cs2103aug2013-w13-03j";
	
	/*types of task status*/
	
	static enum Status {
		UNCHANGED, NEWLY_ADDED, DELETED
	}
	
	/*use username and password to login*/
	private String username = null;
	private String password = null;
	
	/*calendar id*/
	private String calendarId = null;
	
	/*calendar service*/
	CalendarService service;
	
	/*The base URL for a user's calendar metafeed (needs a username appended).*/
	private static final String METAFEED_URL_BASE = "https://www.google.com/calendar/feeds/";
	
	// The string to add to the user's metafeedUrl to access the owncalendars feed.
	private static final String OWNCALENDARS_FEED_URL_SUFFIX = "/owncalendars/full";
	
	private URL feedUrl = null;
	
	/*a list of event entry from Google calendar*/
	private List<CalendarEventEntry> eventEntry = new ArrayList<CalendarEventEntry>();
	
	/*a list of tasks which have been deleted locally since last synchronization*/
	private ArrayList<String> deletedTasksId = new ArrayList<String>();
	
	/*a list of tasks which have been added locally since last synchronization*/
	private ArrayList<Task> newlyAddedTasks = new ArrayList<Task>();
	
	private ArrayList<Task> remainingTasks = new ArrayList<Task>();
	
	public Synchronization(){
		
	}
	
	public void execute() throws IOException, ServiceException{
		//initialize
		init();
		
		//form feed url
		feedUrl = formFeedUrl(service);
		
		//get all events from Google calendar
		eventEntry = getEventsFromGCal(service, feedUrl);
		
		//sync newly added tasks to GCal
		syncNewTasksToGCal(service, newlyAddedTasks, feedUrl);
		
		//delete events on GCal which have been deleted locally
		snycDeletedTasksToGCal(service, deletedTasksId, eventEntry, feedUrl);
		
		//delete tasks locally which have been deleted on GCal
		deleteTasksLocally(remainingTasks, eventEntry);
		
		//add tasks locally which have been added on GCal
		addEventsLocally(remainingTasks, eventEntry);
	}
	
	private void init() throws AuthenticationException{
		//create a new service
		service = new CalendarService(SERVICE_NAME);
		
		//authenticate using ClientLogin
		service.setUserCredentials(username, password);
	}
	
	private URL formFeedUrl(CalendarService service) throws IOException, ServiceException{
		URL url;
		if(calendarId == null){
			url = new URL(METAFEED_URL_BASE + username + OWNCALENDARS_FEED_URL_SUFFIX);
			CalendarEntry calendar = createCalendar(service, url);
			calendarId = calendar.getId();
		} else {
			url = new URL(METAFEED_URL_BASE + calendarId + OWNCALENDARS_FEED_URL_SUFFIX);	
		}
		return url;
	}
	
	private void syncNewTasksToGCal(CalendarService service, ArrayList<Task> tasks, URL feedUrl){
		for(int i; i<tasks.size(); i++){
			Task task = tasks.get(i);
			if(task.isRecurringTask){
				createRecurringEvent(service, task.getWorkInfo(), task.getStartDateString(), task.getEndDateString(), task.getFreq(), feedUrl);
			} else {
				createSingleEvent(service, task.getWorkInfo(), task.getStartDateString(), task.getEndDateString(), feedUrl);
			}
		}
	}
	
	private void snycDeletedTasksToGCal(CalendarService service, ArrayList<String> tasksId, List<CalendarEventEntry> entries, URL feedUrl) throws ServiceException, IOException{
		List<CalendarEventEntry> tobeDelete = new ArrayList<CalendarEventEntry>();
		for(int i=0; i<tasksId.size(); i++){
			for(int j=0; j<entries.size();j++){
				if(tasksId.get(i).equals(entries.get(j).getId())){
					tobeDelete.add(entries.get(j));
					break;
				}
			}
		}
		deleteEvents(service, tobeDelete, feedUrl);
	}
	
	private void deleteTasksLocally(ArrayList<Task> remainingTasks, List<CalendarEventEntry> entries){
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
	
	private void addEventsLocally(ArrayList<Task> remainingTasks, List<CalendarEventEntry> entries){
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
	  private CalendarEntry createCalendar(CalendarService service, URL owncalendarsFeedUrl)
	      throws IOException, ServiceException {

	    // Create the calendar
	    CalendarEntry calendar = new CalendarEntry();
	    calendar.setTitle(new PlainTextConstruct(CALENDAR_TITLE));
	    calendar.setSummary(new PlainTextConstruct(CALENDAR_SUMMARY));
	    calendar.setHidden(HiddenProperty.FALSE);

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
	  private static CalendarEventEntry createEvent(CalendarService service,
	      String eventContent, CustomDate startDate, CustomDate endDate, String recurData,
	      boolean isQuickAdd, URL feedUrl) throws ServiceException, IOException {
	    CalendarEventEntry myEntry = new CalendarEventEntry();

	    myEntry.setContent(new PlainTextConstruct(eventContent));
	    myEntry.setQuickAdd(isQuickAdd);

	    // If a recurrence was requested, add it. Otherwise, set the
	    // time (the current date and time) and duration (30 minutes)
	    // of the event.
	    if (recurData == null) {
	      Calendar calendar = new GregorianCalendar();
	      DateTime startTime = new DateTime(calendar.getTime(), TimeZone
	          .getDefault());

	      calendar.add(Calendar.MINUTE, 30);
	      DateTime endTime = new DateTime(calendar.getTime(), 
	          TimeZone.getDefault());

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
	  private static CalendarEventEntry createSingleEvent(CalendarService service,
	      String eventTitle, String eventContent, URL feedUrl) throws ServiceException,
	      IOException {
	    return createEvent(service, eventContent, null, false, feedUrl);
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
	  private static CalendarEventEntry createQuickAddEvent(
	      CalendarService service, String quickAddContent, URL feedUrl) throws ServiceException,
	      IOException {
	    return createEvent(service, null, quickAddContent, null, true, feedUrl);
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
	  private static CalendarEventEntry createRecurringEvent(
	      CalendarService service, String eventContent, String startDate, String endDate, String freq, URL feedUrl)
	      throws ServiceException, IOException {
	   
	    String recurData = "DTSTART;VALUE=DATE:"+ startDate +"\r\n"
	        + "DTEND;VALUE=DATE:"+ endDate +"\r\n"
	        + "RRULE:FREQ=" + freq + ";BYDAY=Tu;UNTIL=20200101\r\n";

	    return createEvent(service, eventContent, recurData, false, feedUrl);
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
