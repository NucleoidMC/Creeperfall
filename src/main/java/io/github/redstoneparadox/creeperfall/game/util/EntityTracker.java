package io.github.redstoneparadox.creeperfall.game.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntityTracker {
	private Map<EntityType<?>, Set<Entity>> entityMap = new HashMap<>();

	public void add(Entity entity) {
		EntityType<?> type = entity.getType();
		Set<Entity> set = entityMap.computeIfAbsent(type, entityType -> new HashSet<>());
		set.add(entity);
	}

	public void remove(Entity entity) {
		EntityType<?> type = entity.getType();
		Set<Entity> set = entityMap.computeIfAbsent(type, entityType -> new HashSet<>());
		set.remove(entity);
	}

	public void clean() {
		Collection<Set<Entity>> setCollection = entityMap.values();

		for (Set<Entity> set : setCollection) {
			set.removeIf(entity -> entity.isRemoved());
		}
	}

	@SuppressWarnings("unchecked")
	public <E extends Entity> Set<E> getAll(EntityType<E> type) {
		return (Set<E>) entityMap.computeIfAbsent(type, entityType -> new HashSet<>());
	}
}
