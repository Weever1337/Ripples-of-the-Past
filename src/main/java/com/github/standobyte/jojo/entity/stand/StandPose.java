package com.github.standobyte.jojo.entity.stand;

import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.action.stand.StandEntityLightAttack;
import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.StandActionAnimation;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;

import net.minecraft.util.HandSide;

public class StandPose {
    private final String name;
    public final boolean armsObstructView;
    
    public StandPose(String name, boolean armsObstructView) {
        this.name = name.toLowerCase();
        this.armsObstructView = armsObstructView;
    }
    
    public StandPose(String name) {
        this(name, false);
    }
    
    public String getName() {
        return name;
    }
    
    public StandActionAnimation getAnim(List<StandActionAnimation> variants, StandEntity standEntity) {
        return variants.get(0);
    }
    
    public <T extends StandEntity> boolean applyAnim(T entity, StandEntityModel<T> model, StandActionAnimation anim, 
            float ticks, float yRotOffsetRad, float xRotRad, 
            Optional<Phase> actionPhase, float phaseCompletion, HandSide swingingHand) {
        return anim.poseStand(entity, model, ticks, yRotOffsetRad, xRotRad, 
                this, actionPhase, phaseCompletion, swingingHand);
    }
    
    public static final StandPose IDLE = new StandPose("idle");
    public static final StandPose SUMMON = new StandPose("summon") {
        @Override
        public StandActionAnimation getAnim(List<StandActionAnimation> variants, StandEntity standEntity) {
            return variants.get(standEntity.getSummonPoseRandomByte() % variants.size());
        }
    };
    public static final StandPose BLOCK = new StandPose("block");
    public static final StandPose LIGHT_ATTACK = StandEntityLightAttack.STAND_POSE;
    public static final StandPose HEAVY_ATTACK = new StandPose("heavy_punch");
    @Deprecated public static final StandPose HEAVY_ATTACK_FINISHER = new StandPose("finisher_punch");
    @Deprecated public static final StandPose RANGED_ATTACK = new StandPose("ranged_attack");
    public static final StandPose BARRAGE = new StandPose("barrage");
}
