package lang;



import lang.ast.FormalPredicate;
import lang.ast.FormalPredicateMap;
import lang.ast.Program;

import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.collections4.SetUtils;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class DomainSeparationTest {
	private static Program loadAndCompile(String file) {
		String path = "./tests/domain-separation/" + file;
		CmdLineOpts opts = new CmdLineOpts();
		opts.setAction(CmdLineOpts.Action.CHECK);
		opts.setInputFile(path);
		opts.setLang(CmdLineOpts.Lang.JAVA);

		try {
			Program p = Compiler.parseProgram(opts);
			Compiler.checkProgram(p, opts);
			return p;
		} catch (Exception e) {
			return null;
		}
	}

	@Test public void testLocalPredicates() {
		Program p = loadAndCompile("domain.mdl");
		FormalPredicateMap predMap = p.formalPredicateMap();
		FormalPredicate Name = predMap.get("Name");
		assertTrue(Name.hasLocalDef());
		assertFalse(Name.hasGlobalDef());
		assertTrue(Name.isASTPredicate());

		FormalPredicate A1 = predMap.get("A1");
		assertFalse(A1.hasLocalDef());
		assertTrue(A1.hasGlobalDef());
		assertTrue(A1.isASTPredicate());

		FormalPredicate A2 = predMap.get("A2");
		assertTrue(A2.hasGlobalDef());
		assertFalse(A2.hasLocalDef());
		assertTrue(A2.isASTPredicate());

		FormalPredicate A3 = predMap.get("A3");
		assertTrue(A3.hasLocalDef());
		assertFalse(A3.hasGlobalDef());
		assertTrue(A3.isASTPredicate());

		FormalPredicate B = predMap.get("B");
		assertFalse(B.hasLocalDef());
		assertTrue(B.hasGlobalDef());
		assertTrue(B.isASTPredicate());

		FormalPredicate C = predMap.get("C");
		assertTrue(C.hasLocalDef());
		assertFalse(C.hasGlobalDef());
		assertTrue(C.isASTPredicate());

		FormalPredicate Pat = predMap.get("Pat");
		assertTrue(Pat.hasLocalDef());
		assertFalse(Pat.hasGlobalDef());
		assertTrue(Pat.isASTPredicate());
	}

	@Test public void testCircularDeps() {
		Program p = loadAndCompile("circular.mdl");

		FormalPredicateMap predMap = p.formalPredicateMap();
		FormalPredicate B = predMap.get("B");
		FormalPredicate C = predMap.get("C");
		assertEquals(SetUtils.<Set<Integer>>unmodifiableSet(SetUtils.unmodifiableSet(0, 1)), B.domainSignature().equivalenceSets());
		assertEquals(SetUtils.<Set<Integer>>unmodifiableSet(SetUtils.unmodifiableSet(0, 1)), C.domainSignature().equivalenceSets());
		assertTrue(B.isASTPredicate());
		assertTrue(C.isASTPredicate());
		// B and C use the DECL attribute, which is global
		assertTrue(B.hasLocalDef());
		assertFalse(B.hasGlobalDef());
		assertTrue(C.hasLocalDef());
		assertFalse(C.hasGlobalDef());
	}

	@Test public void testLocal1() {
		Program p = loadAndCompile("locals1.mdl");

		FormalPredicateMap predMap = p.formalPredicateMap();
		FormalPredicate P = predMap.get("P");
		assertEquals(P.localTerms(), Collections.emptySet());

		FormalPredicate Q = predMap.get("Q");
		assertEquals(Q.localTerms(), SetUtils.unmodifiableSet(0));

		FormalPredicate R = predMap.get("R");
		assertEquals(R.localTerms(), SetUtils.unmodifiableSet(2));

		FormalPredicate S = predMap.get("S");
		assertEquals(S.localTerms(), Collections.emptySet());

		FormalPredicate T = predMap.get("T");
		assertEquals(T.localTerms(), SetUtils.unmodifiableSet(0, 1));

		FormalPredicate U = predMap.get("U");
		assertEquals(U.localTerms(), SetUtils.unmodifiableSet(0));

		FormalPredicate V = predMap.get("V");
		assertEquals(V.localTerms(), Collections.emptySet());

		FormalPredicate W = predMap.get("W");
		assertEquals(W.localTerms(), SetUtils.unmodifiableSet(0));
	}
}
