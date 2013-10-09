import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import com.google.gdata.data.calendar.ColorProperty;
import com.google.gdata.data.calendar.HiddenProperty;
import com.google.gdata.data.calendar.WebContent;
import com.google.gdata.data.extensions.Recurrence;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;


public class Sync {
	// The base URL for a user's calendar metafeed (needs a username appended).
	private static final String METAFEED_URL_BASE = "https://www.google.com/calendar/feeds/";
	
	// The string to add to the user's metafeedUrl to access the owncalendars feed.
	private static final String OWNCALENDARS_FEED_URL_SUFFIX = "/owncalendars/full";
	
	private static final String EVENT_FEED_URL_SUFFIX = "/private/full";
	
	private static final String CALENDAR_NAME = "iDo";
	private static final String CALENDAR_SUMMARY = "iDo";
	private static final String SERVICE_NAME = "cs2103aug2013-w13-03j-iDo";
	
	private static URL owncalendarsFeedUrl = null;
	private static URL eventFeedUrl = null;
	
	private String userName = "";
	private String password = "";
	
	private CalendarService service = null;
	
	protected Sync() {
	}
	
	private void setUserNameAndPassword(String userName, String password){
		this.userName = userName;
		this.password = password;
	}
	
	
	private void initService() throws MalformedURLException, AuthenticationException{
		setOwncalendarsFeedUrl(userName);
		setEventFeedUrl(userName);
		
		service = new CalendarService("SERVICE_NAME");
		
		service.setUserCredentials(userName, password);
	}
	
	private void setOwncalendarsFeedUrl(String username) throws MalformedURLException{
		owncalendarsFeedUrl = new URL(METAFEED_URL_BASE + username + OWNCALENDARS_FEED_URL_SUFFIX);
	}
	
	private void setEventFeedUrl(String username) throws MalformedURLException{
		eventFeedUrl = new URL(METAFEED_URL_BASE + username + EVENT_FEED_URL_SUFFIX);
	}
	
	protected static CalendarEntry createCalendar(CalendarService service, String title, String summary, ColorProperty color) throws IOException, ServiceException {
	    // Create the calendar
	    CalendarEntry calendar = new CalendarEntry();
	    calendar.setTitle(new PlainTextConstruct(title));
	    calendar.setSummary(new PlainTextConstruct(summary));
	    calendar.setHidden(HiddenProperty.FALSE);
	    calendar.setColor(color);
	    
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
	      String eventTitle, String eventContent, String recurData,
	      boolean isQuickAdd, WebContent wc) throws ServiceException, IOException {
	    CalendarEventEntry myEntry = new CalendarEventEntry();

	    myEntry.setTitle(new PlainTextConstruct(eventTitle));
	    myEntry.setContent(new PlainTextConstruct(eventContent));
	    myEntry.setQuickAdd(isQuickAdd);
	    myEntry.setWebContent(wc);

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
	    return service.insert(eventFeedUrl, myEntry);
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
	      String eventTitle, String eventContent) throws ServiceException,
	      IOException {
	    return createEvent(service, eventTitle, eventContent, null, false, null);
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
	      CalendarService service, String eventTitle, String eventContent)
	      throws ServiceException, IOException {
	    // Specify a recurring event that occurs every Tuesday from May 1,
	    // 2007 through September 4, 2007. Note that we are using iCal (RFC 2445)
	    // syntax; see http://www.ietf.org/rfc/rfc2445.txt for more information.
	    String recurData = "DTSTART;VALUE=DATE:20070501\r\n"
	        + "DTEND;VALUE=DATE:20070502\r\n"
	        + "RRULE:FREQ=WEEKLY;BYDAY=Tu;UNTIL=20070904\r\n";

	    return createEvent(service, eventTitle, eventContent, recurData, false,
	        null);
	  }
	  
	  /**
	   * Updates the title of an existing calendar event.
	   * 
	   * @param entry The event to update.
	   * @param newTitle The new title for this event.
	   * @return The updated CalendarEventEntry object.
	   * @throws ServiceException If the service is unable to handle the request.
	   * @throws IOException Error communicating with the server.
	   */
	  private static CalendarEventEntry updateTitle(CalendarEventEntry entry,
	      String newTitle) throws ServiceException, IOException {
	    entry.setTitle(new PlainTextConstruct(newTitle));
	    return entry.update();
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
	      List<CalendarEventEntry> eventsToDelete) throws ServiceException,
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
	    CalendarEventFeed feed = service.getFeed(eventFeedUrl,
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
