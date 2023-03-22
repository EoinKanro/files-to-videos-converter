<a href="https://www.buymeacoffee.com/EoinKanro" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-red.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>

# Preface

I found an interesting idea and wanted to write the same.

Please, check original idea creator's page: <a href="https://github.com/DvorakDwarf/Infinite-Storage-Glitch">DvorakDwarf</a>

# Files to videos converter
The main idea of this project is to use video hosting as cloud.

So, you can encode files to videos and decode them back.

# How to use
### Build and start
Requirements for build:
- Java 17

Build project:
```
mvn package -DskipTests
```

Start application:
```
cd <project-folder>/target
java -jar FilesToVideos.jar <arguments>
```

### Arguments
There are several arguments for command line, you can use -h to see them

Example of converting files to videos:
```
cd <project-folder>/target
java -jar FilesToVideos.jar -fti -itv -fp in -diip
```

It transforms files from <project-folder>/target/in to videos and delete temp images in process

Example of converting videos to files:
```
cd <project-folder>/target
java -jar FilesToVideos.jar -itf -vti -vp resultVideos202303222343 -diip
```

It transforms videos from <project-folder>/target/resultVideos202303222343 to files and delete temp images in process
