package com.wcagapp.wcagathome;

import java.awt.*;

import static java.lang.Math.abs;

public final class ColorUtil {

    public static String colorToHex(Color c) {
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        return "#"
                + (Integer.toHexString(red).length() == 2 ? Integer.toHexString(red) : 0 + Integer.toHexString(red))
                + (Integer.toHexString(green).length() == 2 ? Integer.toHexString(green) : 0 + Integer.toHexString(green))
                + (Integer.toHexString(blue).length() == 2 ? Integer.toHexString(blue) : 0 + Integer.toHexString(blue));
    }

    public static String rgbToHex(int red, int green, int blue) {
        return "#"
                + (Integer.toHexString(red).length() == 2 ? Integer.toHexString(red) : 0 + Integer.toHexString(red))
                + (Integer.toHexString(green).length() == 2 ? Integer.toHexString(green) : 0 + Integer.toHexString(green))
                + (Integer.toHexString(blue).length() == 2 ? Integer.toHexString(blue) : 0 + Integer.toHexString(blue));
    }

    public static String rgbToHex(int red, int green, int blue, int alpha) {
        return "#"
                + (Integer.toHexString(red).length() == 2 ? Integer.toHexString(red) : 0 + Integer.toHexString(red))
                + (Integer.toHexString(green).length() == 2 ? Integer.toHexString(green) : 0 + Integer.toHexString(green))
                + (Integer.toHexString(blue).length() == 2 ? Integer.toHexString(blue) : 0 + Integer.toHexString(blue))
                + alpha;
    }

    public static String hsvToHex(double h, double s, double v) {
        return colorToHex(hsvToColor(h, s, v));
    }

    public static Color hexToColor(String hex) {
        int red = Integer.parseInt(hex.substring(1, 3), 16);
        int green = Integer.parseInt(hex.substring(3, 5), 16);
        int blue = Integer.parseInt(hex.substring(5, 7), 16);

        return new Color(red, green, blue);
    }

    public static Color hsvToColor(double h, double s, double v) {
        double c = v * s;
        double h_ = h / 60;

        double x = c * (1 - abs(h_ % 2 - 1));

        double r = 0, g = 0, b = 0;

        if (h_ == 0 || h_ <= 1) {
            r = c;
            g = x;
            b = 0;
        } else if (h_ <= 2) {
            r = x;
            g = c;
            b = 0;
        } else if (h_ <= 3) {
            r = 0;
            g = c;
            b = x;
        } else if (h_ <= 4) {
            r = 0;
            g = x;
            b = c;
        } else if (h_ <= 5) {
            r = x;
            g = 0;
            b = c;
        } else if (h_ <= 6) {
            r = c;
            g = 0;
            b = x;
        }

        double m = v - c;

        int R = (int) ((r + m) * 255);
        int G = (int) ((g + m) * 255);
        int B = (int) ((b + m) * 255);

        if(R > 255)
            R = 255;
        if(R < 0)
            R = 0;
        if(G > 255)
            G = 255;
        if(G < 0)
            G = 0;
        if(B > 255)
            B = 255;
        if(B < 0)
            B = 0;

        return new Color(R, G, B);
    }

    public static double[] colorToHsv(Color c) {
        double r_ = 0, g_ = 0, b_ = 0;

        r_ = c.getRed() / 255.0;
        g_ = c.getGreen() / 255.0;
        b_ = c.getBlue() / 255.0;

        // h, s, v = hue, saturation, value
        double cmax = Math.max(r_, Math.max(g_, b_)); // maximum of r, g, b
        double cmin = Math.min(r_, Math.min(g_, b_)); // minimum of r, g, b
        double diff = cmax - cmin; // diff of cmax and cmin.
        double h = -1, s = -1;

        if (cmax == cmin)
            h = 0;

        else if (cmax == r_) {
            h = 60 * (((g_ - b_) / diff) % 6);
        } else if (cmax == g_) {
            h = 60 * (((b_ - r_) / diff) + 2);
        } else if (cmax == b_) {
            h = 60 * (((r_ - g_) / diff) + 4);
        }
        double v = cmax;

        if (v == 0)
            s = 0;
        else
            s = (diff / v);

        // compute v

        double[] result = new double[3];
        result[0] = h;
        result[1] = s;
        result[2] = v;
        return result;
    }

    public static double[] hexToHsv(String hex) {
        return colorToHsv(hexToColor(hex));
    }


}
