/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package clang.swig;

public class ClangClog {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected ClangClog(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ClangClog obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        clogJNI.delete_ClangClog(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  static public class Loc {
    private transient long swigCPtr;
    protected transient boolean swigCMemOwn;
  
    protected Loc(long cPtr, boolean cMemoryOwn) {
      swigCMemOwn = cMemoryOwn;
      swigCPtr = cPtr;
    }
  
    protected static long getCPtr(Loc obj) {
      return (obj == null) ? 0 : obj.swigCPtr;
    }
  
    @SuppressWarnings("deprecation")
    protected void finalize() {
      delete();
    }
  
    public synchronized void delete() {
      if (swigCPtr != 0) {
        if (swigCMemOwn) {
          swigCMemOwn = false;
          clogJNI.delete_ClangClog_Loc(swigCPtr);
        }
        swigCPtr = 0;
      }
    }
  
    public void setFilename(String value) {
      clogJNI.ClangClog_Loc_Filename_set(swigCPtr, this, value);
    }
  
    public String getFilename() {
      return clogJNI.ClangClog_Loc_Filename_get(swigCPtr, this);
    }
  
    public void setStartLine(long value) {
      clogJNI.ClangClog_Loc_StartLine_set(swigCPtr, this, value);
    }
  
    public long getStartLine() {
      return clogJNI.ClangClog_Loc_StartLine_get(swigCPtr, this);
    }
  
    public void setStartCol(long value) {
      clogJNI.ClangClog_Loc_StartCol_set(swigCPtr, this, value);
    }
  
    public long getStartCol() {
      return clogJNI.ClangClog_Loc_StartCol_get(swigCPtr, this);
    }
  
    public void setEndLine(long value) {
      clogJNI.ClangClog_Loc_EndLine_set(swigCPtr, this, value);
    }
  
    public long getEndLine() {
      return clogJNI.ClangClog_Loc_EndLine_get(swigCPtr, this);
    }
  
    public void setEndCol(long value) {
      clogJNI.ClangClog_Loc_EndCol_set(swigCPtr, this, value);
    }
  
    public long getEndCol() {
      return clogJNI.ClangClog_Loc_EndCol_get(swigCPtr, this);
    }
  
    public Loc() {
      this(clogJNI.new_ClangClog_Loc__SWIG_0(), true);
    }
  
    public Loc(String Filename, long StartLine, long StartCol, long EndLine, long EndCol) {
      this(clogJNI.new_ClangClog_Loc__SWIG_1(Filename, StartLine, StartCol, EndLine, EndCol), true);
    }
  
  }

  public ClangClog(SWIGTYPE_p_clang__tooling__CompilationDatabase CDB, VectorString Srcs) {
    this(clogJNI.new_ClangClog(SWIGTYPE_p_clang__tooling__CompilationDatabase.getCPtr(CDB), VectorString.getCPtr(Srcs), Srcs), true);
  }

  public boolean init() {
    return clogJNI.ClangClog_init(swigCPtr, this);
  }

  public long registerMatcher(String Matcher, boolean IsGlobal) {
    return clogJNI.ClangClog_registerMatcher(swigCPtr, this, Matcher, IsGlobal);
  }

  public void runGlobalMatchers() {
    clogJNI.ClangClog_runGlobalMatchers(swigCPtr, this);
  }

  public VectorVectorLong matchFromRoot(long MatcherId) {
    return new VectorVectorLong(clogJNI.ClangClog_matchFromRoot(swigCPtr, this, MatcherId), true);
  }

  public VectorVectorLong matchFromNode(long MatcherId, long NodeId) {
    return new VectorVectorLong(clogJNI.ClangClog_matchFromNode(swigCPtr, this, MatcherId, NodeId), true);
  }

  public ClangClog.Loc srcLocation(long NodeId) {
    return new ClangClog.Loc(clogJNI.ClangClog_srcLocation(swigCPtr, this, NodeId), true);
  }

  public long type(long NodeId) {
    return clogJNI.ClangClog_type(swigCPtr, this, NodeId);
  }

  public long decl(long NodeId) {
    return clogJNI.ClangClog_decl(swigCPtr, this, NodeId);
  }

  public VectorLong parent(long NodeId) {
    return new VectorLong(clogJNI.ClangClog_parent(swigCPtr, this, NodeId), true);
  }

}
