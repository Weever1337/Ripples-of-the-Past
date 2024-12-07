package com.github.standobyte.jojo.client.render.entity.model.animnew.floatquery;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Keyframe;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation;
import com.google.gson.JsonArray;

import net.minecraft.util.math.vector.Vector3f;

public class KeyframeWithQuery {
    private Keyframe keyframe;
    private final Vector3f keyframeTarget;
    @Nullable private final IFloatSupplier[] query;
    
    public static KeyframeWithQuery constant(Vector3f vec) {
        return new KeyframeWithQuery(vec, null);
    }
    
    private KeyframeWithQuery(Vector3f keyframeTarget, @Nullable IFloatSupplier[] query) {
        this.keyframeTarget = keyframeTarget;
        this.query = query;
    }
    
    public KeyframeWithQuery withKeyframe(float timestamp, Transformation.Interpolation interpolation) {
        keyframe = new Keyframe(timestamp, keyframeTarget, interpolation);
        return this;
    }
    
    public Keyframe getKeyframe() {
        return keyframe;
    }
    
    public void applyContext(AnimContext animContext) {
        if (query != null) {
            keyframeTarget.set(query[0].get(animContext), query[1].get(animContext), query[2].get(animContext));
        }
    }
    
    public static KeyframeWithQuery parseJsonVec(JsonArray vecJson) {
        boolean isNumericLiteral = true;
        IFloatSupplier[] elements = new IFloatSupplier[3];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = IFloatSupplier.elementFromJson(vecJson.get(i));
            isNumericLiteral &= elements[i].isNumericLiteral();
        }
        if (isNumericLiteral) {
            return new KeyframeWithQuery(new Vector3f(elements[0].get(null), elements[1].get(null), elements[2].get(null)), null);
        }
        else {
            return new KeyframeWithQuery(new Vector3f(0, 0, 0), new IFloatSupplier[] { elements[0], elements[1], elements[2] });
        }
    }
    
    public void mul(float multiplier) {
        this.keyframeTarget.mul(multiplier);
        if (query != null) {
            for (IFloatSupplier element : query) {
                element.multiplyNumerics(multiplier);
            }
        }
    }
    
    public void mul(float x, float y, float z) {
        this.keyframeTarget.mul(x, y, z);
        if (query != null) {
            query[0].multiplyNumerics(x);
            query[1].multiplyNumerics(y);
            query[2].multiplyNumerics(z);
        }
    }
    
}
