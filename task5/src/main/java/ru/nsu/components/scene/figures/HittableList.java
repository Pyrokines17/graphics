package ru.nsu.components.scene.figures;

import ru.nsu.components.scene.Interval;
import ru.nsu.components.scene.algebra.Ray;

import java.util.ArrayList;
import java.util.List;

public class HittableList implements Hittable {
    private List<Hittable> objects = new ArrayList<>();

    public HittableList() {}

    public HittableList(Hittable object) {
        add(object);
    }

    public void clear() {
        objects.clear();
    }

    public void add(Hittable object) {
        objects.add(object);
    }

    @Override
    public boolean hit(Ray ray, Interval rayT, HitRecord record) {
        HitRecord tempRecord = new HitRecord();
        boolean hitAnything = false;
        double closestSoFar = rayT.getMax();

        for (Hittable object : objects) {
            if (object.hit(ray, new Interval(rayT.getMin(), closestSoFar), tempRecord)) {
                hitAnything = true;
                closestSoFar = tempRecord.getT();
                record.setT(tempRecord.getT());
                record.setPoint(tempRecord.getPoint());
                record.setNormal(tempRecord.getNormal());
                record.setMaterial(tempRecord.getMaterial());
                record.setU(tempRecord.getU());
                record.setV(tempRecord.getV());
            }
        }

        return hitAnything;
    }
}
