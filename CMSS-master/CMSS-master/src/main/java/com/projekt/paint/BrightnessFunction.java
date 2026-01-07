package com.projekt.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;

public class BrightnessFunction implements Function {

    private final double brightness; 

    public BrightnessFunction(double brightness) {
        this.brightness = brightness;
    }

    @Override
    public void apply(Canvas canvas, GraphicsContext gc) {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(brightness);
        gc.applyEffect(colorAdjust);
    }

    @Override
    public String getName() {
        return brightness >= 0 ? "Zesvětlení" : "Ztmavení";
    }
}