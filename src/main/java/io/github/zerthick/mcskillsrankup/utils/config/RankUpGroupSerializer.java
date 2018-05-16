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
