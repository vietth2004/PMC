package cia.cpp.node;

import org.jetbrains.annotations.NotNull;
import cia.struct.graph.Graph;
import cia.struct.graph.GraphException;
import cia.struct.graph.Group;

/**
 * Cpp Group
 */
public final class CppGroup extends Group<CppNode> {
	public static final @NotNull String CPP_GROUP = "Cpp";
	public static final @NotNull String CPP_CLASS_NODE = "Class";
	public static final @NotNull String CPP_ENUMERATION_NODE = "Enum";
	public static final @NotNull String CPP_FUNCTION_NODE = "Function";
	public static final @NotNull String CPP_NAMESPACE_NODE = "Namespace";
	public static final @NotNull String CPP_ROOT_NODE = "Root";
	public static final @NotNull String CPP_STRUCT_NODE = "Struct";
	public static final @NotNull String CPP_TYPEDEF_NODE = "Typedef";
	public static final @NotNull String CPP_UNION_NODE = "Union";
	public static final @NotNull String CPP_VARIABLE_NODE = "Variable";


	public CppGroup(@NotNull Graph graph) {
		super(graph);
	}


	@Override
	protected @NotNull String getGroupClass() {
		return CPP_GROUP;
	}

	@Override
	protected @NotNull Class<CppNode> getNodeClass() {
		return CppNode.class;
	}

	@Override
	protected @NotNull CppNode newNode(@NotNull String nodeClass) throws GraphException {
		switch (nodeClass) {
			case CPP_CLASS_NODE:
				return new CppClassNode(this);
			case CPP_ENUMERATION_NODE:
				return new CppEnumerationNode(this);
			case CPP_FUNCTION_NODE:
				return new CppFunctionNode(this);
			case CPP_NAMESPACE_NODE:
				return new CppNamespaceNode(this);
			case CPP_ROOT_NODE:
				return new CppRootNode(this);
			case CPP_STRUCT_NODE:
				return new CppStructNode(this);
			case CPP_TYPEDEF_NODE:
				return new CppTypedefNode(this);
			case CPP_UNION_NODE:
				return new CppUnionNode(this);
			case CPP_VARIABLE_NODE:
				return new CppVariableNode(this);
		}
		throw new GraphException("Cannot create node: Unknown node class!");
	}
}
