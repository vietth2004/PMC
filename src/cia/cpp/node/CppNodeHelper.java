package cia.cpp.node;

import org.jetbrains.annotations.NotNull;
import cia.cpp.type.CppFunctionType;
import cia.cpp.type.CppType;
import cia.struct.graph.Node;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.stream.Collectors;

public final class CppNodeHelper {
	private static final @NotNull Set<Class<? extends CppNode>> CONTAINER_NODE_TYPES = Set.of(
			CppClassNode.class,
			CppEnumerationNode.class,
			CppNamespaceNode.class,
			CppStructNode.class,
			CppUnionNode.class
	);

	private CppNodeHelper() {
	}


	public static @NotNull String readableName(@NotNull CppNode node) {
		if (node instanceof CppFunctionNode) {
			return readableName((CppFunctionNode) node).trim().replaceAll("\\s+", " ");
		} else if (node instanceof CppVariableNode) {
			return readableName((CppVariableNode) node).trim().replaceAll("\\s+", " ");
		} else {
			return qualifiedName(node);
		}
	}

	public static @NotNull String readableName(@NotNull CppFunctionNode node) {
		final CppFunctionType type = node.getType();
		if (type == null) return qualifiedName(node);
		final StringBuilder builder = new StringBuilder();
		final CppType returnType = type.getReturnType();
		if (returnType != null) builder.append(returnType.getName()).append(' ');
		builder.append(qualifiedName(node)).append('(').append(
				type.getParameters().stream().map(CppType::getName).collect(Collectors.joining(","))
		).append(')');
		return builder.toString();
	}

	public static @NotNull String readableName(@NotNull CppVariableNode node) {
		final CppType type = node.getType();
		return type != null ? type.getName() + ' ' + qualifiedName(node) : qualifiedName(node);
	}

	public static @NotNull String qualifiedName(@NotNull CppNode node) {
		final Deque<CppNode> nodes = new ArrayDeque<>();
		CppNode current = node;
		while (true) {
			final Node parent = current.getParent();
			if (parent instanceof CppNode && CONTAINER_NODE_TYPES.contains(parent.getClass())) {
				current = (CppNode) parent;
				nodes.push(current);
			} else {
				break;
			}
		}
		final StringBuilder builder = new StringBuilder();
		while (true) {
			final CppNode cppNode = nodes.pollFirst();
			if (cppNode == null) break;
			builder.append(cppNode.getName()).append("::");
		}
		builder.append(node.getName());
		return builder.toString();
	}
}
