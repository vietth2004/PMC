package cia.struct.graph;

import org.jetbrains.annotations.NotNull;

public class GraphException extends Exception {
	public GraphException(@NotNull String message) {
		super(message);
	}

	public GraphException(@NotNull String message, @NotNull Throwable cause) {
		super(message, cause);
	}
}
