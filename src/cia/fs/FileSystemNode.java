package cia.fs;

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
import java.util.Objects;

import static cia.struct.graph.GraphReader.expectNullableString;

public abstract class FileSystemNode extends Node {
	private @Nullable String name = null;


	FileSystemNode(@NotNull FileSystemGroup group) {
		super(group);
	}


	public final @Nullable String getName() {
		return name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}


	@Override
	protected void serialize(@NotNull GraphWriter writer) throws GraphException, IOException {
		super.serialize(writer);

		if (name != null) {
			writer.name("name");
			writer.value(name);
		}
	}

	@Override
	protected void deserialize(@NotNull GraphReader reader, @NotNull JsonObject nodeObject) throws GraphException {
		super.deserialize(reader, nodeObject);

		this.name = expectNullableString(nodeObject.get("name"), "name");
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
		final FileSystemNode other = (FileSystemNode) node;
		return Objects.equals(name, other.name);
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isIdentical(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isIdentical(differ, node)) return false;
		final FileSystemNode other = (FileSystemNode) node;
		return Objects.equals(name, other.name);
	}
}
