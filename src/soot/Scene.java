/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */


package soot;
import soot.*;
import soot.options.*;


import soot.util.*;
import java.util.*;
import java.io.*;
import soot.jimple.toolkits.invoke.*;
import soot.jimple.toolkits.callgraph.*;
import soot.jimple.toolkits.pointer.*;
import soot.toolkits.exceptions.ThrowAnalysis;
import soot.toolkits.exceptions.PedanticThrowAnalysis;
import soot.toolkits.exceptions.UnitThrowAnalysis;

/** Manages the SootClasses of the application being analyzed. */
public class Scene  //extends AbstractHost
{
    public Scene ( Singletons.Global g )
    {
    	setReservedNames();
    	
        // load soot.class.path system property, if defined
        String scp = System.getProperty("soot.class.path");

        if (scp != null)
            setSootClassPath(scp);
    }
    public static Scene  v() { return G.v().soot_Scene (); }

    Chain classes = new HashChain();
    Chain applicationClasses = new HashChain();
    Chain libraryClasses = new HashChain();
    Chain phantomClasses = new HashChain();
    
    private Map nameToClass = new HashMap();

    Numberer typeNumberer = new Numberer();
    Numberer methodNumberer = new Numberer();
    Numberer fieldNumberer = new Numberer();
    Numberer classNumberer = new Numberer();
    StringNumberer subSigNumberer = new StringNumberer();
    Numberer localNumberer = new Numberer();

    private Hierarchy activeHierarchy;
    private FastHierarchy activeFastHierarchy;
    private CallGraph activeCallGraph;
    private ReachableMethods reachableMethods;
    private PointsToAnalysis activePointsToAnalysis;
    private SideEffectAnalysis activeSideEffectAnalysis;
    private List entryPoints;

    boolean allowsPhantomRefs = false;

    // temporary for testing cfgs in plugin
    
    public ArrayList cfgList = new ArrayList();
    
    SootClass mainClass;
    String sootClassPath = null;

    // Two default values for constructing ExceptionalUnitGraphs:
    private ThrowAnalysis defaultThrowAnalysis = null;
    private boolean alwaysAddEdgesFromExceptingUnits = false;
    
    public void setMainClass(SootClass m)
    {
        mainClass = m;
    }
    
    Set reservedNames = new HashSet();
    
    /**
        Returns a set of tokens which are reserved.  Any field, class, method, or local variable with such a name will be quoted.
     */
     
    public Set getReservedNames()
    {
        return reservedNames;
    }
    
    /**
        If this name is in the set of reserved names, then return a quoted version of it.  Else pass it through.
     */
    
    public String quotedNameOf(String s)
    {
        if(reservedNames.contains(s))
            return "\'" + s + "\'";
        else
            return s;
    }
    
    public SootClass getMainClass()
    {
        if(mainClass == null)
            throw new RuntimeException("There is no main class set!");
            
        return mainClass;
    }
    
    
    public void setSootClassPath(String p)
    {
        sootClassPath = p;
    }
    
    public String getSootClassPath()
    {
        if( sootClassPath == null ) {
            String optionscp = Options.v().soot_classpath();
            if( optionscp.length() > 0 )
                sootClassPath = optionscp;
        }
        if( sootClassPath == null ) {
            sootClassPath = System.getProperty("java.class.path")+File.pathSeparator+
                System.getProperty("java.home")+File.separator+
                "lib"+File.separator+"rt.jar";
        }
        return sootClassPath;
    }


    private int stateCount;
    public int getState() { return this.stateCount; }
    private void modifyHierarchy() {
        stateCount++;
        activeFastHierarchy = null;
        activeSideEffectAnalysis = null;
        activePointsToAnalysis = null;
    }

    public void addClass(SootClass c) 
    {
        if(c.isInScene())
            throw new RuntimeException("already managed: "+c.getName());

        if(containsClass(c.getName()))
            throw new RuntimeException("duplicate class: "+c.getName());

        classes.add(c);
        c.setLibraryClass();

        nameToClass.put(c.getName(), c.getType());
        c.getType().setSootClass(c);
        c.setInScene(true);
        modifyHierarchy();
    }

