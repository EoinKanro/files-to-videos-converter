<a href="https://www.buymeacoffee.com/EoinKanro" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-red.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>

# Preface

I found an interesting idea and wanted to write the same.

Please, check the original idea creator's page: <a href="https://github.com/DvorakDwarf/Infinite-Storage-Glitch">DvorakDwarf</a>

# Files to videos converter
The main idea of this project is to use video hosting as cloud.

So, you can encode files to videos and decode them back. To protect your files you can encode zip with password.

You can check a demo:
[<img src="https://img.youtube.com/vi/HEu7-zT8ANE/maxresdefault.jpg" width="100%">](https://youtu.be/watch?v=HEu7-zT8ANE)

### Testing
I've tested this project on file with size 97.6 mb and on ryzen 3600

**Encoding**
- Duplicate factor: 2 (a square of 4 pixels per bit)
- FPS: 30
- Result video size: 706 mb
- Time: ~315s

**Decoding**
- Time: ~182s

It seems that this realization is faster than
<a href="https://github.com/pixelomer/bin2video">bin2video</a>
and
<a href="https://github.com/DvorakDwarf/Infinite-Storage-Glitch">Infinite-Storage-Glitch</a>


# How to use

You can use prebuilt jar from releases or build project by yourself

### Build
Requirements for build:
- Java 17
- Maven

Build project:
```
mvn package -DskipTests
```

### Start
Requirements for start:
- Java 17
- FFmpeg

Start application:
- Download ffmpeg. For example from here: <a href="https://www.gyan.dev/ffmpeg/builds/ffmpeg-git-full.7z">FFmpeg</a>
- Put ffmpeg.exe into project-folder near the jar
```
cd <project-folder>
java -jar FilesToVideos.jar <arguments>
```

### Arguments
There are several custom arguments for command line, you can use -h to see them

**Example of converting files to videos:**
```
cd <project-folder>
java -jar -Xmx2048m -Xms2048m FilesToVideosConverter.jar -fti -itv -fp in -diip
```

It transforms files from project-folder/in to videos and delete temp images in process

**Example of converting videos to files:**
```
cd <project-folder>
java -jar -Xmx2048m -Xms2048m FilesToVideosConverter.jar -vti -itf -vp resultVideos202303222343 -diip
```

It transforms videos from project-folder/resultVideos202303222343 to files and delete temp images in process

### Be careful
- Result names of videos have a pattern, it's necessary to save the names to decode videos back to files
- It's slow, so I recommend to set threads amount (default 4) and encode several parted zip files in the same time.
- While progress, it can take x14 space. One part for images and one for videos.
If you use -diip flag converter will delete images after encoding / decoding

### Future releases
The main problem is speed. I've investigated that it's slow mostly because of IO operations like read bytes and write to files
and because of FFmpeg. I've optimized all the processes by 2 - 3 times but I have no idea what I can do now.

If you have any idea how I can improve it, please, let me know

P.S.
Please, read terms and conditions of video hosting before uploading. Don't brake the rules with this soft ;)