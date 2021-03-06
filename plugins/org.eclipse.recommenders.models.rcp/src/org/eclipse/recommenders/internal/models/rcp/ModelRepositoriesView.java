/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - change to tree viewer.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static java.text.MessageFormat.format;
import static org.eclipse.recommenders.internal.models.rcp.Constants.BUNDLE_ID;
import static org.eclipse.recommenders.internal.models.rcp.Constants.PREF_REPOSITORY_URL_LIST;
import static org.eclipse.recommenders.internal.models.rcp.ModelsRcpModule.MODEL_CLASSIFIER;
import static org.eclipse.recommenders.rcp.SharedImages.Images.ELCL_ADD_REPOSITORY;
import static org.eclipse.recommenders.rcp.SharedImages.Images.ELCL_COLLAPSE_ALL;
import static org.eclipse.recommenders.rcp.SharedImages.Images.ELCL_DELETE;
import static org.eclipse.recommenders.rcp.SharedImages.Images.ELCL_EXPAND_ALL;
import static org.eclipse.recommenders.rcp.SharedImages.Images.ELCL_REFRESH;
import static org.eclipse.recommenders.rcp.SharedImages.Images.ELCL_REMOVE_REPOSITORY;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OBJ_BULLET_BLUE;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OBJ_CHECK_GREEN;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OBJ_CROSS_RED;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OBJ_REPOSITORY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.recommenders.models.Coordinates;
import org.eclipse.recommenders.models.IModelIndex;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.rcp.ModelEvents.AdvisorConfigurationChangedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelArchiveDownloadedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelIndexOpenedEvent;
import org.eclipse.recommenders.models.rcp.actions.TriggerModelDownloadForModelCoordinatesAction;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.SharedImages.ImageResource;
import org.eclipse.recommenders.rcp.utils.Selections;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.name.Named;

public class ModelRepositoriesView extends ViewPart {

    private static final Logger LOG = LoggerFactory.getLogger(ModelRepositoriesView.class);

    private DataBindingContext bindingContext;

    private Tree tree;

    private Action addRemoteRepositoryAction;

    private final IModelIndex index;

    private final SharedImages images;

    private final EclipseModelRepository repo;

    private final ModelsRcpPreferences prefs;

    private final List<String> modelClassifiers;

    private final EventBus bus;

    private TreeViewer treeViewer;
    private List<KnownCoordinate> values = Lists.newArrayList();

    private Text txtSearch;

    @Inject
    public ModelRepositoriesView(IModelIndex index, SharedImages images, EclipseModelRepository repo,
            ModelsRcpPreferences prefs, @Named(MODEL_CLASSIFIER) ImmutableSet<String> modelClassifiers, EventBus bus) {
        this.index = index;
        this.images = images;
        this.repo = repo;
        this.prefs = prefs;
        this.modelClassifiers = Lists.newArrayList(modelClassifiers);
        Collections.sort(this.modelClassifiers);
        this.bus = bus;
    }

    final PatternFilter patternFilter = new PatternFilter() {
        @Override
        protected boolean isLeafMatch(final Viewer viewer, final Object element) {
            if (element instanceof KnownCoordinate) {
                final KnownCoordinate coor = (KnownCoordinate) element;
                return wordMatches(coor.pc.toString());
            }
            if (element instanceof String) {
                return true;
            }
            return super.isLeafMatch(viewer, element);
        }
    };

