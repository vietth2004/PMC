package cia.fs;

import org.jetbrains.annotations.NotNull;
import cia.struct.graph.Graph;
import cia.struct.graph.GraphException;
import cia.struct.graph.Group;

public final class FileSystemGroup extends Group<FileSystemNode> {
	public static final @NotNull String FILE_SYSTEM_GROUP = "FileSystem";
	public static final @NotNull String DIRECTORY_NODE = "Directory";
	public static final @NotNull String FILE_NODE = "File";
	public static final @NotNull String ROOT_NODE = "Root";


	public FileSystemGroup(@NotNull Graph graph) {
		super(graph);
	}


	@Override
	protected @NotNull String getGroupClass() {
		return FILE_SYSTEM_GROUP;
	}

	@Override
	protected @NotNull Class<FileSystemNode> getNodeClass() {
		return FileSystemNode.class;
	}

	@Override
	protected @NotNull FileSystemNode newNode(@NotNull String nodeClass) throws GraphException {
		switch (nodeClass) {
			case DIRECTORY_NODE:
				return new DirectoryNode(this);
			case FILE_NODE:
				return new FileNode(this);
			case ROOT_NODE:
				return new RootNode(this);
		}
		throw new GraphException("Cannot create node: Unknown node class!");
	}
}
