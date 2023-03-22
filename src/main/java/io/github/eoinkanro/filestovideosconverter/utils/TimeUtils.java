package io.github.eoinkanro.filestovideosconverter.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeUtils {

    private static final String TIME_FORMAT = "yyyyMMddHHmm";

    public static String  getCurrentTime() {
        return new SimpleDateFormat(TIME_FORMAT).format(new Date());
    }

}
