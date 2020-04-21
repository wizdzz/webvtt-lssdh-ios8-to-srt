package com.wizd;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubtitleConverter {
    public static class Phase{
        public Integer index;
        public Integer phaseSourceLineCount;
        public String timeCodes;
        public List<String> textContents;
    }

    public static List<String> convertVttToSrt(String vttFileName) throws IOException {
        int startLineIndex = 0;
        List<String> lines = Files.readAllLines(Paths.get(vttFileName), StandardCharsets.UTF_8);

        for(int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.equals("")) {
                continue;
            }

            // Get the first line
            if (line.equals("1")) {
                startLineIndex = i;
                break;
            }
        }
        if (startLineIndex > 0) {
            lines.subList(0, startLineIndex).clear();
        }

        Pattern p = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}.\\d{3} --> \\d{2}:\\d{2}:\\d{2}.\\d{3})[\\W\\w^\\n]*");
        Pattern p1 = Pattern.compile("<.*?>");
        for(int i = 0; i < lines.size(); i++) {
            Matcher m = p.matcher(lines.get(i));
            Matcher m1 = p1.matcher(lines.get(i));
            if (m.find()) {
                lines.set(i, m.group(1).replace(".", ","));
            }
            else if(m1.find()){
                lines.set(i, lines.get(i).replaceAll("<.*?>", "").replace("&lrm;", ""));
            }
        }

        return lines;
    }

    public static int convertVttToSrt(String vttFileName, String srtFileName) throws IOException {
        List<String> lines = convertVttToSrt(vttFileName);
        String content = String.join("\r\n", lines);
        Files.write(Paths.get(srtFileName), content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return lines.size();
    }

}
