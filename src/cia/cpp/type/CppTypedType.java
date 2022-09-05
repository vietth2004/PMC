package cia.cpp.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cia.struct.graph.GraphException;
import cia.struct.graph.GraphReader;
import cia.struct.graph.GraphWriter;
import cia.struct.graph.Node;
import cia.struct.graph.NodeDiffer;
import cia.struct.graph.NodeHasher;

import java.io.IOException;

import static cia.struct.graph.GraphReader.expectNullableArray;

/**
 * Used for array type, pointer type, reference type
 */
public final class CppTypedType extends CppType {
	private @Nullable CppType type;


	public CppTypedType(@NotNull CppTypeGroup group) {
		super(group);
	}


	@Override
	protected @NotNull String getNodeClass() {
		return CppTypeGroup.CPP_TYPED_TYPE;
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
	protected int hash(@NotNull NodeHasher hasher) {
		int hash = super.hash(hasher);
		hash = hash * 31 + hasher.hash(type);
		return hash;
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isSimilar(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isSimilar(differ, node)) return false;
		final CppTypedType other = (CppTypedType) node;
		return differ.isSimilar(type, other.type);
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isIdentical(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isIdentical(differ, node)) return false;
		final CppTypedType other = (CppTypedType) node;
		return differ.isSimilar(type, other.type);
	}
}
