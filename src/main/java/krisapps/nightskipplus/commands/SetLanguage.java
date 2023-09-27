package krisapps.nightskipplus.commands;

import krisapps.nightskipplus.NightSkipPlus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetLanguage implements CommandExecutor {

    NightSkipPlus main;

    public SetLanguage(NightSkipPlus main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1) {
            if (main.localizationUtility.languageFileExists(args[0])) {
                main.localizationUtility.changeLanguage(args[0]);
                main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedPhrase("commands.setlanguage.changed")
                        .replaceAll("%lang%", main.localizationUtility.getLocalizedPhrase("languageName"))
                );
            } else {
                main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedPhrase("commands.setlanguage.notfound")
                        .replaceAll("%lang%", args[0])
                );
            }
        } else {
            main.messageUtility.sendMessage(sender, main.localizationUtility.getLocalizedPhrase("commands.setlanguage.insuff"));
        }

        return true;
    }
}
