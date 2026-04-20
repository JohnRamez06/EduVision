package com.eduvision.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateUtils {
    private DateUtils() {}

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }

    public static ZonedDateTime toZoned(LocalDateTime dt, String zoneId) {
        if (dt == null) return null;
        ZoneId zid = (zoneId == null || zoneId.isBlank()) ? ZoneId.of("UTC") : ZoneId.of(zoneId);
        return dt.atZone(ZoneId.of("UTC")).withZoneSameInstant(zid);
    }
}