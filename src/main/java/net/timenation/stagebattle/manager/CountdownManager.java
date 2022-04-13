package net.timenation.stagebattle.manager;

import net.timenation.stagebattle.StageBattle;
import net.timenation.timespigotapi.manager.ItemManager;
import net.timenation.timespigotapi.manager.game.TimeGame;
import net.timenation.timespigotapi.manager.game.countdown.Countdown;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.metadata.FixedMetadataValue;

public class CountdownManager extends Countdown<StageBattle> {

    public CountdownManager(TimeGame timeGame) {
        super(timeGame);
    }

    @Override
    public void at0() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            Bukkit.getScheduler().runTask(StageBattle.getInstance(), () -> {
                player.teleport(new Location(game.getWorld(), game.configManager.getDouble("x"), game.configManager.getDouble("y"), game.configManager.getDouble("z"), game.configManager.getInt("yaw"), 0));
                player.setMetadata("lives", new FixedMetadataValue(StageBattle.getInstance(), 5));
            });
            player.getInventory().addItem(new ItemManager(Material.SCAFFOLDING, 32).build());
            Bukkit.getScheduler().runTask(game, () -> {
                StageBattle.getInstance().getIngameScoreboard().sendIngameScoreboard(player, game);
            });
        });
    }

    @Override
    public void before0() {

    }

    @Override
    public void at10() {

    }

    @Override
    public void atEnd() {

    }
}
