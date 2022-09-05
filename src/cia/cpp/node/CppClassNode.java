package cia.cpp.node;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import cia.cpp.type.CppType;
import cia.struct.graph.GraphException;
import cia.struct.graph.GraphReader;
import cia.struct.graph.GraphWriter;
import cia.struct.graph.Node;
import cia.struct.graph.NodeDiffer;
import cia.struct.graph.NodeHasher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cia.struct.graph.GraphReader.expectArray;
import static cia.struct.graph.GraphReader.expectNullableArray;
import static cia.struct.graph.GraphReader.expectNullableBoolean;

/**
 * Class
 */
public final class CppClassNode extends CppNode {
	private final @NotNull List<CppType> bases = new ArrayList<>();
	private boolean anonymous = false;


	public CppClassNode(@NotNull CppGroup group) {
		super(group);
	}

	@Override
	protected @NotNull String getNodeClass() {
		return CppGroup.CPP_CLASS_NODE;
	}


	public @NotNull List<CppType> getBases() {
		return Collections.unmodifiableList(bases);
	}

	public void setBases(@NotNull List<CppType> bases) {
		this.bases.clear();
		this.bases.addAll(bases);
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

		if (!bases.isEmpty()) {
			writer.name("bases");
			writer.beginArray();
			for (final CppType type : bases) {
				writer.writeLink(type);
			}
			writer.endArray();
		}

		if (anonymous) {
			writer.name("anonymous");
			writer.value(true);
		}
	}

	@Override
	protected void deserialize(@NotNull GraphReader reader, @NotNull JsonObject nodeObject) throws GraphException {
		super.deserialize(reader, nodeObject);

		final JsonArray basesArray = expectNullableArray(nodeObject.get("bases"), "bases");
		if (basesArray != null) {
			for (final JsonElement baseElement : basesArray) {
				final JsonArray baseArray = expectArray(baseElement, "base");
				reader.readLinkAndSetLater(CppType.class, bases::add, baseArray);
			}
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
		final CppClassNode other = (CppClassNode) node;
		return anonymous == other.anonymous
				&& (!anonymous || differ.similarUnordered(getChildren(), other.getChildren()));
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isIdentical(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isIdentical(differ, node)) return false;
		final CppClassNode other = (CppClassNode) node;
		return anonymous == other.anonymous
				&& differ.similarUnordered(bases, other.bases)
				&& (!anonymous || differ.similarUnordered(getChildren(), other.getChildren()));
	}
}
