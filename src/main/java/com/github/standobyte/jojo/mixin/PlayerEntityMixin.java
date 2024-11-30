package com.github.standobyte.jojo.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.action.non_stand.HamonWallClimbing2;
import com.github.standobyte.jojo.capability.entity.player.PlayerMixinExtension;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrPossessEntityPacket;
import com.github.standobyte.jojo.util.mc.EntityOwnerResolver;
import com.github.standobyte.jojo.util.mod.IPlayerLeap;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin implements PlayerMixinExtension, IPlayerLeap, IPlayerPossess {
    
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void jojoMixinTick(CallbackInfo ci) {
        leapFlagTick();
        jojoTickEntityPossession();
    }
    
    @Override
    public void jojoPlayerUndeadCreature(CallbackInfoReturnable<CreatureAttribute> ci) {
        if (JojoModUtil.playerUndeadAttribute((LivingEntity) (Object) this)) {
            ci.setReturnValue(CreatureAttribute.UNDEAD);
        }
    }
    
    @Override
    public boolean _isEntityOnGround() {
        return isOnGround();
    }
    
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void jojoPlayerWallClimb(Vector3d pTravelVector, CallbackInfo ci) {
        PlayerEntity thisPlayer = (PlayerEntity) (Object) this;
        if (HamonWallClimbing2.travelWallClimb(thisPlayer, pTravelVector)) {
            ci.cancel();
        }
    }
    

    private boolean isDoingLeap;
    @Override
    public void setIsDoingLeap(boolean isDoingLeap) {
        this.isDoingLeap = isDoingLeap;
    }
    
    @Override
    public boolean isDoingLeap() {
        return isDoingLeap;
    }
    
//    private boolean isDoingDash = false;
    
    @Inject(method = "isStayingOnGroundSurface", at = @At("HEAD"), cancellable = true)
    public void jojoBackOffFromEdgeFlag(CallbackInfoReturnable<Boolean> ci) {
        if (isDoingLeap) {
            ci.setReturnValue(false);
        }
//        else if (isDoingDash) {
//            ci.setReturnValue(true);
//        }
    }
    
    
    
    private final EntityOwnerResolver jojoPossessedEntity = new EntityOwnerResolver();
    private GameType jojoPossessPrevGameMode;
    private boolean jojoPossessingAsAlive;
    
    /* TODO specific interactions when possessing someone with asAlive flag:
     *   render hp/hunger/etc.
     *   tick the player
     *     tick status effects
     *   allow the power HUD to be opened
     *   ...?
     */
    @Override
    public void jojoPossessEntity(@Nullable Entity entity, boolean asAlive) {
        jojoPossessedEntity.setOwner(entity);
        if (!level.isClientSide()) {
            ServerPlayerEntity player = ((ServerPlayerEntity) (Entity) this);
            if (entity != null) {
                jojoPossessPrevGameMode = player.gameMode.getGameModeForPlayer();
                player.setGameMode(GameType.SPECTATOR);
                player.setCamera(entity);
            }
            else {
                if (jojoPossessPrevGameMode != null) {
                    player.setGameMode(jojoPossessPrevGameMode);
                }
                player.setCamera(player);
            }
            PacketManager.sendToClientsTrackingAndSelf(new TrPossessEntityPacket(
                    this.getId(), jojoPossessedEntity.getNetworkId(), jojoPossessingAsAlive, 
                    jojoPossessPrevGameMode), this);
        }
        this.jojoPossessingAsAlive = asAlive;
    }
    
    private void jojoTickEntityPossession() {
        if (!level.isClientSide() && jojoPossessedEntity.hasEntityId()) {
            Entity possessed = jojoGetPossessedEntity();
            if (possessed == null || !possessed.isAlive() || !this.isAlive()) {
                jojoPossessEntity(null, jojoPossessingAsAlive);
            }
        }
    }
    
    @Override
    @Nullable public Entity jojoGetPossessedEntity() {
        return jojoPossessedEntity.getEntity(level);
    }
    
    @Override
    public boolean jojoIsPossessingAsAlive() {
        return jojoPossessingAsAlive;
    }
    
    @Override
    @Nullable public GameType jojoGetPrePossessGameMode() {
        return jojoGetPossessedEntity() != null ? jojoPossessPrevGameMode : null;
    }
    
    @Override
    public void jojoSetPrePossessGameMode(GameType gameMode) {
        this.jojoPossessPrevGameMode = gameMode;
        if (!level.isClientSide()) {
            PacketManager.sendToClient(new TrPossessEntityPacket(this.getId(), 
                    jojoPossessedEntity.getNetworkId(), jojoPossessingAsAlive, null), ((ServerPlayerEntity) (Entity) this));
        }
    }
    
    @Override
    public void jojoOnPossessingDead() {
        if (!level.isClientSide() && getType() == EntityType.PLAYER) {
            Entity possessedEntity = jojoGetPossessedEntity();
            if (possessedEntity != null) {
                jojoPossessEntity(null, false);
            }
        }
    }
    
    
    @Override
    public void toNBT(CompoundNBT forgeCapNbt) {
//        jojoPossessedEntity.saveNbt(forgeCapNbt, "Possessed");
//        if (jojoPossessPrevGameMode != null) forgeCapNbt.putString("PossessPrevMode", jojoPossessPrevGameMode.name());
//        forgeCapNbt.putBoolean("PossessAsAlive", jojoPossessingAsAlive);
    }
    
    @Override
    public void fromNBT(CompoundNBT forgeCapNbt) {
//        jojoPossessedEntity.loadNbt(forgeCapNbt, "Possessed");
//        jojoPossessPrevGameMode = GameType.byName(forgeCapNbt.getString("PossessPrevMode"), null);
//        jojoPossessingAsAlive = forgeCapNbt.getBoolean("PossessAsAlive");
    }

    @Override
    public void syncToClient(ServerPlayerEntity thisAsPlayer) {
        PacketManager.sendToClient(new TrPossessEntityPacket(this.getId(), 
                jojoPossessedEntity.getNetworkId(), jojoPossessingAsAlive, jojoPossessPrevGameMode), thisAsPlayer);
    }

    @Override
    public void syncToTracking(ServerPlayerEntity tracking) {
        PacketManager.sendToClient(new TrPossessEntityPacket(this.getId(), 
                jojoPossessedEntity.getNetworkId(), jojoPossessingAsAlive, null), tracking);
    }
}
