package com.wcagapp.wcagathome;

import com.wcagapp.wcagathome.colorblindness.ColorBlindness;
import com.wcagapp.wcagathome.colorblindness.VisionType;
import com.wcagapp.wcagathome.spectrumrenderer.SpectrumRenderer;
import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SiteProcessor {

    @Getter
    private String originalSite = "Default Original Site";
    @Getter
    private String modifiedSite = "Default Modified Site";

    private final ColorBlindness colorBlindness = new ColorBlindness();

    private int cssCounter = 0;

    private final SpectrumRenderer spectrumRenderer;

    public SiteProcessor(SpectrumRenderer spectrumRenderer) {
        this.spectrumRenderer = spectrumRenderer;
    }

    public void setOriginalSite(String urlString) {
        Document document = null;
        try {
            document = Jsoup.connect(urlString).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String baseUrl = getBaseUrl(urlString);

        Elements srcElements = document.getElementsByAttribute("src");
        Elements hrefElements = document.select("href");
        if(srcElements != null)
            hrefElements.addAll(srcElements);

        relativeLinksToAbsolute(baseUrl, hrefElements);

        List<Element> linkElements = document.select("link");
        for (Element element : linkElements) {
            if (element.attr("rel").equals("stylesheet") && element.attr("href") != null) {
                parseCss(baseUrl, element.attr("href"));
                element.attr("href", "tmpCss/" + cssCounter + ".css");
                cssCounter++;
            }
        }

        List<Element> imgElements = document.select("img");
        for (Element element : imgElements) {
            if (element.attr("src") != null) {
                if (element.hasAttr("srcset"))
                    element.attr("srcset", "");
                String fileName = requestImage(element.attr("src"));
                element.attr("src", "tmpImg/" + fileName);
            }
        }

        originalSite = document.toString();
        cssCounter = 0;
    }

    private String requestImage(String src) {
        try {
            System.out.println(src);
            Connection.Response resultImageResponse = Jsoup.connect(src)
                    .ignoreContentType(true).execute();
            if (!(src.toLowerCase().endsWith(".jpg") || src.toLowerCase().endsWith(".png") || src.toLowerCase().endsWith("jpeg") || src.toLowerCase().endsWith("svg")))
                src += ".jpg";
            File file = new File("src/main/resources/static/tmpImg" + src.substring(src.lastIndexOf("/")));
            FileOutputStream out = new FileOutputStream(file);
            out.write(resultImageResponse.bodyAsBytes());
            out.close();
            return src.substring(src.lastIndexOf("/") + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void relativeLinksToAbsolute(String baseUrl, Elements srcElements) {
        srcElements.forEach(element -> {
            if (!element.attr("src").startsWith("http"))
                if (element.attr("src").startsWith("//"))
                    element.attr("src", "https:" + element.attr("src"));
                else if (element.attr("src").startsWith("/"))
                    element.attr("src", baseUrl + element.attr("src").substring(1));
                else
                    element.attr("src", baseUrl + element.attr("src"));
        });
        srcElements.forEach(element -> {
            if (!element.attr("href").startsWith("http"))
                if (element.attr("href").startsWith("//"))
                    element.attr("href", "https:" + element.attr("href"));
                else if (element.attr("href").startsWith("/"))
                    element.attr("href", baseUrl + element.attr("href").substring(1));
                else
                    element.attr("href", baseUrl + element.attr("href"));
        });
    }

    private void parseCss(String baseUrl, String cssFile) {
        String cssContent = "";
        try {
            if (cssFile.startsWith("http"))
                cssContent = requestSite(cssFile);
        else if (cssFile.startsWith("/"))
                    cssContent = requestSite(baseUrl + cssFile.substring(1));
                else
                    cssContent = requestSite(baseUrl + cssFile);
            } catch(Exception e){
                e.printStackTrace();
            }
        try {
            saveFile(cssContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//        Matcher matcher = Pattern.compile("/url\\((.*)\\)/gm").matcher(cssContent);

//        int lastIndex = 0;
//        while (matcher.find()) {
//            if (matcher.group(1).endsWith(".css\"") || matcher.group(1).endsWith(".css'")) {
//                StringBuilder out = new StringBuilder(cssContent);
//                parseCss(baseUrl, matcher.group(1));
//
//                out.append(cssContent, lastIndex, matcher.start())
//                        .append();
//
//                lastIndex = matcher.end();
//            }
//            if (lastIndex < input.length()) {
//                out.append(input, lastIndex, input.length());
//            }
//
//        }

    private String requestSite(String url) throws Exception {
        URL URL = new URL(url);
        HttpURLConnection con = (HttpURLConnection) URL.openConnection();
        con.setRequestMethod("GET");

        StringBuilder content = new StringBuilder();
        if (con.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            return content.toString();
        } else
            throw new Exception("Couldn't get css file");
    }

    private void saveFile(String content) throws IOException {
        StringBuilder stringBuffer = new StringBuilder(content);
        File file = new File("src/main/resources/static/tmpCss/" + cssCounter + ".css");
        if (file.createNewFile()) {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(stringBuffer.toString());
            fileWriter.close();
        }
    }

    private String getBaseUrl(String urlString) {
        String baseUrl = "";
        int slashCount = 0;
        for (int c = 0; c < urlString.length(); c++) {
            if (urlString.charAt(c) == '/') {
                if (slashCount == 2) {
                    baseUrl = urlString.substring(0, c + 1);
                    break;
                }
                slashCount++;
            }
        }
        return baseUrl;
    }

    public void modifyColors(VisionType visionType) {
        switch (visionType) {
            case ACHROMATOPSIA:
                reColorSite();
        }
    }

    private void reColorSite() {
        recolorCss();
        recolorImages();
        recolorHtml();
    }

    private void recolorImages() {
        File[] dir = new File("src/main/resources/static/tmpImg/").listFiles();
        if (dir.length != 0) {
            for (File file : dir) {
                String name = file.getName();
                if (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith("jpeg")) {
                    try {
                        BufferedImage image = ImageIO.read(file);
                        for (int x = 0; x < image.getWidth(); x++) {
                            for (int y = 0; y < image.getHeight(); y++) {
                                Color c = new Color(image.getRGB(x, y), true);
                                int a = c.getAlpha();
                                Color tempColor = ColorUtil.hexToColor(colorBlindness.processColor(ColorUtil.colorToHex(c)));
                                Color finalColor = new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), a);
                                image.setRGB(x, y, finalColor.getRGB());
                            }
                        }
                        ImageIO.write(image, name.substring(name.lastIndexOf(".") + 1), file);
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void recolorHtml() {

        StringBuilder source = new StringBuilder(originalSite);

        StringBuilder output;
        Pattern pattern = Pattern.compile("#([a-fA-F0-9]{6})");
        output = new StringBuilder(replaceColorsByRegex(source.toString(), pattern));
        pattern = Pattern.compile("(rgba?|hsla?)\\(\\s*(\\d+%?)\\s*,\\s*(\\d+%?)\\s*,\\s*(\\d+%?)\\s*(?:,\\s*(.?\\d+%?(?:\\.\\d+)?))?\\s*\\)");
        output = new StringBuilder(replaceColorsByRegex(output.toString(), pattern));

        originalSite = output.toString();
//TODO itt tartottam :)
//        List<String> colors = new ArrayList<>();
//        for (int i = 0; i < originalSite.length(); i++) {
//            if (i == originalSite.length() - 8)
//                break;
//            String targetColorLarge = originalSite.substring(i, i + 7);
//            if (targetColorLarge.matches("#([a-fA-F0-9]{6})")) {
//                colors.add(targetColorLarge);
//            }
//        }
//        Map<String, String> resultColors = colorBlindness.processColors(colors);
//        resultColors.forEach((s, s2) -> {
//            originalSite = originalSite.replace(s, s2);
//        });
    }

    private String replaceColorsByRegex(String input, Pattern pattern) {
        StringBuilder out = new StringBuilder();
        Matcher matcher = pattern.matcher(input);
        int lastIndex = 0;
        File file = new File("src/main/resources/static/log.txt");
        while (matcher.find()) {
            String originalColor = "";
            String finalColor = originalColor;
            if (pattern.pattern().startsWith("#")) {
                originalColor = "#" + matcher.group(1);
                finalColor = colorBlindness.processColor(originalColor);

            } else if (matcher.group(1).equalsIgnoreCase("rgba")) {
                int a = processAlpha(matcher.group(5));
                originalColor = ColorUtil.rgbToHex(
                        Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3)),
                        Integer.parseInt(matcher.group(4)));
                finalColor = colorBlindness.processColor(originalColor) + Integer.toHexString(a);
            } else if (matcher.group(1).equalsIgnoreCase("rgb")) {
                originalColor = ColorUtil.rgbToHex(
                        Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3).endsWith("%") ? matcher.group(3).substring(0, matcher.group(3).length() - 1) : matcher.group(3)),
                        Integer.parseInt(matcher.group(4).endsWith("%") ? matcher.group(4).substring(0, matcher.group(4).length() - 1) : matcher.group(4)));

                finalColor = colorBlindness.processColor(originalColor);
            } else if (matcher.group(1).equalsIgnoreCase("hsla")) {
                int a = processAlpha(matcher.group(5));
                Color c = Color.getHSBColor(
                        Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3).endsWith("%") ? matcher.group(3).substring(0, matcher.group(3).length() - 1) : matcher.group(3)),
                        Integer.parseInt(matcher.group(4).endsWith("%") ? matcher.group(4).substring(0, matcher.group(4).length() - 1) : matcher.group(4)));
                originalColor = ColorUtil.colorToHex(c);
                finalColor = colorBlindness.processColor(originalColor) + Integer.toHexString(a);
            } else if (matcher.group(1).equalsIgnoreCase("hsl")) {
                Color c = Color.getHSBColor(
                        Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3).endsWith("%") ? matcher.group(3).substring(0, matcher.group(3).length() - 1) : matcher.group(3)),
                        Integer.parseInt(matcher.group(4).endsWith("%") ? matcher.group(4).substring(0, matcher.group(4).length() - 1) : matcher.group(4)));
                originalColor = ColorUtil.colorToHex(c);
                finalColor = colorBlindness.processColor(originalColor);
            }

            try {

                FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(input.substring(matcher.start(), matcher.end()) + " to " + finalColor);
                bw.newLine();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            out.append(input, lastIndex, matcher.start())
                    .append(finalColor);

            lastIndex = matcher.end();
        }
        if (lastIndex < input.length()) {
            out.append(input, lastIndex, input.length());
        }
        return out.toString();
    }

    private int processAlpha(String alpha) {
        if (alpha.startsWith("."))
            alpha = "0" + alpha;
        return alpha == null ? 255 : (int) (Double.parseDouble(alpha) * 255);
    }

    private void recolorCss() {
        File[] dir = new File("src/main/resources/static/tmpCss/").listFiles();
        if (dir.length != 0) {
            for (File file : dir) {
                try {
                    BufferedReader sourceReader = new BufferedReader(new FileReader(file));
                    String strCurrentLine;
                    StringBuilder source = new StringBuilder();
                    while ((strCurrentLine = sourceReader.readLine()) != null) {
                        source.append(strCurrentLine);
                    }
                    sourceReader.close();

                    StringBuilder output;
                    Pattern pattern = Pattern.compile("#([a-fA-F0-9]{6})");
                    output = new StringBuilder(replaceColorsByRegex(source.toString(), pattern));
                    pattern = Pattern.compile("(rgba?|hsla?)\\(\\s*(\\d+%?)\\s*,\\s*(\\d+%?)\\s*,\\s*(\\d+%?)\\s*(?:,\\s*(.?\\d+%?(?:\\.\\d+)?))?\\s*\\)");
                    output = new StringBuilder(replaceColorsByRegex(output.toString(), pattern));

                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(output.toString());
                    fileWriter.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
