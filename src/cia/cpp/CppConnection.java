package cia.cpp;

import org.jetbrains.annotations.NotNull;
import cia.struct.graph.Connection;

import java.util.List;

public enum CppConnection implements Connection {
	USE("C++ Use"),
	MEMBER("C++ Member"),
	INHERIT("C++ Inherit"),
	CALL("C++ Call"),
	OVERRIDE("C++ Override");


	public static final @NotNull List<CppConnection> VALUES = List.of(values());


	private final @NotNull String name;


	CppConnection(@NotNull String name) {
		this.name = name;
	}


	@Override
	public @NotNull String getName() {
		return name;
	}
}
