package cia.cpp.node;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cia.struct.graph.GraphReader.expectNullableArray;
import static cia.struct.graph.GraphReader.expectNullableString;
import static cia.struct.graph.GraphReader.expectObject;

public abstract class CppNode extends Node {
	private @Nullable String name = null;
	private final @NotNull List<CppLocation> locations = new ArrayList<>();


	CppNode(@NotNull CppGroup group) {
		super(group);
	}


	public final @Nullable String getName() {
		return name;
	}

	public final void setName(@Nullable String name) {
		this.name = name;
	}

	public final @NotNull List<CppLocation> getLocations() {
		return locations;
	}

	public final void setLocations(@NotNull List<CppLocation> locations) {
		this.locations.clear();
		this.locations.addAll(locations);
	}


	@Override
	protected void serialize(@NotNull GraphWriter writer) throws GraphException, IOException {
		super.serialize(writer);

		if (name != null) {
			writer.name("name");
			writer.value(name);
		}

		if (!locations.isEmpty()) {
			writer.name("locations");
			writer.beginArray();
			for (final CppLocation location : locations) {
				location.serialize(writer);
			}
			writer.endArray();
		}
	}

	@Override
	protected void deserialize(@NotNull GraphReader reader, @NotNull JsonObject nodeObject) throws GraphException {
		super.deserialize(reader, nodeObject);

		this.name = expectNullableString(nodeObject.get("name"), "name");

		final JsonArray locationsArray = expectNullableArray(nodeObject.get("locations"), "locations");
		if (locationsArray != null) {
			for (final JsonElement locationElement : locationsArray) {
				final JsonObject locationObject = expectObject(locationElement, "location");
				final CppLocation location = new CppLocation();
				location.deserialize(reader, locationObject);
				locations.add(location);
			}
		}
	}

	@MustBeInvokedByOverriders
	@Override
	protected int hash(@NotNull NodeHasher hasher) {
		int hash = super.hash(hasher);
		hash = hash * 31 + Objects.hashCode(name);
		return hash;
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isSimilar(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isSimilar(differ, node)) return false;
		final CppNode other = (CppNode) node;
		return Objects.equals(name, other.name);
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isIdentical(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isIdentical(differ, node)) return false;
		final CppNode other = (CppNode) node;
		return Objects.equals(name, other.name);
	}
}
