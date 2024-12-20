package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.render.entity.model.animnew.floatquery.AnimContext;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import net.minecraft.util.math.MathHelper;

public class StandActionAnimation {
    public static final float ANIM_SPEED = 1;
    public final Animation anim;
    
    @Nullable public AnimObjTimeline<Phase> phasesTimeline;
    @Nullable public AnimObjTimeline<String> barrageTimeline;
    
    public float animTime;
    
    public StandActionAnimation(Animation anim) {
        this.anim = anim;
    }
    
    
    
    public boolean poseStand(@Nullable StandEntity entity, StandEntityModel<?> model, 
            float ticks, float yRotOffsetDeg, float xRotDeg, StandPoseData poseData) {
        if (poseData.actionPhase.isPresent() && phasesTimeline != null) {
            Phase taskPhase = poseData.actionPhase.get();
            
            Float curPhaseTime = null;
            Float nextPhaseTime = null;
            Float2ObjectMap.Entry<Phase> prevAnimPhase = null;
            for (Float2ObjectMap.Entry<Phase> animPhase : phasesTimeline.getEntries()) {
                if (animPhase.getValue().ordinal() > taskPhase.ordinal()) {
                    curPhaseTime = prevAnimPhase != null ? prevAnimPhase.getFloatKey() : 0;
                    nextPhaseTime = animPhase.getFloatKey();
                    break;
                }
                prevAnimPhase = animPhase;
            }
            if (curPhaseTime == null) {
                curPhaseTime = prevAnimPhase.getValue() == taskPhase ? prevAnimPhase.getFloatKey() : anim.lengthInSeconds();
                nextPhaseTime = anim.lengthInSeconds();
            }
            
            if (poseData.phaseCompletion >= 0) {
                animTime = MathHelper.lerp(poseData.phaseCompletion, curPhaseTime, nextPhaseTime);
            }
            else if (poseData.animTime >= 0) {
                animTime = curPhaseTime + poseData.animTime / 20f;
                if (entity != null && animTime >= anim.lengthInSeconds()) {
                    entity.onSetPoseAnimEnded();
                }
            }
            else {
                animTime = curPhaseTime;
            }
        }
        else {
            animTime = anim.looping() ? (ticks / 20f) % anim.lengthInSeconds() : ticks / 20f;
        }
        
        AnimContext animContext = AnimContext.makeContext(entity, ticks, yRotOffsetDeg, xRotDeg);
        GeckoStandAnimator.animateSecs(model, anim, animTime, ANIM_SPEED, animContext);
        
        return true;
    }
    
    
    public void parseAssignmentInstruction(String field, String value, float keyframeTime) {
        switch (field) {
        case "phase":
            Phase phase = Phase.valueOf(value);
            if (phasesTimeline == null) {
                phasesTimeline = new AnimObjTimeline<>();
            }
            phasesTimeline.add(keyframeTime, phase);
            break;
        case "barrage":
            if (barrageTimeline == null) {
                barrageTimeline = new AnimObjTimeline<>();
            }
            barrageTimeline.add(keyframeTime, value);
            break;
        }
    }
    
    public void onFinishedParsing() {
        if (phasesTimeline != null) {
            phasesTimeline.sort();
        }
        if (barrageTimeline != null) {
            barrageTimeline.sort();
        }
    }
    
}
