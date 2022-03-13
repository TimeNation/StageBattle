package net.timenation.stagebattle.manager;

import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.service.ServiceState;
import net.timenation.stagebattle.StageBattle;
import net.timenation.timespigotapi.TimeSpigotAPI;
import net.timenation.timespigotapi.manager.ItemManager;
import net.timenation.timespigotapi.manager.game.TimeGame;
import net.timenation.timespigotapi.manager.game.gamestates.GameState;
import net.timenation.timespigotapi.manager.language.I18n;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CountdownManager {

    private int countdown;
    private TimeGame game;
    private BukkitTask bukkitTask;

    public CountdownManager(TimeGame timeGame) {
        countdown = 60;
        this.game = timeGame;
    }

    public void startCountdown() {
        StageBattle.getInstance().setGameState(GameState.STARTING);
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                switch (countdown) {
                    case 60:
                    case 50:
                    case 40:
                    case 30:
                    case 20:
                    case 15:
                    case 10:
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            player.sendMessage(I18n.format(player, game.getPrefix(), "api.game.messages.countdown", game.getSecoundColor(), countdown));
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 0);
                        });
                        break;
                }

                if (countdown > 0 && countdown < 6) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendMessage(I18n.format(player, game.getPrefix(), "api.game.messages.countdown", game.getSecoundColor(), countdown));
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 0);
                    });
                }

                if (countdown == 0) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        Bukkit.getScheduler().runTask(StageBattle.getInstance(), () -> {
                            //StageBattle.getInstance().getIngameScoreboard().sendIngameScoreboard(SkyWars.getInstance(), player);
                            player.teleport(new Location(game.getWorld(), 166.5, 21, -143.5, 90, 0));
                            player.setMetadata("lives", (MetadataValue) new FixedMetadataValue(StageBattle.getInstance(), Integer.valueOf(5)));
                        });
                        player.sendMessage(I18n.format(player, "api.game.messages.gamestart", game.getPrefix()));
                        player.getInventory().clear();
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0);
                        player.setLevel(0);
                        player.setExp(0);
                        player.getInventory().addItem(new ItemManager(Material.SCAFFOLDING, 32).build());
                        StageBattle.getInstance().getPlayers().add(player);
                        TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle").setGames(TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle").getGames() + 1);
                        StageBattle.getInstance().setGameState(GameState.INGAME);
                        Bukkit.getScheduler().runTask(game, () -> {
                            StageBattle.getInstance().getIngameScoreboard().sendIngameScoreboard(player, game);
                        });
                        CloudAPI.getInstance().getCloudServiceManager().getCloudServiceByName(CloudAPI.getInstance().getThisSidesName()).setState(ServiceState.INVISIBLE);
                    });
                }

                if (countdown > 0 && countdown < 60) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.setLevel(countdown);
                        player.setExp(countdown / 60F);
                        game.getScoreboardManager().updateLobbyScoreboard(player, countdown);

                        if (Bukkit.getOnlinePlayers().size() >= StageBattle.getInstance().getNeededPlayers() && StageBattle.getInstance().getGameState().equals(GameState.LOBBY) || StageBattle.getInstance().getGameState().equals(GameState.STARTING)) {
                            player.sendActionBar(I18n.format(player, StageBattle.getInstance().getPrefix(), "api.game.actionbar.starting", game.getSecoundColor(), countdown));
                        }
                    });
                }

                if (countdown < 0) {
                    cancel();
                }

                countdown--;
            }
        }.runTaskTimerAsynchronously(StageBattle.getInstance(), 0, 20);
    }

    public void startEndountdown() {
        countdown = 10;
        StageBattle.getInstance().setGameState(GameState.ENDING);
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    StageBattle.getInstance().getSpecatePlayers().remove(player);

                    player.setLevel(countdown);
                    player.setExp(countdown / 10F);

                    if (countdown != 0) {
                        player.sendMessage(I18n.format(player, game.getPrefix(), "api.game.messages.countdown.stop", game.getSecoundColor(), countdown));
                    }
                    if (countdown == 0) {
                        player.sendMessage(I18n.format(player, game.getPrefix(), "api.game.messages.countdown.stopnow", game.getSecoundColor()));
                        TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().updateTimeStatsPlayer(TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle"));
                        CloudAPI.getInstance().getCloudPlayerManager().getCachedCloudPlayer(player.getUniqueId()).connect(CloudAPI.getInstance().getCloudServiceManager().getCloudServiceByName("Lobby-1"));
                        Bukkit.shutdown();
                    }
                }

                countdown--;
            }
        }.runTaskTimer(StageBattle.getInstance(), 0, 20);
    }

    public void stopCountdown() {
        bukkitTask.cancel();
        countdown = 60;
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.setLevel(countdown);
            player.setExp(countdown / 60F);
        });
        StageBattle.getInstance().setGameState(GameState.LOBBY);
    }

    public int getCountdown() {
        return countdown;
    }

    public void startGame() {
        countdown = 10;
    }
}
