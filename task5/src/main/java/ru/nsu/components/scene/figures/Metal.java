package ru.nsu.components.scene.figures;

import ru.nsu.components.scene.algebra.Ray;
import ru.nsu.components.scene.algebra.Vector;

public class Metal extends Material {
    private Vector albedo;

    public Metal(Vector albedo) {
        this.albedo = albedo;
    }

    @Override
    public boolean scatter(Ray rayIn, HitRecord record, Vector attenuation, Ray scattered) {
        Vector reflected = Vector.reflect(rayIn.getDirection(), record.getNormal());
        scattered.setOrigin(record.getPoint());
        scattered.setDirection(reflected);

        attenuation.setX(albedo.getX());
        attenuation.setY(albedo.getY());
        attenuation.setZ(albedo.getZ());

        return true;
    }
}
