package clang;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import clang.AST.BinaryOperator;
import clang.AST.CallExpr;
import clang.AST.CompoundAssignOperator;
import clang.AST.CompoundStmt;
import clang.AST.ConditionalOperator;
import clang.AST.Decl;
import clang.AST.DeclRefExpr;
import clang.AST.DeclStmt;
import clang.AST.ExplicitCastExpr;
import clang.AST.ForStmt;
import clang.AST.FunctionDecl;
import clang.AST.ImplicitCastExpr;
import clang.AST.IntegerLiteral;
import clang.AST.Node;
import clang.AST.ParmVarDecl;
import clang.AST.ReturnStmt;
import clang.AST.Stmt;
import clang.AST.TranslationUnitDecl;
import clang.AST.UnaryOperator;
import clang.AST.VarDecl;
import lang.c.obj.ast.ASTNode;
import lang.c.obj.ast.AddExpression;
import lang.c.obj.ast.AddressOfExpression;
import lang.c.obj.ast.AndExpression;
import lang.c.obj.ast.AssignmentAddExpression;
import lang.c.obj.ast.AssignmentAndExpression;
import lang.c.obj.ast.AssignmentDivExpression;
import lang.c.obj.ast.AssignmentExpression;
import lang.c.obj.ast.AssignmentLShiftExpression;
import lang.c.obj.ast.AssignmentModExpression;
import lang.c.obj.ast.AssignmentMulExpression;
import lang.c.obj.ast.AssignmentOrExpression;
import lang.c.obj.ast.AssignmentRShiftExpression;
import lang.c.obj.ast.AssignmentSubExpression;
import lang.c.obj.ast.AssignmentXorExpression;
import lang.c.obj.ast.BitwiseAndExpression;
import lang.c.obj.ast.BitwiseNotExpression;
import lang.c.obj.ast.BitwiseOrExpression;
import lang.c.obj.ast.BitwiseXorExpression;
import lang.c.obj.ast.CallExpression;
import lang.c.obj.ast.CompoundStatement;
import lang.c.obj.ast.Constant;
import lang.c.obj.ast.ConstantExpression;
import lang.c.obj.ast.Declaration;
import lang.c.obj.ast.DeclarationSpecifier;
import lang.c.obj.ast.DeclarationStatement;
import lang.c.obj.ast.DivExpression;
import lang.c.obj.ast.EQExpression;
import lang.c.obj.ast.Expression;
import lang.c.obj.ast.ExpressionStatement;
import lang.c.obj.ast.ExternalDeclaration;
import lang.c.obj.ast.ExternalDeclarationOrDefinition;
import lang.c.obj.ast.ForDeclStatement;
import lang.c.obj.ast.ForStatement;
import lang.c.obj.ast.FunctionDefinition;
import lang.c.obj.ast.GEQExpression;
import lang.c.obj.ast.GTExpression;
import lang.c.obj.ast.Identifier;
import lang.c.obj.ast.IdentifierDeclarator;
import lang.c.obj.ast.IdentifierExpression;
import lang.c.obj.ast.InitDeclarator;
import lang.c.obj.ast.InitializerExpression;
import lang.c.obj.ast.LEQExpression;
import lang.c.obj.ast.LShiftExpression;
import lang.c.obj.ast.LTExpression;
import lang.c.obj.ast.List;
import lang.c.obj.ast.ModExpression;
import lang.c.obj.ast.MulExpression;
import lang.c.obj.ast.NEQExpression;
import lang.c.obj.ast.NotExpression;
import lang.c.obj.ast.Opt;
import lang.c.obj.ast.OrExpression;
import lang.c.obj.ast.PointerDereferenceExpression;
import lang.c.obj.ast.PostDecrementExpression;
import lang.c.obj.ast.PostIncrementExpression;
import lang.c.obj.ast.PreDecrementExpression;
import lang.c.obj.ast.PreIncrementExpression;
import lang.c.obj.ast.RShiftExpression;
import lang.c.obj.ast.Register;
import lang.c.obj.ast.Statement;
import lang.c.obj.ast.Static;
import lang.c.obj.ast.SubExpression;
import lang.c.obj.ast.TranslationUnit;
import lang.c.obj.ast.UnaryMinusExpression;
import lang.c.obj.ast.UnaryPlusExpression;
import lang.c.obj.ast.UnknownDeclaration;
import lang.c.obj.ast.UnknownStatement;
import lang.c.obj.ast.UnknownTypeSpecifier;


public class ClangASTTranslator implements ASTVisitor {
	private Map<AST.Node, ASTNode> nodeMap = new HashMap<>();

	private void t(AST.Node node, ASTNode internalNode) {
		internalNode.setStart(node.range.begin.line, node.range.begin.col);
		internalNode.setEnd(node.range.end.line, node.range.end.col);

		nodeMap.put(node, internalNode);
	}

	private <T> T t(AST.Node node) {
		return (T) nodeMap.get(node);
	}

