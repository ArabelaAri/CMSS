package com.projekt.paint;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

public class MagnifierTool implements Tool {
    private PaintController controller;
    private boolean isMagnifying = false;

    public MagnifierTool(PaintController controller) {
        this.controller = controller;
    }

    @Override
    public void onPress(GraphicsContext gc, double x, double y) {
        if (!isMagnifying) {
            magnify(gc, x, y);
            isMagnifying = true;
        }
    }

    @Override
    public void onDrag(GraphicsContext gc, double x, double y) {
        if (isMagnifying) {
            controller.redrawCanvas();
            magnify(gc, x, y);
        }
    }

    @Override
    public void onRelease(GraphicsContext gc, double x, double y) {
        if (isMagnifying) {
            controller.redrawCanvas();
            isMagnifying = false;
        }
    }

    private void magnify(GraphicsContext gc, double x, double y) {
        double size = 100; 
        double scale = 2; 

        WritableImage snapshot = new WritableImage((int) size, (int) size);
        gc.getCanvas().snapshot(null, snapshot);

        gc.save();
        gc.translate(x - size / 2, y - size / 2);
        gc.scale(scale, scale);
        gc.drawImage(snapshot, 0, 0, size / scale, size / scale);
        gc.restore();

        gc.setStroke(javafx.scene.paint.Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x - size / 2, y - size / 2, size, size);
    }
}
