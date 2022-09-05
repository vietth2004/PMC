package cia.cpp.node;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cia.cpp.type.CppFunctionType;
import cia.struct.graph.GraphException;
import cia.struct.graph.GraphReader;
import cia.struct.graph.GraphWriter;
import cia.struct.graph.Node;
import cia.struct.graph.NodeDiffer;
import cia.struct.graph.NodeHasher;

import java.io.IOException;
import java.util.Objects;

import static cia.struct.graph.GraphReader.expectNullableArray;
import static cia.struct.graph.GraphReader.expectNullableString;

/**
 * Function / Method / Constructor / Destructor
 */
public final class CppFunctionNode extends CppNode {
	private @Nullable CppFunctionType type = null;
	private @Nullable String body = null;


	public CppFunctionNode(@NotNull CppGroup group) {
		super(group);
	}

	@Override
	protected @NotNull String getNodeClass() {
		return CppGroup.CPP_FUNCTION_NODE;
	}


	public @Nullable CppFunctionType getType() {
		return type;
	}

	public void setType(@Nullable CppFunctionType type) {
		this.type = type;
	}


	public @Nullable String getBody() {
		return body;
	}

	public void setBody(@Nullable String body) {
		this.body = body;
	}


	@Override
	protected void serialize(@NotNull GraphWriter writer) throws GraphException, IOException {
		super.serialize(writer);

		if (type != null) {
			writer.name("type");
			writer.writeLink(type);
		}
		if (body != null) {
			writer.name("body");
			writer.value(body);
		}
	}

	@Override
	protected void deserialize(@NotNull GraphReader reader, @NotNull JsonObject nodeObject) throws GraphException {
		super.deserialize(reader, nodeObject);

		final JsonArray typeArray = expectNullableArray(nodeObject.get("type"), "type");
		if (typeArray != null) {
			reader.readLinkAndSetLater(CppFunctionType.class, this::setType, typeArray);
		}

		this.body = expectNullableString(nodeObject.get("body"), "body");
	}


	@MustBeInvokedByOverriders
	@Override
	protected int hash(@NotNull NodeHasher hasher) {
		int hash = super.hash(hasher);
		hash = hash * 31 + hasher.hash(type);
		return hash;
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isSimilar(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isSimilar(differ, node)) return false;
		final CppFunctionNode other = (CppFunctionNode) node;
		return differ.isSimilar(type, other.type);
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isIdentical(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isIdentical(differ, node)) return false;
		final CppFunctionNode other = (CppFunctionNode) node;
		return Objects.equals(body, other.body)
				&& differ.isSimilar(type, other.type);
	}
}
