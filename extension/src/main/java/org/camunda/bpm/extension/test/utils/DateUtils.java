package org.camunda.bpm.extension.test.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtils {

  public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

  public static Date dateInFutureFrom(Date from, Duration duration) {
    return dateInFutureFrom(from, duration.toMinutes(), ChronoUnit.MINUTES);
  }

  public static Date dateInFutureFrom(Date from, long amountToAdd, ChronoUnit amount) {
    return dateInFutureFrom(toLocalDateTime(from), amountToAdd, amount);
  }

  public static LocalDateTime toLocalDateTime(Date dateToConvert) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(dateToConvert.getTime()), DEFAULT_ZONE_ID);
  }

  private static Date dateInFutureFrom(LocalDateTime from, long amountToAdd, ChronoUnit amount) {
    return toDate(from.plus(amountToAdd, amount));
  }

  // -- Converters Date/LocalDate/LocalDateTime

  public static Date toDate(LocalDateTime dateToConvert) {
    return Date.from(dateToConvert.atZone(DEFAULT_ZONE_ID).toInstant());
  }

}
