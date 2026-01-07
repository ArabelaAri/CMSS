package com.projekt.paint;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PenTool implements Tool {
    private PaintController controller;
    private double lastX, lastY;

    public PenTool(PaintController controller) {
        this.controller = controller;
    }

    @Override
    public void onPress(GraphicsContext gc, double x, double y) {
        lastX = x;
        lastY = y;
        gc.setStroke(controller.getCurrentColor());
        gc.setLineWidth(controller.getBrushSize());
        gc.strokeLine(x, y, x, y);
    }

    @Override
    public void onDrag(GraphicsContext gc, double x, double y) {
        gc.strokeLine(lastX, lastY, x, y);
        lastX = x;
        lastY = y;
    }

    @Override
    public void onRelease(GraphicsContext gc, double x, double y) {
    }
}
