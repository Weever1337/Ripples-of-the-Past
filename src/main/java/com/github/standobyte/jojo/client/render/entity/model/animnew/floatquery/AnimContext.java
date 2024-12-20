package com.github.standobyte.jojo.client.render.entity.model.animnew.floatquery;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;

public class AnimContext {
    @Nullable public Entity entity;
    public float ticks;
    public float yRotOffsetDeg;
    public float xRotDeg;
    
    public static AnimContext makeContext(Entity entity, float ticks, float yRotOffsetDeg, float xRotDeg) {
        INSTANCE.entity = entity;
        INSTANCE.ticks = ticks;
        INSTANCE.yRotOffsetDeg = yRotOffsetDeg;
        INSTANCE.xRotDeg = xRotDeg;
        return INSTANCE;
    }
    
    public static AnimContext clearContext() {
        return makeContext(null, 0, 0, 0);
    }
    
    
    private static final AnimContext INSTANCE = new AnimContext();
    private AnimContext() {}
}
