package cia.cpp.node;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cia.cpp.type.CppType;
import cia.struct.graph.GraphException;
import cia.struct.graph.GraphReader;
import cia.struct.graph.GraphWriter;
import cia.struct.graph.Node;
import cia.struct.graph.NodeDiffer;

import java.io.IOException;

import static cia.struct.graph.GraphReader.expectNullableArray;

/**
 * Typedef
 */
public final class CppTypedefNode extends CppNode {
	private @Nullable CppType type = null;


	public CppTypedefNode(@NotNull CppGroup group) {
		super(group);
	}

	@Override
	protected @NotNull String getNodeClass() {
		return CppGroup.CPP_TYPEDEF_NODE;
	}


	public @Nullable CppType getType() {
		return type;
	}

	public void setType(@Nullable CppType type) {
		this.type = type;
	}


	@Override
	protected void serialize(@NotNull GraphWriter writer) throws GraphException, IOException {
		super.serialize(writer);

		if (type != null) {
			writer.name("type");
			writer.writeLink(type);
		}
	}

	@Override
	protected void deserialize(@NotNull GraphReader reader, @NotNull JsonObject nodeObject) throws GraphException {
		super.deserialize(reader, nodeObject);

		final JsonArray typeArray = expectNullableArray(nodeObject.get("type"), "type");
		if (typeArray != null) {
			reader.readLinkAndSetLater(CppType.class, this::setType, typeArray);
		}
	}


	@MustBeInvokedByOverriders
	@Override
	protected boolean isIdentical(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isIdentical(differ, node)) return false;
		final CppTypedefNode other = (CppTypedefNode) node;
		return differ.isIdentical(type, other.type);
	}
}
