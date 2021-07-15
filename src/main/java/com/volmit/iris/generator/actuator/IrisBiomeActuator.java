/*
 * Iris is a World Generator for Minecraft Bukkit Servers
 * Copyright (c) 2021 Arcane Arts (Volmit Software)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.volmit.iris.generator.actuator;

import com.volmit.iris.Iris;
import com.volmit.iris.nms.INMS;
import com.volmit.iris.object.IrisBiome;
import com.volmit.iris.object.IrisBiomeCustom;
import com.volmit.iris.scaffold.engine.Engine;
import com.volmit.iris.scaffold.engine.EngineAssignedActuator;
import com.volmit.iris.scaffold.hunk.Hunk;
import com.volmit.iris.scaffold.hunk.view.BiomeGridHunkView;
import com.volmit.iris.scaffold.parallel.BurstExecutor;
import com.volmit.iris.scaffold.parallel.MultiBurst;
import com.volmit.iris.util.PrecisionStopwatch;
import com.volmit.iris.util.RNG;
import com.volmit.iris.util.TerrainChunk;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

public class IrisBiomeActuator extends EngineAssignedActuator<Biome> {
    private final RNG rng;

    public IrisBiomeActuator(Engine engine) {
        super(engine, "Biome");
        rng = new RNG(engine.getWorld().getSeed() + 243995);
    }

    private boolean injectBiome(Hunk<Biome> h, int x, int y, int z, Object bb) {
        try {
            if (h instanceof BiomeGridHunkView hh) {
                ChunkGenerator.BiomeGrid g = hh.getChunk();
                if (g instanceof TerrainChunk) {
                    ((TerrainChunk) g).getBiomeBaseInjector().setBiome(x, y, z, bb);
                } else {
                    hh.forceBiomeBaseInto(x, y, z, bb);
                }
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Iris.reportError(e);
        }

        return false;
    }

    @Override
    public void onActuate(int x, int z, Hunk<Biome> h) {
        PrecisionStopwatch p = PrecisionStopwatch.start();
        int zf;

        for (int xf = 0; xf < h.getWidth(); xf++) {
            for (zf = 0; zf < h.getDepth(); zf++) {

                IrisBiome ib = getComplex().getTrueBiomeStream().get(modX(xf + x), modZ(zf + z));

                if (ib.isCustom()) {
                    try {
                        IrisBiomeCustom custom = ib.getCustomBiome(rng, x, 0, z);
                        Object biomeBase = INMS.get().getCustomBiomeBaseFor(getDimension().getLoadKey() + ":" + custom.getId());

                        if (!injectBiome(h, x, 0, z, biomeBase)) {
                            throw new RuntimeException("Cant inject biome!");
                        }

                        for (int i = 0; i < h.getHeight(); i++) {
                            injectBiome(h, xf, i, zf, biomeBase);
                        }
                    } catch (Throwable e) {
                        Iris.reportError(e);
                        e.printStackTrace();
                        Biome v = ib.getSkyBiome(rng, x, 0, z);
                        for (int i = 0; i < h.getHeight(); i++) {
                            h.set(xf, i, zf, v);
                        }
                    }
                } else {
                    Biome v = ib.getSkyBiome(rng, x, 0, z);
                    for (int i = 0; i < h.getHeight(); i++) {
                        h.set(xf, i, zf, v);
                    }
                }
            }
        }

        getEngine().getMetrics().getBiome().put(p.getMilliseconds());
    }
}
