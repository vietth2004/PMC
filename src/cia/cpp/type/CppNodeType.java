package cia.cpp.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cia.cpp.node.CppNode;
import cia.struct.graph.GraphException;
import cia.struct.graph.GraphReader;
import cia.struct.graph.GraphWriter;
import cia.struct.graph.Node;
import cia.struct.graph.NodeDiffer;

import java.io.IOException;

import static cia.struct.graph.GraphReader.expectNullableArray;

/**
 * Class Type / Enum Type / Struct Type / Union Type
 */
public final class CppNodeType extends CppType {
	private @Nullable CppNode node;


	public CppNodeType(@NotNull CppTypeGroup group) {
		super(group);
	}


	@Override
	protected @NotNull String getNodeClass() {
		return CppTypeGroup.CPP_NODE_TYPE;
	}


	public @Nullable CppNode getNode() {
		return node;
	}

	public void setNode(@Nullable CppNode node) {
		this.node = node;
	}


	@Override
	protected void serialize(@NotNull GraphWriter writer) throws GraphException, IOException {
		super.serialize(writer);

		if (node != null) {
			writer.name("node");
			writer.writeLink(node);
		}
	}

	@Override
	protected void deserialize(@NotNull GraphReader reader, @NotNull JsonObject nodeObject) throws GraphException {
		super.deserialize(reader, nodeObject);

		final JsonArray nodeArray = expectNullableArray(nodeObject.get("node"), "node");
		if (nodeArray != null) {
			reader.readLinkAndSetLater(CppNode.class, this::setNode, nodeArray);
		}
	}


	@MustBeInvokedByOverriders
	@Override
	protected boolean isIdentical(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isIdentical(differ, node)) return false;
		final CppNodeType other = (CppNodeType) node;
		return differ.isSimilar(this.node, other.node);
	}
}
