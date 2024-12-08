package io.github.redstoneparadox.creeperfall.game.shop;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.redstoneparadox.creeperfall.game.CreeperfallActive;
import io.github.redstoneparadox.creeperfall.game.config.CreeperfallShopConfig;
import io.github.redstoneparadox.creeperfall.game.participant.CreeperfallParticipant;
import io.github.redstoneparadox.creeperfall.game.participant.StatUpgrade;
import io.github.redstoneparadox.creeperfall.game.participant.Upgrade;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.shop.Cost;
import xyz.nucleoid.plasmid.api.shop.ShopEntry;
import xyz.nucleoid.plasmid.api.util.Guis;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CreeperfallShop {
    private static final String TRANSLATION_ROOT = "shop.creeperfall.";

    public static SimpleGui create(CreeperfallParticipant participant, CreeperfallActive game, CreeperfallShopConfig shopConfig) {
        var shop = new ArrayList<GuiElementInterface>();

        shop.add(upgrade(
                participant,
                shopConfig.armorUpgradePrices,
                participant.armorUpgrade,
                Text.translatable(TRANSLATION_ROOT + "upgrade_armor"),
                Text.translatable(TRANSLATION_ROOT + "upgrade_armor_complete")
        ));
        shop.add(upgrade(
                participant,
                shopConfig.maxArrowUpgradePrices,
                participant.maxArrowsUpgrade,
                CreeperfallShop::getMaxArrowsText,
                Text.translatable(TRANSLATION_ROOT + "upgrade_arrows_complete")
        ));
        shop.add(summonSkeleton(game, shopConfig));
        shop.add(summonOcelot(game, shopConfig));
			/*
			shop.add(
					ShopEntry.ofIcon(Items.FIREWORK_ROCKET)
					.withName(Text.literal("Crossbow and Fireworks"))
					.withCost(Cost.ofEmeralds(1))
					.onBuy(playerEntity -> participant.enableCrossbowAndFireworks())
			);

			 */

        var gui = Guis.createSelectorGui(participant.getPlayerEntity(), Text.translatable(TRANSLATION_ROOT + "title"), false, shop);
        gui.open();

        return gui;
    }

    private static ShopEntry summonGuardian(CreeperfallActive game, CreeperfallShopConfig shopConfig) {
        return ally(Items.GUARDIAN_SPAWN_EGG, "guardian", 60, shopConfig.skeletonPrice, playerEntity -> game.spawnGuardian());
    }

    private static ShopEntry summonSkeleton(CreeperfallActive game, CreeperfallShopConfig shopConfig) {
        return ally(Items.SKELETON_SPAWN_EGG, "skeleton", 30, shopConfig.skeletonPrice, playerEntity -> game.spawnSkeleton());
    }

    private static ShopEntry summonOcelot(CreeperfallActive game, CreeperfallShopConfig shopConfig) {
        return ally(Items.OCELOT_SPAWN_EGG, "ocelot", 30, shopConfig.ocelotPrice, playerEntity -> game.spawnOcelot());
    }

    private static ShopEntry ally(Item icon, String allyName, int despawnTime, int price, Consumer<ServerPlayerEntity> summon) {
        return ShopEntry.ofIcon(icon)
                .withName(Text.translatable(TRANSLATION_ROOT + allyName + "_ally"))
                .addLore(Text.translatable(TRANSLATION_ROOT + allyName + "_ally.desc"))
                .addLore(Text.translatable(TRANSLATION_ROOT + "despawn", despawnTime))
                .withCost(Cost.ofEmeralds(price))
                .onBuy(summon);

    }

    private static <T> Text getMaxArrowsText(Upgrade<T> upgrade) {
        if (upgrade instanceof StatUpgrade) {
            int currentTier = upgrade.getTier();
            int currentArrows = ((StatUpgrade) upgrade).getValue(currentTier);
            int nextArrows = ((StatUpgrade) upgrade).getValue(currentTier + 1);

            return Text.translatable(TRANSLATION_ROOT + "upgrade_arrows", currentArrows, nextArrows);
        }

        return Text.translatable(TRANSLATION_ROOT + "error");
    }

    private static <T> ShopEntry upgrade(CreeperfallParticipant participant, List<Integer> prices, Upgrade<T> upgrade, Text message, Text fullyUpgradedMessage) {
        return upgrade(participant, prices, upgrade, (upgrade1 -> message), fullyUpgradedMessage);
    }

    private static <T> ShopEntry upgrade(CreeperfallParticipant participant, List<Integer> prices, Upgrade<T> upgrade, Function<Upgrade<T>, Text> messageSupplier, Text fullyUpgradedMessage) {
        return ShopEntry.ofIcon((p, e) -> {
            var icon = upgrade.getIcon();

            var cost = e.getCost(p);
            assert cost != null;

            var style = Style.EMPTY.withItalic(false).withColor(cost.canBuy(p) ? Formatting.BLUE : Formatting.RED);
            var name = messageSupplier.apply(upgrade).copy().setStyle(style);

            var costText = cost.getDisplay();
            costText = Text.literal(" (").append(costText).append(")").setStyle(costText.getStyle());
            name.append(costText);

            icon.set(DataComponentTypes.CUSTOM_NAME, name);
            return icon;
        })
                .withCost((p, e) -> upgrade.canUpgrade() ? Cost.ofEmeralds(prices.get(upgrade.getTier())) : Cost.no())
                .onBuy(playerEntity -> upgrade.upgrade(participant));

    }
}
