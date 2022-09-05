package cia.cpp.type;

import org.jetbrains.annotations.NotNull;
import cia.struct.graph.Graph;
import cia.struct.graph.GraphException;
import cia.struct.graph.Group;

/**
 * Cpp Type Group
 */
public final class CppTypeGroup extends Group<CppType> {
	public static final @NotNull String CPP_TYPE_GROUP = "CppType";
	public static final @NotNull String CPP_BASIC_TYPE = "BasicType";
	public static final @NotNull String CPP_FUNCTION_TYPE = "FunctionType";
	public static final @NotNull String CPP_NODE_TYPE = "NodeType";
	public static final @NotNull String CPP_TYPED_TYPE = "TypedType";


	public CppTypeGroup(@NotNull Graph graph) {
		super(graph);
	}


	@Override
	protected @NotNull String getGroupClass() {
		return CPP_TYPE_GROUP;
	}

	@Override
	protected @NotNull Class<CppType> getNodeClass() {
		return CppType.class;
	}

	@Override
	protected @NotNull CppType newNode(@NotNull String nodeClass) throws GraphException {
		switch (nodeClass) {
			case CPP_BASIC_TYPE:
				return new CppBasicType(this);
			case CPP_FUNCTION_TYPE:
				return new CppFunctionType(this);
			case CPP_NODE_TYPE:
				return new CppNodeType(this);
			case CPP_TYPED_TYPE:
				return new CppTypedType(this);
		}
		throw new GraphException("Cannot create node: Unknown node class!");
	}
}
