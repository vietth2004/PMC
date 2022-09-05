package cia.struct.graph;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static cia.struct.graph.GraphReader.expectArray;
import static cia.struct.graph.GraphReader.expectNullableArray;
import static cia.struct.graph.GraphReader.expectObject;

public abstract class Node {
	private @Nullable Node parent = null;
	private final @NotNull Set<Node> children = new LinkedHashSet<>();

	private final @NotNull Map<Node, Connections> inboundConnections = new LinkedHashMap<>();
	private final @NotNull Map<Node, Connections> outboundConnections = new LinkedHashMap<>();


	protected Node(@NotNull Group<?> group) {
		group.newNode(this);
	}

	protected abstract @NotNull String getNodeClass();


	public final @Nullable Node getParent() {
		return parent;
	}

	public final @NotNull Set<Node> getChildren() {
		return Collections.unmodifiableSet(children);
	}

	public final @NotNull Map<Node, Connections> getInboundConnections() {
		return Collections.unmodifiableMap(inboundConnections);
	}

	public final @NotNull Map<Node, Connections> getOutboundConnections() {
		return Collections.unmodifiableMap(outboundConnections);
	}

	public final @Nullable Connections getConnectionsFrom(@NotNull Node node) {
		return inboundConnections.get(node);
	}

	public final @Nullable Connections getConnectionsTo(@NotNull Node node) {
		return outboundConnections.get(node);
	}


	public final void setParent(@NotNull Node parent) throws GraphException {
		if (this.parent != null) throw new GraphException("Parent already exist!");
		this.parent = parent;
		parent.children.add(this);
	}

	public final void addChild(@NotNull Node child) throws GraphException {
		child.setParent(this);
	}

	public final void addConnectionFrom(@NotNull Node from, @NotNull Connection type) {
		final Connections connections = inboundConnections.get(from);
		if (connections != null) {
			connections.addCount(type, 1);
		} else {
			final Connections newConnections = new Connections();
			newConnections.addCount(type, 1);
			inboundConnections.put(from, newConnections);
			from.outboundConnections.put(this, newConnections);
		}
	}

	public final void addConnectionTo(@NotNull Node to, @NotNull Connection type) {
		to.addConnectionFrom(this, type);
	}

	public final void addConnectionsFrom(@NotNull Node from, @NotNull Connections connections) {
		final Connections currentConnections = inboundConnections.get(from);
		assert from.outboundConnections.get(this) == currentConnections;
		if (currentConnections != null) {
			for (final Connection connection : connections) {
				currentConnections.addCount(connection, connections.getCount(connection));
			}
		} else {
			final Connections newConnections = new Connections(connections);
			inboundConnections.put(from, newConnections);
			from.outboundConnections.put(this, newConnections);
		}
	}

	public final void addConnectionsTo(@NotNull Node to, @NotNull Connections connections) {
		to.addConnectionsFrom(this, connections);
	}

	public final void setConnectionsFrom(@NotNull Node from, @NotNull Connections connections) {
		final Connections newConnections = new Connections(connections);
		inboundConnections.put(from, newConnections);
		from.outboundConnections.put(this, newConnections);
	}

	public final void setConnectionsTo(@NotNull Node to, @NotNull Connections connections) {
		to.setConnectionsFrom(this, connections);
	}


	/**
	 * The only {@code Node} that equal to this {@code Node} is this {@code Node} itself. This makes all collections of
	 * {@code Node}s effectively become identity collections.
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
		graph.getGroup(this); // implicit check node exist in graph
		if (parent != null && parent.getChildren().contains(this)) {
			throw new GraphException("Parent of this node does not have this node as a child!");
		}
		for (final Node child : children) {
			if (child.parent == null) throw new GraphException("Child node has empty parent!");
			if (child.parent != this) throw new GraphException("Parent of this child node is not this node!");
		}
		for (final Map.Entry<Node, Connections> entry : inboundConnections.entrySet()) {
			if (entry.getValue() != entry.getKey().getConnectionsTo(this)) {
				throw new GraphException("Connection from a node that does not connect to this node!");
			}
		}
		for (final Map.Entry<Node, Connections> entry : outboundConnections.entrySet()) {
			if (entry.getValue() != entry.getKey().getConnectionsFrom(this)) {
				throw new GraphException("Connection to a node that doest not have connection from this node!");
			}
		}
	}

	@MustBeInvokedByOverriders
	protected void serialize(@NotNull GraphWriter writer) throws GraphException, IOException {
		if (!children.isEmpty()) {
			writer.name("children");
			writer.beginArray();
			for (final Node child : children) {
				writer.writeLink(child);
			}
			writer.endArray();
		}

		if (!outboundConnections.isEmpty()) {
			writer.name("outboundConnections");
			writer.beginArray();
			for (final Map.Entry<Node, Connections> entry : outboundConnections.entrySet()) {
				writer.beginArray();
				writer.writeLink(entry.getKey());
				writer.writeConnections(entry.getValue());
				writer.endArray();
			}
			writer.endArray();
		}
	}

	@MustBeInvokedByOverriders
	protected void deserialize(@NotNull GraphReader reader, @NotNull JsonObject nodeObject) throws GraphException {
		final JsonArray childrenArray = expectNullableArray(nodeObject.get("children"), "children");
		if (childrenArray != null) {
			for (final JsonElement childElement : childrenArray) {
				final JsonArray childArray = expectArray(childElement, "child");
				reader.readLinkAndSetLater(Node.class, this::addChild, childArray);
			}
		}

		final JsonArray connectionsMapArray = expectNullableArray(nodeObject.get("connectionsMap"), "connectionsMap");
		if (connectionsMapArray != null) {
			for (final JsonElement entryElement : connectionsMapArray) {
				final JsonArray entryArray = expectArray(entryElement, "entry");
				if (entryArray.size() != 2) {
					throw new GraphException("Invalid json: connections entry should have only two elements!");
				}
				final JsonArray nodeArray = expectArray(entryArray.get(0), "node");
				final JsonObject connectionsObject = expectObject(entryArray.get(1), "connections");

				final Connections connections = reader.readConnections(connectionsObject);
				reader.readLinkAndSetLater(Node.class, node -> setConnectionsTo(node, connections), nodeArray);
			}
		}
	}


	/**
	 * If two node are similar then they have the same hash.
	 */
	@MustBeInvokedByOverriders
	protected int hash(@NotNull NodeHasher hasher) {
		int hashCode = hasher.hash(parent); // must have the similar parent
		hashCode = hashCode * 31 + getClass().hashCode(); // must be the same type
		return hashCode;
	}

	/**
	 * If two node are identical then they are also similar. If two node are similar then they can be identical or not
	 * identical.
	 */
	@MustBeInvokedByOverriders
	protected boolean isSimilar(@NotNull NodeDiffer differ, @NotNull Node node) {
		return getClass() == node.getClass()
				&& differ.isSimilar(parent, node.getParent());
	}

	/**
	 * If two node are identical then they are also similar. If two node are similar then they can be identical or not
	 * identical.
	 */
	@MustBeInvokedByOverriders
	protected boolean isIdentical(@NotNull NodeDiffer differ, @NotNull Node node) {
		return getClass() == node.getClass()
				&& differ.isSimilar(parent, node.getParent());
	}
}
