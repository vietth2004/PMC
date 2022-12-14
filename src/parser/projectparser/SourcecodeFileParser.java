package parser.projectparser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

import config.Paths;
import normalizer.AbstractNormalizer;
import normalizer.Cpp11ClassNormalizer;
import tree.object.AttributeOfStructureVariableNode;
import tree.object.ClassNode;
import tree.object.ConstructorNode;
import tree.object.DefinitionFunctionNode;
import tree.object.DestructorNode;
import tree.object.EnumNode;
import tree.object.EnumTypedefNode;
import tree.object.ExternalVariableNode;
import tree.object.FunctionNode;
import tree.object.INode;
import tree.object.ISourcecodeFileNode;
import tree.object.IncludeHeaderNode;
import tree.object.NamespaceNode;
import tree.object.Node;
import tree.object.PrimitiveTypedefDeclaration;
import tree.object.SourcecodeFileNode;
import tree.object.SpecialEnumTypedefNode;
import tree.object.SpecialStructTypedefNode;
import tree.object.SpecialUnionTypedefNode;
import tree.object.StructNode;
import tree.object.StructTypedefNode;
import tree.object.StructureNode;
import tree.object.TypedefDeclaration;
import tree.object.UnionNode;
import tree.object.UnionTypedefNode;
import tree.object.VariableNode;
import utils.Utils;
import utils.search.EnumTypedefNodeCondifion;
import utils.search.Search;
import utils.search.SearchCondition;
import utils.search.StructTypedefNodeCondifion;
import utils.search.UnionTypedefNodeCondifion;
import utils.tostring.SimpleTreeDisplayer;

/**
 * Construct structure tree of the given source code file
 *
 * @author DucAnh
 */
public class SourcecodeFileParser implements ISourcecodeFileParser {
	private SourcecodeFileNode sourcecodeNode;

	private IASTTranslationUnit translationUnit;

	public static void main(String[] args) throws Exception {
		SourcecodeFileParser cppParser = new SourcecodeFileParser();
		//INode root = cppParser.parseSourcecodeFile(new File(Paths.STUDENT_MANAGEMENT + "/src/object/CourceClass.h"));
		INode root = cppParser.parseSourcecodeFile2(new File("F:\\TestData\\TestApp"));

		/**
		 * display tree of project
		 */
//		SimpleTreeDisplayer treeDisplayer = new SimpleTreeDisplayer(root);
//		System.out.println(treeDisplayer.getTreeInString());

	}

	@Override
	public INode generateTree() throws Exception {
		File f = new File(sourcecodeNode.getAbsolutePath());
		if (f.exists())
			return parseSourcecodeFile(f);
		else
			return null;
	}

