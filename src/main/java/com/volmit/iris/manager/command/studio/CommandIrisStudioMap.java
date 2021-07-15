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

package com.volmit.iris.manager.command.studio;

import com.volmit.iris.Iris;
import com.volmit.iris.IrisSettings;
import com.volmit.iris.manager.gui.IrisVision;
import com.volmit.iris.scaffold.IrisWorlds;
import com.volmit.iris.scaffold.engine.IrisAccess;
import com.volmit.iris.util.KList;
import com.volmit.iris.util.MortarCommand;
import com.volmit.iris.util.MortarSender;

public class CommandIrisStudioMap extends MortarCommand {
    public CommandIrisStudioMap() {
        super("map", "render");
        setDescription("Render a map (gui outside of mc)");
        requiresPermission(Iris.perm.studio);
        setCategory("World");
    }

    @Override
    public void addTabOptions(MortarSender sender, String[] args, KList<String> list) {

    }

    @Override
    public boolean handle(MortarSender sender, String[] args) {
        if (!IrisSettings.get().isStudio()) {
            sender.sendMessage("To use Iris Studio, please enable studio in Iris/settings.json");
            return true;
        }

        if (!IrisSettings.get().isUseServerLaunchedGuis()) {
            sender.sendMessage("To use Iris Guis, please enable serverLaunchedGuis in Iris/settings.json");
            return true;
        }


        try {
            IrisAccess g = Iris.proj.getActiveProject().getActiveProvider();
            IrisVision.launch(g, 0);
            sender.sendMessage("Opening Map!");
        } catch (Throwable e) {
            Iris.reportError(e);
            IrisAccess g = IrisWorlds.access(sender.player().getWorld());
            IrisVision.launch(g, 0);
            sender.sendMessage("Opening Map!");
        }

        return true;
    }

    @Override
    protected String getArgsUsage() {
        return "";
    }
}
