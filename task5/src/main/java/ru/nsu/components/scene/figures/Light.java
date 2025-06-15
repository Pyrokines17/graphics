package ru.nsu.components.scene.figures;

import ru.nsu.components.scene.algebra.Vector;

public class Light extends Material {
    private Texture texture;

    public Light(Texture texture) {
        this.texture = texture;
    }

    public Light(Vector color) {
        this.texture = new SolidColor(color);
    }

    @Override
    public Vector emitted(Double u, Double v, Vector point) {
        return texture.value(u, v, point);
    }
}
