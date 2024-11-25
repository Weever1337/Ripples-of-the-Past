package com.github.standobyte.jojo.util.mod;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.world.GameType;

public interface IPlayerPossess {
    void jojoPossessEntity(@Nullable Entity entity);
    @Nullable Entity jojoGetPossessedEntity();
    @Nullable GameType jojoGetPrePossessGameMode();
    void jojoSetPrePossessGameMode(GameType gameType);
    
    public static Entity getPossessedEntity(Entity possessing) {
        return possessing instanceof IPlayerPossess ? ((IPlayerPossess) possessing).jojoGetPossessedEntity() : null;
    }
}
