package cn.superiormc.manyouitems.methods;

import cn.superiormc.manyouitems.ErrorManager;
import cn.superiormc.manyouitems.hooks.CheckValidHook;
import cn.superiormc.manyouitems.util.CommonUtil;
import com.google.common.base.Enums;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrushableBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.*;

public class DebuildItem {

    public static ConfigurationSection debuildItem(ItemStack itemStack, ConfigurationSection section) {

        if (CheckValidHook.checkValid(itemStack) != null) {
            section.set("hook-plugin", CheckValidHook.checkValid(itemStack)[0]);
            section.set("hook-item", CheckValidHook.checkValid(itemStack)[1]);
        } else {
            // Material
            section.set("material", itemStack.getType().name());
        }

        // Amount
        section.set("amount", itemStack.getAmount());

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return section;
        }

        // Custom Name
        if (meta.hasDisplayName()) {
            section.set("name", meta.getDisplayName());
        }

        // Item Name
        if (CommonUtil.getMajorVersion() > 20 || (CommonUtil.getMajorVersion() == 20 && CommonUtil.getMinorVersion() >= 5)) {
            if (meta.hasItemName()) {
                section.set("item-name", meta.getItemName());
            }
        }

        // Lore
        if (meta.hasLore()) {
            section.set("lore", meta.getLore());
        }

        // Custom Model Data
        if (CommonUtil.getMajorVersion() >= 14) {
            if (meta.hasCustomModelData()) {
                section.set("custom-model-data", meta.getCustomModelData());
            }
        }

        // Max Stack
        if (CommonUtil.getMajorVersion() > 20 || (CommonUtil.getMajorVersion() == 20 && CommonUtil.getMinorVersion() >= 5)) {
            if (meta.hasMaxStackSize()) {
                section.set("max-stack", meta.getMaxStackSize());
            }
        }

        // Food
        if (CommonUtil.getMajorVersion() > 20 || (CommonUtil.getMajorVersion() == 20 && CommonUtil.getMinorVersion() >= 5)) {
            FoodComponent foodComponent = meta.getFood();
            if (foodComponent.getEatSeconds() != 1.6) {
                section.set("food.eat-seconds", foodComponent.getEatSeconds());
            }
            if (foodComponent.canAlwaysEat()) {
                section.set("food.can-alawys-eat", true);
            }
            if (foodComponent.getNutrition() > 0) {
                section.set("nutrition", foodComponent.getNutrition());
            }
            if (foodComponent.getSaturation() > 0) {
                section.set("saturation", foodComponent.getSaturation());
            }
            List<String> effects = new ArrayList<>();
            for (FoodComponent.FoodEffect foodEffect : foodComponent.getEffects()) {
                effects.add(foodEffect.getEffect().getType().getKey() + ", " + foodEffect.getEffect().getDuration() + ", " +
                        foodEffect.getEffect().getAmplifier() + ", " + foodEffect.getEffect().isAmbient() + ", " +
                        foodEffect.getEffect().hasParticles() + ", " + foodEffect.getEffect().hasIcon() + ", " +
                        foodEffect.getProbability());
            }
            if (!effects.isEmpty()) {
                section.set("effects", effects);
            }
        }

        // Fire Resistant
        if (CommonUtil.getMajorVersion() > 20 || (CommonUtil.getMajorVersion() == 20 && CommonUtil.getMinorVersion() >= 5)) {
            if (meta.isFireResistant()) {
                section.set("fire-resistant", "true");
            }
        }

        // Hide Tooltip
        if (CommonUtil.getMajorVersion() > 20 || (CommonUtil.getMajorVersion() == 20 && CommonUtil.getMinorVersion() >= 5)) {
            if (meta.isHideTooltip()) {
                section.set("hide-tool-tip", "true");
            }
        }

        // Unbreakable
        if (meta.isUnbreakable()) {
            section.set("unbreakable", "true");
        }

        // Rarity
        if (CommonUtil.getMajorVersion() > 20 || (CommonUtil.getMajorVersion() == 20 && CommonUtil.getMinorVersion() >= 5)) {
            if (meta.hasRarity()) {
                section.set("rarity", meta.getRarity().name());
            }
        }