	private <T extends ASTNode<ASTNode>> Opt<T> opt(AST.Node node) {
		if (node == null)
			return new Opt<T>();
		ASTNode n = (T) nodeMap.get(node);
		assert n != null;
		return new Opt(n);
	}

	private <T extends ASTNode<ASTNode>, U extends ASTNode<ASTNode>> Opt<T> opt(Opt<U> opt, Function<U, T> cons) {
		if (opt.getNumChild() == 0) {
			return new Opt<T>();
		} else {
			return new Opt<T>(cons.apply(opt.getChild(0)));
		}
	}

	private Statement stmt(ASTNode n) {
		if (n instanceof Expression) {
			return new ExpressionStatement((Expression) n);
		} else {
			return (Statement) n;
		}
	}

	@Override public void visit(Node n) { }
	@Override public void visit(CallExpr e) {
		Expression callee = t(e.getCallee());
		List<Expression> args = new List<>();
		for (int i = 0; i < e.getNumArgs(); ++i)
			args.add(t(e.getArg(i)));
		t(e, new CallExpression(callee, args));
	}

	@Override public void visit(BinaryOperator b) {
		Expression lhs = t(b.getLHS());
		Expression rhs = t(b.getRHS());

		BiFunction<Expression, Expression, Expression> c = null;

		switch (b.opcode) {
		case "+": c = AddExpression::new; break;
		case "-": c = SubExpression::new; break;
		case "*": c = MulExpression::new; break;
		case "/": c = DivExpression::new; break;
		case "%": c = ModExpression::new; break;
		case ">>": c = RShiftExpression::new; break;
		case "<<": c = LShiftExpression::new; break;
		case "<": c = LTExpression::new; break;
		case ">": c= GTExpression::new; break;
		case "<=": c = LEQExpression::new; break;
		case ">=": c = GEQExpression::new; break;
		case "==": c = EQExpression::new; break;
		case "!=": c = NEQExpression::new; break;
		case "&" : c = BitwiseAndExpression::new; break;
		case "^" : c = BitwiseXorExpression::new; break;
		case "|" : c = BitwiseOrExpression::new; break;
		case "&&" : c = AndExpression::new; break;
		case "||" : c = OrExpression::new; break;
		case "=" : c = AssignmentExpression::new; break;
		default: throw new RuntimeException("Opcode not yet supported: " + b.opcode);
		}

		t(b, c.apply(lhs, rhs));
	}

	@Override public void visit(CompoundAssignOperator b) {
		Expression lhs = t(b.getLHS());
		Expression rhs = t(b.getRHS());

		BiFunction<Expression, Expression, Expression> c = null;

		switch (b.opcode) {
		case "+=": c = AssignmentAddExpression::new; break;
		case "-=": c = AssignmentSubExpression::new; break;
		case "*=": c = AssignmentMulExpression::new; break;
		case "/=": c = AssignmentDivExpression::new; break;
		case "%=": c = AssignmentModExpression::new; break;
		case ">>=": c = AssignmentRShiftExpression::new; break;
		case "<<=": c = AssignmentLShiftExpression::new; break;
		case "^=": c = AssignmentXorExpression::new; break;
		case "&=" : c = AssignmentAndExpression::new; break;
		case "|=" : c = AssignmentOrExpression::new; break;
		default: throw new RuntimeException("Opcode not yet supported: " + b.opcode);
		}

		t(b, c.apply(lhs, rhs));
	}
	@Override public void visit(ConditionalOperator c) { }
	@Override public void visit(UnaryOperator u) {
		Expression opd = t(u.getOperand());
		Function<Expression, Expression> c = null;

		switch (u.opcode) {
		case "++": c = u.isPostfix ? PostIncrementExpression::new : PreIncrementExpression::new; break;
		case "--": c = u.isPostfix ? PostDecrementExpression::new : PreDecrementExpression::new; break;
		case "&": c = AddressOfExpression::new; break;
		case "*": c = PointerDereferenceExpression::new; break;
		case "+": c = UnaryPlusExpression::new; break;
		case "-": c = UnaryMinusExpression::new; break;
		case "!": c = NotExpression::new; break;
		case "~": c = BitwiseNotExpression::new; break;
		default: throw new RuntimeException("Opcode not yet supported: " + u.opcode);
		}

		t(u, c.apply(opd));
	}
	@Override public void visit(DeclRefExpr e) {
		t(e, new IdentifierExpression(new Identifier(e.getDecl().name)));
	}

	@Override public void visit(ExplicitCastExpr e) {
	}

	@Override public void visit(ImplicitCastExpr e) {
		Expression operand = t(e.getOperand());
		t(e, operand);
	}

	@Override public void visit(IntegerLiteral n) {
		t(n, new ConstantExpression(new Constant()));
	}

	@Override public void visit(Stmt s) {
		List<ASTNode> children = new List<>();
		for (Node c : s.children()) {
			if (c != null) {
				ASTNode tc = t(c);
				if (tc != null)
					children.add(tc);
			}
		}
		t(s, new UnknownStatement(children));
	}

