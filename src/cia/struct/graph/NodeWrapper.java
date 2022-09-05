package cia.struct.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class NodeWrapper<E extends Node> {
	private final @NotNull NodeDiffer differ;
	private final @NotNull E node;
	private final int hashCode;
	private final boolean similarMatch;

	NodeWrapper(@NotNull NodeDiffer differ, @NotNull E node, int hashCode, boolean similarMatch) {
		this.differ = differ;
		this.node = node;
		this.hashCode = hashCode;
		this.similarMatch = similarMatch;
	}

	@NotNull E getNode() {
		return node;
	}

	@Override
	public boolean equals(@Nullable Object object) {
		if (this == object) return true;
		if (!(object instanceof NodeWrapper)) return false;
		final NodeWrapper<?> wrapper = (NodeWrapper<?>) object;
		if (hashCode != wrapper.hashCode) return false;
		return similarMatch
				? differ.isSimilar(node, wrapper.node)
				: differ.isIdentical(node, wrapper.node);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
}
