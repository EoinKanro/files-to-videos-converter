package io.github.eoinkanro.filestoimage.conf;

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
    LIBX264("libx264"),
    START_NUMBER("-start_number"),
    DEFAULT_YES("-y"),
    BRACKETS_PATTERN("\"%s\"");

    private final String value;

    public String formatValue(String value) {
        return String.format(this.value, value);
    }
}
