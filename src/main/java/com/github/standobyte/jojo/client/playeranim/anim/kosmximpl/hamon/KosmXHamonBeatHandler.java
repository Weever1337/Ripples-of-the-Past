package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedMirrorModifier;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

public class KosmXHamonBeatHandler extends AnimLayerHandler<ModifierLayer<IAnimation>> implements BasicToggleAnim {
    private static final float SPEED = 3;

    public KosmXHamonBeatHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        ModifierLayer<IAnimation> anim = new ModifierLayer<>(null, new SpeedModifier(SPEED));
        if (player.getMainArm() == HandSide.LEFT) {
            anim.addModifierBefore(new KosmXFixedMirrorModifier());
        }
        return anim;
    }
    

    private static final ResourceLocation ANIM = new ResourceLocation(JojoMod.MOD_ID, "hamon_beat");
    @Override
    public boolean setAnimEnabled(PlayerEntity player, boolean enabled) {
        if (enabled) {
            return setAnimFromName((AbstractClientPlayerEntity) player, ANIM);
        }
        else {
            return fadeOutAnim((AbstractClientPlayerEntity) player, AbstractFadeModifier.standardFadeIn((int) (10 * SPEED), Ease.OUTCUBIC), null);
        }
    }

}
