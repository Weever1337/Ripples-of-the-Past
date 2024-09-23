package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.render.entity.model.animnew.Interpolations;
import com.github.standobyte.jojo.client.render.entity.model.animnew.ParseGeckoAnims;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Keyframe;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation.Interpolation;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class GeckoStandAnimator implements IStandAnimator {
    public static boolean IS_TESTING_GECKO = false; // if false, the legacy code animator is used as a fallback
    private final Map<String, StandActionAnimation> namedAnimations = new HashMap<>();
    private final List<StandActionAnimation> summonAnims = new ArrayList<>();
    private StandActionAnimation idleAnim;
    
    public GeckoStandAnimator() {}
    
    
    public void putNamedAnim(String name, StandActionAnimation anim) {
        StandActionAnimation prevAnim = namedAnimations.put(name, anim);
        if (name.startsWith(StandPose.SUMMON.getName())) {
            if (prevAnim != null) {
                summonAnims.remove(prevAnim);
            }
            summonAnims.add(anim);
        }
        else if ("idle".equals(name)) {
            idleAnim = anim;
        }
    }

    public StandActionAnimation getNamedAnim(String name) {
        return namedAnimations.get(name);
    }
    
    public void onLoad() {
        
    }
    
    @Override
    public boolean poseStand(StandEntity entity, StandEntityModel<?> model, float ticks, float yRotOffsetRad, float xRotRad, 
            StandPose standPose, Optional<Phase> actionPhase, float phaseCompletion, HandSide swingingHand) {
        if (standPose == StandPose.SUMMON && summonAnims.size() > 0) {
            StandActionAnimation summonAnim = summonAnims.get(entity.getSummonPoseRandomByte() % summonAnims.size());

            if (ticks > summonAnim.anim.lengthInSeconds() * 20) {
                standPose = StandPose.IDLE;
                model.setStandPose(standPose, entity);
            }

            model.idleLoopTickStamp = ticks;
            return summonAnim.poseStand(entity, model, ticks, yRotOffsetRad, xRotRad, 
                    standPose, actionPhase, phaseCompletion, swingingHand);
        }
        
        if (standPose != null && standPose != StandPose.IDLE) {
            model.idleLoopTickStamp = ticks;
            
            if (namedAnimations.containsKey(standPose.getName())) {
                StandActionAnimation anim = namedAnimations.get(standPose.getName());
                if (anim != null) {
                    return anim.poseStand(entity, model, ticks, yRotOffsetRad, xRotRad, 
                            standPose, actionPhase, phaseCompletion, swingingHand);
                }
            }
            return false;
        }
        
        IStandAnimator idleAnim = getIdleAnim(entity);
        if (idleAnim != null) {
            return idleAnim.poseStand(entity, model, ticks, yRotOffsetRad, xRotRad, 
                    standPose, actionPhase, phaseCompletion, swingingHand);
        }
        
        return false;
    }
    
    public IStandAnimator getIdleAnim(@Nullable StandEntity entity) {
        return idleAnim;
    }
    
    
    
    public static Vector3f lerpKeyframes(Keyframe[] keyframes, float seconds, float animSpeed) {
        int i = Math.max(0, MathHelper.binarySearch(0, keyframes.length, index -> seconds <= keyframes[index].timestamp()) - 1);
        int j = Math.min(keyframes.length - 1, i + 1);
        Keyframe keyframe = keyframes[i];
        Keyframe keyframe2 = keyframes[j];
        float h = seconds - keyframe.timestamp();
        float k = j != i ? MathHelper.clamp(h / (keyframe2.timestamp() - keyframe.timestamp()), 0.0f, 1.0f) : 0.0f;
        keyframe2.interpolation().apply(TEMP, k, keyframes, i, j, animSpeed);
        return TEMP;
    }
    
    private static final Vector3f TEMP = new Vector3f();
    public static void animate(StandEntityModel<?> model, Animation animation, float ticks, float animSpeed) {
        float seconds = animation.looping() ? (ticks / 20.0f) % animation.lengthInSeconds() : ticks / 20.0f;
        animateSecs(model, animation, seconds, animSpeed);
    }
    
    public static void animateSecs(StandEntityModel<?> model, Animation animation, float seconds, float animSpeed) {
        for (Map.Entry<String, List<Transformation>> entry : animation.boneAnimations().entrySet()) {
            ModelRenderer modelPart = model.getModelPart(entry.getKey());
            if (modelPart != null) {
                List<Transformation> transformations = entry.getValue();
                for (Transformation tf : transformations) {
                    Keyframe[] keyframes = tf.keyframes();
                    lerpKeyframes(keyframes, seconds, animSpeed);
                    tf.target().apply(modelPart, TEMP);
                }
            }
        }
    }
    
    
    public void animFromJson(Animation parsedAnim, JsonObject animJson, String name) {
        StandActionAnimation standAnim = new StandActionAnimation(parsedAnim);
        
        JsonObject instructionsJson = animJson.getAsJsonObject("timeline");
        if (instructionsJson != null) {
            Float2ObjectMap<Keyframe> headRotTimeline = new Float2ObjectArrayMap<>();
            Float2ObjectMap<Phase> phasesTimeline = new Float2ObjectArrayMap<>();
            
            for (Map.Entry<String, JsonElement> keyframeEntry : instructionsJson.entrySet()) {
                float time = Float.parseFloat(keyframeEntry.getKey());
                JsonElement value = keyframeEntry.getValue();
                Iterable<JsonElement> instructions = value.isJsonArray() ? value.getAsJsonArray() : Collections.singleton(value);
                for (JsonElement instrJson : instructions) {
                    if (JSONUtils.isStringValue(instrJson)) {
                        String instr = instrJson.getAsString();
                        
                        String[] assignment = instr.split("[ ]*=[ ]*");
                        if (assignment.length == 2) {
                            if (assignment[1].endsWith(";")) assignment[1] = assignment[1].substring(0, assignment[1].length() - 1);
                            switch (assignment[0]) {
                            case "headRot":
                                float headRotVal = Float.parseFloat(assignment[1]);
                                Interpolation lerp = Interpolations.LINEAR;
                                headRotTimeline.put(time, new Keyframe(time, new Vector3f(headRotVal, 0, 0), lerp));
                                break;
                            case "phase":
                                Phase phase = Phase.valueOf(assignment[1]);
                                phasesTimeline.put(time, phase);
                                break;
                            }
                        }
                    }
                }
            }
            
            if (!headRotTimeline.isEmpty()) {
                standAnim.headRot = ParseGeckoAnims.keyframesToArray(headRotTimeline, Keyframe[]::new);
            }
            if (!phasesTimeline.isEmpty()) {
                standAnim.phasesTimeline = phasesTimeline;
            }
        }
        
        putNamedAnim(name, standAnim);
    }
    
}
