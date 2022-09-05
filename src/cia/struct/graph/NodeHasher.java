package cia.struct.graph;

import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public final class NodeHasher {
	private final @NotNull Graph graph;

	private final @NotNull Set<Node> calculatingNodes = new HashSet<>();
	private final @NotNull Deque<MutableObjectIntMap<Node>> cacheStack = new ArrayDeque<>();

	NodeHasher(@NotNull Graph graph) {
		this.graph = graph;

		cacheStack.push(new ObjectIntHashMap<>());
	}

	@NotNull Graph getGraph() {
		return graph;
	}

	int unsafeHash(@NotNull Node node) {
		// Loop breaker
		if (calculatingNodes.contains(node)) return -1;

		// Multiple level but independent cache
		final MutableObjectIntMap<Node> hashCache = cacheStack.element(); // peek or throw
		final int cached = hashCache.get(node);
		if (cached != 0 || hashCache.containsKey(node)) return cached;

		// Create new cache level
		cacheStack.push(new ObjectIntHashMap<>()); // create
		calculatingNodes.add(node);

		final int hash = node.hash(this);

		calculatingNodes.remove(node);
		cacheStack.pop(); // remove

		// add to current level cache
		hashCache.put(node, hash);
		return hash;
	}

	public int hash(@Nullable Node node) {
		if (node != null && !graph.containsNode(node)) {
			throw new IllegalArgumentException("The graph doesn't contain the node!");
		}
		return node != null ? unsafeHash(node) : 1;
	}

	public int hashOrdered(@NotNull Collection<? extends Node> nodes) {
		int hash = nodes.size() * 31;
		for (final Node node : nodes) {
			hash += hash(node);
		}
		return hash;
	}

	public int hashUnordered(@NotNull Collection<? extends Node> nodes) {
		int hash = nodes.size();
		for (final Node node : nodes) {
			hash = hash * 31 + hash(node);
		}
		return hash;
	}
}
