package me.udnekjupiter.util;

import org.realityforge.vecmath.Vector3d;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.function.Consumer;

public class Utils {

    public static int NANOS_IN_SECOND = (int) Math.pow(10, 9);

    ///////////////////////////////////////////////////////////////////////////
    // GENERAL
    ///////////////////////////////////////////////////////////////////////////
    public static String roundToPrecision(double number, int precision){
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumFractionDigits(precision);
        return numberFormat.format(number);
    }

    public static boolean roughlyEquals(double a, double b, double maxDistance){
        return Math.abs(a + b) <= maxDistance;
    }

    public static <T> void iterate(T[][] array, Consumer<T> consumer){
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                consumer.accept(array[i][j]);
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // IMAGE
    ///////////////////////////////////////////////////////////////////////////
    public static BufferedImage resizeImage(BufferedImage image, int newWidth, int newHeight){
        BufferedImage newImage = new BufferedImage(newWidth, newHeight, image.getType());
        newImage.getGraphics().drawImage(image, 0, 0, newWidth, newHeight, null);
        return newImage;
    }
    public static void resizeImage(BufferedImage image, int newWidth, int newHeight, BufferedImage output){
        output.getGraphics().drawImage(image, 0, 0, newWidth, newHeight, null);
    }

    ///////////////////////////////////////////////////////////////////////////
    // COLOR
    ///////////////////////////////////////////////////////////////////////////

    public static Color vectorToColor(Vector3d color){
        return new Color((float) color.x, (float) color.y, (float) color.z);
    }

    public static int multiplyColor(int intColor, float n){
        Color color = new Color(intColor);
        return new Color(
                Math.max(color.getRed()/255f * n, 1),
                Math.max(color.getGreen()/255f * n, 1),
                Math.max(color.getBlue()/255f * n, 1)
        ).getRGB();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    public static float normalizeYaw(float angle){
        angle = angle % 360f;
        if (angle > 180) angle = -360 + angle;
        else if (angle < -180) angle = 360 + angle;
        return angle;
    }


    public static float normalizePitch(float angle){
        if (angle > 90) {return 90;}
        if (angle < -90) {return -90;}
        return angle;
    }

}
