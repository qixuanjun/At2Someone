package top.qixuanjun233.at2someone;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getLogger;

public class PlayerChatListener implements Listener {

    private static final Pattern AT_PATTERN = Pattern.compile("@([a-zA-Z0-9_]{3,16})");//匹配正则
    private static final Pattern UN_PATTERN = Pattern.compile("([a-zA-Z0-9_]{3,16})");//匹配正则

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        //在had been disabled(had been表被动👍👍👍)的情况下 不处理@信息
        if (!plugin.isPluginEnabled()) {
            return;
        }
        String originalMessage = event.getMessage();//聊天原文的一个获取
        //替换username为黄色
        if(plugin.isPrefix()) {
            getLogger().info("yes");
            //替换@玩家(isPrefix==true)
            Matcher matcher = AT_PATTERN.matcher(originalMessage);
            String modifiedMessage = matcher.replaceAll("§e@$1§r");//$1表玩家
            event.setMessage(modifiedMessage);
            matcher.reset();//重置匹配器
            while (matcher.find()) {
                String playerName = matcher.group(1);
                Player mentionedPlayer = Bukkit.getPlayerExact(playerName);
                if (mentionedPlayer != null && mentionedPlayer.isOnline()) {
                    //给被@的玩家发提示
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.remindPlayer(event.getPlayer().getName(),event.getPlayer().getDisplayName(),mentionedPlayer,true);
                    });
                }
            }
        }else{
            //替换@玩家(isPrefix==false)
            Matcher matcher = UN_PATTERN.matcher(originalMessage);
            while (matcher.find()) {
                String playerName = matcher.group(1);
                Player mentionedPlayer = Bukkit.getPlayerExact(playerName);
                if (mentionedPlayer != null && mentionedPlayer.isOnline()) {
                    //给被@的玩家发提示
                    String modifiedMessage = matcher.replaceAll("§e$1§r");//$1表玩家
                    event.setMessage(modifiedMessage);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.remindPlayer(event.getPlayer().getName(),event.getPlayer().getDisplayName(),mentionedPlayer,false);
                    });
                }
            }
        }
    }
    private final At2someone plugin;
    public PlayerChatListener(At2someone plugin) {
        this.plugin = plugin;
    }
}
