package com.github.standobyte.jojo.client;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.client.ui.screen.mob.RockPaperScissorsScreen;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.general.MathUtil.Matrix4ZYX;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.SSpawnParticlePacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.gui.GuiUtils;

@SuppressWarnings("resource")
public class ClientUtil {
    public static final ResourceLocation ADDITIONAL_UI = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/additional.png");
    public static final int MAX_MODEL_LIGHT = LightTexture.pack(15, 15);
    static boolean canSeeStands;
    static boolean canHearStands;

    public static PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static World getClientWorld() {
        return Minecraft.getInstance().level;
    }
    
    public static boolean isLocalServer() {
        return Minecraft.getInstance().isLocalServer();
    }
    
    public static boolean isShiftPressed() {
        return Screen.hasShiftDown();
    }
    
    public static boolean isDestroyingBlock() {
        return Minecraft.getInstance().gameMode.isDestroying();
    }

    public static Entity getEntityById(int entityId) {
        return Minecraft.getInstance().level.getEntity(entityId);
    }
    
    public static Entity getCrosshairPickEntity() {
        return Minecraft.getInstance().crosshairPickEntity;
    }
    
    public static float getPartialTick() {
        return Minecraft.getInstance().getFrameTime();
    }
    
    public static boolean canSeeStands() {
        return canSeeStands;
    }
    
    public static boolean canHearStands() {
        return canHearStands;
    }
    
    public static void setCameraEntityPreventShaderSwitch(Minecraft mc, Entity entity) {
        mc.setCameraEntity(entity);
        if (mc.gameRenderer.currentEffect() == null) {
            ResourceLocation shader = ClientEventHandler.getInstance().getCurrentShader();
            if (shader != null) {
                mc.gameRenderer.loadEffect(shader);
            }
        }
    }
    
    public static void openScreen(Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }
    
    public static void openRockPaperScissorsScreen(RockPaperScissorsGame game) {
        Minecraft.getInstance().setScreen(new RockPaperScissorsScreen(game));
    }
    
    public static void closeRockPaperScissorsScreen(RockPaperScissorsGame game) {
        if (Minecraft.getInstance().screen instanceof RockPaperScissorsScreen) {
            RockPaperScissorsScreen screen = (RockPaperScissorsScreen) Minecraft.getInstance().screen;
            if (screen.game == game) {
                Minecraft.getInstance().setScreen(null);
            }
        }
    }

    public static void openHamonTeacherUi() {
        Minecraft.getInstance().setScreen(new HamonScreen());
    }
    
    public static void setThirdPerson() {
        GameSettings options = Minecraft.getInstance().options;
        if (options.getCameraType() == PointOfView.FIRST_PERSON) {
            options.setCameraType(PointOfView.THIRD_PERSON_FRONT);
        }
    }

    public static void drawRightAlignedString(MatrixStack matrixStack, FontRenderer font, String line, float x, float y, int color) {
        font.drawShadow(matrixStack, line, x - font.width(line), y, color);
    }

    public static void drawRightAlignedString(MatrixStack matrixStack, FontRenderer font, ITextComponent line, float x, float y, int color) {
        drawRightAlignedString(matrixStack, font, line.getVisualOrderText(), x, y, color);
    }

