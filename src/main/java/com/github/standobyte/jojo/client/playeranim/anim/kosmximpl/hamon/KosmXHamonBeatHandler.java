package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXHandsideMirrorModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.playermotion.KosmXFrontMotionModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.playermotion.KosmXPlayerMotionAnimHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.playermotion.KosmXPlayerMotionModifiersLayer;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class KosmXHamonBeatHandler extends AnimLayerHandler<KosmXPlayerMotionModifiersLayer<IAnimation>> implements BasicToggleAnim, KosmXPlayerMotionAnimHandler {
    private static final float SPEED = 3;

    public KosmXHamonBeatHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    protected KosmXPlayerMotionModifiersLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        KosmXPlayerMotionModifiersLayer<IAnimation> animLayer = new KosmXPlayerMotionModifiersLayer<>(null, 
                new KosmXHandsideMirrorModifier(player), 
                new SpeedModifier(SPEED));
        
        KosmXFrontMotionModifier frontMotion = new KosmXFrontMotionModifier(player);
        animLayer.setPlayerMotionModifier(frontMotion);
        return animLayer;
    }
    

    private static final ResourceLocation ANIM = new ResourceLocation(JojoMod.MOD_ID, "hamon_beat");
    @Override
    public boolean setAnimEnabled(PlayerEntity player, boolean enabled) {
        if (enabled) {
            AbstractClientPlayerEntity clPlayer = (AbstractClientPlayerEntity) player;
            if (setAnimFromName(clPlayer, ANIM)) {
                KosmXPlayerMotionModifiersLayer<?> animLayer = getAnimLayer(clPlayer);
                KosmXFrontMotionModifier frontMotion = animLayer.getPlayerMotionModifier();
                if (frontMotion != null) {
                    frontMotion.setAnimStart(player);
                }
                return true;
            }
            return false;
        }
        else {
            return fadeOutAnim((AbstractClientPlayerEntity) player, KosmXFixedFadeModifier.standardFadeIn((int) (10 * SPEED), Ease.OUTCUBIC), null);
        }
    }

    @Override
    public KosmXFrontMotionModifier getPlayerMotionModifier() {
        return getAnimLayer(Minecraft.getInstance().player).getPlayerMotionModifier();
    }

}