    @Override
    public void createPartControl(Composite parent) {
        bus.register(this);
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        txtSearch = new Text(container, SWT.BORDER | SWT.ICON_SEARCH | SWT.SEARCH | SWT.CANCEL);
        txtSearch.setMessage(Messages.SEARCH_PLACEHOLDER_FILTER_TEXT);
        txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN && tree.getItemCount() != 0) {
                    tree.setFocus();
                    tree.setSelection(tree.getTopItem());
                }
            }
        });

        Composite composite = new Composite(container, SWT.NONE);

        TreeColumnLayout treeLayout = new TreeColumnLayout();
        composite.setLayout(treeLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        treeViewer = new TreeViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
        ColumnViewerToolTipSupport.enableFor(treeViewer);
        tree = treeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        TreeViewerColumn repositoryViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        TreeColumn repositoryColumn = repositoryViewerColumn.getColumn();
        treeLayout.setColumnData(repositoryColumn, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
        repositoryColumn.setText(Messages.COLUMN_LABEL_REPOSITORY);
        repositoryViewerColumn.setLabelProvider(new StyledCellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                Object element = cell.getElement();
                StyledString text = new StyledString();
                if (element instanceof String) {
                    String url = (String) element;
                    text.append(url);
                    text.append(" "); //$NON-NLS-1$
                    text.append(format(Messages.TABLE_CELL_SUFFIX_KNOWN_COORDINATES, fetchNumberOfModels(url)), StyledString.COUNTER_STYLER);
                    cell.setImage(images.getImage(OBJ_REPOSITORY));
                }
                if (element instanceof KnownCoordinate) {
                    KnownCoordinate v = (KnownCoordinate) element;
                    text.append(v.pc.toString());
                }
                cell.setText(text.toString());
                cell.setStyleRanges(text.getStyleRanges());
                super.update(cell);
            }
        });

        int minWidth = calculateMinColumnWidthForClassifier();
        for (String classifier : modelClassifiers) {
            newColumn(treeLayout, classifier, minWidth);
        }

        treeViewer.setContentProvider(new ITreeContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public boolean hasChildren(Object element) {
                if (element instanceof String) {
                    return true;
                }
                return false;
            }

            @Override
            public Object getParent(Object element) {
                if (element instanceof KnownCoordinate) {
                    KnownCoordinate v = (KnownCoordinate) element;
                    return v.url;
                }
                return null;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return prefs.remotes;
            }

            @Override
            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof String) {
                    String url = (String) parentElement;
                    ImmutableListMultimap<String, KnownCoordinate> multimap = groupByUrl(values);
                    return multimap.get(url).toArray();
                }
                return new Object[0];
            }
        });

        treeViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (e1 instanceof String && e2 instanceof String) {
                    String s1 = (String) e1;
                    String s2 = (String) e2;
                    return position(s1).compareTo(position(s2));
                }
                if (e1 instanceof KnownCoordinate && e2 instanceof KnownCoordinate) {
                    KnownCoordinate v1 = (KnownCoordinate) e1;
                    KnownCoordinate v2 = (KnownCoordinate) e2;
                    return v1.pc.toString().compareTo(v2.pc.toString());
                }
                return super.compare(viewer, e1, e2);
            }

            public Integer position(String string) {
                String[] remotes = prefs.remotes;
                for (int i = 0; i < remotes.length; i++) {
                    if (remotes[i].equals(string)) {
                        return i;
                    }
                }
                return -1;
            }
        });

        initializeContent();

        addRemoteRepositoryAction = new Action() {
            @Override
            public void run() {
                addRemoteRepository();
            }
        };

        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
        addAction(Messages.TOOLBAR_TOOLTIP_ADD_REPOSITORY, ELCL_ADD_REPOSITORY, toolBarManager, addRemoteRepositoryAction);

        addAction(Messages.TOOLBAR_TOOLTIP_REFRESH, ELCL_REFRESH, toolBarManager, new Action() {
            @Override
            public void run() {
                refreshData();
            }
        });

        addAction(Messages.TOOLBAR_TOOLTIP_EXPAND_ALL, ELCL_EXPAND_ALL, toolBarManager, new Action() {
            @Override
            public void run() {
                treeViewer.expandAll();
            }
        });

        addAction(Messages.TOOLBAR_TOOLTIP_COLLAPSE_ALL, ELCL_COLLAPSE_ALL, toolBarManager, new Action() {
            @Override
            public void run() {
                treeViewer.collapseAll();
            }
        });

        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        addAction(Messages.MENUITEM_DELETE_MODELS, ELCL_DELETE, menuManager, new Action() {
            @Override
            public void run() {
                deleteCacheAndRefresh();
            }
        });

        addContextMenu();
        bindingContext = initDataBindings();
    }

    private int calculateMinColumnWidthForClassifier() {
        GC gc = new GC(tree);
        int maxLength = 0;
        for (String classifier : modelClassifiers) {
            int extent = gc.textExtent(classifier).x;
            if (extent > maxLength) {
                maxLength = extent;
            }
        }
        gc.dispose();
        return maxLength;
    }

    private ImmutableListMultimap<String, KnownCoordinate> groupByUrl(List<KnownCoordinate> values) {
        return Multimaps.index(values, new Function<KnownCoordinate, String>() {

            @Override
            public String apply(KnownCoordinate arg0) {
                return arg0.url;
            }
        });
    }

    protected int fetchNumberOfModels(String url) {
        return groupByUrl(values).get(url).size();
    }

    public class KnownCoordinate {
        public String url;
        public ProjectCoordinate pc;
        public Collection<ModelCoordinate> mcs;

        public KnownCoordinate(String url, ProjectCoordinate pc, Collection<ModelCoordinate> mcs) {
            this.url = url;
            this.pc = pc;
            this.mcs = mcs;
        }

        private Optional<ModelCoordinate> searchModelCoordinate(String classifier) {
            for (ModelCoordinate mc : mcs) {
                if (mc.getClassifier().equals(classifier)) {
                    return Optional.of(mc);
                }
            }
            return Optional.absent();
        }

        public boolean isDownloaded(String classifier) {
            Optional<ModelCoordinate> omc = searchModelCoordinate(classifier);
            if (omc.isPresent()) {
                return repo.getLocation(omc.get(), false).isPresent();
            }
            return false;
        }

        public boolean hasModelCoordinate(String classifier) {
            return searchModelCoordinate(classifier).isPresent();
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this, "mcs"); //$NON-NLS-1$
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj, "mcs"); //$NON-NLS-1$
        }

    }

    private void addAction(String text, ImageResource imageResource, IContributionManager contributionManager,
            IAction action) {
        action.setImageDescriptor(images.getDescriptor(imageResource));
        action.setText(text);
        action.setToolTipText(text);
        contributionManager.add(action);
    }

    private void addContextMenu() {
        final MenuManager menuManager = new MenuManager();
        Menu contextMenu = menuManager.createContextMenu(tree);
        menuManager.setRemoveAllWhenShown(true);
        tree.setMenu(contextMenu);

        menuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                if (isValidType(treeViewer.getSelection(), KnownCoordinate.class)) {
                    Set<KnownCoordinate> selectedValues = Selections.toSet(treeViewer.getSelection());
                    Set<ModelCoordinate> selectedModelCoordinates = Sets.newHashSet();
                    for (KnownCoordinate value : selectedValues) {
                        Collection<ModelCoordinate> mcs = value.mcs;
                        selectedModelCoordinates.addAll(mcs);
                    }
                    if (!selectedValues.isEmpty()) {
                        TriggerModelDownloadForModelCoordinatesAction action = new TriggerModelDownloadForModelCoordinatesAction(
                                Messages.MENUITEM_DOWNLOAD_MODELS, selectedModelCoordinates, repo, bus);
                        menuManager.add(action);
                    }
                }

                if (isValidType(treeViewer.getSelection(), String.class)) {
                    addAction(Messages.MENUITEM_ADD_REPOSITORY, ELCL_ADD_REPOSITORY, menuManager, addRemoteRepositoryAction);

                    final Optional<String> url = Selections.getFirstSelected(treeViewer.getSelection());
                    if (url.isPresent() && prefs.remotes.length > 1) {
                        addAction(Messages.MENUITEM_REMOVE_REPOSITORY, ELCL_REMOVE_REPOSITORY, menuManager, new Action() {
                            @Override
                            public void run() {
                                deleteRepository(url.get());
                                refreshData();
                            }
                        });
                    }
                }
            }

            private boolean isValidType(ISelection selection, Class<?> expectedType) {
                return Selections.safeFirstElement(treeViewer.getSelection(), expectedType).isPresent();
            }
        });
    }

    private void newColumn(TreeColumnLayout treeLayout, final String classifier, int minWidth) {
        TreeViewerColumn classifierViewerColumn = new TreeViewerColumn(treeViewer, SWT.CENTER);
        TreeColumn classifierColumn = classifierViewerColumn.getColumn();
        classifierColumn.setMoveable(true);
        classifierColumn.setResizable(false);
        treeLayout.setColumnData(classifierColumn, new ColumnPixelData(minWidth, false, true));
        classifierColumn.setText(classifier.toUpperCase());
        classifierViewerColumn.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                return null;
            }

            @Override
            public String getToolTipText(Object element) {
                if (element instanceof KnownCoordinate) {
                    KnownCoordinate v = (KnownCoordinate) element;

                    if (!v.hasModelCoordinate(classifier)) {
                        return Messages.TABLE_CELL_TOOLTIP_UNAVAILABLE;
                    } else if (v.isDownloaded(classifier)) {
                        return Messages.TABLE_CELL_TOOLTIP_AVAILABLE_LOCALLY;
                    } else {
                        return Messages.TABLE_CELL_TOOLTIP_AVAILABLE_REMOTELY;
                    }
                }
                return null;
            }

            @Override
            public Image getImage(Object element) {
                if (element instanceof KnownCoordinate) {
                    KnownCoordinate v = (KnownCoordinate) element;
                    if (!v.hasModelCoordinate(classifier)) {
                        return images.getImage(OBJ_CROSS_RED);
                    } else if (v.isDownloaded(classifier)) {
                        return images.getImage(OBJ_CHECK_GREEN);
                    } else {
                        return images.getImage(OBJ_BULLET_BLUE);
                    }
                }
                return null;
            }
        });
    }

    private void initializeContent() {
        Multimap<String, ModelCoordinate> groupedByRepository = fetchDataGroupedByRepository();

        values = Lists.newArrayList();
        for (Entry<String, Collection<ModelCoordinate>> entry : groupedByRepository.asMap().entrySet()) {
            values.addAll(createValues(entry.getKey(), entry.getValue()));
        }

        treeViewer.setInput(values);
    }

    private List<KnownCoordinate> createValues(String url, Collection<ModelCoordinate> collection) {
        Multimap<ProjectCoordinate, ModelCoordinate> map = groupDataByProjectCoordinate(collection);

        List<KnownCoordinate> values = Lists.newArrayList();
        for (ProjectCoordinate rep : map.keySet()) {
            values.add(new KnownCoordinate(url, rep, map.get(rep)));
        }

        return values;
    }

    private Multimap<String, ModelCoordinate> fetchDataGroupedByRepository() {
        Multimap<String, ModelCoordinate> temp = LinkedListMultimap.create();

        for (String classifier : modelClassifiers) {
            addModelCoordinateToIndex(temp, classifier);
        }

        return temp;
    }

    private void addModelCoordinateToIndex(Multimap<String, ModelCoordinate> temp, String classifier) {
        for (ModelCoordinate mc : index.getKnownModels(classifier)) {
            Optional<String> hint = mc.getHint(ModelCoordinate.HINT_REPOSITORY_URL);
            if (hint.isPresent()) {
                temp.put(hint.get(), mc);
            }
        }
    }

    private Multimap<ProjectCoordinate, ModelCoordinate> groupDataByProjectCoordinate(
            Collection<ModelCoordinate> collection) {
        Multimap<ProjectCoordinate, ModelCoordinate> map = LinkedListMultimap.create();

        for (ModelCoordinate modelCoordinate : collection) {
            map.put(Coordinates.toProjectCoordinate(modelCoordinate), modelCoordinate);
        }
        return map;
    }

    @Override
    public void setFocus() {
        tree.setFocus();
    }

    // needs to be public to work with PojoProperties
    public void setFilter(final String filter) {
        treeViewer.getTree().setRedraw(false);
        PatternFilter patternFilter = new KnownCoordinatePatternFilter();
        patternFilter.setPattern("*" + filter); //$NON-NLS-1$
        treeViewer.setFilters(new ViewerFilter[] { patternFilter });
        treeViewer.getTree().setRedraw(true);
        treeViewer.expandAll();
    }

    @Override
    public void dispose() {
        super.dispose();
        bindingContext.dispose();
        bus.unregister(this);
    }

    @Subscribe
    public void onModelIndexOpened(ModelIndexOpenedEvent e) {
        refreshData();
    }

    @Subscribe
    public void onAdvisorConfigurationChanged(AdvisorConfigurationChangedEvent e) throws IOException {
        refreshData();
    }

    @Subscribe
    public void onModelArchiveDownloaded(final ModelArchiveDownloadedEvent e) {
        new UIJob("") { //$NON-NLS-1$

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                KnownCoordinate key = createKey(e.model);
                KnownCoordinate element = Iterables.tryFind(values, Predicates.equalTo(key)).orNull();
                if (element != null) {
                    treeViewer.update(element, null);
                }
                return Status.OK_STATUS;
            }

            private KnownCoordinate createKey(ModelCoordinate mc) {
                Optional<String> remoteUrl = mc.getHint(ModelCoordinate.HINT_REPOSITORY_URL);
                if (remoteUrl.isPresent()) {
                    return new KnownCoordinate(remoteUrl.get(), Coordinates.toProjectCoordinate(mc),
                            Collections.<ModelCoordinate>emptyList());
                }
                return null;
            }
        }.schedule();
    }

    private void refreshData() {
        new UIJob(Messages.JOB_REFRESHING_MODEL_REPOSITORIES_VIEW) {
            {
                schedule();
            }

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                initializeContent();
                return Status.OK_STATUS;
            }
        };
    }

    private void addRemoteRepository() {
        InputDialog inputDialog = Dialogs.newModelRepositoryUrlDialog(tree.getShell(), prefs.remotes);
        if (inputDialog.open() == Window.OK) {
            addRepository(inputDialog.getValue());
        }
    }

    private void deleteRepository(String remoteUrl) {
        ArrayList<String> newRemotes = Lists.newArrayList(prefs.remotes);
        newRemotes.remove(remoteUrl);
        storeRepositories(newRemotes);
    }

    private void addRepository(String remoteUrl) {
        ArrayList<String> newRemotes = Lists.newArrayList(prefs.remotes);
        newRemotes.add(remoteUrl);
        storeRepositories(newRemotes);
    }

    private void storeRepositories(ArrayList<String> newRemotes) {
        try {
            IEclipsePreferences s = InstanceScope.INSTANCE.getNode(BUNDLE_ID);
            s.put(PREF_REPOSITORY_URL_LIST, ModelsRcpPreferences.joinRemoteRepositoriesToString(newRemotes));
            s.flush();
        } catch (BackingStoreException e) {
            LOG.error("Exception during storing of remote repository preferences", e); //$NON-NLS-1$
        }
    }

    private void deleteCacheAndRefresh() {
        new Job(Messages.JOB_DELETING_MODEL_CACHE) {
            {
                schedule();
            }

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    repo.deleteModels();
                    refreshData();
                    return Status.OK_STATUS;
                } catch (IOException e) {
                    return new Status(Status.ERROR, org.eclipse.recommenders.internal.models.rcp.Constants.BUNDLE_ID,
                            Messages.LOG_ERROR_FAILED_TO_DELETE_MODEL_CACHE);
                }
            }
        };
    }

    protected DataBindingContext initDataBindings() {
        DataBindingContext bindingContext = new DataBindingContext();
        IObservableValue search = WidgetProperties.text(SWT.Modify).observeDelayed(400, txtSearch);
        IObservableValue filter = PojoProperties.value("filter").observe(this); //$NON-NLS-1$
        bindingContext.bindValue(filter, search, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        return bindingContext;
    }
}
