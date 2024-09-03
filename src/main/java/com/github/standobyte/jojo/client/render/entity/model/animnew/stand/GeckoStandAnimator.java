package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Keyframe;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class GeckoStandAnimator implements IStandAnimator {
    private final Map<String, Animation> namedAnimations = new HashMap<>();
    private final List<Animation> summonAnims = new ArrayList<>();
    private Animation idleAnim;
    
    public GeckoStandAnimator() {}
    
    
    public void putNamedAnim(String name, Animation anim) {
        Animation prevAnim = namedAnimations.put(name, anim);
        if (name.startsWith("summon")) {
            if (prevAnim != null) {
                summonAnims.remove(prevAnim);
            }
            summonAnims.add(anim);
        }
        else if ("idle".equals(name)) {
            idleAnim = anim;
        }
    }

    public Animation getNamedAnim(String name) {
        return namedAnimations.get(name);
    }
    
    public void onLoad() {
        
    }
    
    @Override
    public boolean poseStand(StandEntity entity, StandEntityModel<?> model, float ticks, float yRotOffsetRad, float xRotRad, 
            StandPose standPose, Optional<Phase> actionPhase, float phaseCompletion, HandSide swingingHand) {
        if (standPose == StandPose.SUMMON && summonAnims.size() > 0) {
            Animation summonAnim = summonAnims.get(entity.getSummonPoseRandomByte() % summonAnims.size());

            if (ticks > summonAnim.lengthInSeconds() * 20) {
                standPose = StandPose.IDLE;
                model.setStandPose(standPose, entity);
            }

            model.idleLoopTickStamp = ticks;
            animate(model, summonAnim, ticks, 1);
            return true;
        }
        
        if (standPose != null && standPose != StandPose.IDLE && namedAnimations.containsKey(standPose.getName())) {
            model.idleLoopTickStamp = ticks;
            
            Animation anim = namedAnimations.get(standPose.getName());
            if (anim != null) {
                animate(model, anim, ticks, 1);
                return true;
            }
        }
        
        if (idleAnim != null) {
            animate(model, idleAnim, ticks, 1);
            return true;
        }
        
        return false;
    }
    
    
    private static final Vector3f TEMP = new Vector3f();
    public static void animate(StandEntityModel<?> model, Animation animation, float ticks, float scale) {
        float seconds = animation.looping() ? (ticks / 20.0f) % animation.lengthInSeconds() : ticks / 20.0f;
        for (Map.Entry<String, List<Transformation>> entry : animation.boneAnimations().entrySet()) {
            ModelRenderer modelPart = model.getModelPart(entry.getKey());
            if (modelPart != null) {
                List<Transformation> transformations = entry.getValue();
                for (Transformation tf : transformations) {
                    Keyframe[] keyframes = tf.keyframes();
                    int i = Math.max(0, MathHelper.binarySearch(0, keyframes.length, index -> seconds <= keyframes[index].timestamp()) - 1);
                    int j = Math.min(keyframes.length - 1, i + 1);
                    Keyframe keyframe = keyframes[i];
                    Keyframe keyframe2 = keyframes[j];
                    float h = seconds - keyframe.timestamp();
                    float k = j != i ? MathHelper.clamp(h / (keyframe2.timestamp() - keyframe.timestamp()), 0.0f, 1.0f) : 0.0f;
                    keyframe2.interpolation().apply(TEMP, k, keyframes, i, j, scale);
                    tf.target().apply(modelPart, TEMP);
                }
            }
        }
    }
}
