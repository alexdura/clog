package clang;

import static clang.AST.*;

public interface ASTVisitor {
	void visit(Node n);

	void visit(Expr e);
	void visit(CallExpr e);
	void visit(BinaryOperator b);
	void visit(CompoundAssignOperator c);
	void visit(ConditionalOperator c);
	void visit(UnaryOperator u);
	void visit(DeclRefExpr e);
	void visit(ExplicitCastExpr e);
	void visit(ImplicitCastExpr e);
	void visit(IntegerLiteral n);
	void visit(ArraySubscriptExpr a);

	void visit(Stmt s);
	void visit(ForStmt f);
	void visit(WhileStmt w);
	void visit(DoStmt d);
	void visit(CompoundStmt c);
	void visit(ReturnStmt r);
	void visit(DeclStmt d);
	void visit(IfStmt f);
	void visit(CXXForRangeStmt f);

	void visit(Decl d);
	void visit(ParmVarDecl p);
	void visit(FunctionDecl f);
	void visit(VarDecl d);
	void visit(RecordDecl d);
	void visit(FieldDecl f);
	void visit(TranslationUnitDecl tu);

	void visit(Type t);
	void visit(PointerType t);
	void visit(ParenType t);
	void visit(FunctionProtoType t);
	void visit(BuiltinType t);
	void visit(ArrayType t);
	//void visit(TypeMetaVariable t);

	void visit(Comment c);
}
