package completion.calls;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CompletionOnSimpleTypeInTypeBody  {

	// Button<^Space> should not trigger any template. Ensure templates are only triggered within method bodies.
	Button<^Space>

}
