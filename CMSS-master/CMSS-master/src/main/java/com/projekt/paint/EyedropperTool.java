package com.projekt.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class EyedropperTool implements Tool {
    private PaintController controller;

    public EyedropperTool(PaintController controller) {
        this.controller = controller;
    }

    @Override
    public void onPress(GraphicsContext gc, double x, double y) {
        pickColor(gc, x, y);
    }

    @Override
    public void onDrag(GraphicsContext gc, double x, double y) {
    }

    @Override
    public void onRelease(GraphicsContext gc, double x, double y) {
    }

    private void pickColor(GraphicsContext gc, double x, double y) {
        WritableImage snapshot = new WritableImage((int) gc.getCanvas().getWidth(), (int) gc.getCanvas().getHeight());
        gc.getCanvas().snapshot(null, snapshot);
        PixelReader pixelReader = snapshot.getPixelReader();

        if (x >= 0 && x < snapshot.getWidth() && y >= 0 && y < snapshot.getHeight()) {
            Color color = pixelReader.getColor((int) x, (int) y);
            controller.setCurrentColor(color);
        }
    }
}