	@Override public void visit(ForStmt f) {
		ASTNode init = t(f.getInit());
		Opt<Expression> cond = opt(f.getCond());
		Opt<Expression> incr = opt(f.getIncr());
		Statement body = t(f.getBody());

		if (init == null) {
			t(f, new ForStatement(new Opt(), cond, incr, body));
		} else if (init instanceof Expression) {
			t(f, new ForStatement(new Opt(init), cond, incr, body));
		} else if (init instanceof DeclarationStatement) {
			t(f, new ForDeclStatement((DeclarationStatement) init, cond, incr, body));
		} else {
			visit((Stmt) f);
		}

	}
	@Override public void visit(CompoundStmt c) {
		List<Statement> stmts = new List<>();
		for (int i = 0; i < c.getNumStmts(); ++i) {
			stmts.add(stmt(t(c.getStmt(i))));
		}
		t(c, new CompoundStatement(stmts));
	}

	@Override public void visit(ReturnStmt r) {
		visit((Stmt) r);
	}

	@Override public void visit(DeclStmt r) {
		if (r.getNumDecl() == 1) {
			t(r, new DeclarationStatement(t(r.getDecl(0))));
		} else {
			visit((Stmt) r);
		}
	}

	@Override public void visit(Decl d) {
		List<ASTNode> children = new List<>();
		for (Node c : d.children()) {
			if (c != null) {
				ASTNode tc = t(c);
				if (tc != null)
					children.add(tc);
			}
		}

		t(d, new UnknownDeclaration(new List(), new List(), children));
	}

	@Override public void visit(ParmVarDecl p) {
		visit((Decl) p);
	}

	@Override public void visit(FunctionDecl f) {
		visit((Decl) f);
	}

	@Override public void visit(VarDecl v) {
		List<DeclarationSpecifier> scSpecs = new List<>();
		if (v.storageClass != null) {
			switch (v.storageClass) {
			case "static":
				scSpecs.add(new Static());
				break;
			case "register":
				scSpecs.add(new Register());
				break;
			default:
				// do nothing
				break;
			}
		}


		Opt<Expression> init = opt(v.getInit());
		Opt initExpr = opt(init, InitializerExpression::new);
		IdentifierDeclarator id = new IdentifierDeclarator(new Opt(), new Identifier(v.name));
		InitDeclarator initDecl = new InitDeclarator(id, initExpr);
		UnknownTypeSpecifier typeSpec = new UnknownTypeSpecifier(v.type.qualType);

		scSpecs.add(typeSpec);
		Declaration d = new Declaration(scSpecs, new List().add(initDecl));
		t(v, d);
	}

	@Override public void visit(TranslationUnitDecl tu) {
		List<ExternalDeclarationOrDefinition> declOrDef = new List<>();
		for (int i = 0; i < tu.getNumDecl(); ++i) {
			ASTNode tn = t(tu.getDecl(i));
			if (tn instanceof Declaration) {
				declOrDef.add(new ExternalDeclaration((Declaration) tn));
			} else {
				declOrDef.add((FunctionDefinition) tn);
			}
		}
		t(tu, new TranslationUnit(declOrDef));
	}

	public ASTNode translate(AST.Node root) {
		nodeMap.clear();
		root.acceptPO(this);
		return nodeMap.get(root);
	}


	interface Function4<T0, T1, T2, T3, R> {
		R apply(T0 t0, T1 t1, T2 t2, T3 t3);
	}

	interface Function3<T0, T1, T2, R> {
		R apply(T0 t0, T1 t1, T2 t2);
	}


	class Builder {
		public <T0, T1, T2, R> Expect3<T0, T1, T2, R> build(Function3<T0, T1, T2, R> f) {
			Expect3<T0, T1, T2, R> ret = new Expect3<>();
			ret.f = f;
			return ret;
		}


		public <T0, T1, R> Expect2<T0, T1, R> build(BiFunction<T0, T1, R> f) {
			Expect2<T0, T1, R> ret = new Expect2<>();
			ret.f = f;
			return ret;
		}

		public <T0, R> Expect1<T0, R> build(Function<T0, R> f) {
			Expect1<T0, R> ret = new Expect1<>();
			ret.f = f;
			return ret;
		}
	}

	class Expect3<T0, T1, T2, R> {
		Function3<T0, T1, T2, R> f;
		Expect2<T1, T2, R> bind(T0 t0) {
			Expect2<T1, T2, R> ret = new Expect2<>();
			ret.f = (t1, t2) -> f.apply(t0, t1, t2);
			return ret;
		}
	}

	class Expect2<T1, T2, R> {
		BiFunction<T1, T2, R> f;
		Expect1<T2, R> bind(T1 t1) {
			Expect1<T2, R> ret = new Expect1<>();
			ret.f = t2 -> f.apply(t1, t2);
			return ret;
		}
	}

	class Expect1<T2, R> {
		Function<T2, R> f;
		Expect0<R> bind(T2 t2) {
			Expect0<R> ret = new Expect0<>();
			ret.f = () -> f.apply(t2);
			return ret;
		}
	}

	class Expect0<R> {
		Supplier<R> f;
		R done() {
			return f.get();
		}
	}
}
