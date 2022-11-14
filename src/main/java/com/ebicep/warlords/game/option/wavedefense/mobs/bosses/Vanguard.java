package com.ebicep.warlords.game.option.wavedefense.mobs.bosses;

import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.effects.FireWorkEffectPlayer;
import com.ebicep.warlords.events.player.ingame.WarlordsDamageHealingEvent;
import com.ebicep.warlords.game.option.wavedefense.WaveDefenseOption;
import com.ebicep.warlords.game.option.wavedefense.mobs.MobTier;
import com.ebicep.warlords.game.option.wavedefense.mobs.mobtypes.BossMob;
import com.ebicep.warlords.game.option.wavedefense.mobs.spider.Spider;
import com.ebicep.warlords.game.option.wavedefense.mobs.zombie.AbstractZombie;
import com.ebicep.warlords.game.option.wavedefense.mobs.zombie.ExiledZombie;
import com.ebicep.warlords.player.general.Weapons;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import com.ebicep.warlords.util.bukkit.PacketUtils;
import com.ebicep.warlords.util.pve.SkullID;
import com.ebicep.warlords.util.pve.SkullUtils;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class Vanguard extends AbstractZombie implements BossMob {

    private boolean phaseOneTriggered = false;
    private boolean phaseTwoTriggered = false;
    private boolean phaseThreeTriggered = false;
    private boolean phaseFourTriggered = false;

    public Vanguard(Location spawnLocation) {
        super(spawnLocation,
                "Vanguard",
                MobTier.BOSS,
                new Utils.SimpleEntityEquipment(
                        SkullUtils.getSkullFrom(SkullID.SCULK_CORRUPTION),
                        Utils.applyColorTo(Material.LEATHER_CHESTPLATE, 200, 200, 200),
                        Utils.applyColorTo(Material.LEATHER_LEGGINGS, 200, 200, 200),
                        Utils.applyColorTo(Material.LEATHER_BOOTS, 200, 200, 200),
                        Weapons.SILVER_PHANTASM_SWORD_3.getItem()
                ),
                30000,
                0.15f,
                10,
                3200,
                3600
        );
    }

    @Override
    public void onSpawn(WaveDefenseOption option) {
        for (WarlordsEntity we : PlayerFilter.playingGame(getWarlordsNPC().getGame())) {
            if (we.getEntity() instanceof Player) {
                PacketUtils.sendTitle(
                        (Player) we.getEntity(),
                        ChatColor.LIGHT_PURPLE + "Vanguard",
                        ChatColor.BLACK + "General of the Illusion Legion",
                        20, 30, 20
                );
            }
        }

        for (int i = 0; i < (2 * option.getGame().warlordsPlayers().count()); i++) {
            option.spawnNewMob(new ExiledZombie(spawnLocation));
        }
    }

    @Override
    public void whileAlive(int ticksElapsed, WaveDefenseOption option) {
        long playerCount = option.getGame().warlordsPlayers().count();

        if (warlordsNPC.getHealth() < (warlordsNPC.getMaxHealth() * .75f)) {
            phaseOneTriggered = true;
        }

        if (warlordsNPC.getHealth() < (warlordsNPC.getMaxHealth() * .5f)) {
            phaseOneTriggered = false;
            phaseTwoTriggered = true;
        }

        if (warlordsNPC.getHealth() < (warlordsNPC.getMaxHealth() * .25f)) {
            phaseTwoTriggered = false;
            phaseThreeTriggered = true;
        }

        if (ticksElapsed % 60 == 0 && phaseOneTriggered) {

        }

        if (ticksElapsed % 60 == 0 && phaseTwoTriggered) {

        }

        if (ticksElapsed % 20 == 0 && phaseThreeTriggered) {
            for (WarlordsEntity we : PlayerFilter
                    .entitiesAround(warlordsNPC, 100, 100, 100)
                    .aliveEnemiesOf(warlordsNPC)
            ) {
                EffectUtils.playParticleLinkAnimation(we.getLocation(), warlordsNPC.getLocation(), 255, 255, 255, 3);
                we.addDamageInstance(warlordsNPC, "Vampiric Leash", 250, 250, -1, 100, true);
            }
        }
    }

    @Override
    public void onAttack(WarlordsEntity attacker, WarlordsEntity receiver, WarlordsDamageHealingEvent event) {
        FireWorkEffectPlayer.playFirework(receiver.getLocation(), FireworkEffect.builder()
                .withColor(Color.BLACK)
                .withColor(Color.WHITE)
                .with(FireworkEffect.Type.BALL)
                .build());
    }

    @Override
    public void onDamageTaken(WarlordsEntity self, WarlordsEntity attacker, WarlordsDamageHealingEvent event) {

    }

    @Override
    public void onDeath(WarlordsEntity killer, Location deathLocation, WaveDefenseOption option) {
        FireWorkEffectPlayer.playFirework(deathLocation, FireworkEffect.builder()
                .withColor(Color.BLACK)
                .withColor(Color.WHITE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .build());
        EffectUtils.strikeLightning(deathLocation, false, 2);
    }
}
