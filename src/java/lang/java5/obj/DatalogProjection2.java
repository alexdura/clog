package lang.java5.obj;

import java.lang.reflect.Method;
import java.net.IDN;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.extendj.ast.ASTNode;
import lang.relation.Relation;
import lang.io.StringUID;
import lang.relation.PseudoTuple;

abstract class ASTNodeTraversalPostorder {
    public void traverse(ASTNode<?> n) {
		for (int i = 0; i < n.getNumChildNoTransform(); ++i) {
			traverse(n.getChildNoTransform(i));
		}
		visit(n);
    }

    public abstract void visit(ASTNode<?> n);
}

public class DatalogProjection2 extends ASTNodeTraversalPostorder {
	HashMap<ASTNode<?>, Integer> nodeNumber = new HashMap<>();
	int currentNumber = 0;
	Relation datalogProjection = new Relation(5);

	private int nodeId(ASTNode<?> n) {
		assert nodeNumber.containsKey(n);
		return nodeNumber.get(n);
	}

	public Relation getRelation() {
		return datalogProjection;
	}


	@Override
	public void visit(ASTNode<?> n) {
		currentNumber++;
		assert !nodeNumber.containsKey(n);
		nodeNumber.put(n, currentNumber);

		datalogProjection.addTuples(toTuples(n));
		datalogProjection.addTuples(srcLoc(n));
	}

	private static String getRelation(ASTNode<?> n) {
		String nodeName = n.getClass().getName();
		String[] splitNodeName = nodeName.split("\\.");
		String relName = splitNodeName[splitNodeName.length - 1];
		return relName;
	}

	private static String getSourceFile(ASTNode<?> n) {
		// TODO: ExtendJ does not expose the source file as a public method,
		// but only the source location, which concatentates the file name
		// and the line number.
		String[] loc =  n.sourceLocation().split(":");
		return loc[0];
	}

	private List<PseudoTuple> srcLoc(ASTNode<?> n) {
		java.util.List<PseudoTuple> ret = new ArrayList<>();
		// record the source location
		{
			// ("SourceInfo, CurrentNodeId, StartLine, StartCol, FileName)
			lang.ast.StringConstant Kind = new lang.ast.StringConstant("SrcLocStart");
			lang.ast.IntConstant CurrentNodeId = new lang.ast.IntConstant("" + nodeId(n));
			lang.ast.IntConstant Line = new lang.ast.IntConstant("" + beaver.Symbol.getLine(n.getStart()));
			lang.ast.IntConstant Col = new lang.ast.IntConstant("" + beaver.Symbol.getColumn(n.getStart()));
			lang.ast.StringConstant SrcFile = new lang.ast.StringConstant(getSourceFile(n));
			ret.add(new PseudoTuple(Kind, CurrentNodeId, Line, Col, SrcFile));
		}

		{
			// ("SourceInfo, CurrentNodeId, EndLine, EndCol, "")
			lang.ast.StringConstant Kind = new lang.ast.StringConstant("SrcLocEnd");
			lang.ast.IntConstant CurrentNodeId = new lang.ast.IntConstant("" + nodeId(n));
			lang.ast.IntConstant Line = new lang.ast.IntConstant("" + beaver.Symbol.getLine(n.getEnd()));
			lang.ast.IntConstant Col = new lang.ast.IntConstant("" + beaver.Symbol.getColumn(n.getEnd()));
			// avoid printing the source file once again to avoid bloating the output table
			lang.ast.StringConstant SrcFile = new lang.ast.StringConstant("");
			ret.add(new PseudoTuple(Kind, CurrentNodeId, Line, Col, SrcFile));
		}
		return ret;
	}

	/**
	   Helper class to match the type of the object and apply
	   an action.
	 */
	static class CastWrapper {
		private Object o;
		CastWrapper(Object o) {
			this.o = o;
		}
		<T> CastWrapper bind(Consumer<T> f) {
			if (o == null)
				return null;
			try {
				f.accept((T)o);
				return new CastWrapper(null);
			} catch (ClassCastException e) {
				return this;
			}
		}
	}

