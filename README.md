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
- Duplicate factor: 4 (a square of 16 pixels per bit)
- FPS: 30
- Result video size: 1GB
- Time: ~79s

**Decoding**
- Time: ~51s

# How to use

Requirements for build:
- Java 17
- Maven

Build project:
```
mvn package -DskipTests
```

Start project:
```
cd <project-folder>
java -jar FilesToVideos.jar <arguments>
```

### Arguments
There are several custom arguments for command line, you can use -h to see them

**Example of converting files to videos:**
```
java -jar -Xmx2048m -Xms2048m FilesToVideosConverter.jar -ftv -fp in
```

It transforms files from project-folder/in to videos

**Example of converting videos to files:**
```
java -jar -Xmx2048m -Xms2048m FilesToVideosConverter.jar -vtf -vp resultVideos202303222343
```

It transforms videos from project-folder/resultVideos202303222343 to files

### Be careful
- Result names of videos have a pattern, it's necessary to save the names to decode videos back to files
- It's slow, so I recommend to set threads amount (default 4) and encode several parted zip files in the same time
- Result videos can be ~x11 size

P.S.
Please, read terms and conditions of video hosting before uploading. Don't brake the rules with this soft ;)