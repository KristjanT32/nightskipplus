package krisapps.nightskipplus.events;

import krisapps.nightskipplus.NightSkipPlus;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class NightListener implements Listener {

    NightSkipPlus main;
    HashMap<UUID, Integer> taskIDList = new HashMap<>();

    public NightListener(NightSkipPlus main) {
        this.main = main;
    }

    @EventHandler
    public void onNight(NightStartEvent nightStartEvent) {

        if (!nightStartEvent.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            main.appendToLog("Skipped world: " + nightStartEvent.getWorld().getName() + " (" + nightStartEvent.getWorld().getUID().toString() + ") since it's a dimension.");
            return;
        }

        main.appendToLog("Started voting for world: " + nightStartEvent.getWorld().getName() + " (" + nightStartEvent.getWorld().getUID().toString() + ")");

        main.dataUtility.startVotingForWorld(nightStartEvent.getWorld().getUID());
        for (Player p : nightStartEvent.getWorld().getPlayers()) {
            p.spigot().sendMessage(
                    new TextComponent(ChatColor.translateAlternateColorCodes('&', "&e=====================================================\n")),
                    new TextComponent(ChatColor.translateAlternateColorCodes('&',
                            main.localizationUtility.getLocalizedPhrase("messages.night-voting.header")
                                    .replaceAll("%duration%", String.valueOf(main.dataUtility.getVotingDuration()))
                    )),
                    main.messageUtility.createClickableButton("messages.night-voting.skip-button.text", "/votefor " + p.getUniqueId().toString(), "messages.night-voting.skip-button.hover"),
                    new TextComponent(ChatColor.translateAlternateColorCodes('&', "   &for   ")),
                    main.messageUtility.createClickableButton("messages.night-voting.sleep-through.text", "/voteagainst " + p.getUniqueId().toString(), "messages.night-voting.sleep-through.hover"),
                    new TextComponent(ChatColor.translateAlternateColorCodes('&', "\n&e====================================================="))
            );
        }
        scheduleFinishVoting(nightStartEvent.getWorld());
    }

    public void scheduleFinishVoting(World world) {

        if (taskIDList.get(world.getUID()) != null) {
            if (taskIDList.get(world.getUID()) != -1) {

                main.getServer().getScheduler().cancelTask(taskIDList.get(world.getUID()));
                taskIDList.replace(world.getUID(), -1);

                main.pluginData.set(world.getUID().toString() + ".finish_task", "-1");
                main.saveData();
            } else {
                main.pluginData.set(world.getUID().toString() + ".finish_task", "-1");
                main.saveData();
            }
        } else {
            taskIDList.put(world.getUID(), -1);

            main.pluginData.set(world.getUID().toString() + ".finish_task", "-1");
            main.saveData();
        }

        int task = main.getServer().getScheduler().scheduleAsyncRepeatingTask(main, new Runnable() {
            int timer = main.dataUtility.getVotingDuration();

            @Override
            public void run() {

                if (timer > 0) {
                    for (Player p : world.getPlayers()) {
                        switch (timer) {
                            case 5:
                            case 4:
                            case 3:
                            case 2:
                            case 1:
                                if (!main.dataUtility.hasVoted(p.getUniqueId(), world.getUID())) {
                                    main.messageUtility.sendActionbarMessage(p, main.localizationUtility.getLocalizedPhrase("messages.night-voting.actionbar-timer-finals")
                                            .replaceAll("%timer%", main.dataUtility.generateTimeStringFromSeconds(timer))
                                    );
                                } else {
                                    main.messageUtility.sendActionbarMessage(p, main.localizationUtility.getLocalizedPhrase("messages.night-voting.actionbar-done")
                                            .replaceAll("%vote%", main.dataUtility.votedAgainst(p.getUniqueId(), world.getUID()) ?
                                                    "&c&l" + main.localizationUtility.getLocalizedPhrase("messages.night-voting.against")
                                                    : "&a&l" + main.localizationUtility.getLocalizedPhrase("messages.night-voting.for")
                                            )
                                            .replaceAll("%timer%", main.dataUtility.generateTimeStringFromSeconds(timer))
                                    );
                                }
                                break;
                            default:
                                if (!main.dataUtility.hasVoted(p.getUniqueId(), world.getUID())) {
                                    main.messageUtility.sendActionbarMessage(p, main.localizationUtility.getLocalizedPhrase("messages.night-voting.actionbar-timer")
                                            .replaceAll("%timer%", main.dataUtility.generateTimeStringFromSeconds(timer))
                                    );
                                } else {
                                    main.messageUtility.sendActionbarMessage(p, main.localizationUtility.getLocalizedPhrase("messages.night-voting.actionbar-done")
                                            .replaceAll("%vote%", main.dataUtility.votedAgainst(p.getUniqueId(), world.getUID()) ?
                                                    "&c&l" + main.localizationUtility.getLocalizedPhrase("messages.night-voting.against")
                                                    : "&a&l" + main.localizationUtility.getLocalizedPhrase("messages.night-voting.for")
                                            )
                                            .replaceAll("%timer%", main.dataUtility.generateTimeStringFromSeconds(timer))
                                    );
                                }
                                break;
                        }
                    }
                    timer--;
                } else {
                    main.appendToLog("Stopped voting task for world: " + world.getName() + " (" + world.getUID().toString() + ")");

                    main.getServer().getScheduler().cancelTask(taskIDList.get(world.getUID()));
                    taskIDList.replace(world.getUID(), -1);
                    main.pluginData.set(world.getUID().toString() + ".finish_task", "-1");
                    main.dataUtility.setVotingActive(world.getUID(), false);
                    main.saveData();


                    for (Player p : world.getPlayers()) {
                        main.messageUtility.sendActionbarMessage(p, main.localizationUtility.getLocalizedPhrase("messages.night-voting.ended"));
                    }

                    for (Player p : world.getPlayers()) {
                        main.messageUtility.sendMessage(p, main.localizationUtility.getLocalizedPhrase("messages.night-voting.results")
                                .replaceAll("%infavour%", String.valueOf(main.dataUtility.getVotes(world.getUID(), false)))
                                .replaceAll("%against%", String.valueOf(main.dataUtility.getVotes(world.getUID(), true)))
                        );
                    }


                    main.appendToLog("Deciding what to do based on data: IN FAVOUR: " + main.dataUtility.getVotes(world.getUID(), false) + "| AGAINST: " + main.dataUtility.getVotes(world.getUID(), true));

                    if (((double) main.dataUtility.getVotes(world.getUID(), false) / world.getPlayers().size()) * 100 >= main.dataUtility.getRequiredVotePercentage()) {
                        // Skip night

                        main.appendToLog("[" + world.getName() + "]: Skipping the night");

                        main.getServer().getScheduler().runTask(main, () -> {
                            world.setTime(1000);
                        });
                        for (Player p : world.getPlayers()) {
                            main.messageUtility.sendMessage(p, main.localizationUtility.getLocalizedPhrase("messages.night-voting.result-skipping"));
                        }

                    } else {
                        // Don't skip the night

                        main.appendToLog("[" + world.getName() + "]: Sleeping through the night");

                        for (Player p : world.getPlayers()) {
                            main.messageUtility.sendMessage(p, main.localizationUtility.getLocalizedPhrase("messages.night-voting.result-notskipping"));
                        }
                    }

                    main.cooldowns.replace(world.getUID(), true);
                }
            }
        }, 0, 20L);

        taskIDList.replace(world.getUID(), task);
        main.pluginData.set(world.getUID().toString() + ".finish_task", task);
        main.saveData();
    }

}
