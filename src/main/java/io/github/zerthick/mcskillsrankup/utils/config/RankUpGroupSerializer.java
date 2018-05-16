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

package io.github.zerthick.mcskillsrankup.utils.config;

import com.google.common.reflect.TypeToken;
import io.github.zerthick.mcskillsrankup.ladder.RankUpGroup;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.util.TypeTokens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankUpGroupSerializer implements TypeSerializer<RankUpGroup> {
    @Override
    public RankUpGroup deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {

        String id = value.getNode("id").getString();
        List<String> exclude = value.getNode("exclude").getList(TypeTokens.STRING_TOKEN, new ArrayList<>());
        List<String> cmds = value.getNode("cmds").getList(TypeTokens.STRING_TOKEN);
        Map<String, Integer> requirements = value.getNode("requirements").getValue(new TypeToken<Map<String, Integer>>() {
        }, new HashMap<>());

        return new RankUpGroup(id, exclude, cmds, requirements);
    }

    @Override
    public void serialize(TypeToken<?> type, RankUpGroup obj, ConfigurationNode value) {
        throw new UnsupportedOperationException("RankUp Groups should not be serialized to configs!");
    }
}
