package top.qixuanjun233.at2someone;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record PlayerChatListener(At2someone plugin) implements Listener {

    // 统一匹配规则：可选的@前缀 + 用户名 + 单词边界(防止匹配qixuanjun233abcd中的前半部分)
    private static final Pattern MENTION_PATTERN = Pattern.compile("(@?)([a-zA-Z0-9_]{3,16})\\b");
    private static final Pattern AA_PATTERN = Pattern.compile("@(all|全体成员)\\b");

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.isPluginEnabled()) {
            return;
        }
        if (!event.getPlayer().hasPermission("at.mention")) {
            return;
        }

        String message = event.getMessage();
        StringBuffer sb = new StringBuffer();

        // 统一处理玩家提及
        Matcher matcher = MENTION_PATTERN.matcher(message);
        while (matcher.find()) {
            String prefix = matcher.group(1); // "@" or ""
            String playerName = matcher.group(2);
            boolean hasAt = "@".equals(prefix);

            // 如果配置强制要求前缀(isPrefix=true)，但这只是一个不带@的普通文本匹配 -> 跳过，当做普通文本处理
            if (plugin.isPrefix() && !hasAt) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }

            Player mentionedPlayer = Bukkit.getPlayerExact(playerName);
            if (mentionedPlayer != null && mentionedPlayer.isOnline()) {
                // 是在线玩家 -> 高亮黄色
                // 如果有@，replacement就是 §e@name§r (黄色)，没有就是 §7name§r (浅灰色)
                String color = hasAt ? "§e" : "§7";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(color + prefix + playerName + "§r"));
                
                // 决定通知模式：有@ -> mode 0 (通知), 无@ -> mode 1 (不通知)
                int mode = hasAt ? 0 : 1;
                Player finalMentionedPlayer = mentionedPlayer;
                Bukkit.getScheduler().runTask(plugin, () -> plugin.remindPlayer(event.getPlayer().getName(), event.getPlayer().getDisplayName(), finalMentionedPlayer, mode));
            } else {
                // 不是在线玩家
                if (hasAt) {
                    // 如果带了@，但是玩家不存在/不在线 -> 显示灰色表示无效提及
                    matcher.appendReplacement(sb, Matcher.quoteReplacement("§8" + prefix + playerName + "§r"));
                } else {
                    // 只是普通文本匹配也不是玩家 -> 原样输出，不做任何染色
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
                }
            }
        }
        matcher.appendTail(sb);
        message = sb.toString();

        if (plugin.isAtAll()) {
            Matcher matchAll = AA_PATTERN.matcher(message);
            sb = new StringBuffer();
            while (matchAll.find()) {
                if (event.getPlayer().hasPermission("at.atall")) {
                    matchAll.appendReplacement(sb, Matcher.quoteReplacement("§e" + matchAll.group(0) + "§r"));
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (Player mentionedPlayer : Bukkit.getOnlinePlayers()) {
                            plugin.remindPlayer(event.getPlayer().getName(), event.getPlayer().getDisplayName(), mentionedPlayer, 2);
                        }
                    });
                } else {
                    matchAll.appendReplacement(sb, Matcher.quoteReplacement(matchAll.group(0)));
                }
            }
            matchAll.appendTail(sb);
            message = sb.toString();
        }
        event.setMessage(message);
    }

    @EventHandler
    public void onTabComplete(AsyncTabCompleteEvent event) {
        // 只处理聊天补全 (非命令)
        if (event.isCommand()) return;
        
        String buffer = event.getBuffer();
        // 如果buffer只是"/"或者以"/"开头但不是命令的某种情况(不太可能如果isCommand检查过了)
        // 其实isCommand已经处理了大部分，但为了保险，若非命令且buffer空
        if (buffer.isEmpty()) return;

        int lastSpace = buffer.lastIndexOf(' ');
        // 获取当前输入的最后一个单词
        String lastWord = lastSpace == -1 ? buffer : buffer.substring(lastSpace + 1);

        // 检查是否以@开头 (包括只输入了一个@的情况)
        if (lastWord.startsWith("@")) {
            String token = lastWord.toLowerCase(); // e.g. "@" or "@abc"

            List<String> completions = new java.util.ArrayList<>();
            
            // 匹配在线玩家
            for (Player p : Bukkit.getOnlinePlayers()) {
                String name = p.getName();
                String candidate = "@" + name;
                if (candidate.toLowerCase().startsWith(token)) {
                    completions.add(candidate);
                }
            }

            // 匹配 @all 和 @全体成员
            if (plugin.isAtAll() && event.getSender().hasPermission("at.atall")) {
                if ("@all".startsWith(token)) completions.add("@all");
                if ("@全体成员".startsWith(token)) completions.add("@全体成员");
            }

            Collections.sort(completions);
            // 只有当有匹配项时才设置，避免覆盖客户端可能的默认行为(虽然聊天通常没默认行为)
            if (!completions.isEmpty()) {
                event.setCompletions(completions);
                event.setHandled(true); // 标记为已处理，告诉客户端使用我们提供的列表
            }
        }
    }
}
