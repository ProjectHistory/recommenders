/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Kaluza, Marko Martin, Marcel Bruch - chain completion test scenario definitions 
 */
package data;

import java.io.File;

public class CompletionOnMemberCallChainDepth2 {
    class A {
        public B b = new B();

        class B {
            public File findMe = new File("");
        }
    }
    
    //@start
    A a = new A();
    File c = <^Space|a.b.findMe.*>
    //@end
    //A a = new A();
    //File c = a.b.findMe

    public CompletionOnMemberCallChainDepth2(){
        final A a = new A();
        final File c =<@Ignore^Space>
    }
    
    public void method() {
        final A a = new A();
        final File c =<@Ignore^Space>
    }
}