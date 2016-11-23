package mchorse.metamorph.api.morph;

import java.util.ArrayList;
import java.util.List;

import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import mchorse.metamorph.entity.EntityMorph;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

/**
 * Server event handler
 * 
 * This event handler (or rather listener) is responsible for morphings. In 
 * essence, there are few things going on over here:
 * 
 * 1. Acquiring morphs from killed entities
 * 2. Grant additional attack effect while morphed (more damage, explosions, 
 *    potion effects, etc.)
 * 3. Update morphs in the player's loop 
 */
public class MorphHandler
{
    public static List<Runnable> FUTURE_TASKS_CLIENT = new ArrayList<Runnable>();
    public static List<Runnable> FUTURE_TASKS_SERVER = new ArrayList<Runnable>();

    /**
     * When a player kills an entity, he gains a morph based on the properties 
     * of this entity.
     * 
     * I think I need to implement some mechanism or a callback to map some 
     * entities with the same name onto different morphs. 
     */
    @SubscribeEvent
    public void onPlayerKillEntity(LivingDeathEvent event)
    {
        Entity source = event.getSource().getEntity();
        Entity target = event.getEntity();

        if (target.worldObj.isRemote || !(source instanceof EntityPlayer) || target instanceof EntityPlayer)
        {
            return;
        }

        EntityPlayer player = (EntityPlayer) source;
        IMorphing capability = Morphing.get(player);

        if (capability == null)
        {
            return;
        }

        String morph = MorphManager.INSTANCE.morphNameFromEntity(target);

        if (!MorphManager.INSTANCE.morphs.containsKey(morph))
        {
            System.out.println("Morph by key '" + morph + "' doesn't exist!");

            return;
        }

        if (!player.worldObj.isRemote)
        {
            EntityMorph morphEntity = new EntityMorph(player.worldObj, player.getUniqueID(), morph);

            morphEntity.setPositionAndRotation(target.posX, target.posY + target.height / 2, target.posZ, target.rotationYaw, target.rotationPitch);
            player.worldObj.spawnEntityInWorld(morphEntity);
        }
    }

    /**
     * When player is morphed, he can deal an damage or effect onto the enemy. 
     * 
     * For example, if player is morphed into the wither skeleton, he also 
     * grants a target wither potion effect. Pretty realistic, however I don't 
     * really know what that does. 
     */
    @SubscribeEvent
    public void onPlayerAttack(LivingAttackEvent event)
    {
        Entity source = event.getSource().getEntity();
        Entity target = event.getEntity();

        if (source instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) source;
            IMorphing capability = Morphing.get(player);

            if (capability == null || !capability.isMorphed())
            {
                return;
            }

            capability.getCurrentMorph().attack(target, player);
        }
    }

    /**
     * When player is morphed, its morphing abilities are executed over here.
     * 
     * Stuff like gliding, allergies, climbing, swiming and other stuff are 
     * get applied on the player over here.
     */
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.phase == Phase.START)
        {
            return;
        }

        EntityPlayer player = event.player;
        IMorphing capability = Morphing.get(player);

        this.runFutureTasks(player);

        if (capability == null || !capability.isMorphed())
        {
            /* Restore default eye height */
            player.eyeHeight = player.getDefaultEyeHeight();

            return;
        }

        capability.getCurrentMorph().update(player, capability);
    }

    /**
     * Run future tasks on both client and server. 
     */
    private void runFutureTasks(EntityPlayer player)
    {
        if (player.worldObj.isRemote && !FUTURE_TASKS_CLIENT.isEmpty())
        {
            FUTURE_TASKS_CLIENT.remove(0).run();
        }

        if (!player.worldObj.isRemote && !FUTURE_TASKS_SERVER.isEmpty())
        {
            FUTURE_TASKS_SERVER.remove(0).run();
        }
    }
}