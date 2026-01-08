package com.projekt.paint;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class HighlighterTool implements Tool {
    private PaintController controller;
    private double lastX, lastY;

    public HighlighterTool(PaintController controller) {
        this.controller = controller;
    }

    @Override
    public void onPress(GraphicsContext gc, double x, double y) {
        lastX = x;
        lastY = y;
        gc.save();
        Color baseColor = controller.getCurrentColor();
        gc.setStroke(baseColor.deriveColor(0, 1.2, 1.2, 0.4)); 
        gc.setLineWidth(controller.getBrushSize() * 2.5);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setGlobalBlendMode(BlendMode.MULTIPLY); 
        
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
        gc.restore();
    }
}