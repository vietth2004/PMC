package cia.struct.graph;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Provider {
	@NotNull Group<?> newGroup(@NotNull Graph graph, @NotNull String groupClass) throws GraphException;

	@NotNull Set<String> getProvidedGroups();

	@NotNull Connection newConnection(@NotNull String connectionClass) throws GraphException;

	@NotNull Set<String> getProvidedConnections();
}
