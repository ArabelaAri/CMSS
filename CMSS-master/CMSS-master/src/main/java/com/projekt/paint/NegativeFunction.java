package com.projekt.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class NegativeFunction implements Function {

    @Override
    public void apply(Canvas canvas, GraphicsContext gc) {
        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);

        PixelReader reader = snapshot.getPixelReader();
        PixelWriter writer = gc.getPixelWriter();

        for (int y = 0; y < snapshot.getHeight(); y++) {
            for (int x = 0; x < snapshot.getWidth(); x++) {
                Color c = reader.getColor(x, y);
                Color negative = Color.color(1 - c.getRed(), 1 - c.getGreen(), 1 - c.getBlue());
                writer.setColor(x, y, negative);
            }
        }
    }

    @Override
    public String getName() {
        return "Negativ";
    }
}