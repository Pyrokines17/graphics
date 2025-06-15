package ru.nsu.components.scene.figures;

import ru.nsu.components.scene.algebra.Ray;
import ru.nsu.components.scene.algebra.Vector;

public class Material {
    public Vector emitted(Double u, Double v, Vector point) {
        return new Vector(0, 0, 0);
    }

    public boolean scatter(Ray rayIn, HitRecord record, Vector attenuation, Ray scattered) {
        return false;
    }
}
