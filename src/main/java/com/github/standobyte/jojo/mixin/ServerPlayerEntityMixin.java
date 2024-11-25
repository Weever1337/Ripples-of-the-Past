package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.util.mod.IPlayerPossess;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile) {
        super(pLevel, pPos, pYRot, pGameProfile);
    }
    
    @Inject(method = "doTick", at = @At("HEAD"), cancellable = true)
    public void jojoTsCancelPlayerTick(CallbackInfo ci) {
        if (!this.canUpdate()) {
            ci.cancel();
        }
    }
    
    
    @Inject(method = "setCamera", at = @At("HEAD"), cancellable = true)
    public void jojoCancelEntitySpectate(Entity entityToSpectate, CallbackInfo ci) {
        if (this instanceof IPlayerPossess) {
            Entity possessedEntity = IPlayerPossess.getPossessedEntity(this);
            if (possessedEntity != null && possessedEntity != entityToSpectate) {
                ci.cancel();
            }
        }
    }

}
