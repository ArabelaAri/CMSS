package com.projekt.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.nio.IntBuffer;

public class NegativeFunction implements Function {

    @Override
    public void apply(Canvas canvas, GraphicsContext gc) {
        int width = (int) canvas.getWidth();
        int height = (int) canvas.getHeight();
        
        WritableImage snapshot = new WritableImage(width, height);
        canvas.snapshot(null, snapshot);

        PixelReader reader = snapshot.getPixelReader();
        PixelWriter writer = snapshot.getPixelWriter();

     
        int[] pixels = new int[width * height];
        reader.getPixels(0, 0, width, height, 
                        javafx.scene.image.PixelFormat.getIntArgbInstance(), 
                        pixels, 0, width);
        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            int a = (argb >> 24) & 0xFF; 
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            r = 255 - r;
            g = 255 - g;
            b = 255 - b;
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        writer.setPixels(0, 0, width, height, 
                        javafx.scene.image.PixelFormat.getIntArgbInstance(), 
                        pixels, 0, width);
        gc.drawImage(snapshot, 0, 0);
    }

    @Override
    public String getName() {
        return "Negativ";
    }
}