    public void removeClass(SootClass c)
    {
        if(!c.isInScene())
            throw new RuntimeException();

        classes.remove(c);
        c.getType().setSootClass(null);
        c.setInScene(false);
        modifyHierarchy();
    }

    public boolean containsClass(String className)
    {
        RefType type = (RefType) nameToClass.get(className);
        if( type == null ) return false;
        SootClass c = type.getSootClass();
        if( c == null ) return false;
        return c.isInScene();
    }

    public String signatureToClass(String sig) {
        if( sig.charAt(0) != '<' ) throw new RuntimeException("oops "+sig);
        if( sig.charAt(sig.length()-1) != '>' ) throw new RuntimeException("oops "+sig);
        int index = sig.indexOf( ":" );
        if( index < 0 ) throw new RuntimeException("oops "+sig);
        return sig.substring(1,index);
    }

    public String signatureToSubsignature(String sig) {
        if( sig.charAt(0) != '<' ) throw new RuntimeException("oops "+sig);
        if( sig.charAt(sig.length()-1) != '>' ) throw new RuntimeException("oops "+sig);
        int index = sig.indexOf( ":" );
        if( index < 0 ) throw new RuntimeException("oops "+sig);
        return sig.substring(index+2,sig.length()-1);
    }

    private SootField grabField(String fieldSignature)
    {
        String cname = signatureToClass( fieldSignature );
        String fname = signatureToSubsignature( fieldSignature );
        if( !containsClass(cname) ) return null;
        SootClass c = getSootClass(cname);
        if( !c.declaresField( fname ) ) return null;
        return c.getField( fname );
    }

    public boolean containsField(String fieldSignature)
    {
        return grabField(fieldSignature) != null;
    }
    
    private SootMethod grabMethod(String methodSignature)
    {
        String cname = signatureToClass( methodSignature );
        String mname = signatureToSubsignature( methodSignature );
        if( !containsClass(cname) ) return null;
        SootClass c = getSootClass(cname);
        if( !c.declaresMethod( mname ) ) return null;
        return c.getMethod( mname );
    }

    public boolean containsMethod(String methodSignature)
    {
        return grabMethod(methodSignature) != null;
    }

    public SootField getField(String fieldSignature)
    {
        SootField f = grabField( fieldSignature );
        if (f != null)
            return f;

        throw new RuntimeException("tried to get nonexistent field "+fieldSignature);
    }

    public SootMethod getMethod(String methodSignature)
    {
        SootMethod m = grabMethod( methodSignature );
        if (m != null)
            return m;
        throw new RuntimeException("tried to get nonexistent method "+methodSignature);
    }

    /** 
     * Loads the given class and all of the required support classes.  Returns the first class.
     */
     
    public SootClass loadClassAndSupport(String className) 
    {   
        /*
        if(Options.v().time())
            Main.v().resolveTimer.start();
        */
        
        Scene.v().setPhantomRefs(true);
        //SootResolver resolver = new SootResolver();
        SootResolver resolver = SootResolver.v();
        SootClass toReturn = resolver.resolveClassAndSupportClasses(className);
        Scene.v().setPhantomRefs(false);

        return toReturn;
        
        /*
        if(Options.v().time())
            Main.v().resolveTimer.end(); */
    }
    
    /**
     * Returns the RefType with the given className.  
     */
    public RefType getRefType(String className) 
    {
        return (RefType) nameToClass.get(className);
    }

    /**
     * Returns the RefType with the given className.  
     */
    public void addRefType(RefType type) 
    {
        nameToClass.put(type.getClassName(), type);
    }

    /**
     * Returns the SootClass with the given className.  
     */

    public SootClass getSootClass(String className) 
    {   
        RefType type = (RefType) nameToClass.get(className);
        SootClass toReturn = null;
        if( type != null ) toReturn = type.getSootClass();
        
        if(toReturn != null) {
	    return toReturn;
	} else  if(Scene.v().allowsPhantomRefs()) {            
	    SootClass c = new SootClass(className);
	    c.setPhantom(true);
	    addClass(c);
	    return c;
	}
	else {          
	    throw new RuntimeException( System.getProperty("line.separator") + "Aborting: can't find classfile " + className );            
	}
    }

