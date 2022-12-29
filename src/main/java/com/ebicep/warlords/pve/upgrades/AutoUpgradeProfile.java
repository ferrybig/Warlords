package com.ebicep.warlords.pve.upgrades;

import net.md_5.bungee.api.ChatColor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class AutoUpgradeProfile {

    public static final String AUTO_UPGRADE_PREFIX = ChatColor.AQUA + "Auto Upgrade" + ChatColor.DARK_GRAY + " > ";

    @Field("profiles")
    private List<AutoUpgradeEntry> autoUpgradeEntries = new ArrayList<>();

    public AutoUpgradeProfile() {
    }

    public AutoUpgradeProfile(AutoUpgradeProfile autoUpgradeProfile) {
        this.autoUpgradeEntries = new ArrayList<>(autoUpgradeProfile.autoUpgradeEntries);
    }

    public void addEntry(int branchIndex, int upgradeIndex, AutoUpgradeEntry.UpgradeType upgradeType) {
        autoUpgradeEntries.add(new AutoUpgradeEntry(branchIndex, upgradeIndex, upgradeType));
    }

    public List<String> getLore(AbilityTree abilityTree) {
        List<String> lore = new ArrayList<>();
        List<AbstractUpgradeBranch<?>> upgradeBranches = abilityTree.getUpgradeBranches();
        for (int i = 0, autoUpgradeEntriesSize = autoUpgradeEntries.size(); i < autoUpgradeEntriesSize; i++) {
            AutoUpgradeEntry entry = autoUpgradeEntries.get(i);
            AbstractUpgradeBranch<?> upgradeBranch = upgradeBranches.get(entry.getBranchIndex());
            Upgrade upgrade = entry.getUpgradeType().getUpgradeFunction.apply(upgradeBranch).get(entry.getUpgradeIndex());
            ChatColor chatColor = upgrade.isUnlocked() ? ChatColor.GOLD : ChatColor.GRAY;
            String position = ChatColor.AQUA.toString() + (i + 1) + ". ";
            if (entry.getUpgradeType() == AutoUpgradeEntry.UpgradeType.MASTER) {
                lore.add(position + chatColor + upgradeBranch.getAbility().getName() + " - §c§l" + upgrade.getName());
            } else {
                lore.add(position + chatColor + upgradeBranch.getAbility().getName() + " - " + upgrade.getName());
            }
        }
        return lore;
    }

    public String getPosition(AbilityTree abilityTree, Upgrade upgrade) {
        List<AbstractUpgradeBranch<?>> upgradeBranches = abilityTree.getUpgradeBranches();
        for (int i = 0, autoUpgradeEntriesSize = autoUpgradeEntries.size(); i < autoUpgradeEntriesSize; i++) {
            AutoUpgradeEntry entry = autoUpgradeEntries.get(i);
            AbstractUpgradeBranch<?> upgradeBranch = upgradeBranches.get(entry.getBranchIndex());
            Upgrade currentUpgrade = entry.getUpgradeType().getUpgradeFunction.apply(upgradeBranch).get(entry.getUpgradeIndex());
            if (currentUpgrade == upgrade) {
                return ChatColor.AQUA + "#" + (i + 1) + "/" + autoUpgradeEntries.size();
            }
        }
        return null;
    }

    public List<AutoUpgradeEntry> getAutoUpgradeEntries() {
        return autoUpgradeEntries;
    }

    @Override
    public int hashCode() {
        return Objects.hash(autoUpgradeEntries);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AutoUpgradeProfile otherProfile = (AutoUpgradeProfile) o;
        List<AutoUpgradeEntry> otherEntries = otherProfile.getAutoUpgradeEntries();
        if (otherEntries.size() != autoUpgradeEntries.size()) {
            return false;
        }
        for (int i = 0; i < autoUpgradeEntries.size(); i++) {
            AutoUpgradeEntry entry = autoUpgradeEntries.get(i);
            AutoUpgradeEntry otherEntry = otherEntries.get(i);
            if (!entry.equals(otherEntry)) {
                return false;
            }
        }
        return true;
    }

    public static class AutoUpgradeEntry {

        @Field("branch_index")
        private int branchIndex;
        @Field("upgrade_index")
        private int upgradeIndex;
        @Field("type")
        private UpgradeType upgradeType;

        public AutoUpgradeEntry(int branchIndex, int upgradeIndex, UpgradeType upgradeType) {
            this.branchIndex = branchIndex;
            this.upgradeIndex = upgradeIndex;
            this.upgradeType = upgradeType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(branchIndex, upgradeIndex, upgradeType);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AutoUpgradeEntry that = (AutoUpgradeEntry) o;
            return branchIndex == that.branchIndex && upgradeIndex == that.upgradeIndex && upgradeType == that.upgradeType;
        }

        public int getBranchIndex() {
            return branchIndex;
        }

        public int getUpgradeIndex() {
            return upgradeIndex;
        }

        public UpgradeType getUpgradeType() {
            return upgradeType;
        }

        public enum UpgradeType {
            A(AbstractUpgradeBranch::getTreeA),
            B(AbstractUpgradeBranch::getTreeB),
            MASTER(upgradeBranch -> Collections.singletonList(upgradeBranch.getMasterUpgrade()));

            public final Function<AbstractUpgradeBranch<?>, List<Upgrade>> getUpgradeFunction;

            UpgradeType(Function<AbstractUpgradeBranch<?>, List<Upgrade>> getUpgradeFunction) {
                this.getUpgradeFunction = getUpgradeFunction;
            }
        }
    }
}
