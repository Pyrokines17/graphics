package ru.nsu.components.scene.algebra;

public class Ray {
    private Vector origin;
    private Vector direction;

    public Ray(Vector origin, Vector direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vector getOrigin() {
        return origin;
    }

    public void setOrigin(Vector origin) {
        this.origin = origin;
    }

    public Vector getDirection() {
        return direction;
    }

    public void setDirection(Vector direction) {
        this.direction = direction;
    }

    public Vector pointAtParameter(double t) {
        return new Vector(
            origin.getX() + t * direction.getX(),
            origin.getY() + t * direction.getY(),
            origin.getZ() + t * direction.getZ()
        );
    }
}
