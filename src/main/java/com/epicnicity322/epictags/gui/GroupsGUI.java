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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class GroupsGUI extends ListGUI<Map.Entry<String, TreeSet<Tag>>> {
    private static @NotNull Material[] groupMaterials = new Material[]{Material.WHITE_BANNER};
    private final @NotNull UUID pUUID;
    private int groupMaterialCount = 0;

    public GroupsGUI(@NotNull Player player) {
        super(getGroups(player), EpicTags.getLanguage().getColored("Groups GUI.Title").replace("<player>", player.getName()), Material.matchMaterial(EpicTags.CONFIG.getConfiguration().getString("Groups GUI.Items.Filling").orElse(Material.BLACK_STAINED_GLASS_PANE.toString())));
        this.pUUID = player.getUniqueId();

        // Info button.
        inventory.setItem(3, InventoryUtils.getItemStack("Groups GUI.Items.Info", EpicTags.CONFIG.getConfiguration(), EpicTags.getLanguage(), EpicTags.getTagsFormatted(player)));

        // Reset button.
        inventory.setItem(5, InventoryUtils.getItemStack("Groups GUI.Items.Reset", EpicTags.CONFIG.getConfiguration(), EpicTags.getLanguage(), player.getName()));
        buttons.put(5, event -> {
            Player p = closeIfPlayerOffline(pUUID, inventory);
            if (p == null) return;
            EpicTags.resetTagOverrides(p);
            // Open a new instance of this inventory to update it.
            new GroupsGUI(p).open(event.getWhoClicked());
        });
    }

    private static @NotNull Set<Map.Entry<String, TreeSet<Tag>>> getGroups(@NotNull Player player) {
        TreeMap<String, TreeSet<Tag>> groups = new TreeMap<>();
        EpicTags.getAllTags(player).forEach(tag -> groups.computeIfAbsent(tag.options().group(), k -> new TreeSet<>()).add(tag));
        return groups.entrySet();
    }

    private static @NotNull String lastColor(@NotNull String last) {
        String lore = EpicTags.getLanguage().getColored("Groups GUI.Items.Group.Lore");
        int index = lore.lastIndexOf(last) - 1;
        if (index < 0) return "";
        return ChatColor.getLastColors(lore.substring(0, index));
    }

    static @Nullable Player closeIfPlayerOffline(@NotNull UUID pUUID, @NotNull Inventory inv) {
        Player p = Bukkit.getPlayer(pUUID);
        if (p == null) {
            inv.getViewers().forEach(viewer -> {
                viewer.closeInventory();
                EpicTags.getLanguage().send(viewer, EpicTags.getLanguage().get("Logged Out"));
            });
        }
        return p;
    }

    private static @NotNull String formatTagListLore(@NotNull TreeSet<Tag> tags) {
        MessageSender lang = EpicTags.getLanguage();
        String onColor = lang.getColored("Groups GUI.Items.Group.Tag On Color");
        String offColor = lang.getColored("Groups GUI.Items.Group.Tag Off Color");
        String separator = lang.getColored("Groups GUI.Items.Group.Tag Separator");
        StringBuilder builder = new StringBuilder();

        for (Tag tag : tags) {
            builder.append(tag.options().visible() ? onColor : offColor);
            builder.append(tag.id());
            builder.append(separator);
        }

        return builder.substring(0, builder.length() - separator.length()) + lang.getColored("Groups GUI.Items.Group.Tag Period");
    }

    public static void setGroupMaterials(@NotNull Material[] materials) {
        if (materials.length == 0) groupMaterials = new Material[]{Material.WHITE_BANNER};
        else groupMaterials = materials;
    }

    @Override
    protected @NotNull ItemStack item(@NotNull Map.Entry<String, TreeSet<Tag>> obj) {
        ItemStack item = InventoryUtils.getItemStack("Groups GUI.Items.Group", EpicTags.CONFIG.getConfiguration(),
                EpicTags.getLanguage(), obj.getKey(), InventoryUtils.breakLore(formatTagListLore(obj.getValue()), 40, 5, 0, "<line>" + lastColor("<var1>")));
        item.setType(groupMaterials[groupMaterialCount > groupMaterials.length ? groupMaterialCount = 0 : groupMaterialCount++]);
        return item;
    }

    @Override
    protected @NotNull Consumer<InventoryClickEvent> event(@NotNull Map.Entry<String, TreeSet<Tag>> obj) {
        return event -> {
            Player p = closeIfPlayerOffline(pUUID, inventory);
            if (p == null) return;
            new TagListGUI(obj.getValue(), obj.getKey(), p).open(event.getWhoClicked());
        };
    }

    @Override
    public void open(@NotNull HumanEntity player) {
        if (Bukkit.getPlayer(pUUID) == null) {
            EpicTags.getLanguage().send(player, EpicTags.getLanguage().get("Player Is Offline"));
            return;
        }
        super.open(player);
    }
}
