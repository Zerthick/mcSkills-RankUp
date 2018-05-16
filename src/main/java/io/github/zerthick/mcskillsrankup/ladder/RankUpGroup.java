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