	private List<String> tokens(ASTNode<?> n) {
		ArrayList<String> r = new ArrayList<>();

		CastWrapper w = new CastWrapper(n);
		w.bind((org.extendj.ast.Literal l) -> r.add(l.getLITERAL()))
			.bind((org.extendj.ast.Literal l) -> r.add(l.getLITERAL()))
			.bind((org.extendj.ast.CompilationUnit l) -> r.add(l.getPackageDecl()))
			.bind((org.extendj.ast.PackageAccess l) -> r.add(l.getPackage()))
			.bind((org.extendj.ast.IdUse id) -> r.add(id.getID()))
			.bind((org.extendj.ast.Modifier m) -> r.add(m.getID()))
			.bind((org.extendj.ast.LabeledStmt s) -> r.add(s.getLabel()))
			.bind((org.extendj.ast.BreakStmt b) -> r.add(b.getLabel()))
			.bind((org.extendj.ast.ContinueStmt c) -> r.add(c.getLabel()))
			.bind((org.extendj.ast.ParseName p) -> {
					// TODO: In ExtendJ ParseName.nameParts is not accessible; resort to
					// spliting the string as a workaround
					String[] parts = p.name().split("\\.");
					r.addAll(Arrays.asList(parts));
				})
			.bind((org.extendj.ast.ASTNode nn) -> {
					try {
						Method m = nn.getClass().getMethod("getID");
						String id = (String)m.invoke(this);
						assert id != null;
						r.add(id);
					} catch (ReflectiveOperationException e) {
						// do nothing
					}
				});
		return r;
	}

	private List<PseudoTuple> toTuples(ASTNode<?> n) {
		String relName = getRelation(n);
		java.util.List<PseudoTuple> ret = new ArrayList<>();

		// the children in the tree
		int childIndex = 0;
		for (int i = 0; i < n.getNumChildNoTransform(); ++i) {
			ASTNode<?> child = n.getChildNoTransform(i);

			lang.ast.StringConstant Kind = new lang.ast.StringConstant(relName);
			lang.ast.IntConstant ChildId  = new lang.ast.IntConstant("" + nodeId(child));
			lang.ast.IntConstant CurrentNodeId = new lang.ast.IntConstant("" + nodeId(n));
			lang.ast.IntConstant ChildIdx = new lang.ast.IntConstant("" + childIndex++);
			lang.ast.StringConstant Token = new lang.ast.StringConstant("");
			ret.add(new PseudoTuple(Kind, CurrentNodeId, ChildIdx, ChildId, Token));
		}

		// other tokens attached to the node
		for (String t : tokens(n)) {
			// For every token, we generate two tuples
			// ("NodeKind", CurrentNodeId, ChildIdx, ChildId, "")
			// ("Token", ChildId, 0, 0, "TokenAsString")
			int tokenUID = StringUID.getInstance().uid(t);
			{
				// Add a tuple to the current node relation
				lang.ast.StringConstant Kind = new lang.ast.StringConstant(relName);
				lang.ast.IntConstant ChildId = new lang.ast.IntConstant("" + tokenUID);
				lang.ast.IntConstant CurrentNodeId = new lang.ast.IntConstant("" + nodeId(n));
				lang.ast.IntConstant ChildIdx = new lang.ast.IntConstant("" + childIndex++);
				lang.ast.StringConstant Token = new lang.ast.StringConstant("");
				ret.add(new PseudoTuple(Kind, CurrentNodeId, ChildIdx, ChildId, Token));
			}

			{
				// Add a tuple to Token relation
				lang.ast.StringConstant Kind = new lang.ast.StringConstant("Terminal");
				lang.ast.IntConstant ChildId = new lang.ast.IntConstant("0");
				lang.ast.IntConstant CurrentNodeId = new lang.ast.IntConstant("" + tokenUID);
				lang.ast.IntConstant ChildIdx = new lang.ast.IntConstant("0");
				lang.ast.StringConstant Token = new lang.ast.StringConstant(t);
				ret.add(new PseudoTuple(Kind, CurrentNodeId, ChildIdx, ChildId, Token));
			}
		}

		if (childIndex == 0) {
			// This node has no children, emit that
			lang.ast.StringConstant Kind = new lang.ast.StringConstant(relName);
			lang.ast.IntConstant ChildId  = new lang.ast.IntConstant("-1");
			lang.ast.IntConstant CurrentNodeId = new lang.ast.IntConstant("" + nodeId(n));
			lang.ast.IntConstant ChildIdx = new lang.ast.IntConstant("-1");
			lang.ast.StringConstant Token = new lang.ast.StringConstant("");
			ret.add(new PseudoTuple(Kind, CurrentNodeId, ChildIdx, ChildId, Token));
		}

		return ret;
	}
}