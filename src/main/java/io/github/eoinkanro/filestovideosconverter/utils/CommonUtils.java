package io.github.eoinkanro.filestovideosconverter.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CommonUtils {

    private String osName;

    public int parseInt(String anIntString) {
        if (!StringUtils.isBlank(anIntString)) {
            return Integer.parseInt(anIntString);
        }
        return 0;
    }

    public boolean isWindows() {
        return getOsName().startsWith("windows");
    }

    public String getOsName() {
        if (StringUtils.isEmpty(osName)) {
            osName = System.getProperty("os.name").toLowerCase();
        }
        return osName;
    }

}
