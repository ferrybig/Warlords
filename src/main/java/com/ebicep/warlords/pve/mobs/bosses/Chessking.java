package com.ebicep.warlords.pve.mobs.bosses;

import com.ebicep.warlords.abilities.internal.AbstractAbility;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.game.option.pve.PveOption;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.pve.mobs.AbstractMob;
import com.ebicep.warlords.pve.mobs.Mob;
import com.ebicep.warlords.pve.mobs.abilities.SpawnMobAbility;
import com.ebicep.warlords.pve.mobs.slime.SlimyChess;
import com.ebicep.warlords.pve.mobs.tiers.BossMob;
import com.ebicep.warlords.util.java.Pair;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import net.citizensnpcs.trait.SlimeSize;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class Chessking extends AbstractMob implements BossMob {

    private static final int MAX_SLIMY_CHESS = 50;

    public Chessking(Location spawnLocation) {
        this(spawnLocation, "Chessking", 75000, 0.3f, 30, 0, 0);
    }

    public Chessking(
            Location spawnLocation,
            String name,
            int maxHealth,
            float walkSpeed,
            int damageResistance,
            float minMeleeDamage,
            float maxMeleeDamage
    ) {
        super(spawnLocation,
                name,
                maxHealth,
                walkSpeed,
                damageResistance,
                minMeleeDamage,
                maxMeleeDamage,
                new Belch(),
                new SpawnMobAbility(
                        20,
                        Mob.SLIME_GUARD
                ) {
                    @Override
                    public int getSpawnAmount() {
                        return (int) pveOption.getGame().warlordsPlayers().count();
                    }
                },
                new SpawnMobAbility(
                        60,
                        Mob.SLIMY_CHESS
                ) {
                    @Override
                    public int getSpawnAmount() {
                        int slimyChessCount = pveOption.getMobs().stream()
                                                       .filter(mob -> mob instanceof SlimyChess)
                                                       .mapToInt(mob -> 1)
                                                       .sum();
                        return Math.min(MAX_SLIMY_CHESS - slimyChessCount, (int) pveOption.getGame().warlordsPlayers().count());
                    }
                }
        );
    }

    @Override
    public Mob getMobRegistry() {
        return Mob.CHESSKING;
    }

    @Override
    public Component getDescription() {
        return Component.text("Goblin from the local basement", NamedTextColor.GRAY);
    }

    @Override
    public NamedTextColor getColor() {
        return NamedTextColor.GREEN;
    }

    @Override
    public void onNPCCreate() {
        npc.getOrAddTrait(SlimeSize.class).setSize(20);
    }

    @Override
    public void whileAlive(int ticksElapsed, PveOption option) {
    }

    @Override
    public void onAttack(WarlordsEntity attacker, WarlordsEntity receiver, WarlordsDamageHealingEvent event) {

    }

    @Override
    public void onDamageTaken(WarlordsEntity self, WarlordsEntity attacker, WarlordsDamageHealingEvent event) {
        if (Utils.isProjectile(event.getAbility())) {
            Utils.playGlobalSound(warlordsNPC.getLocation(), Sound.ENTITY_ARROW_HIT, 2, 0.1f);
            warlordsNPC.addHealingInstance(warlordsNPC, "Blob Heal", 500, 500, -1, 100);
        } else {
            Utils.playGlobalSound(warlordsNPC.getLocation(), Sound.ENTITY_SLIME_ATTACK, 2, 0.2f);
        }
        SlimeSize slimeSize = npc.getOrAddTrait(SlimeSize.class);
        float healthPercent = warlordsNPC.getHealth() / warlordsNPC.getMaxHealth();
        int size = slimeSize.getSize();
        int newSize = (int) (21 * healthPercent);
        if (size != newSize && 0 < newSize && newSize < 21) {
            slimeSize.setSize(newSize);
            int zeroJumpAmplifier = -33;
            float newJumpPower = 1 + ((20 - newSize) * .02f);
            warlordsNPC.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 99999, (int) (newJumpPower * 100 + zeroJumpAmplifier), true, false));
            warlordsNPC.getAbilitiesMatching(Belch.class)
                       .forEach(belch -> belch.setRange(9 - ((20 - newSize) * .2f)));
            warlordsNPC.getAbilitiesMatching(SpawnMobAbility.class)
                       .forEach(spawnMobAbility -> spawnMobAbility.getCooldown().addMultiplicativeModifierAdd("Chessking", -((20 - newSize) * .01f)));
        }
    }

    private static class Belch extends AbstractAbility {

        private float range = 9;

        public Belch() {
            super("Belch", 2800, 3600, 10, 100);
        }

        @Override
        public void updateDescription(Player player) {

        }

        @Override
        public List<Pair<String, String>> getAbilityInfo() {
            return null;
        }

        @Override
        public boolean onActivate(@Nonnull WarlordsEntity wp, @Nullable Player player) {
            wp.subtractEnergy(name, energyCost, false);

            for (WarlordsEntity we : PlayerFilter
                    .entitiesAround(wp, range, range, range)
                    .aliveEnemiesOf(wp)
            ) {
                we.addDamageInstance(
                        wp,
                        name,
                        minDamageHeal,
                        maxDamageHeal,
                        critChance,
                        critMultiplier
                );
            }
            return true;
        }

        public float getRange() {
            return range;
        }

        public void setRange(float range) {
            this.range = range;
        }
    }

}
