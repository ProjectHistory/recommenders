/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.preferences;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.recommenders.internal.udc.Activator;
import org.eclipse.recommenders.internal.udc.AutomaticUploadJob;
import org.eclipse.recommenders.internal.udc.PreferenceKeys;
import org.eclipse.recommenders.internal.udc.UploadPreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wb.swt.ResourceManager;

import com.google.common.collect.Lists;

public class SharingPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {
    private Button automaticallyUploadButton;
    private Button everyStartupButton;
    private Button scheduleButton;
    private Text lastUploadText;
    private Combo hourCombo;
    private Button askBeforeUploadButton;
    private IPropertyChangeListener lastUploadPropertyChangeListener;
    private Text daysText;
    private final ControlDecorationDelegate decorationDelegate = new ControlDecorationDelegate();

    /**
     * @wbp.parser.constructor
     */
    public SharingPreferencesPage() {
    }

    @Override
    public void init(final IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("The Code Recommenders tools are based on statistical models generated by analysing lots of usage data. "
                + "As a result of sharing your data you will get improved models and better recommendations. "
                + "Select the Depersonalization page below to see a preview of extracted informations contained in usage data. "
                + "You can filter what you want to share and what not on the preference pages Shared Projects, Shared Libraries and Shared Packages.");
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.None);
        final GridLayout gl_composite = new GridLayout(1, false);
        gl_composite.verticalSpacing = 20;
        gl_composite.horizontalSpacing = 20;
        composite.setLayout(gl_composite);

        createAutomaticallyUploadButton(composite);

        createScheduleGroup(composite);

        createUploadNowButton(composite);

        initializeContentFromPreferences();