    public static void drawRightAlignedString(MatrixStack matrixStack, FontRenderer font, IReorderingProcessor line, float x, float y, int color) {
        font.drawShadow(matrixStack, line, x - font.width(line), y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer font, IReorderingProcessor line, float x, float y, int color) {
        font.drawShadow(matrixStack, line, x - font.width(line) / 2, y, color);
    }

    public static void drawCenteredStringNoShadow(MatrixStack matrixStack, FontRenderer font, ITextComponent line, float x, float y, int color) {
        font.draw(matrixStack, line, x - font.width(line) / 2, y, color);
    }
    
    public static void drawTooltipRectangle(MatrixStack matrixStack, int x, int y, int width, int height) {
        drawTooltipRectangle(matrixStack, x, y, width, height, 
                GuiUtils.DEFAULT_BACKGROUND_COLOR, GuiUtils.DEFAULT_BORDER_COLOR_START, GuiUtils.DEFAULT_BORDER_COLOR_END, 400);
    }

    @SuppressWarnings("deprecation")
    public static void drawTooltipRectangle(MatrixStack matrixStack, int x, int y, int width, int height, 
            int backgroundColor, int borderColorStart, int borderColorEnd, int zLevel) {
        RenderSystem.disableRescaleNormal();
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        matrixStack.pushPose();
        Matrix4f mat = matrixStack.last().pose();
        
        drawGradientRect(mat, zLevel, x - 3, y - 4, x + width + 3, y - 3, backgroundColor, backgroundColor);
        drawGradientRect(mat, zLevel, x - 3, y + height + 3, x + width + 3, y + height + 4, backgroundColor, backgroundColor);
        drawGradientRect(mat, zLevel, x - 3, y - 3, x + width + 3, y + height + 3, backgroundColor, backgroundColor);
        drawGradientRect(mat, zLevel, x - 4, y - 3, x - 3, y + height + 3, backgroundColor, backgroundColor);
        drawGradientRect(mat, zLevel, x + width + 3, y - 3, x + width + 4, y + height + 3, backgroundColor, backgroundColor);
        drawGradientRect(mat, zLevel, x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, borderColorStart, borderColorEnd);
        drawGradientRect(mat, zLevel, x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, borderColorStart, borderColorEnd);
        drawGradientRect(mat, zLevel, x - 3, y - 3, x + width + 3, y - 3 + 1, borderColorStart, borderColorStart);
        drawGradientRect(mat, zLevel, x - 3, y + height + 2, x + width + 3, y + height + 3, borderColorEnd, borderColorEnd);

        matrixStack.popPose();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableRescaleNormal();
    }
    
    private static void drawGradientRect(Matrix4f mat, int zLevel, int left, int top, int right, int bottom, int startColor, int endColor) {
        float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
        float startRed   = (float)(startColor >> 16 & 255) / 255.0F;
        float startGreen = (float)(startColor >>  8 & 255) / 255.0F;
        float startBlue  = (float)(startColor       & 255) / 255.0F;
        float endAlpha   = (float)(endColor   >> 24 & 255) / 255.0F;
        float endRed     = (float)(endColor   >> 16 & 255) / 255.0F;
        float endGreen   = (float)(endColor   >>  8 & 255) / 255.0F;
        float endBlue    = (float)(endColor         & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.vertex(mat, right,    top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.vertex(mat,  left,    top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.vertex(mat,  left, bottom, zLevel).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        buffer.vertex(mat, right, bottom, zLevel).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        tessellator.end();
    }
    
    public static void fillSingleRect(double x, double y, double width, double height, int red, int green, int blue, int alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuilder();
        fillRect(bufferBuilder, x, y, width, height, red, green, blue, alpha);
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }
    
    public static void fillRect(BufferBuilder bufferBuilder, double x, double y, double width, double height, int red, int green, int blue, int alpha) {
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(x + 0 , y + 0, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x + 0 , y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x + width , y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x + width , y + 0, 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().end();
    }
    

    /*
     *             r1
     *             |
     *    r6 \     |      / r2   
     *         \   |    /
     *           center
     *         /   |    \
     *    r5 /     |      \ r3   
     *             |
     *             r4
     */
    private static final double COS_PI_BY_6 = Math.sqrt(3.0) / 2.0;
    private static final double SIN_PI_BY_6 = 0.5;
    public static void fillHexagon(double xCenter, double yCenter, 
            double r1, double r2, double r3, double r4, double r5, double r6, 
            int red, int green, int blue, int alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuilder();

        /*
         *          {x1, y1}
         * 
         * {x6, y6}          {x2, y2}
         * 
         *          {x0, y0}
         * 
         * {x5, y5}          {x3, y3}
         * 
         *          {x4, y4}
         */
        double x1 = xCenter;                        double y1 = yCenter - r1;
        double x2 = xCenter + r2 * COS_PI_BY_6;     double y2 = yCenter - r2 * SIN_PI_BY_6;
        double x3 = xCenter + r3 * COS_PI_BY_6;     double y3 = yCenter + r3 * SIN_PI_BY_6;
        double x4 = xCenter;                        double y4 = yCenter + r4;
        double x5 = xCenter - r5 * COS_PI_BY_6;     double y5 = yCenter + r5 * SIN_PI_BY_6;
        double x6 = xCenter - r6 * COS_PI_BY_6;     double y6 = yCenter - r6 * SIN_PI_BY_6;
        bufferBuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(xCenter, yCenter, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x1, y1, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x6, y6, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x5, y5, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x4, y4, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x3, y3, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x2, y2, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x1, y1, 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().end();

        if (r1 > 0 && r6 <= 0 && r2 <= 0) {
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(x1 + 2,                    yCenter + 2,                0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x1 + 2,                    y1,                         0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter - 2,               y1,                         0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter - 2,               yCenter + 2,                0.0D).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().end();
        }
        if (r2 > 0 && r1 <= 0 && r3 <= 0) {
            bufferBuilder.begin(9, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(xCenter + 2 * SIN_PI_BY_6, yCenter + 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x2 + 2 * SIN_PI_BY_6,      y2 + 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x2 - 2 * SIN_PI_BY_6,      y2 - 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter - 2 * SIN_PI_BY_6, yCenter - 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().end();
        }
        if (r3 > 0 && r2 <= 0 && r4 <= 0) {
            bufferBuilder.begin(9, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(xCenter + 2 * SIN_PI_BY_6, yCenter - 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter - 2 * SIN_PI_BY_6, yCenter + 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x3 - 2 * SIN_PI_BY_6,      y3 + 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x3 + 2 * SIN_PI_BY_6,      y3 - 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().end();
        }
        if (r4 > 0 && r3 <= 0 && r5 <= 0) {
            bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(xCenter - 2,               yCenter - 2,                0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter - 2,               y4,                         0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x4 + 2,                    y4,                         0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x4 + 2,                    yCenter - 2,                0.0D).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().end();
        }
        if (r5 > 0 && r4 <= 0 && r6 <= 0) {
            bufferBuilder.begin(9, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(xCenter - 2 * SIN_PI_BY_6, yCenter - 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x5 - 2 * SIN_PI_BY_6,      y5 - 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x5 + 2 * SIN_PI_BY_6,      y5 + 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter + 2 * SIN_PI_BY_6, yCenter + 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().end();
        }
        if (r6 > 0 && r5 <= 0 && r1 <= 0) {
            bufferBuilder.begin(9, DefaultVertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(xCenter + 2 * SIN_PI_BY_6, yCenter - 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x6 + 2 * SIN_PI_BY_6,      y6 - 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(x6 - 2 * SIN_PI_BY_6,      y6 + 2 * COS_PI_BY_6,       0.0D).color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(xCenter - 2 * SIN_PI_BY_6, yCenter + 2 * COS_PI_BY_6,  0.0D).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().end();
        }

        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }
    
    public static String getShortenedTranslationKey(String originalKey) {
        String shortenedKey = originalKey + ".shortened";
        return I18n.exists(shortenedKey) ? shortenedKey : originalKey;
    }
    
    public static Style textColor(int color) {
        return Style.EMPTY.withColor(Color.fromRgb(color));
    }
    
    public static int getFoliageColor(BlockState blockState, @Nullable IBlockDisplayReader world, BlockPos blockPos) {
        return Minecraft.getInstance().getBlockColors().getColor(blockState, world, blockPos, 0);
    }
    
    public static void playSoundAtClient(SoundEvent sound, SoundCategory category, BlockPos soundPos, float volume, float pitch) {
        ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (renderInfo.isInitialized()) {
            Vector3d clientPos = renderInfo.getPosition();
            Vector3d soundDir = Vector3d.atCenterOf(soundPos).subtract(clientPos);
            double dist = soundDir.length();
            if (dist > 0) {
                clientPos = clientPos.add(soundDir.scale(2 / dist));
            }
            ClientUtil.getClientWorld().playLocalSound(clientPos.x, clientPos.y, clientPos.z, 
                    sound, category, volume, pitch, false);
        }
    }
    
    public static void playMusic(SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(new SimpleSound(
                sound.getLocation(), 
                SoundCategory.RECORDS, 
                volume, pitch, false, 0, ISound.AttenuationType.NONE, 
                0, 0, 0, true));
    }
    
    public static void createHamonSparkParticles(double x, double y, double z, int particlesCount) {
        Minecraft.getInstance().getConnection().handleParticleEvent(new SSpawnParticlePacket(
                ModParticles.HAMON_SPARK.get(), false, x, y, z, 0.05F, 0.05F, 0.05F, 0.25F, particlesCount));
    }
    
    public static void createParticlesEmitter(Entity entity, IParticleData type, int ticks) {
        Minecraft.getInstance().particleEngine.createTrackingEmitter(entity, type, ticks);
    }
    
    public static boolean decreasedParticlesSetting() {
        return Minecraft.getInstance().options.particles == ParticleStatus.DECREASED;
    }
    
    public static float[] rgb(int color) {
        int[] rgbInt = rgbInt(color);
        return new float[] {
                (float) rgbInt[0] / 255F,
                (float) rgbInt[1] / 255F,
                (float) rgbInt[2] / 255F
        };
    }
    
    public static int[] rgbInt(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        return new int[] {red, green, blue};
    }
    
    public static int discColor(int color) {
        return (((0xFFFFFF - color) & 0xFEFEFE) >> 1) + color;
    }
    
    public static void vertex(MatrixStack.Entry matrixEntry, IVertexBuilder vertexBuilder, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha, 
            float x, float y, float z, float texU, float texV) {
        vertexBuilder
        .vertex(matrixEntry.pose(), x, y, z)
        .color(red, green, blue, alpha)
        .uv(texU, texV)
        .overlayCoords(packedOverlay)
        .uv2(packedLight)
        .normal(matrixEntry.normal(), 0.0F, 1.0F, 0.0F)
        .endVertex();
    }    
    
    public static void vertex(Matrix4f matrix, Matrix3f normals, IVertexBuilder vertexBuilder, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha, 
            float offsetX, float offsetY, float offsetZ, 
            float texU, float texV, 
            float normalX, float normalY, float normalZ) {
        vertexBuilder
        .vertex(matrix, offsetX, offsetY, offsetZ)
        .color(red, green, blue, alpha)
        .uv(texU, texV)
        .overlayCoords(packedOverlay)
        .uv2(packedLight)
        .normal(normals, normalX, normalZ, normalY)
        .endVertex();
    }

    public static float getHighlightAlpha(float ticks, float cycleTicks, float maxAlphaTicks, float minAlpha, float maxAlpha) {
        ticks %= cycleTicks;
        float coeff = maxAlpha / maxAlphaTicks;
        float alpha = ticks <= cycleTicks / 2 ? coeff * ticks : coeff * (cycleTicks - ticks);
        return Math.min(alpha, maxAlpha - minAlpha) + minAlpha;
    }
    
    public static ResourceLocation getPlayerSkin(GameProfile gameProfile) {
        Minecraft minecraft = Minecraft.getInstance();
        Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(gameProfile);
        if (map.containsKey(Type.SKIN)) {
            return minecraft.getSkinManager().registerTexture(map.get(Type.SKIN), Type.SKIN);
        } else {
            return DefaultPlayerSkin.getDefaultSkin(PlayerEntity.createPlayerUUID(gameProfile));
        }
    }

    public static void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    public static void setRotationAngleDegrees(ModelRenderer modelRenderer, float x, float y, float z) {
        setRotationAngle(modelRenderer, x * MathUtil.DEG_TO_RAD, y * MathUtil.DEG_TO_RAD, z * MathUtil.DEG_TO_RAD);
    }
    
    public static void rotateAngles(ModelRenderer modelRenderer, float xRotSecond) {
        Vector3f angles = rotateAngles(modelRenderer.xRot, modelRenderer.yRot, modelRenderer.zRot, xRotSecond);
        modelRenderer.xRot = angles.x();
        modelRenderer.yRot = angles.y();
        modelRenderer.zRot = angles.z();
    }
    
    public static Vector3f rotateAngles(float xRot, float yRot, float zRot, float xRotSecond) {
        Quaternion quat = MathUtil.quaternionZYX(xRot, yRot, zRot, false);
        Quaternion q2 = Vector3f.XP.rotation(xRotSecond);
        q2.mul(quat);
        Matrix4ZYX rotMatrix = new Matrix4ZYX(q2);
        Vector3f rotVec = rotMatrix.rotationVec();
        return rotVec;
    }
    
    public static void clearCubes(ModelRenderer modelRenderer) {
        ClientReflection.setCubes(modelRenderer, new ObjectArrayList<>());
    }
    
    public static void addItemReferenceQuote(List<ITextComponent> tooltip, Item item) {
        tooltip.add(new StringTextComponent(" "));
        ResourceLocation itemId = item.getRegistryName();
        tooltip.add(new TranslationTextComponent("item." + itemId.getNamespace() + "." + itemId.getPath() + ".reference_quote").withStyle(TextFormatting.ITALIC, TextFormatting.DARK_GRAY));
    }
    
    public static ITextComponent donoItemTooltip(String donoUsername) {
        return new TranslationTextComponent("item.jojo.dono_tooltip", donoUsername).withStyle(TextFormatting.DARK_GRAY);
    }
}
