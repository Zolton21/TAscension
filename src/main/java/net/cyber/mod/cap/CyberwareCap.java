package net.cyber.mod.cap;

import net.cyber.mod.CyberMod;
import net.cyber.mod.helper.*;
import net.cyber.mod.items.CyberItems;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.Direction;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

public class CyberwareCap implements ICyberData {
    private NonNullList<NonNullList<ItemStack>> cyberwaresBySlot = NonNullList.create();
    private boolean[] missingEssentials = new boolean[ICyberInfo.EnumSlot.values().length * 2];


    private static ItemStack[][] startingItems;
    private static NonNullList<NonNullList<ItemStack>> startingStacks;
    private int power_stored = 0;
    private int power_production = 0;
    private int power_lastProduction = 0;
    private int power_consumption = 0;
    private int power_lastConsumption = 0;
    private int power_capacity = 0;
    public PlayerEntity player;
    private Map<ItemStack, Integer> power_buffer = new HashMap<>();
    private Map<ItemStack, Integer> power_lastBuffer = new HashMap<>();
    private NonNullList<ItemStack> nnlPowerOutages = NonNullList.create();
    private List<Integer> ticksPowerOutages = new ArrayList<>();
    private int missingEssence = 0;
    private NonNullList<ItemStack> specialBatteries = NonNullList.create();
    private NonNullList<ItemStack> activeItems = NonNullList.create();
    private NonNullList<ItemStack> hudjackItems = NonNullList.create();
    private Map<Integer, ItemStack> hotkeys = new HashMap<>();
    private CompoundNBT hudData;
    private boolean hasOpenedRadialMenu = false;
    private static ItemStack[][] defaultStartingItems;

    private int hudColor = 0x00FFFF;
    private float[] hudColorFloat = new float[] { 0.0F, 1.0F, 1.0F };


    public CyberwareCap()
    {
        hudData = new CompoundNBT();
        for (ICyberInfo.EnumSlot slot : ICyberInfo.EnumSlot.values())
        {
            NonNullList<ItemStack> nnlCyberwaresInSlot = NonNullList.create();
            for (int indexSlot = 0; indexSlot < CyberCon.WARE_PER_SLOT; indexSlot++)
            {
                nnlCyberwaresInSlot.add(ItemStack.EMPTY);
            }
            cyberwaresBySlot.add(nnlCyberwaresInSlot);
        }
        resetWare(null);
    }

    @Override
    public void resetWare(Entity entityLivingBase) {
        for (NonNullList<ItemStack> nnlCyberwaresInSlot : cyberwaresBySlot)
        {
            for (ItemStack item : nnlCyberwaresInSlot)
            {
                if (CyberAPI.isCyberware(item))
                {
                    CyberAPI.getCyberware(item).onRemoved(entityLivingBase, item);
                }
            }
        }
        missingEssence = 0;
        for (ICyberInfo.EnumSlot slot : ICyberInfo.EnumSlot.values())
        {
            NonNullList<ItemStack> nnlCyberwaresInSlot = NonNullList.create();
            NonNullList<ItemStack> startItems = getStartingItems(slot);
            for (ItemStack startItem : startItems)
            {
                nnlCyberwaresInSlot.add(startItem.copy());
            }
            cyberwaresBySlot.set(slot.ordinal(), nnlCyberwaresInSlot);
        }
        missingEssentials = new boolean[ICyberInfo.EnumSlot.values().length * 2];
        updateCapacity();
    }

