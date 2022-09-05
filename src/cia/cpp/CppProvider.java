package cia.cpp;

import org.jetbrains.annotations.NotNull;
import cia.cpp.node.CppGroup;
import cia.cpp.type.CppTypeGroup;
import cia.struct.graph.Connection;
import cia.struct.graph.Graph;
import cia.struct.graph.GraphException;
import cia.struct.graph.Group;
import cia.struct.graph.Provider;

import java.util.Set;

public enum CppProvider implements Provider {
	INSTANCE;

	private static final @NotNull Set<String> GROUPS = Set.of(CppGroup.CPP_GROUP, CppTypeGroup.CPP_TYPE_GROUP);
	private static final @NotNull Set<String> CONNECTIONS
			= Set.of(CppConnection.VALUES.stream().map(Connection::getName).toArray(String[]::new));


	@Override
	public @NotNull Group<?> newGroup(@NotNull Graph graph, @NotNull String groupClass) throws GraphException {
		switch (groupClass) {
			case CppGroup.CPP_GROUP:
				return new CppGroup(graph);
			case CppTypeGroup.CPP_TYPE_GROUP:
				return new CppTypeGroup(graph);
		}
		throw new GraphException("Cannot create group: Unknown group class!");
	}

	@Override
	public @NotNull Set<String> getProvidedGroups() {
		return GROUPS;
	}

	@Override
	public @NotNull Connection newConnection(@NotNull String connectionClass) throws GraphException {
		for (final CppConnection connection : CppConnection.VALUES) {
			if (connectionClass.equals(connection.getName())) {
				return connection;
			}
		}
		throw new GraphException("Cannot create connection: Unknown connection class!");
	}

	@Override
	public @NotNull Set<String> getProvidedConnections() {
		return CONNECTIONS;
	}
}
