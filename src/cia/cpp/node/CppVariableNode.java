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
import cia.struct.graph.NodeHasher;

import java.io.IOException;
import java.util.Objects;

import static cia.struct.graph.GraphReader.expectNullableArray;
import static cia.struct.graph.GraphReader.expectNullableString;

/**
 * Variable / Field / Template Parameter / Enumerator
 */
public final class CppVariableNode extends CppNode {
	private @Nullable CppType type = null;
	private @Nullable String value = null;


	public CppVariableNode(@NotNull CppGroup group) {
		super(group);
	}

	@Override
	protected @NotNull String getNodeClass() {
		return CppGroup.CPP_VARIABLE_NODE;
	}


	public @Nullable CppType getType() {
		return type;
	}

	public void setType(@Nullable CppType type) {
		this.type = type;
	}


	public @Nullable String getValue() {
		return value;
	}

	public void setValue(@Nullable String value) {
		this.value = value;
	}


	@Override
	protected void serialize(@NotNull GraphWriter writer) throws GraphException, IOException {
		super.serialize(writer);

		if (type != null) {
			writer.name("type");
			writer.writeLink(type);
		}
		if (value != null) {
			writer.name("value");
			writer.value(value);
		}
	}

	@Override
	protected void deserialize(@NotNull GraphReader reader, @NotNull JsonObject nodeObject) throws GraphException {
		super.deserialize(reader, nodeObject);

		final JsonArray typeArray = expectNullableArray(nodeObject.get("type"), "type");
		if (typeArray != null) {
			reader.readLinkAndSetLater(CppType.class, this::setType, typeArray);
		}

		this.value = expectNullableString(nodeObject.get("value"), "value");
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
		final CppVariableNode other = (CppVariableNode) node;
		return differ.isSimilar(type, other.type);
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isIdentical(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isIdentical(differ, node)) return false;
		final CppVariableNode other = (CppVariableNode) node;
		return Objects.equals(value, other.value)
				&& differ.isSimilar(type, other.type);
	}
}
