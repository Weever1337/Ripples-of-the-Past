package com.github.standobyte.jojo.entity.stand;

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
    
    public static final StandPose IDLE = new StandPose("idle");
    public static final StandPose SUMMON = new StandPose("summon");
    public static final StandPose BLOCK = new StandPose("block");
    public static final StandPose LIGHT_ATTACK = new StandPose("jab");
    public static final StandPose HEAVY_ATTACK = new StandPose("punch");
    public static final StandPose HEAVY_ATTACK_FINISHER = new StandPose("finisher_punch");
    public static final StandPose RANGED_ATTACK = new StandPose("ranged_attack");
    public static final StandPose BARRAGE = new StandPose("barrage");
}
