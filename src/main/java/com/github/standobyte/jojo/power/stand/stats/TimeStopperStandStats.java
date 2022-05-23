package com.github.standobyte.jojo.power.stand.stats;

import com.github.standobyte.jojo.action.actions.TimeStop;

import net.minecraft.network.PacketBuffer;

public class TimeStopperStandStats extends StandStats {
    private final int timeStopMaxTicks;
    private final int timeStopMaxTicksVampire;
    public final float timeStopLearningPerTick;
    // FIXME (!!!!) ts decay
    public final float timeStopDecayPerDay;
    public final float timeStopCooldownPerTick;

    protected TimeStopperStandStats(Builder builder) {
        super(builder);
        this.timeStopMaxTicks = builder.timeStopMaxTicks;
        this.timeStopMaxTicksVampire = builder.timeStopMaxTicksVampire;
        this.timeStopLearningPerTick = builder.timeStopLearningPerTick;
        this.timeStopDecayPerDay = builder.timeStopDecayPerDay;
        this.timeStopCooldownPerTick = builder.timeStopCooldownPerTick;
    }
    
    protected TimeStopperStandStats(PacketBuffer buf) {
        super(buf);
        this.timeStopMaxTicks = buf.readInt();
        this.timeStopMaxTicksVampire = buf.readInt();
        this.timeStopLearningPerTick = buf.readFloat();
        this.timeStopDecayPerDay = buf.readFloat();
        this.timeStopCooldownPerTick = buf.readFloat();
    }
    
    public int getMaxTimeStopTicks(boolean vampire) {
        return vampire ? timeStopMaxTicksVampire : timeStopMaxTicks;
    }
    
    @Override
    public void write(PacketBuffer buf) {
        super.write(buf);
        buf.writeInt(timeStopMaxTicks);
        buf.writeInt(timeStopMaxTicksVampire);
        buf.writeFloat(timeStopLearningPerTick);
        buf.writeFloat(timeStopDecayPerDay);
        buf.writeFloat(timeStopCooldownPerTick);
    }
    
    static {
        registerFactory(TimeStopperStandStats.class, TimeStopperStandStats::new);
    }
    
    

    public static class Builder extends AbstractBuilder<Builder> {
        private int timeStopMaxTicks = 100;
        private int timeStopMaxTicksVampire = 180;
        private float timeStopLearningPerTick = 0.1F;
        private float timeStopDecayPerDay = 0;
        private float timeStopCooldownPerTick = 3;
        
        public Builder timeStopMaxTicks(int forHuman, int forVampire) {
            forHuman = Math.max(TimeStop.MIN_TIME_STOP_TICKS, forHuman);
            forVampire = Math.max(forHuman, forVampire);
            this.timeStopMaxTicks = forHuman;
            this.timeStopMaxTicksVampire = forVampire;
            return getThis();
        }
        
        public Builder timeStopLearningPerTick(float points) {
            this.timeStopLearningPerTick = points;
            return this;
        }
        
        public Builder timeStopDecayPerDay(float points) {
            this.timeStopDecayPerDay = points;
            return this;
        }
        
        public Builder timeStopCooldownPerTick(float ticks) {
            this.timeStopCooldownPerTick = ticks;
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
        
        public TimeStopperStandStats build() {
            return new TimeStopperStandStats(this);
        }
    }
}
