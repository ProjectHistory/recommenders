/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.codesearch.views;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.recommenders.commons.codesearch.FeedbackType;
import org.eclipse.recommenders.commons.codesearch.SnippetSummary;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPProposal;
import org.eclipse.recommenders.internal.rcp.codesearch.utils.ByteStorage;
import org.eclipse.recommenders.internal.rcp.codesearch.utils.CrASTUtil;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.rcp.utils.ast.TypeDeclarationFinder;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("restriction")
public class OpenSourceCodeInEditorJob extends WorkspaceJob {
    private final List<String> previouslyCopiedClipboardContents;
    private final SnippetSummary request;
    private final RCPProposal hit;
    private Clipboard clipboard;
    private final CodesearchController controller;

    public OpenSourceCodeInEditorJob(final SnippetSummary request, final RCPProposal proposal,
            final CodesearchController controller) {
        super("Loading Source Code from Examples Repository");
        this.controller = controller;
        previouslyCopiedClipboardContents = new ArrayList<String>();
        this.request = checkNotNull(request);
        this.hit = checkNotNull(proposal);
        clipboard = new Clipboard(Display.getCurrent());
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Opening Source in Editor...", 1);
        openViews();
        monitor.done();
        return Status.OK_STATUS;
    }

    private void openViews() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                final JavaEditor editor = openSourceInEditor(hit);
                if (editor == null) {
                    return;
                }
                try {
                    revealMethodIfAvailable(hit, editor);
                } catch (final IOException e) {
                    RecommendersPlugin.logError(e, "failed to parse source code.");
                }
            }
        });
    }

    private void revealMethodIfAvailable(final RCPProposal result, final JavaEditor editor) throws IOException {
        final IMethodName methodToReveal = result.getMethodName();
        if (methodToReveal != null) {
            final MethodDeclaration methodDeclaration = CrASTUtil.findMethod(result.getAst(new NullProgressMonitor()),
                    methodToReveal);
            if (methodDeclaration == null) {
                return;
            }
            CrASTUtil.revealInEditor(editor, methodDeclaration);
        } else {
            final TypeDeclaration decl = TypeDeclarationFinder.find(hit.getAst(new NullProgressMonitor()),
                    hit.getClassName());
            if (decl != null) {
                CrASTUtil.revealInEditor(editor, decl);
            }
        }
    }

    private JavaEditor openSourceInEditor(final RCPProposal hit) {
        // XXX: note, we cannot use some characters that often occur inside
        // method signatures here! we need to sanitize method names somehow if
        // we want to use them as title
        final String title = Names.vm2srcTypeName(hit.getClassName().getIdentifier());

        final String source = hit.getSource(new NullProgressMonitor());
        final ByteStorage storage = new ByteStorage(source, title);

        final JavaEditor openJavaEditor = JdtUtils.openJavaEditor(storage);
        final SourceViewer s = (SourceViewer) openJavaEditor.getViewer();
        final ITextPresentationListener listener = new VariableUsagesHighlighter(s, request, hit, "");
        s.addTextPresentationListener(listener);
        s.invalidateTextPresentation();
        s.getTextWidget().addFocusListener(new FocusListener() {
            @Override
            public void focusLost(final FocusEvent e) {
                clipboard = new Clipboard(Display.getCurrent());
                final TextTransfer textTransfer = TextTransfer.getInstance();
                final String contents = (String) clipboard.getContents(textTransfer);
                if (contents == null) {
                    return;
                }
                final String textData = contents.replaceAll("\r", "");
                if (!previouslyCopiedClipboardContents.contains(textData)) {
                    final TextSelection sel = (TextSelection) s.getSelection();
                    if (sel.getText().equals(textData)) {
                        previouslyCopiedClipboardContents.add(textData);
                        controller.sendFeedback(hit, FeedbackType.TEXT_COPIED);
                    }
                }
                return;
            }

            @Override
            public void focusGained(final FocusEvent e) {
                // focusLost(e);
            }
        });
        return openJavaEditor;
    }
}