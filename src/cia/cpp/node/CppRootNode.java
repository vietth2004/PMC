package cia.cpp.node;

import org.jetbrains.annotations.NotNull;

/**
 * Root
 */
public final class CppRootNode extends CppNode {
	public CppRootNode(@NotNull CppGroup group) {
		super(group);
	}

	@Override
	protected @NotNull String getNodeClass() {
		return CppGroup.CPP_ROOT_NODE;
	}
}
