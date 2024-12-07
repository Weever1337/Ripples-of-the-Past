package com.github.standobyte.jojo.client.render.entity.model.animnew.floatquery;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;

public class AnimContext {
    @Nullable public Entity entity;
    public float ticks;
    public float yRotOffsetRad;
    public float xRotRad;
    
    public static AnimContext makeContext(Entity entity, float ticks, float yRotOffsetRad, float xRotRad) {
        INSTANCE.entity = entity;
        INSTANCE.ticks = ticks;
        INSTANCE.yRotOffsetRad = yRotOffsetRad;
        INSTANCE.xRotRad = xRotRad;
        return INSTANCE;
    }
    
    public static AnimContext clearContext() {
        return makeContext(null, 0, 0, 0);
    }
    
    
    private static final AnimContext INSTANCE = new AnimContext();
    private AnimContext() {}
}
