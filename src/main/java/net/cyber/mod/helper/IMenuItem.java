package net.cyber.mod.helper;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public interface IMenuItem {
    public boolean hasMenu(ItemStack stack);
    public void use(Entity entity, ItemStack stack);
    public String getUnlocalizedLabel(ItemStack stack);
    public float[] getColor(ItemStack stack);
}