package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.fx.EntityFXFacingParticle;
import hellfirepvp.astralsorcery.common.base.CropTypes;
import hellfirepvp.astralsorcery.common.base.TileAccelerationBlacklist;
import hellfirepvp.astralsorcery.common.base.WorldMeltables;
import hellfirepvp.astralsorcery.common.constellation.Constellation;
import hellfirepvp.astralsorcery.common.constellation.effect.CEffectPositionList;
import hellfirepvp.astralsorcery.common.constellation.effect.CEffectPositionMap;
import hellfirepvp.astralsorcery.common.lib.Constellations;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.packet.server.PktParticleEvent;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectHorologium
 * Created by HellFirePvP
 * Date: 01.11.2016 / 02:31
 */
public class CEffectHorologium extends CEffectPositionList {

    //public static final int MAX_SEARCH_RANGE = 8;
    //public static final int MAX_ACCEL_COUNT = 30;

    public boolean enabled = true;
    public static double potencyMultiplier = 1;

    public static int searchRange = 8;
    public static int maxCount = 30;

    public CEffectHorologium() {
        super(Constellations.horologium, "horologium", searchRange, maxCount, (world, pos) -> TileAccelerationBlacklist.canAccelerate(world.getTileEntity(pos)));
    }

    @Override
    public boolean playMainEffect(World world, BlockPos pos, float percStrength, boolean mayDoTraitEffect, @Nullable Constellation possibleTraitEffect) {
        if(!enabled) return false;
        percStrength *= potencyMultiplier;
        if(percStrength < 1) {
            if(world.rand.nextFloat() > percStrength) return false;
        }

        boolean changed = false;
        if(doRandomOnPositions(world)) {
            BlockPos sel = positions.get(world.rand.nextInt(positions.size()));
            if(MiscUtils.isChunkLoaded(world, new ChunkPos(sel))) {
                TileEntity te = world.getTileEntity(sel);
                if(TileAccelerationBlacklist.canAccelerate(te)) { //Does != null && instanceof ITickable check.
                    PktParticleEvent ev = new PktParticleEvent(PktParticleEvent.ParticleEventType.CE_ACCEL_TILE, sel.getX(), sel.getY(), sel.getZ());
                    PacketChannel.CHANNEL.sendToAllAround(ev, PacketChannel.pointFromPos(world, sel, 16));
                    try {
                        long startNs = System.nanoTime();
                        int times = 5 + rand.nextInt(3);
                        while (times > 0) {
                            ((ITickable) te).update();
                            if((System.nanoTime() - startNs) >= 80_000) {
                                break;
                            }
                            times--;
                        }
                    } catch (Exception exc) {
                        TileAccelerationBlacklist.errored(te.getClass());
                        positions.remove(sel);
                        AstralSorcery.log.warn("Couldn't accelerate TileEntity " + te.getClass().getName() + " properly.");
                        AstralSorcery.log.warn("Temporarily blacklisting that class. Consider adding that to the blacklist if it persists?");
                        exc.printStackTrace();
                    }
                } else {
                    positions.remove(sel);
                }
            }
        }

        if(super.playMainEffect(world, pos, percStrength, mayDoTraitEffect, possibleTraitEffect)) changed = true;

        return changed;
    }

    @Override
    public boolean playTraitEffect(World world, BlockPos pos, Constellation traitType, float traitStrength) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public static void playParticles(PktParticleEvent event) {
        Vector3 at = event.getVec();
        EntityFXFacingParticle p = EffectHelper.genericFlareParticle(
                at.getX() + 0.5,
                at.getY() + 0.5,
                at.getZ() + 0.5);
        p.motion((rand.nextFloat() * 0.03F) * (rand.nextBoolean() ? 1 : -1),
                 (rand.nextFloat() * 0.03F) * (rand.nextBoolean() ? 1 : -1),
                 (rand.nextFloat() * 0.03F) * (rand.nextBoolean() ? 1 : -1));
        p.scale(0.25F).setColor(Color.CYAN.brighter());
    }

    @Override
    public void loadFromConfig(Configuration cfg) {
        searchRange = cfg.getInt(getKey() + "Range", getConfigurationSection(), 8, 1, 32, "Defines the radius (in blocks) in which the ritual will search for valid tileEntities to accelerate");
        maxCount = cfg.getInt(getKey() + "Count", getConfigurationSection(), 30, 1, 4000, "Defines the amount of tileEntities the ritual can cache and accelerate at max count");
        enabled = cfg.getBoolean(getKey() + "Enabled", getConfigurationSection(), true, "Set to false to disable this ConstellationEffect.");
        potencyMultiplier = cfg.getFloat(getKey() + "PotencyMultiplier", getConfigurationSection(), 1.0F, 0.01F, 100F, "Set the potency multiplier for this ritual effect. Will affect all ritual effects and their efficiency.");
    }

}