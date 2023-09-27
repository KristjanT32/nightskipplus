package krisapps.nightskipplus.commands;

import krisapps.nightskipplus.NightSkipPlus;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceSkip implements CommandExecutor {

    NightSkipPlus main;

    public ForceSkip(NightSkipPlus main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        World world = ((Player) sender).getWorld();

        if (main.pluginData.contains(world.getUID() + ".finish_task")) {
            if (main.pluginData.getInt(world.getUID() + "finish_task") != -1) {
                main.getServer().getScheduler().runTask(main, () -> {
                    world.setTime(1000);
                    main.cooldowns.replace(((Player) sender).getWorld().getUID(), true);
                    main.dataUtility.setVotingActive(((Player) sender).getWorld().getUID(), false);
                });

                for (Player p : ((Player) sender).getWorld().getPlayers()) {
                    main.messageUtility.sendMessage(p, main.localizationUtility.getLocalizedPhrase("messages.skipped-forced")
                            .replaceAll("%by%", sender.getName())
                    );
                }
                main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedPhrase("commands.forceskip.skipped"));
            } else {
                main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedPhrase("commands.forceskip.none"));
            }
        } else {
            main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedPhrase("commands.forceskip.none"));
        }
        return true;
    }
}
