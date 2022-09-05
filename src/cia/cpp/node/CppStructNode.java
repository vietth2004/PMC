package cia.cpp.node;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import cia.struct.graph.GraphException;
import cia.struct.graph.GraphReader;
import cia.struct.graph.GraphWriter;
import cia.struct.graph.Node;
import cia.struct.graph.NodeDiffer;
import cia.struct.graph.NodeHasher;

import java.io.IOException;

import static cia.struct.graph.GraphReader.expectNullableBoolean;

/**
 * Struct
 */
public final class CppStructNode extends CppNode {
	private boolean anonymous = false;


	public CppStructNode(@NotNull CppGroup group) {
		super(group);
	}

	@Override
	protected @NotNull String getNodeClass() {
		return CppGroup.CPP_STRUCT_NODE;
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

		if (anonymous) {
			writer.name("anonymous");
			writer.value(true);
		}
	}

	@Override
	protected void deserialize(@NotNull GraphReader reader, @NotNull JsonObject nodeObject) throws GraphException {
		super.deserialize(reader, nodeObject);

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
		final CppStructNode other = (CppStructNode) node;
		return anonymous == other.anonymous
				&& (!anonymous || differ.similarUnordered(getChildren(), other.getChildren()));
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isIdentical(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isIdentical(differ, node)) return false;
		final CppStructNode other = (CppStructNode) node;
		return anonymous == other.anonymous
				&& (!anonymous || differ.similarUnordered(getChildren(), other.getChildren()));
	}
}
