package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.pillarman.ModPillarmanActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class PillarmanUnnaturalAgility extends PillarmanAction {

    public PillarmanUnnaturalAgility(PillarmanAction.Builder builder) {
        super(builder.holdType());
        stage = 2;
    }

//    @Override
//    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
//        
//    }
    
    private static boolean canSeeStands(LivingEntity user) {
        return IStandPower.getStandPowerOptional(user).map(power -> {
            if (power.hasPower() || user.hasEffect(ModStatusEffects.SPIRIT_VISION.get())) {
                return true;
            }
            return false;
        }).orElse(false);
    }
    
    public static boolean onUserAttacked(LivingAttackEvent event) {
        Entity attacker = event.getSource().getDirectEntity();
        DamageSource source = event.getSource();
        if ((attacker instanceof LivingEntity && !attacker.isOnFire() && !DamageUtil.isImmuneToCold(attacker)) 
        		|| attacker instanceof StandEntity || attacker instanceof ProjectileEntity || source.isExplosion()) {
            LivingEntity targetLiving = event.getEntityLiving();
            return INonStandPower.getNonStandPowerOptional(targetLiving).map(power -> {
                if (power.getHeldAction(true) == ModPillarmanActions.PILLARMAN_UNNATURAL_AGILITY.get() 
                		|| power.getHeldAction(true) == ModPillarmanActions.PILLARMAN_EVASION.get()) {
                    World world = attacker.level;
                    if (attacker instanceof ModdedProjectileEntity) {
                		if (!(((ModdedProjectileEntity) attacker).canBeEvaded(attacker)) 
                				|| ((ModdedProjectileEntity) attacker).standDamage() && !canSeeStands(targetLiving)) {
                			return false;
                		}
                		return true;
                	}
                    if (attacker instanceof StandEntity && !canSeeStands(targetLiving) || source.isExplosion()) {
                    	return false;
                    }
                    if (power.getHeldAction(true) == ModPillarmanActions.PILLARMAN_UNNATURAL_AGILITY.get() 
                    		&& attacker instanceof LivingEntity && !(attacker instanceof StandEntity)) {
                    	double counterAttack = Math.random();
                    	if(counterAttack > 0.7D) {
                    		attacker.hurt(EntityDamageSource.playerAttack((PlayerEntity) targetLiving), 
	                            (DamageUtil.getDamageWithoutHeldItem(targetLiving) * 0.75F));
                    	}
                    }
                    world.playSound(null, attacker, ModSounds.HAMON_SYO_SWING.get(), attacker.getSoundSource(), 1.0F, 1.0F); // TODO separate sound event
                    return true;
                }
                return false;
            }).orElse(false);
        }
        return false;
    }

    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (stateRefreshed && requirementsFulfilled) {
            ClientTickingSoundsHelper.playHeldActionSound(ModSounds.HAMON_SYO_SWING.get(), 1.0F, 1.25F, true, user, power, this); // TODO separate sound event
        }
    }
    
    @Override
    public boolean clHeldStartAnim(PlayerEntity user) {
        return ModPlayerAnimations.unnaturalAgility.setAnimEnabled(user, true);
    }
    
    @Override
    public void clHeldStopAnim(PlayerEntity user) {
        ModPlayerAnimations.unnaturalAgility.setAnimEnabled(user, false);
    }
}
