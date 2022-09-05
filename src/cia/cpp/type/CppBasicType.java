package cia.cpp.type;

import org.jetbrains.annotations.NotNull;

/**
 * Basic type
 */
public final class CppBasicType extends CppType {
	public CppBasicType(@NotNull CppTypeGroup group) {
		super(group);
	}

	@Override
	protected @NotNull String getNodeClass() {
		return CppTypeGroup.CPP_BASIC_TYPE;
	}
}
