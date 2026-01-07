package com.projekt.paint;

import javafx.scene.canvas.GraphicsContext;
import java.util.Random;

public class SprayTool implements Tool {
    private PaintController controller;
    private Random random = new Random();

    public SprayTool(PaintController controller) {
        this.controller = controller;
    }

    @Override
    public void onPress(GraphicsContext gc, double x, double y) {
        spray(gc, x, y);
    }

    @Override
    public void onDrag(GraphicsContext gc, double x, double y) {
        spray(gc, x, y);
    }

    @Override
    public void onRelease(GraphicsContext gc, double x, double y) {
    }

    private void spray(GraphicsContext gc, double x, double y) {
        gc.setFill(controller.getCurrentColor());
        int density = controller.getBrushSize() * 2;
        for (int i = 0; i < density; i++) {
            double offsetX = (random.nextDouble() - 0.5) * controller.getBrushSize();
            double offsetY = (random.nextDouble() - 0.5) * controller.getBrushSize();
            gc.fillOval(x + offsetX, y + offsetY, 1, 1);
        }
    }
}
