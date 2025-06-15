package ru.nsu.components.scene.figures;

import ru.nsu.components.scene.Interval;
import ru.nsu.components.scene.algebra.Ray;
import ru.nsu.components.scene.algebra.Vector;

public class Sphere implements Hittable {
    Vector center;
    double radius;
    Material material;

    public Sphere(Vector center, double radius, Material material) {
        this.center = center;
        this.radius = Math.max(0, radius);
        this.material = material;
    }

    @Override
    public boolean hit(Ray ray, Interval rayT, HitRecord record) {
        Vector oc = Vector.subtract(center, ray.getOrigin());
        double a = Vector.getLengthSquared(ray.getDirection());
        double h = Vector.dot(ray.getDirection(), oc);
        double c = Vector.getLengthSquared(oc) - radius * radius;
        double discriminant = h * h - a * c;

        if (discriminant < 0) {
            return false;
        }

        double sqrtDiscriminant = Math.sqrt(discriminant);

        double root = (h - sqrtDiscriminant) / a;

        if (!(rayT.surrounds(root))) {
            root = (h + sqrtDiscriminant) / a;
            if (!rayT.surrounds(root)) {
                return false;
            }
        }

        record.setT(root);
        record.setPoint(ray.pointAtParameter(root));
        Vector outwardNormal = Vector.divide(
            Vector.subtract(record.getPoint(), center),
            radius
        );
        record.setFaceNormal(ray, outwardNormal);
        getSphereUV(outwardNormal, record);
        record.setMaterial(material);

        return true;
    }

    private static void getSphereUV(Vector p, HitRecord record) {
        double theta = Math.acos(-p.getY());
        double phi = Math.atan2(-p.getZ(), p.getX()) + Math.PI;

        double u = phi / (2 * Math.PI);
        double v = theta / Math.PI;

        record.setU(u);
        record.setV(v);
    }

    public Vector getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }
}
