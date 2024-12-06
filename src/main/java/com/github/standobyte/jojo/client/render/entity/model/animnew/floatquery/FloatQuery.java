package com.github.standobyte.jojo.client.render.entity.model.animnew.floatquery;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;

public class FloatQuery implements IFloatSupplier {
    private final QueryType queryType;
    private float value;
    
    FloatQuery(float value) {
        this.queryType = QueryType.NUMERIC_LITERAL;
        this.value = value;
    }
    
    FloatQuery(QueryType type) {
        this.queryType = type;
        this.value = 0;
    }
    
    @Override
    public boolean isNumericLiteral() {
        return queryType == QueryType.NUMERIC_LITERAL;
    }
    
    @Override
    public void multiplyNumerics(float multiplier) {
        if (isNumericLiteral()) {
            this.value *= multiplier;
        }
    }
    
    
    
    enum QueryType {
        NUMERIC_LITERAL,
        HEAD_X_ROTATION,
        HEAD_Y_ROTATION;

        static QueryType fromName(String string) {
            switch (string) {
            case "query.head_x_rotation":
                return QueryType.HEAD_X_ROTATION;
            case "query.head_y_rotation":
                return QueryType.HEAD_Y_ROTATION;
            }
            return null;
        }
    }
    
    @Override
    public float get(FloatQuery.AnimContext animContext) {
        switch (queryType) {
        case NUMERIC_LITERAL:
            return value;
        case HEAD_X_ROTATION:
            return animContext.xRotRad;
        case HEAD_Y_ROTATION:
            return animContext.yRotOffsetRad;
        }
        throw new AssertionError();
    }
    
    public static class AnimContext {
        @Nullable private Entity entity;
        private float ticks;
        private float yRotOffsetRad;
        private float xRotRad;
        
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
}
