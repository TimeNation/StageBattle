package net.timenation.stagebattle.listener;

import net.timenation.stagebattle.StageBattle;
import net.timenation.timespigotapi.TimeSpigotAPI;
import net.timenation.timespigotapi.manager.game.gamestates.GameState;
import net.timenation.timespigotapi.manager.language.I18n;
import net.timenation.timespigotapi.timeplayer.TimePlayer;
import net.timenation.timespigotapi.timeplayer.TimeStatsPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.sql.Time;

public class LobbyProtection implements Listener {

    @EventHandler
    public void handleBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().getGameMode().equals(GameMode.CREATIVE) || StageBattle.getInstance().getSpecatePlayers().contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handlePlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!event.getPlayer().getGameMode().equals(GameMode.CREATIVE) || StageBattle.getInstance().getSpecatePlayers().contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handlePlayerBucketFill(PlayerBucketFillEvent event) {
        if (!event.getPlayer().getGameMode().equals(GameMode.CREATIVE) || StageBattle.getInstance().getSpecatePlayers().contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleBlockPlace(BlockPlaceEvent event) {
        if (StageBattle.getInstance().getGameState() != GameState.INGAME || StageBattle.getInstance().getSpecatePlayers().contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (StageBattle.getInstance().getGameState() != GameState.INGAME || StageBattle.getInstance().getSpecatePlayers().contains((Player) event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (StageBattle.getInstance().getGameState() != GameState.INGAME || StageBattle.getInstance().getSpecatePlayers().contains(player)) {
                event.setCancelled(true);
            }

            if (player.getKiller() instanceof Player) {
                player.setKiller(player.getKiller());
            }
        }
    }

    @EventHandler
    public void handleWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerDropItem(PlayerDropItemEvent event) {
        if (StageBattle.getInstance().getGameState() != GameState.INGAME || StageBattle.getInstance().getSpecatePlayers().contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handlePlayerPickupItem(PlayerPickupItemEvent event) {
        if (StageBattle.getInstance().getGameState() != GameState.INGAME || StageBattle.getInstance().getSpecatePlayers().contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleFoodLevelChange(FoodLevelChangeEvent event) {
        if (StageBattle.getInstance().getGameState() != GameState.INGAME) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handlePlayerChat(PlayerChatEvent event) {
        if (StageBattle.getInstance().getSpecatePlayers().contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handlePlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (StageBattle.getInstance().getGameState() != GameState.INGAME || StageBattle.getInstance().getSpecatePlayers().contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!StageBattle.getInstance().getGameState().equals(GameState.INGAME) || StageBattle.getInstance().getSpecatePlayers().contains(event.getEntity())) {
            if (event.getEntity() instanceof ArmorStand) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void handlePlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block block = player.getLocation().subtract(0, 1, 0).getBlock();

        if(block.getLocation().equals(new Location(StageBattle.getInstance().getWorld(), 97, 47, -155)) && block.getType().equals(Material.DIAMOND_BLOCK)) {
            TimePlayer timePlayer = TimeSpigotAPI.getInstance().getTimePlayerManager().getTimePlayer(player);
            TimeStatsPlayer timeStatsPlayer = TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle");
            StageBattle.getInstance().getCountdownManager().startEndCountdown();
            timePlayer.setCoins(timePlayer.getCoins() + 300);
            timeStatsPlayer.setWins(timeStatsPlayer.getWins() + 1);
            StageBattle.getInstance().setGameState(GameState.ENDING);
            Bukkit.getOnlinePlayers().forEach(current -> {
                current.teleport(new Location(Bukkit.getWorld("world"), 111.5, 114.00, -262.5, -45, 0));
                current.getInventory().clear();
                current.sendTitle(I18n.format(current, "api.game.title.loose.top"), I18n.format(current, "api.game.title.loose.bottom", (Object) TimeSpigotAPI.getInstance().getRankManager().getPlayersRank(player.getUniqueId()).getPlayersNameWithRankColor(player.getUniqueId())));
                current.showPlayer(current);
                StageBattle.getInstance().getDefaultGameQuitItem().setItem(current);
                StageBattle.getInstance().getScoreboardManager().sendEndScoreboardToPlayer(current, player);
                if(current != player) TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(current, "StageBattle").setLooses(TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(current, "StageBattle").getLooses() + 1);
                TimeSpigotAPI.getInstance().getTimePlayerManager().updateTimePlayer(TimeSpigotAPI.getInstance().getTimePlayerManager().getTimePlayer(current));
            });
            player.sendTitle(I18n.format(player, "api.game.title.win.top"), I18n.format(player, "api.game.title.win.bottom"));
            player.sendMessage(I18n.format(player, StageBattle.getInstance().getPrefix(), "api.game.messages.playerhaswongame", 300));
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0);
        }

        if (StageBattle.getInstance().getGameState() == GameState.INGAME) {
            if (StageBattle.getInstance().getSpecatePlayers().contains(event.getPlayer())) {
                if (player.getLocation().getY() <= 100) {
                    player.teleport(new Location(Bukkit.getWorld("world"), 111.5, 114.00, -262.5, -45, 0));
                }
            }

            return;
        }

        if (player.getLocation().getY() <= 100) {
            player.teleport(new Location(Bukkit.getWorld("world"), 111.5, 114.00, -262.5, -45, 0));
            player.sendMessage(I18n.format(player, "api.game.messages.jumpinvoid", StageBattle.getInstance().getPrefix()));
        }
    }
}
