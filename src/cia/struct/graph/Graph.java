package cia.struct.graph;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static cia.struct.graph.GraphReader.expectNullableArray;
import static cia.struct.graph.GraphReader.expectObject;

public final class Graph {
	private final @NotNull Set<Group<?>> groups = new LinkedHashSet<>();


	void newGroup(@NotNull Group<?> group) {
		groups.add(group);
	}

	public @NotNull Set<Group<?>> getGroups() {
		return Collections.unmodifiableSet(groups);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean containsGroup(@NotNull Group<?> group) {
		return groups.contains(group);
	}

	public @NotNull Group<?> getGroup(@NotNull Node node) throws GraphException {
		for (final Group<?> group : groups) {
			if (group.containsNode(node)) return group;
		}
		throw new GraphException("This group does not exist in this graph!");
	}

	public <E extends Group<?>> @NotNull E getGroup(@NotNull Class<E> groupClass) throws GraphException {
		for (final Group<?> group : groups) {
			if (groupClass.isInstance(group)) return groupClass.cast(group);
		}
		throw new GraphException("The specified group does not exist!");
	}

	public <E extends Group<?>> @NotNull E getGroup(@NotNull Node node, @NotNull Class<E> groupClass)
			throws GraphException {
		for (final Group<?> group : groups) {
			if (group.containsNode(node)) {
				if (groupClass.isInstance(group)) return groupClass.cast(group);
				throw new GraphException("The specified group does not match the specified type!");
			}
		}
		throw new GraphException("This group does not exist in this graph!");
	}


	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean containsNode(@NotNull Node node) {
		for (final Group<?> group : groups) {
			if (group.containsNode(node)) return true;
		}
		return false;
	}


	public void validate() throws GraphException {
		for (final Group<?> group : groups) group.validate(this);
	}

	void serialize(@NotNull Writer outputWriter) throws GraphException {
		try (final GraphWriter writer = new GraphWriter(this, outputWriter)) {
			writer.beginObject();

			if (!groups.isEmpty()) {
				writer.name("groups");
				writer.beginArray();
				for (final Group<?> group : groups) {
					writer.writeGroup(group);
				}
				writer.endArray();
			}

			writer.endObject();
		} catch (final IOException exception) {
			throw new GraphException("Exception while serialize graph!", exception);
		}
	}

	static @NotNull Graph deserialize(@NotNull Reader inputReader, @NotNull List<Provider> providers)
			throws GraphException {
		final Graph graph = new Graph();
		try (final GraphReader reader = new GraphReader(graph, providers)) {
			final JsonObject graphObject = expectObject(JsonParser.parseReader(inputReader), "graph");

			final JsonArray groupsArray = expectNullableArray(graphObject.get("groups"), "groups");
			if (groupsArray != null) {
				for (final JsonElement groupElement : groupsArray) {
					final JsonObject groupObject = expectObject(groupElement, "group");
					graph.groups.add(reader.readGroup(groupObject));
				}
			}

		} catch (final JsonParseException exception) {
			throw new GraphException("Exception while deserialize graph json!", exception);
		}
		return graph;
	}
}