        registerPreferenceListeners();
        return composite;
    }

    private void registerPreferenceListeners() {
        getPreferenceStore().addPropertyChangeListener(getLastUploadPropertyChangedListener());
    }

    private IPropertyChangeListener getLastUploadPropertyChangedListener() {
        if (lastUploadPropertyChangeListener == null) {
            lastUploadPropertyChangeListener = new IPropertyChangeListener() {

                @Override
                public void propertyChange(final PropertyChangeEvent event) {
                    if (event.getProperty().equals(PreferenceKeys.lastTimeUploaded)) {
                        Display.getDefault().syncExec(new Runnable() {
                            @Override
                            public void run() {
                                updateLastUploadField();
                            }
                        });
                    }
                }
            };
        }
        return lastUploadPropertyChangeListener;
    }

    private void initializeContentFromPreferences() {
        automaticallyUploadButton.setSelection(UploadPreferences.isUploadData());

        final boolean uploadOnStartup = UploadPreferences.isUploadOnStartup();
        everyStartupButton.setSelection(uploadOnStartup);
        scheduleButton.setSelection(!uploadOnStartup);
        askBeforeUploadButton.setSelection(UploadPreferences.doAskBeforeUploading());

        daysText.setText(UploadPreferences.getUploadIntervalDays() + "");
        if (UploadPreferences.isUploadAtAnyHour()) {
            hourCombo.select(0);
        } else {
            hourCombo.select(UploadPreferences.getUploadHour() + 1);
        }

        updateScheduleEnablement();

        updateLastUploadField();
    }

    private void updateLastUploadField() {
        final long lastUpload = UploadPreferences.getLastUploadDate();
        if (lastUpload == 0) {
            lastUploadText.setText("N/A");
        } else {
            final SimpleDateFormat dateFormat = new SimpleDateFormat();
            lastUploadText.setText(dateFormat.format(new Date(lastUpload)));
        }
    }

    private void createAutomaticallyUploadButton(final Composite composite) {
        automaticallyUploadButton = new Button(composite, SWT.CHECK);
        automaticallyUploadButton.setText("Upload usage data");
        automaticallyUploadButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                updateScheduleEnablement();
            }
        });
    }

    private SelectionListener createUploadNowSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                uploadDataNow();
            }
        };
    }

    protected void uploadDataNow() {
        final AutomaticUploadJob job = new AutomaticUploadJob();
        job.schedule();
    }

    private void createUploadNowButton(final Composite composite) {
        final Button btnUploadNow = new Button(composite, SWT.NONE);
        btnUploadNow.setText("Upload Now");

        btnUploadNow.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/etool16/export_wiz.gif"));
        btnUploadNow.setToolTipText("Upload Usage Data now");
        btnUploadNow.addSelectionListener(createUploadNowSelectionListener());
    }

    private ModifyListener createModifyListener() {
        return new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                validatePage();
            }
        };
    }

    private void createScheduleGroup(final Composite composite) {
        final Group grpUploadShedule = new Group(composite, SWT.NONE);
        grpUploadShedule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpUploadShedule.setText("Upload schedule");
        grpUploadShedule.setLayout(new GridLayout(1, false));

        askBeforeUploadButton = new Button(grpUploadShedule, SWT.CHECK);
        askBeforeUploadButton.setText("Ask before uploading");

        everyStartupButton = new Button(grpUploadShedule, SWT.RADIO);
        everyStartupButton.setBounds(0, 0, 90, 16);
        everyStartupButton.setText("Upload data every time Eclipse starts");
        everyStartupButton.addSelectionListener(createScheduleSelectionListener());

        scheduleButton = new Button(grpUploadShedule, SWT.RADIO);
        scheduleButton.setBounds(0, 0, 90, 16);
        scheduleButton.setText("Upload data according to the following schedule");
        scheduleButton.addSelectionListener(createScheduleSelectionListener());

        createUploadIntervalComposite(grpUploadShedule);

        final Composite composite_1 = new Composite(grpUploadShedule, SWT.NONE);
        final GridData gd_composite_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_composite_1.horizontalIndent = 20;
        composite_1.setLayoutData(gd_composite_1);
        composite_1.setLayout(new GridLayout(2, false));

        final Label label = new Label(composite_1, SWT.NONE);
        label.setText("Last Upload:");

        lastUploadText = new Text(composite_1, SWT.BORDER);
        lastUploadText.setEditable(false);
        lastUploadText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    }

    private void createUploadIntervalComposite(final Group grpUploadShedule) {
        final Composite composite_1 = new Composite(grpUploadShedule, SWT.NONE);
        final GridData gd_composite_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_composite_1.horizontalIndent = 20;
        composite_1.setLayoutData(gd_composite_1);
        composite_1.setBounds(0, 0, 64, 64);
        composite_1.setLayout(new GridLayout(4, false));

        final Label lblNewLabel = new Label(composite_1, SWT.NONE);
        lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblNewLabel.setText("Every");

        daysText = new Text(composite_1, SWT.BORDER);
        final GridData gd_daysText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_daysText.horizontalIndent = 5;
        gd_daysText.minimumWidth = 25;
        daysText.setLayoutData(gd_daysText);
        daysText.addModifyListener(createModifyListener());

        final Label lblAt = new Label(composite_1, SWT.NONE);
        lblAt.setText("day(s) at");

        hourCombo = new Combo(composite_1, SWT.READ_ONLY);
        hourCombo.setBounds(0, 0, 91, 23);
        hourCombo.setItems(getHoursStrings());
    }

    protected boolean isUploadIntervalValid() {
        try {
            final int days = Integer.valueOf(daysText.getText());
            if (days < 1) {
                decorationDelegate.setDecoratorText(daysText, "Enter an interval of at least one day");
                return false;
            }

        } catch (final Exception e) {
            decorationDelegate.setDecoratorText(daysText, "Invalid number");
            return false;
        }
        return true;
    }

    private SelectionAdapter createScheduleSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                updateIntervalEnablement();
            }
        };
    }

    private String[] getHoursStrings() {
        final DateFormatSymbols dfs = new DateFormatSymbols(Locale.getDefault());
        final List<String> result = Lists.newArrayList();
        result.add("any time");
        for (final String amPm : dfs.getAmPmStrings()) {
            for (int i = 1; i <= 12; i++) {
                result.add(i + ":00 " + amPm);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    private void updateScheduleEnablement() {
        final boolean enabled = automaticallyUploadButton.getSelection();
        askBeforeUploadButton.setEnabled(enabled);
        everyStartupButton.setEnabled(enabled);
        scheduleButton.setEnabled(enabled);
        updateIntervalEnablement();
    }

    private void updateIntervalEnablement() {
        final boolean enabled = scheduleButton.getSelection() && automaticallyUploadButton.getSelection();
        hourCombo.setEnabled(enabled);
        daysText.setEnabled(enabled);
    }

    private void storePreferences() {
        UploadPreferences.setUploadData(automaticallyUploadButton.getSelection());
        UploadPreferences.setUploadOnStartup(everyStartupButton.getSelection());
        UploadPreferences.setUploadIntervalDays(Integer.valueOf(daysText.getText()));
        if (hourCombo.getSelectionIndex() == 0) {
            UploadPreferences.setUploadAtAnyHour(true);
        } else {
            UploadPreferences.setUploadAtAnyHour(false);
            UploadPreferences.setUploadHour(hourCombo.getSelectionIndex() - 1);
        }
        UploadPreferences.setAskBeforeUploading(askBeforeUploadButton.getSelection());
    }

    @Override
    public boolean performOk() {
        storePreferences();
        return super.performOk();
    }

    @Override
    protected void performApply() {
        storePreferences();
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        automaticallyUploadButton.setSelection(UploadPreferences.isUploadData());

        final boolean uploadOnStartup = true;
        everyStartupButton.setSelection(uploadOnStartup);
        scheduleButton.setSelection(!uploadOnStartup);

        daysText.setText(UploadPreferences.getUploadIntervalDays() + "");
        hourCombo.select(0);

        updateScheduleEnablement();

        askBeforeUploadButton.setSelection(false);
        super.performDefaults();
    }

    @Override
    public void dispose() {
        getPreferenceStore().removePropertyChangeListener(lastUploadPropertyChangeListener);
        super.dispose();
    }

    private void validatePage() {
        decorationDelegate.clearDecorations();
        final boolean valid = isUploadIntervalValid();
        if (valid) {
            this.setErrorMessage(null);
            this.setValid(true);
        } else {
            this.setErrorMessage("Page contains errors");
            this.setValid(false);
        }

    }
}