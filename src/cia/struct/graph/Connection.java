package cia.struct.graph;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public interface Connection extends Serializable {
	@NotNull String getName();
}
