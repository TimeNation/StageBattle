package net.timenation.stagebattle;

import net.timenation.stagebattle.commands.StartCommand;
import net.timenation.stagebattle.listener.LobbyProtection;
import net.timenation.stagebattle.listener.PlayerDeathListener;
import net.timenation.stagebattle.listener.PlayerJoinListener;
import net.timenation.stagebattle.listener.PlayerQuitListener;
import net.timenation.stagebattle.manager.CountdownManager;
import net.timenation.stagebattle.manager.IngameScoreboard;
import net.timenation.timespigotapi.TimeSpigotAPI;
import net.timenation.timespigotapi.manager.game.TimeGame;
import net.timenation.timespigotapi.manager.game.countdown.Timer;
import net.timenation.timespigotapi.manager.game.defaultitems.DefaultGameExplainItem;
import net.timenation.timespigotapi.manager.game.defaultitems.DefaultGameNavigatorItem;
import net.timenation.timespigotapi.manager.game.defaultitems.DefaultGameQuitItem;
import net.timenation.timespigotapi.manager.game.features.TrampolineFeature;
import net.timenation.timespigotapi.manager.game.gamestates.GameState;
import net.timenation.timespigotapi.manager.game.manager.ConfigManager;
import net.timenation.timespigotapi.manager.game.modules.NickModule;
import net.timenation.timespigotapi.manager.game.scoreboard.ScoreboardManager;
import net.timenation.timespigotapi.manager.language.I18n;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;

public final class StageBattle extends TimeGame {

    private static StageBattle instance;
    private CountdownManager countdownManager;
    private ScoreboardManager scoreboardManager;
    private IngameScoreboard ingameScoreboard;
    private DefaultGameQuitItem defaultGameQuitItem;
    private Timer timer;

    @Override
    public void onEnable() {
       instance = this;
       countdownManager = new CountdownManager(this);
       scoreboardManager = new ScoreboardManager(this);
       ingameScoreboard = new IngameScoreboard();
       defaultGameQuitItem = new DefaultGameQuitItem(this, 7);
       timer = new Timer(this, 1800);

        Bukkit.createWorld(new WorldCreator("StageBattle-1"));

        setPrefix("StageBattle");
        setColor("§a");
        setSecoundColor("§2");
        setGameState(GameState.LOBBY);
        setScoreboardManager(scoreboardManager);
        setGameMap("Flowers", "TimeNation Building", Bukkit.getWorld("StageBattle-1"));
        new TrampolineFeature(this);
        new DefaultGameNavigatorItem(this, 1);
        new DefaultGameExplainItem(this, 4, "api.game.stagebattle.explain");
        new NickModule(this);

        Bukkit.getWorlds().forEach(world -> {
            world.setDifficulty(Difficulty.PEACEFUL);
            world.setTime(0);
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (timer.getTime() == 0 && getGameState() != GameState.ENDING) {
                setGameState(GameState.ENDING);

                Bukkit.getScheduler().runTask(this, () -> {
                    timer.stopTimer();
                    countdownManager.startEndountdown();
                    Bukkit.getOnlinePlayers().forEach(current -> {
                        TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(current, "StageBattle").setLooses(TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(current, "StageBattle").getLooses() + 1);
                        current.showPlayer(this, getSpecatePlayers().get(0));
                        current.getInventory().clear();
                        defaultGameQuitItem.setItem(current);
                        current.teleport(new Location(Bukkit.getWorld("world"), 111.5, 114.00, -262.5, -45, 0));
                        current.sendTitle(I18n.format(current, "api.game.title.undecided.top"), I18n.format(current, "api.game.title.undecided.bottom"));
                        this.getSpecatePlayers().remove(current);
                    });
                });
            }
        }, 1L, 1L);

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(), this);
        pluginManager.registerEvents(new PlayerQuitListener(), this);
        pluginManager.registerEvents(new LobbyProtection(), this);
        pluginManager.registerEvents(new PlayerDeathListener(), this);
        getCommand("start").setExecutor(new StartCommand());

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            TimeSpigotAPI.getInstance().getParticleManager().spawnNormalSizeRotateParticles(new Location(instance.getWorld(), 97.5, 48, -154.5), Particle.ASH);
        }, 0, 1);
    }

    public static StageBattle getInstance() {
        return instance;
    }

    public CountdownManager getCountdownManager() {
        return countdownManager;
    }

    public DefaultGameQuitItem getDefaultGameQuitItem() {
        return defaultGameQuitItem;
    }

    public IngameScoreboard getIngameScoreboard() {
        return ingameScoreboard;
    }

    @Override
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    @Override
    public ArrayList<Player> getPlayers() {
        return players;
    }

    @Override
    public ArrayList<Player> getSpecatePlayers() {
        return specatePlayer;
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public int getNeededPlayers() {
        return 2;
    }

    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }
}
