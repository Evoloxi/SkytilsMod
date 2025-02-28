/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package skytils.skytilsmod.utils

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraftforge.common.util.Constants
import java.util.*
import java.util.regex.Pattern

object ItemUtil {
    private val RARITY_PATTERN = Pattern.compile("(§[0-9a-f]§l§ka§r )?([§0-9a-fk-or]+)(?<rarity>[A-Z]+)")
    private val PET_PATTERN = Pattern.compile("§7\\[Lvl \\d+] (?<color>§[0-9a-fk-or]).+")
    private const val NBT_INTEGER = 3
    private const val NBT_STRING = 8
    private const val NBT_LIST = 9
    private const val NBT_COMPOUND = 10

    /**
     * Returns the display name of a given item
     * @author Mojang
     * @param item the Item to get the display name of
     * @return the display name of the item
     */
    @JvmStatic
    fun getDisplayName(item: ItemStack): String {
        var s = item.item.getItemStackDisplayName(item)
        if (item.tagCompound != null && item.tagCompound.hasKey("display", 10)) {
            val nbttagcompound = item.tagCompound.getCompoundTag("display")
            if (nbttagcompound.hasKey("Name", 8)) {
                s = nbttagcompound.getString("Name")
            }
        }
        return s
    }

    /**
     * Returns the Skyblock Item ID of a given Skyblock item
     *
     * @author BiscuitDevelopment
     * @param item the Skyblock item to check
     * @return the Skyblock Item ID of this item or `null` if this isn't a valid Skyblock item
     */
    @JvmStatic
    fun getSkyBlockItemID(item: ItemStack?): String? {
        if (item == null) {
            return null
        }
        val extraAttributes = getExtraAttributes(item) ?: return null
        return if (!extraAttributes.hasKey("id", NBT_STRING)) {
            null
        } else extraAttributes.getString("id")
    }

    /**
     * Returns the `ExtraAttributes` compound tag from the item's NBT data.
     *
     * @author BiscuitDevelopment
     * @param item the item to get the tag from
     * @return the item's `ExtraAttributes` compound tag or `null` if the item doesn't have one
     */
    @JvmStatic
    fun getExtraAttributes(item: ItemStack?): NBTTagCompound? {
        return if (item == null || !item.hasTagCompound()) {
            null
        } else item.getSubCompound("ExtraAttributes", false)
    }

    /**
     * Returns the Skyblock Item ID of a given Skyblock Extra Attributes NBT Compound
     *
     * @author BiscuitDevelopment
     * @param extraAttributes the NBT to check
     * @return the Skyblock Item ID of this item or `null` if this isn't a valid Skyblock NBT
     */
    @JvmStatic
    fun getSkyBlockItemID(extraAttributes: NBTTagCompound?): String? {
        if (extraAttributes != null) {
            val itemId = extraAttributes.getString("id")
            if (itemId != "") {
                return itemId
            }
        }
        return null
    }

    /**
     * Returns a string list containing the nbt lore of an ItemStack, or
     * an empty list if this item doesn't have a lore. The returned lore
     * list is unmodifiable since it has been converted from an NBTTagList.
     *
     * @author BiscuitDevelopment
     * @param itemStack the ItemStack to get the lore from
     * @return the lore of an ItemStack as a string list
     */
    @JvmStatic
    fun getItemLore(itemStack: ItemStack): List<String> {
        if (itemStack.hasTagCompound() && itemStack.tagCompound.hasKey("display", NBT_COMPOUND)) {
            val display = itemStack.tagCompound.getCompoundTag("display")
            if (display.hasKey("Lore", NBT_LIST)) {
                val lore = display.getTagList("Lore", NBT_STRING)
                val loreAsList: MutableList<String> = ArrayList()
                for (lineNumber in 0 until lore.tagCount()) {
                    loreAsList.add(lore.getStringTagAt(lineNumber))
                }
                return Collections.unmodifiableList(loreAsList)
            }
        }
        return emptyList()
    }

    @JvmStatic
    fun hasRightClickAbility(itemStack: ItemStack): Boolean {
        for (line in getItemLore(itemStack)) {
            val stripped = StringUtils.stripControlCodes(line)
            if (stripped.startsWith("Item Ability:") && stripped.endsWith("RIGHT CLICK")) return true
        }
        return false
    }

    /**
     * Returns the rarity of a given Skyblock item
     * Modified
     * @author BiscuitDevelopment
     * @param item the Skyblock item to check
     * @return the rarity of the item if a valid rarity is found, `null` if no rarity is found, `null` if item is `null`
     */
    fun getRarity(item: ItemStack?): ItemRarity? {
        if (item == null || !item.hasTagCompound()) {
            return null
        }
        val display = item.getSubCompound("display", false)
        if (display == null || !display.hasKey("Lore")) {
            return null
        }
        val lore = display.getTagList("Lore", Constants.NBT.TAG_STRING)
        val name = display.getString("Name")

        // Determine the item's rarity
        for (i in 0 until lore.tagCount()) {
            val currentLine = lore.getStringTagAt(i)
            val rarityMatcher = RARITY_PATTERN.matcher(currentLine)
            val petRarityMatcher = PET_PATTERN.matcher(name)
            if (rarityMatcher.find()) {
                val rarity = rarityMatcher.group("rarity")
                for (itemRarity in ItemRarity.values()) {
                    if (rarity.startsWith(itemRarity.rarityName)) {
                        return itemRarity
                    }
                }
            } else if (petRarityMatcher.find()) {
                val color = petRarityMatcher.group("color")
                return ItemRarity.byBaseColor(color)
            }
        }

        // If the item doesn't have a valid rarity, return null
        return null
    }

    fun isPet(item: ItemStack?): Boolean {
        if (item == null || !item.hasTagCompound()) {
            return false
        }
        val display = item.getSubCompound("display", false)
        if (display == null || !display.hasKey("Lore")) {
            return false
        }
        val name = display.getString("Name")
        val petRarityMatcher = PET_PATTERN.matcher(name)
        return petRarityMatcher.find()
    }

    fun setSkullTexture(item: ItemStack, texture: String, SkullOwner: String): ItemStack {
        val textureTagCompound = NBTTagCompound()
        textureTagCompound.setString("Value", texture)

        val textures = NBTTagList()
        textures.appendTag(textureTagCompound)

        val properties = NBTTagCompound()
        properties.setTag("textures", textures)

        val skullOwner = NBTTagCompound()
        skullOwner.setString("Id", SkullOwner)
        skullOwner.setTag("Properties", properties)

        val nbtTag = NBTTagCompound()
        nbtTag.setTag("SkullOwner", skullOwner)

        item.tagCompound = nbtTag
        return item
    }
}