    /**
     * Returns an backed chain of the classes in this manager.
     */
     
    public Chain getClasses()
    {
        return classes;
    }

    /* The four following chains are mutually disjoint. */

    /**
     * Returns a chain of the application classes in this scene.
     * These classes are the ones which can be freely analysed & modified.
     */
    public Chain getApplicationClasses()
    {
        return applicationClasses;
    }

    /**
     * Returns a chain of the library classes in this scene.
     * These classes can be analysed but not modified.
     */
    public Chain getLibraryClasses()
    {
        return libraryClasses;
    }

    /**
     * Returns a chain of the phantom classes in this scene.
     * These classes are referred to by other classes, but cannot be loaded.
     */
    public Chain getPhantomClasses()
    {
        return phantomClasses;
    }

    Chain getContainingChain(SootClass c)
    {
        if (c.isApplicationClass())
            return getApplicationClasses();
        else if (c.isLibraryClass())
            return getLibraryClasses();
        else if (c.isPhantomClass())
            return getPhantomClasses();

        return null;
    }

    /****************************************************************************/
    /**
        Retrieves the active side-effect analysis
     */

    public SideEffectAnalysis getSideEffectAnalysis() 
    {
        if(!hasSideEffectAnalysis()) {
	    setSideEffectAnalysis( new SideEffectAnalysis(
			getPointsToAnalysis(),
			getCallGraph() ) );
	}
            
        return activeSideEffectAnalysis;
    }
    
    /**
        Sets the active side-effect analysis
     */
     
    public void setSideEffectAnalysis(SideEffectAnalysis sea)
    {
        activeSideEffectAnalysis = sea;
    }

    public boolean hasSideEffectAnalysis()
    {
        return activeSideEffectAnalysis != null;
    }
    
    public void releaseSideEffectAnalysis()
    {
        activeSideEffectAnalysis = null;
    }

    /****************************************************************************/
    /**
        Retrieves the active pointer analysis
     */

    public PointsToAnalysis getPointsToAnalysis() 
    {
        if(!hasPointsToAnalysis()) {
	    return DumbPointerAnalysis.v();
	}
            
        return activePointsToAnalysis;
    }
    
    /**
        Sets the active pointer analysis
     */
     
    public void setPointsToAnalysis(PointsToAnalysis pa)
    {
        activePointsToAnalysis = pa;
    }

    public boolean hasPointsToAnalysis()
    {
        return activePointsToAnalysis != null;
    }
    
    public void releasePointsToAnalysis()
    {
        activePointsToAnalysis = null;
    }

    /****************************************************************************/
    /** Makes a new fast hierarchy is none is active, and returns the active
     * fast hierarchy. */
    public FastHierarchy getOrMakeFastHierarchy() {
	if(!hasFastHierarchy() ) {
	    setFastHierarchy( new FastHierarchy() );
	}
	return getFastHierarchy();
    }
    /**
        Retrieves the active fast hierarchy
     */

    public FastHierarchy getFastHierarchy() 
    {
        if(!hasFastHierarchy())
            throw new RuntimeException("no active FastHierarchy present for scene");
            
        return activeFastHierarchy;
    }
    
    /**
        Sets the active hierarchy
     */
     
    public void setFastHierarchy(FastHierarchy hierarchy)
    {
        activeFastHierarchy = hierarchy;
    }

    public boolean hasFastHierarchy()
    {
        return activeFastHierarchy != null;
    }
    
    public void releaseFastHierarchy()
    {
        activeFastHierarchy = null;
    }

    /****************************************************************************/
    /**
        Retrieves the active hierarchy
     */

    public Hierarchy getActiveHierarchy() 
    {
        if(!hasActiveHierarchy())
            //throw new RuntimeException("no active Hierarchy present for scene");
            setActiveHierarchy( new Hierarchy() );
            
        return activeHierarchy;
    }
    
    /**
        Sets the active hierarchy
     */
     
    public void setActiveHierarchy(Hierarchy hierarchy)
    {
        activeHierarchy = hierarchy;
    }

    public boolean hasActiveHierarchy()
    {
        return activeHierarchy != null;
    }
    
    public void releaseActiveHierarchy()
    {
        activeHierarchy = null;
    }

