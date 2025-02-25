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
            Integer opLevelMax,
            Integer opLevelMin,
            Optional<HashSet<String>> usernamesWhitelisted,
            Optional<HashSet<String>> usernamesBlacklisted,
            Optional<HashSet<String>> teamsWhitelisted,
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
                        Codec.STRING.listOf().optionalFieldOf("teams_whitelist")
                                .xmap(o -> o.map(HashSet::new), o -> o.map(ArrayList::new))
                                .forGetter(Properties::teamsWhitelisted),
                        Codec.STRING.listOf().optionalFieldOf("teams_blacklist")
                                .xmap(o -> o.map(HashSet::new), o -> o.map(ArrayList::new))
                                .forGetter(Properties::teamsBlacklisted)
                ).apply(instance, Properties::new)
        );

        public boolean test(Player player) {
            String username = player.getGameProfile().getName();
            if (usernamesWhitelisted.map(s -> s.contains(username)).orElse(false)) {
                return true;
            }
            if (usernamesBlacklisted.map(s -> s.contains(username)).orElse(false)) {
                return false;
            }

            String team = player.getTeam() == null ? null : player.getTeam().getName();
            if (team != null) {
                if (teamsWhitelisted.map(s -> s.contains(team)).orElse(false)) {
                    return true;
                }
                if (teamsBlacklisted.map(s -> s.contains(team)).orElse(false)) {
                    return false;
                }
            }

            int opLevel = player.getPermissionLevel();
            if (opLevelMin <= opLevel && opLevelMax >= opLevel) {
                return true;
            }

            return false;
        }
    }
}
