package cia.struct.graph;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GraphDiffer {
	private final @NotNull Graph graphA;
	private final @NotNull Graph graphB;

	private final @NotNull NodeDiffer differ;

	public GraphDiffer(@NotNull Graph graphA, @NotNull Graph graphB) {
		this.graphA = graphA;
		this.graphB = graphB;
		this.differ = new NodeDiffer(new NodeHasher(graphA), new NodeHasher(graphB));
	}


	// TODO: compare whole graph
	public <N extends Node, G extends Group<N>> @NotNull GraphDifference<N> compare(@NotNull G groupA,
			@NotNull G groupB) {
		if (graphA.containsGroup(groupA) && graphB.containsGroup(groupB)) {
			return compareSwap(groupA, groupB);
		} else if (graphA.containsGroup(groupB) && graphB.containsGroup(groupA)) {
			return compareSwap(groupB, groupA);
		} else {
			throw new IllegalArgumentException("Node does not exist in graph!");
		}
	}

	public <N extends Node, G extends Group<N>> @NotNull GraphDifference<N> compareSwap(@NotNull G groupA,
			@NotNull G groupB) {
		assert graphA.containsGroup(groupA) && graphB.containsGroup(groupB);

		final Map<NodeWrapper<N>, N> nodesB = new HashMap<>();
		for (final N node : groupB.getNodes()) {
			final NodeWrapper<N> wrapper = differ.wrapB(node, true);
			nodesB.put(wrapper, node);
		}

		final List<Pair<N, N>> differences = new ArrayList<>();
		for (final N nodeA : groupA.getNodes()) {
			final NodeWrapper<N> wrapperA = differ.wrapA(nodeA, true);
			final N nodeB = nodesB.remove(wrapperA);
			differences.add(differ.isIdentical(nodeA, nodeB)
					? Tuples.twin(nodeA, nodeB)
					: Tuples.pair(nodeA, nodeB));
		}

		for (final N nodeB : nodesB.values()) {
			differences.add(Tuples.twin(null, nodeB));
		}

		return new GraphDifference<>(differences);
	}


//	private <N extends Node> @NotNull List<Twin<N>> findMatch(@NotNull Collection<N> collectionA,
//			@NotNull Collection<N> collectionB, boolean isSimilar) throws GraphException {
//		if (collectionA.isEmpty() || collectionB.isEmpty()) return List.of();
//
//		final Map<NodeWrapper<N>, N> nodeMapA = new LinkedHashMap<>();
//		for (final N nodeA : collectionA) {
//			nodeMapA.put(differ.wrap(nodeA, isSimilar, true), nodeA);
//		}
//
//		final List<Twin<N>> twins = new ArrayList<>(Math.min(collectionA.size(), collectionB.size()));
//		for (final N nodeB : collectionB) {
//			final N nodeA = nodeMapA.get(differ.wrap(nodeB, isSimilar, false));
//			if (nodeA != null) {
//				twins.add(Tuples.twin(nodeA, nodeB));
//			}
//		}
//		return twins;
//	}
//
//	public <N extends Node> @NotNull List<Twin<N>> findSimilar(@NotNull Collection<N> collectionA,
//			@NotNull Collection<N> collectionB) throws GraphException {
//		return findMatch(collectionA, collectionB, true);
//	}
//
//	public <N extends Node> @NotNull List<Twin<N>> findIdentical(@NotNull Collection<N> collectionA,
//			@NotNull Collection<N> collectionB) throws GraphException {
//		return findMatch(collectionA, collectionB, false);
//	}


	public boolean isSimilar(@Nullable Node nodeA, @Nullable Node nodeB) throws GraphException {
		return differ.isSimilar(nodeA, nodeB);
	}

	public boolean isIdentical(@Nullable Node nodeA, @Nullable Node nodeB) throws GraphException {
		return differ.isIdentical(nodeA, nodeB);
	}
}
