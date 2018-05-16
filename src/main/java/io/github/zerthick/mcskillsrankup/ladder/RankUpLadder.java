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
