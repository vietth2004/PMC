package cia.fs;

import org.jetbrains.annotations.NotNull;

public final class FileNode extends FileSystemNode {
	public FileNode(@NotNull FileSystemGroup group) {
		super(group);
	}

	@Override
	protected @NotNull String getNodeClass() {
		return FileSystemGroup.FILE_NODE;
	}
}
