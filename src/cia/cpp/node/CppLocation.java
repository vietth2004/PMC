package cia.cpp.node;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import cia.fs.FileNode;
import cia.struct.graph.GraphException;
import cia.struct.graph.GraphReader;
import cia.struct.graph.GraphWriter;

import java.io.IOException;

import static cia.struct.graph.GraphReader.expectArray;
import static cia.struct.graph.GraphReader.expectInt;
import static cia.struct.graph.GraphReader.expectString;

public final class CppLocation {
	private @Nullable FileNode file;
	private int startLine;
	private int endLine;
	private @Nullable String content;


	public @Nullable FileNode getFile() {
		return file;
	}

	public void setFile(@Nullable FileNode file) {
		this.file = file;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public @Nullable String getContent() {
		return content;
	}

	public void setContent(@Nullable String content) {
		this.content = content;
	}


	void serialize(@NotNull GraphWriter writer) throws GraphException, IOException {
		writer.beginObject();
		if (file != null) {
			writer.name("file");
			writer.writeLink(file);
		}

		writer.name("lines");
		writer.beginArray();
		writer.value(startLine);
		writer.value(endLine);
		writer.endArray();

		if (content != null) {
			writer.name("content");
			writer.value(content);
		}
		writer.endObject();
	}

	void deserialize(@NotNull GraphReader reader, @NotNull JsonObject nodeObject) throws GraphException {
		final JsonArray fileArray = expectArray(nodeObject.get("file"), "file");
		reader.readLinkAndSetLater(FileNode.class, this::setFile, fileArray);
		final JsonArray linesArray = expectArray(nodeObject.get("lines"), "lines");
		if (linesArray.size() != 2) {
			throw new GraphException("Invalid json: lines should have only two elements!");
		}
		this.startLine = expectInt(linesArray.get(0), "startLine");
		this.endLine = expectInt(linesArray.get(1), "endLine");
		this.content = expectString(nodeObject.get("content"), "content");
	}
}
