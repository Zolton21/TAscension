package net.cyber.mod.helper;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.List;

public interface ICyberInfo {
    public EnumSlot getSlot(ItemStack stack);

    public int installedStackSize(ItemStack stack);

    public NonNullList<NonNullList<ItemStack>> required(ItemStack stack);

    public boolean isIncompatible(ItemStack stack, ItemStack comparison);

    boolean isEssential(ItemStack stack);

    public List<String> getInfo(ItemStack stack);

    public int getCapacity(ItemStack wareStack);


    // @TODO rename to BodyRegion since it's more a type/category than an actual inventory slot
    public enum EnumSlot {
        EYES(12, "eyes"),
        CRANIUM(11, "cranium"),
        HEART(14, "heart"),
        LUNGS(15, "lungs"),
        LOWER_ORGANS(17, "lower_organs"),
        SKIN(18, "skin"),
        MUSCLE(19, "muscle"),
        BONE(20, "bone"),
        ARM(21, "arm", true, true),
        HAND(22, "hand", true, false),
        LEG(23, "leg", true, true),
        FOOT(24, "foot", true, false);

        private final int slotNumber;
        private final String name;
        private final boolean sidedSlot;
        private final boolean hasEssential;

        private EnumSlot(int slot, String name, boolean sidedSlot, boolean hasEssential) {
            this.slotNumber = slot;
            this.name = name;
            this.sidedSlot = sidedSlot;
            this.hasEssential = hasEssential;
        }

        private EnumSlot(int slot, String name) {
            this(slot, name, false, true);
        }

        public int getSlotNumber() {
            return slotNumber;
        }

        public static EnumSlot getSlotByPage(int page) {
            for (EnumSlot slot : values()) {
                if (slot.getSlotNumber() == page) {
                    return slot;
                }
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public boolean isSided() {
            return sidedSlot;
        }

        public boolean hasEssential() {
            return hasEssential;
        }
    }

    public void onAdded(Entity entityLivingBase, ItemStack stack);

    public void onRemoved(Entity entityLivingBase, ItemStack stack);

    interface ISidedLimb {
        public EnumSide getSide(ItemStack stack);

        enum EnumSide {
            LEFT,
            RIGHT;
        }
    }

    public int getEssenceCost(ItemStack stack);
}