package io.github.redstoneparadox.creeperfall.game.shop;

import io.github.redstoneparadox.creeperfall.game.CreeperfallActive;
import io.github.redstoneparadox.creeperfall.game.config.CreeperfallShopConfig;
import io.github.redstoneparadox.creeperfall.game.participant.CreeperfallParticipant;
import io.github.redstoneparadox.creeperfall.game.participant.StatUpgrade;
import io.github.redstoneparadox.creeperfall.game.participant.Upgrade;
import io.github.redstoneparadox.creeperfall.game.util.TextHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.shop.ShopUi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CreeperfallShop {
	private static final String TRANSLATION_ROOT = "shop.creeperfall.";

	public static ShopUi create(CreeperfallParticipant participant, CreeperfallActive game, CreeperfallShopConfig shopConfig) {
		return ShopUi.create(new LiteralText("Shop"), shop -> {
			shop.add(upgrade(
					participant,
					shopConfig.armorUpgradePrices,
					participant.armorUpgrade,
					new TranslatableText(TRANSLATION_ROOT + "upgrade_armor"),
					new TranslatableText(TRANSLATION_ROOT + "upgrade_armor_complete")
			));
			shop.add(upgrade(
					participant,
					shopConfig.maxArrowUpgradePrices,
					participant.maxArrowsUpgrade,
					CreeperfallShop::getMaxArrowsText,
					new TranslatableText(TRANSLATION_ROOT + "upgrade_arrows_complete")
			));
			shop.add(summonSkeleton(game, shopConfig));
			shop.add(summonOcelot(game, shopConfig));
			shop.add(
					ShopEntry.ofIcon(Items.FIREWORK_ROCKET)
					.withName(new LiteralText("Crossbow and Fireworks"))
					.withCost(Cost.ofEmeralds(1))
					.onBuy(playerEntity -> participant.enableCrossbowAndFireworks())
			);
		});
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
				.withName(new TranslatableText(TRANSLATION_ROOT + allyName + "_ally"))
				.addLore(new TranslatableText(TRANSLATION_ROOT + allyName + "_ally.desc"))
				.addLore(new TranslatableText(TRANSLATION_ROOT + "despawn", despawnTime))
				.withCost(Cost.ofEmeralds(price))
				.onBuy(summon);

	}

	private static <T> Text getMaxArrowsText(Upgrade<T> upgrade) {
		if (upgrade instanceof StatUpgrade) {
			int currentTier = upgrade.getTier();
			int currentArrows = ((StatUpgrade) upgrade).getValue(currentTier);
			int nextArrows = ((StatUpgrade) upgrade).getValue(currentTier + 1);

			return new TranslatableText(TRANSLATION_ROOT + "upgrade_arrows", currentArrows, nextArrows);
		}

		return new LiteralText(TRANSLATION_ROOT + "error");
	}

	private static <T> ShopEntry upgrade(CreeperfallParticipant participant, List<Integer> prices, Upgrade<T> upgrade, Text message, Text fullyUpgradedMessage) {
		return upgrade(participant, prices, upgrade, (upgrade1 -> message), fullyUpgradedMessage);
	}

	private static <T> ShopEntry upgrade(CreeperfallParticipant participant, List<Integer> prices, Upgrade<T> upgrade, Function<Upgrade<T>, Text> messageSupplier, Text fullyUpgradedMessage) {
		ItemStack icon = upgrade.getIcon();
		int tier = upgrade.getTier();

		if (upgrade.canUpgrade()) {
			return ShopEntry
					.ofIcon(icon)
					.withName(messageSupplier.apply(upgrade))
					.withCost(Cost.ofEmeralds(prices.get(tier)))
					.onBuy(playerEntity -> upgrade.upgrade(participant));
		}
		else {
			return ShopEntry
					.ofIcon(icon)
					.withName(fullyUpgradedMessage)
					.withCost(Cost.no());
		}
	}
}
