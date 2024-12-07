package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.render.entity.model.animnew.BarrageSwings;
import com.github.standobyte.jojo.client.render.entity.model.animnew.BarrageSwings.BarrageSwing;
import com.github.standobyte.jojo.client.render.entity.model.animnew.floatquery.AnimContext;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Keyframe;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class GeckoStandAnimator implements IStandAnimator {
    public static boolean IS_TESTING_GECKO = false; // if false, the legacy code animator is used as a fallback
    protected final Map<String, List<StandActionAnimation>> namedAnimations = new HashMap<>();
    protected StandActionAnimation idleAnim;
    @Nullable protected StandActionAnimation curAnim;
    
    public GeckoStandAnimator() {}
    
    
    public void putNamedAnim(String name, StandActionAnimation anim) {
        name = name.replaceAll("\\d*$", ""); // removes digits at the end
        namedAnimations.computeIfAbsent(name, __ -> new ArrayList<>()).add(anim);
        if (StandPose.IDLE.getName().equals(name)) {
            idleAnim = anim;
        }
    }

//    public StandActionAnimation getNamedAnim(String name) {
//        return namedAnimations.get(name);
//    }
    
    public void onLoad() {
        
    }
    
    @Override
    public <T extends StandEntity> boolean poseStand(T entity, StandEntityModel<T> model, float ticks, float yRotOffsetRad, float xRotRad, 
            StandPose standPose, Optional<Phase> actionPhase, float phaseCompletion) {
        model.resetPose(entity);
        curAnim = null;
        if (standPose == StandPose.SUMMON) {
            List<StandActionAnimation> summonAnims = namedAnimations.get(StandPose.SUMMON.getName());
            if (summonAnims != null && summonAnims.size() > 0) {
                StandActionAnimation summonAnim = StandPose.SUMMON.getAnim(summonAnims, entity);
                
                if (ticks > summonAnim.anim.lengthInSeconds() * 20) {
                    standPose = StandPose.IDLE;
                    model.setStandPose(standPose, entity);
                }
                
                model.idleLoopTickStamp = ticks;
                return applyAnim(summonAnim, entity, model, ticks, yRotOffsetRad, xRotRad, 
                        standPose, actionPhase, phaseCompletion);
            }
        }
        
        if (standPose != null && standPose != StandPose.IDLE) {
            model.idleLoopTickStamp = ticks;
            
            List<StandActionAnimation> anims = getAnims(entity, standPose);
            if (anims != null) {
                StandActionAnimation anim = standPose.getAnim(anims, entity);
                if (anim != null) {
                    return applyAnim(anim, entity, model, ticks, yRotOffsetRad, xRotRad, 
                            standPose, actionPhase, phaseCompletion);
                }
            }
        }
        
        StandActionAnimation idleAnim = getIdleAnim(entity);
        if (idleAnim != null) {
            return applyAnim(idleAnim, entity, model, ticks, yRotOffsetRad, xRotRad, 
                    standPose, actionPhase, phaseCompletion);
        }
        
        return false;
    }
    
    protected <T extends StandEntity> boolean applyAnim(StandActionAnimation anim, 
            T entity, StandEntityModel<T> model, float ticks, float yRotOffsetRad, float xRotRad, 
            StandPose standPose, Optional<Phase> actionPhase, float phaseCompletion) {
        curAnim = anim;
        return standPose.applyAnim(entity, model, anim, ticks, yRotOffsetRad, xRotRad, 
                actionPhase, phaseCompletion);
        
    }
    
    public StandActionAnimation getIdleAnim(@Nullable StandEntity entity) {
        return idleAnim;
    }
    
    protected List<StandActionAnimation> getAnims(StandEntity entity, StandPose standPose) {
        String key = standPose.getName();
        if (entity.isArmsOnlyMode()) {
            String key2 = "armsOnly_" + key;
            if (namedAnimations.containsKey(key2)) {
                return namedAnimations.get(key2);
            }
        }
        return namedAnimations.get(key);
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
    
    protected static final Vector3f TEMP = new Vector3f();
    public static void animate(StandEntityModel<?> model, Animation animation, float ticks, float animSpeed, AnimContext animContext) {
        float seconds = animation.looping() ? (ticks / 20.0f) % animation.lengthInSeconds() : ticks / 20.0f;
        animateSecs(model, animation, seconds, animSpeed, animContext);
    }
    
    public static void animateSecs(StandEntityModel<?> model, Animation animation, float seconds, float animSpeed, AnimContext animContext) {
        for (Map.Entry<String, List<Transformation>> entry : animation.boneAnimations().entrySet()) {
            ModelRenderer modelPart = model.getModelPart(entry.getKey());
            if (modelPart != null) {
                List<Transformation> transformations = entry.getValue();
                for (Transformation tf : transformations) {
                    Keyframe[] keyframes = tf.keyframes(animContext);
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
                            standAnim.parseAssignmentInstruction(assignment[0], assignment[1], time);
                        }
                    }
                }
            }
        }
        
        standAnim.onFinishedParsing();
        putNamedAnim(name, standAnim);
    }


    protected String barrageType;
    @Override
    public <T extends StandEntity> void addBarrageSwings(T entity, StandEntityModel<T> model, float ticks) {
        if (curAnim != null && curAnim.barrageTimeline != null) {
            barrageType = curAnim.barrageTimeline.getCurValue(curAnim.lastPoseTime);
            if (barrageType != null) {
                BarrageSwings.onBarrageAnim(barrageType, entity, model, curAnim, ticks);
            }
        }
    }

    @Override
    public <T extends StandEntity> void renderBarrageSwings(T entity, StandEntityModel<T> model, float yRotOffsetRad, float xRotRad,
            MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green,
            float blue, float alpha) {
        BarrageSwings swings = entity.getBarrageSwings();
        if (swings != null) {
            for (BarrageSwing swing : swings.getSwings()) {
                swing.poseAndRender(entity, model, 
                        matrixStack, buffer, yRotOffsetRad, xRotRad, 
                        packedLight, packedOverlay, red, green, blue, alpha);
            }
        }
    }
    
}
