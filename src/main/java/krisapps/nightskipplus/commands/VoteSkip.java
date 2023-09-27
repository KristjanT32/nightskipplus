package krisapps.nightskipplus.commands;

import krisapps.nightskipplus.NightSkipPlus;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VoteSkip implements CommandExecutor {

    NightSkipPlus main;

    public VoteSkip(NightSkipPlus main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Syntax: /votefor [UUID/name]

        if (args.length >= 1) {
            try {

                // Check if a UUID was provided

                UUID player = UUID.fromString(args[0]);
                Player p = Bukkit.getPlayer(player);

                if (main.dataUtility.votingActive(p.getWorld().getUID())) {
                    main.dataUtility.setVote(p.getUniqueId(), p.getWorld().getUID(), false);
                    main.messageUtility.sendMessage(p, main.localizationUtility.getLocalizedPhrase("commands.vote.for"));
                } else {
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedPhrase("commands.vote.inactive"));
                }
            } catch (IllegalArgumentException e) {

                // If we catch an error, a player name was provided.

                Player p = Bukkit.getPlayer(args[0]);
                if (p != null) {
                    if (main.dataUtility.votingActive(p.getWorld().getUID())) {
                        main.dataUtility.setVote(p.getUniqueId(), p.getWorld().getUID(), false);
                        main.messageUtility.sendMessage(p, main.localizationUtility.getLocalizedPhrase("commands.vote.for"));
                    } else {
                        main.messageUtility.sendMessage(p, main.localizationUtility.getLocalizedPhrase("commands.vote.inactive"));
                    }
                } else {
                    main.messageUtility.sendMessage(p, main.localizationUtility.getLocalizedPhrase("commands.vote.invplayer")
                            .replaceAll("%player%", p.getName())
                    );
                }
            }
        } else {
            if ((sender instanceof Player)) {
                if (main.dataUtility.votingActive(((Player) sender).getWorld().getUID())) {
                    main.dataUtility.setVote(((Player) sender).getUniqueId(), ((Player) sender).getWorld().getUID(), false);
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedPhrase("commands.vote.for"));
                } else {
                    main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedPhrase("commands.vote.inactive"));
                }
            } else {
                main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedPhrase("commands.vote.non-player"));
            }
        }
        return true;
    }
}
