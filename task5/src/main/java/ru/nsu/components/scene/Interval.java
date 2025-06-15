package ru.nsu.components.scene;

public class Interval {
    private double min;
    private double max;

    public Interval() {
        this.min = Double.POSITIVE_INFINITY;
        this.max = Double.NEGATIVE_INFINITY;
    }

    public Interval(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double size() {
        return max - min;
    }

    public boolean contains(double value) {
        return value >= min && value <= max;
    }

    public boolean surrounds(double value) {
        return value > min && value < max;
    }

    double clamp(double value) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }
}
