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

Start application:

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
- It's slow, so I recommend to set threads amount (default 4) and encode several parted zip files in the same time.

P.S.
Please, read terms and conditions of video hosting before uploading. Don't brake the rules with this soft ;)