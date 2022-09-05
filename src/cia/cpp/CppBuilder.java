package cia.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationListOwner;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAliasDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStaticAssertDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriterVisitor;
import org.eclipse.cdt.internal.core.parser.IMacroDictionary;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.collections.api.multimap.list.MutableListMultimap;
import org.eclipse.collections.impl.multimap.list.FastListMultimap;
import org.eclipse.core.runtime.CoreException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cia.cpp.node.CppClassNode;
import cia.cpp.node.CppEnumerationNode;
import cia.cpp.node.CppFunctionNode;
import cia.cpp.node.CppGroup;
import cia.cpp.node.CppLocation;
import cia.cpp.node.CppNamespaceNode;
import cia.cpp.node.CppNode;
import cia.cpp.node.CppRootNode;
import cia.cpp.node.CppStructNode;
import cia.cpp.node.CppTypedefNode;
import cia.cpp.node.CppUnionNode;
import cia.cpp.node.CppVariableNode;
import cia.cpp.type.CppBasicType;
import cia.cpp.type.CppFunctionType;
import cia.cpp.type.CppNodeType;
import cia.cpp.type.CppType;
import cia.cpp.type.CppTypeGroup;
import cia.cpp.type.CppTypedType;
import cia.fs.DirectoryNode;
import cia.fs.FileNode;
import cia.fs.FileSystemGroup;
import cia.fs.FileSystemNode;
import cia.fs.RootNode;
import cia.struct.graph.Connections;
import cia.struct.graph.Graph;
import cia.struct.graph.GraphDiffer;
import cia.struct.graph.GraphException;
import cia.struct.graph.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CppBuilder {
	private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(CppBuilder.class);

	private static final @NotNull FileContent EMPTY_FILE = FileContent.create("", new char[]{});
	private static final @NotNull IncludeFileContentProvider CONTENT_PROVIDER = new ContentProvider();
	private static final @NotNull IParserLogService LOG_SERVICE = new NullLogService();

	private final @NotNull Path projectPath;
	private final @NotNull List<Path> projectFiles;
	private final @NotNull IASTTranslationUnit translationUnit;

	private final @NotNull Graph graph;
	private final @NotNull FileSystemGroup structureGroup;
	private final @NotNull CppGroup nodeGroup;
	private final @NotNull CppTypeGroup typeGroup;
	private final @NotNull CppRootNode rootNode;

	private final @NotNull Map<Path, FileNode> projectFileMap = new HashMap<>();
	private final @NotNull Map<IBinding, CppNode> nodeMap = new HashMap<>();
	private final @NotNull Map<TypeWrapper, CppType> typeMap = new HashMap<>();
	private final @NotNull Map<IBinding, Map<CppNode, Connections>> connectionsMap = new HashMap<>();


	CppBuilder(@NotNull Path projectPath, @NotNull List<Path> projectFiles,
			@NotNull IASTTranslationUnit translationUnit) {
		this.projectPath = projectPath;
		this.projectFiles = projectFiles;
		this.translationUnit = translationUnit;

		this.graph = new Graph();
		this.structureGroup = new FileSystemGroup(graph);
		this.nodeGroup = new CppGroup(graph);
		this.typeGroup = new CppTypeGroup(graph);
		this.rootNode = new CppRootNode(nodeGroup);
	}

	private void buildStructure() throws GraphException {
		final RootNode rootNode = new RootNode(structureGroup);
		final Map<Path, DirectoryNode> directoryMap = new HashMap<>();
		for (final Path projectFile : projectFiles) {
			final Path relativePath = projectPath.relativize(projectFile);
			final int nameCount = relativePath.getNameCount();
			FileSystemNode parentNode = rootNode;
			for (int i = 1; i < nameCount; i++) {
				final Path childPath = relativePath.subpath(0, i);
				final DirectoryNode createdDirectoryNode = directoryMap.get(childPath);
				if (createdDirectoryNode != null) {
					parentNode = createdDirectoryNode;
				} else {
					final DirectoryNode directoryNode = new DirectoryNode(structureGroup);
					directoryNode.setParent(parentNode);
					directoryNode.setName(childPath.getFileName().toString());
					directoryMap.put(childPath, directoryNode);
					parentNode = directoryNode;
				}
			}
			final FileNode fileNode = new FileNode(structureGroup);
			fileNode.setParent(parentNode);
			fileNode.setName(relativePath.getFileName().toString());
			projectFileMap.put(relativePath, fileNode);
		}
	}

	private void buildCpp() throws GraphException, CppBuilderException {
		buildDeclarations(rootNode, translationUnit);
	}

	private static <E> @NotNull List<E> filter(@NotNull Collection<? super E> collection, @NotNull Class<E> filter) {
		final List<E> result = new ArrayList<>(collection.size());
		for (final Object object : collection) {
			if (filter.isInstance(object)) {
				result.add(filter.cast(object));
			}
		}
		return result;
	}

	private @NotNull Graph finalizeCpp() throws GraphException {
		final GraphDiffer graphDiffer = new GraphDiffer(graph, graph);

		// class inherit
		for (final CppNode node : nodeMap.values()) {
			if (!(node instanceof CppClassNode)) continue;

			final CppClassNode classNode = (CppClassNode) node;
			final List<CppType> bases = classNode.getBases();
			if (bases.isEmpty()) continue;

			// filter for functions
			final List<CppFunctionNode> functions = filter(classNode.getChildren(), CppFunctionNode.class);
			final MutableListMultimap<String, CppFunctionNode> multimap = functions.isEmpty()
					? null
					: new FastListMultimap<>();

			for (final CppType base : bases) {
				if (!(base instanceof CppNodeType)) continue;
				final CppNode baseNode = ((CppNodeType) base).getNode();
				if (baseNode == null) continue;

				// inherit
				classNode.addConnectionTo(baseNode, CppConnection.INHERIT);

				if (functions.isEmpty() || !(baseNode instanceof CppClassNode)) continue;
				final CppClassNode baseClassNode = (CppClassNode) baseNode;

				final List<CppFunctionNode> baseFunctions = filter(baseClassNode.getChildren(), CppFunctionNode.class);
				if (baseFunctions.isEmpty()) continue;

				assert multimap != null;
				if (multimap.isEmpty()) {
					for (final CppFunctionNode function : functions) {
						multimap.put(function.getName(), function);
					}
				}

				for (final CppFunctionNode baseFunction : baseFunctions) {
					for (final CppFunctionNode function : multimap.get(baseFunction.getName())) {
						if (graphDiffer.isSimilar(function.getType(), baseFunction.getType())) {
							function.addConnectionFrom(baseFunction, CppConnection.OVERRIDE);
						}
					}
				}
			}
		}

		// connections map
		for (final Map.Entry<IBinding, Map<CppNode, Connections>> entry : connectionsMap.entrySet()) {
			final CppNode cppNode = nodeMap.get(entry.getKey());
			if (cppNode == null) continue;
			for (final Map.Entry<CppNode, Connections> innerEntry : entry.getValue().entrySet()) {
				cppNode.addConnectionsFrom(innerEntry.getKey(), innerEntry.getValue());
			}
		}

		// aided garbage collector a bit
		projectFileMap.clear();
		nodeMap.clear();
		typeMap.clear();
		connectionsMap.clear();

		return graph;
	}

	public static @NotNull Graph build(@NotNull Map<String, String> predefinedMacros,
			@NotNull List<Path> includePaths, @NotNull Path projectPath, @NotNull List<Path> projectSources,
			@NotNull List<Path> projectHeaders) throws IOException, CppBuilderException {
		LOGGER.info("Start building C++ project...");

		final List<Path> realIncludePaths = new ArrayList<>(includePaths.size());
		for (final Path includePath : includePaths) {
			if (Files.isReadable(includePath)) {
				realIncludePaths.add(includePath.toRealPath(LinkOption.NOFOLLOW_LINKS));
			} else {
				LOGGER.warn("Include path not exist! " + includePath);
			}
		}
		final Path realProjectPath = projectPath.toRealPath(LinkOption.NOFOLLOW_LINKS);

		final List<Path> realProjectFiles = new ArrayList<>();
		for (final Path projectHeader : projectHeaders) {
			if (Files.isReadable(projectHeader)) {
				realProjectFiles.add(projectHeader.toRealPath(LinkOption.NOFOLLOW_LINKS));
			} else {
				LOGGER.warn("Header file not exist! " + projectHeader);
			}
		}
		for (final Path projectSource : projectSources) {
			if (Files.isReadable(projectSource)) {
				realProjectFiles.add(projectSource.toRealPath(LinkOption.NOFOLLOW_LINKS));
			} else {
				LOGGER.warn("Source file not exist! " + projectSource);
			}
		}

		final Map<String, String> realPredefinedMacros = new HashMap<>(predefinedMacros);
		realPredefinedMacros.putIfAbsent("__DATE__", "\"??? ?? ????\"");
		realPredefinedMacros.putIfAbsent("__TIME__", "\"??:??:??\"");

		final IScannerInfo scannerInfo = new ExtendedScannerInfo(realPredefinedMacros,
				realIncludePaths.stream().map(Path::toString).toArray(String[]::new), null,
				realProjectFiles.stream().map(Path::toString).toArray(String[]::new));

		try {
			final IASTTranslationUnit translationUnit = GPPLanguage.getDefault()
					.getASTTranslationUnit(EMPTY_FILE, scannerInfo, CONTENT_PROVIDER, null,
							ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS
									| ILanguage.OPTION_NO_IMAGE_LOCATIONS, LOG_SERVICE);

			final CppBuilder builder = new CppBuilder(realProjectPath, realProjectFiles, translationUnit);

			builder.buildStructure();
			builder.buildCpp();

			return builder.finalizeCpp();

		} catch (final GraphException | CoreException exception) {
			LOGGER.error("Finish building C++ project with an exception!", exception);
			throw new CppBuilderException("Exception while building the project!", exception);
		} finally {
			LOGGER.info("Finish building C++ project.");
		}
	}


	private static final class ContentProvider extends InternalFileContentProvider {
		private @Nullable InternalFileContent fileContent(@NotNull String path) {
			if (!getInclusionExists(path)) return null;
			return (InternalFileContent) FileContent.createForExternalFileLocation(path);
		}

		@Override
		public @Nullable InternalFileContent getContentForInclusion(@NotNull String path,
				@Nullable IMacroDictionary dictionary) {
			return fileContent(path);
		}

		@Override
		public @Nullable InternalFileContent getContentForInclusion(@NotNull IIndexFileLocation location,
				@Nullable String astPath) {
			final String path = location.getFullPath();
			return path == null ? null : fileContent(path);
		}
	}

	//region Node Builder

	private <E extends IASTNode> @NotNull E findParentNode(@NotNull IASTNode astNode, @NotNull Class<E> astNodeClass)
			throws CppBuilderException {
		IASTNode node = astNode;
		while (!astNodeClass.isInstance(node)) {
			node = node.getParent();
			if (node == null) {
				LOGGER.error("Cannot find parent of AST node! astNode = " + astNode.getClass().getName());
				throw new CppBuilderException("Cannot find parent of AST node!");
			}
		}
		return astNodeClass.cast(node);
	}

	private <E extends IASTNode> @NotNull List<E> findParentsNode(@NotNull IASTNode @NotNull [] astNodes,
			@NotNull Class<E> astNodeClass) throws CppBuilderException {
		if (astNodes.length == 0) return List.of();
		final List<E> parentNodes = new ArrayList<>(astNodes.length);
		for (final IASTNode astNode : astNodes) {
			parentNodes.add(findParentNode(astNode, astNodeClass));
		}
		return parentNodes;
	}

	private @NotNull List<CppLocation> locateBinding(@NotNull List<? extends IASTNode> astNodes) {
		if (astNodes.size() == 0) return List.of();
		final List<CppLocation> locations = new ArrayList<>(astNodes.size());
		for (final IASTNode astNode : astNodes) {
			final IASTFileLocation fileLocation = astNode.getFileLocation();
			if (fileLocation == null) continue;
			try {
				final Path absolutePath = Path.of(fileLocation.getFileName()).toRealPath(LinkOption.NOFOLLOW_LINKS);
				final Path relativePath = projectPath.relativize(absolutePath);
				final FileNode file = projectFileMap.get(relativePath);
				if (file != null) {
					final CppLocation location = new CppLocation();
					location.setFile(file);
					location.setStartLine(fileLocation.getStartingLineNumber());
					location.setEndLine(fileLocation.getEndingLineNumber());

					final BodyBuilder builder = new BodyBuilder();
					astNode.accept(builder);
					location.setContent(builder.toString());

					locations.add(location);
				}
			} catch (final IOException exception) {
				LOGGER.info("Cannot find real path of file!", exception);
			}
		}
		return locations;
	}

	private static class BodyBuilder extends ASTWriterVisitor {
		@Override
		public int visit(@NotNull IASTDeclaration declaration) {
			if (declaration instanceof IASTProblemDeclaration) {
				scribe.print(declaration.getRawSignature());
				LOGGER.warn("Visitor meet problem declaration! declaration = " + declaration.getClass().getName());
				return ASTVisitor.PROCESS_SKIP;
			}
			return super.visit(declaration);
		}

		@Override
		public int visit(@NotNull IASTExpression expression) {
			if (expression instanceof IASTProblemExpression) {
				scribe.print(expression.getRawSignature());
				LOGGER.warn("Visitor meet problem expression! expression = " + expression.getClass().getName());
				return ASTVisitor.PROCESS_SKIP;
			}
			return super.visit(expression);
		}

		@Override
		public int visit(@NotNull IASTStatement statement) {
			if (statement instanceof IASTProblemStatement) {
				scribe.print(statement.getRawSignature());
				LOGGER.warn("Visitor meet problem statement! statement = " + statement.getClass().getName());
				return ASTVisitor.PROCESS_SKIP;
			}
			return super.visit(statement);
		}
	}


	private <E extends CppNode> @NotNull E initializeCppNode(@NotNull IBinding binding, @NotNull Node parent,
			@NotNull E node) throws GraphException, CppBuilderException {
		// parent
		node.setParent(parent);
		parent.addConnectionTo(node, CppConnection.MEMBER);
		// name
		final String name = binding.getName();
		node.setName(name.isEmpty()
				? "{anonymous}"
				: name.trim().replaceAll("\\s+", " "));
		// put node to map
		putNodeToNodeMap(binding, node);
		// set node for type
		if (binding instanceof IType) {
			final IType type = (IType) binding;
			setNodeForNodeType(type, node);
		}
		return node;
	}

	private @Nullable CppNode buildBinding(@NotNull Node parent, @NotNull IBinding binding)
			throws GraphException, CppBuilderException {
		if (binding instanceof IVariable) {
			// this includes ICPPVariable, ICPPField, ICPPParameter
			return buildVariable(parent, (IVariable) binding);

		} else if (binding instanceof IFunction) {
			// this includes ICPPFunction, ICPPMethod
			return buildFunction(parent, (IFunction) binding);

		} else if (binding instanceof IEnumeration) {
			// this includes ICPPEnumeration
			return buildEnumeration(parent, (IEnumeration) binding);

		} else if (binding instanceof IEnumerator) {
			// this should never happen
			return buildEnumerator(parent, (IEnumerator) binding);

		} else if (binding instanceof ITypedef) {
			return buildTypedef(parent, (ITypedef) binding);

		} else if (binding instanceof ICompositeType) {
			return buildComposite(parent, (ICompositeType) binding);

		} else if (binding instanceof ICPPNamespace) {
			return buildNamespace(parent, (ICPPNamespace) binding);

		} else if (binding instanceof ILabel || binding instanceof IIndexBinding
				|| binding instanceof IMacroBinding || binding instanceof IProblemBinding
				|| binding instanceof ICPPUnknownBinding || binding instanceof ICPPASTUsingDeclaration) {
			LOGGER.info("Skipped binding! binding = " + binding.getClass().getName());
			return null;

		} else {
			LOGGER.error("Unsupported binding! binding = " + binding.getClass().getName());
			throw new CppBuilderException("Unsupported binding!");
		}
	}

	private @NotNull CppVariableNode buildVariable(@NotNull Node parent, @NotNull IVariable binding)
			throws GraphException, CppBuilderException {
		final CppVariableNode foundNode = getNodeFromNodeMap(binding, CppVariableNode.class, parent);
		if (foundNode != null) {
			LOGGER.info("Skipped binding: Node already exist! binding = " + binding.getClass().getName());
			return foundNode;
		}

		final CppVariableNode node = initializeCppNode(binding, parent, new CppVariableNode(nodeGroup));

		// locations
		final List<IASTDeclarator> parentsNode
				= findParentsNode(translationUnit.getDefinitionsInAST(binding), IASTDeclarator.class);
		node.setLocations(locateBinding(parentsNode));

		// type
		node.setType(buildType(binding.getType()));

		// value
		for (final IASTDeclarator declarator : parentsNode) {
			final IASTInitializer initializer = declarator.getInitializer();
			if (initializer != null) {
				final ConnectionBuilder visitor = new ConnectionBuilder(node);
				initializer.accept(visitor);
				visitor.rethrowException();
				node.setValue(visitor.toString());
				break;
			}
		}

		return node;
	}

	private class ConnectionBuilder extends BodyBuilder {
		protected final @NotNull CppNode parent;
		protected @Nullable GraphException graphException;
		protected @Nullable CppBuilderException builderException;

		ConnectionBuilder(@NotNull CppNode parent) {
			this.parent = parent;
		}

		final void rethrowException() throws GraphException, CppBuilderException {
			if (graphException != null) throw graphException;
			if (builderException != null) throw builderException;
		}

		@Override
		public int visit(@NotNull IASTExpression expression) {
			try {
				if (expression instanceof IASTFunctionCallExpression) {
					final IASTFunctionCallExpression callExpression = (IASTFunctionCallExpression) expression;
					final IASTExpression nameExpression = callExpression.getFunctionNameExpression();
					if (nameExpression instanceof IASTIdExpression) {
						final IASTName name = ((IASTIdExpression) nameExpression).getName();
						final IBinding binding = checkBinding(name.resolveBinding());
						final Map<CppNode, Connections> map
								= connectionsMap.computeIfAbsent(binding, any -> new LinkedHashMap<>());
						final Connections connections = map.computeIfAbsent(parent, any -> new Connections());
						connections.addCount(CppConnection.CALL, 1);
					}
				}
				return super.visit(expression);
			} catch (final CppBuilderException exception) {
				this.builderException = exception;
				return ASTVisitor.PROCESS_ABORT;
			}
		}

		@Override
		public int visit(@NotNull IASTName name) {
			final IBinding binding = name.resolveBinding();
			if (binding != null) {
				final Map<CppNode, Connections> map
						= connectionsMap.computeIfAbsent(binding, any -> new LinkedHashMap<>());
				final Connections connections = map.computeIfAbsent(parent, any -> new Connections());
				connections.addCount(CppConnection.USE, 1);
			}
			return super.visit(name);
		}
	}

	private @NotNull CppFunctionNode buildFunction(@NotNull Node parent, @NotNull IFunction binding)
			throws GraphException, CppBuilderException {
		final CppFunctionNode foundNode = getNodeFromNodeMap(binding, CppFunctionNode.class, parent);
		if (foundNode != null) {
			LOGGER.info("Skipped binding: Node already exist! binding = " + binding.getClass().getName());
			return foundNode;
		}

		final CppFunctionNode node = initializeCppNode(binding, parent, new CppFunctionNode(nodeGroup));

		// locations
		final List<IASTFunctionDefinition> parentsNode
				= findParentsNode(translationUnit.getDefinitionsInAST(binding), IASTFunctionDefinition.class);
		node.setLocations(locateBinding(parentsNode));

		// type
		node.setType(buildFunctionType(binding.getType()));

		// function body
		for (final IASTFunctionDefinition definition : parentsNode) {
			final IASTStatement body = definition.getBody();
			if (body != null) {
				final FunctionBodyBuilder visitor = new FunctionBodyBuilder(node);
				body.accept(visitor);
				visitor.rethrowException();
				node.setBody(visitor.toString());
				break;
			}
		}

		return node;
	}

	private class FunctionBodyBuilder extends ConnectionBuilder {
		FunctionBodyBuilder(@NotNull CppFunctionNode parent) {
			super(parent);
		}

		@Override
		public int visit(@NotNull IASTDeclSpecifier specifier) {
			try {
				if (specifier instanceof IASTCompositeTypeSpecifier) {
					final IASTCompositeTypeSpecifier compositeSpecifier = (IASTCompositeTypeSpecifier) specifier;
					buildCompositeSpecifier(parent, compositeSpecifier);
					return super.visit(compositeSpecifier.getName());
				} else if (specifier instanceof IASTEnumerationSpecifier) {
					final IASTEnumerationSpecifier enumerationSpecifier = (IASTEnumerationSpecifier) specifier;
					buildEnumerationSpecifier(parent, enumerationSpecifier);
					return super.visit(enumerationSpecifier.getName());
				} else {
					return super.visit(specifier);
				}
			} catch (final GraphException exception) {
				this.graphException = exception;
				return ASTVisitor.PROCESS_ABORT;
			} catch (final CppBuilderException exception) {
				this.builderException = exception;
				return ASTVisitor.PROCESS_ABORT;
			}
		}
	}

	private @NotNull CppEnumerationNode buildEnumeration(@NotNull Node parent, @NotNull IEnumeration binding)
			throws GraphException, CppBuilderException {
		final CppEnumerationNode foundNode = getNodeFromNodeMap(binding, CppEnumerationNode.class, parent);
		if (foundNode != null) {
			LOGGER.info("Skipped binding: Node already exist! binding = " + binding.getClass().getName());
			return foundNode;
		}

		final CppEnumerationNode node = initializeCppNode(binding, parent, new CppEnumerationNode(nodeGroup));

		// anonymous
		node.setAnonymous(binding.getName().isEmpty());

		// locations
		node.setLocations(locateBinding(
				findParentsNode(translationUnit.getDefinitionsInAST(binding), IASTEnumerationSpecifier.class)));

		// type
		if (binding instanceof ICPPEnumeration) {
			final IType fixedType = ((ICPPEnumeration) binding).getFixedType();
			if (fixedType != null) {
				node.setType(buildType(fixedType));
			}
		}

		// children
		for (final IEnumerator enumerator : binding.getEnumerators()) {
			if (enumerator.getOwner() == binding) {
				buildEnumerator(node, enumerator);
			} else {
				LOGGER.info("Skipped enumerator: not in current scope! binding = " + binding.getClass().getName()
						+ "; enumerator = " + enumerator.getClass().getName());
			}
		}

		return node;
	}

	private @NotNull CppVariableNode buildEnumerator(@NotNull Node parent, @NotNull IEnumerator binding)
			throws GraphException, CppBuilderException {
		final CppVariableNode foundNode = getNodeFromNodeMap(binding, CppVariableNode.class, parent);
		if (foundNode != null) {
			LOGGER.info("Skipped binding: Node already exist! binding = " + binding.getClass().getName());
			return foundNode;
		}

		final CppVariableNode node = initializeCppNode(binding, parent, new CppVariableNode(nodeGroup));

		// locations
		final List<IASTEnumerationSpecifier.IASTEnumerator> parentsNode = findParentsNode(
				translationUnit.getDefinitionsInAST(binding), IASTEnumerationSpecifier.IASTEnumerator.class);
		node.setLocations(locateBinding(parentsNode));

		// type
		node.setType(buildType(binding.getType()));

		// value
		for (final IASTEnumerationSpecifier.IASTEnumerator enumerator : parentsNode) {
			final IASTExpression value = enumerator.getValue();
			if (value != null) {
				final ASTWriterVisitor visitor = new ASTWriterVisitor();
				value.accept(visitor);
				node.setValue(visitor.toString());
				break;
			}
		}

		return node;
	}

	private @NotNull CppTypedefNode buildTypedef(@NotNull Node parent, @NotNull ITypedef binding)
			throws GraphException, CppBuilderException {
		final CppTypedefNode foundNode = getNodeFromNodeMap(binding, CppTypedefNode.class, parent);
		if (foundNode != null) {
			LOGGER.info("Skipped binding: Node already exist! binding = " + binding.getClass().getName());
			return foundNode;
		}

		final CppTypedefNode node = initializeCppNode(binding, parent, new CppTypedefNode(nodeGroup));

		// locations
		node.setLocations(locateBinding(
				findParentsNode(translationUnit.getDefinitionsInAST(binding), IASTDeclaration.class)));

		// type
		node.setType(buildType(binding.getType()));

		return node;
	}

	private @NotNull CppNode buildComposite(@NotNull Node parent, @NotNull ICompositeType binding)
			throws GraphException, CppBuilderException {
		if (binding instanceof ICPPClassType) {
			return compositeCreateClass(parent, (ICPPClassType) binding);
		}

		final int key = binding.getKey();
		if (key == ICompositeType.k_struct) {
			return compositeCreateStruct(parent, binding);
		} else if (key == ICompositeType.k_union) {
			return compositeCreateUnion(parent, binding);
		} else {
			LOGGER.error("Unsupported composite type! binding = " + binding.getClass().getName());
			throw new CppBuilderException("Unsupported composite type!");
		}
	}

	private @NotNull CppClassNode compositeCreateClass(@NotNull Node parent, @NotNull ICPPClassType binding)
			throws GraphException, CppBuilderException {
		final CppClassNode foundNode = getNodeFromNodeMap(binding, CppClassNode.class, parent);
		if (foundNode != null) {
			LOGGER.info("Skipped binding: Node already exist! binding = " + binding.getClass().getName());
			return foundNode;
		}

		final CppClassNode node = initializeCppNode(binding, parent, new CppClassNode(nodeGroup));

		if (binding.getName().isEmpty()) {
			System.out.println();
		}

		// anonymous
		node.setAnonymous(binding.getName().isEmpty());

		// locations
		final List<IASTCompositeTypeSpecifier> parentsNode
				= findParentsNode(translationUnit.getDefinitionsInAST(binding), IASTCompositeTypeSpecifier.class);
		node.setLocations(locateBinding(parentsNode));

		// bases
		final ICPPBase[] baseBindings = binding.getBases();
		final List<CppType> bases = new ArrayList<>(baseBindings.length);
		for (final ICPPBase baseBinding : baseBindings) bases.add(buildType(baseBinding.getBaseClassType()));
		node.setBases(bases);

		// children
		for (final IASTCompositeTypeSpecifier specifier : parentsNode) {
			final ChildrenBuilder builder = new ChildrenBuilder(node);
			specifier.accept(builder);
			builder.rethrowException();
		}

//		for (final ICPPMethod method : binding.getDeclaredMethods()) {
//			if (method.getOwner() == binding) {
//				buildFunction(node, method);
//			} else {
//				LOGGER.info("Skipped method: not in current scope! binding = " + binding.getClass().getName()
//						+ "; method = " + method.getClass().getName());
//			}
//		}
//
//		for (final ICPPField field : binding.getDeclaredFields()) {
//			if (field.getOwner() == binding) {
//				buildVariable(node, field);
//			} else {
//				LOGGER.info("Skipped field: not in current scope! binding = " + binding.getClass().getName()
//						+ "; field = " + field.getClass().getName());
//			}
//		}
//
//		for (final ICPPClassType nestedClass : binding.getNestedClasses()) {
//			if (nestedClass.getOwner() == binding) {
//				buildComposite(node, nestedClass);
//			} else {
//				LOGGER.info("Skipped nested class: not in current scope! nestedClass = "
//						+ nestedClass.getClass().getName());
//			}
//		}

		return node;
	}

	private @NotNull CppStructNode compositeCreateStruct(@NotNull Node parent, @NotNull ICompositeType binding)
			throws GraphException, CppBuilderException {
		final CppStructNode foundNode = getNodeFromNodeMap(binding, CppStructNode.class, parent);
		if (foundNode != null) {
			LOGGER.info("Skipped binding: Node already exist! binding = " + binding.getClass().getName());
			return foundNode;
		}

		final CppStructNode node = initializeCppNode(binding, parent, new CppStructNode(nodeGroup));

		// anonymous
		node.setAnonymous(binding.getName().isEmpty());

		// locations
		final List<IASTCompositeTypeSpecifier> parentsNode
				= findParentsNode(translationUnit.getDefinitionsInAST(binding), IASTCompositeTypeSpecifier.class);
		node.setLocations(locateBinding(parentsNode));

		// children
		for (final IASTCompositeTypeSpecifier specifier : parentsNode) {
			final ChildrenBuilder builder = new ChildrenBuilder(node);
			specifier.accept(builder);
			builder.rethrowException();
		}

//		for (final IField field : binding.getFields()) {
//			if (field.getOwner() == binding) {
//				buildVariable(node, field);
//			} else {
//				LOGGER.info("Skipped field: not in current scope! binding = " + binding.getClass().getName()
//						+ "; field = " + field.getClass().getName());
//			}
//		}

		return node;
	}

	private @NotNull CppUnionNode compositeCreateUnion(@NotNull Node parent, @NotNull ICompositeType binding)
			throws GraphException, CppBuilderException {
		final CppUnionNode foundNode = getNodeFromNodeMap(binding, CppUnionNode.class, parent);
		if (foundNode != null) {
			LOGGER.info("Skipped binding: Node already exist! binding = " + binding.getClass().getName());
			return foundNode;
		}

		final CppUnionNode node = initializeCppNode(binding, parent, new CppUnionNode(nodeGroup));

		// anonymous
		node.setAnonymous(binding.getName().isEmpty());

		// locations
		final List<IASTCompositeTypeSpecifier> parentsNode
				= findParentsNode(translationUnit.getDefinitionsInAST(binding), IASTCompositeTypeSpecifier.class);
		node.setLocations(locateBinding(parentsNode));

		// children
		for (final IASTCompositeTypeSpecifier specifier : parentsNode) {
			final ChildrenBuilder builder = new ChildrenBuilder(node);
			specifier.accept(builder);
			builder.rethrowException();
		}

//		for (final IField field : binding.getFields()) {
//			if (field.getOwner() == binding) {
//				buildVariable(node, field);
//			} else {
//				LOGGER.info("Skipped field: not in current scope! binding = " + binding.getClass().getName()
//						+ "; field = " + field.getClass().getName());
//			}
//		}

		return node;
	}

	private class ChildrenBuilder extends ASTVisitor {
		private final @NotNull CppNode parent;
		protected @Nullable GraphException graphException;
		protected @Nullable CppBuilderException builderException;

		ChildrenBuilder(@NotNull CppNode parent) {
			this.shouldVisitDeclarations = true;
			this.parent = parent;
		}

		final void rethrowException() throws GraphException, CppBuilderException {
			if (graphException != null) throw graphException;
			if (builderException != null) throw builderException;
		}

		@Override
		public int visit(@NotNull IASTDeclaration declaration) {
			try {
				buildDeclaration(parent, declaration);
				return PROCESS_SKIP;
			} catch (final GraphException exception) {
				this.graphException = exception;
				return PROCESS_ABORT;
			} catch (final CppBuilderException exception) {
				this.builderException = exception;
				return PROCESS_ABORT;
			}
		}
	}

	private @Nullable CppNamespaceNode buildNamespace(@NotNull Node parent, @NotNull ICPPNamespace binding)
			throws GraphException, CppBuilderException {
		if (binding instanceof ICPPNamespaceAlias) return null;

		final CppNamespaceNode foundNode = getNodeFromNodeMap(binding, CppNamespaceNode.class, parent);
		if (foundNode != null) {
			LOGGER.info("Skipped binding: Node already exist! binding = " + binding.getClass().getName());
			return foundNode;
		}

		final CppNamespaceNode node = initializeCppNode(binding, parent, new CppNamespaceNode(nodeGroup));

		// locations
		node.setLocations(locateBinding(
				findParentsNode(translationUnit.getDefinitionsInAST(binding), ICPPASTNamespaceDefinition.class)));

		// children
		for (final IBinding memberBinding : binding.getMemberBindings()) {
			if (memberBinding.getOwner() == binding) {
				buildBinding(node, memberBinding);
			} else {
				LOGGER.info("Skipped member binding: not in current scope! binding = " + binding.getClass().getName()
						+ "; memberBinding = " + memberBinding.getClass().getName());
			}
		}

		return node;
	}

	private @Nullable CppNode getNodeFromNodeMap(@NotNull IBinding binding) {
		return nodeMap.get(binding);
	}

	private <E extends CppNode> @Nullable E getNodeFromNodeMap(@NotNull IBinding binding, @NotNull Class<E> nodeClass)
			throws CppBuilderException {
		final CppNode node = nodeMap.get(binding);
		if (node == null) return null;
		if (nodeClass.isInstance(node)) return nodeClass.cast(node);
		LOGGER.error("Node mismatch! binding = " + binding.getClass().getName());
		throw new CppBuilderException("Node mismatch!");
	}

	private <E extends CppNode> @Nullable E getNodeFromNodeMap(@NotNull IBinding binding, @NotNull Class<E> nodeClass,
			@NotNull Node parent) throws CppBuilderException {
		final E node = getNodeFromNodeMap(binding, nodeClass);
		if (node == null) return null;
		if (node.getParent() == parent) return node;
		LOGGER.error("Node have wrong parent! binding = " + binding.getClass().getName());
		throw new CppBuilderException("Node have wrong parent!");
	}

	private void putNodeToNodeMap(@NotNull IBinding binding, @NotNull CppNode node) throws CppBuilderException {
		if (nodeMap.put(binding, node) != null) {
			LOGGER.error("Node already exist! binding = " + binding.getClass().getName());
			throw new CppBuilderException("Node already exist!");
		}
	}

	//endregion Node Builder

	//region Type Builder

	private @NotNull CppType buildType(@NotNull IType type) throws GraphException, CppBuilderException {
		if (type instanceof IBasicType || type instanceof IProblemType || type instanceof ICPPUnknownType) {
			return buildBasicType(type);


		} else if (type instanceof ICompositeType) {
			final ICompositeType compositeType = (ICompositeType) type;
			if (compositeType instanceof ICPPClassType) {
				return buildNodeType(type, compositeType, CppClassNode.class);
			}
			switch (compositeType.getKey()) {
				case ICompositeType.k_struct:
					return buildNodeType(type, compositeType, CppStructNode.class);
				case ICompositeType.k_union:
					return buildNodeType(type, compositeType, CppUnionNode.class);
			}
			LOGGER.error("Unsupported composite type! type = " + type.getClass().getName());
			throw new CppBuilderException("Unsupported composite type!");

		} else if (type instanceof IEnumeration) {
			return buildNodeType(type, (IBinding) type, CppEnumerationNode.class);

		} else if (type instanceof ITypedef) {
			return buildNodeType(type, (IBinding) type, CppTypedefNode.class);


		} else if (type instanceof IArrayType) {
			final IArrayType arrayType = (IArrayType) type;
			return buildTypedType(type, arrayType.getType());

		} else if (type instanceof IPointerType) {
			final IPointerType pointerType = (IPointerType) type;
			return buildTypedType(type, pointerType.getType());

		} else if (type instanceof IQualifierType) {
			final IQualifierType qualifierType = (IQualifierType) type;
			return buildTypedType(type, qualifierType.getType());

		} else if (type instanceof ICPPReferenceType) {
			final ICPPReferenceType referenceType = (ICPPReferenceType) type;
			return buildTypedType(type, referenceType.getType());

		} else if (type instanceof ICPPParameterPackType) {
			final ICPPParameterPackType parameterPackType = (ICPPParameterPackType) type;
			return buildTypedType(type, parameterPackType.getType());


		} else if (type instanceof IFunctionType) {
			return buildFunctionType((IFunctionType) type);

		} else {
			LOGGER.error("Unsupported type! type = " + type.getClass().getName());
			throw new CppBuilderException("Unsupported type!");
		}
	}

	private <E extends CppNode> @NotNull CppNodeType buildNodeType(@NotNull IType type, @NotNull IBinding binding,
			@NotNull Class<E> nodeClass) throws CppBuilderException {
		assert type == binding;
		// check if exist
		final TypeWrapper wrapper = new TypeWrapper(type);
		final CppNodeType foundType = getTypeFromTypeMap(wrapper, CppNodeType.class);
		if (foundType != null) return foundType;

		// create new
		final CppNodeType nodeType = new CppNodeType(typeGroup);
		putTypeToTypeMap(wrapper, nodeType);
		nodeType.setName(wrapper.getTypeString());

		// set node
		final E node = getNodeFromNodeMap(binding, nodeClass);
		if (node != null) nodeType.setNode(node);
		return nodeType;
	}

	private void setNodeForNodeType(@NotNull IType type, @NotNull CppNode node) throws CppBuilderException {
		final TypeWrapper wrapper = new TypeWrapper(type);
		final CppNodeType foundType = getTypeFromTypeMap(wrapper, CppNodeType.class);
		if (foundType != null) {
			if (foundType.getNode() != null) {
				LOGGER.error("Type linked to wrong node! type = " + type.getClass().getName());
				throw new CppBuilderException("Type linked to wrong node!");
			}
			foundType.setNode(node);
		}
	}

	private @NotNull CppTypedType buildTypedType(@NotNull IType type, @NotNull IType innerType)
			throws GraphException, CppBuilderException {
		final TypeWrapper wrapper = new TypeWrapper(type);
		final CppTypedType foundType = getTypeFromTypeMap(wrapper, CppTypedType.class);
		if (foundType != null) return foundType;

		final CppTypedType typedType = new CppTypedType(typeGroup);
		putTypeToTypeMap(wrapper, typedType);
		typedType.setName(wrapper.getTypeString());

		typedType.setType(buildType(innerType));
		return typedType;
	}

	private @NotNull CppBasicType buildBasicType(@NotNull IType type) throws CppBuilderException {
		// check if exist
		final TypeWrapper wrapper = new TypeWrapper(type);
		final CppBasicType cppType = getTypeFromTypeMap(wrapper, CppBasicType.class);
		if (cppType != null) return cppType;

		// create new
		final CppBasicType basicType = new CppBasicType(typeGroup);
		putTypeToTypeMap(wrapper, basicType);
		basicType.setName(wrapper.getTypeString());
		return basicType;
	}

	private @NotNull CppFunctionType buildFunctionType(@NotNull IFunctionType type)
			throws GraphException, CppBuilderException {
		// check if exist
		final TypeWrapper wrapper = new TypeWrapper(type);
		final CppFunctionType cppType = getTypeFromTypeMap(wrapper, CppFunctionType.class);
		if (cppType != null) return cppType;

		// create new
		final CppFunctionType functionType = new CppFunctionType(typeGroup);
		putTypeToTypeMap(wrapper, functionType);
		functionType.setName(wrapper.getTypeString());

		functionType.setReturnType(buildType(type.getReturnType()));

		final IType[] parameterTypes = type.getParameterTypes();
		final List<CppType> parameters = new ArrayList<>(parameterTypes.length);
		for (final IType parameterType : parameterTypes) {
			parameters.add(buildType(parameterType));
		}
		functionType.setParameters(parameters);

		functionType.setVarArgs(type.takesVarArgs());

		return functionType;
	}

	private <E extends CppType> @Nullable E getTypeFromTypeMap(@NotNull TypeWrapper wrapper,
			@NotNull Class<E> typeClass) throws CppBuilderException {
		final CppType cppType = typeMap.get(wrapper);
		if (cppType == null) return null;
		if (typeClass.isInstance(cppType)) return typeClass.cast(cppType);
		LOGGER.error("Type mismatch! type = " + wrapper.getType().getClass().getName());
		throw new CppBuilderException("Type mismatch!");
	}

	private void putTypeToTypeMap(@NotNull TypeWrapper wrapper, @NotNull CppType cppType)
			throws CppBuilderException {
		if (typeMap.put(wrapper, cppType) != null) {
			LOGGER.error("Type already existed! type = " + wrapper.getType().getClass().getName());
			throw new CppBuilderException("Type already existed!");
		}
	}

	private static final class TypeWrapper {
		private final @NotNull IType type;
		private final @NotNull String typeString;


		TypeWrapper(@NotNull IType type) {
			this.type = type;
			this.typeString = ASTTypeUtil.getType(type, false)
					.trim()
					.replaceAll("\\{.*?}", "{anonymous}")
					.replaceAll("\\s+", " ");
		}


		public @NotNull IType getType() {
			return type;
		}

		public @NotNull String getTypeString() {
			return typeString;
		}


		@Override
		public boolean equals(@Nullable Object object) {
			if (this == object) return true;
			if (!(object instanceof TypeWrapper)) return false;
			final TypeWrapper wrapper = (TypeWrapper) object;
			return typeString.equals(wrapper.typeString) && type.isSameType(wrapper.type);
		}

		@Override
		public int hashCode() {
			return typeString.hashCode();
		}
	}

	//endregion Node Builder

	//region AST Builder

	private @NotNull CppNode qualifiedNameGetParent(@NotNull ICPPASTQualifiedName name)
			throws GraphException, CppBuilderException {
		final ICPPASTNameSpecifier[] qualifier = name.getQualifier();
		if (qualifier.length == 0) {
			LOGGER.error("Invalid qualified name! name = " + name.getClass().getName());
			throw new CppBuilderException("Invalid qualified name!");
		}
		final IBinding parentBinding = checkBinding(qualifier[0].resolveBinding());
		CppNode parent = getNodeFromNodeMap(parentBinding);
		if (parent == null && translationUnit instanceof ICPPASTTranslationUnit) {
			final ICPPASTTranslationUnit translationUnit = (ICPPASTTranslationUnit) this.translationUnit;
			final ICPPNamespace globalNamespace = translationUnit.getGlobalNamespace();
			final ICPPNamespaceScope globalScope = globalNamespace.getNamespaceScope();
			try {
				if (parentBinding.getScope().equals(globalScope)) {
					parent = buildBinding(rootNode, parentBinding);
				}
			} catch (final DOMException exception) {
				LOGGER.info("Exception while getting scope of binding!", exception);
			}
		}
		int index = 1;
		while (true) {
			if (parent == null) {
				LOGGER.error("Cannot resolve qualified name: name = " + name.getClass().getName());
				throw new CppBuilderException("Cannot resolve qualified name!");
			}
			if (index >= qualifier.length) return parent;
			final IBinding binding = checkBinding(qualifier[index].resolveBinding());
			parent = buildBinding(parent, binding);
			index++;
		}
	}

	private void buildDeclarations(@NotNull Node container, @NotNull IASTDeclarationListOwner declarations)
			throws GraphException, CppBuilderException {
		for (final IASTDeclaration declaration : declarations.getDeclarations(false)) {
			final IASTFileLocation fileLocation = declaration.getFileLocation();
			if (fileLocation != null) {
				final String fileName = fileLocation.getFileName();
				try {
					final Path filePath = Path.of(fileName).toRealPath(LinkOption.NOFOLLOW_LINKS);
					if (projectFiles.contains(filePath)) {
						buildDeclaration(container, declaration);
					} else {
						LOGGER.info("Skipped declaration: not in project! declaration = "
								+ declaration.getClass().getName());
					}
				} catch (final IOException exception) {
					LOGGER.info("Skipped declaration: file location not exist! declaration = "
							+ declaration.getClass().getName(), exception);
				}
			} else {
				LOGGER.info("Skipped declaration: unknown file location! declaration = "
						+ declaration.getClass().getName());
			}
		}
	}

	private void buildDeclaration(@NotNull Node parent, @NotNull IASTDeclaration declaration)
			throws GraphException, CppBuilderException {
		if (declaration instanceof ICPPASTVisibilityLabel
				|| declaration instanceof ICPPASTUsingDeclaration
				|| declaration instanceof ICPPASTUsingDirective
				|| declaration instanceof ICPPASTAliasDeclaration
				|| declaration instanceof ICPPASTStaticAssertDeclaration
				|| declaration instanceof ICPPASTExplicitTemplateInstantiation
				|| declaration instanceof ICPPASTInitCapture
				|| declaration instanceof IASTASMDeclaration
				|| declaration instanceof IASTProblemDeclaration) {
			/*
			private:                                   // ICPPASTVisibilityLabel
			using std::string;                         // ICPPASTUsingDeclaration
			using namespace std;                       // ICPPASTUsingDirective
			using flags = std::ios_base::fmtflags;     // ICPPASTAliasDeclaration
			static_assert(true, "message");            // ICPPASTStaticAssertDeclaration
			template class N::Y<char*>;                // ICPPASTExplicitTemplateInstantiation
			auto func = [&a](int b) { return a + b; }; // ICPPASTInitCapture
			asm("inc eax");                            // IASTASMDeclaration
			*/
			LOGGER.info("Skipped declaration! declaration = " + declaration.getClass().getName());

		} else if (declaration instanceof ICPPASTNamespaceDefinition) {
			buildNamespaceDefinition(parent, (ICPPASTNamespaceDefinition) declaration);

		} else if (declaration instanceof IASTFunctionDefinition) {
			buildFunctionDefinition(parent, (IASTFunctionDefinition) declaration);

		} else if (declaration instanceof IASTSimpleDeclaration) {
			buildSimpleDeclaration(parent, (IASTSimpleDeclaration) declaration);

		} else if (declaration instanceof ICPPASTLinkageSpecification) {
			buildDeclarations(parent, (ICPPASTLinkageSpecification) declaration);

		} else if (declaration instanceof ICPPASTTemplateDeclaration) {
			buildDeclaration(parent, ((ICPPASTTemplateDeclaration) declaration).getDeclaration());

		} else {
			LOGGER.error("Unsupported declaration! declaration = " + declaration.getClass().getName());
			throw new CppBuilderException("Unsupported declaration!");
		}
	}

	private void buildNamespaceDefinition(@NotNull Node parent, @NotNull ICPPASTNamespaceDefinition definition)
			throws GraphException, CppBuilderException {
		final IASTName name = definition.getName();
		final Node trueParent
				= name instanceof ICPPASTQualifiedName
				? qualifiedNameGetParent((ICPPASTQualifiedName) name)
				: parent;
		final IBinding binding = checkBinding(name.resolveBinding());
		if (binding instanceof ICPPNamespace) {
			buildNamespace(trueParent, (ICPPNamespace) binding);
		} else if (binding instanceof IProblemBinding) {
			LOGGER.warn("Problem binding when resolve namespace definition! definition = "
					+ definition.getClass().getName() + "; binding = " + binding.getClass().getName());
		} else {
			LOGGER.error("Unsupported namespace definition! definition = " + definition.getClass().getName()
					+ "; binding = " + binding.getClass().getName());
			throw new CppBuilderException("Unsupported namespace definition!");
		}
	}

	private void buildFunctionDefinition(@NotNull Node parent, @NotNull IASTFunctionDefinition definition)
			throws GraphException, CppBuilderException {
		final IASTFunctionDeclarator declarator = definition.getDeclarator();
		final IASTName name = declarator.getName();
		final Node trueParent
				= name instanceof ICPPASTQualifiedName
				? qualifiedNameGetParent((ICPPASTQualifiedName) name)
				: parent;
		final IBinding binding = checkBinding(name.resolveBinding());
		if (binding instanceof IFunction) {
			buildFunction(trueParent, (IFunction) binding);
			return;
		} else if (binding instanceof ICPPClassType) {
			// is this function a constructor?
			final ICPPClassType classType = (ICPPClassType) binding;
			for (final ICPPConstructor constructor : classType.getConstructors()) {
				if (Arrays.asList(translationUnit.getDefinitionsInAST(constructor)).contains(name)) {
					buildFunction(trueParent, constructor);
					return;
				}
			}
		} else if (binding instanceof IProblemBinding) {
			LOGGER.warn("Problem binding when resolve function definition! definition = "
					+ definition.getClass().getName() + "; binding = " + binding.getClass().getName());
			return;
		}
		LOGGER.error("Unsupported function definition! definition = " + definition.getClass().getName()
				+ "; binding = " + binding.getClass().getName());
		throw new CppBuilderException("Unsupported function definition!");
	}

	private void buildSimpleDeclaration(@NotNull Node parent, @NotNull IASTSimpleDeclaration declaration)
			throws GraphException, CppBuilderException {
		buildDeclarationSpecifier(parent, declaration.getDeclSpecifier());

		for (final IASTDeclarator declarator : declaration.getDeclarators()) {
			final IASTName name = declarator.getName();
			final Node trueParent
					= name instanceof ICPPASTQualifiedName
					? qualifiedNameGetParent((ICPPASTQualifiedName) name)
					: parent;
			final IBinding binding = checkBinding(name.resolveBinding());
			if (binding instanceof IVariable) {
				buildVariable(trueParent, (IVariable) binding);
			} else if (binding instanceof ITypedef) {
				buildTypedef(trueParent, (ITypedef) binding);
			} else if (binding instanceof IFunction) {
				buildFunction(trueParent, (IFunction) binding);
			} else if (binding instanceof IProblemBinding) {
				LOGGER.warn("Problem binding when resolve simple declaration! declaration = "
						+ declaration.getClass().getName() + "; binding = " + binding.getClass().getName());
			} else {
				LOGGER.error("Unsupported simple declaration! declaration = " + declaration.getClass().getName()
						+ "; binding = " + binding.getClass().getName());
				throw new CppBuilderException("Unsupported simple declaration!");
			}
		}
	}

	private void buildDeclarationSpecifier(@NotNull Node parent, @NotNull IASTDeclSpecifier specifier)
			throws GraphException, CppBuilderException {
		if (specifier instanceof IASTElaboratedTypeSpecifier
				|| specifier instanceof IASTNamedTypeSpecifier
				|| specifier instanceof IASTSimpleDeclSpecifier) {
			// skipped
			LOGGER.info("Skipped declaration specifier! specifier = " + specifier.getClass().getName());

		} else if (specifier instanceof IASTCompositeTypeSpecifier) {
			buildCompositeSpecifier(parent, (IASTCompositeTypeSpecifier) specifier);

		} else if (specifier instanceof IASTEnumerationSpecifier) {
			buildEnumerationSpecifier(parent, (IASTEnumerationSpecifier) specifier);

		} else {
			LOGGER.error("Unsupported declaration specifier! specifier = " + specifier.getClass().getName());
			throw new CppBuilderException("Unsupported declaration specifier!");
		}
	}

	private void buildCompositeSpecifier(@NotNull Node parent, @NotNull IASTCompositeTypeSpecifier specifier)
			throws GraphException, CppBuilderException {
		final IASTName name = specifier.getName();
		final Node trueParent
				= name instanceof ICPPASTQualifiedName
				? qualifiedNameGetParent((ICPPASTQualifiedName) name)
				: parent;
		final IBinding binding = checkBinding(name.resolveBinding());
		if (binding instanceof ICompositeType) {
			buildComposite(trueParent, (ICompositeType) binding);
		} else if (binding instanceof IProblemBinding) {
			LOGGER.warn("Problem binding when resolve composite type specifier! declaration = "
					+ specifier.getClass().getName() + "; binding = " + binding.getClass().getName());
		} else {
			LOGGER.error("Unsupported composite type specifier! declaration = " + specifier.getClass().getName()
					+ "; binding = " + binding.getClass().getName());
			throw new CppBuilderException("Unsupported composite type specifier!");
		}
	}

	private void buildEnumerationSpecifier(@NotNull Node parent, @NotNull IASTEnumerationSpecifier specifier)
			throws GraphException, CppBuilderException {
		final IASTName name = specifier.getName();
		final Node trueParent
				= name instanceof ICPPASTQualifiedName
				? qualifiedNameGetParent((ICPPASTQualifiedName) name)
				: parent;
		final IBinding binding = checkBinding(name.resolveBinding());
		if (binding instanceof IEnumeration) {
			buildEnumeration(trueParent, (IEnumeration) binding);
		} else if (binding instanceof IProblemBinding) {
			LOGGER.warn("Problem binding when resolve enumeration specifier! declaration = "
					+ specifier.getClass().getName() + "; binding = " + binding.getClass().getName());
		} else {
			LOGGER.error("Unsupported enumeration specifier! declaration = " + specifier.getClass().getName()
					+ "; binding = " + binding.getClass().getName());
			throw new CppBuilderException("Unsupported enumeration specifier!");
		}
	}

	//endregion AST Builder

	private static @NotNull IBinding checkBinding(@Nullable IBinding binding) throws CppBuilderException {
		if (binding != null) return binding;
		throw new CppBuilderException("Binding is null!");
	}
}