	public INode parseSourcecodeFile(File filePath) throws Exception {
		normalizeFile(filePath);

		translationUnit = getIASTTranslationUnit(Utils.readFileContent(filePath.getAbsolutePath()).toCharArray());

		CustomCppStack stackNodes = new CustomCppStack();
		Node vituralRoot = new TemporaryNode("tmp root Node");
		vituralRoot.setAbsolutePath(filePath.getCanonicalPath());
		stackNodes.push(vituralRoot);

		ASTVisitor visitor = new ASTVisitor() {
			boolean isPrivate = false;

			@Override
			public int leave(IASTDeclaration declaration) {
				stackNodes.pop();

				/**
				 * N???u tho??t kh???i class th?? scope bi???n lu??n l?? public
				 */
				if (SourcecodeFileParser.this
						.getTypeOfAstDeclaration(declaration) == ISourcecodeFileParser.IS_CLASS_DECLARATION
						|| SourcecodeFileParser.this
								.getTypeOfAstDeclaration(declaration) == ISourcecodeFileParser.IS_STRUCT_DECLARATION)
					isPrivate = false;

				return ASTVisitor.PROCESS_CONTINUE;
			}

			@Override
			public int leave(ICPPASTNamespaceDefinition namespaceDefinition) {
				stackNodes.pop();
				return ASTVisitor.PROCESS_CONTINUE;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				/**
				 * IASTDeclaration ?????i di???n m???t khai b???o trong m?? ngu???n
				 */
				Node declarationNode = new TemporaryNode("tmpNode");

				int typeOfDeclaration = SourcecodeFileParser.this.getTypeOfAstDeclaration(declaration);

				switch (typeOfDeclaration) {
				case IS_FUNCTION_DECLARATION:
					declarationNode = new FunctionNode();
					((FunctionNode) declarationNode).setAST((IASTFunctionDefinition) declaration);
					break;

				case IS_FUNCTION_AS_VARIABLE_DECLARATION:
					declarationNode = new DefinitionFunctionNode();
					((DefinitionFunctionNode) declarationNode).setAST((CPPASTSimpleDeclaration) declaration);
					break;

				case IS_CONSTRUCTOR_DECLARATION:
					declarationNode = new ConstructorNode();
					((ConstructorNode) declarationNode).setAST((IASTFunctionDefinition) declaration);
					break;

				case IS_DESTRUCTOR_DECLARATION:
					declarationNode = new DestructorNode();
					((DestructorNode) declarationNode).setAST((IASTFunctionDefinition) declaration);
					break;

				case IS_TEMPLATE_DECLARATION:
					IASTDeclaration template = ((ICPPASTTemplateDeclaration) declaration).getDeclaration();
					if (template instanceof IASTFunctionDefinition) {
						FunctionNode fn = new FunctionNode();
						fn.setAST((IASTFunctionDefinition) template);
						declarationNode = fn;
					}
					break;

				case IS_STRUCT_DECLARATION:
					declarationNode = new StructNode();
					((StructNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);

					isPrivate = false;
					break;

				case IS_CLASS_DECLARATION:
					declarationNode = new ClassNode();
					((ClassNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);

					isPrivate = false;
					break;

				case IS_VARIABLE_DECLARATION: {
					IASTSimpleDeclaration decList = (IASTSimpleDeclaration) declaration;
					IASTDeclSpecifier type = decList.getDeclSpecifier();
					INodeFactory fac = translationUnit.getASTNodeFactory();

					for (IASTDeclarator dec : decList.getDeclarators()) {
						IASTSimpleDeclaration decItem = fac.newSimpleDeclaration(type.copy(CopyStyle.withLocations));
						decItem.addDeclarator(dec.copy(CopyStyle.withLocations));

						stackNodes.push(declarationNode);
						stackNodes.pop();

						// Note: We have two types of variables known as internal variable, and external
						// variable.
						// Internal variable are passed into the function, e.g., void test(int a, int b)
						// -----> a, b: internal variable
						// All variables declared outside functions are considered as external
						// variables.
						// Because we only parse down to method level, so all variables discovered in
						// this process belong to kind of external variables.
						VariableNode var;
						if (stackNodes.peek().getParent() == null
								|| stackNodes.peek().getParent() instanceof NamespaceNode) {
							var = new ExternalVariableNode();
						} else {
							var = new AttributeOfStructureVariableNode();
						}
						var.setAST(decItem);
						declarationNode = var;
						var.setPrivate(isPrivate);
					}
					break;
				}

				case IS_PRIMITIVE_TYPEDEF_DECLARATION: {
					IASTSimpleDeclaration decList = (IASTSimpleDeclaration) declaration;
					IASTDeclSpecifier type = decList.getDeclSpecifier();
					INodeFactory fac = translationUnit.getASTNodeFactory();

					for (IASTDeclarator dec : decList.getDeclarators()) {
						IASTSimpleDeclaration decItem = fac.newSimpleDeclaration(type.copy(CopyStyle.withLocations));
						decItem.addDeclarator(dec.copy(CopyStyle.withLocations));

						stackNodes.push(declarationNode);
						stackNodes.pop();

						TypedefDeclaration td = new PrimitiveTypedefDeclaration();
						td.setAST(decItem);
						declarationNode = td;
					}
					break;
				}

				case IS_STRUCT_TYPEDEF_DECLARATION: {
					/*
					 * Ex1: typedef struct MyStruct4{ int x; } MyStruct5;
					 *
					 * Ex2: typedef struct { int x; } MyStruct5;
					 */
					declarationNode = new StructTypedefNode();
					((StructTypedefNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);

					isPrivate = false;
					break;
				}

				case IS_PROTECTED_LABEL:
				case IS_PRIVATE_LABEL:
					isPrivate = true;
					break;

				case IS_PUBLIC_LABEL:
					isPrivate = false;
					break;

				case IS_ENUM:
					declarationNode = new EnumNode();
					((EnumNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);
					break;

				case IS_ENUM_TYPEDEF_DECLARATION:
					declarationNode = new EnumTypedefNode();
					((EnumTypedefNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);
					isPrivate = false;
					break;

				case IS_UNION:
					declarationNode = new UnionNode();
					((UnionNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);
					break;

				case IS_UNION_TYPEDEF_DECLARATION:
					declarationNode = new UnionTypedefNode();
					((UnionTypedefNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);
					isPrivate = false;
					break;
				}

				stackNodes.push(declarationNode);

				if (typeOfDeclaration == ISourcecodeFileParser.IS_FUNCTION_DECLARATION) {
					stackNodes.pop();
					return ASTVisitor.PROCESS_SKIP;
				}
				return ASTVisitor.PROCESS_CONTINUE;
			}

			@Override
			public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
				NamespaceNode namespaceNode = new NamespaceNode();
				namespaceNode.setAST(namespaceDefinition);
				stackNodes.push(namespaceNode);
				return ASTVisitor.PROCESS_CONTINUE;
			}

		};
		visitor.shouldVisitDeclarations = true;
		visitor.shouldVisitNamespaces = true;

		translationUnit.accept(visitor);

		INode root = stackNodes.rootOfStack;

		createSpecialNode(root);
		addIncludeHeaderNodes(getHeader(translationUnit), root);
		return root;
	}

	ArrayList fileList = new ArrayList();

	public void listFilesForFolder(final File folder) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				if (fileEntry.getPath().endsWith(".h") || fileEntry.getPath().endsWith(".cpp"))
					fileList.add(fileEntry.getPath());
			}
		}
	}


	public INode parseSourcecodeFile2(File filePath) throws Exception {
		normalizeFile(filePath);

		File folder = new File("F:\\TestData\\TestApp");
		listFilesForFolder(folder);

		String source1 = "";

		for (int i = 0;i < fileList.size();i++)
		{
			source1 += Utils.readFileContent(fileList.get(i).toString());
		}

		IASTTranslationUnit translationUnit1 = getIASTTranslationUnit(source1.toCharArray());

		translationUnit = getIASTTranslationUnit(source1.toCharArray());

		//endregion

		//translationUnit = getIASTTranslationUnit(Utils.readFileContent(filePath.getAbsolutePath()).toCharArray());

		CustomCppStack stackNodes = new CustomCppStack();
		Node vituralRoot = new TemporaryNode("tmp root Node");
		vituralRoot.setAbsolutePath(filePath.getCanonicalPath());
		stackNodes.push(vituralRoot);

		ASTVisitor visitor = new ASTVisitor() {
			boolean isPrivate = false;

			@Override
			public int leave(IASTDeclaration declaration) {
				stackNodes.pop();

				/**
				 * N???u tho??t kh???i class th?? scope bi???n lu??n l?? public
				 */
				if (SourcecodeFileParser.this
						.getTypeOfAstDeclaration(declaration) == ISourcecodeFileParser.IS_CLASS_DECLARATION
						|| SourcecodeFileParser.this
						.getTypeOfAstDeclaration(declaration) == ISourcecodeFileParser.IS_STRUCT_DECLARATION)
					isPrivate = false;

				return ASTVisitor.PROCESS_CONTINUE;
			}

			@Override
			public int leave(ICPPASTNamespaceDefinition namespaceDefinition) {
				stackNodes.pop();
				return ASTVisitor.PROCESS_CONTINUE;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				/**
				 * IASTDeclaration ?????i di???n m???t khai b???o trong m?? ngu???n
				 */
				Node declarationNode = new TemporaryNode("tmpNode");

				int typeOfDeclaration = SourcecodeFileParser.this.getTypeOfAstDeclaration(declaration);

				switch (typeOfDeclaration) {
					case IS_FUNCTION_DECLARATION:
						declarationNode = new FunctionNode();
						((FunctionNode) declarationNode).setAST((IASTFunctionDefinition) declaration);
						break;

					case IS_FUNCTION_AS_VARIABLE_DECLARATION:
						declarationNode = new DefinitionFunctionNode();
						((DefinitionFunctionNode) declarationNode).setAST((CPPASTSimpleDeclaration) declaration);
						break;

					case IS_CONSTRUCTOR_DECLARATION:
						declarationNode = new ConstructorNode();
						((ConstructorNode) declarationNode).setAST((IASTFunctionDefinition) declaration);
						break;

					case IS_DESTRUCTOR_DECLARATION:
						declarationNode = new DestructorNode();
						((DestructorNode) declarationNode).setAST((IASTFunctionDefinition) declaration);
						break;

					case IS_TEMPLATE_DECLARATION:
						IASTDeclaration template = ((ICPPASTTemplateDeclaration) declaration).getDeclaration();
						if (template instanceof IASTFunctionDefinition) {
							FunctionNode fn = new FunctionNode();
							fn.setAST((IASTFunctionDefinition) template);
							declarationNode = fn;
						}
						break;

					case IS_STRUCT_DECLARATION:
						declarationNode = new StructNode();
						((StructNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);

						isPrivate = false;
						break;

					case IS_CLASS_DECLARATION:
						declarationNode = new ClassNode();
						((ClassNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);

						isPrivate = false;
						break;

					case IS_VARIABLE_DECLARATION: {
						IASTSimpleDeclaration decList = (IASTSimpleDeclaration) declaration;
						IASTDeclSpecifier type = decList.getDeclSpecifier();
						INodeFactory fac = translationUnit.getASTNodeFactory();

						for (IASTDeclarator dec : decList.getDeclarators()) {
							IASTSimpleDeclaration decItem = fac.newSimpleDeclaration(type.copy(CopyStyle.withLocations));
							decItem.addDeclarator(dec.copy(CopyStyle.withLocations));

							stackNodes.push(declarationNode);
							stackNodes.pop();

							// Note: We have two types of variables known as internal variable, and external
							// variable.
							// Internal variable are passed into the function, e.g., void test(int a, int b)
							// -----> a, b: internal variable
							// All variables declared outside functions are considered as external
							// variables.
							// Because we only parse down to method level, so all variables discovered in
							// this process belong to kind of external variables.
							VariableNode var;
							if (stackNodes.peek().getParent() == null
									|| stackNodes.peek().getParent() instanceof NamespaceNode) {
								var = new ExternalVariableNode();
							} else {
								var = new AttributeOfStructureVariableNode();
							}
							var.setAST(decItem);
							declarationNode = var;
							var.setPrivate(isPrivate);
						}
						break;
					}

					case IS_PRIMITIVE_TYPEDEF_DECLARATION: {
						IASTSimpleDeclaration decList = (IASTSimpleDeclaration) declaration;
						IASTDeclSpecifier type = decList.getDeclSpecifier();
						INodeFactory fac = translationUnit.getASTNodeFactory();

						for (IASTDeclarator dec : decList.getDeclarators()) {
							IASTSimpleDeclaration decItem = fac.newSimpleDeclaration(type.copy(CopyStyle.withLocations));
							decItem.addDeclarator(dec.copy(CopyStyle.withLocations));

							stackNodes.push(declarationNode);
							stackNodes.pop();

							TypedefDeclaration td = new PrimitiveTypedefDeclaration();
							td.setAST(decItem);
							declarationNode = td;
						}
						break;
					}

					case IS_STRUCT_TYPEDEF_DECLARATION: {
						/*
						 * Ex1: typedef struct MyStruct4{ int x; } MyStruct5;
						 *
						 * Ex2: typedef struct { int x; } MyStruct5;
						 */
						declarationNode = new StructTypedefNode();
						((StructTypedefNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);

						isPrivate = false;
						break;
					}

					case IS_PROTECTED_LABEL:
					case IS_PRIVATE_LABEL:
						isPrivate = true;
						break;

					case IS_PUBLIC_LABEL:
						isPrivate = false;
						break;

					case IS_ENUM:
						declarationNode = new EnumNode();
						((EnumNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);
						break;

					case IS_ENUM_TYPEDEF_DECLARATION:
						declarationNode = new EnumTypedefNode();
						((EnumTypedefNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);
						isPrivate = false;
						break;

					case IS_UNION:
						declarationNode = new UnionNode();
						((UnionNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);
						break;

					case IS_UNION_TYPEDEF_DECLARATION:
						declarationNode = new UnionTypedefNode();
						((UnionTypedefNode) declarationNode).setAST((IASTSimpleDeclaration) declaration);
						isPrivate = false;
						break;
				}

				stackNodes.push(declarationNode);

				if (typeOfDeclaration == ISourcecodeFileParser.IS_FUNCTION_DECLARATION) {
					stackNodes.pop();
					return ASTVisitor.PROCESS_SKIP;
				}
				return ASTVisitor.PROCESS_CONTINUE;
			}

			@Override
			public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
				NamespaceNode namespaceNode = new NamespaceNode();
				namespaceNode.setAST(namespaceDefinition);
				stackNodes.push(namespaceNode);
				return ASTVisitor.PROCESS_CONTINUE;
			}

		};
		visitor.shouldVisitDeclarations = true;
		visitor.shouldVisitNamespaces = true;

		translationUnit.accept(visitor);

		INode root = stackNodes.rootOfStack;

		createSpecialNode(root);
		addIncludeHeaderNodes(getHeader(translationUnit), root);
		return root;
	}

	private void createSpecialNode(INode root) {
		List<SearchCondition> conditions = new ArrayList<>();
		conditions.add(new StructTypedefNodeCondifion());
		conditions.add(new UnionTypedefNodeCondifion());
		conditions.add(new EnumTypedefNodeCondifion());
		List<INode> typedefNodes = Search.searchNodes(root, conditions);

		for (INode typedefNode : typedefNodes) {
			StructureNode newTypedefNode = null;

			if (typedefNode instanceof StructNode) {
				newTypedefNode = new SpecialStructTypedefNode();
			} else if (typedefNode instanceof EnumNode) {
				newTypedefNode = new SpecialEnumTypedefNode();
			} else if (typedefNode instanceof UnionNode) {
				newTypedefNode = new SpecialUnionTypedefNode();
			}
			/*
			 * Ex: typedef xxx myXXX { int x; } MyXXX5;
			 */
			try {
				newTypedefNode.setAST(((StructureNode) typedefNode).getAST());
				newTypedefNode.setChildren(typedefNode.getChildren());
				newTypedefNode.setParent(typedefNode.getParent());
				typedefNode.getParent().getChildren().add(newTypedefNode);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Th??m c??c node m?? t??? include v??o c??y
	 *
	 * @param includeHeaderNodes
	 * @param root
	 */

	private void addIncludeHeaderNodes(List<IASTPreprocessorIncludeStatement> includeHeaderNodes, INode root) {
		for (IASTPreprocessorIncludeStatement include : includeHeaderNodes) {
			IncludeHeaderNode includeHeaderNode = new IncludeHeaderNode();
			includeHeaderNode.setAST(include);
			includeHeaderNode.setAbsolutePath(
					root.getAbsolutePath() + File.separator + "\"" + includeHeaderNode.getNewType() + "\"");
			root.getChildren().add(includeHeaderNode);
		}
	}

	/**
	 * L???y danh s??ch include c???a m???t t???p .cpp
	 *
	 * @param u
	 * @return
	 */
	private List<IASTPreprocessorIncludeStatement> getHeader(IASTTranslationUnit u) {
		List<IASTPreprocessorIncludeStatement> includes = new ArrayList<>();
		for (IASTPreprocessorIncludeStatement child : u.getIncludeDirectives())
			includes.add(child);
		return includes;
	}

	public IASTTranslationUnit getIASTTranslationUnit(char[] code) throws Exception {
		FileContent fc = FileContent.create("", code);
		Map<String, String> macroDefinitions = new HashMap<>();
		String[] includeSearchPaths = new String[0];
		IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
		IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
		IIndex idx = null;
		int options = ILanguage.OPTION_IS_SOURCE_UNIT;
		IParserLogService log = new DefaultLogService();
		return GPPLanguage.getDefault().getASTTranslationUnit(fc, si, ifcp, idx, options, log);
	}

	/**
	 * L???y ki???u AST Node
	 *
	 * @param astNode
	 * @return
	 */
	private int getTypeOfAstDeclaration(IASTDeclaration astNode) {
		// C??u l???nh r???ng
		if (astNode instanceof IASTNullStatement)
			return ISourcecodeFileParser.IS_UNSPECIFIED_DECLARATION;
		else
		/*
		 * Ex: enum Color { RED, GREEN, BLUE };
		 */
		if (astNode.getChildren().length >= 1 && astNode.getChildren()[0] instanceof ICPPASTEnumerationSpecifier
				&& astNode.getChildren()[0].getRawSignature().contains(ISourcecodeFileParser.ENUM_SYMBOL))
			if (astNode.getChildren()[0].getRawSignature().contains(ISourcecodeFileParser.TYPEDEF_SYMBOL))
				return ISourcecodeFileParser.IS_ENUM_TYPEDEF_DECLARATION;
			else
				return ISourcecodeFileParser.IS_ENUM;
		else
		/*
		 * Ex: union RGBA{ int color; int aliasColor;}
		 */
		if (astNode.getChildren().length >= 1 && astNode.getChildren()[0] instanceof CPPASTCompositeTypeSpecifier
				&& astNode.getChildren()[0].getRawSignature().contains(ISourcecodeFileParser.UNION_SYMBOL))
			if (astNode.getChildren()[0].getRawSignature().contains(ISourcecodeFileParser.TYPEDEF_SYMBOL))
				return ISourcecodeFileParser.IS_UNION_TYPEDEF_DECLARATION;
			else
				return ISourcecodeFileParser.IS_UNION;
		else
		/*
		 *
		 * N???u node l?? public/private/protected
		 */
		if (astNode instanceof ICPPASTVisibilityLabel)
			switch (((ICPPASTVisibilityLabel) astNode).getVisibility()) {
			case ICPPASTVisibilityLabel.v_private:
				return ISourcecodeFileParser.IS_PRIVATE_LABEL;
			case ICPPASTVisibilityLabel.v_protected:
				return ISourcecodeFileParser.IS_PROTECTED_LABEL;
			case ICPPASTVisibilityLabel.v_public:
				return ISourcecodeFileParser.IS_PUBLIC_LABEL;
			}
		else if (astNode instanceof IASTFunctionDefinition) {
			if (astNode.getRawSignature().contains(ISourcecodeFileParser.FUNCTION_BODY_SIGNAL)) {
				if (isConstructor((IASTFunctionDefinition) astNode))
					return ISourcecodeFileParser.IS_CONSTRUCTOR_DECLARATION;
				else if (isDestructor((IASTFunctionDefinition) astNode))
					return ISourcecodeFileParser.IS_DESTRUCTOR_DECLARATION;
				else
					return ISourcecodeFileParser.IS_FUNCTION_DECLARATION;
			} else
				return ISourcecodeFileParser.IS_UNSPECIFIED_DECLARATION;
		} else if (astNode instanceof ICPPASTTemplateDeclaration)
			return ISourcecodeFileParser.IS_TEMPLATE_DECLARATION;
		else if (astNode instanceof IASTSimpleDeclaration) {
			/*
			 * IASTSimpleDeclaration ?????i di???n c??u l???nh khai b??o bi???n, struct, class, enum,
			 * union
			 */
			IASTDeclSpecifier declSpecifier = ((IASTSimpleDeclaration) astNode).getDeclSpecifier();
			/*
			 * IASTCompositeTypeSpecifier ?????i di???n cho c???u tr??c ch???a khai b??o nhi???u th??nh
			 * ph???n con b??n trong VD: struct, class, union.
			 */
			if (declSpecifier instanceof IASTCompositeTypeSpecifier)
				switch (((IASTCompositeTypeSpecifier) declSpecifier).getKey()) {

				case IASTCompositeTypeSpecifier.k_struct:
					/*
					 * Ex: typedef struct { int x; } MyStruct1;
					 */
					if (astNode.getRawSignature().startsWith("typedef "))
						return ISourcecodeFileParser.IS_STRUCT_TYPEDEF_DECLARATION;
					else
						return ISourcecodeFileParser.IS_STRUCT_DECLARATION;
				case ICPPASTCompositeTypeSpecifier.k_class:
					return ISourcecodeFileParser.IS_CLASS_DECLARATION;
				}
			else
			/*
			 * IASTSimpleDeclSpecifier t????ng ???ng v???i bi???n ki???u c?? b???n nh?? int, float,
			 * double, v.v.
			 */
			if (declSpecifier instanceof IASTSimpleDeclSpecifier
					|| declSpecifier instanceof CPPASTCompositeTypeSpecifier
					|| declSpecifier instanceof IASTNamedTypeSpecifier)
			/*
			 * CPPASTNamedTypeSpecifier t????ng ???ng v???i bi???n t??? ?????nh ngh??a. V?? d??? nh?? ki???u
			 * DEPT trong khai b??o DEPT department;
			 */ {
				if (declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_typedef)
					return ISourcecodeFileParser.IS_PRIMITIVE_TYPEDEF_DECLARATION;
				else if (!astNode.getRawSignature().contains(ISourcecodeFileParser.METHOD_SIGNAL))
					return ISourcecodeFileParser.IS_VARIABLE_DECLARATION;
				else
					return ISourcecodeFileParser.IS_FUNCTION_AS_VARIABLE_DECLARATION;
			} else if (declSpecifier instanceof ICPPASTElaboratedTypeSpecifier) {
				ICPPASTElaboratedTypeSpecifier decl = (ICPPASTElaboratedTypeSpecifier) declSpecifier;
				if (((IASTDeclSpecifier) decl).getStorageClass() == IASTDeclSpecifier.sc_typedef)
					return ISourcecodeFileParser.IS_PRIMITIVE_TYPEDEF_DECLARATION;
			}
		}

		return ISourcecodeFileParser.IS_UNSPECIFIED_DECLARATION;
	}

	/**
	 * Ex:
	 * 
	 * <pre>
	 |    CourceClass::CourceClass() {}: CPPASTFunctionDefinition
	 |       : CPPASTSimpleDeclSpecifier
	 |       CourceClass::CourceClass(): CPPASTFunctionDeclarator
	 |          CourceClass::CourceClass: CPPASTQualifiedName
	 |             CourceClass: CPPASTName
	 |             CourceClass: CPPASTName
	 |       {}: CPPASTCompoundStatement
	 * </pre>
	 * 
	 * @param ast
	 * @return
	 */
	private boolean isConstructor(IASTFunctionDefinition ast) {
		boolean isConstructor = false;
		IASTFunctionDeclarator declarator = ast.getDeclarator();

		// Filter 1: Check whether parameters exist
		boolean hasAtLeastOneParameter = false;
		for (IASTNode child : declarator.getChildren())
			if (child instanceof CPPASTParameterDeclaration) {
				hasAtLeastOneParameter = true;
				break;
			}

		if (hasAtLeastOneParameter)
			isConstructor = false;
		else {
			// filter 2: check return type
			IASTDeclSpecifier decl = ast.getDeclSpecifier();

			if (decl.getRawSignature().equals("") /* no return type */) {
				IASTNode firstChild = declarator.getChildren()[0];
				// filter 3
				if (firstChild instanceof CPPASTQualifiedName) {
					IASTNode[] qualifiedNames = firstChild.getChildren();

					// filter 4.
					if (qualifiedNames.length == 1) {
						isConstructor = false;
					} else {
						IASTNode lastName = qualifiedNames[qualifiedNames.length - 1];
						IASTNode nextToLastName = qualifiedNames[qualifiedNames.length - 2];

						// filter 5
						if (lastName.getRawSignature().equals(nextToLastName.getRawSignature()))
							isConstructor = true;
						else
							isConstructor = false;
					}
				} else
					isConstructor = false;

			} else
				isConstructor = false;
		}
		return isConstructor;
	}

	/**
	 * Ex:
	 * 
	 * <pre>
	 |    CourceClass::~CourceClass() {}: CPPASTFunctionDefinition
	 |       : CPPASTSimpleDeclSpecifier
	 |       CourceClass::~CourceClass(): CPPASTFunctionDeclarator
	 |          CourceClass::~CourceClass: CPPASTQualifiedName
	 |             CourceClass: CPPASTName
	 |             ~CourceClass: CPPASTName
	 |       {}: CPPASTCompoundStatement
	 * </pre>
	 * 
	 * @param ast
	 * @return
	 */
	private boolean isDestructor(IASTFunctionDefinition ast) {
		boolean isConstructor = false;
		IASTFunctionDeclarator declarator = ast.getDeclarator();

		// Filter 1: Check whether parameters exist
		boolean hasAtLeastOneParameter = false;
		for (IASTNode child : declarator.getChildren())
			if (child instanceof CPPASTParameterDeclaration) {
				hasAtLeastOneParameter = true;
				break;
			}

		if (hasAtLeastOneParameter)
			isConstructor = false;
		else {
			// filter 2: check return type
			IASTDeclSpecifier decl = ast.getDeclSpecifier();

			if (decl.getRawSignature().equals("") /* no return type */) {
				IASTNode firstChild = declarator.getChildren()[0];
				// filter 3
				if (firstChild instanceof CPPASTQualifiedName) {
					IASTNode[] qualifiedNames = firstChild.getChildren();

					// filter 4.
					if (qualifiedNames.length == 1) {
						isConstructor = false;
					} else {
						IASTNode lastName = qualifiedNames[qualifiedNames.length - 1];
						IASTNode nextToLastName = qualifiedNames[qualifiedNames.length - 2];

						// filter 5
						// Ex: CourceClass::~CourceClass
						if (nextToLastName.getRawSignature().equals("~" + lastName.getRawSignature()))
							isConstructor = true;
						else
							isConstructor = false;
					}
				} else
					isConstructor = false;

			} else
				isConstructor = false;
		}
		return isConstructor;
	}

	private void normalizeFile(File file) {
		String statement = Utils.readFileContent(file);

		List<AbstractNormalizer> normalizers = new ArrayList<>();
		Cpp11ClassNormalizer cpp11Norm = new Cpp11ClassNormalizer();
		cpp11Norm.setOriginalSourcecode(statement);
		normalizers.add(cpp11Norm);

		for (AbstractNormalizer n : normalizers) {
			n.normalize();
			String normalizeSourcecode = n.getNormalizedSourcecode();
			if (file.exists() && !normalizeSourcecode.equals(statement))
				Utils.writeContentToFile(normalizeSourcecode, file.getAbsolutePath());
		}
	}

	public IASTTranslationUnit getTranslationUnit() {
		return translationUnit;
	}

	public void setTranslationUnit(IASTTranslationUnit translationUnit) {
		this.translationUnit = translationUnit;
	}

	@Override
	public File getSourcecodeFile() {
		return new File(sourcecodeNode.getAbsolutePath());
	}

	@Override
	public void setSourcecodeFile(File sourcecodeFile) {
		// nothing to do
	}

	public ISourcecodeFileNode getSourcecodeNode() {
		return sourcecodeNode;
	}

	public void setSourcecodeNode(SourcecodeFileNode sourcecodeNode) {
		this.sourcecodeNode = sourcecodeNode;
	}

	/**
	 * Ch??ng ta c???n m???t stack l??u c??c node ???? th??m, v???i m???c ????ch t???o quan h??? cha -
	 * con gi???a c??c node
	 *
	 * @author DucAnh
	 */
	class CustomCppStack extends Stack<INode> {

		private static final long serialVersionUID = 1L;
		INode rootOfStack;

		public INode getRootOfStack() {
			return rootOfStack;
		}

		/**
		 * Khi th??m m???t node m???i v??o stack, ta t???o lu??n quan h??? cha - con v???i node tr?????c
		 * ????. Ngo??i ra, n???u node ???? l?? node ?????u ti??n th??m v??o stack, hi???n nhi??n node ????
		 * l?? root
		 */
		@Override
		public INode push(INode item) {
			if (size() == 0)
				rootOfStack = item;
			else if (item instanceof TemporaryNode) {
				// Khong xem xet virtual (temporary) Node
			} else {
				peek().getChildren().add(item);
				item.setParent(peek());
				item.setAbsolutePath(peek().getAbsolutePath() + File.separator + item.getNewType());

				if (item instanceof VariableNode) {

				}
			}
			return super.push(item);
		}
	}

	/**
	 * ????y l?? m???t node t???m th???i
	 *
	 * @author DucAnh
	 */
	class TemporaryNode extends Node {
		public TemporaryNode(String name) {
			setName(name);
		}
	}
}