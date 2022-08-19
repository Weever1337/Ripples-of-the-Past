package com.github.standobyte.jojo.client.ui.hud.marker;

import java.util.List;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockCheckpointMake;
import com.github.standobyte.jojo.client.resources.ActionSpriteUploader;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.IPower.ActionType;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

public class CrazyDiamondAnchorMarker extends MarkerRenderer {
    
    public CrazyDiamondAnchorMarker(Minecraft mc) {
        super(ModStandTypes.CRAZY_DIAMOND.get().getColor(), ActionSpriteUploader.getIcon(ModActions.CRAZY_DIAMOND_BLOCK_ANCHOR_MOVE.get()), mc);
    }
    
    @Override
    protected boolean shouldRender() {
        Action<?> selectedAbility = ActionsOverlayGui.getInstance().getSelectedAction(ActionType.ABILITY);
        return selectedAbility != null && selectedAbility.getShiftVariationIfPresent()
                == ModActions.CRAZY_DIAMOND_BLOCK_ANCHOR_MOVE.get().getShiftVariationIfPresent();
    }

    @Override
    protected void updatePositions(List<Vector3d> list, float partialTick) {
        CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(mc.level, mc.player.getOffhandItem()).ifPresent(pos -> list.add(Vector3d.atCenterOf(pos)));
    }
}
