package io.github.eoinkanro.filestoimage.conf;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InputCLIArguments {

    public static final InputCLIArgument<Boolean> HELP = new InputCLIArgument<>("h", "help", false, "Show possible arguments", false);
    public static final InputCLIArgument<Boolean> FILES_TO_IMAGES = new InputCLIArgument<>("fti", "filesToImages", false, "Process files to images", false);
    public static final InputCLIArgument<String> FILES_PATH = new InputCLIArgument<>("fp", "filesPath", true, "Path to folder with files that will be transformed", "");
    public static final InputCLIArgument<Integer> IMAGE_WIDTH = new InputCLIArgument<>("iw", "imageWidth", true, "Width of result image", 1920);
    public static final InputCLIArgument<Integer> IMAGE_HEIGHT = new InputCLIArgument<>("ih", "imageHeight", true, "Height of result image", 1080);
    public static final InputCLIArgument<Boolean> IMAGES_TO_FILE = new InputCLIArgument<>("itf", "imagesToFiles", false, "Process images to files", false);
    public static final InputCLIArgument<String> IMAGES_PATH = new InputCLIArgument<>("ip", "imagesPath", true, "Path to folder with images that will be transformed", "");
    public static final InputCLIArgument<Boolean> IMAGES_TO_VIDEOS = new InputCLIArgument<>("itv", "imagesToVideos", false, "Process images to videos", false);
    public static final InputCLIArgument<String> FRAMERATE = new InputCLIArgument<>("fr", "framerate", true, "Framerate of generated video", "60");

}
