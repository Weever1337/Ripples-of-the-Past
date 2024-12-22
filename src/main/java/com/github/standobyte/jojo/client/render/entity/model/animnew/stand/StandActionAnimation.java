package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.molang.AnimContext;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import net.minecraft.util.math.MathHelper;

public class StandActionAnimation {
    public static final float ANIM_SPEED = 1;
    public final Animation anim;
    
    @Nullable public AnimObjTimeline<Phase> phasesTimeline;
    @Nullable public Map<String, AnimObjTimeline<String>> stringValTimelines = new HashMap<>();
    @Nullable public Map<String, AnimObjTimeline<Double>> numericValTimelines = new HashMap<>();
    
    public float animTime;
    
    public StandActionAnimation(Animation anim) {
        this.anim = anim;
    }
    
    
    
    public void poseStand(@Nullable StandEntity entity, StandEntityModel<?> model, 
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
        
        AnimContext animContext = AnimContext.fillContext(entity, ticks, yRotOffsetDeg, xRotDeg);
        GeckoStandAnimator.animateSecs(model, anim, animTime, ANIM_SPEED, animContext);
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
        default:
            if (stringValTimelines == null) {
                stringValTimelines = new HashMap<>();
            }
            AnimObjTimeline<String> timeline = stringValTimelines.computeIfAbsent(field, __ -> new AnimObjTimeline<>());
            timeline.add(keyframeTime, value);
            break;
        }
    }
    
    public void onFinishedParsing() {
        if (stringValTimelines != null) {
            Iterator<Map.Entry<String, AnimObjTimeline<String>>> iter = stringValTimelines.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, AnimObjTimeline<String>> entry = iter.next();
                timelineToNumeric(entry.getValue()).ifPresent(numericTimeline -> {
                    if (numericValTimelines == null) {
                        numericValTimelines = new HashMap<>();
                    }
                    numericValTimelines.put(entry.getKey(), numericTimeline);
                    iter.remove();
                });
            }
        }
        
        if (phasesTimeline != null) phasesTimeline.sort();
        if (stringValTimelines != null) stringValTimelines.values().forEach(AnimObjTimeline::sort);
        if (numericValTimelines != null) numericValTimelines.values().forEach(AnimObjTimeline::sort);
    }
    
    private static Optional<AnimObjTimeline<Double>> timelineToNumeric(AnimObjTimeline<String> stringTimeline) {
        AnimObjTimeline<Double> timeline = new AnimObjTimeline<>();
        for (Float2ObjectMap.Entry<String> entry : stringTimeline.getEntries()) {
            try {
                double numericVal = Double.parseDouble(entry.getValue());
                timeline.add(entry.getFloatKey(), numericVal);
            }
            catch (NumberFormatException notNumeric) {
                return Optional.empty();
            }
        }
        return Optional.of(timeline);
    }
    
    @Nullable
    public String getStringTimelineVal(String key, float animTime) {
        if (stringValTimelines == null) {
            return null;
        }
        AnimObjTimeline<String> timeline = stringValTimelines.get(key);
        if (timeline == null) {
            return null;
        }
        return timeline.getCurValue(animTime);
    }
    
    @Nullable
    public Double getNumericTimelineVal(String key, float animTime) {
        if (numericValTimelines == null) {
            return null;
        }
        AnimObjTimeline<Double> timeline = numericValTimelines.get(key);
        if (timeline == null) {
            return null;
        }
        return timeline.getCurValue(animTime);
    }
    
    public static class TimelineKeys {
        public static final String BARRAGE = "barrage";
    }
    
}
