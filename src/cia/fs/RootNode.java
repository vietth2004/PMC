package cia.fs;

import org.jetbrains.annotations.NotNull;

public final class RootNode extends FileSystemNode {
	public RootNode(@NotNull FileSystemGroup group) {
		super(group);
	}

	@Override
	protected @NotNull String getNodeClass() {
		return FileSystemGroup.ROOT_NODE;
	}
}
