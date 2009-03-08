/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
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






package soot.jimple.internal;

import soot.*;
import soot.jimple.*;
import soot.util.*;
import java.util.*;

public class JSpecialInvokeExpr extends AbstractSpecialInvokeExpr 
{
    public JSpecialInvokeExpr(Local base, SootMethod method, List args)
    {
        super(Jimple.v().newLocalBox(base), method,
             (ValueBox[]) new ImmediateBox[args.size()]);

        for(int i = 0; i < args.size(); i++) {
            this.argBoxes[i] = Jimple.v().newImmediateBox((Value) args.get(i));
        }
    }
    
    JSpecialInvokeExpr(Local base, SootMethod method, ValueBox[] args)
    {
        super(Jimple.v().newLocalBox(base), method, args);
    }
    
 
    public Object clone() 
    {
        ArrayList clonedArgs = new ArrayList(getArgCount());
 
        for(int i = 0; i < getArgCount(); i++) {
            clonedArgs.add(i, getArg(i));
        }
        
        return new  JSpecialInvokeExpr((Local) getBase(), method, clonedArgs);
    }
}