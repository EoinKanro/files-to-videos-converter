package io.github.eoinkanro.filestoimages.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeUtils {

    private static final String TIME_FORMAT = "yyyyMMddhhmm";

    public static String  getCurrentTime() {
        return new SimpleDateFormat(TIME_FORMAT).format(new Date());
    }

}
