## Additional Features in this Fork
### v1.0.6-rhae.1
- New Scan menu entry to move all out-of-bounds nodes to centre
- New Align buttons to align selected nodes horizontally/vertically and distribute them evenly along that axis
- New Edit Buttons to flip selected nodes across horizontal oder vertical axis

## Java
A Java version **greater than 11** is required. ( JRE or JDK are both possible )<br>
Current Java versions can be downloaded [here](https://www.oracle.com/java/technologies/javase-downloads.html) and installation instructions can be found [here](https://www3.ntu.edu.sg/home/ehchua/programming/howto/JDK_Howto.html)

## Compatibility
The editor works with configs generated by the [FS19](https://github.com/Stephan-S/FS19_AutoDrive) + [FS22](https://github.com/Stephan-S/FS22_AutoDrive) versions of AutoDrive.

## Starting the AutoDrive Course Editor
The editor can be started multiple ways.<br><ol type="1">
<li>Using the supplied CMD file: "Open Editor with Console to see errors.cmd".</li>
<li>Directly from a command line: java -jar -Xms1024m -Xmx1024m .\AutoDriveEditor.Jar</li>
<li>Directly from a command line: java -jar .\AutoDriveEditor.jar (**Use this only for importing map DDS files that fail using normal run arguments**)</li></ol>

The editor will automatically create a log file autoDriveEditor.log, which can be used for later troubleshooting.<br>
The log file has a maximum size of 20MB and a maximum of 3 files are then created.

## Bug Reports

Please use the "Issues" section to report any bugs, i will try to respond as soon as humanly possible, but bear in mind
real life doesn't always allow for quick answers.

## Map Images

I maintain another GitHub repository of map images that the editor can automatically download when needed [here](https://github.com/KillBait/AutoDrive_MapImages)<br>
If you want to create images for yourself, you can find a guide [here](https://github.com/KillBait/AutoDrive_MapImages/discussions/20)

## Map Image Requests

Do you have any map images that are not on the repository?, please consider doing a pull request to add to
the collection, it will help other people.

Help with pull requests can be found on [GitHub Docs](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request)

**Not familiar with GitHub.. go over to the [Discussion](https://github.com/KillBait/AutoDrive_MapImages/discussions) section of the map image repository and create a request**


## License

The Editor does not currently have a license attached to it.

## Credits

**@Stephan_S** - The creator of the AutoDrive mod and original editor. With his permission i have taken over maintaining this editor.
