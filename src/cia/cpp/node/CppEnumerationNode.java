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

import static cia.struct.graph.GraphReader.expectNullableArray;
import static cia.struct.graph.GraphReader.expectNullableBoolean;

/**
 * Enumeration
 */
public final class CppEnumerationNode extends CppNode {
	private @Nullable CppType type = null;
	private boolean anonymous = false;


	public CppEnumerationNode(@NotNull CppGroup group) {
		super(group);
	}

	@Override
	protected @NotNull String getNodeClass() {
		return CppGroup.CPP_ENUMERATION_NODE;
	}


	public @Nullable CppType getType() {
		return type;
	}

	public void setType(@Nullable CppType type) {
		this.type = type;
	}

	public boolean isAnonymous() {
		return anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}


	@Override
	protected void serialize(@NotNull GraphWriter writer) throws GraphException, IOException {
		super.serialize(writer);

		if (type != null) {
			writer.name("type");
			writer.writeLink(type);
		}

		if (anonymous) {
			writer.name("anonymous");
			writer.value(true);
		}
	}

	@Override
	protected void deserialize(@NotNull GraphReader reader, @NotNull JsonObject nodeObject) throws GraphException {
		super.deserialize(reader, nodeObject);

		final JsonArray typeArray = expectNullableArray(nodeObject.get("type"), "type");
		if (typeArray != null) {
			reader.readLinkAndSetLater(CppType.class, this::setType, typeArray);
		}

		this.anonymous = expectNullableBoolean(nodeObject.get("anonymous"), "anonymous", false);
	}


	@MustBeInvokedByOverriders
	@Override
	protected int hash(@NotNull NodeHasher hasher) {
		int hash = super.hash(hasher);
		hash = hash * 31 + Boolean.hashCode(anonymous);
		hash = hash * 31 + (anonymous ? hasher.hashUnordered(getChildren()) : 1);
		return hash;
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isSimilar(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isSimilar(differ, node)) return false;
		final CppEnumerationNode other = (CppEnumerationNode) node;
		return anonymous == other.anonymous
				&& (!anonymous || differ.similarUnordered(getChildren(), other.getChildren()));
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isIdentical(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isIdentical(differ, node)) return false;
		final CppEnumerationNode other = (CppEnumerationNode) node;
		return anonymous == other.anonymous
				&& differ.isIdentical(type, other.type)
				&& (!anonymous || differ.similarUnordered(getChildren(), other.getChildren()));
	}
}
