package com.projekt.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;

public class BlurFunction implements Function {

    private final double radius;

    public BlurFunction(double radius) {
        this.radius = radius;
    }

    @Override
    public void apply(Canvas canvas, GraphicsContext gc) {
        GaussianBlur blur = new GaussianBlur(radius);
        gc.applyEffect(blur);
    }

    @Override
    public String getName() {
        return "Rozostření";
    }
}