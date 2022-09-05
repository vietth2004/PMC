package cia.struct.graph;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum GraphChange {
	ADDED,
	CHANGED,
	UNCHANGED,
	REMOVED;

	public static final @NotNull List<@NotNull GraphChange> values = List.of(values());
}
