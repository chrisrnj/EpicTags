/*
 * EpicTags - A bukkit plugin for managing player tags.
 * Copyright (C) 2023  Christiano Rangel
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

package com.epicnicity322.epictags.gui;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.util.InventoryUtils;
import com.epicnicity322.epictags.EpicTags;
import com.epicnicity322.epictags.Tag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TagListGUI extends ListGUI<Tag> {
    private final @NotNull UUID pUUID;
    private final @NotNull AtomicBoolean clickingButton = new AtomicBoolean(false);

    public TagListGUI(@NotNull Collection<Tag> collection, @NotNull String group, @NotNull Player player) {
        super(collection, EpicTags.getLanguage().getColored("Tag List GUI.Title").replace("<group>", group), Material.matchMaterial(EpicTags.CONFIG.getConfiguration().getString("Tag List GUI.Items.Filling").orElse(Material.WHITE_STAINED_GLASS_PANE.toString())));
        this.pUUID = player.getUniqueId();
    }

    @Override
    protected @NotNull ItemStack item(@NotNull Tag obj) {
        MessageSender lang = EpicTags.getLanguage();
        return InventoryUtils.getItemStack("Tag List GUI.Items.Tag", EpicTags.CONFIG.getConfiguration(), lang,
                obj.id(), lang.getColored(obj.options().visible() ? "Tag List GUI.Items.Tag.Visibility On" : "Tag List GUI.Items.Tag.Visibility Off"),
                Integer.toString(obj.options().priority()), obj.options().group(), obj.tag());
    }

    @Override
    protected @NotNull Consumer<InventoryClickEvent> event(@NotNull Tag obj) {
        return event -> {
            clickingButton.set(true);
            Player p = GroupsGUI.closeIfPlayerOffline(pUUID, inventory);
            if (p == null) return;
            new TagGUI(obj, p).open(event.getWhoClicked());
            clickingButton.set(false);
        };

    }

    @Override
    public void open(@NotNull HumanEntity player) {
        if (Bukkit.getPlayer(pUUID) == null) {
            EpicTags.getLanguage().send(player, EpicTags.getLanguage().get("Player Is Offline"));
            return;
        }
        InventoryUtils.openInventory(inventory, buttons, player, event -> {
            if (clickingButton.get()) return;
            Player p = Bukkit.getPlayer(pUUID);
            if (p != null) {
                GroupsGUI groupsGUI = new GroupsGUI(p);
                if (EpicTags.getInstance() == null) {
                    groupsGUI.open(event.getPlayer());
                    return;
                }
                Bukkit.getScheduler().runTask(EpicTags.getInstance(), () -> groupsGUI.open(event.getPlayer()));
            }
        });
    }
}