    /** Get the set of entry points that are used to build the call graph. */
    public List getEntryPoints() {
        if( entryPoints == null ) {
            entryPoints = EntryPoints.v().all();
        }
        return entryPoints;
    }

    /** Change the set of entry point methods used to build the call graph. */
    public void setEntryPoints( List entryPoints ) {
        this.entryPoints = entryPoints;
    }

    public CallGraph getCallGraph() 
    {
        if(!hasCallGraph()) {
            throw new RuntimeException( "No call graph present in Scene. Maybe you want Whole Program mode (-w)." );
        }
            
        return activeCallGraph;
    }
    
    public void setCallGraph(CallGraph cg)
    {
        reachableMethods = null;
        activeCallGraph = cg;
    }

    public boolean hasCallGraph()
    {
        return activeCallGraph != null;
    }
    
    public void releaseCallGraph()
    {
        activeCallGraph = null;
        reachableMethods = null;
    }
    public ReachableMethods getReachableMethods() {
        if( reachableMethods == null ) {
            reachableMethods = new ReachableMethods(
                    getCallGraph(), getEntryPoints() );
        }
        reachableMethods.update();
        return reachableMethods;
    }
    public void setReachableMethods( ReachableMethods rm ) {
        reachableMethods = rm;
    }
    public boolean hasReachableMethods() {
        return reachableMethods != null;
    }
    public void releaseReachableMethods() {
        reachableMethods = null;
    }
   
    public boolean getPhantomRefs()
    {
        if( !Options.v().allow_phantom_refs() ) return false;
        return allowsPhantomRefs;
    }

    public void setPhantomRefs(boolean value)
    {
        allowsPhantomRefs = value;
    }
    
    public boolean allowsPhantomRefs()
    {
        return getPhantomRefs();
    }
    public Numberer getTypeNumberer() { return typeNumberer; }
    public Numberer getMethodNumberer() { return methodNumberer; }
    public Numberer getFieldNumberer() { return fieldNumberer; }
    public Numberer getClassNumberer() { return classNumberer; }
    public StringNumberer getSubSigNumberer() { return subSigNumberer; }
    public Numberer getLocalNumberer() { return localNumberer; }

    /**
     * Returns the {@link ThrowAnalysis} to be used by default when
     * constructing CFGs which include exceptional control flow.
     *
     * @return the default {@link ThrowAnalysis}
     */
    public ThrowAnalysis getDefaultThrowAnalysis() 
    {
	if( defaultThrowAnalysis == null ) {
	    int optionsThrowAnalysis = Options.v().throw_analysis();
	    switch (optionsThrowAnalysis) {
	    case Options.throw_analysis_pedantic:
		defaultThrowAnalysis = PedanticThrowAnalysis.v();
		break;
	    case Options.throw_analysis_unit:
		defaultThrowAnalysis = UnitThrowAnalysis.v();
		break;
	    default:
		throw new IllegalStateException("Options.v().throw_analysi() == " +
						Options.v().throw_analysis());
	    }
	}
	return defaultThrowAnalysis;
    }

    /**
     * Sets the {@link ThrowAnalysis} to be used by default when
     * constructing CFGs which include exceptional control flow.
     *
     * @param the default {@link ThrowAnalysis}.
     */
    public void setDefaultThrowAnalysis(ThrowAnalysis ta) 
    {
	defaultThrowAnalysis = ta;
    }

    private void setReservedNames()
    {
        Set rn = getReservedNames();        
        rn.add("newarray");
        rn.add("newmultiarray");
        rn.add("nop");
        rn.add("ret");
        rn.add("specialinvoke");
        rn.add("staticinvoke");
        rn.add("tableswitch");
        rn.add("virtualinvoke");
        rn.add("null_type");
        rn.add("unknown");
        rn.add("cmp");
        rn.add("cmpg");
        rn.add("cmpl");
        rn.add("entermonitor");
        rn.add("exitmonitor");
        rn.add("interfaceinvoke");
        rn.add("lengthof");
        rn.add("lookupswitch");
        rn.add("neg");
        rn.add("if");
        rn.add("abstract");
        rn.add("boolean");
        rn.add("break");
        rn.add("byte");
        rn.add("case");
        rn.add("catch");
        rn.add("char");
        rn.add("class");
        rn.add("final");
        rn.add("native");
        rn.add("public");
        rn.add("protected");
        rn.add("private");
        rn.add("static");
        rn.add("synchronized");
        rn.add("transient");
        rn.add("volatile");
	rn.add("interface");
        rn.add("void");
        rn.add("short");
        rn.add("int");
        rn.add("long");
        rn.add("float");
        rn.add("double");
        rn.add("extends");
        rn.add("implements");
        rn.add("breakpoint");
        rn.add("default");
        rn.add("goto");
        rn.add("instanceof");
        rn.add("new");
        rn.add("return");
        rn.add("throw");
        rn.add("throws");
        rn.add("null");
        rn.add("from");
	rn.add("to");
    }

