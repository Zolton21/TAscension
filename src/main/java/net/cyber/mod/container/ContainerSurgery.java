package net.cyber.mod.container;

import net.cyber.mod.cap.CyberCapabilities;
import net.cyber.mod.container.slots.UpgradeSlot;
import net.cyber.mod.helper.CyberPartEnum;
import net.cyber.mod.helper.Helper;
import net.cyber.mod.tileentity.TileEntitySurgery;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;


public class ContainerSurgery extends BEContainer<TileEntitySurgery> {
    public PlayerEntity entity;
    public NonNullList<ItemStack> oldstackslist = NonNullList.create();
    public NonNullList<ItemStack> newstackslist = NonNullList.create();


    protected ContainerSurgery(ContainerType<?> type, int id) {
        super(type, id);
    }
    /** Client Only constructor */
    public ContainerSurgery(int id, PlayerInventory inv, PacketBuffer buf) {
        super(CyberContainers.SURGEON.get(), id);
        this.init(inv, (TileEntitySurgery) inv.player.world.getTileEntity(buf.readBlockPos()));
    }
    /** Server Only constructor */
    public ContainerSurgery(int id, PlayerInventory inv, TileEntitySurgery tile) {
        super(CyberContainers.SURGEON.get(), id);
        this.init(inv, tile);
        this.entity = inv.player;
    }





    public void init(PlayerInventory inv, TileEntitySurgery tile) {

        //Upgrades
        //Part 1
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.ARM), tile, 0, -5, 28));
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.ARM), tile, 1, -5, 46));

        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.BONE), tile,  2, 29, 28));
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.BONE), tile,  3, 29, 46));

        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.CRANIUM), tile,  4, 63, 28));
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.CRANIUM), tile,  5, 63, 46));

        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.EYES), tile,  6, 97, 28));
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.EYES), tile,  7, 97, 46));

        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.FOOT), tile,  8, 131, 28));
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.FOOT), tile,  9, 131, 46));

        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.HAND), tile,  10, 165, 28));
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.HAND), tile,  11, 165, 46));

        //Part 2
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.HEART), tile,  12, -5, 67));
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.HEART), tile,  13, -5, 85));

        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.LEG), tile,  14, 29, 67));
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.LEG), tile,  15, 29, 85));

        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.LOWER_ORGANS), tile,  16, 63, 67));
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.LOWER_ORGANS), tile,  17, 63, 85));

        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.LUNGS), tile,  18, 97, 67));
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.LUNGS), tile,  19, 97, 85));

        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.MUSCLE), tile,  20, 131, 67));
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.MUSCLE), tile,  21, 131, 85));

        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.SKIN), tile,  22, 165, 67));
        this.addSlot(new UpgradeSlot(this, type -> type.equals(CyberPartEnum.SKIN), tile,  23, 165, 85));

        Helper.addPlayerInvContainer(this, inv, 0, 54);

        for (int i = 0; i < 24; i++) {
            oldstackslist.add(ItemStack.EMPTY);
        }
        //System.out.println("oldstackslist size: " + oldstackslist.size());
        /*for (int i = 0; i < 24; i++) {
            oldstackslist.set(i, this.getSlot(i).getStack().copy());
            //System.out.println("Initialization stack: " + oldstackslist.get(i));
        }*/
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        //System.out.println("onContainerClosed called");
        playerIn.getCapability(CyberCapabilities.CYBERWARE_CAPABILITY).ifPresent(cap -> {
            /*NonNullList<ItemStack> stacks = NonNullList.create();
            if (cap.getAllCyberware() != stacks) {
                cap.setAllCyberware(stacks);
            }*/
            if(!playerIn.world.isRemote) {
                for (int i = 0; i < 24; i++) {
                    newstackslist.add(ItemStack.EMPTY);
                }
                for (int i = 0; i < 24; i++) {
                    newstackslist.set(i, this.getSlot(i).getStack().copy());
                }
                for (int i = 0; i < 24; i++) {
                    Slot slot = this.getSlot(i);
                    newstackslist.set(i, slot.getStack());
                    if(!ItemStack.areItemStacksEqual(oldstackslist.get(i), newstackslist.get(i))){
                        //System.out.println("oldstack: " + oldstacks.get(i));
                        //System.out.println("oldstack: " + newstackslist.get(i));
                        if(!newstackslist.get(i).isEmpty()){
                            cap.handleAdded(newstackslist.get(i));
                            oldstackslist.set(i, newstackslist.get(i));
                        }
                        else if (!oldstackslist.get(i).isEmpty()) {
                            cap.handleRemoved(oldstackslist.get(i));
                            oldstackslist.set(i, newstackslist.get(i));
                        }
                    }
                }
                saveDataToNBT(newstackslist);
                oldstackslist.clear();
                newstackslist.clear();
            }
        });
        super.onContainerClosed(playerIn);
    }

    private void saveDataToNBT(NonNullList<ItemStack> stacklist) {
        if((entity != null) && (entity.getPersistentData() != null)){
            CompoundNBT playerData = entity.getPersistentData();
            CompoundNBT customData = new CompoundNBT();
            ListNBT stacksNBT = new ListNBT();
            for (int i = 0; i<stacklist.size(); i++) {
                if (!stacklist.get(i).isEmpty()) {
                    CompoundNBT stackTag = new CompoundNBT();
                    stacklist.get(i).write(stackTag);
                    stacksNBT.add(stackTag);
                }
                else{
                    CompoundNBT stackTag = new CompoundNBT();
                    ItemStack.EMPTY.write(stackTag);
                    stacksNBT.add(stackTag);
                }
            }
            customData.put("SurgeryStacks", stacksNBT);
            playerData.put("SurgeryData", customData);
            //System.out.println("StackNBT size: " + stacksNBT.size());
        }
    }

    private void loadDataFromNBT() {
        System.out.println("loadDataFromNBT called");
        if((entity != null) && (entity.getPersistentData() != null)){
            CompoundNBT playerData = entity.getPersistentData();
            if (playerData.contains("SurgeryData")) {
                CompoundNBT customData = playerData.getCompound("SurgeryData");
                ListNBT stacksNBT = customData.getList("SurgeryStacks", Constants.NBT.TAG_COMPOUND);
                System.out.println("stacksNBT Size: " + stacksNBT.size());
                for (int i = 0; i < stacksNBT.size(); i++) {
                    CompoundNBT stackTag = stacksNBT.getCompound(i);
                    ItemStack stack = ItemStack.read(stackTag);
                    oldstackslist.set(i, stack);
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }
}
