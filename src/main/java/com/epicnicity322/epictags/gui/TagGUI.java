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
import com.epicnicity322.epicpluginlib.bukkit.util.InputGetterUtil;
import com.epicnicity322.epicpluginlib.bukkit.util.InventoryUtils;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epictags.EpicTags;
import com.epicnicity322.epictags.Tag;
import com.epicnicity322.epictags.TagOptions;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TagGUI {
    private final @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> buttons;
    private final @NotNull Inventory inventory;
    private final @NotNull UUID pUUID;
    private final @NotNull AtomicBoolean clickingButton = new AtomicBoolean(false);

    public TagGUI(@NotNull Tag tag, @NotNull Player player) {
        MessageSender lang = EpicTags.getLanguage();
        Configuration config = EpicTags.CONFIG.getConfiguration();
        pUUID = player.getUniqueId();
        buttons = new HashMap<>((int) (3 / 0.75) + 1);
        inventory = Bukkit.createInventory(null, 9, lang.getColored("Tag GUI.Title").replace("<id>", tag.id()).replace("<tag>", tag.tag()));

        Material filling = Material.matchMaterial(config.getString("Tag GUI.Items.Filling").orElse(Material.GLASS_PANE.toString()));
        if (filling == null) filling = Material.GLASS_PANE;
        InventoryUtils.fill(filling, inventory, 0, 8);

        String priority = Integer.toString(tag.options().priority());
        inventory.setItem(1, InventoryUtils.getItemStack("Tag GUI.Items.Priority", config, lang, priority));
        buttons.put(1, event -> {
            HumanEntity whoClicked = event.getWhoClicked();
            if (!whoClicked.hasPermission("epictags.edit.priority")) {
                lang.send(whoClicked, lang.get("General.No Permission"));
                return;
            }

            clickingButton.set(true);
            Player p = GroupsGUI.closeIfPlayerOffline(pUUID, inventory);
            if (p == null) return;

            if (InputGetterUtil.askInput(whoClicked, InventoryUtils.getItemStack("Tag GUI.Items.Input", config, lang, priority), input -> {
                Player p1 = Bukkit.getPlayer(pUUID);
                if (p1 == null) {
                    lang.send(whoClicked, lang.get("Logged Out"));
                    return;
                }

                int newPriority;
                try {
                    newPriority = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    newPriority = tag.options().priority();
                }

                try {
                    EpicTags.setTagOverride(p1, tag.id(), new TagOptions(tag.options().group(), newPriority, tag.options().visible()));
                } catch (Exception e) {
                    EpicTags.logger().log("Something went wrong while setting tag override for player '" + pUUID + "':", ConsoleLogger.Level.WARN);
                    e.printStackTrace();
                    lang.send(whoClicked, lang.get("Override Error"));
                }

                // Reopening GroupsGUI
                GroupsGUI groupsGUI = new GroupsGUI(p1);
                Bukkit.getScheduler().runTask(EpicTags.getInstance(), () -> groupsGUI.open(whoClicked));
            })) {
                lang.send(whoClicked, lang.get("New Priority In Chat").replace("<current>", priority).replace("<id>", tag.id()));
            }
            clickingButton.set(false);
        });

        inventory.setItem(4, InventoryUtils.getItemStack(tag.options().visible() ? "Tag GUI.Items.Visibility On" : "Tag GUI.Items.Visibility Off", config, lang));
        buttons.put(4, event -> {
            HumanEntity whoClicked = event.getWhoClicked();
            if (!whoClicked.hasPermission("epictags.edit.visibility")) {
                lang.send(whoClicked, lang.get("General.No Permission"));
                return;
            }

            clickingButton.set(true);
            Player p = GroupsGUI.closeIfPlayerOffline(pUUID, inventory);
            clickingButton.set(false);
            if (p == null) return;

            boolean newVisibility = !tag.options().visible();
            try {
                EpicTags.setTagOverride(p, tag.id(), new TagOptions(tag.options().group(), tag.options().priority(), newVisibility));
            } catch (Exception e) {
                EpicTags.logger().log("Something went wrong while setting tag override for player '" + pUUID + "':", ConsoleLogger.Level.WARN);
                e.printStackTrace();
                lang.send(whoClicked, lang.get("Override Error"));
            }
            whoClicked.closeInventory();
        });

        inventory.setItem(7, InventoryUtils.getItemStack("Tag GUI.Items.Group", config, lang, tag.options().group()));
        buttons.put(7, event -> {
            HumanEntity whoClicked = event.getWhoClicked();
            if (!whoClicked.hasPermission("epictags.edit.group")) {
                lang.send(whoClicked, lang.get("General.No Permission"));
                return;
            }

            clickingButton.set(true);
            Player p = GroupsGUI.closeIfPlayerOffline(pUUID, inventory);
            if (p == null) return;

            if (InputGetterUtil.askInput(whoClicked, InventoryUtils.getItemStack("Tag GUI.Items.Input", config, lang, tag.options().group()), input -> {
                Player p1 = Bukkit.getPlayer(pUUID);
                if (p1 == null) {
                    lang.send(whoClicked, lang.get("Logged Out"));
                    return;
                }

                try {
                    EpicTags.setTagOverride(p, tag.id(), new TagOptions(input, tag.options().priority(), tag.options().visible()));
                } catch (Exception e) {
                    EpicTags.logger().log("Something went wrong while setting tag override for player '" + pUUID + "':", ConsoleLogger.Level.WARN);
                    e.printStackTrace();
                    lang.send(whoClicked, lang.get("Override Error"));
                }

                // Reopening GroupsGUI
                GroupsGUI groupsGUI = new GroupsGUI(p1);
                Bukkit.getScheduler().runTask(EpicTags.getInstance(), () -> groupsGUI.open(whoClicked));
            })) {
                lang.send(whoClicked, lang.get("New Group In Chat").replace("<current>", priority).replace("<id>", tag.id()));
            }
            clickingButton.set(false);
        });
    }

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
