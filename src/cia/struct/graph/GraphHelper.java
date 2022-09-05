package cia.struct.graph;

import org.jetbrains.annotations.NotNull;
import cia.struct.utils.AppendableWriter;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class GraphHelper {
	private GraphHelper() {
	}


	public static @NotNull Graph graphFromString(@NotNull String jsonString, @NotNull List<Provider> providers)
			throws GraphException {
		return Graph.deserialize(new StringReader(jsonString), providers);
	}

	/**
	 * This method will automatically close the input reader, even when an exception is thrown.
	 */
	public static @NotNull Graph graphFromReader(@NotNull Reader inputReader, @NotNull List<Provider> providers)
			throws GraphException {
		return Graph.deserialize(inputReader, providers);
	}

	/**
	 * This method will automatically close the input stream, even when an exception is thrown.
	 */
	public static @NotNull Graph graphFromInputStream(@NotNull InputStream inputStream,
			@NotNull List<Provider> providers) throws GraphException {
		try (final InputStreamReader inputReader = new InputStreamReader(inputStream)) {
			return Graph.deserialize(inputReader, providers);
		} catch (final IOException exception) {
			throw new GraphException("Exception while reading from input stream!", exception);
		}
	}

	public static @NotNull Graph graphFromFile(@NotNull Path inputFile, @NotNull List<Provider> providers)
			throws GraphException {
		try (final Reader inputReader = Files.newBufferedReader(inputFile)) {
			return Graph.deserialize(inputReader, providers);
		} catch (final IOException exception) {
			throw new GraphException("Exception while reading from file!", exception);
		}
	}


	public static @NotNull String graphToString(@NotNull Graph graph) throws GraphException {
		try (final CharArrayWriter outputWriter = new CharArrayWriter(4096)) {
			graph.serialize(outputWriter);
			return outputWriter.toString();
		}
	}

	public static void graphToAppendable(@NotNull Appendable appendable, @NotNull Graph graph) throws GraphException {
		try (final AppendableWriter outputWriter = new AppendableWriter(appendable)) {
			graph.serialize(outputWriter);
		}
	}

	/**
	 * This method will automatically close the output writer, even when an exception is thrown.
	 */
	public static void graphToWriter(@NotNull Writer outputWriter, @NotNull Graph graph) throws GraphException {
		graph.serialize(outputWriter);
	}

	/**
	 * This method will automatically close the output stream, even when an exception is thrown.
	 */
	public static void graphToOutputStream(@NotNull OutputStream outputStream, @NotNull Graph graph)
			throws GraphException {
		try (final Writer outputWriter = new OutputStreamWriter(outputStream)) {
			graph.serialize(outputWriter);
		} catch (final IOException exception) {
			throw new GraphException("Exception while writing to output stream!", exception);
		}
	}

	public static void graphToFile(@NotNull Path outputFile, @NotNull Graph graph) throws GraphException {
		try (final Writer outputWriter = Files.newBufferedWriter(outputFile)) {
			graph.serialize(outputWriter);
		} catch (final IOException exception) {
			throw new GraphException("Exception while writing to output stream!", exception);
		}
	}
}
