package io.github.zerthick.mcskillsrankup.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerGroupManager {

    private Map<UUID, Map<String, String>> playerGroups;

    public PlayerGroupManager() {
        playerGroups = new HashMap<>();
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
}
