package com.projekt.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;


public interface Function {
    void apply(Canvas canvas, GraphicsContext gc);
    String getName();
    default Image getPreviewImage() {
        return null;
    }
}