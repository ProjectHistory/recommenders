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

import helper.FileFindHelper;

import java.io.File;

//call chain 1 ok --> 1 element chain does not lead to expected solution 
public class CompletionOnMemberAccessInMethod {

    FileFindHelper helper = new FileFindHelper();

    public void method() {
        //@start
        final File c = <^Space|helper.findMe.*2 elements.*>
        //@end
        //final File c = helper.findMe
    }
}