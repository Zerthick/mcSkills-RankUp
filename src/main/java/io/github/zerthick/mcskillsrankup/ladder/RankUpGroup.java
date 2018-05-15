package io.github.zerthick.mcskillsrankup.ladder;

import io.github.zerthick.mcskills.api.account.McSkillsAccount;
import io.github.zerthick.mcskills.api.account.McSkillsAccountService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RankUpGroup {

    private final String id;
    private List<String> exclude;
    private List<String> cmds;
    private Map<String, Integer> requirements;
    private RankUpGroup next;

    public RankUpGroup(String id, List<String> exclude, List<String> cmds, Map<String, Integer> requirements) {
        this.id = id;
        this.exclude = exclude;
        this.cmds = cmds;
        this.requirements = requirements;
    }

    public String getId() {
        return id;
    }

    public List<String> getExclude() {
        return exclude;
    }

    public List<String> getCmds() {
        return cmds;
    }

    public Map<String, Integer> getRequirements() {
        return requirements;
    }

    public boolean isExcluded(String perm) {
        return exclude.contains(perm);
    }

    public boolean meetsRequirements(Player player, Map<String, Integer> newLevels) {

        McSkillsAccountService accountService = Sponge.getServiceManager().provideUnchecked(McSkillsAccountService.class);
        McSkillsAccount account = accountService.getOrCreateAccount(player.getUniqueId());

        for (Map.Entry<String, Integer> requirement : requirements.entrySet()) {

            String requiredSkill = requirement.getKey();

            int playerLevel = account.getSkillLevel(requiredSkill);
            if (newLevels.containsKey(requiredSkill)) {
                playerLevel = newLevels.get(requiredSkill);
            }
            if (playerLevel < requirement.getValue()) {
                return false;
            }
        }

        for (String exclusion : exclude) {
            if (player.hasPermission(exclusion)) {
                return false;
            }
        }

        return true;
    }

    public void executeCommands(Map<String, String> dropIns) {
        for (String cmd : cmds) {
            String processedCmd = cmd;
            for (String dropIn : dropIns.keySet()) {
                processedCmd = processedCmd.replaceAll(dropIn, dropIns.get(dropIn));
            }
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), processedCmd);
        }
    }

    public Optional<RankUpGroup> getNext() {
        return Optional.ofNullable(next);
    }

    public void setNext(RankUpGroup next) {
        this.next = next;
    }
}
