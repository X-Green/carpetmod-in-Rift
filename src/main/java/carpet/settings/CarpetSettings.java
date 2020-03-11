package carpet.settings;

import carpet.CarpetServer;
import carpet.utils.Messenger;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.math.shapes.VoxelShapes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static carpet.settings.RuleCategory.*;

public class CarpetSettings {
    public static final String carpetVersion = "Rift-Carpet";
    public static final Logger LOG = LogManager.getLogger();
    public static boolean skipGenerationChecks = false;
    public static boolean impendingFillSkipUpdates = false;
    public static final int SHULKERBOX_MAX_STACK_AMOUNT = 64;


    @Rule(
            desc = "Enables /ping command to see your ping",
            category = COMMAND
    )
    public static boolean commandPing = true;

    @Rule(desc = "Enables /tick command to control game clocks", category = COMMAND)
    public static boolean commandTick = true;

    @Rule(desc = "Enables /log command to monitor events in the game via chat and overlays", category = COMMAND)
    public static boolean commandLog = true;

    @Rule(
            desc = "Enables /distance command to measure in game distance between points",
            extra = "Also enables brown carpet placement action if 'carpets' rule is turned on as well",
            category = COMMAND
    )
    public static boolean commandDistance = true;

    @Rule(
            desc = "Enables /info command for blocks",
            extra = {
                    "Also enables gray carpet placement action",
                    "if 'carpets' rule is turned on as well"
            },
            category = COMMAND
    )
    public static boolean commandInfo = true;

    @Rule(
            desc = "Enables /c and /s commands to quickly switch between camera and survival modes",
            extra = "/c and /s commands are available to all players regardless of their permission levels",
            category = COMMAND
    )
    public static boolean commandCameramode = true;

    @Rule(
            desc = "Enables /perimeterinfo command",
            extra = "... that scans the area around the block for potential spawnable spots",
            category = COMMAND
    )
    public static boolean commandPerimeterInfo = true;

    @Rule(desc = "microtick information", category = SURVIVAL)
    public static boolean microTick = false;

    @Rule(desc = "Enables /draw commands", extra = "... allows for drawing simple shapes", category = COMMAND)
    public static boolean commandDraw = true;

    @Rule(desc = "Enables /spawn command for spawn tracking", category = COMMAND)
    public static boolean commandSpawn = true;

    @Rule(desc = "fill/clone/setblock and structure blocks cause block updates", category = CREATIVE)
    public static boolean fillUpdates = true;

    @Rule(desc = "Placing carpets may issue carpet commands for non-op players", category = SURVIVAL)
    public static boolean carpets = false;

    @Rule(
            desc = "Pistons, Glass and Sponge can be broken faster with their appropriate tools",
            category = SURVIVAL
    )
    public static boolean missingTools = false;

    @Rule(
            desc = "hoppers pointing to wool will count items passing through them",
            extra = {
                    "Enables /counter command, and actions while placing red and green carpets on wool blocks",
                    "Use /counter <color?> reset to reset the counter, and /counter <color?> to query",
                    "In survival, place green carpet on same color wool to query, red to reset the counters",
                    "Counters are global and shared between players, 16 channels available",
                    "Items counted are destroyed, count up to one stack per tick per hopper"
            },
            category = {COMMAND, CREATIVE, FEATURE}
    )
    public static boolean hopperCounters = false;

    @Rule(
            desc = "Players can flip and rotate blocks when holding cactus",
            extra = {
                    "Doesn't cause block updates when rotated/flipped",
                    "Applies to pistons, observers, droppers, repeaters, stairs, glazed terracotta etc..."
            },
            category = {CREATIVE, SURVIVAL, FEATURE}
    )
    public static boolean flippinCactus = false;
}