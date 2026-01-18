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

    // 移除了静态正则，改用动态构建
    // private static final Pattern MENTION_PATTERN = ...
    // private static final Pattern AA_PATTERN = ...
    // 为了支持中文匹配，移除了\b边界检查，改为更智能的动态匹配

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.isPluginEnabled()) {
            return;
        }
        if (!event.getPlayer().hasPermission("at.mention")) {
            return;
        }

        // 构建动态正则：匹配真实在线玩家ID，按长度倒序排列以优先匹配长名字
        List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .sorted((a, b) -> b.length() - a.length())
                .toList();

        String message = event.getMessage();
        StringBuffer sb = new StringBuffer();

        if (!playerNames.isEmpty()) {
            // 构建正则: (@?)(\b)(Name1|Name2|...)(?!\w)
            // 解释:
            // (@?): 捕获可选的@前缀 (Group 1)
            // (\b)?: 为防止匹配到单词中间（如banana中的nana），需要在名字前加边界或@。
            //        但是\b对于@并不友好(@是\W)。
            //        所以逻辑是: 要么前面是@，要么前面是边界。
            //        简化逻辑: 只要匹配到了名字，我们再手动检查前面是否是单词字符。
            // (Name1|...): 捕获名字 (Group 2)
            // 我们不加后缀边界，以便匹配 "Player你好" 这种情况
            
            String namePatternStr = playerNames.stream()
                    .map(Pattern::quote)
                    .collect(Collectors.joining("|"));
            
            // Regex: (@?)(Name1|Name2|...)
            // 使用 Case_INSENSITIVE 让匹配更灵活，但替换时需注意
            Pattern dynamicPattern = Pattern.compile("(@?)(" + namePatternStr + ")", Pattern.CASE_INSENSITIVE);
            Matcher matcher = dynamicPattern.matcher(message);

            while (matcher.find()) {
                String prefix = matcher.group(1); // "@" or ""
                String matchedName = matcher.group(2); // 匹配到的名字文本(可能是小写)

                // 手动检查单词左边界：
                // 如果没有@前缀，且名字前面一个字符是字母或数字或下划线，则视为单词内部匹配(如banana匹配nana)，应跳过。
                if (prefix.isEmpty() && matcher.start() > 0) {
                     char prevChar = message.charAt(matcher.start() - 1);
                     if ((prevChar >= 'a' && prevChar <= 'z') || 
                         (prevChar >= 'A' && prevChar <= 'Z') || 
                         (prevChar >= '0' && prevChar <= '9') || 
                         prevChar == '_') {
                         matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
                         continue;
                     }
                }

                // 获取真实玩家对象(因为忽略大小写匹配，需要用matchedName查找)
                Player mentionedPlayer = Bukkit.getPlayerExact(matchedName);
                // 如果精确匹配失败(因为大小写)，尝试模糊匹配或遍历查找
                if (mentionedPlayer == null) {
                    mentionedPlayer = Bukkit.getPlayer(matchedName);
                }
                
                // 双重确认: 只有当找到的玩家名字确实等于(忽略大小写)匹配到的名字时才算数
                // Bukkit.getPlayer是模糊匹配(prefix match)，如输入"A"可能匹配"Admin"。
                // 但我们的正则已经是完整名字列表了，所以理论上这里肯定是全名匹配。
                // 不过为了保险，检查名字一致性
                if (mentionedPlayer != null && !mentionedPlayer.getName().equalsIgnoreCase(matchedName)) {
                     mentionedPlayer = null; // 排除错误的前缀匹配
                }

                boolean hasAt = "@".equals(prefix);

                // 如果配置强制要求前缀(isPrefix=true)，但这只是一个不带@的普通文本匹配 -> 跳过
                if (plugin.isPrefix() && !hasAt) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
                    continue;
                }

                if (mentionedPlayer != null && mentionedPlayer.isOnline()) {
                    String color = hasAt ? "§e" : "§7";
                    // 使用真实玩家名字(mentionedPlayer.getName())替换匹配到的文本(matchedName)，修正大小写
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(color + prefix + mentionedPlayer.getName() + "§r"));
                    
                    int mode = hasAt ? 0 : 1;
                    Player finalMentionedPlayer = mentionedPlayer;
                    Bukkit.getScheduler().runTask(plugin, () -> plugin.remindPlayer(event.getPlayer().getName(), event.getPlayer().getDisplayName(), finalMentionedPlayer, mode));
                } else {
                     // 理论上不可能进入这里，因为正则是从在线玩家列表构建的。
                     // 除非玩家在这一瞬间下线了。
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
                }
            }
            matcher.appendTail(sb);
            message = sb.toString();
        }

        if (plugin.isAtAll()) {
            // 处理 @all / @全体成员
            // 同样需要手动处理边界，防止 "small" 匹配 "all"
            // Regex: (@)(all|全体成员)
            // 这里我们强制要求有@前缀才算@all
            Pattern aaPattern = Pattern.compile("(@)(all|全体成员)", Pattern.CASE_INSENSITIVE);
            Matcher matchAll = aaPattern.matcher(message);
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
