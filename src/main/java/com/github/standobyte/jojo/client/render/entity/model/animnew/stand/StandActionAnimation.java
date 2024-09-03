package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.render.entity.model.animnew.Interpolations;
import com.github.standobyte.jojo.client.render.entity.model.animnew.ParseGeckoAnims;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Keyframe;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation.Interpolation;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import net.minecraft.util.HandSide;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.vector.Vector3f;

public class StandActionAnimation implements IStandAnimator {
    public static final float ANIM_SPEED = 1;
    public final Animation anim;
    @Nullable private Keyframe[] headRot;
    
    public StandActionAnimation(Animation anim) {
        this.anim = anim;
    }
    
    public static StandActionAnimation fromJson(Animation parsedAnim, JsonObject animJson) {
        StandActionAnimation standAnim = new StandActionAnimation(parsedAnim);

        JsonObject instructionsJson = animJson.getAsJsonObject("timeline");
        if (instructionsJson != null) {
            Float2ObjectMap<Keyframe> headRotTimeline = new Float2ObjectArrayMap<>();
            
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
//                            case "phase":
//                                break;
                            }
                        }
                    }
                }
            }
            
            if (!headRotTimeline.isEmpty()) {
                standAnim.headRot = ParseGeckoAnims.keyframesToArray(headRotTimeline, Keyframe[]::new);
            }
            
        }
        
        return standAnim;
    }
    
    
    
    @Override
    public boolean poseStand(StandEntity entity, StandEntityModel<?> model, float ticks, float yRotOffsetRad, float xRotRad, 
            StandPose standPose, Optional<Phase> actionPhase, float phaseCompletion, HandSide swingingHand) {
        GeckoStandAnimator.animate(model, anim, ticks, ANIM_SPEED);
        float seconds = GeckoStandAnimator.ticksToSecs(anim, ticks);
        if (headRot != null) {
            float headRotAmount = GeckoStandAnimator.lerpKeyframes(headRot, seconds, ANIM_SPEED).x();
            model.headPartsPublic().forEach(part -> {
                part.yRot = MathUtil.rotLerpRad(headRotAmount, part.yRot, yRotOffsetRad);
                part.xRot = MathUtil.rotLerpRad(headRotAmount, part.xRot, xRotRad);
                part.zRot = 0;
            });
        }
        return true;
    }
    
}
