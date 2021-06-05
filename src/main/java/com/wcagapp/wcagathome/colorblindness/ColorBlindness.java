package com.wcagapp.wcagathome.colorblindness;

import com.wcagapp.wcagathome.ColorUtil;
import org.ejml.simple.SimpleMatrix;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ColorBlindness {

    private MatrixHelper matrixHelper = new MatrixHelper();

    private double removeGammaCorrection(int v) {
        if (v <= 0.04045 * 255)
            return (v / 255.0) / 12.92;
        else
            return Math.pow(((v / 255.0) + 0.055) / 1.055, 2.4);
    }

    private int reapplyGammaCorrection(double v_) {
        if (v_ <= 0.0031308)
            return (int) (255 * 12.92 * v_);
        else
            return (int) (255 * (1.055 * Math.pow(v_, 0.41666) - 0.055));
    }

    public String processColor(String hex) {
        Color c = ColorUtil.hexToColor(hex);

        double r = removeGammaCorrection(c.getRed());
        double g = removeGammaCorrection(c.getGreen());
        double b = removeGammaCorrection(c.getBlue());

        SimpleMatrix Mlms = matrixHelper.MrgbToMlms(new double[][]{
                new double[]{r},
                new double[]{g},
                new double[]{b}
        });

        Mlms = MatrixHelper.M_TRITANOPIA.mult(Mlms);

        SimpleMatrix resultMatrix = matrixHelper.MlmsToMrgb(Mlms);
        r = resultMatrix.get(0, 0);
        g = resultMatrix.get(1, 0);
        b = resultMatrix.get(2, 0);

        r = reapplyGammaCorrection(r);
        g = reapplyGammaCorrection(g);
        b = reapplyGammaCorrection(b);

        if (r > 255)
            r = 255;
        if (r < 0)
            r = 0;
        if (g > 255)
            g = 255;
        if (g < 0)
            g = 0;
        if (b > 255)
            b = 255;
        if (b < 0)
            b = 0;

        return ColorUtil.rgbToHex((int) r, (int) g, (int) b);
    }

    public Map<String, String> processColors(List<String> colors) {
        Map<String, String> resultMap = new HashMap<>();
        for (String c : colors) {
            resultMap.put(c, processColor(c));
        }
        return resultMap;
    }

}
