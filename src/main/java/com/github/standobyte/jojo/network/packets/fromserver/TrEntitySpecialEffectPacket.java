package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrEntitySpecialEffectPacket {
    private final int entityId;
    private final Type type;
    private final int playerId;
    
    public TrEntitySpecialEffectPacket(int entityId, Type type, int playerId) {
        this.entityId = entityId;
        this.type = type;
        this.playerId = playerId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrEntitySpecialEffectPacket> {
    
        public void encode(TrEntitySpecialEffectPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeEnum(msg.type);
            buf.writeInt(msg.playerId);
        }
        
        public TrEntitySpecialEffectPacket decode(PacketBuffer buf) {
            return new TrEntitySpecialEffectPacket(buf.readInt(), buf.readEnum(Type.class), buf.readInt());
        }
    
        public void handle(TrEntitySpecialEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
                Entity trigerringPlayer = ClientUtil.getEntityById(msg.playerId);
                if (trigerringPlayer == ClientUtil.getClientPlayer()) {
                    switch (msg.type) {
                    case SOLD_METEORITE_MAP:
                        ClientUtil.playMusic(ModSounds.MAP_BOUGHT_METEORITE.get(), 1.0F, 1.0F);
                        break;
                    case SOLD_HAMON_TEMPLE_MAP:
                        ClientUtil.playMusic(ModSounds.MAP_BOUGHT_HAMON_TEMPLE.get(), 1.0F, 1.0F);
                        break;
                    case SOLD_PILLAR_MAN_TEMPLE_MAP:
                        ClientUtil.playMusic(ModSounds.MAP_BOUGHT_PILLAR_MAN_TEMPLE.get(), 1.0F, 1.0F);
                        break;
                    }
                }
                CustomParticlesHelper.addMenacingParticleEmitter(entity, ModParticles.MENACING.get());
            }
        }

        @Override
        public Class<TrEntitySpecialEffectPacket> getPacketClass() {
            return TrEntitySpecialEffectPacket.class;
        }
    }
    
    public static enum Type {
        SOLD_METEORITE_MAP,
        SOLD_HAMON_TEMPLE_MAP,
        SOLD_PILLAR_MAN_TEMPLE_MAP
    }
}
