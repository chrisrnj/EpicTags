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

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public record Tag(@NotNull String id, @NotNull String tag,
                  @NotNull TagOptions options) implements Comparable<Tag> {
    @NotNull
    public String permission() {
        return "epictags.tag." + id;
    }

    @Override
    public int compareTo(@NotNull Tag other) {
        int priorityCompare = Integer.compare(options.priority(), other.options.priority());
        if (priorityCompare != 0) return priorityCompare;

        int idCompare = id.compareTo(other.id);
        if (idCompare != 0) return idCompare;

        int groupCompare = options.group().compareTo(other.options.group());
        if (groupCompare != 0) return groupCompare;

        return ChatColor.stripColor(tag).compareTo(ChatColor.stripColor(other.tag));
    }
}
