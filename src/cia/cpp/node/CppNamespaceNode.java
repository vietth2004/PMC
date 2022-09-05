package cia.cpp.node;

import org.jetbrains.annotations.NotNull;

/**
 * Namespace
 */
public final class CppNamespaceNode extends CppNode {
	public CppNamespaceNode(@NotNull CppGroup group) {
		super(group);
	}

	@Override
	protected @NotNull String getNodeClass() {
		return CppGroup.CPP_NAMESPACE_NODE;
	}
}
