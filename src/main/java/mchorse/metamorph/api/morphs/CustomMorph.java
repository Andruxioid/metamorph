package mchorse.metamorph.api.morphs;

import com.google.common.base.Objects;

import mchorse.metamorph.api.EntityUtils;
import mchorse.metamorph.api.models.Model;
import mchorse.metamorph.api.models.Model.Pose;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.client.gui.utils.GuiUtils;
import mchorse.metamorph.client.model.ModelCustom;
import mchorse.metamorph.client.render.RenderCustomModel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Custom morph class
 * 
 * This morph subclass is responsible for updating custom model based morphs
 */
public class CustomMorph extends AbstractMorph
{
    /**
     * Morph's model
     */
    public Model model;

    /**
     * Current pose 
     */
    protected Pose pose;

    /**
     * Current custom pose
     */
    public String currentPose = "";

    /**
     * Apply current pose on sneaking
     */
    public boolean currentPoseOnSneak = false;

    /* Rendering */

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOnScreen(EntityPlayer player, int x, int y, float scale, float alpha)
    {
        ModelCustom model = ModelCustom.MODELS.get(this.name);

        if (model != null)
        {
            Model data = model.model;

            if (data != null && data.defaultTexture != null)
            {
                model.pose = this.pose == null ? model.model.poses.get("standing") : this.pose;
                model.swingProgress = 0;

                Minecraft.getMinecraft().renderEngine.bindTexture(data.defaultTexture);
                GuiUtils.drawModel(model, player, x, y, scale, alpha);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderHand(EntityPlayer player, EnumHand hand)
    {
        if (this.renderer == null || !(this.renderer instanceof RenderCustomModel))
        {
            return false;
        }

        RenderCustomModel renderer = (RenderCustomModel) this.renderer;

        renderer.setupModel(player);

        if (renderer.getMainModel() == null)
        {
            return false;
        }

        if (hand.equals(EnumHand.MAIN_HAND))
        {
            renderer.renderRightArm(player);
        }
        else
        {
            renderer.renderLeftArm(player);
        }

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        RenderCustomModel render = (RenderCustomModel) this.renderer;

        render.current = this;
        render.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    /* Updating and stuff */

    /**
     * Get a pose for rendering
     */
    public Pose getPose(EntityLivingBase target)
    {
        String poseName = EntityUtils.getPose(target, this.currentPose, this.currentPoseOnSneak);

        return model.getPose(poseName);
    }

    public void setPose(Pose pose)
    {
        this.pose = pose;
    }

    /**
     * Update the player based on its morph abilities and properties. This 
     * method also responsible for updating AABB size. 
     */
    @Override
    public void update(EntityLivingBase target, IMorphing cap)
    {
        this.updateSize(target, cap);

        super.update(target, cap);
    }

    /**
     * Update size of the player based on the given morph.
     */
    public void updateSize(EntityLivingBase target, IMorphing cap)
    {
        String poseName = EntityUtils.getPose(target, this.currentPose, this.currentPoseOnSneak);

        this.pose = model.getPose(poseName);

        if (this.pose != null)
        {
            float[] pose = this.pose.size;

            this.updateSize(target, pose[0], pose[1]);
        }
    }

    /**
     * Clone this {@link CustomMorph} 
     */
    @Override
    public AbstractMorph clone(boolean isRemote)
    {
        CustomMorph morph = new CustomMorph();

        morph.currentPose = this.currentPose;
        morph.currentPoseOnSneak = this.currentPoseOnSneak;

        morph.name = this.name;
        morph.settings = settings;

        morph.model = this.model;

        if (isRemote)
        {
            morph.renderer = this.renderer;
        }

        return morph;
    }

    @Override
    public float getWidth(EntityLivingBase target)
    {
        return this.pose != null ? this.pose.size[0] : 0.6F;
    }

    @Override
    public float getHeight(EntityLivingBase target)
    {
        return this.pose != null ? this.pose.size[1] : 1.8F;
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean result = super.equals(obj);

        if (obj instanceof CustomMorph)
        {
            CustomMorph morph = (CustomMorph) obj;

            result = result && Objects.equal(this.currentPose, morph.currentPose);
            result = result && this.currentPoseOnSneak == morph.currentPoseOnSneak;
        }

        return result;
    }

    @Override
    public void toNBT(NBTTagCompound tag)
    {
        super.toNBT(tag);

        tag.setString("Pose", this.currentPose);
        tag.setBoolean("Sneak", this.currentPoseOnSneak);
    }

    @Override
    public void fromNBT(NBTTagCompound tag)
    {
        super.fromNBT(tag);

        this.currentPose = tag.getString("Pose");
        this.currentPoseOnSneak = tag.getBoolean("Sneak");
    }
}