package cia.cpp;

import org.jetbrains.annotations.NotNull;

public class CppBuilderException extends Exception {
	public CppBuilderException(@NotNull String message) {
		super(message);
	}

	public CppBuilderException(@NotNull String message, @NotNull Throwable cause) {
		super(message, cause);
	}
}
