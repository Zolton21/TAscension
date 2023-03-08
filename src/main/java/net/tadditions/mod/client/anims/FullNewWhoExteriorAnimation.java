package net.tadditions.mod.client.anims;

import net.minecraft.util.math.MathHelper;
import net.tardis.mod.client.animation.ExteriorAnimation;
import net.tardis.mod.client.animation.ExteriorAnimationEntry;
import net.tardis.mod.enums.EnumMatterState;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;

public class FullNewWhoExteriorAnimation extends ExteriorAnimation {

    private float alpha = 1.0F;

    private int pulseTime = 20; //How long each pulse lasts
    private int pulses = 0;
	    
    public FullNewWhoExteriorAnimation(ExteriorAnimationEntry entry, ExteriorTile exterior) {
		super(entry, exterior);
	}

    @Override
	public void tick(int timeLeft) {
    	
        if(this.exterior.getMatterState() == EnumMatterState.DEMAT && timeLeft >= this.getMaxTime() - 213)
            this.setAlpha(1.0F - this.animateRemat(timeLeft));
        else if(timeLeft <= this.getMaxTime() - 213)
            this.setAlpha(this.animateRemat(timeLeft));
    }

    public float animateRemat(int timeLeft){

        if(timeLeft != this.getMaxTime() && timeLeft % pulseTime == 0){
            ++pulses;
        }

        float pulseAmount = pulses / (float)this.calcMaxPulses();
        return (pulseAmount) + this.pulseWithIntensity(0.4F, timeLeft * 0.175F);

    }

    public void setAlpha(float alpha){
        this.alpha = MathHelper.clamp(alpha, 0.0F, 1.0F);
    }

    public int calcMaxPulses(){
        return (int)Math.floor(this.getMaxTime() / (double)this.pulseTime);
    }

    public float pulseWithIntensity(float intensity, float frequency){
        return (float) Math.cos(frequency) * intensity;
    }

    @Override
    public float getAlpha() {
        return this.alpha;
    }


    @Override
    public void reset() {
        this.alpha = 0.0F;
        this.pulses = 0;
    }
}
