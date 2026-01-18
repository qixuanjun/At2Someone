package top.qixuanjun233.at2someone;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public record PlayerJoinLeaveListener(At2someone plugin) implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateCompletionsForEveryone();
    }

    private void updateCompletionsForEveryone() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Remove old completions first to avoid duplicates or stale data
            player.removeCustomChatCompletions(plugin.getLastCompletions());
            
            // Add new completions
            java.util.Collection<String> newCompletions = new java.util.ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                newCompletions.add("@" + p.getName());
            }
            // Add custom @all suggestions
             if (plugin.isAtAll() && player.hasPermission("at.atall")) {
                newCompletions.add("@all");
                newCompletions.add("@全体成员");
            }
            
            player.addCustomChatCompletions(newCompletions);
            plugin.setLastCompletions(newCompletions);
        }
    }
}
