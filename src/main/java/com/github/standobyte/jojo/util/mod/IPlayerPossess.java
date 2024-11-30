package com.github.standobyte.jojo.util.mod;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.world.GameType;

public interface IPlayerPossess {
    void jojoPossessEntity(@Nullable Entity entity, boolean asAlive);
    @Nullable Entity jojoGetPossessedEntity();
    boolean jojoIsPossessingAsAlive();
    @Nullable GameType jojoGetPrePossessGameMode();
    void jojoSetPrePossessGameMode(GameType gameType);
    void jojoOnPossessingDead();
    
    public static Entity getPossessedEntity(Entity possessing) {
        return possessing instanceof IPlayerPossess ? ((IPlayerPossess) possessing).jojoGetPossessedEntity() : null;
    }
}
