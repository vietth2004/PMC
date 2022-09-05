package cia.struct.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;

public final class AppendableWriter extends Writer {
	private final @NotNull Appendable appendable;

	public AppendableWriter(@NotNull Appendable appendable) {
		this.appendable = appendable;
	}

	@Override
	public void write(int c) throws IOException {
		appendable.append((char) c);
	}

	@Override
	public void write(char @NotNull [] chars) throws IOException {
		appendable.append(CharBuffer.wrap(chars));
	}

	@Override
	public void write(@NotNull String string) throws IOException {
		appendable.append(string);
	}

	@Override
	public void write(@NotNull String string, int offset, int length) throws IOException {
		appendable.append(string, offset, length);
	}

	@Override
	public @NotNull AppendableWriter append(@NotNull CharSequence charSequence) throws IOException {
		appendable.append(charSequence);
		return this;
	}

	@Override
	public @NotNull AppendableWriter append(@NotNull CharSequence charSequence, int start, int end) throws IOException {
		appendable.append(charSequence, start, end);
		return this;
	}

	@Override
	public @NotNull AppendableWriter append(char c) throws IOException {
		appendable.append(c);
		return this;
	}

	@Override
	public void write(char @NotNull [] chars, int offset, int length) throws IOException {
		appendable.append(CharBuffer.wrap(chars, offset, length));
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}
}
