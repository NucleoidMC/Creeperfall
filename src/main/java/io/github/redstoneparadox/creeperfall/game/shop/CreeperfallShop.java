package io.github.redstoneparadox.creeperfall.game.shop;

import io.github.redstoneparadox.creeperfall.game.CreeperfallActive;
import io.github.redstoneparadox.creeperfall.game.config.CreeperfallShopConfig;
import io.github.redstoneparadox.creeperfall.game.participant.CreeperfallParticipant;
import io.github.redstoneparadox.creeperfall.game.participant.StatUpgrade;
import io.github.redstoneparadox.creeperfall.game.participant.Upgrade;
import io.github.redstoneparadox.creeperfall.game.util.TextHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.shop.ShopUi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CreeperfallShop {
	public static ShopUi create(CreeperfallParticipant participant, CreeperfallActive game, CreeperfallShopConfig shopConfig) {
		return ShopUi.create(new LiteralText("Shop"), shop -> {
			shop.add(upgrade(participant, shopConfig.armorUpgradePrices, participant.armorUpgrade, new LiteralText("Upgrade Armor"), new LiteralText("Armor fully upgraded")));
			shop.add(upgrade(participant, shopConfig.maxArrowUpgradePrices, participant.maxArrowsUpgrade, CreeperfallShop::getMaxArrowsText, new LiteralText("Max arrows fully increased.")));
			shop.add(summonSkeleton(game, shopConfig));
			shop.add(summonOcelot(game, shopConfig));
		});
	}

	private static ShopEntry summonGuardian(CreeperfallActive game, CreeperfallShopConfig shopConfig) {
		ShopEntry entry = ShopEntry.
				ofIcon(Items.GUARDIAN_SPAWN_EGG)
				.withName(new LiteralText("Spawn Guardian"));

		List<Text> wrapped = TextHelper.wrapText(
				new LiteralText("Summons a Guardian to shoot down creepers that get too close. Despawns after 30 seconds."),
				25
		);
		for (Text text : wrapped) {
			entry.addLore(text);
		}

		entry
				.withCost(Cost.ofEmeralds(shopConfig.skeletonPrice))
				.onBuy(playerEntity -> game.spawnGuardian());

		return entry;
	}

	private static ShopEntry summonSkeleton(CreeperfallActive game, CreeperfallShopConfig shopConfig) {
		ShopEntry entry = ShopEntry.
				ofIcon(Items.SKELETON_SPAWN_EGG)
				.withName(new LiteralText("Spawn Skeleton"));

		List<Text> wrapped = TextHelper.wrapText(
				new LiteralText("Summons a Skeleton to shoot at Creepers. Despawns after 30 seconds."),
				25
		);
		for (Text text : wrapped) {
			entry.addLore(text);
		}

		entry
				.withCost(Cost.ofEmeralds(shopConfig.skeletonPrice))
				.onBuy(playerEntity -> game.spawnSkeleton());

		return entry;
	}

	private static ShopEntry summonOcelot(CreeperfallActive game, CreeperfallShopConfig shopConfig) {
		ShopEntry entry = ShopEntry.
				ofIcon(Items.OCELOT_SPAWN_EGG)
				.withName(new LiteralText("Spawn Cat"));

		List<Text> wrapped = TextHelper.wrapText(
				new LiteralText("Summons a Cat to scare Creepers to death. Despawns after 3 seconds."),
				25
		);
		for (Text text : wrapped) {
			entry.addLore(text);
		}

		entry
				.withCost(Cost.ofEmeralds(shopConfig.ocelotPrice))
				.onBuy(playerEntity -> game.spawnOcelot());

		return entry;
	}

	private static <T> Text getMaxArrowsText(Upgrade<T> upgrade) {
		if (upgrade instanceof StatUpgrade) {
			int currentTier = upgrade.getTier();
			int currentArrows = ((StatUpgrade) upgrade).getValue(currentTier);
			int nextArrows = ((StatUpgrade) upgrade).getValue(currentTier + 1);

			return new LiteralText(currentArrows + " Arrows -> " + nextArrows + " Arrows on refresh.");
		}

		return new LiteralText("");
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
