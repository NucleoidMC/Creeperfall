package io.github.redstoneparadox.creeperfall.game.shop;

import io.github.redstoneparadox.creeperfall.game.CreeperfallActive;
import io.github.redstoneparadox.creeperfall.game.participant.CreeperfallParticipant;
import io.github.redstoneparadox.creeperfall.game.participant.Upgrade;
import io.github.redstoneparadox.creeperfall.game.util.OrderedTextReader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.*;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.shop.ShopUi;

import java.util.List;

public class CreeperfallShop {
	private static final OrderedTextReader READER = new OrderedTextReader();

	public static ShopUi create(CreeperfallParticipant participant, CreeperfallActive game, CreeperfallShopConfig shopConfig) {
		return ShopUi.create(new LiteralText("Shop"), shop -> {
			shop.add(upgrade(participant, shopConfig, participant.armorUpgrade));
			shop.add(summonGuardian(game, shopConfig));
			shop.add(summonOcelot(game, shopConfig));
			shop.add(jumpBoost(shopConfig));
		});
	}

	private static ShopEntry summonGuardian(CreeperfallActive game, CreeperfallShopConfig shopConfig) {
		ShopEntry entry = ShopEntry.
				ofIcon(Items.GUARDIAN_SPAWN_EGG)
				.withName(new LiteralText("Spawn Guardian"));

		List<OrderedText> wrapped = wrapText(new LiteralText("Summons a Guardian to shoot down creepers that get too close. Despawns after 30 seconds."));
		for (OrderedText orderedText : wrapped) {
			Text text = READER.read(orderedText);
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

		List<OrderedText> wrapped = wrapText(new LiteralText("Summons a Cat to scare Creepers to death. Despawns after 3 seconds."));
		for (OrderedText orderedText : wrapped) {
			Text text = READER.read(orderedText);
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

	private static ShopEntry jumpBoost(CreeperfallShopConfig shopConfig) {
		ItemStack icon = PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.LEAPING);

		ShopEntry entry = ShopEntry
				.ofIcon(icon);

		List<OrderedText> wrapped = wrapText(new LiteralText("Gives you 30 seconds of jump boost."));
		for (OrderedText orderedText : wrapped) {
			Text text = READER.read(orderedText);
			entry.addLore(text);
		}

		return entry
				.withCost(Cost.ofEmeralds(1))
				.onBuy(player -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 30 * 20 + 5)));
	}

	private static List<OrderedText> wrapText(StringVisitable text) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		return textRenderer.wrapLines(text, 24 * 8);
	}
}
