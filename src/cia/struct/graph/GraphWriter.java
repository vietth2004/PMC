package cia.struct.graph;

import com.google.gson.stream.JsonWriter;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

public final class GraphWriter extends JsonWriter {
	private final @NotNull Graph graph;
	private final @NotNull MutableObjectIntMap<Group<?>> groupIds = new ObjectIntHashMap<>();
	private final @NotNull MutableObjectIntMap<Node> nodeIds = new ObjectIntHashMap<>();

	GraphWriter(@NotNull Graph graph, @NotNull Writer writer) {
		super(writer);
		this.graph = graph;
		for (final Group<?> group : graph.getGroups()) {
			groupIds.put(group, groupIds.size()); // make every group have unique id
			int count = 0;
			for (final Node node : group.getNodes()) {
				nodeIds.put(node, count++); // make every node in a group have unique id
			}
		}
	}

	void writeNode(@NotNull Node node) throws GraphException, IOException {
		beginObject();
		name("class");
		value(node.getNodeClass());
		name("id");
		writeNodeId(node);
		node.serialize(this);
		endObject();
	}

	void writeGroup(@NotNull Group<?> group) throws GraphException, IOException {
		beginObject();
		name("class");
		value(group.getGroupClass());
		name("id");
		writeGroupId(group);
		group.serialize(this);
		endObject();
	}

	private void writeNodeId(@NotNull Node node) throws GraphException, IOException {
		final int nodeId = nodeIds.getIfAbsent(node, -1);
		if (nodeId < 0) throw new GraphException("Node not found!");
		value(nodeId);
	}

	private void writeGroupId(@NotNull Group<?> group) throws GraphException, IOException {
		final int groupId = groupIds.getIfAbsent(group, -1);
		if (groupId < 0) throw new GraphException("Group not found!");
		value(groupId);
	}

	public void writeLink(@NotNull Node node) throws GraphException, IOException {
		final Group<?> group = graph.getGroup(node);
		beginArray();
		writeGroupId(group);
		writeNodeId(node);
		endArray();
	}

	void writeConnections(@NotNull Connections connections) throws IOException {
		beginObject();
		for (final Connection connection : connections) {
			name(connection.getName());
			value(connections.getCount(connection));
		}
		endObject();
	}
}
