package org.eclipse.recommenders.internal.rcp.codecompletion.chain;

import java.util.HashSet;
import java.util.Set;

/**
 * The context in which a proposal chain is created. Particularly, this includes
 * used variables.
 * 
 */
public class ProposalNameGenerator {

  private static final Set<String> variableNames = new HashSet<String>();

  private static char nextFreeVariableName = 'i';

  private static int freeVariableSequenceCounter = 1;

  /**
   * Marks the specified variable name as "used" so that
   * {@link #variableNameIsFree(String)} will return false for this name.
   * 
   * @param name
   * @return true if the variable name was free
   */
  public static boolean markVariableNameAsUsed(String name) {
    return variableNames.add(name);
  }

  /**
   * Generates a name for a new variable and marks this name as used.
   * 
   * @return a valid name for a new variable
   */
  public static String generateFreeVariableName() {
    while (true) {
      String varName = getNextFreeVariableName();
      toggleFreeVariableName();
      if (markVariableNameAsUsed(varName))
        return varName;
    }
  }

  private static String getNextFreeVariableName() {
    StringBuilder str = new StringBuilder(String.valueOf(nextFreeVariableName));
    if (freeVariableSequenceCounter > 1)
      str.append(freeVariableSequenceCounter);
    return str.toString();
  }

  private static void toggleFreeVariableName() {
    if (nextFreeVariableName < 'z')
      nextFreeVariableName++;
    else {
      nextFreeVariableName = 'i';
      freeVariableSequenceCounter++;
    }
  }

  public static void resetProposalNameGenerator() {
    variableNames.clear();
    nextFreeVariableName = 'i';
    freeVariableSequenceCounter = 1;
  }
}
