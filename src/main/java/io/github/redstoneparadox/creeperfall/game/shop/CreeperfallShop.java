package io.github.redstoneparadox.creeperfall.game.shop;

import io.github.redstoneparadox.creeperfall.game.CreeperfallActive;
import io.github.redstoneparadox.creeperfall.game.participant.CreeperfallParticipant;
import io.github.redstoneparadox.creeperfall.game.participant.Upgrade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.*;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.shop.ShopUi;

import java.util.ArrayList;
import java.util.List;

public class CreeperfallShop {
	public static ShopUi create(CreeperfallParticipant participant, CreeperfallActive game, CreeperfallShopConfig shopConfig) {
		return ShopUi.create(new LiteralText("Shop"), shop -> {
			shop.add(upgrade(participant, shopConfig, participant.armorUpgrade));
			shop.add(summonGuardian(game, shopConfig));
			shop.add(summonOcelot(game, shopConfig));
		});
	}

	private static ShopEntry summonGuardian(CreeperfallActive game, CreeperfallShopConfig shopConfig) {
		ShopEntry entry = ShopEntry.
				ofIcon(Items.GUARDIAN_SPAWN_EGG)
				.withName(new LiteralText("Spawn Guardian"));

		List<Text> wrapped = wrapText(new LiteralText("Summons a Guardian to shoot down creepers that get too close. Despawns after 30 seconds."));
		for (Text text : wrapped) {
			entry.addLore(text);
		}

		entry
				.withCost(Cost.ofEmeralds(shopConfig.guardianPrice))
				.onBuy(playerEntity -> game.spawnGuardian());

		return entry;
	}

	private static ShopEntry summonOcelot(CreeperfallActive game, CreeperfallShopConfig shopConfig) {
		ShopEntry entry = ShopEntry.
				ofIcon(Items.OCELOT_SPAWN_EGG)
				.withName(new LiteralText("Spawn Cat"));

		List<Text> wrapped = wrapText(new LiteralText("Summons a Cat to scare Creepers to death. Despawns after 3 seconds."));
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

	private static List<Text> wrapText(StringVisitable text) {
		String s = text.getString();
		StringBuilder sb = new StringBuilder(s);

		int i = 0;
		while ((i = sb.indexOf(" ", i + 20)) != -1) {
			sb.replace(i, i + 1, "\n");
		}

		String[] strings = sb.toString().split("\n");
		List<Text> texts = new ArrayList<>();

		for (String string: strings) {
			texts.add(new LiteralText(string));
		}

		return texts;
	}
}
