package cia.struct.graph;

import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.api.tuple.Twin;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.impl.tuple.Tuples;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class NodeDiffer {
	private final @NotNull NodeHasher hasherA;
	private final @NotNull NodeHasher hasherB;

	private final @NotNull DifferCache cacheA;
	private final @NotNull DifferCache cacheB;
	private final @NotNull DifferCache cache;

	NodeDiffer(@NotNull NodeHasher hasherA, @NotNull NodeHasher hasherB) {
		this.hasherA = hasherA;
		this.hasherB = hasherB;
		this.cacheA = new DifferCache();
		this.cacheB = new DifferCache();
		this.cache = new DifferCache();
	}

	private final class DifferCache {
		private final @NotNull Deque<Twin<Set<Twin<Node>>>> setTwinStack = new ArrayDeque<>();

		private final @NotNull Set<Twin<Node>> notSimilarSet = new HashSet<>();
		private final @NotNull Set<Twin<Node>> notIdenticalSet = new HashSet<>();

		public DifferCache() {
			setTwinStack.push(Tuples.twin(new HashSet<>(), new HashSet<>()));
		}

		private boolean match(@NotNull Node nodeA, @NotNull Node nodeB, boolean similarMatch) {
			assert nodeA != nodeB;
			// This key will guarantee to work on when in same graph differ mode
			final Twin<Node> key = nodeA.hashCode() <= nodeB.hashCode()
					? Tuples.twin(nodeA, nodeB)
					: Tuples.twin(nodeB, nodeA);

			// === Negative cache ===
			// not similar -> never match
			if (notSimilarSet.contains(key)) return false;
			// not identical && looking for identical -> not match
			if (!similarMatch && notIdenticalSet.contains(key)) return false;

			// === Layered positive cache ===
			for (final Twin<Set<Twin<Node>>> setTwin : setTwinStack) {
				// if they are identical then they are also similar
				// identicalSet == setTwin.getTwo()
				if (setTwin.getTwo().contains(key)) return true;
				// similarSet == setTwin.getOne()
				if (similarMatch && setTwin.getOne().contains(key)) return true;
			}

			// ===  Calculate and compare ===
			// create new layer
			final Set<Twin<Node>> newSimilarSet = new HashSet<>();
			final Set<Twin<Node>> newIdenticalSet = new HashSet<>();
			setTwinStack.push(Tuples.twin(newSimilarSet, newIdenticalSet));

			// theory: current key is true
			if (similarMatch) {
				newSimilarSet.add(key);
			} else {
				newIdenticalSet.add(key);
			}

			// check for theory
			final boolean result = similarMatch
					? nodeA.isSimilar(NodeDiffer.this, nodeB)
					: nodeA.isIdentical(NodeDiffer.this, nodeB);

			// remove new layer
			setTwinStack.pop();

			// theory is incorrect
			if (!result) {
				if (similarMatch) {
					notSimilarSet.add(key);
					// not similar -> never identical
					// duplicate; removed for memory
					notIdenticalSet.remove(key);
				} else {
					notIdenticalSet.add(key);
				}
				return false;
			}

			// theory is correct -> combine result
			final Twin<Set<Twin<Node>>> setTwin = setTwinStack.element();
			final Set<Twin<Node>> similarSet = setTwin.getOne();
			final Set<Twin<Node>> identicalSet = setTwin.getTwo();
			similarSet.removeAll(newIdenticalSet);
			similarSet.addAll(newSimilarSet);
			identicalSet.addAll(newIdenticalSet);
			return true;
		}

	}

	<E extends Node> @NotNull NodeWrapper<E> wrapA(@NotNull E node, boolean similarMatch) {
		return new NodeWrapper<>(this, node, hasherA.unsafeHash(node), similarMatch);
	}

	<E extends Node> @NotNull NodeWrapper<E> wrapB(@NotNull E node, boolean similarMatch) {
		return new NodeWrapper<>(this, node, hasherB.unsafeHash(node), similarMatch);
	}

	private boolean match(@Nullable Node nodeA, @Nullable Node nodeB, boolean similarMatch) {
		if (nodeA == nodeB) return true;
		if (nodeA == null || nodeB == null) return false;
		final Graph graphA = hasherA.getGraph();
		final Graph graphB = hasherB.getGraph();
		final boolean aa = graphA.containsNode(nodeA);
		final boolean bb = graphB.containsNode(nodeB);
		if (aa && bb) return cache.match(nodeA, nodeB, similarMatch);
		final boolean ab = graphA.containsNode(nodeB);
		final boolean ba = graphB.containsNode(nodeA);
		if (ab && ba) return cache.match(nodeB, nodeA, similarMatch);
		if (aa && ab) return cacheA.match(nodeA, nodeB, similarMatch);
		if (ba && bb) return cacheB.match(nodeA, nodeB, similarMatch);
		throw new IllegalArgumentException("Node does not exist in graph!");
	}

	private boolean matchOrdered(@NotNull Collection<? extends Node> nodesA,
			@NotNull Collection<? extends Node> nodesB, boolean similarMatch) {
		if (nodesA.size() != nodesB.size()) return false;
		final Iterator<? extends Node> iteratorA = nodesA.iterator();
		final Iterator<? extends Node> iteratorB = nodesB.iterator();
		if (similarMatch) {
			while (iteratorA.hasNext()/* && iteratorB.hasNext()*/) {
				if (!isSimilar(iteratorA.next(), iteratorB.next())) return false;
			}
		} else {
			while (iteratorA.hasNext()/* && iteratorB.hasNext()*/) {
				if (!isIdentical(iteratorA.next(), iteratorB.next())) return false;
			}
		}
		return true;
	}

	private boolean matchUnordered(@NotNull Collection<? extends Node> nodesA,
			@NotNull Collection<? extends Node> nodesB, boolean similarMatch) {
		if (nodesA.size() != nodesB.size()) return false;
		final MutableObjectIntMap<NodeWrapper<?>> map = new ObjectIntHashMap<>();
		for (final Node node : nodesA) {
			final NodeWrapper<?> wrapper = wrapA(node, similarMatch);
			map.addToValue(wrapper, 1);
		}
		for (final Node node : nodesB) {
			final NodeWrapper<?> wrapper = wrapB(node, similarMatch);
			if (map.addToValue(wrapper, -1) < 0) return false;
		}
		return true;
	}

	public boolean isSimilar(@Nullable Node nodeA, @Nullable Node nodeB) {
		return match(nodeA, nodeB, true);
	}

	public boolean similarOrdered(@NotNull Collection<? extends Node> nodesA,
			@NotNull Collection<? extends Node> nodesB) {
		return matchOrdered(nodesA, nodesB, true);
	}

	public boolean similarUnordered(@NotNull Collection<? extends Node> nodesA,
			@NotNull Collection<? extends Node> nodesB) {
		return matchUnordered(nodesA, nodesB, true);
	}

	public boolean isIdentical(@Nullable Node nodeA, @Nullable Node nodeB) {
		return match(nodeA, nodeB, false);
	}

	public boolean identicalOrdered(@NotNull Collection<? extends Node> nodesA,
			@NotNull Collection<? extends Node> nodesB) {
		return matchOrdered(nodesA, nodesB, false);
	}

	public boolean identicalUnordered(@NotNull Collection<? extends Node> nodesA,
			@NotNull Collection<? extends Node> nodesB) {
		return matchUnordered(nodesA, nodesB, false);
	}
}