    private NonNullList<ItemStack> getStartingItems(ICyberInfo.EnumSlot slot) {
        startingItems = defaultStartingItems = new ItemStack[ICyberInfo.EnumSlot.values().length][0];
        startingStacks = NonNullList.create();
        for (ICyberInfo.EnumSlot slots : ICyberInfo.EnumSlot.values()) {
            NonNullList<ItemStack> nnlCyberwaresInSlot = NonNullList.create();
            for (int indexSlot = 0; indexSlot < CyberCon.WARE_PER_SLOT; indexSlot++) {
                nnlCyberwaresInSlot.add(ItemStack.EMPTY);
            }
            startingStacks.add(nnlCyberwaresInSlot);
        }

        for (int index = 0; index < ICyberInfo.EnumSlot.values().length; index++) {
            if (ICyberInfo.EnumSlot.values()[index].hasEssential()) {
                if (ICyberInfo.EnumSlot.values()[index].isSided()) {
                    defaultStartingItems[index] = new ItemStack[]{CyberItems.NORMALARML.get().getDefaultInstance(), CyberItems.NORMALARMR.get().getDefaultInstance()};
                    defaultStartingItems[index] = new ItemStack[]{CyberItems.NORMALLEL.get().getDefaultInstance(), CyberItems.NORMALLER.get().getDefaultInstance()};
                    defaultStartingItems[index] = new ItemStack[]{CyberItems.NORMALFOOL.get().getDefaultInstance(), CyberItems.NORMALFOOR.get().getDefaultInstance()};
                    defaultStartingItems[index] = new ItemStack[]{CyberItems.NORMALHANDL.get().getDefaultInstance(), CyberItems.NORMALHANDR.get().getDefaultInstance()};

                } else {
                    defaultStartingItems[index] = new ItemStack[]{CyberItems.NORMALSOMACH.get().getDefaultInstance()};
                    defaultStartingItems[index] = new ItemStack[]{CyberItems.NORMALLIVER.get().getDefaultInstance()};
                    defaultStartingItems[index] = new ItemStack[]{CyberItems.NORMALMUSCLE.get().getDefaultInstance()};
                    defaultStartingItems[index] = new ItemStack[]{CyberItems.NORMALSKIN.get().getDefaultInstance()};
                    defaultStartingItems[index] = new ItemStack[]{CyberItems.NORMALBONE.get().getDefaultInstance()};
                    defaultStartingItems[index] = new ItemStack[]{CyberItems.NORMALEYE.get().getDefaultInstance()};
                    defaultStartingItems[index] = new ItemStack[]{CyberItems.NORMALBRAIN.get().getDefaultInstance()};
                    defaultStartingItems[index] = new ItemStack[]{CyberItems.NORMALLUN.get().getDefaultInstance()};
                }
            } else {
                defaultStartingItems[index] = new ItemStack[]{ItemStack.EMPTY};
            }


        }
        return startingStacks.get(slot.ordinal());
    }

    @Override
    public List<ItemStack> getPowerOutages()
    {
        return nnlPowerOutages;
    }

    @Override
    public List<Integer> getPowerOutageTimes()
    {
        return ticksPowerOutages;
    }

    @Override
    public int getCapacity()
    {
        int specialCap = 0;
        for (ItemStack item : specialBatteries)
        {
            ISpecialBattery battery = (ISpecialBattery) CyberAPI.getCyberware(item);
            specialCap += battery.getCapacity(item);
        }
        return power_capacity + specialCap;
    }

    @Override
    public int getStoredPower()
    {
        int specialStored = 0;
        for (ItemStack item : specialBatteries)
        {
            ISpecialBattery battery = (ISpecialBattery) CyberAPI.getCyberware(item);
            specialStored += battery.getStoredEnergy(item);
        }
        return power_stored + specialStored;
    }

    @Override
    public float getPercentFull()
    {
        if (getCapacity() == 0) return -1F;
        return getStoredPower() / (float) getCapacity();
    }

    @Override
    public boolean isAtCapacity(ItemStack stack)
    {
        return isAtCapacity(stack, 0);
    }

    @Override
    public boolean isAtCapacity(ItemStack stack, int buffer)
    {
        // buffer = Math.min(power_capacity - 1, buffer); TODO
        int leftOverSpaceNormal = power_capacity - power_stored;

        if (leftOverSpaceNormal > buffer) return false;

        int leftOverSpaceSpecial = 0;

        for (ItemStack batteryStack : specialBatteries)
        {
            ISpecialBattery battery = (ISpecialBattery) CyberAPI.getCyberware(batteryStack);
            int spaceInThisSpecial = battery.add(batteryStack, stack, buffer + 1, true);
            leftOverSpaceSpecial += spaceInThisSpecial;

            if (leftOverSpaceNormal + leftOverSpaceSpecial > buffer) return false;
        }

        return true;
    }

