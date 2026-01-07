package com.projekt.paint;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;

public class OilColorsTool implements Tool {
    private PaintController controller;

    public OilColorsTool(PaintController controller) {
        this.controller = controller;
    }

    @Override
    public void onPress(GraphicsContext gc, double x, double y) {
        drawOilColors(gc, x, y);
    }

    @Override
    public void onDrag(GraphicsContext gc, double x, double y) {
        drawOilColors(gc, x, y);
    }

    @Override
    public void onRelease(GraphicsContext gc, double x, double y) {
    }

    private void drawOilColors(GraphicsContext gc, double x, double y) {
        Color baseColor = controller.getCurrentColor();
        double size = controller.getBrushSize();
        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.setGlobalAlpha(0.8);
        gc.setFill(baseColor);
        gc.fillOval(x - size, y - size, size * 2, size * 2);
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * size * 0.6;
            double offsetY = (Math.random() - 0.5) * size * 0.6;
            double radius = size * (0.2 + Math.random() * 0.3);

            gc.setFill(new Color(baseColor.getRed() * (0.8 + Math.random() * 0.4),
                                baseColor.getGreen() * (0.8 + Math.random() * 0.4),
                                baseColor.getBlue() * (0.8 + Math.random() * 0.4),
                                0.6 + Math.random() * 0.4));
            gc.fillOval(x + offsetX - radius, y + offsetY - radius, radius * 2, radius * 2);
        }

        gc.setGlobalAlpha(1.0);
    }
}
