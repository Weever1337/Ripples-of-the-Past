package com.github.standobyte.jojo.client.model.entity.stand;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.client.model.pose.IModelPose;
import com.github.standobyte.jojo.client.model.pose.ModelPose;
import com.github.standobyte.jojo.client.model.pose.ModelPoseSided;
import com.github.standobyte.jojo.client.model.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.model.pose.ModelPoseTransitionMultiple;
import com.github.standobyte.jojo.client.model.pose.RotationAngle;
import com.github.standobyte.jojo.client.model.pose.StandActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.util.MathUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

// Made with Blockbench 3.9.2


public abstract class HumanoidStandModel<T extends StandEntity> extends StandEntityModel<T> {
    protected ModelRenderer head;
    protected ModelRenderer body;
    protected ModelRenderer upperPart;
    protected ModelRenderer torso;
    protected ModelRenderer leftArm;
    protected ModelRenderer leftArmJoint;
    protected ModelRenderer leftForeArm;
    protected ModelRenderer rightArm;
    protected ModelRenderer rightArmJoint;
    protected ModelRenderer rightForeArm;
    protected ModelRenderer leftLeg;
    protected ModelRenderer leftLegJoint;
    protected ModelRenderer leftLowerLeg;
    protected ModelRenderer rightLeg;
    protected ModelRenderer rightLegJoint;
    protected ModelRenderer rightLowerLeg;
    

    public HumanoidStandModel() {
        this(64, 64);
    }

    public HumanoidStandModel(int textureWidth, int textureHeight) {
        super(true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
        this.texWidth = textureWidth;
        this.texHeight = textureHeight;

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);


        upperPart = new ModelRenderer(this);
        upperPart.setPos(0.0F, 12.0F, 0.0F);
        body.addChild(upperPart);


        torso = new ModelRenderer(this);
        torso.setPos(0.0F, -12.0F, 0.0F);
        upperPart.addChild(torso);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(6.0F, -10.0F, 0.0F);
        upperPart.addChild(leftArm);

        leftArmJoint = new ModelRenderer(this);
        leftArmJoint.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftArmJoint);

