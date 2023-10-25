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

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationLoader;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.epictags.command.ReloadCommand;
import com.epicnicity322.epictags.command.TagCommand;
import com.epicnicity322.epictags.gui.GroupsGUI;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public final class EpicTags extends JavaPlugin implements Listener {
    public static final @NotNull ConfigurationHolder CONFIG = new ConfigurationHolder(Path.of("plugins", "EpicTags", "config.yml"), """
            # Available languages: en_us and pt_br.
            language: en_us
                        
            # What to place between tags, if the player has multiple of them.
            spliterator: ' '
                        
            # The placeholder will always end with the spliterator. Useful for chat plugins, to avoid double spaces.
            end with spliterator: false
                        
            List GUI:
              Items:
                Next Page:
                  Material: SPECTRAL_ARROW
                  Glowing: false
                Previous Page:
                  Material: SPECTRAL_ARROW
                  Glowing: false
                        
            Groups GUI:
              Items:
                Filling: 'BLACK_STAINED_GLASS_PANE'
                Group:
                  Material:
                  - 'WHITE_BANNER'
                  - 'ORANGE_BANNER'
                  - 'MAGENTA_BANNER'
                  - 'LIGHT_BLUE_BANNER'
                  - 'YELLOW_BANNER'
                  - 'LIME_BANNER'
                  - 'PINK_BANNER'
                  - 'GRAY_BANNER'
                  - 'LIGHT_GRAY_BANNER'
                  - 'CYAN_BANNER'
                  - 'PURPLE_BANNER'
                  - 'BLUE_BANNER'
                  - 'BROWN_BANNER'
                  - 'GREEN_BANNER'
                  - 'RED_BANNER'
                  - 'BLACK_BANNER'
                  Glowing: false
                Info:
                  Material: FEATHER
                  Glowing: false
                Reset:
                  Material: BARRIER
                  Glowing: true
                        
            Tag List GUI:
              Items:
                Filling: 'WHITE_STAINED_GLASS_PANE'
                Tag:
                  Material: NAME_TAG
                  Glowing: false
                        
            Tag GUI:
              Items:
                Filling: 'GLASS_PANE'
                Group:
                  Material: WHITE_BANNER
                  Glowing: false
                Input:
                  Material: NETHER_STAR
                  Glowing: false
                Priority:
                  Material: HOPPER
                  Glowing: false
                Visibility On:
                  Material: LIME_CONCRETE
                  Glowing: false
                Visibility Off:
                  Material: RED_CONCRETE
                  Glowing: false
            """);
    public static final @NotNull ConfigurationHolder TAGS = new ConfigurationHolder(Path.of("plugins", "EpicTags", "tags.yml"), """
            # Add your tags to this configuration. You can assign them to players with the permission 'epictags.tag.<tag name>'.
            # Use the placeholder %epictags_tags% to show the tags.
            #
            # To create a tag, copy and paste the following format, then edit to your liking:
            #
            #tagid:
            #  tag: 'My Tag'
            #  priority: 0
            #
            # If the player has two or more tag permissions, they will show right beside the other. To avoid that, you
            #can create a group in this configuration, so the tags will not be shown at the same time, like so:
            #
            #Group Name:
            #  admin:
            #    tag: '&cAdmin'
            #    priority: 0
            #  owner:
            #    tag: '&4Owner'
            #    priority: 1
            #
            # Now if the player has both the permissions 'epictags.tag.admin' and 'epictags.tag.owner', only owner will
            #show, because it has higher priority.
            # When tags are in different groups and the player has permission for multiple tags, the priority is used to
            #determine the order of the tags (They will be sorted according to priority).
                        
            Rank Tags:
              default:
                tag: '&8[&7Default&8]'
                priority: 1
                        
              veteran:
                tag: '&8[&2Veteran&8]'
                priority: 2
                        
              admin:
                tag: '&8[&cAdmin&8]'
                priority: 3
                        
              owner:
                tag: '&8[&4Owner&8]'
                priority: 4
                        
            # You can add tags from PlaceholderAPI too!
            Clan Tags:
              clantag:
                tag: '%simpleclans_tag_label%'
                priority: 0
                        
              clanname:
                tag: '%simpleclans_clan_name%'
                priority: 0
                        
            # I want the KDR to show before the clan's name, so I placed the tag outside of 'Clan Tags' group. This way
            #both tags can show at the same time.
            clankdr:
              tag: '%simpleclans_kdr%'
              priority: -1
                        
            marrytag:
              tag: '%marriagemaster_Heart%'
              priority: -2
                        
            # Now if I give the player the permissions:
            # 'epictags.tag.default', 'epictags.tag.clantag', 'epictags.tag.clankdr', 'epictags.tag.marrytag'
            # The %epictags_tag% placeholder will display as:
            # ❤ 2.0 CLAN [Default]
            # (%marriagemaster_Heart% %simpleclans_kdr% %simpleclans_tag_label% <rank>)
            """);
    public static final @NotNull ConfigurationHolder LANG_EN_US = new ConfigurationHolder(Path.of("plugins", "EpicTags", "lang", "lang_en-us.yml"), """
            General:
              Player Not Found: '&cA player with name "&7<value>&c" does not exist or is not online.'
              Prefix: '&8[&6Epic&9Tags&8] '
              No Permission: '&cYou don''t have permission to do this.'
              Not A Player: '&cYou must be a player to use this command.'
                        
            Logged Out: '&cThe player you were editing tags of has logged out.'
                        
            New Priority In Chat: '&7Please type in the new priority for tag &f<id>&7. Current: &6<current>'
                        
            New Group In Chat: '&7Please type in the new group for tag &f<id>&7. Current: &6<current>'
                                    
            Player Is Offline: '&cCannot edit tags of an offline player.'
                        
            Open Other Player Editor: '&7Opening editor for &f<player>''s&7 tag settings.'
                        
            Override Error: '&cSomething went wrong while editing tag settings.'
                       
            Reload:
              Error: '&cSome issues happened while reloading the plugin.'
              Success: '&aEpicTags reloaded successfully.'
                        
            List GUI:
              Items:
                Next Page:
                  Display Name: '&6Next Page'
                  Lore: '&7Click to go to page &f<var0>'
                Previous Page:
                  Display Name: '&6Previous Page'
                  Lore: '&7Click to go to page &f<var0>'
                        
            Groups GUI:
              Title: 'Tag Groups for &6&l&n<player>'
              Items:
                Reset:
                  Display Name: '&4&lRESET'
                  Lore: |-
                    
                    Reset all tag settings of <var0>
                    back to default.
                Group:
                  Tag On Color: '&a&o'
                  Tag Off Color: '&c'
                  Tag Separator: '&5&o, '
                  Tag Period: '&5&o.'
                  Display Name: '&6&l<var0>'
                  Lore: |-
                    
                    Tags in this group:
                    <var1>
                    
                    Click to see the full list of tags and
                    edit them.
                Info:
                  Display Name: '&7&lCurrent:&7 <var0>'
                  Lore: |-
                    
                    This is how your tags currently look like!
                        
            Tag List GUI:
              Title: 'Tags in &6&l&n<group>&8 group'
              Items:
                Tag:
                  Display Name: '&e&l<var0>'
                  Visibility On: '&aON'
                  Visibility Off: '&cOFF'
                  # Variable <var4> is also available to show contents of the tag.
                  Lore: |-
                    
                    Visibility: <var1>
                    Priority: &6<var2>
                    Group: &6<var3>
                        
            Tag GUI:
              # Variable <tag> is also available to show contents of the tag.
              Title: 'Editing tag &6&l&n<id>'
              Items:
                Group:
                  Display Name: '&7&lGroup: &6&l<var0>'
                  Lore: |-
                    
                    The group of this tag. Tags of the same
                    group do not show at the same time, only
                    the tag with highest priority will show.
                    Change this value if you want to use
                    more than one tag of this group.
                Input:
                  Display Name: '<var0>'
                  Lore: |-
                    
                    Please type in the new value.
                Priority:
                  Display Name: '&7&lPriority: &6&l<var0>'
                  Lore: |-
                    
                    The priority of this tag. Tags are
                    ordered based on this priority.
                    If there are tags from the same group,
                    only the tag with highest priority will
                    show.
                Visibility On:
                  Display Name: '&7&lVisibility: &a&lON'
                  Lore: |-
                    
                    Whether this tag is showing or not.
                    Click to turn it off.
                Visibility Off:
                  Display Name: '&7&lVisibility: &c&lOFF'
                  Lore: |-
                    
                    Whether this tag is showing or not.
                    Click to turn it on.
            """);
    public static final @NotNull ConfigurationHolder LANG_PT_BR = new ConfigurationHolder(Path.of("plugins", "EpicTags", "lang", "lang_pt-br.yml"), """
            General:
              Player Not Found: '&cUm jogador com o nome "&7<value>&c" não existe ou não está online.'
              Prefix: '&8[&6Epic&9Tags&8] '
              No Permission: '&cVocê não tem permissão para fazer isso.'
              Not A Player: '&cVocê precisa ser um jogador para usar esse comando.'
                        
            Logged Out: '&cO jogador que você estava editando as flags desconectou.'
                        
            New Priority In Chat: '&7Por favor digite a nova prioridade para a tag &f<id>&7. Atual: &6<current>'
                        
            New Group In Chat: '&7Por favor digite o novo grupo para a tag &f<id>&7. Atual: &6<current>'
                                    
            Player Is Offline: '&cNão é possível editar tags de um jogador offline.'
                        
            Open Other Player Editor: '&7Abrindo editor das configurações de flags de &f<player>&7.'
                        
            Override Error: '&cAlgo de errado ocorreu ao editar essa configuração.'
                       
            Reload:
              Error: '&cAlguns erros ocorreram ao recarregar o plugin.'
              Success: '&aEpicTags recarregado com sucesso.'
                        
            List GUI:
              Items:
                Next Page:
                  Display Name: '&6Próxima Página'
                  Lore: '&7Clique para ir à página &f<var0>'
                Previous Page:
                  Display Name: '&6Página Anterior'
                  Lore: '&7Clique para ir à página &f<var0>'
                        
            Groups GUI:
              Title: 'Grupos de Tags de &6&l&n<player>'
              Items:
                Reset:
                  Display Name: '&4&lRESETAR'
                  Lore: |-
                    
                    Resetar todas as configurações de <var0>
                    de volta ao padrão.
                Group:
                  Tag On Color: '&a&o'
                  Tag Off Color: '&c'
                  Tag Separator: '&5&o, '
                  Tag Period: '&5&o.'
                  Display Name: '&6&l<var0>'
                  Lore: |-
                    
                    Tags nesse grupo:
                    <var1>
                    
                    Clique para ver a lista inteira de flags
                    e editar-las.
                Info:
                  Display Name: '&7&lAtual:&7 <var0>'
                  Lore: |-
                    
                    Assim que suas tags se parecem atualmente!
                        
            Tag List GUI:
              Title: 'Tags no grupo &6&l&n<group>'
              Items:
                Tag:
                  Display Name: '&e&l<var0>'
                  Visibility On: '&aATIVADO'
                  Visibility Off: '&cDESATIVADO'
                  # Variable <var4> is also available to show contents of the tag.
                  Lore: |-
                    
                    Visibilidade: <var1>
                    Prioridade: &6<var2>
                    Grupo: &6<var3>
                        
            Tag GUI:
              # Variable <tag> is also available to show contents of the tag.
              Title: 'Editando tag &6&l&n<id>'
              Items:
                Group:
                  Display Name: '&7&lGrupo: &6&l<var0>'
                  Lore: |-
                    
                    O grupo deta tag. Tags do mesmo grupo
                    não aparecem ao mesmo tempo, somente
                    a tag com maior prioridade vai aparecer.
                    Mude esse valor se você deseja usar mais
                    de uma tag deste grupo.
                Input:
                  Display Name: '<var0>'
                  Lore: |-
                    
                    Por favor digite o novo valor.
                Priority:
                  Display Name: '&7&lPrioridade: &6&l<var0>'
                  Lore: |-
                    
                    A prioridade desta tag. Tags são ordenadas
                    baseado nessa prioridade.
                    Se existem tags no mesmo grupo, somente
                    a tag com maior prioridade vai aparecer.
                Visibility On:
                  Display Name: '&7&lVisibilidade: &a&lATIVADA'
                  Lore: |-
                    
                    Se esta flag está aparecendo ou não.
                    Clique para desativar.
                Visibility Off:
                  Display Name: '&7&lVisibilidade: &c&lDESATIVADA'
                  Lore: |-
                    
                    Se esta flag está aparecendo ou não.
                    Clique para ativar.
            """);
    private static final @NotNull ConfigurationLoader loader = new ConfigurationLoader() {{
        registerConfiguration(CONFIG);
        registerConfiguration(TAGS);
        registerConfiguration(LANG_EN_US);
        registerConfiguration(LANG_PT_BR);
    }};
    private static final @NotNull MessageSender language = new MessageSender(() -> CONFIG.getConfiguration().getString("language").orElse("en-us"), LANG_EN_US.getDefaultConfiguration());
    private static final @NotNull Logger logger = new Logger("[EpicTags] ");

    private static final @NotNull HashMap<String, Tag> allTags = new HashMap<>();
    private static @NotNull String spliterator = " ";
    private static boolean endWithSpliterator = false;
    private static @Nullable EpicTags instance;

    static {
        language.addLanguage("en_us", LANG_EN_US);
        language.addLanguage("pt_br", LANG_PT_BR);
    }

    private final @NotNull NamespacedKey tag_overrides = new NamespacedKey(this, "tag_overrides");

    public EpicTags() {
        instance = this;
        logger.setLogger(getLogger());
    }

    public static @NotNull Logger logger() {
        return logger;
    }

    public static @NotNull MessageSender getLanguage() {
        return language;
    }

    public static @Nullable EpicTags getInstance() {
        return instance;
    }

    public static boolean reload() {
        HashMap<ConfigurationHolder, Exception> exceptions = loader.loadConfigurations();
        exceptions.forEach((config, exception) -> {
            logger.log("Something went wrong while loading config '" + config.getPath() + "'!", ConsoleLogger.Level.ERROR);
            exception.printStackTrace();
        });

        spliterator = CONFIG.getConfiguration().getString("spliterator").orElse(" ");
        endWithSpliterator = CONFIG.getConfiguration().getBoolean("end with spliterator").orElse(false);

        allTags.clear();
        // Prevent loading default tags if configuration failed to load.
        if (!exceptions.containsKey(TAGS)) {
            TAGS.getConfiguration().getAbsoluteNodes().forEach((key, obj) -> {
                if (!key.endsWith(".tag") || !(obj instanceof String tag)) return;

                ConfigurationSection tagSection = TAGS.getConfiguration().getConfigurationSection(key.substring(0, key.lastIndexOf(".tag")));
                if (tagSection == null) return;

                String group;
                ConfigurationSection parent = tagSection.getParent();

                if (parent == null || parent == tagSection.getRoot()) {
                    group = tagSection.getName();
                } else {
                    group = parent.getPath();
                }

                // Translate color codes and remove any placeholders of this plugin, to avoid StackOverflow.
                tag = ChatColor.translateAlternateColorCodes('&', tag.replaceAll("%epictags_.*?%.*?\\b", ""));

                Tag t = new Tag(tagSection.getName(), tag, new TagOptions(group, tagSection.getNumber("priority").orElse(0).intValue(), true));
                allTags.put(t.id(), t);
            });
        }

        ArrayList<Material> materials = CONFIG.getConfiguration().getCollection("Groups GUI.Items.Group.Material", obj -> Material.matchMaterial(obj.toString()));
        materials.removeIf(Objects::isNull);
        GroupsGUI.setGroupMaterials(materials.toArray(new Material[0]));

        return exceptions.isEmpty();
    }

    /**
     * Gets all the tags a player is allowed to show. The method runs through the loaded tags and checks if the player
     * has permission to use them.
     * <p>
     * This method enforces that when there is more than one tag per group, only the tag with the highest priority is chosen.
     * <p>
     * The returned collection is NOT sorted as the {@link Tag#compareTo(Tag)} method specifies.
     *
     * @param player The player to get the tags of.
     * @return The tags of this player.
     */
    public static @NotNull Collection<Tag> getTags(@NotNull Player player) {
        HashMap<String, Tag> tags = new HashMap<>();

        for (Tag tag : getAllTags(player)) {
            if (!tag.options().visible()) continue;

            Tag tagInGroup = tags.get(tag.options().group());
            if (tagInGroup == null) {
                tags.put(tag.options().group(), tag);
            } else {
                if (tagInGroup.compareTo(tag) < 0) tags.put(tag.options().group(), tag);
            }
        }
        return tags.values();
    }

    /**
     * Gets all the tags a player is allowed to show. The method runs through the loaded tags and checks if the player
     * has permission to use them.
     * <p>
     * The returned collection is NOT sorted as the {@link Tag#compareTo(Tag)} method specifies.
     *
     * @param player The player to get the tags of.
     * @return The tags of this player.
     * @see #getTags(Player)
     */
    public static @NotNull Collection<Tag> getAllTags(@NotNull Player player) {
        if (instance == null) return getAllTagsNoOverrides(player);

        // Getting tag map from player data.
        byte[] serializedTagOverrideMap = player.getPersistentDataContainer().get(instance.tag_overrides, PersistentDataType.BYTE_ARRAY);
        HashMap<String, TagOptions> tagOverrides;

        if (serializedTagOverrideMap == null) {
            return getAllTagsNoOverrides(player);
        } else {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(serializedTagOverrideMap); ObjectInputStream ois = new ObjectInputStream(bis)) {
                tagOverrides = (HashMap<String, TagOptions>) ois.readObject();
            } catch (Exception e) {
                return getAllTagsNoOverrides(player);
            }
        }

        HashSet<Tag> tags = new HashSet<>();

        for (Tag tag : allTags.values()) {
            if (!player.hasPermission(tag.permission())) continue;

            TagOptions overriddenOptions = tagOverrides.get(tag.id());
            if (overriddenOptions == null) {
                tags.add(tag);
            } else {
                tags.add(new Tag(tag.id(), tag.tag(), overriddenOptions));
            }
        }

        return tags;
    }

    private static @NotNull Collection<Tag> getAllTagsNoOverrides(@NotNull Player player) {
        HashSet<Tag> tags = new HashSet<>();

        for (Tag tag : allTags.values()) {
            if (!player.hasPermission(tag.permission())) continue;

            tags.add(tag);
        }

        return tags;
    }

    /**
     * Gets all the tags a player is allowed to show. The method runs through the loaded tags and checks if the player
     * has permission to use them.
     * <p>
     * The tags are separated using the spliterator set in config. If they have placeholders from PlaceholderAPI, they are formatted as well.
     * <p>
     * The order of the tags is based on priority. If there is more than one tag per group, the tag with the highest priority takes place.
     *
     * @param player The player to get the tags of.
     * @return The tags of this player, or empty string if they don't have any.
     */
    public static @NotNull String getTagsFormatted(@NotNull Player player) {
        Collection<Tag> tags = getTags(player);
        if (tags.isEmpty()) return "";

        ArrayList<Tag> tagList = new ArrayList<>(tags);
        Collections.sort(tagList);

        StringBuilder finalTags = new StringBuilder();

        for (Tag tag : tagList) {
            String s = PlaceholderAPI.setPlaceholders(player, tag.tag());
            if (s.isEmpty()) continue;
            finalTags.append(s).append(spliterator);
        }

        if (endWithSpliterator) {
            return finalTags.toString();
        } else {
            // Can happen if one tag is empty after having its placeholders set. Otherwise, the spliterator is always
            // guaranteed to be at the end.
            if (finalTags.isEmpty()) return "";
            return finalTags.substring(0, finalTags.length() - spliterator.length());
        }
    }

    /**
     * Sets overriding options for the tag with specified ID for this specific player only.
     * <p>
     * When getting tags for this player, the options set by this method will be used instead of the default options in config.
     *
     * @param player   The player to set the custom options to.
     * @param tagID    The ID of the tag to set options.
     * @param override The overriding options the tag with this ID should use.
     * @throws IOException            If something went wrong while deserializing/serializing options map from the player's data.
     * @throws ClassNotFoundException If something went wrong while deserializing/serializing options map from the player's data.
     */
    public static void setTagOverride(@NotNull Player player, @NotNull String tagID, @Nullable TagOptions override) throws IOException, ClassNotFoundException {
        if (instance == null) return;
        PersistentDataContainer container = player.getPersistentDataContainer();

        // Getting tag map from player data.
        byte[] serializedTagMap = container.get(instance.tag_overrides, PersistentDataType.BYTE_ARRAY);
        HashMap<String, TagOptions> tags;

        if (serializedTagMap == null) {
            tags = new HashMap<>();
        } else {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(serializedTagMap); ObjectInputStream ois = new ObjectInputStream(bis)) {
                tags = (HashMap<String, TagOptions>) ois.readObject();
            }
        }

        tags.put(tagID, override);

        // Writing map back to the player data with the new override.
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(tags);
            oos.flush();
            container.set(instance.tag_overrides, PersistentDataType.BYTE_ARRAY, bos.toByteArray());
        }
    }

    /**
     * Resets the personal settings a player has set for their tags.
     *
     * @param player The player to set the tags back to default.
     */
    public static void resetTagOverrides(@NotNull Player player) {
        if (instance == null) return;
        player.getPersistentDataContainer().remove(instance.tag_overrides);
    }

    @Override
    public void onEnable() {
        if (EpicPluginLib.version.compareTo(new Version("2.6")) < 0) {
            logger.log("This plugin requires EpicPluginLib version at least 2.6!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (new EpicTagsPlaceholder(this).register()) {
            logger.log("PlaceholderAPI expansion registered.");
        } else {
            logger.log("PlaceholderAPI expansion failed to register.", ConsoleLogger.Level.ERROR);
            logger.log("Please make sure you don't already have an expansion with the same name as this plugin.", ConsoleLogger.Level.ERROR);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        boolean success = reload();

        Objects.requireNonNull(getCommand("epictags")).setExecutor(new TagCommand());
        Objects.requireNonNull(getCommand("epictagsreload")).setExecutor(new ReloadCommand());

        ConsoleLogger.Level level = ConsoleLogger.Level.INFO;
        if (success) {
            logger.log("EpicTags was enabled successfully!");
        } else {
            logger.log("An issue happened while enabling EpicTags!", ConsoleLogger.Level.ERROR);
            level = ConsoleLogger.Level.ERROR;
        }

        logger.log((allTags.isEmpty() ? "No" : allTags.size()) + " tags were loaded.", level);
    }
}
