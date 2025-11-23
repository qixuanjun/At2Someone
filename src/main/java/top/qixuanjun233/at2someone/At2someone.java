package top.qixuanjun233.at2someone;

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
    private FileConfiguration config;
    private Set<UUID> dndPlayers;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Starting up At2someone...");
        getLogger().info("Loading configs...");
        config = getConfig();
        dndPlayers = new CopyOnWriteArraySet<>();
        isPrefix = getConfig().getBoolean("prefix");
        loadPluginStatus();
        loadPrefix();
        getLogger().info("Plugin Configs Loaded.");
        loadDndPlayers();
        getLogger().info("DndPlayers Loaded.");
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getLogger().info("ChatListener Registered.");
        if (getCommand("at") != null) {
            Objects.requireNonNull(getCommand("at")).setExecutor(new CommandHandler(this));
            getLogger().info("CommandHandler Registered.");
        } else {
            getLogger().severe("Command /at register failed,please check the plugin.yml");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("At2someone is Enabled Successfully!");
        if(!pluginEnabled) {
            getLogger().warning("Plugin is Disabled,");
            getLogger().warning("If you want to trigger this function, you can enable the plugin by using /at enable.");
        }
        getLogger().info("@MainTread ❤");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveDndPlayers();
        getLogger().info("DndPlayers Saved.");
        savePrefix();
        savePluginStatus();
        getLogger().info("Plugin Configs Saved.");
        getServer().getPluginManager().disablePlugins();
        getLogger().info("At2someone Disabled.");
        getLogger().info("❤ daerTniaM@");
    }
    //其实我在想要不要给我这一大坨石山去写注释...

    //负责reload指令
    public void reloadTheFuckingShallowPlugin() {
        reloadConfig();
        config = getConfig();
        loadPluginStatus();
        loadDndPlayers();
        loadPrefix();
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
    }

    //加载 勿扰模式的玩家
    private void loadDndPlayers() {
        dndPlayers.clear();
        config.getStringList("dnd-players").forEach(uuidStr -> {
            try {
                dndPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid UUID format：" + uuidStr + "（Ignored）");
            }
        });
    }

    //加载 是否需输入@才能提到人 的config
    private void loadPrefix() {
        config.getBoolean("prefix", isPrefix);
    }

    //一个判断插件是否启用的布尔 方便后面用
    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    public boolean isPrefix(){
        return isPrefix;
    }

    //help栏里负责显示插件状态的 我在这里写纯粹是为了别的地方看起来好看😋
    public String isPluginEnabledText() {
        if(pluginEnabled) {
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
        if (bool) {
            isPrefix = true;
        } else {
            isPrefix = false;
        }
        savePrefix();
    }

    //提示玩家你被@了
    public void remindPlayer(String senderRealName,String senderDisplayName,Player receiver,boolean bool) {
        //声明一个str C#大手发力了 为什么java的string不是string而是String啊 我不想写大写字母啊
        String mplayersubtitle = null;
        //在这里我先处理一下@完之后的玩家名变成黄色的事情。
        if(senderRealName.equals(senderDisplayName)){
            if(bool){
                mplayersubtitle = "§e" + senderRealName + "§e@了你!";
            }else{
                mplayersubtitle = "§e" + senderRealName + "§e提到了你!";
            }
        }else{
            if(bool){
                mplayersubtitle = "§e" + senderRealName + "§e(" + senderDisplayName + "§e)" + "@了你!";
            }else{
                mplayersubtitle = "§e" + senderRealName + "§e(" + senderDisplayName + "§e)" + "提到了你!";
            }
        }
        //这里进行一个"dndplayer"的判断 然后选择性的提供title和sound
        if(!dndPlayers.contains(receiver.getUniqueId())) {
            receiver.sendTitle("§b有人在公屏提到了你❤", mplayersubtitle);
            receiver.playSound(receiver.getLocation(), "entity.experience_orb.pickup", 1.0f, 1.0f);
        }
    }
}
