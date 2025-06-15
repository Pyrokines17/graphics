package ru.nsu.components.scene.figures;

import ru.nsu.components.scene.algebra.Ray;
import ru.nsu.components.scene.algebra.Vector;

public class Lambertian extends Material {
    private Vector albedo;

    public Lambertian(Vector albedo) {
        this.albedo = albedo;
    }

    @Override
    public boolean scatter(Ray rayIn, HitRecord record, Vector attenuation, Ray scattered) {
        var scatterDirection = Vector.add(record.getNormal(), Vector.randomUnitVector());

        if (scatterDirection.nearZero()) {
            scatterDirection = record.getNormal();
        }

        scattered.setOrigin(record.getPoint());
        scattered.setDirection(scatterDirection);

        attenuation.setX(albedo.getX());
        attenuation.setY(albedo.getY());
        attenuation.setZ(albedo.getZ());
        return true;
    }
}