        // Item Flag
        Set<ItemFlag> flags = meta.getItemFlags();
        if (!flags.isEmpty()) {
            List<String> flagNames = new ArrayList<>();
            for (ItemFlag flag : flags) {
                flagNames.add(flag.name());
            }
            section.set("flags", flagNames);
        }

        // Enchantments
        for (Map.Entry<Enchantment, Integer> enchant : meta.getEnchants().entrySet()) {
            String entry = "enchants." + enchant.getKey().getKey().getKey();
            section.set(entry, enchant.getValue());
        }

        // Attribute
        Multimap<Attribute, AttributeModifier> attributes = meta.getAttributeModifiers();
        if (attributes != null) {
            for (Map.Entry<Attribute, AttributeModifier> attribute : attributes.entries()) {
                String path = "attributes." + attribute.getKey().name() + '.';
                AttributeModifier modifier = attribute.getValue();

                section.set(path + "id", modifier.getUniqueId().toString());
                section.set(path + "name", modifier.getName());
                section.set(path + "amount", modifier.getAmount());
                section.set(path + "operation", modifier.getOperation().name());

                if (CommonUtil.getMajorVersion() > 20 || (CommonUtil.getMajorVersion() == 20 && CommonUtil.getMinorVersion() >= 5)) {
                    section.set("slot", modifier.getSlotGroup().toString());
                } else if (modifier.getSlot() != null) {
                    section.set(path + "slot", modifier.getSlot().name());
                }
            }
        }

