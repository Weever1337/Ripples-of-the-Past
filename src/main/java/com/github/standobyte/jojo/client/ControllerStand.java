package com.github.standobyte.jojo.client;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.POTION_ICONS;
import static net.minecraftforge.event.TickEvent.Phase.END;
import static net.minecraftforge.fml.LogicalSide.CLIENT;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClStandManualMovementPacket;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType.MovementType;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ControllerStand {
    private static ControllerStand instance = null;
    
    private final Minecraft mc;
    private boolean isProbablyControllingStand;
    private StandEntity stand;

    private ControllerStand(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new ControllerStand(mc);
            MinecraftForge.EVENT_BUS.register(instance);
        }
    }

    public static ControllerStand getInstance() {
        return instance;
    }
    
    public static void setStartedControllingStand() {
        if (instance.mc.getCameraEntity() instanceof StandEntity) {
            instance.isProbablyControllingStand = true;
            instance.stand = (StandEntity) instance.mc.getCameraEntity();
        }
    }

    public boolean isControllingStand() {
        if (isProbablyControllingStand) {
            isProbablyControllingStand = mc.getCameraEntity() instanceof StandEntity;
            if (!isProbablyControllingStand) {
                stand = null;
            }
        }
        return isProbablyControllingStand;
    }
    
    public MovementType getMovementTypeOfStand() {
    	return getManuallyControlledStand() == null ? MovementType.FLYING : getManuallyControlledStand().getMovementType();
    }
    
    @Nullable
    public StandEntity getManuallyControlledStand() {
        if (isControllingStand()) {
            return stand;
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onInputUpdate(InputUpdateEvent event) {
        if (isControllingStand()) {
            MovementInput input = event.getMovementInput();
            MovementType movementType = getMovementTypeOfStand();
            stand.moveStandManually(input.leftImpulse, input.forwardImpulse, input.jumping, input.shiftKeyDown, movementType);
            // FIXME do not reset deltaMovement in manual control
            PacketManager.sendToServer(new ClStandManualMovementPacket(
                    stand.getX(), stand.getY(), stand.getZ(), stand.xRot, stand.yRot, stand.hadInput()));
        }
        else {
            if ((mc.getCameraEntity() == mc.player || mc.getCameraEntity() == null) && ModStatusEffects.isStunned(mc.player)) {
                MovementInput input = event.getMovementInput();
                input.forwardImpulse = 0;
                input.leftImpulse = 0;
                input.jumping = false;
                if (mc.player.getVehicle() != null) {
                    input.shiftKeyDown = false;
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onMouseScroll(InputEvent.MouseScrollEvent event) {
        if (isControllingStand() && getMovementTypeOfStand() == MovementType.FLYING) {
            stand.manualMovementSpeed = MathHelper.clamp(stand.manualMovementSpeed + 0.025f * (float) event.getScrollDelta(), 0, 1);
        }
    }
    
    @SubscribeEvent
    public void sendPlayerPositionRotation(PlayerTickEvent event) {
        if (event.side != CLIENT || event.phase != END || !isControllingStand()) {
            return;
        }

        ClientPlayerEntity player = mc.player;
        if (!stand.isAlive()) {
            ClientUtil.setCameraEntityPreventShaderSwitch(player);
        }
        else {
            player.connection.send(new CPlayerPacket.PositionRotationPacket(player.getX(), player.getY(), player.getZ(), player.yRot, player.xRot, player.isOnGround()));
        }
    }
    
    

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void renderStandHands(RenderHandEvent event) {
        if (!isControllingStand()) {
            return;
        }

        ClientPlayerEntity player = mc.player;
        player.yBobO = player.yBob;
        player.xBobO = player.xBob;
        player.xBob = (float)((double)player.xBob + (double)(player.xRot - player.xBob) * 0.5D);
        player.yBob = (float)((double)player.yBob + (double)(player.yRot - player.yBob) * 0.5D);
        MatrixStack matrixStack = event.getMatrixStack();
        IRenderTypeBuffer buffer = event.getBuffers();
        float partialTick = event.getPartialTicks();
        int light = mc.getEntityRenderDispatcher().getPackedLightCoords(stand, partialTick);
        StandEntityRenderer renderer = (StandEntityRenderer<?, ?>)mc.getEntityRenderDispatcher().<StandEntity>getRenderer(stand);
//        renderer.renderFirstPersonArms(matrixStack, buffer, light, stand, partialTick);
        renderer.renderFirstPerson(stand, partialTick, matrixStack, buffer, light);
        event.setCanceled(true);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelBlockOverlayRender(RenderBlockOverlayEvent event) {
        if (isControllingStand()) {
            event.setCanceled(true);
        }
    }
    
    

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderStandEffectsGui(RenderGameOverlayEvent.Pre event) {
        if (!isControllingStand()) {
            return;
        }
        if (event.getType() == POTION_ICONS) {
            MatrixStack matrixStack = event.getMatrixStack();
            event.setCanceled(true);
            IngameGui gui = mc.gui;
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            renderStandPotionEffects(matrixStack, gui, event, width, height);     
        }
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderStandGui(RenderGameOverlayEvent.Pre event) {
        if (!isControllingStand()) {
            return;
        }

        MatrixStack matrixStack = event.getMatrixStack();
        if (!mc.options.hideGui) {
            switch (event.getType()) {
            case ALL:
                if (mc.gameMode.canHurtPlayer()) {
                    IngameGui gui = mc.gui;
                    int width = mc.getWindow().getGuiScaledWidth();
                    int height = mc.getWindow().getGuiScaledHeight();
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    if (ForgeIngameGui.renderHealth) renderCameraStandHealth(matrixStack, gui, event, width, height);
                    if (ForgeIngameGui.renderArmor)  renderCameraStandArmor(matrixStack, gui, event, width, height);
                }
                break;
            default:
                break;
            }
        }
    }

    private void renderCameraStandHealth(MatrixStack matrixStack, IngameGui gui, RenderGameOverlayEvent event, int width, int height) {
        LivingEntity entity = StandUtil.getStandUser(stand);
        ClientEventHandler.getInstance().renderHealthWithBleeding(entity, matrixStack, gui, event, width, height);
    }

    private void renderCameraStandArmor(MatrixStack matrixStack, IngameGui gui, RenderGameOverlayEvent event, int width, int height) {
        mc.getProfiler().push("armor");

        RenderSystem.enableBlend();
        int left = width / 2 - 91;
        int top = height - ForgeIngameGui.left_height;

        int level = stand.getArmorValue();
        for (int i = 1; level > 0 && i < 20; i += 2)
        {
            if (i < level)
            {
                gui.blit(matrixStack, left, top, 34, 9, 9, 9);
            }
            else if (i == level)
            {
                gui.blit(matrixStack, left, top, 25, 9, 9, 9);
            }
            else if (i > level)
            {
                gui.blit(matrixStack, left, top, 16, 9, 9, 9);
            }
            left += 8;
        }
        ForgeIngameGui.left_height += 10;

        RenderSystem.disableBlend();
        mc.getProfiler().pop();
    }

    @SuppressWarnings("deprecation")
    private void renderStandPotionEffects(MatrixStack matrixStack, IngameGui gui, RenderGameOverlayEvent event, int width, int height) {
        Collection<EffectInstance> collection = stand.getActiveEffects();
        if (!collection.isEmpty()) {
            RenderSystem.enableBlend();
            int i = 0;
            int j = 0;
            PotionSpriteUploader potionspriteuploader = mc.getMobEffectTextures();
            List<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());
            mc.getTextureManager().bind(ContainerScreen.INVENTORY_LOCATION);

            for(EffectInstance effectinstance : Ordering.natural().reverse().sortedCopy(collection)) {
                Effect effect = effectinstance.getEffect();
                if (!effectinstance.shouldRenderHUD()) continue;
                // Rebind in case previous renderHUDEffect changed texture
                mc.getTextureManager().bind(ContainerScreen.INVENTORY_LOCATION);
                if (effectinstance.showIcon()) {
                    int k = width;
                    int l = 1;
                    if (mc.isDemo()) {
                        l += 15;
                    }

                    if (effect.isBeneficial()) {
                        ++i;
                        k = k - 25 * i;
                    } else {
                        ++j;
                        k = k - 25 * j;
                        l += 26;
                    }

                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    float f = 1.0F;
                    if (effectinstance.isAmbient()) {
                        gui.blit(matrixStack, k, l, 165, 166, 24, 24);
                    } else {
                        gui.blit(matrixStack, k, l, 141, 166, 24, 24);
                        if (effectinstance.getDuration() <= 200) {
                            int i1 = 10 - effectinstance.getDuration() / 20;
                            f = MathHelper.clamp((float)effectinstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float)effectinstance.getDuration() * (float)Math.PI / 5.0F) * MathHelper.clamp((float)i1 / 10.0F * 0.25F, 0.0F, 0.25F);
                        }
                    }

                    TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effect);
                    int j1 = k;
                    int k1 = l;
                    float f1 = f;
                    list.add(() -> {
                        mc.getTextureManager().bind(textureatlassprite.atlas().location());
                        RenderSystem.color4f(1.0F, 1.0F, 1.0F, f1);
                        AbstractGui.blit(matrixStack, j1 + 3, k1 + 3, gui.getBlitOffset(), 18, 18, textureatlassprite);
                    });
                    effectinstance.renderHUDEffect(gui, matrixStack, k, l, gui.getBlitOffset(), f);
                }
            }

            list.forEach(Runnable::run);
        }
    }
}
