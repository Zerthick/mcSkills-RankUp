package io.github.zerthick.mcskillsrankup.ladder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RankUpLadder {

    private final String id;
    private RankUpGroup root;
    private Map<String, RankUpGroup> groupMap;

    public RankUpLadder(String id, List<RankUpGroup> groups) {

        this.id = id;
        root = groups.get(0);
        groupMap = new HashMap<>();

        for (int i = 0; i < groups.size() - 1; i++) {
            RankUpGroup current = groups.get(i);
            groupMap.put(current.getId(), current);
            current.setNext(groups.get(i + 1));
        }

        RankUpGroup last = groups.get(groups.size() - 1);
        groupMap.put(last.getId(), last);
    }

    public String getId() {
        return id;
    }

    public RankUpGroup getRoot() {
        return root;
    }

    public Optional<RankUpGroup> getGroup(String groupId) {
        return Optional.ofNullable(groupMap.get(groupId));
    }
}
