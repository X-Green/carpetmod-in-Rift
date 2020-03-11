package carpet;

import carpet.commands.*;
import carpet.logging.LoggerRegistry;
import carpet.settings.CarpetSettings;
import carpet.utils.HUDController;
import carpet.helpers.TickSpeed;

import java.util.Random;

import carpet.settings.SettingsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;

public class CarpetServer // static for now - easier to handle all around the code, its one anyways
{
    public static final Random rand = new Random((int)((2>>16)*Math.random()));
    public static MinecraftServer minecraft_server;
    private static final Logger LOGGER = LogManager.getLogger();

    public static SettingsManager settingsManager;
    static
    {
        SettingsManager.parseSettingsClass(carpet.settings.CarpetSettings.class);

    }
    public static void init(MinecraftServer server) //aka constructor of this static singleton class
    {
        CarpetServer.minecraft_server = server;
        LOGGER.debug("Carpet Initialize");
    }
    public static void onServerLoaded(MinecraftServer server)
    {
        settingsManager = new SettingsManager(server);
    }
    // Separate from onServerLoaded, because a server can be loaded multiple times in singleplayer
    public static void onGameStarted()
    {
        LOGGER.debug("Carpet GameStart");
        LoggerRegistry.initLoggers();
    }

    public static void tick(MinecraftServer server)
    {
        TickSpeed.tick(server);
        HUDController.update_hud(server);
        //in case something happens
        CarpetSettings.impendingFillSkipUpdates = false;
    }

    public static void registerCarpetCommands(CommandDispatcher<CommandSource> dispatcher)
    {
        CarpetCommand.register(dispatcher);
        TickCommand.register(dispatcher);
        CounterCommand.register(dispatcher);
        LogCommand.register(dispatcher);
        CameraModeCommand.register(dispatcher);
        InfoCommand.register(dispatcher);
        PingCommand.register(dispatcher);
        DistanceCommand.register(dispatcher);
        PerimeterInfoCommand.register(dispatcher);
        DrawCommand.register(dispatcher);
        SpawnCommand.register(dispatcher);
        LOGGER.info("Carpet Commands Registered");
    }
}

