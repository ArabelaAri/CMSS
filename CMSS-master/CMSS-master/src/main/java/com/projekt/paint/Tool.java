package com.projekt.paint;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

public interface Tool {
    void onPress(GraphicsContext gc, double x, double y);
    void onDrag(GraphicsContext gc, double x, double y);
    void onRelease(GraphicsContext gc, double x, double y);
}
