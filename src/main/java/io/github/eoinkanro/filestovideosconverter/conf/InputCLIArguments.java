package io.github.eoinkanro.filestovideosconverter.conf;

import io.github.eoinkanro.filestovideosconverter.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InputCLIArguments {

    public static final InputCLIArgument<Boolean> HELP = new InputCLIArgument<>("h", "help", false, "Show possible arguments", false);

    public static final InputCLIArgument<Boolean> FILES_TO_VIDEOS = new InputCLIArgument<>("ftv", "filesToImages", false, "Transform files to images", false);
    public static final InputCLIArgument<String> FILES_PATH = new InputCLIArgument<>("fp", "filesPath", true,
            "Path to file or folder that will be transformed to images. " +
                      "Or path to result folder for files after transformation from images. Default: resultFiles+time",
            "resultFiles" + TimeUtils.getCurrentTime());
    public static final InputCLIArgument<Integer> IMAGE_WIDTH = new InputCLIArgument<>("iw", "imageWidth", true, "Width of result image. Default: 1280", 1280);
    public static final InputCLIArgument<Integer> IMAGE_HEIGHT = new InputCLIArgument<>("ih", "imageHeight", true, "Height of result image. Default: 720", 720);
    public static final InputCLIArgument<Integer> FRAMERATE = new InputCLIArgument<>("fr", "framerate", true, "Framerate of generated video. Default: 30", 30);
    public static final InputCLIArgument<Integer> DUPLICATE_FACTOR = new InputCLIArgument<>("df", "duplicateFactor", true,
            "Duplicate factor of pixel per bit. Example: 2, then bit = square of 4 pixels total. Default: 2", 2);

    public static final InputCLIArgument<Boolean> VIDEOS_TO_FILES = new InputCLIArgument<>("vtf", "imagesToFiles", false, "Transform images to files", false);

    public static final InputCLIArgument<String> VIDEOS_PATH = new InputCLIArgument<>("vp", "videosPath", true,
            "Path to video or folder with videos that will be transformed to images. " +
                    "Or path to result folder for videos after transformation from images. Default: resultVideos+time",
            "resultVideos" + TimeUtils.getCurrentTime());

    public static final InputCLIArgument<Integer> THREADS = new InputCLIArgument<>("t", "threads", true, "Number of threads", 4);

}
