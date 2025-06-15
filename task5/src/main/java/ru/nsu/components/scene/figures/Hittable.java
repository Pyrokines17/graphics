package ru.nsu.components.scene.figures;

import ru.nsu.components.scene.Interval;
import ru.nsu.components.scene.algebra.Ray;

public interface Hittable {
    boolean hit(Ray ray, Interval rayT, HitRecord record);
}
