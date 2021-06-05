package com.wcagapp.wcagathome.colorblindness;

import org.ejml.simple.SimpleMatrix;

public class MatrixHelper {

    public MatrixHelper() {
        transMatrix = HPETransMatrix.mult(MsRGBTransMatrix);
        transInvertMatrix = transMatrix.invert();
    }

    public SimpleMatrix transMatrix;
    //    new SimpleMatrix(new double[][]{
//            new double[]{0.31399022, 0.63951294, 0.04649755},
//            new double[]{0.15537241, 0.75789446, 0.8670142},
//            new double[]{0.01775239, 0.10944209, 0.87256922}
//    });
    public SimpleMatrix transInvertMatrix;
//    = new SimpleMatrix(new double[][]{
//            new double[]{5.47221206, -4.6419601, 0.16963708},
//            new double[]{-1.1252419, 2.29317094, -0.1678952},
//            new double[]{0.02980165, -0.19318073, 1.16364789}
//    });;


    public static SimpleMatrix M_PROTANOPIA = new SimpleMatrix(new double[][]{
            new double[]{0, 1.05118294, -0.05116099},
            new double[]{0, 1, 0},
            new double[]{0, 0, 1}
    });


    public static SimpleMatrix M_DEUTRANOPIA = new SimpleMatrix(new double[][]{
            new double[]{1, 0, 0},
            new double[]{0.9513092, 0, 0.04866992},
            new double[]{0, 0, 1}
    });

    public static SimpleMatrix M_TRITANOPIA = new SimpleMatrix(new double[][]{
            new double[]{1, 0, 0},
            new double[]{0, 1, 0},
            new double[]{-0.86744736, 1.86727089, 0}
    });

    public static SimpleMatrix MsRGBTransMatrix = new SimpleMatrix(new double[][]{
            new double[]{0.4124564, 0.3575761, 0.1804375},
            new double[]{0.2126729, 0.7151522, 0.0721750},
            new double[]{0.0193339, 0.1191920, 0.9503041}
    });

    public static SimpleMatrix HPETransMatrix = new SimpleMatrix(new double[][]{
            new double[]{0.4002, 0.7076, -0.0808},
            new double[]{-0.2263, 1.1653, 0.0457},
            new double[]{0, 0, 0.9182}
    });

    public SimpleMatrix MrgbToMlms(double[][] Mrgb) {
        return new SimpleMatrix(transMatrix).mult(new SimpleMatrix(Mrgb));
    }

    public SimpleMatrix MrgbToMlms(SimpleMatrix Mrgb) {
        return Mrgb.mult(transMatrix);
    }

    public SimpleMatrix MlmsToMrgb(SimpleMatrix Mlms) {
        return transInvertMatrix.mult(Mlms);
    }

}
