import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Client{
    protected ObjectInputStream ois;
    protected ObjectOutputStream oos;

    /** Application name. */
    private static final String APPLICATION_NAME =
            "Google Calendar API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/calendar-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES =
            Arrays.asList(CalendarScopes.CALENDAR);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                Client.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp
                (flow, new LocalServerReceiver.Builder().setPort(8080).build()).
                authorize("user8");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static com.google.api.services.calendar.Calendar
    getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // Event builder
    public static Event addEvent(){
        Event event = new Event()
                .setSummary("Google I/O 2015")
                .setLocation("800 Howard St., San Francisco, CA 94103")
                .setDescription("A chance to hear more about Google's developer products.");

        // set the date and time of the event
        DateTime startDateTime = new DateTime("2015-05-28T09:00:00-07:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("America/Los_Angeles");
        event.setStart(start);

        DateTime endDateTime = new DateTime("2015-05-28T17:00:00-07:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("America/Los_Angeles");
        event.setEnd(end);

        // set recurrence of the event
        String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};
        event.setRecurrence(Arrays.asList(recurrence));

        // set the attendees of the event
        EventAttendee[] attendees = new EventAttendee[] {
                new EventAttendee().setEmail("lpage@example.com"),
                new EventAttendee().setEmail("sbrin@example.com"),
        };
        event.setAttendees(Arrays.asList(attendees));

        // set the reminder of the event
        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("email").setMinutes(24 * 60),
                new EventReminder().setMethod("popup").setMinutes(10),
        };
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);
        return event;
    }

    public static com.google.api.services.calendar.model.Calendar addCalendar()
    {
        com.google.api.services.calendar.model.Calendar calendar = new Calendar();
        calendar.setSummary("calendarSummary");
        calendar.setTimeZone("America/Los_Angeles");
        return calendar;
    }


    public void sendObject(Command command) {
        try {
            if(oos == null)
            {
                System.out.println("oos is null");
            }
            oos.writeObject(command);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void main(String[] args) throws IOException {
        // Build a new authorized API client service.
        // Note: Do not confuse this class with the
        //   com.google.api.services.calendar.model.Calendar class.

        // connect to serverThread and server
        // port

        com.google.api.services.calendar.Calendar service =
                getCalendarService();

        // Retrieve the user account
        com.google.api.services.calendar.model.Calendar alreadyExistedCalendar =
                service.calendars().get("primary").execute();

        System.out.println(alreadyExistedCalendar.getSummary());

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        List<Event> items = events.getItems();
        if (items.size() == 0) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }

        // Create a new calendar
        com.google.api.services.calendar.model.Calendar calendar = new Calendar();
        calendar.setSummary("calendarSummary");
        calendar.setTimeZone("America/Los_Angeles");

        Event groupEvent = addEvent();
        Command command = new Command(CommandType.GROUP_EVENT, groupEvent);
        sendObject(command);

        // Insert the new calendar
        Calendar createdCalendar = service.calendars().insert(calendar).execute();
        System.out.println(createdCalendar.getId());

        /*
        String calendarId = "primary";
        Event event = addEvent();
        service.events().insert(calendarId, event).execute();
        System.out.printf("Event created! ");
        */

        // Insert the new calendar
       // com.google.api.services.calendar.model.Calendar calendar = addCalendar();
       // com.google.api.services.calendar.Calendar createdCalendar = service.calendars().insert(calendar).execute();
        //System.out.println(createdCalendar.getId());

    }
}