package com.projekt.paint;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;

public class WatercolorTool implements Tool {
    private PaintController controller;

    public WatercolorTool(PaintController controller) {
        this.controller = controller;
    }

    @Override
    public void onPress(GraphicsContext gc, double x, double y) {
        drawWatercolor(gc, x, y);
    }

    @Override
    public void onDrag(GraphicsContext gc, double x, double y) {
        drawWatercolor(gc, x, y);
    }

    @Override
    public void onRelease(GraphicsContext gc, double x, double y) {
    }

    private void drawWatercolor(GraphicsContext gc, double x, double y) {
        Color baseColor = controller.getCurrentColor();
        double size = controller.getBrushSize();

        gc.setGlobalBlendMode(BlendMode.MULTIPLY);
        gc.setGlobalAlpha(0.5);

        for (int i = 0; i < 3; i++) {
            double offsetX = (Math.random() - 0.5) * size * 0.3;
            double offsetY = (Math.random() - 0.5) * size * 0.3;
            double radius = size * (0.5 + Math.random() * 0.5);

            gc.setFill(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(),
                                baseColor.getOpacity() * (0.3 + Math.random() * 0.4)));
            gc.fillOval(x + offsetX - radius, y + offsetY - radius, radius * 2, radius * 2);
        }

        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.setGlobalAlpha(1.0);
    }
}
