package io.github.redstoneparadox.creeperfall.item;

import io.github.redstoneparadox.creeperfall.entity.CreeperfallCreeperEntity;
import io.github.redstoneparadox.creeperfall.entity.CreeperfallGuardianEntity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.fake.FakeItem;

import java.util.Objects;

public class CreeperfallGuardianSpawnEgg extends Item implements FakeItem {
	public CreeperfallGuardianSpawnEgg() {
		super(new FabricItemSettings());
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (!Objects.requireNonNull(context.getPlayer()).abilities.allowModifyWorld) {
			return ActionResult.PASS;
		}

		Direction facing = context.getPlayerFacing();
		BlockPos centerPos = context.getBlockPos().offset(facing, 1);

		ServerWorld world = (ServerWorld) context.getWorld();
		CreeperfallGuardianEntity entity = new CreeperfallGuardianEntity(world);

		int x = centerPos.getX();
		int y = centerPos.getY();
		int z = centerPos.getZ();

		Objects.requireNonNull(entity).setPos(x, y, z);
		entity.updatePosition(x, y, z);
		entity.setVelocity(Vec3d.ZERO);

		entity.prevX = x;
		entity.prevY = y;
		entity.prevZ = z;

		entity.setInvulnerable(true);
		entity.initialize(world, world.getLocalDifficulty(new BlockPos(0, 0, 0)), SpawnReason.SPAWN_EGG, null, null);
		world.spawnEntity(entity);

		return ActionResult.SUCCESS;
	}

	@Override
	public Item asProxy() {
		return Items.GUARDIAN_SPAWN_EGG;
	}
}
