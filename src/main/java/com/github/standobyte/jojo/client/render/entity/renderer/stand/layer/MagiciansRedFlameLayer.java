package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import java.util.Optional;

import com.github.standobyte.jojo.client.render.entity.model.stand.MagiciansRedFlameLayerModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry.StandModelRegistryObj;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.MagiciansRedRenderer;
import com.github.standobyte.jojo.entity.stand.stands.MagiciansRedEntity;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class MagiciansRedFlameLayer extends StandModelLayerRenderer<MagiciansRedEntity, StandEntityModel<MagiciansRedEntity>> {

    public MagiciansRedFlameLayer(MagiciansRedRenderer entityRenderer, StandModelRegistryObj baseModel) {
        super(entityRenderer, new MagiciansRedFlameLayerModel(), null);
        model.setAnimatorSupplier(baseModel::getDefaultGeckoAnims);
    }
    
    @Override
    public int getPackedLight(int packedLight) {
        return LightTexture.pack(15, 15);
    }

    @Deprecated
    @Override
    public ResourceLocation getLayerTexture(Optional<ResourceLocation> standSkin) {
        return PlayerContainer.BLOCK_ATLAS;
    }

    @Override
    public boolean shouldRender(MagiciansRedEntity entity, Optional<ResourceLocation> standSkin) {
        return entity == null || !entity.isInWaterOrRain();
    }
}
