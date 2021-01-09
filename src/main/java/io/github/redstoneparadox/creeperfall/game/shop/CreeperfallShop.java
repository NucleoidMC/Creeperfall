package io.github.redstoneparadox.creeperfall.game.shop;

import io.github.redstoneparadox.creeperfall.game.CreeperfallActive;
import io.github.redstoneparadox.creeperfall.game.participant.CreeperfallParticipant;
import io.github.redstoneparadox.creeperfall.game.participant.Upgrade;
import io.github.redstoneparadox.creeperfall.game.util.TextHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.shop.ShopUi;

import java.util.List;

public class CreeperfallShop {
	public static ShopUi create(CreeperfallParticipant participant, CreeperfallActive game, CreeperfallShopConfig shopConfig) {
		return ShopUi.create(new LiteralText("Shop"), shop -> {
			shop.add(upgrade(participant, shopConfig, participant.armorUpgrade));
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

	private static ShopEntry upgrade(CreeperfallParticipant participant, CreeperfallShopConfig shopConfig, Upgrade upgrade) {
		ItemStack icon = upgrade.getIcon();
		int tier = upgrade.getTier();

		if (upgrade.canUpgrade()) {
			return ShopEntry
					.ofIcon(icon)
					.withName(new LiteralText("Upgrade Armor"))
					.withCost(Cost.ofEmeralds(shopConfig.armorUpgradePrices.get(tier)))
					.onBuy(playerEntity -> upgrade.upgrade(participant));
		}
		else {
			return ShopEntry
					.ofIcon(icon)
					.withName(new LiteralText("Armor Fully Upgraded"))
					.withCost(Cost.no());
		}
	}
}