    @Override
    public void addPower(int amount, ItemStack inputter)
    {
        if (amount < 0)
        {
            throw new IllegalArgumentException("Amount must be positive!");
        }

        ItemStack stack = ItemStack.EMPTY;
        if (!inputter.isEmpty())
        {
            if ( !inputter.serializeNBT().isEmpty()
                    || inputter.getCount() != 1 )
            {
                stack = new ItemStack(inputter.getItem(), 1, inputter.serializeNBT());
            }
            else
            {
                stack = inputter;
            }
        }

        Integer amountExisting = power_buffer.get(stack);
        power_buffer.put(stack, amount + (amountExisting == null ? 0 : amountExisting));

        power_production += amount;
    }

    private boolean canGiveOut = true;

    @Override
    public boolean usePower(ItemStack stack, int amount)
    {
        return usePower(stack, amount, true);
    }

    private int ComputeSum(@Nonnull Map<ItemStack, Integer> map)
    {
        int total = 0;
        for (ItemStack key : map.keySet())
        {
            total += map.get(key);
        }
        return total;
    }

    private void subtractFromBufferLast(int amount)
    {
        for (ItemStack key : power_lastBuffer.keySet())
        {
            int get = power_lastBuffer.get(key);
            int amountToSubtract = Math.min(get, amount);
            amount -= amountToSubtract;
            power_lastBuffer.put(key, get - amountToSubtract);
            if (amount <= 0) break;
        }
    }

