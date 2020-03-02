# webvtt-lssdh-ios8 to srt
Convert subtitle type of webvtt-lssdh-ios8 (on NetFlix ) to srt

If you are using this script: [Netflix - subtitle downloader](https://greasyfork.org/en/scripts/26654-netflix-subtitle-downloader) (or  save the subtitle content through other approaches);
and you want to convert the file with unstandard webvtt to standard srt;
then it can help you.

Depencies:
[jdom2](http://www.jdom.org/)

Usage:

```java
SubtitleConverter.convertVttToSrt(String vttFileName, String srtFileName);
// Read the source code if you want, easy to understand.
```