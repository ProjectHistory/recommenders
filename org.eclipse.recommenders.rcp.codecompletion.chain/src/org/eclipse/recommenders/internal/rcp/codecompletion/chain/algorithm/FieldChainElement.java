/**
 * Copyright (c) 2010 Gary Fritz, Andreas Kaluza, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gary Fritz - initial API and implementation.
 *    Andreas Kaluza - modified implementation to use WALA
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm;

import java.io.UTFDataFormatException;

import org.eclipse.jdt.internal.ui.JavaPlugin;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.TypeReference;

/**
 * This class is part of the internal call chain. It represents a field for
 * which the completion and the field references are stored.
 */
@SuppressWarnings("restriction")
public class FieldChainElement implements IChainElement {
  private String completion;

  private final TypeReference fieldReference;

  private final IClassHierarchy classHierarchy;

  private IClass returnType;

  public FieldChainElement(final IField field) {
    try {
      completion = field.getName().toUnicodeString();
    } catch (final UTFDataFormatException e) {
      completion = field.getName().toString();
      JavaPlugin.log(e);
    }
    fieldReference = field.getFieldTypeReference();
    classHierarchy = field.getClassHierarchy();
    if (fieldReference.isPrimitiveType()) {
      returnType = null;
    }
    returnType = classHierarchy.lookupClass(fieldReference);
  }

  @Override
  public ChainElementType getElementType() {
    return ChainElementType.FIELD;
  }

  @Override
  public String getCompletion() {
    return completion;
  }

  @Override
  public TypeReference getResultingType() {
    return fieldReference;
  }

  @Override
  public IClass getType() {
    return returnType;
  }
}