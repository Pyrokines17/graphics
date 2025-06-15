package ru.nsu.components.scene.figures;

import ru.nsu.components.scene.algebra.Vector;

public class SolidColor implements Texture {
    private final Vector color;

    public SolidColor(Vector color) {
        this.color = color;
    }

    public SolidColor(double r, double g, double b) {
        this.color = new Vector(r, g, b);
    }

    @Override
    public Vector value(double u, double v, Vector point) {
        return color;
    }
}