    public void loadNecessaryClasses() {
        Iterator it = Options.v().classes().iterator();

        while (it.hasNext()) {
            String name = (String) it.next();
            SootClass c;

            c = Scene.v().loadClassAndSupport(name);

            if (mainClass == null) {
                mainClass = c;
                Scene.v().setMainClass(c);
            }
            c.setApplicationClass();
        }

        HashSet dynClasses = new HashSet();
        dynClasses.addAll(Options.v().dynamic_class());

        for( Iterator pathIt = Options.v().dynamic_dir().iterator(); pathIt.hasNext(); ) {

            final String path = (String) pathIt.next();
            dynClasses.addAll(SourceLocator.v().getClassesUnder(path));
        }

        for( Iterator pkgIt = Options.v().dynamic_package().iterator(); pkgIt.hasNext(); ) {

            final String pkg = (String) pkgIt.next();
            dynClasses.addAll(SourceLocator.v().classesInDynamicPackage(pkg));
        }

        for( Iterator classNameIt = dynClasses.iterator(); classNameIt.hasNext(); ) {

            final String className = (String) classNameIt.next();
            Scene.v().loadClassAndSupport(className);
        }

        for( Iterator pathIt = Options.v().process_dir().iterator(); pathIt.hasNext(); ) {

            final String path = (String) pathIt.next();
            for( Iterator clIt = SourceLocator.v().getClassesUnder(path).iterator(); clIt.hasNext(); ) {
                final String cl = (String) clIt.next();
                Scene.v().loadClassAndSupport(cl).setApplicationClass();
            }
        }

        prepareClasses();
    }

    /* Generate classes to process, adding or removing package marked by
     * command line options.
     */
    private void prepareClasses() {

        LinkedList excludedPackages = new LinkedList();
        if (Options.v().exclude() != null)
            excludedPackages.addAll(Options.v().exclude());

        if( !Options.v().include_all() ) {
            excludedPackages.add("java.");
            excludedPackages.add("sun.");
            excludedPackages.add("javax.");
            excludedPackages.add("com.sun.");
            excludedPackages.add("com.ibm.");
            excludedPackages.add("org.xml.");
            excludedPackages.add("org.w3c.");
            excludedPackages.add("org.apache.");
        }

        // Remove/add all classes from packageInclusionMask as per -i option
        for( Iterator sIt = Scene.v().getClasses().iterator(); sIt.hasNext(); ) {
            final SootClass s = (SootClass) sIt.next();
            if( s.isPhantom() ) continue;
            if(Options.v().app()) {
                s.setApplicationClass();
            }
            if (Options.v().classes().contains(s.getName())) {
                s.setApplicationClass();
                continue;
            }
            for( Iterator pkgIt = excludedPackages.iterator(); pkgIt.hasNext(); ) {
                final String pkg = (String) pkgIt.next();
                if (s.isApplicationClass()
                && s.getPackageName().startsWith(pkg)) {
                        s.setLibraryClass();
                }
            }
            for( Iterator pkgIt = Options.v().include().iterator(); pkgIt.hasNext(); ) {
                final String pkg = (String) pkgIt.next();
                if (s.getPackageName().startsWith(pkg))
                    s.setApplicationClass();
            }
        }
    }

    ArrayList pkgList;

    public void setPkgList(ArrayList list){
        pkgList = list;
    }

    public ArrayList getPkgList(){
        return pkgList;
    }
    
}
