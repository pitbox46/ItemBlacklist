package github.pitbox46.itemblacklist.blacklist;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.mixins.EntityAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

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
            Optional<Integer> opLevelMin,
            Optional<Integer> opLevelMax,
            Optional<HashSet<String>> usernames,
            Optional<HashSet<String>> usernamesBlacklisted,
            Optional<HashSet<String>> teams,
            Optional<HashSet<String>> teamsBlacklisted,
            Optional<HashSet<GameType>> gameTypes
    ) implements Predicate<Player> {
        public static Properties EMPTY = new Properties(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        public static Codec<Properties> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.optionalFieldOf("op_level_min").forGetter(Properties::opLevelMin),
                        Codec.INT.optionalFieldOf("op_level_max").forGetter(Properties::opLevelMax),
                        ExtraCodecs.PLAYER_NAME.listOf().optionalFieldOf("usernames")
                                .xmap(o -> o.map(HashSet::new), o -> o.map(ArrayList::new))
                                .forGetter(Properties::usernames),
                        ExtraCodecs.PLAYER_NAME.listOf().optionalFieldOf("usernames_blacklist")
                                .xmap(o -> o.map(HashSet::new), o -> o.map(ArrayList::new))
                                .forGetter(Properties::usernamesBlacklisted),
                        Codec.STRING.listOf().optionalFieldOf("teams")
                                .xmap(o -> o.map(HashSet::new), o -> o.map(ArrayList::new))
                                .forGetter(Properties::teams),
                        Codec.STRING.listOf().optionalFieldOf("teams_blacklist")
                                .xmap(o -> o.map(HashSet::new), o -> o.map(ArrayList::new))
                                .forGetter(Properties::teamsBlacklisted),
                        GameType.CODEC.listOf().optionalFieldOf("game_types")
                                .xmap(o -> o.map(HashSet::new), o -> o.map(ArrayList::new))
                                .forGetter(Properties::gameTypes)
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
            if (this.equals(EMPTY)) {
                return true;
            }

            String username = player.getGameProfile().getName();
            if (usernamesBlacklisted.map(s -> s.contains(username)).orElse(false)) {
                return false;
            }

            int opLevel = ((EntityAccessor) player).callGetPermissionLevel();
            if (opLevelMin.map(i -> opLevel < i).orElse(false)) {
                return false;
            }
            if (opLevelMax.map(i -> opLevel > i).orElse(false)) {
                return false;
            }

            if (player instanceof ServerPlayer serverPlayer &&
                    gameTypes.map(types -> !types.contains(serverPlayer.gameMode.getGameModeForPlayer())).orElse(false)) {
                return false;
            }

            String team = player.getTeam() == null ? null : player.getTeam().getName();
            boolean teamFlag = teams.isEmpty();
            if (team != null) {
                if (teamsBlacklisted.map(s -> s.contains(team)).orElse(false)) {
                    return false;
                }
                teamFlag = teams.map(s -> s.contains(team)).orElse(false);
            }

            return teamFlag && usernames.map(s -> s.contains(username)).orElse(true);
        }
    }
}
