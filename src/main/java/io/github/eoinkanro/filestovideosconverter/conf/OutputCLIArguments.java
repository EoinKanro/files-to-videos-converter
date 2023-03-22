package io.github.eoinkanro.filestovideosconverter.conf;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutputCLIArguments {
    FFMPEG("ffmpeg.exe"),
    DEFAULT_YES("-y"),
    FRAMERATE("-framerate"),
    PATTERN_TYPE("-pattern_type"),
    SEQUENCE("sequence"),
    START_NUMBER("-start_number"),
    INPUT("-i"),
    CODEC_VIDEO("-c:v"),
    LIBX264("libx264"),
    MOV_FLAGS("-movflags"),
    FAST_START("+faststart"),
    CRF("-crf"),
    CRF_18("18"),
    PIXEL_FORMAT("-pix_fmt"),
    GRAY("gray"),
    PRESET("-preset"),
    SLOW("slow"),
    HIDE_BANNER("-hide_banner"),
    BRACKETS_PATTERN("\"%s\"");

    private final String value;

    public String formatValue(String value) {
        return String.format(this.value, value);
    }
}
