package github.pitbox46.itemblacklist.blacklist;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

public record Group(String name, Properties properties) implements Predicate<Player> {
    public static Codec<Group> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(Group::name),
                    Properties.CODEC.fieldOf("properties").forGetter(Group::properties)
            ).apply(instance, Group::new)
    );

    public boolean test(Player player) {
        return properties.test(player);
    }

    public record Properties(
            Integer opLevelMin,
            Integer opLevelMax,
            Optional<HashSet<String>> usernamesWhitelisted,
            Optional<HashSet<String>> usernamesBlacklisted,
            Optional<HashSet<String>> teams,
            Optional<HashSet<String>> teamsBlacklisted
    ) implements Predicate<Player> {
        public static Codec<Properties> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.optionalFieldOf("op_level_min", 0).forGetter(Properties::opLevelMin),
                        Codec.INT.optionalFieldOf("op_level_max", 5).forGetter(Properties::opLevelMax),
                        ExtraCodecs.PLAYER_NAME.listOf().optionalFieldOf("usernames_whitelist")
                                .xmap(o -> o.map(HashSet::new), o -> o.map(ArrayList::new))
                                .forGetter(Properties::usernamesWhitelisted),
                        ExtraCodecs.PLAYER_NAME.listOf().optionalFieldOf("usernames_blacklist")
                                .xmap(o -> o.map(HashSet::new), o -> o.map(ArrayList::new))
                                .forGetter(Properties::usernamesBlacklisted),
                        Codec.STRING.listOf().optionalFieldOf("teams")
                                .xmap(o -> o.map(HashSet::new), o -> o.map(ArrayList::new))
                                .forGetter(Properties::teams),
                        Codec.STRING.listOf().optionalFieldOf("teams_blacklist")
                                .xmap(o -> o.map(HashSet::new), o -> o.map(ArrayList::new))
                                .forGetter(Properties::teamsBlacklisted)
                ).apply(instance, Properties::new)
        );

        /**
         * Usernames take precedent. If the usernames don't match, then
         * we go to teams. If the player is a part of a team, then blacklisted teams aren't
         * included. If the team is whitelisted, the player is only included if they also match
         * the permission levels.
         * @param player the input argument
         * @return If the player belongs to the group
         */
        public boolean test(Player player) {
            String username = player.getGameProfile().getName();
            if (usernamesWhitelisted.map(s -> s.contains(username)).orElse(false)) {
                return true;
            }
            if (usernamesBlacklisted.map(s -> s.contains(username)).orElse(false)) {
                return false;
            }

            int opLevel = player.getPermissionLevel();
            String team = player.getTeam() == null ? null : player.getTeam().getName();
            if (team == null) {
                return opLevelMin <= opLevel && opLevelMax >= opLevel;
            }

            if (teamsBlacklisted.map(s -> s.contains(team)).orElse(false)) {
                return false;
            }

            if (teams.map(s -> s.contains(team)).orElse(false)) {
                return opLevelMin <= opLevel && opLevelMax >= opLevel;
            }

            return false;
        }
    }
}
