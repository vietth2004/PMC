package cia.cpp.type;

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
import java.util.Collections;
import java.util.List;

import static cia.struct.graph.GraphReader.expectArray;
import static cia.struct.graph.GraphReader.expectBoolean;
import static cia.struct.graph.GraphReader.expectNullableArray;

/**
 * Function Type
 */
public final class CppFunctionType extends CppType {
	private @Nullable CppType returnType = null;
	private final @NotNull List<CppType> parameters = new ArrayList<>();
	private boolean isVarArgs = false;


	public CppFunctionType(@NotNull CppTypeGroup group) {
		super(group);
	}


	@Override
	protected @NotNull String getNodeClass() {
		return CppTypeGroup.CPP_FUNCTION_TYPE;
	}


	public @Nullable CppType getReturnType() {
		return returnType;
	}

	public void setReturnType(@Nullable CppType returnType) {
		this.returnType = returnType;
	}


	public @NotNull List<CppType> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	public void setParameters(@NotNull List<CppType> parameters) {
		this.parameters.clear();
		this.parameters.addAll(parameters);
	}


	public boolean isVarArgs() {
		return isVarArgs;
	}

	public void setVarArgs(boolean varArgs) {
		isVarArgs = varArgs;
	}


	@Override
	protected void serialize(@NotNull GraphWriter writer) throws GraphException, IOException {
		super.serialize(writer);

		if (returnType != null) {
			writer.name("returnType");
			writer.writeLink(returnType);
		}

		if (!parameters.isEmpty()) {
			writer.name("parameters");
			writer.beginArray();
			for (final CppType parameter : parameters) {
				writer.writeLink(parameter);
			}
			writer.endArray();
		}

		writer.name("varArgs");
		writer.value(isVarArgs);
	}

	@Override
	protected void deserialize(@NotNull GraphReader reader, @NotNull JsonObject nodeObject) throws GraphException {
		super.deserialize(reader, nodeObject);

		final JsonArray returnTypeArray = expectNullableArray(nodeObject.get("returnType"), "returnType");
		if (returnTypeArray != null) {
			reader.readLinkAndSetLater(CppType.class, this::setReturnType, returnTypeArray);
		}

		final JsonArray parametersArray = expectNullableArray(nodeObject.get("parameters"), "parameters");
		if (parametersArray != null) {
			for (final JsonElement parameterElement : parametersArray) {
				final JsonArray parameterArray = expectArray(parameterElement, "parameter");
				reader.readLinkAndSetLater(CppType.class, parameters::add, parameterArray);
			}
		}

		this.isVarArgs = expectBoolean(nodeObject.get("varArgs"), "varArgs");
	}


	@MustBeInvokedByOverriders
	@Override
	protected int hash(@NotNull NodeHasher hasher) {
		int hash = super.hash(hasher);
		hash = hash * 31 + hasher.hash(returnType);
		hash = hash * 31 + hasher.hashOrdered(parameters);
		return hash;
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isSimilar(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isSimilar(differ, node)) return false;
		final CppFunctionType other = (CppFunctionType) node;
		return differ.isSimilar(returnType, other.returnType)
				&& differ.similarOrdered(parameters, other.parameters);
	}

	@MustBeInvokedByOverriders
	@Override
	protected boolean isIdentical(@NotNull NodeDiffer differ, @NotNull Node node) {
		if (!super.isIdentical(differ, node)) return false;
		final CppFunctionType other = (CppFunctionType) node;
		return differ.isSimilar(returnType, other.returnType)
				&& differ.similarOrdered(parameters, other.parameters);
	}
}
