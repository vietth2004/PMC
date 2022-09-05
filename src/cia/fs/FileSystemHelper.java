package cia.fs;

import org.jetbrains.annotations.NotNull;
import cia.struct.graph.Node;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;

public final class FileSystemHelper {
	private FileSystemHelper() {
	}

	// TODO: result is wrong when name is null, needs fix
	public static @NotNull String absolutePath(@NotNull FileSystemNode node) {
		if (node instanceof RootNode) return "";
		final Deque<FileSystemNode> nodes = new ArrayDeque<>();
		while (true) {
			nodes.push(node);
			final Node parent = node.getParent();
			if (parent instanceof FileSystemNode && !(parent instanceof RootNode)) {
				node = (FileSystemNode) parent;
			} else {
				break;
			}
		}
		return nodes.stream().map(FileSystemNode::getName).collect(Collectors.joining("/"));
	}
}
