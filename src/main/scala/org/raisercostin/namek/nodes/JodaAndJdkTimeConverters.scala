package org.raisercostin.namek.nodes

import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone



import org.joda.time.{ DateTime => JDateTime }
import org.joda.time.DateTimeZone

import rapture.json._
import rapture.json.Json
import rapture.json.jsonBackends.spray._

object JodaAndJdkTimeConverters {
  System.setProperty("net.fortuna.ical4j.timezone.cache.impl", "net.fortuna.ical4j.util.MapTimeZoneCache")
  object implicits {
    implicit val zonedDateTimeSerializer = Json.serializer[String].contramap[ZonedDateTime] { zonedDateTime => TimeFormatConverters(zonedDateTime).asIso8601String }
    implicit val zonedDateTimeExtractor = Json.extractor[Json].map { j => TimeConverters(j.as[String]).asZonedDateTime }
  }

  implicit def toTimeConverters(moment: String) = TimeConverters(moment)
  implicit def toTimeFormatConverters(moment: ZonedDateTime) = TimeFormatConverters(moment)
  implicit def toTimeFormatConverters3(moment: java.util.Date) = TimeFormatConverters3(moment)

  private val format2 = new java.text.SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ssZ")
  private val formatterRFC3339 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
  private val formatterIso8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SZ")
  case class TimeConverters(moment: String) extends AnyRef {
    def asZonedDateTime: ZonedDateTime = new JDateTime(moment).toGregorianCalendar.toZonedDateTime
  }
  case class TimeFormatConverters(moment: ZonedDateTime) extends AnyRef {
    def asIso8601String = formatterIso8601.format(moment)
    def asRFC3339String = formatterRFC3339.format(moment)
    def asLocalIsoString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(moment)
    def asOffsetIsoString = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(moment)
    def asZonedIsoString = DateTimeFormatter.ISO_ZONED_DATE_TIME.format(moment)
    def asJodaDateTime: JDateTime = new JDateTime(moment.toInstant().toEpochMilli(),
      DateTimeZone.forTimeZone(java.util.TimeZone.getTimeZone(moment.getZone)))
  }
  case class TimeFormatConverters3(moment: java.util.Date) extends AnyRef {
    def asIsoString = format2.format(moment)
    def asJodaDateTime: JDateTime = new JDateTime(moment.toInstant().toEpochMilli(),
      DateTimeZone.forTimeZone(???))
  }
}