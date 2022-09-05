package cia.fs;

import org.jetbrains.annotations.NotNull;
import cia.struct.graph.Connection;
import cia.struct.graph.Graph;
import cia.struct.graph.GraphException;
import cia.struct.graph.Group;
import cia.struct.graph.Provider;

import java.util.Set;

public enum FileSystemProvider implements Provider {
	INSTANCE;

	private static final @NotNull Set<String> GROUPS = Set.of(FileSystemGroup.FILE_SYSTEM_GROUP);

	@Override
	public final @NotNull Group<?> newGroup(@NotNull Graph graph, @NotNull String groupClass) throws GraphException {
		if (groupClass.equals(FileSystemGroup.FILE_SYSTEM_GROUP)) {
			return new FileSystemGroup(graph);
		}
		throw new GraphException("Cannot create group: Unknown group class!");
	}

	@Override
	public final @NotNull Set<String> getProvidedGroups() {
		return GROUPS;
	}

	@Override
	public final @NotNull Connection newConnection(@NotNull String connectionClass) throws GraphException {
		throw new GraphException("Cannot create connection: File System does not have any connection class!");
	}

	@Override
	public final @NotNull Set<String> getProvidedConnections() {
		return Set.of();
	}
}
