package io.github.eoinkanro.filestoimages.conf;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutputCLIArguments {
    FFMPEG("ffmpeg.exe"),
    FRAMERATE("-framerate"),
    PATTERN_TYPE("-pattern_type"),
    SEQUENCE("sequence"),
    INPUT("-i"),
    CODEC_VIDEO("-c:v"),
    FFV1("ffv1"),
    LIBX264("libx264"),
    START_NUMBER("-start_number"),
    DEFAULT_YES("-y"),
    HIDE_BANNER("-hide_banner"),
    BRACKETS_PATTERN("\"%s\"");

    private final String value;

    public String formatValue(String value) {
        return String.format(this.value, value);
    }
}
