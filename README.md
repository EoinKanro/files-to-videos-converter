<a href="https://www.buymeacoffee.com/EoinKanro" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-red.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>

# Preface

I found an interesting idea and wanted to write the same.

Please, check original idea creator's page: <a href="https://github.com/DvorakDwarf/Infinite-Storage-Glitch">DvorakDwarf</a>

# Files to videos converter
The main idea of this project is to use video hosting as cloud.

So, you can encode files to videos and decode them back. To protect your files you can encode zip with password.

# How to use
### Build and start
Requirements for build and use:
- Java 17
- Maven
- FFMPEG

Build project:
```
mvn package -DskipTests
```

Start application:
- Download ffmpeg. For example from here: <a href="https://www.gyan.dev/ffmpeg/builds/ffmpeg-git-full.7z">FFMPEG</a>
- Put ffmpeg.exe in <project-folder>/target
```
cd <project-folder>/target
java -jar FilesToVideos.jar <arguments>
```

### Arguments
There are several arguments for command line, you can use -h to see them

**Example of converting files to videos:**
```
cd <project-folder>/target
java -jar -Xmx2048m -Xms2048m FilesToVideosConverter.jar -fti -itv -fp in -diip
```

It transforms files from <project-folder>/target/in to videos and delete temp images in process

**Example of converting videos to files:**
```
cd <project-folder>/target
java -jar -Xmx2048m -Xms2048m FilesToVideosConverter.jar -vti -itf -vp resultVideos202303222343 -diip
```

It transforms videos from <project-folder>/target/resultVideos202303222343 to files and delete temp images in process

### Be careful
- Result names of videos have a pattern, it's necessary to save the names to decode videos back to files
- It's slow, so I recommend to set threads amount (default 4) and encode several parted zip files in the same time.
- While progress, it can take x14 space. One part for images and one for videos.
If you use -diip flag converter will delete images after encoding / decoding

### Future releases
The main problem is speed. I've investigated that it's slow mostly because of IO operations like read bytes and write to files
or because of FFMPEG.

I've optimized all the processes by 2 - 3 times. So it takes ~5 minutes to encode ~90mb and ~3 minutes to decode ~600mb on my hardware.

If you have any idea how I can improve it, please, let me know

P.S.
Please, read terms and conditions of video hosting before uploading. Don't brake the rules with this soft ;)