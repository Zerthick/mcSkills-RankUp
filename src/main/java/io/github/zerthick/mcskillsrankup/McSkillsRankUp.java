package io.github.zerthick.mcskillsrankup;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import io.github.zerthick.mcskills.api.event.experience.McSkillsChangeLevelEvent;
import io.github.zerthick.mcskills.api.event.experience.McSkillsEventContextKeys;
import io.github.zerthick.mcskillsrankup.ladder.RankUpGroup;
import io.github.zerthick.mcskillsrankup.ladder.RankUpLadder;
import io.github.zerthick.mcskillsrankup.player.PlayerGroupManager;
import io.github.zerthick.mcskillsrankup.util.config.RankUpGroupSerializer;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Plugin(
        id = "mcskillsrankup",
        name = "McSkills-RankUp",
        description = "A rank up companion plugin for mcSkills",
        authors = {
            "Zerthick"
        },
        dependencies = {
            @Dependency(id = "mcskills")
        }
)
public class McSkillsRankUp {

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path defaultConfigDir;

    @Inject
    private PluginContainer instance;

    private Set<RankUpLadder> ladders;
    private PlayerGroupManager playerGroupManager;

    @Listener
    public void onGameInit(GameInitializationEvent event) {
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(RankUpGroup.class), new RankUpGroupSerializer());

        ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();

        //Generate default config if it doesn't exist
        if (!defaultConfig.toFile().exists()) {
            Asset defaultConfigAsset = instance.getAsset("mcskillsrankup.conf").get();
            try {
                defaultConfigAsset.copyToFile(defaultConfig);
                configLoader.save(configLoader.load());
            } catch (IOException e) {
                logger.warn("Error loading default config! Error: " + e.getMessage());
            }
        }

        ladders = new HashSet<>();

        try {
            CommentedConfigurationNode ladderNode = configLoader.load().getNode("ladders");
            for (CommentedConfigurationNode node : ladderNode.getChildrenMap().values()) {
                List<RankUpGroup> groups = node.getList(TypeToken.of(RankUpGroup.class));
                ladders.add(new RankUpLadder(node.getKey().toString(), groups));
            }
        } catch (ObjectMappingException | IOException e) {
            logger.warn("Error loading config! Error: " + e.getMessage());
        }

        playerGroupManager = new PlayerGroupManager();

        configLoader = HoconConfigurationLoader.builder().setPath(defaultConfig.resolve("playerdata.conf")).build();

    }


    @Listener
    public void onServerStart(GameStartedServerEvent event) {

        // Log Start Up to Console
        logger.info(
                instance.getName() + " version " + instance.getVersion().orElse("unknown")
                        + " enabled!");
    }

    @Listener
    public void onLevelUp(McSkillsChangeLevelEvent.Up event) {

        Player player = event.getTargetEntity();

        EventContext context = event.getContext();

        Optional<String> skillId = context.get(McSkillsEventContextKeys.MCSKILLS_SKILL_ID);

        int level = event.getLevel();

        Map<String, String> currentPlayerGroups = playerGroupManager.getPlayerGroups(player.getUniqueId());

        skillId.ifPresent(s -> ladders.forEach(ladder -> {

            if (ladder.getId().equals("default") || player.hasPermission("mcskillsrankup.ladder." + ladder.getId().toLowerCase())) {
                if (currentPlayerGroups.containsKey(ladder.getId())) {

                    Optional<RankUpGroup> currentGroupOptional = ladder.getGroup(currentPlayerGroups.get(ladder.getId()));

                    if (currentGroupOptional.isPresent()) {
                        RankUpGroup currentGroup = currentGroupOptional.get();
                        Optional<RankUpGroup> nextGroupOptional = currentGroup.getNext();
                        nextGroupOptional.ifPresent(nextGroup -> {
                            if (nextGroup.meetsRequirements(player, ImmutableMap.of(s, level))) {
                                nextGroup.executeCommands(ImmutableMap.of("%player%", player.getName(),
                                        "%old%", currentGroup.getId(), "%new%", nextGroup.getId()));
                                currentPlayerGroups.put(ladder.getId(), nextGroup.getId());
                            }
                        });
                    } else {
                        logger.warn("Player " + player.getName() + " group data contains an invalid group! Group "
                                + currentPlayerGroups.get(ladder.getId()) + " does not exist! Ignoring...");
                    }

                } else {
                    RankUpGroup root = ladder.getRoot();
                    if (root.meetsRequirements(player, ImmutableMap.of(s, level))) {
                        root.executeCommands(ImmutableMap.of("%player%", player.getName(), "%new%", root.getId()));
                        currentPlayerGroups.put(ladder.getId(), root.getId());
                    }
                }
            }

        }));
    }
}