        leftForeArm = new ModelRenderer(this);
        leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftForeArm);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-6.0F, -10.0F, 0.0F);
        upperPart.addChild(rightArm);

        rightArmJoint = new ModelRenderer(this);
        rightArmJoint.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightArmJoint);

        rightForeArm = new ModelRenderer(this);
        rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightForeArm);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        body.addChild(leftLeg);

        leftLegJoint = new ModelRenderer(this);
        leftLegJoint.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLegJoint);

        leftLowerLeg = new ModelRenderer(this);
        leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLowerLeg);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        body.addChild(rightLeg);

        rightLegJoint = new ModelRenderer(this);
        rightLegJoint.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLegJoint);

        rightLowerLeg = new ModelRenderer(this);
        rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLowerLeg);
        
        
        baseHumanoidBoxes = ImmutableMap.<ModelRenderer, Consumer<ModelRenderer>>builder()
                .put(head, part ->          part.texOffs(24, 0) .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false))
                .put(torso, part ->         part.texOffs(0, 0)  .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false))
                .put(leftArm, part ->       part.texOffs(16, 44).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(leftArmJoint, part ->  part.texOffs(0, 38) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true))
                .put(leftForeArm, part ->   part.texOffs(16, 54).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .put(rightArm, part ->      part.texOffs(0, 44) .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(rightArmJoint, part -> part.texOffs(0, 38) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false))
                .put(rightForeArm, part ->  part.texOffs(0, 54) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .put(leftLeg, part ->       part.texOffs(48, 44).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(leftLegJoint, part ->  part.texOffs(52, 38).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true))
                .put(leftLowerLeg, part ->  part.texOffs(48, 54).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .put(rightLeg, part ->      part.texOffs(32, 44).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(rightLegJoint, part -> part.texOffs(52, 38).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false))
                .put(rightLowerLeg, part -> part.texOffs(32, 54).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .build();
    }
    
    @Override
    public void afterInit() {
        super.afterInit();
    }

    protected final void addHumanoidBaseBoxes(@Nullable Predicate<ModelRenderer> partPredicate) {
        for (Map.Entry<ModelRenderer, Consumer<ModelRenderer>> entry : baseHumanoidBoxes.entrySet()) {
            if (partPredicate == null || partPredicate.test(entry.getKey())) {
                entry.getValue().accept(entry.getKey());
            }
        }
    }
    
    private final Map<ModelRenderer, Consumer<ModelRenderer>> baseHumanoidBoxes;

    @Override
    protected void updatePartsVisibility(VisibilityMode mode) {
        if (mode == VisibilityMode.ALL) {
            head.visible = true;
            torso.visible = true;
            leftLeg.visible = true;
            rightLeg.visible = true;
            leftArm.visible = true;
            rightArm.visible = true;
        }
        else {
            head.visible = false;
            torso.visible = false;
            leftLeg.visible = false;
            rightLeg.visible = false;
            switch (mode) {
            case ARMS_ONLY:
                leftArm.visible = true;
                rightArm.visible = true;
                break;
            case LEFT_ARM_ONLY:
                leftArm.visible = true;
                rightArm.visible = false;
                break;
            case RIGHT_ARM_ONLY:
                leftArm.visible = false;
                rightArm.visible = true;
                break;
            default:
                break;
            }
        }
    }

    protected ModelPoseSided<T> barrageSwing;
    @Override
    protected void initActionPoses() {
        super.initActionPoses();
        
        
        if (barrageSwing == null) barrageSwing = new ModelPoseSided<>(
                initArmSwingPose(HandSide.LEFT, 1.0F, SwingPart.SWING), 
                initArmSwingPose(HandSide.RIGHT, 1.0F, SwingPart.SWING));
        
        
        
        RotationAngle[] jabRightStart = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -15, 0),
                RotationAngle.fromDegrees(leftArm, -7.5F, 0, -15),
                RotationAngle.fromDegrees(leftForeArm, -100, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, 22.5F, 0, 22.5F),
                RotationAngle.fromDegrees(rightForeArm, -105, 0, -15)
        };
        RotationAngle[] jabRight2 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -22.5F, 0),
                RotationAngle.fromDegrees(leftArm, 30F, 0, -15F),
                RotationAngle.fromDegrees(leftForeArm, -107.5F, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, 5.941F, 8.4211F, 69.059F),
                RotationAngle.fromDegrees(rightForeArm, -75, 0, 0)
        };
        RotationAngle[] jabRightImpact = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -30, 0),
                RotationAngle.fromDegrees(leftArm, 37.5F, 0, -15F),
                RotationAngle.fromDegrees(leftForeArm, -115, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, -81.9244F, 11.0311F, 70.2661F),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, 0)
        };
        RotationAngle[] jabRight4 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -7.5F, 0),
                RotationAngle.fromDegrees(leftArm, 5.63F, 0, -20.62F),
                RotationAngle.fromDegrees(leftForeArm, -103.75F, 3.75F, 13.13F),
                RotationAngle.fromDegrees(rightArm, 5.941F, 8.4211F, 69.059F),
                RotationAngle.fromDegrees(rightForeArm, -75, 0, 0)
        };
        
        // FIXME (!!!!!!!!) jab animation x rotation
        actionAnim.putIfAbsent(StandPose.LIGHT_ATTACK, 
                new StandActionAnimation.Builder<T>()
                
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseSided<T>(
                        new ModelPoseTransitionMultiple.Builder<T>(
                                new ModelPose<T>(mirrorAngles(jabRightStart)))
                        .addPose(0.5F, new ModelPose<T>(mirrorAngles(jabRight2)))
                        .addPose(0.75F, new ModelPose<T>(mirrorAngles(jabRightImpact)).setEasing(x -> x * x * x))
                        .build(new ModelPose<T>(mirrorAngles(jabRightImpact))),
                        
                        new ModelPoseTransitionMultiple.Builder<T>(
                                new ModelPose<T>(jabRightStart))
                        .addPose(0.5F, new ModelPose<T>(jabRight2))
                        .addPose(0.75F, new ModelPose<T>(jabRightImpact).setEasing(x -> x * x * x))
                        .build(new ModelPose<T>(jabRightImpact))
                        ))
                
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseSided<T>(
                        new ModelPoseTransitionMultiple.Builder<T>(
                                new ModelPose<T>(mirrorAngles(jabRightImpact)))
                        .addPose(0.25F, new ModelPose<T>(mirrorAngles(jabRightImpact)))
                        .addPose(0.5F, new ModelPose<T>(mirrorAngles(jabRight4)).setEasing(x -> x * x * x))
                        .build(new ModelPose<T>(jabRightStart)),
                        
                        new ModelPoseTransitionMultiple.Builder<T>(
                                new ModelPose<T>(jabRightImpact))
                        .addPose(0.25F, new ModelPose<T>(jabRightImpact))
                        .addPose(0.5F, new ModelPose<T>(jabRight4).setEasing(x -> x * x * x))
                        .build(new ModelPose<T>(mirrorAngles(jabRightStart)))
                        ))
                
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseSided<T>(
                        new ModelPoseTransitionMultiple.Builder<T>(
                                new ModelPose<T>(jabRightStart))
                        .addPose(0.75F, new ModelPose<T>(jabRightStart))
                        .build(idlePose),

                        new ModelPoseTransitionMultiple.Builder<T>(
                                new ModelPose<T>(mirrorAngles(jabRightStart)))
                        .addPose(0.75F, new ModelPose<T>(mirrorAngles(jabRightStart)))
                        .build(idlePose)
                        ))
                .build(idlePose));

        
        
        float backSwing = 1.75F;
        StandActionAnimation<T> heavyAttackAnim = new StandActionAnimation.Builder<T>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseSided<>(
                        initArmSwingPose(HandSide.LEFT, backSwing, SwingPart.BACKSWING), 
                        initArmSwingPose(HandSide.RIGHT, backSwing, SwingPart.BACKSWING)))
                .addPose(StandEntityAction.Phase.PERFORM, initBarrageSwingAnim(backSwing))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransition<T>(initBarrageSwingAnim(backSwing), idlePose)
                        .setEasing(pr -> Math.max(2F * (pr - 1) + 1, 0F)))
                .build(idlePose);
        actionAnim.putIfAbsent(StandPose.HEAVY_ATTACK, heavyAttackAnim);

        actionAnim.putIfAbsent(StandPose.HEAVY_ATTACK_COMBO, heavyAttackAnim);
        
        
        
        actionAnim.putIfAbsent(StandPose.BLOCK, new StandActionAnimation.Builder<T>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<T>(new RotationAngle[] {
                        new RotationAngle(body, 0, 0, 0),
                        new RotationAngle(upperPart, 0.0F, 0.0F, 0.0F),
                        new RotationAngle(rightForeArm, 0.0F, 0.0F, -1.0472F),
                        new RotationAngle(leftForeArm, 0.0F, 0.0F, 1.0472F)
                }).setAdditionalAnim((rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
                    float blockXRot = MathHelper.clamp(xRotation, -60, 60) * MathUtil.DEG_TO_RAD / 2;
                    rightArm.xRot = -1.5708F + blockXRot;
                    leftArm.xRot = rightArm.xRot;

                    rightArm.yRot = blockXRot / 2;
                    leftArm.yRot = -rightArm.yRot;

                    rightArm.zRot = Math.abs(blockXRot) / 2 - 0.7854F;
                    leftArm.zRot = -rightArm.zRot;
                }))
                .build(idlePose));
    }
    
    protected RotationAngle[] mirrorAngles(RotationAngle[] angles) {
        RotationAngle[] mirrored = new RotationAngle[angles.length];
        for (int i = 0; i < angles.length; i++) {
            RotationAngle angle = angles[i];
            mirrored[i] = new RotationAngle(getOppositeHandside(angle.modelRenderer), angle.angleX, -angle.angleY, -angle.angleZ);
        }
        return mirrored;
    }
    
    protected ModelPoseSided<T> initBarrageSwingAnim(float backSwing) {
        return new ModelPoseSided<>(
                initArmSwingPose(HandSide.LEFT, backSwing, SwingPart.SWING), 
                initArmSwingPose(HandSide.RIGHT, backSwing, SwingPart.SWING));
    }
    
    protected ModelPoseTransition<T> initArmSwingPose(HandSide swingingHand, float backSwingAmount, SwingPart animPart) {
        ModelRenderer punchingArm = getArm(swingingHand);
        ModelRenderer punchingForeArm = getForeArm(swingingHand);
        ModelRenderer otherArm = getArm(swingingHand.getOpposite());
        ModelRenderer otherForeArm = getForeArm(swingingHand.getOpposite());

        float yRotBody = 0.5236F;
        float yRotArm = yRotBody;
        if (swingingHand == HandSide.LEFT) {
            yRotBody *= -1.0F;
        }
        
        ModelPoseTransition<T> anim = null;
        switch (animPart) {
        case BACKSWING:
            anim = new ModelPoseTransition<T>(
                    new ModelPose<T>(new RotationAngle[] {
                            new RotationAngle(body, 0, 0, 0),
                            new RotationAngle(upperPart, 0, yRotBody, 0),
                            new RotationAngle(punchingArm, 0.3927F, 0, 1.0472F),
                            new RotationAngle(punchingForeArm, -2.3562F, 0, 0),
                            new RotationAngle(otherArm, -1.5708F, 0, 1.5708F),
                            new RotationAngle(otherForeArm, 0, 0, 0)
                    }), 
                    new ModelPose<T>(new RotationAngle[] {
                            new RotationAngle(upperPart, 0, yRotBody * backSwingAmount, 0),
                            new RotationAngle(otherArm, -1.5708F + yRotArm * (backSwingAmount - 1), 0, 1.5708F),
                    }).setAdditionalAnim((rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
                        leftArm.zRot *= -1.0F;
                        rightArm.yRot = -xRotation * MathUtil.DEG_TO_RAD;
                        leftArm.yRot = xRotation * MathUtil.DEG_TO_RAD;
                    }));
            break;
        case SWING:
            anim = new ModelPoseTransition<T>(
                    new ModelPose<T>(new RotationAngle[] {
                            new RotationAngle(body, 0, 0, 0),
                            new RotationAngle(upperPart, 0, yRotBody * backSwingAmount, 0),
                            new RotationAngle(punchingArm, 0.3927F, 0, 1.0472F),
                            new RotationAngle(punchingForeArm, -2.3562F, 0, 0),
                            new RotationAngle(otherArm, -1.5708F + yRotArm * (backSwingAmount - 1), 0, 1.5708F),
                            new RotationAngle(otherForeArm, 0, 0, 0)
                    }), 
                    new ModelPose<T>(new RotationAngle[] {
                            new RotationAngle(upperPart, 0, -yRotBody * backSwingAmount, 0),
                            new RotationAngle(punchingArm, -1.5708F + yRotArm * (backSwingAmount - 1), 0, 1.5708F),
                            new RotationAngle(punchingForeArm, 0, 0, 0),
                            new RotationAngle(otherArm, 0.3927F, 0, 1.0472F),
                            new RotationAngle(otherForeArm, -2.3562F, 0, 0)
                    }).setAdditionalAnim((rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
                        leftArm.zRot *= -1.0F;
                        rightArm.yRot = -xRotation * MathUtil.DEG_TO_RAD;
                        leftArm.yRot = xRotation * MathUtil.DEG_TO_RAD;
                    }))
                    .setEasing(sw -> sw * sw * sw);
            break;
        }
        return anim;
    }
    
    protected enum SwingPart {
        BACKSWING,
        SWING
    }

    @Override
    protected void swingArmBarrage(T entity, float swingAmount, float yRotation, float xRotation, float ticks, HandSide swingingHand, float recovery) {
        entity.setYBodyRot(entity.yRot);
        getBarrageSwingAnim(entity).poseModel(swingAmount, entity, ticks, yRotation, xRotation, swingingHand);
    }
    
    protected IModelPose<T> getBarrageSwingAnim(T entity) {
        return barrageSwing;
    }

    protected ModelRenderer getArm(HandSide side) {
        switch (side) {
        case LEFT:
            return leftArm;
        case RIGHT:
            return rightArm;
        }
        return null;
    }

    protected ModelRenderer getForeArm(HandSide side) {
        switch (side) {
        case LEFT:
            return leftForeArm;
        case RIGHT:
            return rightForeArm;
        }
        return null;
    }

    @Override
    protected ModelPose<T> initPoseReset() {
        return new ModelPose<T>(
                new RotationAngle[] {
                        new RotationAngle(body, 0, 0, 0),
                        new RotationAngle(upperPart, 0, 0, 0),
                        new RotationAngle(rightArm, 0, 0, 0),
                        new RotationAngle(rightForeArm, 0, 0, 0),
                        new RotationAngle(leftArm, 0, 0, 0),
                        new RotationAngle(leftForeArm, 0, 0, 0),
                        new RotationAngle(rightLeg, 0, 0, 0),
                        new RotationAngle(rightLowerLeg, 0, 0, 0),
                        new RotationAngle(leftLeg, 0, 0, 0),
                        new RotationAngle(leftLowerLeg, 0, 0, 0)
                });
    }

    @Override
    public void setupAnim(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        rotateJoint(leftArmJoint, leftForeArm);
        rotateJoint(rightArmJoint, rightForeArm);
        rotateJoint(leftLegJoint, leftLowerLeg);
        rotateJoint(rightLegJoint, rightLowerLeg);
    }

    protected void rotateJoint(ModelRenderer joint, ModelRenderer limbPart) {
        if (joint != null) {
            joint.xRot = limbPart.xRot / 2;
            joint.yRot = limbPart.yRot / 2;
            joint.zRot = limbPart.zRot / 2;
        }
    }

    @Override
    protected Iterable<ModelRenderer> headParts() {
        return ImmutableList.of(head);
    }

    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return ImmutableList.of(body);
    }

    @Override
    public ModelRenderer armModel(HandSide side) {
        return side == HandSide.LEFT ? leftArm : rightArm;
    }
    
    @Override
    protected void initOpposites() {
        super.initOpposites();
        oppositeHandside.put(leftArm, rightArm);
        oppositeHandside.put(leftForeArm, rightForeArm);
        oppositeHandside.put(leftLeg, rightLeg);
        oppositeHandside.put(leftLowerLeg, rightLowerLeg);
    }
}