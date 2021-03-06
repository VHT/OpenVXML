package org.eclipse.vtp.desktop.model.legacy.v4_0To5_0.dialogs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.vtp.desktop.model.core.natures.WorkflowProjectNature;
import org.eclipse.vtp.desktop.model.interactive.voice.natures.VoiceProjectNature;

public class ConversionSelectionDialog extends Dialog {
	Button allButton = null;
	Button selectedButton = null;
	CheckboxTableViewer viewer = null;
	List<ProjectRecord> convertableProjects = new LinkedList<ProjectRecord>();

	public ConversionSelectionDialog(Shell parentShell) {
		super(parentShell);
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (IProject project : projects) {
			try {
				if (project.hasNature(WorkflowProjectNature.NATURE_ID)) {
					ProjectRecord pr = new ProjectRecord();
					pr.project = project;
					pr.type = ProjectRecord.WORKFLOW;
					convertableProjects.add(pr);
				}
				if (project
						.hasNature("org.eclipse.vtp.desktop.model.interactive.core.InteractiveWorkflowProjectNature")) {
					ProjectRecord pr = new ProjectRecord();
					pr.project = project;
					pr.type = ProjectRecord.INTERACTIVE;
					convertableProjects.add(pr);
				} else if (project.hasNature(VoiceProjectNature.NATURE_ID)) {
					ProjectRecord pr = new ProjectRecord();
					pr.project = project;
					pr.type = ProjectRecord.VOICE;
					convertableProjects.add(pr);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new GridLayout(2, false));

		Label buttonLabel = new Label(comp, SWT.NONE);
		buttonLabel.setText("Which projects should be converted?");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		buttonLabel.setLayoutData(gd);

		allButton = new Button(comp, SWT.RADIO);
		allButton.setText("All Projects");
		allButton.setLayoutData(new GridData());

		selectedButton = new Button(comp, SWT.RADIO);
		selectedButton.setText("Only Selected Projects");
		selectedButton.setLayoutData(new GridData());

		allButton.setSelection(true);

		Composite tableComp = new Composite(comp, SWT.NONE);
		TableColumnLayout tcLayout = new TableColumnLayout();
		tableComp.setLayout(tcLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.heightHint = 200;
		gd.widthHint = 250;
		tableComp.setLayoutData(gd);
		Table projectTable = new Table(tableComp, SWT.BORDER | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.SINGLE | SWT.CHECK);
		projectTable.setHeaderVisible(true);
		TableColumn nameColumn = new TableColumn(projectTable, SWT.NONE);
		nameColumn.setText("Project Name");
		tcLayout.setColumnData(nameColumn, new ColumnWeightData(60, 100));
		TableColumn typeColumn = new TableColumn(projectTable, SWT.NONE);
		typeColumn.setText("Type");
		tcLayout.setColumnData(typeColumn, new ColumnWeightData(40, 50));

		viewer = new CheckboxTableViewer(projectTable);
		ProjectContentProvider provider = new ProjectContentProvider();
		viewer.setContentProvider(provider);
		viewer.setLabelProvider(provider);
		viewer.setCheckStateProvider(provider);
		viewer.setInput(this);
		viewer.getControl().setEnabled(false);
		viewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				for (ProjectRecord pr : convertableProjects) {
					if (pr == event.getElement()) {
						pr.selected = event.getChecked();
						break;
					}
				}
			}
		});

		selectedButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.getTable().setEnabled(selectedButton.getSelection());
			}
		});

		return comp;
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	public List<IProject> getProjectsToConvert() {
		List<IProject> ret = new ArrayList<IProject>();
		for (ProjectRecord pr : convertableProjects) {
			if (pr.selected) {
				ret.add(pr.project);
			}
		}
		return ret;
	}

	public class ProjectContentProvider extends BaseLabelProvider implements
			IStructuredContentProvider, ITableLabelProvider,
			ICheckStateProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return convertableProjects.toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			ProjectRecord pr = (ProjectRecord) element;
			if (columnIndex == 0) {
				return pr.project.getName();
			}
			return pr.type;
		}

		@Override
		public boolean isChecked(Object element) {
			return ((ProjectRecord) element).selected;
		}

		@Override
		public boolean isGrayed(Object element) {
			return false;
		}
	}

	public class ProjectRecord {
		static final String WORKFLOW = "Workflow";
		static final String INTERACTIVE = "Interactive Workflow";
		static final String VOICE = "Voice";

		IProject project = null;
		String type = WORKFLOW;
		boolean selected = true;
	}
}
