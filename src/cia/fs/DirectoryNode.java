package cia.fs;

import org.jetbrains.annotations.NotNull;

public final class DirectoryNode extends FileSystemNode {
	public DirectoryNode(@NotNull FileSystemGroup group) {
		super(group);
	}

	@Override
	protected @NotNull String getNodeClass() {
		return FileSystemGroup.DIRECTORY_NODE;
	}
}
