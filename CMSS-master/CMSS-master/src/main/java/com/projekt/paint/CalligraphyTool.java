package com.projekt.paint;

import javafx.scene.canvas.GraphicsContext;

public class CalligraphyTool implements Tool {
    private PaintController controller;
    private double lastX, lastY;

    public CalligraphyTool(PaintController controller) {
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
        double dx = x - lastX;
        double dy = y - lastY;
        double angle = Math.atan2(dy, dx);
        double length = Math.sqrt(dx * dx + dy * dy);

        for (double t = 0; t < length; t += 1) {
            double px = lastX + (dx * t / length);
            double py = lastY + (dy * t / length);
            double width = controller.getBrushSize() * (0.5 + 0.5 * Math.sin(angle + t * 0.1));
            gc.setLineWidth(width);
            gc.strokeLine(px, py, px + 1, py + 1);
        }

        lastX = x;
        lastY = y;
    }

    @Override
    public void onRelease(GraphicsContext gc, double x, double y) {
    }
}
