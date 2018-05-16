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

package io.github.zerthick.mcskillsrankup.player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerGroupManager {

    private Map<UUID, Map<String, String>> playerGroups;

    public PlayerGroupManager() {
        playerGroups = new ConcurrentHashMap<>();
    }

    public Map<String, String> getPlayerGroups(UUID playerUUID) {
        return playerGroups.get(playerUUID);
    }

    public void addPlayerGroups(UUID playerUUID, Map<String, String> groups) {
        playerGroups.put(playerUUID, groups);
    }

    public Map<String, String> removePlayerGroups(UUID playerUUID) {
        return playerGroups.remove(playerUUID);
    }

    public Map<UUID, Map<String, String>> getAllPlayerGroups() {
        return playerGroups;
    }
}
