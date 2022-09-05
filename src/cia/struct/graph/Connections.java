package cia.struct.graph;

import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public final class Connections implements Iterable<Connection> {
	private final @NotNull MutableObjectIntMap<Connection> map;


	public Connections() {
		this.map = new ObjectIntHashMap<>();
	}

	public Connections(@NotNull Connections connections) {
		this.map = new ObjectIntHashMap<>(connections.map);
	}


	public int getCount(@NotNull Connection type) {
		return map.get(type);
	}

	public void setCount(@NotNull Connection type, int count) {
		if (count < 0) throw new IllegalArgumentException();
		if (count == 0) map.removeKey(type);
		else map.put(type, count);
	}

	public void addCount(@NotNull Connection type, int delta) {
		if (map.addToValue(type, delta) == 0) map.removeKey(type);
	}

	public void subtractCount(@NotNull Connection type, int delta) {
		addCount(type, -delta);
	}


	@Override
	public @NotNull Iterator<Connection> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public @NotNull String toString() {
		return map.toString();
	}

	@Override
	public boolean equals(@Nullable Object object) {
		return this == object || object instanceof Connections
				&& map.equals(((Connections) object).map);
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}
}
