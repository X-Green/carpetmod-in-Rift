package carpet.settings;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SettingsManager
{
    private static final Logger LOG = LogManager.getLogger();
    private static Map<String, ParsedRule<?>> rules = new HashMap<>();
    public boolean locked;
    private MinecraftServer server;
    private static List<TriConsumer<CommandSource, ParsedRule<?>, String>> observers = new ArrayList<>(); // unused, since jarmod is not extendable

    public SettingsManager(MinecraftServer server)
    {
        this.server = server;
        loadConfigurationFromConf();
    }

    public static void parseSettingsClass(Class settingsClass)
    {
        for (Field f : settingsClass.getDeclaredFields())
        {
            Rule rule = f.getAnnotation(Rule.class);
            if (rule == null) continue;
            ParsedRule parsed = new ParsedRule(f, rule);
            rules.put(parsed.name, parsed);
        }
    }

    public static void addRuleObserver(TriConsumer<CommandSource, ParsedRule<?>, String> observer) // unused in jarmod
    {
        observers.add(observer);
    }

    static void notifyRuleChanged(CommandSource source, ParsedRule<?> rule, String userTypedValue) // unused in jarmod
    {
        observers.forEach(observer -> observer.accept(source, rule, userTypedValue));
    }

    public static Iterable<String> getCategories()
    {
        Set<String> categories = new HashSet<>();
        getRules().stream().map(r -> r.categories).forEach(categories::addAll);
        return categories;
    }


    public static ParsedRule<?> getRule(String name)
    {
        return rules.get(name);
    }

    public static Collection<ParsedRule<?>> getRules()
    {
        return rules.values().stream().sorted().collect(Collectors.toList());
    }

    public Collection<ParsedRule<?>> findStartupOverrides()
    {
        Set<String> defaults = readSettingsFromConf().getLeft().keySet();
        return rules.values().stream().filter(r -> defaults.contains(r.name)).
                sorted().collect(Collectors.toList());
    }


    public Collection<ParsedRule<?>> getNonDefault()
    {
        return rules.values().stream().filter(r -> !r.isDefault()).sorted().collect(Collectors.toList());
    }

    private File getFile()
    {
        return server.getActiveAnvilConverter().getFile(server.getFolderName(), "carpet.conf");
    }

    public void disableBooleanFromCategory(String category)
    {
        for (ParsedRule<?> rule : rules.values())
        {
            if (rule.type != boolean.class || !rule.categories.contains(category))
                continue;
            ((ParsedRule<Boolean>) rule).set(server.getCommandSource(), false, "false");
        }
    }


    private void writeSettingsToConf(Map<String, String> values)
    {
        if (locked)
            return;
        try
        {
            FileWriter fw  = new FileWriter(getFile());
            for (String key: values.keySet())
            {
                fw.write(key+" "+values.get(key)+"\n");
            }
            fw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            LOG.error("[CM]: failed write the carpet.conf");
        }
        ///todo is it really needed? resendCommandTree();
    }

    public void notifyPlayersCommandsChanged()
    {
        if (server.getPlayerList() == null)
        {
            return;
        }
        for (EntityPlayerMP entityplayermp : server.getPlayerList().getPlayers())
        {
            server.getCommandManager().send(entityplayermp);
        }
    }

    private void loadConfigurationFromConf()
    {
        for (ParsedRule<?> rule : rules.values()) rule.resetToDefault(server.getCommandSource());
        Pair<Map<String, String>,Boolean> conf = readSettingsFromConf();
        locked = false;
        if (conf.getRight())
        {
            LOG.info("[CM]: Carpet Mod is locked by the administrator");
            disableBooleanFromCategory(RuleCategory.COMMAND);
        }
        for (String key: conf.getLeft().keySet())
        {
            if (rules.get(key).set(server.getCommandSource(), conf.getLeft().get(key)) != null)
                LOG.info("[CM]: loaded setting "+key+" as "+conf.getLeft().get(key)+" from carpet.conf");
        }
        locked = conf.getRight();
    }


    private Pair<Map<String, String>,Boolean> readSettingsFromConf()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(getFile()));
            String line = "";
            boolean confLocked = false;
            Map<String,String> result = new HashMap<String, String>();
            while ((line = reader.readLine()) != null)
            {
                line = line.replaceAll("\\r|\\n", "");
                if ("locked".equalsIgnoreCase(line))
                {
                    confLocked = true;
                }
                String[] fields = line.split("\\s+",2);
                if (fields.length > 1)
                {
                    if (!rules.containsKey(fields[0]))
                    {
                        LOG.error("[CM]: Setting " + fields[0] + " is not a valid - ignoring...");
                        continue;
                    }
                    ParsedRule rule = rules.get(fields[0]);

                    if (!(rule.options.contains(fields[1])) && rule.isStrict)
                    {
                        LOG.error("[CM]: The value of " + fields[1] + " for " + fields[0] + " is not valid - ignoring...");
                        continue;
                    }
                    result.put(fields[0],fields[1]);

                }
            }
            reader.close();
            return Pair.of(result, confLocked);
        }
        catch(FileNotFoundException e)
        {
            return Pair.of(new HashMap<>(), false);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return Pair.of(new HashMap<>(), false);
        }
    }

    // stores different defaults in the file
    public boolean setDefaultRule(CommandSource source, String settingName, String stringValue)
    {
        if (locked) return false;
        if (rules.containsKey(settingName))
        {
            Pair<Map<String, String>,Boolean> conf = readSettingsFromConf();
            conf.getLeft().put(settingName, stringValue);
            writeSettingsToConf(conf.getLeft()); // this may feels weird, but if conf
            // is locked, it will never reach this point.
            rules.get(settingName).set(source,stringValue);
            return true;
        }
        return false;
    }
    // removes overrides of the default values in the file
    public boolean removeDefaultRule(CommandSource source, String settingName)
    {
        if (locked) return false;
        if (rules.containsKey(settingName))
        {
            Pair<Map<String, String>,Boolean> conf = readSettingsFromConf();
            conf.getLeft().remove(settingName);
            writeSettingsToConf(conf.getLeft());
            rules.get(settingName).resetToDefault(source);
            return true;
        }
        return false;
    }

    public Collection<ParsedRule<?>> getRulesMatching(String search) {
        String lcSearch = search.toLowerCase(Locale.ROOT);
        return rules.values().stream().filter(rule ->
        {
            if (rule.name.toLowerCase(Locale.ROOT).contains(lcSearch)) return true;
            for (String c : rule.categories) if (c.toLowerCase(Locale.ROOT).equals(search)) return true;
            return false;
        }).collect(ImmutableList.toImmutableList());
    }

    public int printAllRulesToLog()
    {
        PrintStream ps = System.out;
        ps.println("# Carpet Settings");
        for (Map.Entry<String, ParsedRule<?>> e : new TreeMap<>(rules).entrySet())
        {
            ParsedRule<?> rule = e.getValue();
            ps.println("## " + rule.name);
            ps.println(rule.description+"  ");
            for (String extra : rule.extraInfo)
                ps.println(extra + "  ");
            ps.println("* Type: `" + rule.type.getSimpleName() + "`  ");
            ps.println("* Default value: `" + rule.defaultAsString + "`  ");
            String optionString = rule.options.stream().map(s -> "`" + s + "`").collect(Collectors.joining(", "));
            ps.println((rule.isStrict?"* Required":"* Suggested")+" options: " + optionString + "  ");
            ps.println("* Categories: " + rule.categories.stream().map(s -> "`" + s.toUpperCase(Locale.ROOT) + "`").collect(Collectors.joining(", ")) + "  ");
            boolean preamble = false;
            for (Validator<?> validator : rule.validators)
            {
                if(validator.description() != null)
                {
                    if (!preamble)
                    {
                        ps.println("* Additional notes:  ");
                        preamble = true;
                    }
                    ps.println("  * "+validator.description()+"  ");
                }
            }
            ps.println("  ");
        }
        return 1;
    }

}
