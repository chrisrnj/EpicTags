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

package com.epicnicity322.epictags;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The placeholders for EpicTags.
 * <ul>
 *     <li>%epictags_tags% - Shows the tags of the player.</li>
 *     <li>%epictags_tags_{@literal <player>}% - Shows the tags of the specified player.</li>
 *     <li>%epictags_tag_0% - Shows the tag with highest priority of the player.</li>
 *     <li>%epictags_tag_{@literal <index>}% - Shows the tag of the player at the specified index, the list of tags is sorted by priority.</li>
 *     <li>%epictags_tag_{@literal <index>}_{@literal <player>}% - Shows the tag of ordered priority of the specified player.</li>
 *     <li>%epictags_tag_amount% - Shows the amount of tags the player has.</li>
 *     <li>%epictags_tag_amount_{@literal <player>}% - Shows the amount of tags the specified player has.</li>
 * </ul>
 */
public final class EpicTagsPlaceholder extends PlaceholderExpansion {
    private final @NotNull EpicTags plugin;

    public EpicTagsPlaceholder(@NotNull EpicTags plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getName() {
        return plugin.getName();
    }

    @Override
    public @NotNull String getIdentifier() {
        return getName().toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        String prefix = '%' + getIdentifier();
        return List.of(prefix + "_tags%", prefix + "_tags_<player>%", prefix + "_tag_0%",
                prefix + "_tag_<priority-index>%", prefix + "_tag_amount%", prefix + "_tag_<priority-index>_<player>%",
                prefix + "_tag_amount_<player>%");
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        // Offline players can't have permissions checked, so their tags are unknown.
        if (player == null) return "";

        if (params.startsWith("tags")) {
            // Getting player
            int underlineIndex = params.indexOf('_');
            if (underlineIndex != -1) {
                player = Bukkit.getPlayer(params.substring(underlineIndex + 1));
                if (player == null) return "";
            }
            return EpicTags.getTagsFormatted(player);
        }

        if (params.startsWith("tag_")) {
            int underlineIndex = params.indexOf('_');
            String priorityString = params.substring(underlineIndex + 1);

            // Getting player
            underlineIndex = priorityString.indexOf('_');
            if (underlineIndex != -1) {
                player = Bukkit.getPlayer(params.substring(underlineIndex + 1));
                if (player == null) return "";
                priorityString = priorityString.substring(0, underlineIndex);
            }


            Collection<Tag> tags = EpicTags.getTags(player);
            int priorityIndex;

            if (priorityString.equalsIgnoreCase("amount")) {
                return Integer.toString(tags.size());
            } else {
                try {
                    priorityIndex = Integer.parseInt(priorityString);
                } catch (NumberFormatException e) {
                    return "";
                }
            }

            if (tags.isEmpty()) return "";
            if (priorityIndex < 0) priorityIndex = 0;
            if (priorityIndex > tags.size() - 1) priorityIndex = tags.size() - 1;

            ArrayList<Tag> tagList = new ArrayList<>(tags);
            Collections.sort(tagList);
            Collections.reverse(tagList);

            return PlaceholderAPI.setPlaceholders(player, tagList.get(priorityIndex).tag());
        }

        return null;
    }
}
