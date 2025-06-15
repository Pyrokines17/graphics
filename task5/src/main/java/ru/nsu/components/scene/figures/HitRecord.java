package ru.nsu.components.scene.figures;

import ru.nsu.components.scene.algebra.Ray;
import ru.nsu.components.scene.algebra.Vector;

public class HitRecord {
    private double t;
    private Vector point;
    private Vector normal;
    private boolean frontFace;
    private Material mat;
    private Double u;
    private Double v;

    public HitRecord() {
        this.t = 0.0;
        this.point = new Vector(0, 0, 0);
        this.normal = new Vector(0, 0, 0);
        this.mat = new Material();
    }

    public double getT() {
        return t;
    }

    public void setT(double t) {
        this.t = t;
    }

    public Vector getPoint() {
        return point;
    }

    public void setPoint(Vector point) {
        this.point = point;
    }

    public Vector getNormal() {
        return normal;
    }

    public void setNormal(Vector normal) {
        this.normal = normal;
    }

    public void setFaceNormal(Ray ray, Vector outwardNormal) {
        frontFace = Vector.dot(ray.getDirection(), outwardNormal) < 0;
        normal = frontFace ? outwardNormal : outwardNormal.multiply(-1);
    }

    public boolean isFrontFace() {
        return frontFace;
    }

    public void setMaterial(Material mat) {
        this.mat = mat;
    }

    public Material getMaterial() {
        return mat;
    }

    public Double getU() {
        return u;
    }

    public void setU(Double u) {
        this.u = u;
    }

    public Double getV() {
        return v;
    }

    public void setV(Double v) {
        this.v = v;
    }
}
