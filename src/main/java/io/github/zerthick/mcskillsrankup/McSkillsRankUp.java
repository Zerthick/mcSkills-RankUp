package io.github.zerthick.mcskillsrankup;

import com.google.inject.Inject;
import io.github.zerthick.mcskills.api.event.experience.McSkillsChangeLevelEvent;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

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

    @Listener
    public void onLevelUp(McSkillsChangeLevelEvent.Up event) {
        event.getTargetEntity().sendMessage(Text.of("Hello from mcSkills-Rankup"));
    }
}
