package com.projekt.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;

public class GrayscaleFunction implements Function {

    @Override
    public void apply(Canvas canvas, GraphicsContext gc) {
        ColorAdjust grayscale = new ColorAdjust();
        grayscale.setSaturation(-1.0);
        gc.applyEffect(grayscale);
    }

    @Override
    public String getName() {
        return "Odstíny šedi";
    }
}