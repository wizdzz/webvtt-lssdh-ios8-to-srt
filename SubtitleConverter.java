package com.wizd;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SubtitleConverter {
    private static class Phase{
        public Integer index;
        public Integer phaseSourceLineCount;
        public String timeCodes;
        public List<String> textContents;
    }

    public static List<Phase> parserVtt(String vttFile){
        List<Phase> phaseList = new ArrayList<>();

        try {
            int lineIndex = 0;

            List<String> lines = Files.readAllLines(Paths.get(vttFile), StandardCharsets.UTF_8);

            for(int i = 0; i < lines.size(); i++){
                String line = lines.get(i).trim();
                if(line.equals("")){
                    continue;
                }

                // Get the first line
                if(line.equals("1")){
                    lineIndex = i;
                    break;
                }
            }

            // Ok, begin parse now
            while(true) {
                List<String> phaseLines = readPhase(lineIndex, lines);
                if(phaseLines.size() == 0){
                    break;
                }
                Phase phase = parsePhase(phaseLines);
                phaseList.add(phase);
                lineIndex += (phase.phaseSourceLineCount + 1);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return phaseList;
    }

    private static void writePhaseListToSrt(List<Phase> phaseList, String fileName){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            for (Phase phase : phaseList) {
                fileOutputStream.write(phase.index.toString().getBytes());
                fileOutputStream.write("\r\n".getBytes());
                fileOutputStream.write(phase.timeCodes.getBytes(StandardCharsets.UTF_8));
                fileOutputStream.write("\r\n".getBytes());

                for (String textContent : phase.textContents) {
                    fileOutputStream.write(textContent.getBytes(StandardCharsets.UTF_8));
                    fileOutputStream.write("\r\n".getBytes());
                }

                fileOutputStream.write("\r\n".getBytes());
            }

            fileOutputStream.close();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void convertVttToSrt(String vttFileName, String srtFileName){
        List<SubtitleConverter.Phase> phaseList = parserVtt(vttFileName);

        writePhaseListToSrt(phaseList, srtFileName);
    }

    private static List<String> readPhase(int startLineIndex, List<String> lines) {
        List<String> phaseLines = new ArrayList<>();

        while(startLineIndex < lines.size()) {
            String dirtyText = lines.get(startLineIndex++).trim();
            if(dirtyText.equals("")){  // Read until encounter empty line
                break;
            }
            else {
                phaseLines.add(dirtyText);
            }
        }

        return phaseLines;
    }

    public static Phase parsePhase(List<String> lines){
        Phase phase = new Phase();
        List<String> textContents = new ArrayList<>();

        phase.phaseSourceLineCount = lines.size();
        phase.index = Integer.parseInt(lines.get(0));
        phase.timeCodes = getTimeCodes(lines.get(1));

        for(String line: lines.subList(2, lines.size())){
            textContents.add(appendTextContent(getXmlStrTextContent(line)));
        }

        phase.textContents = textContents;

        return phase;
    }

    public static String appendTextContent(List<String> source){
        StringBuilder builder = new StringBuilder();

        for(String ele: source){
            builder.append(ele);
        }

        return builder.toString();
    }

    public static List<String> getXmlStrTextContent(String xmlStr){
        List<String> textContent = new ArrayList<>();
        try {
            xmlStr = "<wizdzz>" + xmlStr + "</wizdzz>";

            SAXBuilder saxBuilder = new SAXBuilder();

            InputStream stream = new ByteArrayInputStream(xmlStr.getBytes(StandardCharsets.UTF_8));
            Document document = saxBuilder.build(stream);

            for(Object element: document.getRootElement().getContent()){
                textContent.add(getElementTextValue((Element) element));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return textContent;
    }

    private static String getElementTextValue(Element element) {
        if(element.getContent().size() != 1){
            throw new RuntimeException("Element[" + element.toString() + "] has more than one content!");
        }

        Object content = element.getContent().get(0);
        if (content instanceof org.jdom2.Text) {
            return ((Text) content).getText();
        }
        else{
            return getElementTextValue((Element) content);
        }
    }

    public static String getTimeCodes(String timeCodeStr){
        if(!timeCodeStr.contains("->")){
            return "";
        }

        int arrowIndex = timeCodeStr.indexOf("->");

        int endTimeBeginIndex = arrowIndex + 1;
        while (timeCodeStr.charAt(endTimeBeginIndex) != ' '){
            endTimeBeginIndex++;
        }

        int endTimeEndIndex = endTimeBeginIndex + 13;

        return timeCodeStr.substring(0, endTimeEndIndex);
    }
}
