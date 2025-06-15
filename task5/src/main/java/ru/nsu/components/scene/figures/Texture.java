package ru.nsu.components.scene.figures;

import ru.nsu.components.scene.algebra.Vector;

public interface Texture {
    Vector value(double u, double v, Vector point);
}
