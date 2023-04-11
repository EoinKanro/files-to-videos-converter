package io.github.eoinkanro.filestovideosconverter.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CommonUtils {

    public int parseInt(String anIntString) {
        if (!StringUtils.isBlank(anIntString)) {
            return Integer.parseInt(anIntString);
        }
        return 0;
    }

}
