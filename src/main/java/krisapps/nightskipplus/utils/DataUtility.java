package krisapps.nightskipplus.utils;

import krisapps.nightskipplus.NightSkipPlus;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DataUtility {

    NightSkipPlus main;

    public DataUtility(NightSkipPlus main) {
        this.main = main;
    }

    public static String formatTimeUnit(int unit) {
        return unit <= 9
                ? "0" + unit
                : String.valueOf(unit);
    }

    public String getCurrentLanguage() {
        return main.pluginConfig.getString("settings.language");
    }

    public void setCurrentLanguage(String lan) {
        main.pluginConfig.set("settings.language", lan);
        main.saveConfig();
    }

    public Date generateExpirationDate(Date startingDate, int duration) {
        return new Date(startingDate.getTime() + TimeUnit.MINUTES.toMillis(duration));
    }

    public String generateDurationString(Date start, Date current) {
        Instant startInstant = start.toInstant();
        Instant endInstant = current.toInstant();

        Duration dur = Duration.between(startInstant, endInstant);

        long hours = Math.abs(dur.toHours());
        long minutes = Math.abs(dur.minusHours(hours).toMinutes());
        long seconds = Math.abs(dur.minusHours(hours).minusMinutes(minutes).toSeconds());

        return String.format("%s:%s:%s", formatTimeUnit((int) hours), formatTimeUnit((int) minutes), formatTimeUnit((int) seconds));
    }

    public String generateTimeStringFromSeconds(int sec) {
        int minutes = (sec / 60);
        int hours = (sec / 60) / 60;

        int seconds = sec - (hours * 60 * 60);
        seconds = seconds - (minutes * 60);

        return String.format("%s:%s:%s", formatTimeUnit((int) hours), formatTimeUnit((int) minutes), formatTimeUnit((int) seconds));
    }

    public void startVotingForWorld(UUID worldUUID) {
        main.pluginData.set("worlds." + worldUUID.toString() + ".voting.active", true);
        main.pluginData.set("worlds." + worldUUID.toString() + ".voting.responses", new ArrayList<>());
        main.pluginData.set("worlds." + worldUUID.toString() + ".voting.duration", main.pluginConfig.getInt("settings.voting-duration"));
        main.saveData();
    }

    public void setVotingActive(UUID world, boolean active) {
        main.pluginData.set("worlds." + world.toString() + ".voting.active", active);
        main.saveData();

        main.getServer().getScheduler().cancelTask(main.pluginData.getInt(world + ".finish_task"));
    }

    public boolean hasVoted(UUID player, UUID world) {
        return getVotingResponses(world).contains(player.toString());
    }

    public void setVote(UUID player, UUID world, boolean against) {
        main.pluginData.set("worlds." + world.toString() + ".voting.responses." + player.toString() + ".against", against);
        main.saveData();
    }

    public boolean votedAgainst(UUID player, UUID world) {
        return main.pluginData.getBoolean("worlds." + world + ".voting.responses." + player + ".against", false);
    }

    public int getVotes(UUID world, boolean against) {
        Set<String> responses = new HashSet<>();
        Set<UUID> out = new HashSet<>();
        if (main.pluginData.getConfigurationSection("worlds." + world + ".voting.responses") != null) {
            responses = main.pluginData.getConfigurationSection("worlds." + world + ".voting.responses").getKeys(false);
            for (String player : responses) {
                if (votedAgainst(UUID.fromString(player), world) == against) {
                    out.add(UUID.fromString(player));
                }
            }
        } else {
            return 0;
        }
        return out.size();
    }

    public boolean votingActive(UUID world) {
        return main.pluginData.getBoolean("worlds." + world + ".voting.active", false);
    }

    public Set<String> getVotingResponses(UUID worldUUID) {
        if (main.pluginData.getConfigurationSection("worlds." + worldUUID.toString() + ".voting.responses") != null) {
            return main.pluginData.getConfigurationSection("worlds." + worldUUID.toString() + ".voting.responses").getKeys(false);
        } else {
            main.pluginData.set("worlds." + worldUUID.toString() + ".voting.responses", new HashSet<String>());
            main.saveData();

            return new HashSet<>();
        }
    }

    public int getVotingDuration() {
        return main.pluginConfig.getInt("settings.voting-duration");
    }

    public double getRequiredVotePercentage() {
        return main.pluginConfig.getDouble("settings.voting-minimum");
    }
}
