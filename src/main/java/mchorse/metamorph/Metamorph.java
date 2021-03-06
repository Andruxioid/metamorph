package mchorse.metamorph;

import mchorse.mclib.McLib;
import mchorse.mclib.config.ConfigBuilder;
import mchorse.mclib.config.values.ValueBoolean;
import mchorse.mclib.events.RegisterConfigEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.commands.CommandAcquireMorph;
import mchorse.metamorph.commands.CommandMetamorph;
import mchorse.metamorph.commands.CommandMorph;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * Metamorph mod
 * 
 * This mod provides functionality for survival morphing. To gain a morph 
 * you have to kill a mob. Once you killed it, you gain its morphing. Once you 
 * gained its morphing you can use special menu to select a mob into which to 
 * morph.
 * 
 * Except different shape, you gain also special abilities for specific mobs. 
 * In creative you can access all morphings.
 * 
 * Inspired by Morph and Shape Shifter Z mods (mostly due to the fact that 
 * they're outdated), however, iChun saying that he's working on Morph for 
 * 1.10.2, this is really exciting! :D
 */
@Mod(modid = Metamorph.MOD_ID, name = Metamorph.MODNAME, version = Metamorph.VERSION, updateJSON = "https://raw.githubusercontent.com/mchorse/metamorph/master/version.json", dependencies = "after:moreplayermodels;required-after:mclib@[%MCLIB%,)")
public class Metamorph
{
    /* Metadata fields */
    public static final String MOD_ID = "metamorph";
    public static final String MODNAME = "Metamorph";
    public static final String VERSION = "%VERSION%";

    public static final String CLIENT_PROXY = "mchorse.metamorph.ClientProxy";
    public static final String SERVER_PROXY = "mchorse.metamorph.CommonProxy";

    /* Forge stuff classes */
    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static CommonProxy proxy;

    @Mod.Instance(MOD_ID)
    public static Metamorph instance;

    /**
     * Custom payload channel 
     */
    public static FMLEventChannel channel;

    /* Metamorph configuration */
    public static ValueBoolean preventGhosts;
    public static ValueBoolean preventKillAcquire;
    public static ValueBoolean acquireImmediately;

    public static ValueBoolean keepMorphs;
    public static ValueBoolean disablePov;
    public static ValueBoolean disableHealth;
    public static ValueBoolean disableMorphAnimation;
    public static ValueBoolean disableMorphDisguise;
    public static ValueBoolean disableFirstPersonHand;
    public static ValueBoolean morphInTightSpaces;
    public static ValueBoolean showMorphIdleSounds;
    public static ValueBoolean pauseGUIInSP;

    /* Events */

    @SubscribeEvent
    public void onConfigRegister(RegisterConfigEvent event)
    {
        ConfigBuilder builder = event.createBuilder(MOD_ID);

        this.preventGhosts = builder.category("acquiring").getBoolean("prevent_ghosts", true);
        this.preventKillAcquire = builder.getBoolean("prevent_kill_acquire", false);
        this.acquireImmediately = builder.getBoolean("acquire_immediately", false);

        this.keepMorphs = builder.category("morphs").getBoolean("keep_morphs", true);
        this.disablePov = builder.getBoolean("disable_pov", false);
        this.disableHealth = builder.getBoolean("disable_health", false);
        this.disableMorphAnimation = builder.getBoolean("disable_morph_animation", false);
        this.disableMorphDisguise = builder.getBoolean("disable_morph_disguise", false);
        this.disableFirstPersonHand = builder.getBoolean("disable_first_person_hand", false);
        this.morphInTightSpaces = builder.getBoolean("morph_in_tight_spaces", false);
        this.showMorphIdleSounds = builder.getBoolean("show_morph_idle_sounds", true);
        this.pauseGUIInSP = builder.getBoolean("pause_gui_in_sp", true);

        event.modules.add(builder.build());
    }

    @EventHandler
    public void preLoad(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();
        channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("Metamorph");
        McLib.EVENT_BUS.register(this);

        proxy.preLoad(event);
    }

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        proxy.load();
    }

    @EventHandler
    public void postLoad(FMLPostInitializationEvent event)
    {
        proxy.postLoad(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        /* Setting up the blacklist */
        MorphManager.INSTANCE.setActiveBlacklist(MorphUtils.reloadBlacklist());
        MorphManager.INSTANCE.setActiveSettings(MorphUtils.reloadMorphSettings());
        MorphManager.INSTANCE.setActiveMap(MorphUtils.reloadRemapper());

        /* Register commands */
        event.registerServerCommand(new CommandMorph());
        event.registerServerCommand(new CommandAcquireMorph());
        event.registerServerCommand(new CommandMetamorph());
    }

    /* Logging */

    /* TODO: Set to false when publishing and remove all unnecessary printlns */
    public static boolean DEBUG = false;
    public static Logger LOGGER;

    /**
     * Log out the message if in DEBUG mode.
     * 
     * But I always forget to turn it off before releasing the mod.
     */
    public static void log(String message)
    {
        if (DEBUG)
        {
            LOGGER.log(Level.INFO, message);
        }
    }
}