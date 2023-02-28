package io.github.eoinkanro.filestoimage.conf;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandLineArguments {

    public static final CommandLineArgument<Boolean> HELP = new CommandLineArgument<>("h", "help", false, "Show possible arguments", false);
    public static final CommandLineArgument<Boolean> FILES_TO_IMAGES = new CommandLineArgument<>("fti", "filesToImages", false, "Process files to images", false);
    public static final CommandLineArgument<String> FILES_PATH = new CommandLineArgument<>("fp", "filesPath", true, "Path to folder with files that will be transformed", "");
    public static final CommandLineArgument<Integer> IMAGE_WIDTH = new CommandLineArgument<>("iw", "imageWidth", true, "Width of result image", 1920);
    public static final CommandLineArgument<Integer> IMAGE_HEIGHT = new CommandLineArgument<>("ih", "imageHeight", true, "Height of result image", 1080);
    public static final CommandLineArgument<Boolean> IMAGES_TO_FILE = new CommandLineArgument<>("itf", "imagesToFiles", false, "Process images to files", false);
    public static final CommandLineArgument<String> IMAGES_PATH = new CommandLineArgument<>("ip", "imagesPath", true, "Path to folder with images that will be transformed", "");

}
