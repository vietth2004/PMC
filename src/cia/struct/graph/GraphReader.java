package cia.struct.graph;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cia.struct.utils.ThrowingConsumer;
import cia.struct.utils.ThrowingRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GraphReader implements AutoCloseable {
	private final @NotNull Graph graph;
	private final @NotNull List<Provider> providers;

	private final @NotNull MutableIntObjectMap<Group<?>> groups = new IntObjectHashMap<>();
	private final @NotNull Map<Group<?>, MutableIntObjectMap<Node>> groupNodes = new HashMap<>();
	private final @NotNull List<ThrowingRunnable<GraphException>> linkSetters = new ArrayList<>();


	GraphReader(@NotNull Graph graph, @NotNull List<Provider> providers) {
		this.graph = graph;
		this.providers = List.copyOf(providers);
	}


	private static <A, B> @NotNull IntObjectHashMap<A> createIntObjectMap(@Nullable B any) {
		return new IntObjectHashMap<>();
	}


	public static @NotNull JsonObject expectObject(@Nullable JsonElement element, @NotNull String name)
			throws GraphException {
		if (element instanceof JsonObject) return (JsonObject) element;
		throw new GraphException("Invalid json: \"" + name + "\" should be an object!");
	}

	public static @Nullable JsonObject expectNullableObject(@Nullable JsonElement element, @NotNull String name)
			throws GraphException {
		if (element == null || element instanceof JsonObject) return (JsonObject) element;
		throw new GraphException("Invalid json: \"" + name + "\" should be an object!");
	}

	public static @NotNull JsonArray expectArray(@Nullable JsonElement element, @NotNull String name)
			throws GraphException {
		if (element instanceof JsonArray) return (JsonArray) element;
		throw new GraphException("Invalid json: \"" + name + "\" should be an array!");
	}


	public static @Nullable JsonArray expectNullableArray(@Nullable JsonElement element, @NotNull String name)
			throws GraphException {
		if (element == null || element instanceof JsonArray) return (JsonArray) element;
		throw new GraphException("Invalid json: \"" + name + "\" should be an array!");
	}

	public static @NotNull JsonPrimitive expectPrimitive(@Nullable JsonElement element, @NotNull String name)
			throws GraphException {
		if (element instanceof JsonPrimitive) return (JsonPrimitive) element;
		throw new GraphException("Invalid json: \"" + name + "\" should be a primitive!");
	}

	public static @Nullable JsonPrimitive expectNullablePrimitive(@Nullable JsonElement element, @NotNull String name)
			throws GraphException {
		if (element == null || element instanceof JsonPrimitive) return (JsonPrimitive) element;
		throw new GraphException("Invalid json: \"" + name + "\" should be a primitive!");
	}

	public static @NotNull String expectString(@Nullable JsonElement element, @NotNull String name)
			throws GraphException {
		final JsonPrimitive primitive = expectPrimitive(element, name);
		if (primitive.isString()) return primitive.getAsString();
		throw new GraphException("Invalid json: \"" + name + "\" should be a string!");
	}

	public static @Nullable String expectNullableString(@Nullable JsonElement element, @NotNull String name)
			throws GraphException {
		final JsonPrimitive primitive = expectNullablePrimitive(element, name);
		if (primitive == null) return null;
		if (primitive.isString()) return primitive.getAsString();
		throw new GraphException("Invalid json: \"" + name + "\" should be a string!");
	}

	public static @NotNull Number expectNumber(@Nullable JsonElement element, @NotNull String name)
			throws GraphException {
		final JsonPrimitive primitive = expectPrimitive(element, name);
		if (primitive.isNumber()) return primitive.getAsNumber();
		throw new GraphException("Invalid json: \"" + name + "\" should be a number!");
	}

	public static @Nullable Number expectNullableNumber(@Nullable JsonElement element, @NotNull String name)
			throws GraphException {
		final JsonPrimitive primitive = expectNullablePrimitive(element, name);
		if (primitive == null) return null;
		if (primitive.isNumber()) return primitive.getAsNumber();
		throw new GraphException("Invalid json: \"" + name + "\" should be a number!");
	}

	public static int expectInt(@Nullable JsonElement element, @NotNull String name) throws GraphException {
		final Number number = expectNumber(element, name);
		try {
			return number.intValue();
		} catch (final NumberFormatException exception) {
			throw new GraphException("Invalid json: \"" + name + "\" should be an int!", exception);
		}
	}

	public static int expectNullableInt(@Nullable JsonElement element, @NotNull String name, int defaultValue)
			throws GraphException {
		final Number number = expectNullableNumber(element, name);
		if (number == null) return defaultValue;
		try {
			return number.intValue();
		} catch (final NumberFormatException exception) {
			throw new GraphException("Invalid json: \"" + name + "\" should be an int!", exception);
		}
	}

	public static boolean expectBoolean(@Nullable JsonElement element, @NotNull String name) throws GraphException {
		final JsonPrimitive primitive = expectPrimitive(element, name);
		if (primitive.isBoolean()) return primitive.getAsBoolean();
		throw new GraphException("Invalid json: \"" + name + "\" should be a boolean!");
	}

	public static boolean expectNullableBoolean(@Nullable JsonElement element, @NotNull String name,
			boolean defaultValue) throws GraphException {
		final JsonPrimitive primitive = expectNullablePrimitive(element, name);
		if (primitive == null) return defaultValue;
		if (primitive.isBoolean()) return primitive.getAsBoolean();
		throw new GraphException("Invalid json: \"" + name + "\" should be a boolean!");
	}


	private @NotNull Provider findProviderForGroupClass(@NotNull String className) throws GraphException {
		for (final Provider provider : providers) {
			if (provider.getProvidedGroups().contains(className)) return provider;
		}
		throw new GraphException("Invalid json: Unknown group class!");
	}

	@NotNull Group<?> readGroup(@NotNull JsonObject groupObject) throws GraphException {
		final String className = expectString(groupObject.get("class"), "class");
		final Provider provider = findProviderForGroupClass(className);
		final Group<?> group = provider.newGroup(graph, className);
		if (!graph.containsGroup(group)) throw new GraphException("The new group does not exist in this graph!");
		final int id = expectInt(groupObject.get("id"), "id");
		if (groups.put(id, group) != null) throw new GraphException("Invalid json: duplicate group id!");
		group.deserialize(this, groupObject);
		return group;
	}

	<E extends Node> @NotNull E readNode(@NotNull Group<E> group, @NotNull JsonObject nodeObject)
			throws GraphException {
		if (!graph.containsGroup(group)) throw new GraphException("This group does not exist in this graph!");
		final MutableIntObjectMap<Node> nodes = groupNodes.computeIfAbsent(group, GraphReader::createIntObjectMap);
		final String className = expectString(nodeObject.get("class"), "class");
		final E node = group.newNode(className);
		final int id = expectInt(nodeObject.get("id"), "id");
		if (nodes.put(id, node) != null) throw new GraphException("Invalid json: duplicate node id!");
		node.deserialize(this, nodeObject);
		return node;
	}


	public <E extends Node> void readLinkAndSetLater(@NotNull Class<E> nodeClass,
			@NotNull ThrowingConsumer<E, GraphException> nodeSetter, @NotNull JsonArray linkArray)
			throws GraphException {
		if (linkArray.size() != 2) throw new GraphException("Invalid json: link should have only two elements!");

		final int groupId = expectInt(linkArray.get(0), "groupId");
		final int nodeId = expectInt(linkArray.get(1), "nodeId");

		linkSetters.add(() -> resolveLinkAndSet(nodeClass, nodeSetter, groupId, nodeId));
	}

	private <E extends Node> void resolveLinkAndSet(@NotNull Class<E> nodeClass,
			@NotNull ThrowingConsumer<E, GraphException> nodeSetter, int groupId, int nodeId) throws GraphException {
		final Group<?> group = groups.get(groupId);
		if (group == null) throw new GraphException("Invalid json: Cannot find group with this group id!");
		final MutableIntObjectMap<Node> nodes = groupNodes.computeIfAbsent(group, GraphReader::createIntObjectMap);
		final Node node = nodes.get(nodeId);
		if (node == null) throw new GraphException("Invalid json: Cannot find node with this node id!");
		if (!nodeClass.isInstance(node)) throw new GraphException("Invalid json: Unexpected node type!");
		nodeSetter.consume(nodeClass.cast(node));
	}


	private @NotNull Provider findProviderForConnectionClass(@NotNull String className) throws GraphException {
		for (final Provider provider : providers) {
			if (provider.getProvidedConnections().contains(className)) return provider;
		}
		throw new GraphException("Invalid json: Unknown connection class!");
	}

	public @NotNull Connections readConnections(@NotNull JsonObject connectionsObject) throws GraphException {
		final Connections connections = new Connections();
		for (final Map.Entry<String, JsonElement> entry : connectionsObject.entrySet()) {
			final String connectionClass = entry.getKey();
			final Provider provider = findProviderForConnectionClass(connectionClass);
			final Connection connection = provider.newConnection(connectionClass);
			final int count = expectInt(entry.getValue(), "count");
			connections.setCount(connection, count);
		}
		return connections;
	}


	@Override
	public void close() throws GraphException {
		try {
			for (final ThrowingRunnable<GraphException> linkSetter : linkSetters) {
				linkSetter.run();
			}
		} finally {
			groups.clear();
			groupNodes.clear();
			linkSetters.clear();
		}
	}
}
