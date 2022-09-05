package cia.struct.graph;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static cia.struct.graph.GraphReader.expectNullableArray;
import static cia.struct.graph.GraphReader.expectObject;

public abstract class Group<E extends Node> {
	private final @NotNull Set<E> nodes = new LinkedHashSet<>();


	protected Group(@NotNull Graph graph) {
		graph.newGroup(this);
	}

	protected abstract @NotNull String getGroupClass();


	protected abstract @NotNull Class<E> getNodeClass();

	protected abstract @NotNull E newNode(@NotNull String nodeClass) throws GraphException;

	final void newNode(@NotNull Node node) {
		final Class<E> nodeClass = getNodeClass();
		if (!nodeClass.isInstance(node)) {
			throw new IllegalArgumentException("This node group cannot hold this type of node!");
		}
		nodes.add(nodeClass.cast(node));
	}


	public final @NotNull Set<E> getNodes() {
		return Collections.unmodifiableSet(nodes);
	}

	public final boolean containsNode(@NotNull Node node) {
		//noinspection SuspiciousMethodCalls
		return nodes.contains(node);
	}


	/**
	 * The only {@code NodeGroup} that equal to this {@code NodeGroup} is this {@code NodeGroup} itself. This makes all
	 * collections of {@code NodeGroup}s effectively become identity collections.
	 *
	 * @param object object
	 * @return {@code this == object}
	 */
	@Override
	public final boolean equals(@Nullable Object object) {
		return this == object;
	}

	@Override
	public final int hashCode() {
		return super.hashCode();
	}


	@MustBeInvokedByOverriders
	protected void validate(@NotNull Graph graph) throws GraphException {
		for (final E node : nodes) node.validate(graph);
	}

	@MustBeInvokedByOverriders
	protected void serialize(@NotNull GraphWriter writer) throws GraphException, IOException {
		if (!nodes.isEmpty()) {
			writer.name("nodes");
			writer.beginArray();
			for (final E node : nodes) {
				writer.writeNode(node);
			}
			writer.endArray();
		}
	}

	@MustBeInvokedByOverriders
	protected void deserialize(@NotNull GraphReader reader, @NotNull JsonObject group) throws GraphException {
		final JsonArray nodesArray = expectNullableArray(group.get("nodes"), "nodes");
		if (nodesArray != null) {
			for (final JsonElement nodeElement : nodesArray) {
				final JsonObject nodeObject = expectObject(nodeElement, "node");
				nodes.add(reader.readNode(this, nodeObject));
			}
		}
	}
}
