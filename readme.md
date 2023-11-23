Hello virtual farmer!

I forked the original [AutoDrive Course Editor](https://github.com/KillBait/AutoDrive_Course_Editor) to address specific needs for my use of the original tool. Turns out it's quite fun to work with Java, so added more features and fixed some issues I encountered.

This fork is intended for my personal use, but I'm sharing it here for anyone who has similar needs, or wants to apply particular fixes (e.g. out of bounds nodes, or node elevation). While I'm careful and bug-averse, the testing is limited to my use cases and [FS22 AutoDrive](https://github.com/Stephan-S/FS22_AutoDrive). Treat this as beta software and be sure to create backups of the configurations files (if you're editing a savegame this would be `AutoDrive_config.xml` and `vehicles.xml`).

## Usage

### General
I assume that you're already familiar with this editor, otherwise you should first read its [readme.md](https://github.com/KillBait/AutoDrive_Course_Editor/blob/master/readme.md). This fork uses the original repository for map images. 


### Java
A Java version **greater than 11** is required. ( JRE or JDK are both possible )<br>
Current Java versions can be downloaded [here](https://www.oracle.com/java/technologies/javase-downloads.html) and installation instructions can be found [here](https://www3.ntu.edu.sg/home/ehchua/programming/howto/JDK_Howto.html)

### Starting the AutoDrive Course Editor
You can Download a zip archive with current release [here]().

The editor can be started multiple ways.

1. Using the supplied CMD file: `Open Editor with Console to see errors.cmd`.
2. Directly from a command line: `java -jar -Xms1024m -Xmx1024m .\AutoDriveEditor.jar`
3. Directly from a command line: `java -jar .\AutoDriveEditor.jar` (**Use this only for importing map DDS files that fail using normal run arguments**)

The editor will automatically create a log file autoDriveEditor.log, which can be used for later troubleshooting.
The log file has a maximum size of 20MB and a maximum of 3 files are then created.
