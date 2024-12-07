package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.Optional;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.entity.stand.StandPose;

public class StandPoseData {
    public StandPose standPose;
    public Optional<Phase> actionPhase;
    public float phaseCompletion;
    
    public static StandPoseData poseData(StandPose standPose, Optional<Phase> actionPhase, float phaseCompletion) {
        INSTANCE.standPose = standPose;
        INSTANCE.actionPhase = actionPhase;
        INSTANCE.phaseCompletion = phaseCompletion;
        return INSTANCE;
    }
    
    
    private static final StandPoseData INSTANCE = new StandPoseData();
    private StandPoseData() {}
}
