package com.github.standobyte.jojo.client.render.rendertype;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

public class CustomRenderType extends RenderType {
    
    private CustomRenderType(String name, VertexFormat format, int mode, int bufferSize,
            boolean affectCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectCrumbling, sortOnUpload, setupState, clearState);
    }
    
    public static RenderType hamonProjectileShield(ResourceLocation glintTexture) { // it just works
        RenderType.State renderType$state = RenderType.State.builder()
                .setTextureState(new RenderState.TextureState(glintTexture, true, false))
                .setWriteMaskState(COLOR_WRITE)
                .setFogState(NO_FOG)
                .setCullState(NO_CULL)
                .setTransparencyState(GLINT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setTexturingState(ENTITY_GLINT_TEXTURING)
                .createCompositeState(false);
        return RenderType.create("jojo_proj_shield", DefaultVertexFormats.BLOCK, 7, 256, false, true, renderType$state);
    }
}
