package net.cyber.mod.helper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface ICyberPart {
    int getEssenceCost();
    void setEssenceCost(int essence);
    CyberPartEnum getCategory();
    CyberPartType getType();
    void runOnce(PlayerEntity player);
    void runOnceUndo(PlayerEntity player);
    void runOnTick(PlayerEntity player);
}
