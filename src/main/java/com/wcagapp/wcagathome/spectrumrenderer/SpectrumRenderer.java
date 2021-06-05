package com.wcagapp.wcagathome.spectrumrenderer;

import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class SpectrumRenderer {

    @Getter
    private Map<String, String> achromatopsiaMap = new HashMap<>();

    private String toHex(int rgb) {
        return String.format("#%06X", (0xFFFFFF & rgb)).toLowerCase();
    }

    public void init() {
        try {
            BufferedImage normal = ImageIO.read(new File("src/main/resources/static/colorspectrum/normal.png"));
            BufferedImage achromatopsia = ImageIO.read(new File("src/main/resources/static/colorspectrum/deuteranopia.png"));
            Integer width = normal.getWidth();
            Integer height = normal.getHeight();
            Integer midHeight = height / 2;
            for (int x = 0; x < width; x++) {
                achromatopsiaMap.put(toHex(normal.getRGB(x, midHeight)), toHex(achromatopsia.getRGB(x, midHeight)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
