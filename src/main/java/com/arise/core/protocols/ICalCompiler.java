package com.arise.core.protocols;


import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static com.arise.core.tools.StringUtil.hasText;


public class ICalCompiler {



  private String version;

  private String calscale;

  List<ICalEvent> events = new ArrayList<>();

  private Flavour flavour = Flavour.BASIC;

  private String separator = "\r\n";


//  public static String buildLine(String separator, String ... args){
//    StringBuilder sb = new StringBuilder();
//    for (String s: args){
//      sb.append(s);
//    }
//    return fixLine(sb.toString(), separator) + separator;
//  }
//
//  private static String fixLine(String line, String separator){
//    if (line.length() > 75){
//      return line.substring(0, 75) + separator + fixLine(line.substring(75, line.length()), separator);
//    }
//    return line;
//  }

  public ICalCompiler(){

  }


  @Override
  public String toString() {

    LineWriter sb = new LineWriter(separator);
    sb.writeLine("BEGIN:VCALENDAR")
        .writeLine("PRODID:-//Ready Up//iCal4j 1.0//EN")
        .writeLine("VERSION:2.0")
        .writeLine("CALSCALE:GREGORIAN")
        .writeLine("NAME:Ready Up")
        .writeLine("X-WR-CALNAME:Ready Up")
        .writeLine("DESCRIPTION:A description of my calendar")
        .writeLine("X-WR-CALDESC:A description of my calendar")
        .writeLine("COLOR:34:50:105")
//        .writeLine("METHOD:REQUEST");
        .writeLine("METHOD:PUBLISH");


    for (ICalEvent event: events){
      sb.append(event.withFlavour(flavour).withSeparator(separator).writer());
    }
    sb.writeLine("END:VCALENDAR");

//    StringBuilder builder = new StringBuilder();
//    builder.append("BEGIN:VCALENDAR").append(separator);
//    builder.append("PRODID:-//Ready Up//iCal4j 1.0//EN").append(separator);
//    builder.append("VERSION:2.0").append(separator);
//    builder.append("CALSCALE:GREGORIAN").append(separator);
//    builder.append("METHOD:PUBLISH").append(separator);
//    builder.append("X-WR-TIMEZONE:Europe/Bucharest").append(separator);
//merge doar pe google:
//    builder.append("BEGIN:VTIMEZONE\n"
//        + "TZID:Europe/Bucharest\n"
//        + "X-LIC-LOCATION:Europe/Bucharest\n"
//        + "BEGIN:DAYLIGHT\n"
//        + "TZOFFSETFROM:+0200\n"
//        + "TZOFFSETTO:+0300\n"
//        + "TZNAME:EEST\n"
//        + "DTSTART:19700329T030000\n"
//        + "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n"
//        + "END:DAYLIGHT\n"
//        + "BEGIN:STANDARD\n"
//        + "TZOFFSETFROM:+0300\n"
//        + "TZOFFSETTO:+0200\n"
//        + "TZNAME:EET\n"
//        + "DTSTART:19701025T040000\n"
//        + "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n"
//        + "END:STANDARD\n"
//        + "END:VTIMEZONE\n");

//    for (ICalEvent event: events){
//      builder.append(event.withFlavour(flavour).withSeparator(separator).toString());
//    }
//
//    builder.append("END:VCALENDAR");
    return sb.toString();
  }



  public void add(ICalEvent iCalEvent) {
    events.add(iCalEvent);
  }


  static class LineWriter{

    private final String separator;

    List<String> lines = new ArrayList<>();

    public LineWriter(String separator){
      this.separator = separator;
    }


    public static String concat(String ... args){
      StringBuilder sb = new StringBuilder();
      for (String s: args){
        sb.append(s);
      }
      return sb.toString();
    }


    public LineWriter append(LineWriter parent){
      for (String line: parent.lines){
        lines.add(line);
      }
      return this;
    }

    public LineWriter writeLine(String ... args){
      lines.add(concat(args));
      return this;
    }


    /**
     * https://tools.ietf.org/html/rfc2445 section 4.1
     * @param line
     * @return
     */
    private String fixLine(String line){
      if (line.length() > 75){
        return line.substring(0, 75) + separator + " " + fixLine(line.substring(75, line.length()));
      }
      return line;
    }

