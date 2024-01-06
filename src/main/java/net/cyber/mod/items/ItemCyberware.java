package net.cyber.mod.items;

import net.cyber.mod.helper.CyberPartEnum;
import net.cyber.mod.helper.CyberPartType;
import net.cyber.mod.helper.ICyberPart;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class ItemCyberware extends Item implements ICyberPart {
    public ItemCyberware(Properties properties) {
        super(properties);
    }
    AttributeModifier modifier = new AttributeModifier(UUID.fromString("47d50ba2-eff2-4a91-b12b-a1cb95f223c6"),"cyber_main", 2, AttributeModifier.Operation.ADDITION);

    public CyberPartEnum cat;
    public CyberPartType type;
    public int essence;

    public ItemCyberware(Properties properties, CyberPartEnum cat, CyberPartType type, int essenceCost){
        super(properties);
        this.cat = cat;
        this.type = type;
        this.essence = essenceCost;
    }

    public ItemCyberware(Properties properties, CyberPartEnum cat, int essenceCost){
        super(properties);
        this.cat = cat;
        this.type = CyberPartType.UPGRADE;
        this.essence = essenceCost;
    }

    @Override
    public void setEssenceCost(int essence)
    {
        this.essence = essence;
    }

    @Override
    public int getEssenceCost()
    {
        return essence;
    }

    @Override
    public CyberPartEnum getCategory() {
        return this.cat;
    }

    @Override
    public CyberPartType getType() {
        return type;
    }

    @Override
    public void runOnce(PlayerEntity player) {
        ModifiableAttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        //get attribute modif by id
        AttributeModifier oldHealthModifier = healthAttribute.getModifier(modifier.getID());
        //what is our value
        double addedHealth = (oldHealthModifier == null) ? +2.0D : oldHealthModifier.getAmount() + 2.0D;
        //replace the modifier on the main attribute
        healthAttribute.removeModifier(modifier.getID());
        AttributeModifier healthModifier = new AttributeModifier(modifier.getID(), "HP Bonus from Cyberware", addedHealth, AttributeModifier.Operation.ADDITION);
        healthAttribute.applyPersistentModifier(healthModifier);
    }

    @Override
    public void runOnceUndo(PlayerEntity player) {
        ModifiableAttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        //get attribute modif by id
        AttributeModifier oldHealthModifier = healthAttribute.getModifier(modifier.getID());
        //what is our value
        double addedHealth = (oldHealthModifier == null) ? -2.0D : oldHealthModifier.getAmount() - 2.0D;
        //replace the modifier on the main attribute
        healthAttribute.removeModifier(modifier.getID());
        AttributeModifier healthModifier = new AttributeModifier(modifier.getID(), "HP Drain from Cyberware", addedHealth, AttributeModifier.Operation.ADDITION);
        healthAttribute.applyPersistentModifier(healthModifier);
    }

    @Override
    public void runOnTick(PlayerEntity player) {

    }
}
