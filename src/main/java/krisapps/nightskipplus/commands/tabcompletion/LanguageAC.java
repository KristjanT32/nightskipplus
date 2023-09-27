package krisapps.nightskipplus.commands.tabcompletion;

import krisapps.nightskipplus.NightSkipPlus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class LanguageAC implements TabCompleter {

    NightSkipPlus main;

    public LanguageAC(NightSkipPlus main) {
        this.main = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(main.localizationUtility.getLanguages());
        }

        return completions;
    }
}
