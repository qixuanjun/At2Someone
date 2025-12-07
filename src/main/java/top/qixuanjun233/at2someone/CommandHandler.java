package top.qixuanjun233.at2someone;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabExecutor {

    private final At2someone plugin;

    public CommandHandler(At2someone plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!cmd.getName().equalsIgnoreCase("at")) return false;
        //如果就打了个/at 给点help和information
        if (args.length == 0) {
            HelpSomeone(sender);
            return true;
        }
        //指令后arg判断
        switch (args[0].toLowerCase()) {
            case "dnd":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c该指令只有玩家才可以使用!");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c用法错误! 正确用法：/at dnd on/off");
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "on":
                        plugin.toggleDnd(player, true);
                        player.sendMessage("§8[§6At2Someone§8] §a§l✔ §r§e勿扰模式已开启，您将不会收到被@的提示音与title!");
                        return true;
                    case "off":
                        plugin.toggleDnd(player, false);
                        player.sendMessage("§8[§6At2Someone§8] §a§l✔ §r§e勿扰模式已关闭，您将会收到被@的提示音与title!");
                        return true;
                    default:
                        player.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c无效参数! 请使用 on 或 off");
                        return true;
                }
            case "isprefix":
                if (args.length < 2) {
                    if (sender.hasPermission("at.admin")) {
                        sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c用法错误! 正确用法：/at isprefix on/off");
                    }else{
                        sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c您没有权限使用此命令");
                    }
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "on":
                        if (sender.hasPermission("at.admin")) {
                            plugin.togglePrefix(true);
                            sender.sendMessage("§8[§6At2Someone§8] §a§l✔ §r§e功能已开启，必须输入@+用户名才能提及人!");
                        }else{
                            sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c您没有权限使用此命令");
                        }
                        return true;
                    case "off":
                        if (sender.hasPermission("at.admin")) {
                            plugin.togglePrefix(false);
                            sender.sendMessage("§8[§6At2Someone§8] §a§l✔ §r§e功能已关闭，仅需用户名即可提及人!");
                        }else{
                            sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c您没有权限使用此命令");
                        }
                        return true;
                    default:
                        if (sender.hasPermission("at.admin")) {
                            sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c无效参数! 请使用 on 或 off");
                        }else{
                            sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c您没有权限使用此命令");
                        }
                        return true;
                }
            case "atall":
                if (args.length < 2) {
                    if (sender.hasPermission("at.admin")) {
                        sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c用法错误! 正确用法：/at atall on/off");
                    }else{
                        sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c您没有权限使用此命令");
                    }
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "on":
                        if (sender.hasPermission("at.admin")) {
                            plugin.toggleAtAll(true);
                            sender.sendMessage("§8[§6At2Someone§8] §a§l✔ §r§e功能已开启，现在可以@全体成员了");
                        }else{
                            sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c您没有权限使用此命令");
                        }
                        return true;
                    case "off":
                        if (sender.hasPermission("at.admin")) {
                            plugin.toggleAtAll(false);
                            sender.sendMessage("§8[§6At2Someone§8] §a§l✔ §r§e功能已关闭，现在无法@全体成员");
                        }else{
                            sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c您没有权限使用此命令");
                        }
                        return true;
                    default:
                        if (sender.hasPermission("at.admin")) {
                            sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c无效参数! 请使用 on 或 off");
                        }else{
                            sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c您没有权限使用此命令");
                        }
                        return true;
                }
            case "enable":
                if (sender.hasPermission("at.admin")) {
                    if (plugin.isPluginEnabled()) {
                        sender.sendMessage("§8[§6At2Someone§8] §e§l! §r§e插件已经是启用的了");
                    } else {
                        plugin.togglePlugin(true);
                        sender.sendMessage("§8[§6At2Someone§8] §a§l✔ §r§a插件已启用!");
                    }
                }else{
                    sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c您没有权限使用此命令");
                }
                return true;
            case "disable":
                if (sender.hasPermission("at.admin")) {
                    if (!plugin.isPluginEnabled()) {
                        sender.sendMessage("§8[§6At2Someone§8] §e§l! §r§e插件已经是禁用的了");
                    } else {
                        plugin.togglePlugin(false);
                        sender.sendMessage("§8[§6At2Someone§8] §c§l✔ §r§c插件已禁用!");
                    }
                }else{
                    sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c您没有权限使用此命令");
                }
                return true;
            case "reload":
                if (sender.hasPermission("at.admin")) {
                    plugin.reloadTheFuckingShallowPlugin();
                    sender.sendMessage("§8[§6At2Someone§8] §a§l✔ §r§a插件已重载!");
                }else{
                    sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c您没有权限使用此命令");
                }
                return true;
            default:
                sender.sendMessage("§8[§6At2Someone§8] §c§l❌ §r§c无效指令 请使用 /at 查看可用指令!");
                return true;
        }
    }

    private void HelpSomeone(CommandSender sender) {
        if (sender.hasPermission("at.admin")) {
            sender.sendMessage("§7===== §aAt2Someone §cV1.0.3 §e命令帮助§a(管理员) §7=====");
            sender.sendMessage(ChatColor.WHITE + "/at dnd on " + ChatColor.GRAY + "- 开启勿扰模式");
            sender.sendMessage(ChatColor.WHITE + "/at dnd off " + ChatColor.GRAY + "- 关闭勿扰模式");
            sender.sendMessage(ChatColor.WHITE + "/at isprefix on/off" + ChatColor.GRAY + "- 启用/关闭需要输入@才可提及人（管理员）");
            sender.sendMessage(ChatColor.WHITE + "/at atall on/off" + ChatColor.GRAY + "- 启用/关闭@全体成员功能（管理员）");
            sender.sendMessage(ChatColor.WHITE + "/at enable " + ChatColor.GRAY + "- 启用插件（管理员）");
            sender.sendMessage(ChatColor.WHITE + "/at disable " + ChatColor.GRAY + "- 禁用插件（管理员）");
            sender.sendMessage(ChatColor.WHITE + "/at reload " + ChatColor.GRAY + "- 重载插件（管理员）");
            sender.sendMessage(ChatColor.GRAY + "==================================");
            sender.sendMessage(ChatColor.YELLOW + "isprefix状态:" + plugin.isPrefixText());
            sender.sendMessage(ChatColor.YELLOW + "atall状态:" + plugin.isAtAllText());
        }else{
            sender.sendMessage("§7===== §aAt2Someone §cV1.0.3 §e命令帮助 §7=====");
            sender.sendMessage(ChatColor.WHITE + "/at dnd on " + ChatColor.GRAY + "- 开启勿扰模式");
            sender.sendMessage(ChatColor.WHITE + "/at dnd off " + ChatColor.GRAY + "- 关闭勿扰模式");
            sender.sendMessage(ChatColor.GRAY + "==================================");
        }
        sender.sendMessage(ChatColor.YELLOW + "插件当前状态:" + plugin.isPluginEnabledText());
        sender.sendMessage(ChatColor.GRAY+ "==================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("dnd");
            if (sender.hasPermission("at.admin")) {
                list.add("isprefix");
                list.add("atall");
                list.add("enable");
                list.add("disable");
                list.add("reload");
            }
            return list;

        }
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "dnd":
                    List<String> list = new ArrayList<>();
                    list.add("on");
                    list.add("off");
                    return list;
                case "isprefix":
                    if (sender.hasPermission("at.admin")) {
                        List<String> list2 = new ArrayList<>();
                        list2.add("on");
                        list2.add("off");
                        return list2;
                    }
                case "atall":
                    if (sender.hasPermission("at.admin")) {
                        List<String> list3 = new ArrayList<>();
                        list3.add("on");
                        list3.add("off");
                        return list3;
                    }
                default:
                    return null;
            }

        }
        return null;
    }
}