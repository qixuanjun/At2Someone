package top.qixuanjun233.at2someone;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public final class At2someone extends JavaPlugin {

    private boolean pluginEnabled;
    private boolean isPrefix;
    private boolean isAtAll;
    private FileConfiguration config;
    private Set<UUID> dndPlayers;
    // Store the last set of completions added so we can remove them before adding new ones
    private java.util.Collection<String> lastCompletions = new java.util.ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Loading configs...");
        config = getConfig();
        dndPlayers = new CopyOnWriteArraySet<>();
        isPrefix = getConfig().getBoolean("prefix");
        isAtAll = getConfig().getBoolean("isAtAll");
        loadPluginStatus();
        loadPrefix();
        loadAtAll();
        getLogger().info("Plugin Configs Loaded.");
        loadDndPlayers();
        getLogger().info("DndPlayers Loaded.");
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinLeaveListener(this), this);
        getLogger().info("ChatListener Registered.");
        if (getCommand("at") != null) {
            Objects.requireNonNull(getCommand("at")).setExecutor(new CommandHandler(this));
            getLogger().info("CommandHandler Registered.");
        } else {
            getLogger().severe("Command /at register failed,please check the plugin.yml");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("At2someone(Ver.1.0.4-SNAPSHOT) Enabled Successfully.");
        if(!pluginEnabled) {
            getLogger().warning("Plugin is Disabled,");
            getLogger().warning("If you want to trigger this plugin, enable it by using /at enable.");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveDndPlayers();
        getLogger().info("DndPlayers Saved.");
        saveAtAll();
        savePrefix();
        savePluginStatus();
        getLogger().info("Plugin Configs Saved.");
        getServer().getPluginManager().disablePlugins();
        getLogger().info("At2someone Disabled Successfully.");
    }
    //其实我在想要不要给我这一大坨石山去写注释...

    //负责reload指令
    public void reloadTheFuckingShallowPlugin() {
        reloadConfig();
        config = getConfig();
        loadPluginStatus();
        loadDndPlayers();
        loadPrefix();
        loadAtAll();
        getLogger().info("Plugin Configs Reloaded.");
    }

    //加载插件启用状态
    private void loadPluginStatus() {
        pluginEnabled = config.getBoolean("plugin-enabled", true);
    }

    //保存插件启用状态
    private void savePluginStatus() {
        config.set("plugin-enabled", pluginEnabled);
        saveConfig();
    }

    //切换插件状态
    public void togglePlugin(boolean enable) {
        this.pluginEnabled = enable;
        savePluginStatus();
        getLogger().info("The PluginStatus had been switched to " + (enable ? "§aEnable Mode" : "§cDisable Mode") + "§r manually.");
    }//have been switched表现完被动（？）我语法不好不要骂我（（（（（（（

    //加载 勿扰模式的玩家
    private void loadDndPlayers() {
        dndPlayers.clear();
        config.getStringList("dnd-players").forEach(uuidStr -> {
            try {
                dndPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid UUID format: " + uuidStr + "（Ignored）");
            }
        });
    }

    //加载 是否需输入@才能提到人 的config
    private void loadPrefix() {
        isPrefix = config.getBoolean("isPrefix", false);
    }
    //加载 是否起用@所有人
    private void loadAtAll() {
        isAtAll = config.getBoolean("isAtAll", true);
    }

    //一个判断插件是否启用的布尔 方便后面用
    public boolean isPluginEnabled() {
        return pluginEnabled;
    }
    //判断isPrefix是否启用
    public boolean isPrefix(){
        return isPrefix;
    }
    //判断AtAll是否启用
    public boolean isAtAll(){
        return isAtAll;
    }

    public java.util.Collection<String> getLastCompletions() {
        return lastCompletions;
    }

    public void setLastCompletions(java.util.Collection<String> completions) {
        this.lastCompletions = completions;
    }

    //help栏里负责显示插件状态的 我在这里写纯粹是为了别的地方看起来好看😋
    public String isPluginEnabledText() {
        if(pluginEnabled) {
            return "§a已启用";
        }else{
            return "§c已禁用";
        }
    }
    //同上
    public String isPrefixText() {
        if(isPrefix) {
            return "§a已启用";
        }else{
            return "§c已禁用";
        }
    }
    //同上
    public String isAtAllText() {
        if(isAtAll) {
            return "§a已启用";
        }else{
            return "§c已禁用";
        }
    }

    //保存 勿扰模式的玩家 到config里
    private void saveDndPlayers() {
        config.set("dnd-players", dndPlayers.stream().map(UUID::toString).toList());
        saveConfig();
    }

    //保存 是否需输入@才能提到人 到config里
    private void savePrefix() {
        config.set("isPrefix", isPrefix);
        saveConfig();
    }

    //保存 是否允许@all/全体成员
    private void saveAtAll() {
        config.set("isAtAll", isAtAll);
        saveConfig();
    }

    //切换 玩家勿扰状态
    public void toggleDnd(Player player, boolean bool) {
        if (bool) {
            dndPlayers.add(player.getUniqueId());
        } else {
            dndPlayers.remove(player.getUniqueId());
        }
        saveDndPlayers();
    }

    //切换 是否需输入@才能提到人
    public void togglePrefix(boolean bool) {
        isPrefix = bool;
        savePrefix();
    }

    //切换 AtAll使能
    public void toggleAtAll(boolean bool) {
        isAtAll = bool;
        saveAtAll();
    }

    //提示玩家你被@了
    public void remindPlayer(String senderRealName,String senderDisplayName,Player receiver,int mode) {
        //这里说一下mode的意思: 0:带@提及 (@mention) / 1:不带@提及 (plain mention) / 2:at全体 (atAll)
        //根据用户要求: 不带@+用户名的，不发出声音, 只显示白色ActionBar
        
        String actionbarMsg = "";
        boolean playSound = false;

        if (mode == 1) {
             if (senderRealName.equals(senderDisplayName)) {
                 actionbarMsg = "§f" + senderRealName + " 提到了你";
             } else {
                 actionbarMsg = "§f" + senderRealName + " (" + senderDisplayName + "§f) 提到了你";
             }
             playSound = false;
        } else {
             // mode 0 or 2 (Implicitly implies Highlighting + Sound)
             if(senderRealName.equals(senderDisplayName)){
                 actionbarMsg = switch (mode) {
                     case 0 -> "§6§l" + senderRealName + " §e§l@了你!";
                     case 2 -> "§6§l" + senderRealName + " §e§l@了全体成员!";
                     default -> "";
                 };
             }else{
                 actionbarMsg = switch (mode) {
                     case 0 -> "§6§l" + senderRealName + "§r(" + senderDisplayName + "§r) §e§l@了你!";
                     case 2 -> "§6§l" + senderRealName + "§r(" + senderDisplayName + "§r) §e§l@了全体成员!";
                     default -> "";
                 };
             }
             playSound = true;
        }

        //这里进行一个"dndplayer"的判断 然后选择性的提供title和sound
        if(!dndPlayers.contains(receiver.getUniqueId()) && !actionbarMsg.isEmpty()) {
            receiver.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionbarMsg));
            if (playSound) {
                receiver.playSound(receiver.getLocation(), "entity.experience_orb.pickup", 1.0f, 1.0f);
            }
        }
    }
}
