package net.timenation.stagebattle;

import com.google.gson.JsonParser;
import net.timenation.stagebattle.commands.StartCommand;
import net.timenation.stagebattle.commands.UnnickCommand;
import net.timenation.stagebattle.listener.LobbyProtection;
import net.timenation.stagebattle.listener.PlayerDeathListener;
import net.timenation.stagebattle.listener.PlayerJoinListener;
import net.timenation.stagebattle.listener.PlayerQuitListener;
import net.timenation.stagebattle.manager.CountdownManager;
import net.timenation.stagebattle.manager.IngameScoreboard;
import net.timenation.stagebattle.manager.StageBattleConfig;
import net.timenation.timespigotapi.TimeSpigotAPI;
import net.timenation.timespigotapi.manager.game.TimeGame;
import net.timenation.timespigotapi.manager.game.countdown.Timer;
import net.timenation.timespigotapi.manager.game.defaultitems.DefaultGameExplainItem;
import net.timenation.timespigotapi.manager.game.defaultitems.DefaultGameNavigatorItem;
import net.timenation.timespigotapi.manager.game.defaultitems.DefaultGameQuitItem;
import net.timenation.timespigotapi.manager.game.features.TrampolineFeature;
import net.timenation.timespigotapi.manager.game.gamestates.GameState;
import net.timenation.timespigotapi.manager.game.manager.ConfigManager;
import net.timenation.timespigotapi.manager.game.modules.ForcemapModule;
import net.timenation.timespigotapi.manager.game.modules.NickModule;
import net.timenation.timespigotapi.manager.game.scoreboard.ScoreboardManager;
import net.timenation.timespigotapi.manager.game.team.TeamManager;
import net.timenation.timespigotapi.manager.language.I18n;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class StageBattle extends TimeGame {

    private static StageBattle instance;
    private CountdownManager countdownManager;
    private ScoreboardManager scoreboardManager;
    private IngameScoreboard ingameScoreboard;
    private StageBattleConfig stageBattleConfig;
    private DefaultGameQuitItem defaultGameQuitItem;
    private Timer timer;

    @Override
    public void onEnable() {
       instance = this;
       countdownManager = new CountdownManager(this);
       scoreboardManager = new ScoreboardManager(this);
       ingameScoreboard = new IngameScoreboard();
       stageBattleConfig = new StageBattleConfig();
       defaultGameQuitItem = new DefaultGameQuitItem(this, 7);
       timer = new Timer(this, 1800);

        setPrefix("StageBattle");
        setColor("§a");
        setSecoundColor("§2");
        setGameState(GameState.LOBBY);
        setScoreboardManager(scoreboardManager);
        new TrampolineFeature(this);
        new DefaultGameNavigatorItem(this, 1);
        new DefaultGameExplainItem(this, 4, "api.game.stagebattle.explain");
        new ForcemapModule(this);
        new NickModule(this);

        for (File file : new File("plugins/StageBattle/maps").listFiles()) {
            try {
                Bukkit.createWorld(new WorldCreator(new JsonParser().parse(new FileReader(file)).getAsJsonObject().get("mapWorld").getAsString()));
            } catch (FileNotFoundException ignored) {}
        }

        setRandomGameMap();

        Bukkit.getWorlds().forEach(world -> {
            world.setDifficulty(Difficulty.PEACEFUL);
            world.setTime(0);
        });

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (gameState == GameState.LOBBY || gameState == GameState.INGAME) {
                if (Bukkit.getOnlinePlayers().size() < StageBattle.getInstance().getNeededPlayers()) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendActionBar(I18n.format(player, getPrefix(), "api.game.actionbar.idle", getSecoundColor(), StageBattle.getInstance().getNeededPlayers() - Bukkit.getOnlinePlayers().size()));
                    });
                }
            }

            if (timer.getTime() == 0 && getGameState() != GameState.ENDING) {
                setGameState(GameState.ENDING);

                Bukkit.getScheduler().runTask(this, () -> {
                    timer.stopTimer();
                    countdownManager.startEndCountdown();
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
        getCommand("unnick").setExecutor(new UnnickCommand(this));

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
        return this.stageBattleConfig.getInt("neededPlayers");
    }

    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public TeamManager getTeamManager() {
        return teamManager;
    }
}