    @Override
    public String toString() {
      int lsize = lines.size();

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < lsize; i++){
        String line = lines.get(i);
        sb.append(fixLine(line));
        if (i < lsize -1 ){
          sb.append(separator);
        }
      }
      return sb.toString();
    }
  }


  public static class ICalEvent {


    private ICalDate start;
    private ICalDate stamp;
    private ICalDate end;
    private String lastModified;
    private String uid;
    private String summary;
    private boolean allDay;
    private Flavour flavour;

    private Transparency transparency = Transparency.OPAQUE;
    private String created;

    private String location;
    private String description;
    private String recurrenceRule;

    private String separator;
    private ICalOrganizer organizer;


    private List<ICalAtendee> atendees = new ArrayList<>();

    public String getSeparator() {
      return separator;
    }

    ICalEvent withSeparator(String separator) {
      this.separator = separator;
      return this;
    }

    private List<ICalTimezone> timezones = new ArrayList<>();

    ICalEvent withFlavour(Flavour flavour){
      this.flavour = flavour;
      return this;
    }


    public ICalEvent setReccurenceRule(String recurrenceRule) {
      this.recurrenceRule = recurrenceRule;
      return this;
    }

    public ICalEvent setDescription(String description) {
      this.description = description;
      return this;
    }

    public ICalEvent setLocation(String location) {
      this.location = location;
      return this;
    }

    public ICalEvent setCreated(String created) {
      this.created = created;
      return this;
    }

    public ICalEvent setSummary(String summary) {
      this.summary = summary;
      return this;
    }

    public ICalEvent setTransparency(Transparency transparency) {
      this.transparency = transparency;
      return this;
    }


    public ICalEvent setStart(ICalDate start) {
      this.start = start;
      if (start.getTimezone() != null) timezones.add(start.getTimezone());
      return this;
    }


    public ICalEvent setStamp(ICalDate stamp) {
      this.stamp = stamp;
      if (stamp.getTimezone() != null) timezones.add(stamp.getTimezone());
      return this;
    }

    public ICalEvent setEnd(ICalDate end) {
      this.end = end;
      if (end.getTimezone() != null) timezones.add(end.getTimezone());
      return this;
    }

    public ICalEvent setLastModified(String lastModified) {
      this.lastModified = lastModified;
      return this;
    }

    public ICalEvent setUid(String uid) {
      this.uid = uid;
      return this;
    }

    public ICalEvent setAllDay(boolean allDay) {
      this.allDay = allDay;
      return this;
    }

    public boolean isAllDay() {
      return allDay;
    }

    public ICalEvent setOrganizer(ICalOrganizer organizer) {
      this.organizer = organizer;
      return this;
    }

    public ICalEvent setAtendees(List<ICalAtendee> atendees) {
      this.atendees = atendees;
      return this;
    }

    @Override
    public String toString() {
      return writer().toString();
    }


    public LineWriter writer() {
      LineWriter wr = new LineWriter(separator);
      wr.writeLine("BEGIN:VEVENT")
      .writeLine("UID:", uid);

      if (hasText(summary)) wr.writeLine("SUMMARY:", summary);

      if(!isAllDay()){
        if(start != null) wr.writeLine(start.as("DTSTART").toString());
        if(end != null) wr.writeLine(end.as("DTEND").toString());
      } else {
        if(start != null)  wr.writeLine(start.as("DTSTART").toDateString());
        if(end != null) wr.writeLine(end.as("DTEND").toDateString());
      }
      if (stamp != null) wr.writeLine(start.as("DTSTAMP").toString());

      if (hasText(lastModified)) wr.writeLine("LAST-MODIFIED:", lastModified);
      if (hasText(location)) wr.writeLine("LOCATION:", location);
      if (hasText(created)) wr.writeLine("CREATED:", created);
      if (hasText(description)) wr.writeLine("DESCRIPTION:", description);
      if (hasText(recurrenceRule)) wr.writeLine("RRULE:", recurrenceRule);

      if (organizer != null) wr.writeLine(organizer.toString());

      for (ICalAtendee atendee: atendees){
        wr.writeLine(atendee.toString());
      }

      if (Flavour.GOOGLE.equals(flavour)){
        wr.writeLine("TRANSP:", transparency.name());
      }

      wr.writeLine("END:VEVENT");
      return wr;
    }


  }


  public static class ICalAtendee {

    private String role;
    private String type;
    private String name;
    private String email;
    private String partStat;
    private Boolean rsvp;

    public ICalAtendee setEmail(String email) {
      this.email = email;
      return this;
    }

    public ICalAtendee setName(String name) {
      this.name = name;
      return this;
    }

    public ICalAtendee setRole(Roles role) {
      this.role = role.name();
      return this;
    }

    public ICalAtendee setType(AtendeeType type) {
      this.type = type.name();
      return this;
    }

    public ICalAtendee setPartStat(PartStat partStat){
      this.partStat = partStat.id;
      return this;
    }

    public boolean isMailOnly(){
      return rsvp == null;
    }

    @Override
    public String toString() {
      if (isMailOnly()){
        return "ATTENDEE:MAILTO" + email;
      }

      //order is important!!!
      StringBuilder sb = new StringBuilder();
      sb.append("ATTENDEE;RSVP=").append(rsvp ? "TRUE" : "FALSE");
      if (hasText(type)) sb.append(";CUTYPE=").append(type);
      if (hasText(role)) sb.append(";ROLE=").append(role);
      if (hasText(partStat)) sb.append(";PARTSTAT=").append(partStat);
      sb.append(";CN=").append(hasText(name) ? name : email);
      sb.append(";X-NUM-GUESTS=0");
      sb.append(":mailto:").append(email);

      return sb.toString();
    }

    public ICalAtendee setRsvp(boolean b) {
      this.rsvp = b;
      return this;
    }
  }


  public static class ICalOrganizer{
    private String name;
    private String email;



    public String getName() {
      return name;
    }

    public ICalOrganizer setName(String name) {
      this.name = name;
      return this;
    }

    public String getEmail() {
      return email;
    }

    public ICalOrganizer setEmail(String email) {
      this.email = email;
      return this;
    }


    @Override
    public String toString() {
      if (!hasText(name)){
        return "ORGANIZER:mailto:" + email;
      }
      return "ORGANIZER;CN="+name+":MAILTO:" + email;
    }


  }


  public static class ICalTimezone {

    private final TimeZone tz;
    private String separator;

    /**
     * builder.append("BEGIN:VTIMEZONE\n"
     *         + "TZID:Europe/Bucharest\n"
     *         + "X-LIC-LOCATION:Europe/Bucharest\n"
     *         + "BEGIN:DAYLIGHT\n"
     *         + "TZOFFSETFROM:+0200\n"
     *         + "TZOFFSETTO:+0300\n"
     *         + "TZNAME:EEST\n"
     *         + "DTSTART:19700329T030000\n"
     *         + "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n"
     *         + "END:DAYLIGHT\n"
     *         + "BEGIN:STANDARD\n"
     *         + "TZOFFSETFROM:+0300\n"
     *         + "TZOFFSETTO:+0200\n"
     *         + "TZNAME:EET\n"
     *         + "DTSTART:19701025T040000\n"
     *         + "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n"
     *         + "END:STANDARD\n"
     *         + "END:VTIMEZONE\n");
     * @param timeZone
     */
    public ICalTimezone(TimeZone timeZone) {
      this.tz = timeZone;
    }

    @Override
    public String toString() {
      //getID -> Europe/Bucharest
      //getDispplayName -> Eastern European Time

      //tz.getRawOffset() / 3600000 -> STANDARD TZOFFSETTO

      ZoneId italianZoneId = ZoneId.of("Europe/Rome");
//      italianZoneId.getRules().get
      StringBuilder sb = new StringBuilder();
      sb.append("BEGIN:VTIMEZONE\n")
          .append("TZID:").append(tz.getID()).append("\n")
          .append("END:VTIMEZONE\n");
      return sb.toString();
    }

    ICalTimezone withSeparator(String separator) {
      this.separator = separator;
      return this;
    }
  }

  public static class ICalDate {

    private long unixTimestamp;
    private String prefix;
    private ICalTimezone calTz;

    public ICalDate(long unixTimestamp, String timezoneString) {
      this.unixTimestamp = unixTimestamp;
      setTimezone(timezoneString);
    }

    public ICalDate setTimezone(String timezoneString){
      TimeZone tz = null;
      try {
        tz = TimeZone.getTimeZone(timezoneString);
      } catch (Exception ex){
        tz = null;
      } finally {
        if (tz != null){
          this.calTz = new ICalTimezone(tz);
        }
      }
      return this;
    }

    public ICalDate setTimezone(TimeZone timezone){
      this.calTz = new ICalTimezone(timezone);
      return this;
    }

    @Override
    public String toString() {
      DateTimeFormatter formatter = DateTimeFormatter .ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneId.of("GMT"));
     return LineWriter.concat(prefix, ":", formatter.format(Instant.ofEpochSecond(unixTimestamp)) );
    }

    public ICalTimezone getTimezone(){
      return calTz;
    }

//    public static String toDateString(long unixTimestamp, String timezone){
//      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
////    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//      sdf.setTimeZone(TimeZone.getTimeZone(timezone));
//
//      System.out.println("TIMEZONE: " + timezone);
//      return sdf.format(new Date(unixTimestamp * 1000));
//      //+ ";TZID=" + timezone;
//    }


    public ICalDate as(String prefix) {
      this.prefix = prefix;
      return this;
    }

    public String toDateString() {
      return LineWriter.concat(prefix, ";VALUE=DATE:", DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("GMT")).format(Instant.ofEpochSecond(unixTimestamp)));
    }




  }

  private enum  Flavour {
    BASIC, GOOGLE;
  }

  public enum Roles {
    PARTICIPANT("REQ-PARTICIPANT");

    private final String id;

    Roles(String id){
      this.id = id;
    }


  }

  public enum PartStat {
    TENTATIVE("TENTATIVE"), ACCEPTED("ACCEPTED"), NEEDS_ACTION("NEEDS-ACTION");
    private final String id;

    PartStat(String id){
      this.id = id;
    }
  }

  public enum AtendeeType {
    INDIVIDUAL;
  }

  public enum Transparency {
    OPAQUE, TRANSPARENT;
  }
}
