package com.github.standobyte.jojo.entity.stand;

import java.util.List;

import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.StandActionAnimation;

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
    
    public static final StandPose IDLE = new StandPose("idle");
    public static final StandPose SUMMON = new StandPose("summon") {
        @Override
        public StandActionAnimation getAnim(List<StandActionAnimation> variants, StandEntity standEntity) {
            return variants.get(standEntity.getSummonPoseRandomByte() % variants.size());
        }
    };
    public static final StandPose BLOCK = new StandPose("block");
    public static final StandPose LIGHT_ATTACK = new StandPose("jab") {
        @Override
        public StandActionAnimation getAnim(List<StandActionAnimation> variants, StandEntity standEntity) {
            return super.getAnim(variants, standEntity); // FIXME string jab anims one after another (string as in the punches, not the class)
        }
    };
    public static final StandPose HEAVY_ATTACK = new StandPose("heavy_punch");
    @Deprecated public static final StandPose HEAVY_ATTACK_FINISHER = new StandPose("finisher_punch");
    @Deprecated public static final StandPose RANGED_ATTACK = new StandPose("ranged_attack");
    public static final StandPose BARRAGE = new StandPose("barrage");
}
