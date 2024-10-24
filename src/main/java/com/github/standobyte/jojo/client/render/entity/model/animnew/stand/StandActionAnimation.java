package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.render.entity.model.animnew.floatquery.FloatQuery;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

public class StandActionAnimation implements IStandAnimator {
    public static final float ANIM_SPEED = 1;
    public final Animation anim;
    
    @Nullable Float2ObjectMap<Phase> phasesTimeline;
    
    public StandActionAnimation(Animation anim) {
        this.anim = anim;
    }
    
    
    
    @Override
    public boolean poseStand(StandEntity entity, StandEntityModel<?> model, float ticks, float yRotOffsetRad, float xRotRad, 
            StandPose standPose, Optional<Phase> actionPhase, float phaseCompletion, HandSide swingingHand) {
        float seconds;
        if (actionPhase.isPresent() && phasesTimeline != null) {
            Phase taskPhase = actionPhase.get();
            
            Float prevPhaseTime = null;
            Float curPhaseTime = null;
            Float2ObjectMap.Entry<Phase> prevAnimPhase = null;
            for (Float2ObjectMap.Entry<Phase> animPhase : phasesTimeline.float2ObjectEntrySet()) {
                if (animPhase.getValue().ordinal() > taskPhase.ordinal()) {
                    prevPhaseTime = prevAnimPhase != null ? prevAnimPhase.getFloatKey() : 0;
                    curPhaseTime = animPhase.getFloatKey();
                    break;
                }
                prevAnimPhase = animPhase;
            }
            if (prevPhaseTime == null) {
                prevPhaseTime = prevAnimPhase.getValue() == taskPhase ? prevAnimPhase.getFloatKey() : anim.lengthInSeconds();
                curPhaseTime = anim.lengthInSeconds();
            }
            
            seconds = MathHelper.lerp(phaseCompletion, prevPhaseTime, curPhaseTime);
        }
        else {
            seconds = anim.looping() ? (ticks / 20.0f) % anim.lengthInSeconds() : ticks / 20.0f;
        }
        
        FloatQuery.AnimContext animContext = FloatQuery.AnimContext.makeContext(entity, ticks, yRotOffsetRad, xRotRad, actionPhase, phaseCompletion);
        GeckoStandAnimator.animateSecs(model, anim, seconds, ANIM_SPEED, animContext);
        
        return true;
    }
    
    
    public void parseAssignmentInstruction(String field, String value, float keyframeTime) {
        switch (field) {
        case "phase":
            Phase phase = Phase.valueOf(value);
            if (phasesTimeline == null) {
                phasesTimeline = new Float2ObjectArrayMap<>();
            }
            phasesTimeline.put(keyframeTime, phase);
            break;
        case "barrage":
            
            break;
        }
    }
    
}
