package cia.struct.graph;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.api.tuple.Triple;
import org.eclipse.collections.api.tuple.Twin;
import org.eclipse.collections.impl.tuple.Tuples;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;

public final class GraphDifference<N extends Node>
		extends AbstractCollection<Triple<N, N, GraphChange>>
		implements Collection<Triple<N, N, GraphChange>> {
	private final @NotNull Collection<Pair<N, N>> differences;

	GraphDifference(@NotNull Collection<Pair<N, N>> differences) {
		this.differences = differences;
	}


	/**
	 * Create a map from each node in graphA to a corresponding node from graphB, or to null if there is no
	 * corresponding node from graphB.
	 *
	 * @return The new map.
	 */
	public @NotNull Map<N, Pair<N, GraphChange>> createNodeMapA() {
		final Map<N, Pair<N, GraphChange>> changeMapA = new HashMap<>();
		for (final Pair<N, N> pair : differences) {
			final N nodeA = pair.getOne();
			if (nodeA != null) {
				final N nodeB = pair.getTwo();
				final GraphChange change = nodeB == null
						? GraphChange.REMOVED
						: pair instanceof Twin
						? GraphChange.UNCHANGED
						: GraphChange.CHANGED;
				changeMapA.put(nodeA, Tuples.pair(nodeB, change));
			}
		}
		return changeMapA;
	}

	/**
	 * Create a map from each node in graphB to a corresponding node from graphA, or to null if there is no
	 * corresponding node from graphA.
	 *
	 * @return The new map.
	 */
	public @NotNull Map<N, Pair<N, GraphChange>> createNodeMapB() {
		final Map<N, Pair<N, GraphChange>> changeMapB = new HashMap<>();
		for (final Pair<N, N> pair : differences) {
			final N nodeB = pair.getTwo();
			if (nodeB != null) {
				final N nodeA = pair.getOne();
				final GraphChange change = nodeA == null
						? GraphChange.ADDED
						: pair instanceof Twin
						? GraphChange.UNCHANGED
						: GraphChange.CHANGED;
				changeMapB.put(nodeB, Tuples.pair(nodeA, change));
			}
		}
		return changeMapB;
	}


	/**
	 * Create a list from each node in graphB that doesn't have a corresponding node from graphA (the one that is marked
	 * with {@link GraphChange#ADDED}).
	 *
	 * @return The new list.
	 */
	public @NotNull List<N> createAddedList() {
		final List<N> addedList = new ArrayList<>();
		for (final Pair<N, N> pair : differences) {
			if (pair.getOne() == null) addedList.add(pair.getTwo());
		}
		return addedList;
	}

	/**
	 * Create a list from each node in graphA that have a corresponding node to from graphB, regardless of whether they
	 * are marked with {@link GraphChange#CHANGED} or {@link GraphChange#UNCHANGED}.
	 *
	 * @return The new list.
	 */
	public @NotNull List<Triple<N, N, GraphChange>> createSimilarList() {
		final List<Triple<N, N, GraphChange>> similarList = new ArrayList<>();
		for (final Pair<N, N> pair : differences) {
			final N nodeA = pair.getOne();
			final N nodeB = pair.getTwo();
			if (nodeA != null && nodeB != null) {
				similarList.add(Tuples.triple(nodeA, nodeB, pair instanceof Twin
						? GraphChange.UNCHANGED
						: GraphChange.CHANGED));
			}
		}
		return similarList;
	}

	/**
	 * Create a list from each node in graphA that have a corresponding node to from graphB and are marked with {@link
	 * GraphChange#CHANGED}.
	 *
	 * @return The new list.
	 */
	public @NotNull List<Pair<N, N>> createChangedList() {
		final List<Pair<N, N>> changedList = new ArrayList<>();
		for (final Pair<N, N> pair : differences) {
			if (pair.getOne() != null && pair.getTwo() != null && !(pair instanceof Twin)) changedList.add(pair);
		}
		return changedList;
	}

	/**
	 * Create a list from each node in graphA that have a corresponding node to from graphB and are marked with {@link
	 * GraphChange#UNCHANGED}.
	 *
	 * @return The new list.
	 */
	public @NotNull List<Pair<N, N>> createUnchangedList() {
		final List<Pair<N, N>> unchangedList = new ArrayList<>();
		for (final Pair<N, N> pair : differences) {
			if (pair.getOne() != null && pair.getTwo() != null && pair instanceof Twin) unchangedList.add(pair);
		}
		return unchangedList;
	}

	/**
	 * Create a list from each node in graphA that doesn't have a corresponding node from graphB (the one that is marked
	 * with {@link GraphChange#REMOVED}).
	 *
	 * @return The new list.
	 */
	public @NotNull List<N> createRemovedList() {
		final List<N> removedList = new ArrayList<>();
		for (final Pair<N, N> pair : differences) {
			if (pair.getTwo() == null) removedList.add(pair.getOne());
		}
		return removedList;
	}


	@Override
	public int size() {
		return differences.size();
	}

	@Override
	public @NotNull Iterator<Triple<N, N, GraphChange>> iterator() {
		return new Iterator<>() {
			private final @NotNull Iterator<Pair<N, N>> iterator = differences.iterator();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public @NotNull Triple<N, N, GraphChange> next() {
				final Pair<N, N> pair = iterator.next();
				final N one = pair.getOne();
				final N two = pair.getTwo();
				final GraphChange change = one == null
						? GraphChange.ADDED
						: two == null
						? GraphChange.REMOVED
						: pair instanceof Twin
						? GraphChange.UNCHANGED
						: GraphChange.CHANGED;
				return Tuples.triple(one, two, change);
			}
		};
	}

	@Override
	public @NotNull Spliterator<Triple<N, N, GraphChange>> spliterator() {
		return Spliterators.spliterator(iterator(), size(),
				Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL);
	}

	@Override
	public boolean add(@Nullable Triple<N, N, GraphChange> triple) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(@Nullable Object object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends Triple<N, N, GraphChange>> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeIf(@NotNull Predicate<? super Triple<N, N, GraphChange>> filter) {
		throw new UnsupportedOperationException();
	}
}