        // Damage
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            if (damageable.hasDamage()) {
                section.set("damage", damageable.getDamage());
            }
            if (CommonUtil.getMajorVersion() > 20 || (CommonUtil.getMajorVersion() == 20 && CommonUtil.getMinorVersion() >= 5)) {
                if (damageable.hasMaxDamage()) {
                    section.set("max-damage", damageable.getMaxDamage());
                }
            }
        }

        // Stored Enchantments
        if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta book = (EnchantmentStorageMeta) meta;
            for (Map.Entry<Enchantment, Integer> enchant : book.getStoredEnchants().entrySet()) {
                String entry = "stored-enchants." + enchant.getKey().getKey().getKey();
                section.set(entry, enchant.getValue());
            }
        }

        // Banner
        if (meta instanceof BannerMeta) {
            BannerMeta banner = (BannerMeta) meta;
            ConfigurationSection patterns = section.createSection("patterns");
            for (Pattern pattern : banner.getPatterns()) {
                patterns.set(pattern.getPattern().name(), pattern.getColor().name());
            }
        }

        // Potion
        if (meta instanceof PotionMeta) {
            PotionMeta potion = (PotionMeta) meta;
            List<String> effects = new ArrayList<>();
            for (PotionEffect effect : potion.getCustomEffects()) {
                effects.add(effect.getType().getKey() + ", " + effect.getDuration() + ", " + effect.getAmplifier() + ", " +
                        effect.getAmplifier() + ", " + effect.isAmbient() + ", " +
                        effect.hasParticles() + ", " + effect.hasIcon());
            }
            if (!effects.isEmpty()) {
                section.set("effects", effects);
            }
            PotionType potionType = potion.getBasePotionType();
            if (potionType != null) {
                section.set("base-effect", potionType.getKey().toString());
            }
            if (potion.hasColor()) {
                section.set("color", potion.getColor().asRGB());
            }
        }

        // Armor Trim
        if (CommonUtil.getMajorVersion() >= 20) {
            if (meta instanceof ArmorMeta) {
                ArmorMeta armorMeta = (ArmorMeta) meta;
                if (armorMeta.hasTrim()) {
                    ArmorTrim trim = armorMeta.getTrim();
                    ConfigurationSection trimConfig = section.createSection("trim");
                    trimConfig.set("material", trim.getMaterial().getKey().toString());
                    trimConfig.set("pattern", trim.getPattern().getKey().toString());
                }
            }
        }

        // Leather Armor Color
        if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leather = (LeatherArmorMeta) meta;
            Color color = leather.getColor();
            section.set("color", color.asRGB());
        }

        // Axolotl Bucket
        if (CommonUtil.getMajorVersion() >= 17) {
            if (meta instanceof AxolotlBucketMeta) {
                AxolotlBucketMeta bucket = (AxolotlBucketMeta) meta;
                if (bucket.hasVariant()) {
                    section.set("color", bucket.getVariant().toString());
                }
            }
        }

        // Tropical Fish Bucket
        if (meta instanceof TropicalFishBucketMeta) {
            TropicalFishBucketMeta tropical = (TropicalFishBucketMeta) meta;
            section.set("pattern", tropical.getPattern().name());
            section.set("color", tropical.getBodyColor().name());
            section.set("pattern-color", tropical.getPatternColor().name());
        }

        // Skull
        if (meta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) meta;
            String skullTextureNameKey = section.getString("skull-meta", section.getString("skull"));
            if (skullTextureNameKey != null) {
                GameProfile profile = new GameProfile(UUID.randomUUID(), "");
                profile.getProperties().put("textures", new Property("textures", skullTextureNameKey));
                try {
                    Field field = skullMeta.getClass().getDeclaredField("profile");
                    field.setAccessible(true);
                    section.set("skull", field.get(skullMeta));
                } catch (Exception exception) {
                    ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[ManyouItems] §cError: Can not parse skull texture in a item!");
                }
            }
        }

        // Firework
        if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;
            section.set("power", firework.getPower());
            int i = 0;

            for (FireworkEffect fw : firework.getEffects()) {
                section.set("firework." + i + ".type", fw.getType().name());
                ConfigurationSection fwc = section.getConfigurationSection("firework." + i);
                fwc.set("flicker", fw.hasFlicker());
                fwc.set("trail", fw.hasTrail());

                List<Integer> baseColors = new ArrayList<>();
                List<Integer> fadeColors = new ArrayList<>();

                ConfigurationSection colors = fwc.createSection("colors");
                for (Color color : fw.getColors()) {
                    baseColors.add(color.asRGB());
                }
                colors.set("base", baseColors);

                for (Color color : fw.getFadeColors()) {
                    fadeColors.add(color.asRGB());
                }
                colors.set("fade", fadeColors);
                i++;
            }
        }

        // Suspicious Stew
        if (CommonUtil.getMajorVersion() >= 14) {
            if (meta instanceof SuspiciousStewMeta) {
                SuspiciousStewMeta stew = (SuspiciousStewMeta) meta;
                List<String> effects = new ArrayList<>();

                for (PotionEffect effect : stew.getCustomEffects()) {
                    effects.add(effect.getType().getKey() + ", " + effect.getDuration() + ", " + effect.getAmplifier());
                }

                section.set("effects", effects);
            }
        }

        // Bundle
        if (CommonUtil.getMajorVersion() >= 17) {
            if (meta instanceof BundleMeta) {
                BundleMeta bundleMeta = (BundleMeta) meta;

                if (bundleMeta.hasItems()) {
                    ConfigurationSection bundleContentKey = section.createSection("contents");
                    int i = 0;
                    for (ItemStack singleItem : bundleMeta.getItems()) {
                        if (singleItem != null && !singleItem.getType().isAir()) {
                            debuildItem(singleItem, bundleContentKey.createSection(Integer.toString(i)));
                        }
                        i++;
                    }
                }
            }
        }

        // Block
        if (meta instanceof BlockStateMeta) {
            BlockState state = ((BlockStateMeta) meta).getBlockState();

            if (state instanceof CreatureSpawner) {
                CreatureSpawner cs = (CreatureSpawner) state;
                if (cs.getSpawnedType() != null) {
                    section.set("spawner", cs.getSpawnedType().name());
                }
            } else if (state instanceof ShulkerBox) {
                ShulkerBox box = (ShulkerBox) state;

                if (!box.getInventory().isEmpty()) {
                    ConfigurationSection shulkerContentKey = section.createSection("contents");
                    int i = 0;
                    for (ItemStack singleItem : box.getInventory().getContents()) {
                        if (singleItem != null && !singleItem.getType().isAir()) {
                            debuildItem(singleItem, shulkerContentKey.createSection(Integer.toString(i)));
                        }
                        i++;
                    }
                }
            } else if (CommonUtil.getMajorVersion() >= 20 && state instanceof BrushableBlock) {
                BrushableBlock brushableBlock = (BrushableBlock) state;

                if (brushableBlock.getItem() != null && !brushableBlock.getItem().getType().isAir()) {
                    debuildItem(brushableBlock.getItem(), section.createSection("content"));
                }
            }
        }

        return section;
    }
}
