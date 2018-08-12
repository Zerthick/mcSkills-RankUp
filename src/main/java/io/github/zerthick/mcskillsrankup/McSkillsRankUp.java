/*
 * Copyright (C) 2018  Zerthick
 *
 * This file is part of mcSkills-RankUp.
 *
 * mcSkills-RankUp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * mcSkills-RankUp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mcSkills-RankUp.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.zerthick.mcskillsrankup;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import io.github.zerthick.mcskills.api.event.experience.McSkillsChangeLevelEvent;
import io.github.zerthick.mcskills.api.event.experience.McSkillsEventContextKeys;
import io.github.zerthick.mcskillsrankup.ladder.RankUpGroup;
import io.github.zerthick.mcskillsrankup.ladder.RankUpLadder;
import io.github.zerthick.mcskillsrankup.player.PlayerGroupManager;
import io.github.zerthick.mcskillsrankup.utils.config.RankUpGroupSerializer;
import io.github.zerthick.mcskillsrankup.utils.database.Database;
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
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private Database db;
    private PlayerGroupManager playerGroupManager;

    public Logger getLogger() {
        return logger;
    }

    public Path getDefaultConfig() {
        return defaultConfig;
    }

    public Path getDefaultConfigDir() {
        return defaultConfigDir;
    }

    public PluginContainer getInstance() {
        return instance;
    }

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

        db = new Database(this);

        playerGroupManager = new PlayerGroupManager();

        // Save all accounts to the DB asynchronously every 5 mins
        Task.builder()
                .async()
                .interval(5, TimeUnit.MINUTES)
                .name("McSkillsRankUp Player Data Save Task")
                .execute(() -> playerGroupManager.getAllPlayerGroups()
                        .forEach((player, ladders) -> db.savePlayerData(player, ladders)))
                .submit(this);
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

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {

        // Load player data into the cache if it exists, else created a blank entry
        UUID playerUUID = player.getUniqueId();
        Optional<Map<String, String>> playerGroupsOptional = db.getPlayerData(playerUUID);
        if (playerGroupsOptional.isPresent()) {
            playerGroupManager.addPlayerGroups(playerUUID, playerGroupsOptional.get());
        } else {
            playerGroupManager.addPlayerGroups(playerUUID, new HashMap<>());
        }
    }

    @Listener
    public void onPlayerLeave(ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player player) {

        // Remove player account from the cache and save it to the DB
        UUID playerUUID = player.getUniqueId();
        db.savePlayerData(playerUUID, playerGroupManager.removePlayerGroups(playerUUID));
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {

        // Save any remaining player data
        playerGroupManager.getAllPlayerGroups()
                .forEach((player, ladders) -> db.savePlayerData(player, ladders));
    }
}