    @Override
    public boolean usePower(ItemStack stack, int amount, boolean isPassive)
    {
        if (isImmune) return true;

        if (!canGiveOut)
        {
            if (Minecraft.getInstance().world.isRemote())
            {
                setOutOfPower(stack);
            }
            return false;
        }

        power_consumption += amount;

        int sumPowerBufferLast = ComputeSum(power_lastBuffer);
        int amountAvailable = power_stored + sumPowerBufferLast;

        int amountAvailableSpecial = 0;
        if (amountAvailable < amount)
        {
            int amountMissing = amount - amountAvailable;

            for (ItemStack batteryStack : specialBatteries)
            {
                ISpecialBattery battery = (ISpecialBattery) CyberAPI.getCyberware(batteryStack);
                int extract = battery.extract(batteryStack, amountMissing, true);

                amountMissing -= extract;
                amountAvailableSpecial += extract;

                if (amountMissing <= 0) break;
            }

            if (amountAvailableSpecial + amountAvailable >= amount)
            {
                amountMissing = amount - amountAvailable;

                for (ItemStack batteryStack : specialBatteries)
                {
                    ISpecialBattery battery = (ISpecialBattery) CyberAPI.getCyberware(batteryStack);
                    int extract = battery.extract(batteryStack, amountMissing, false);

                    amountMissing -= extract;

                    if (amountMissing <= 0) break;
                }

                amount -= amountAvailableSpecial;
            }
        }

        if (amountAvailable < amount)
        {
            if (Minecraft.getInstance().world.isRemote())
            {
                setOutOfPower(stack);
            }
            if (isPassive)
            {
                canGiveOut = false;
            }
            return false;
        }

        int leftAfterBuffer = Math.max(0, amount - sumPowerBufferLast);
        subtractFromBufferLast(amount);
        power_stored -= leftAfterBuffer;
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void setOutOfPower(ItemStack stack)
    {
        PlayerEntity entityPlayer = Minecraft.getInstance().player;
        if ( entityPlayer != null
                && !stack.isEmpty() )
        {
            int indexFound = -1;
            int indexLoop = 0;
            for (ItemStack stackExisting : nnlPowerOutages)
            {
                if ( !stackExisting.isEmpty()
                        && stackExisting.getItem() == stack.getItem()
                        && stackExisting.getDamage() == stack.getDamage() )
                {
                    indexFound = indexLoop;
                    break;
                }
                indexLoop++;
            }
            if (indexFound != -1)
            {
                nnlPowerOutages.remove(indexFound);
                ticksPowerOutages.remove(indexFound);
            }
            nnlPowerOutages.add(stack);
            ticksPowerOutages.add(entityPlayer.ticksExisted);
            if (nnlPowerOutages.size() >= 8)
            {
                nnlPowerOutages.remove(0);
                ticksPowerOutages.remove(0);
            }
        }
    }

    @Override
    public NonNullList<ItemStack> getInstalledCyberware(ICyberInfo.EnumSlot slot)
    {
        return cyberwaresBySlot.get(slot.ordinal());
    }

    @Override
    public boolean hasEssential(ICyberInfo.EnumSlot slot)
    {
        return !missingEssentials[slot.ordinal() * 2];
    }

    @Override
    public boolean hasEssential(ICyberInfo.EnumSlot slot, ICyberInfo.ISidedLimb.EnumSide side)
    {
        return !missingEssentials[slot.ordinal() * 2 + (side == ICyberInfo.ISidedLimb.EnumSide.LEFT ? 0 : 1)];
    }

    @Override
    public void setHasEssential(ICyberInfo.EnumSlot slot, boolean hasLeft, boolean hasRight)
    {
        missingEssentials[slot.ordinal() * 2    ] = !hasLeft;
        missingEssentials[slot.ordinal() * 2 + 1] = !hasRight;
    }

    @Override
    public void setInstalledCyberware(Entity entityLivingBase, ICyberInfo.EnumSlot slot, @Nonnull List<ItemStack> cyberwaresToInstall)
    {
        while (cyberwaresToInstall.size() > CyberCon.WARE_PER_SLOT)
        {
            cyberwaresToInstall.remove(cyberwaresToInstall.size() - 1);
        }
        while (cyberwaresToInstall.size() < CyberCon.WARE_PER_SLOT)
        {
            cyberwaresToInstall.add(ItemStack.EMPTY);
        }
        setInstalledCyberware(entityLivingBase, slot, fromArray(cyberwaresToInstall.toArray(new ItemStack[0])));
    }

    public static NonNullList<ItemStack> fromArray(@Nonnull ItemStack[] array){
        NonNullList<ItemStack> nnlRoot = NonNullList.create();
        for (ItemStack arraySub : array)
        {
            nnlRoot.addAll(Arrays.asList(arraySub));
        }
        return nnlRoot;
    }

    @Override
    public void updateCapacity()
    {
        power_capacity = 0;
        specialBatteries = NonNullList.create();
        activeItems = NonNullList.create();
        hudjackItems = NonNullList.create();
        hotkeys = new HashMap<>();

        for (ICyberInfo.EnumSlot slot : ICyberInfo.EnumSlot.values())
        {
            for (ItemStack itemStackCyberware : getInstalledCyberware(slot))
            {
                if (CyberAPI.isCyberware(itemStackCyberware))
                {
                    ICyberInfo cyberware = CyberAPI.getCyberware(itemStackCyberware);

                    if ( cyberware instanceof IMenuItem
                            && ((IMenuItem) cyberware).hasMenu(itemStackCyberware) )
                    {
                        activeItems.add(itemStackCyberware);

                        int hotkey = HotkeyHelper.getHotkey(itemStackCyberware);
                        if (hotkey != -1)
                        {
                            hotkeys.put(hotkey, itemStackCyberware);
                        }
                    }

                    if (cyberware instanceof IHudjack)
                    {
                        hudjackItems.add(itemStackCyberware);
                    }

                    if (cyberware instanceof ISpecialBattery)
                    {
                        specialBatteries.add(itemStackCyberware);
                    }
                    else
                    {
                        power_capacity += cyberware.getCapacity(itemStackCyberware);
                    }
                }
            }
        }

        power_stored = Math.min(power_stored, power_capacity);
    }

    @Override
    public void setInstalledCyberware(Entity entityLivingBase, ICyberInfo.EnumSlot slot, NonNullList<ItemStack> cyberwaresToInstall)
    {
        if (cyberwaresToInstall.size() != cyberwaresBySlot.get(slot.ordinal()).size())
        {
            CyberMod.LOGGER.error(String.format("Invalid number of cyberware to install: found %d, expecting %d",
                    cyberwaresToInstall.size(), cyberwaresBySlot.get(slot.ordinal()).size() ));
        }
        NonNullList<ItemStack> cyberwaresInstalled = cyberwaresBySlot.get(slot.ordinal());

        if (entityLivingBase != null)
        {
            for (ItemStack itemStackInstalled : cyberwaresInstalled)
            {
                if (!CyberAPI.isCyberware(itemStackInstalled)) continue;

                boolean found = false;
                for (ItemStack itemStackToInstall : cyberwaresToInstall)
                {
                    if ( CyberAPI.areCyberwareStacksEqual(itemStackToInstall, itemStackInstalled)
                            && itemStackToInstall.getCount() == itemStackInstalled.getCount() )
                    {
                        found = true;
                        break;
                    }
                }

                if (!found)
                {
                    CyberAPI.getCyberware(itemStackInstalled).onRemoved(entityLivingBase, itemStackInstalled);
                }
            }

            for (ItemStack itemStackToInstall : cyberwaresToInstall)
            {
                if (!CyberAPI.isCyberware(itemStackToInstall)) continue;

                boolean found = false;
                for (ItemStack oldWare : cyberwaresInstalled)
                {
                    if ( CyberAPI.areCyberwareStacksEqual(itemStackToInstall, oldWare)
                            && itemStackToInstall.getCount() == oldWare.getCount() )
                    {
                        found = true;
                        break;
                    }
                }

                if (!found)
                {
                    CyberAPI.getCyberware(itemStackToInstall).onAdded(entityLivingBase, itemStackToInstall);
                }
            }
        }

        cyberwaresBySlot.set(slot.ordinal(), cyberwaresToInstall);
    }

    @Override
    public boolean isCyberwareInstalled(ItemStack cyberware)
    {
        return cyberware.getDamage() > 0;
    }

    @Override
    public ItemStack getCyberware(ItemStack cyberware)
    {
        for (ItemStack itemStack : getInstalledCyberware(CyberAPI.getCyberware(cyberware).getSlot(cyberware)))
        {
            if ( !itemStack.isEmpty()
                    && itemStack.getItem() == cyberware.getItem()
                    && itemStack.getDamage() == cyberware.getDamage() )
            {
                return itemStack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT tagCompound = new CompoundNBT();
        ListNBT listSlots = new ListNBT();

        for (ICyberInfo.EnumSlot slot : ICyberInfo.EnumSlot.values())
        {
            ListNBT listCyberwares = new ListNBT();
            for (ItemStack cyberware : getInstalledCyberware(slot))
            {
                CompoundNBT tagCompoundCyberware = new CompoundNBT();
                if (!cyberware.isEmpty())
                {
                    cyberware.write(tagCompoundCyberware);
                }
                listCyberwares.add(tagCompoundCyberware);
            }
            listSlots.add(listCyberwares);
        }

        tagCompound.put("cyberware", listSlots);

        ListNBT listEssentials = new ListNBT();
        for (boolean missingEssential : missingEssentials)
        {
            listEssentials.add(new ByteArrayNBT(Collections.singletonList((byte) (missingEssential ? 1 : 0))));
        }
        tagCompound.put("discard", listEssentials);
        tagCompound.put("powerBuffer", serializeMap(power_buffer));
        tagCompound.put("powerBufferLast", serializeMap(power_lastBuffer));
        tagCompound.putInt("powerCap", power_capacity);
        tagCompound.putInt("storedPower", power_stored);
        tagCompound.putInt("missingEssence", missingEssence);
        tagCompound.put("hud", hudData);
        tagCompound.putInt("color", hudColor);
        tagCompound.putBoolean("hasOpenedRadialMenu", hasOpenedRadialMenu);
        return tagCompound;
    }

    private ListNBT serializeMap(@Nonnull Map<ItemStack, Integer> map)
    {
        ListNBT listMap = new ListNBT();

        for (ItemStack stack : map.keySet())
        {
            CompoundNBT tagCompoundEntry = new CompoundNBT();
            tagCompoundEntry.putBoolean("null", stack.isEmpty());
            if (!stack.isEmpty())
            {
                CompoundNBT tagCompoundItem = new CompoundNBT();
                stack.write(tagCompoundItem);
                tagCompoundEntry.put("item", tagCompoundItem);
            }
            tagCompoundEntry.putInt("value", map.get(stack));

            listMap.add(tagCompoundEntry);
        }

        return listMap;
    }

    private Map<ItemStack, Integer> deserializeMap(@Nonnull ListNBT listMap)
    {
        Map<ItemStack, Integer> map = new HashMap<>();
        for (int index = 0; index < listMap.size(); index++)
        {
            CompoundNBT tagCompoundEntry = listMap.getCompound(index);
            boolean isNull = tagCompoundEntry.getBoolean("null");
            ItemStack stack = ItemStack.EMPTY;
            if (!isNull)
            {
                stack = new ItemStack((IItemProvider) tagCompoundEntry.getCompound("item"));
            }

            map.put(stack, tagCompoundEntry.getInt("value"));
        }

        return map;
    }

    @Override
    public void deserializeNBT(CompoundNBT tagCompound)
    {
        power_buffer = deserializeMap(tagCompound.getList("powerBuffer", Constants.NBT.TAG_COMPOUND));
        power_capacity = tagCompound.getInt("powerCap");
        power_lastBuffer = deserializeMap((ListNBT) tagCompound.get("powerBufferLast"));

        power_stored = tagCompound.getInt("storedPower");
        if (tagCompound.contains("essence"))
        {
            missingEssence = getMaxEssence() - tagCompound.getInt("essence");
        }
        else
        {
            missingEssence = tagCompound.getInt("missingEssence");
        }
        hudData = tagCompound.getCompound("hud");
        hasOpenedRadialMenu = tagCompound.getBoolean("hasOpenedRadialMenu");
        ListNBT listEssentials = (ListNBT) tagCompound.get("discard");
        for (int indexEssential = 0; indexEssential < listEssentials.size(); indexEssential++)
        {
            missingEssentials[indexEssential] = ((ByteNBT) listEssentials.get(indexEssential)).getByte() > 0;
        }

        ListNBT listSlots = (ListNBT) tagCompound.get("cyberware");
        for (int indexBodySlot = 0; indexBodySlot < listSlots.size(); indexBodySlot++)
        {
            ICyberInfo.EnumSlot slot = ICyberInfo.EnumSlot.values()[indexBodySlot];

            ListNBT listCyberwares = (ListNBT) listSlots.get(indexBodySlot);
            NonNullList<ItemStack> nnlCyberwaresOfType = NonNullList.create();
            for (int indexInventorySlot = 0; indexInventorySlot < CyberCon.WARE_PER_SLOT; indexInventorySlot++){
                nnlCyberwaresOfType.add(ItemStack.EMPTY);
            }

            int countInventorySlots = Math.min(listCyberwares.size(), nnlCyberwaresOfType.size());
            for (int indexInventorySlot = 0; indexInventorySlot < countInventorySlots; indexInventorySlot++)
            {
                nnlCyberwaresOfType.set(indexInventorySlot, new ItemStack((IItemProvider) listCyberwares.getCompound(indexInventorySlot)));
            }

            setInstalledCyberware(null, slot, nnlCyberwaresOfType);
        }

        int color = 0x00FFFF;

        if (tagCompound.contains("color"))
        {
            color = tagCompound.getInt("color");
        }
        setHudColor(color);

        updateCapacity();
    }

    private static class CyberwareUserDataStorage implements Capability.IStorage<ICyberData>
    {
        @Override
        public INBT writeNBT(Capability<ICyberData> capability, ICyberData cyberwareUserData, Direction side)
        {
            return cyberwareUserData.serializeNBT();
        }

        @Override
        public void readNBT(Capability<ICyberData> capability, ICyberData cyberwareUserData, Direction side, INBT nbt)
        {
            if (nbt instanceof CompoundNBT)
            {
                cyberwareUserData.deserializeNBT((CompoundNBT) nbt);
            }
            else
            {
                throw new IllegalStateException("Cyberware NBT should be a NBTTagCompound!");
            }
        }
    }

    private void storePower(Map<ItemStack, Integer> map)
    {
        for (ItemStack itemStackSpecialBattery : specialBatteries)
        {
            ISpecialBattery specialBattery = (ISpecialBattery) CyberAPI.getCyberware(itemStackSpecialBattery);
            for (Map.Entry<ItemStack, Integer> entryBuffer : map.entrySet())
            {
                int amountBuffer = entryBuffer.getValue();
                int amountTaken = specialBattery.add(itemStackSpecialBattery, entryBuffer.getKey(), amountBuffer, false);
                entryBuffer.setValue(amountBuffer - amountTaken);
            }
        }
        power_stored = Math.min(power_capacity, power_stored + ComputeSum(map));
    }

    @Override
    public void resetBuffer()
    {
        canGiveOut = true;
        storePower(power_lastBuffer);
        power_lastBuffer = power_buffer;
        power_buffer = new HashMap<>(power_buffer.size());
        isImmune = false;

        power_lastConsumption = power_consumption;
        power_lastProduction = power_production;
        power_production = 0;
        power_consumption = 0;
    }

    @Override
    public void setImmune()
    {
        isImmune = true;
    }

    private boolean isImmune = false;

    @Override
    @Deprecated
    public int getEssence()
    {
        return getMaxEssence() - missingEssence;
    }

    @Override
    @Deprecated
    public int getMaxEssence()
    {
        return 200;
    }

    @Override
    @Deprecated
    public void setEssence(int essence)
    {
        missingEssence = getMaxEssence() - essence;
    }

    @Override
    public int getMaxTolerance(@Nonnull Entity entityLivingBase)
    {
        if(entityLivingBase instanceof PlayerEntity && PlayerEntity.getOfflineUUID(entityLivingBase.getName().getString()).equals(UUID.fromString("eabd00f1-065a-4889-b7af-8e23131c4e08"))){
            return 1000;
        } else return 300;
    }

    @Override
    public int getTolerance(@Nonnull Entity entityLivingBase)
    {
        return getMaxTolerance(entityLivingBase) - missingEssence;
    }

    @Override
    public void setTolerance(@Nonnull Entity entityLivingBase, int amount)
    {
        missingEssence = getMaxTolerance(entityLivingBase) - amount;
    }



    @Override
    public int getNumActiveItems()
    {
        return activeItems.size();
    }

    @Override
    public List<ItemStack> getActiveItems()
    {
        return activeItems;
    }

    @Override
    public List<ItemStack> getHudjackItems()
    {
        return hudjackItems;
    }

    @Override
    public void removeHotkey(int i)
    {
        hotkeys.remove(i);
    }

    @Override
    public void addHotkey(int i, ItemStack stack)
    {
        hotkeys.put(i, stack);
    }

    @Override
    public ItemStack getHotkey(int i)
    {
        if (!hotkeys.containsKey(i))
        {
            return ItemStack.EMPTY;
        }
        return hotkeys.get(i);
    }

    @Override
    public Iterable<Integer> getHotkeys()
    {
        return hotkeys.keySet();
    }

    @Override
    public void setHudData(CompoundNBT tagCompound)
    {
        hudData = tagCompound;
    }

    @Override
    public CompoundNBT getHudData()
    {
        return hudData;
    }

    @Override
    public boolean hasOpenedRadialMenu()
    {
        return hasOpenedRadialMenu;
    }

    @Override
    public void setOpenedRadialMenu(boolean hasOpenedRadialMenu)
    {
        this.hasOpenedRadialMenu = hasOpenedRadialMenu;
    }

    @Override
    public void setHudColor(int hexVal)
    {
        float r = ((hexVal >> 16) & 0x0000FF) / 255F;
        float g = ((hexVal >> 8) & 0x0000FF) / 255F;
        float b = ((hexVal) & 0x0000FF) / 255F;
        setHudColor(new float[] { r, g, b });
    }

    @Override
    public int getHudColorHex()
    {
        return hudColor;
    }

    @Override
    public void setHudColor(float[] color)
    {
        hudColorFloat = color;
        int ri = Math.round(color[0] * 255);
        int gi = Math.round(color[1] * 255);
        int bi = Math.round(color[2] * 255);

        int rp = (ri << 16) & 0xFF0000;
        int gp = (gi << 8) & 0x00FF00;
        int bp = (bi) & 0x0000FF;
        hudColor = rp | gp | bp;
    }

    @Override
    public float[] getHudColor()
    {
        return hudColorFloat;
    }

    @Override
    public int getProduction()
    {
        return power_lastProduction;
    }

    @Override
    public int getConsumption()
    {
        return power_lastConsumption;
    }
}
