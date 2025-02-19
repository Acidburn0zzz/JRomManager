package jrm.ui.batch;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import jrm.locale.Messages;
import jrm.profile.report.FilterOptions;
import jrm.security.Session;

@SuppressWarnings("serial")
public class BatchTrrntChkReportView extends JScrollPane
{
	public BatchTrrntChkReportView(Session session, TrntChkReportNode report)
	{
		final JTree tree = new JTree();
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setModel(report.getModel());
		tree.setCellRenderer(new BatchTrrntChkReportTreeCellRenderer());
		this.setViewportView(tree);

		final JPopupMenu popupMenu = new JPopupMenu();
		BatchTrrntChkReportView.addPopup(tree, popupMenu);

		final JMenuItem mntmOpenAllNodes = new JMenuItem(Messages.getString("ReportFrame.mntmOpenAllNodes.text")); //$NON-NLS-1$
		mntmOpenAllNodes.setIcon(new ImageIcon(BatchTrrntChkReportView.class.getResource("/jrm/resources/folder_open.png"))); //$NON-NLS-1$
		mntmOpenAllNodes.addActionListener(e -> {
			tree.invalidate();
			int j = tree.getRowCount();
			int i = 0;
			while(i < j)
			{
				tree.expandRow(i);
				i += 1;
				j = tree.getRowCount();
			}
			tree.validate();
		});
		popupMenu.add(mntmOpenAllNodes);

		final JMenuItem mntmCloseAllNodes = new JMenuItem(Messages.getString("ReportFrame.mntmCloseAllNodes.text")); //$NON-NLS-1$
		mntmCloseAllNodes.setIcon(new ImageIcon(BatchTrrntChkReportView.class.getResource("/jrm/resources/folder_closed.png"))); //$NON-NLS-1$
		mntmCloseAllNodes.addActionListener(e -> {
			tree.invalidate();
			int j = tree.getRowCount();
			int i = 0;
			while(i < j)
			{
				tree.collapseRow(i);
				i += 1;
				j = tree.getRowCount();
			}
			tree.validate();
		});
		popupMenu.add(mntmCloseAllNodes);

		final JCheckBoxMenuItem chckbxmntmShowOkEntries = new JCheckBoxMenuItem(Messages.getString("ReportFrame.chckbxmntmShowOkEntries.text")); //$NON-NLS-1$
		chckbxmntmShowOkEntries.setIcon(new ImageIcon(BatchTrrntChkReportView.class.getResource("/jrm/resources/folder_closed_green.png"))); //$NON-NLS-1$
		chckbxmntmShowOkEntries.addItemListener(e -> {
			final EnumSet<FilterOptions> options = report.getModel().getFilterOptions();
			if(e.getStateChange() == ItemEvent.SELECTED)
				options.add(FilterOptions.SHOWOK);
			else
				options.remove(FilterOptions.SHOWOK);
			report.getModel().filter(options.toArray(new FilterOptions[0]));
		});
		popupMenu.add(chckbxmntmShowOkEntries);

		final JCheckBoxMenuItem chckbxmntmHideFullyMissing = new JCheckBoxMenuItem(Messages.getString("ReportFrame.chckbxmntmHideFullyMissing.text")); //$NON-NLS-1$
		chckbxmntmHideFullyMissing.setIcon(new ImageIcon(BatchTrrntChkReportView.class.getResource("/jrm/resources/folder_closed_red.png"))); //$NON-NLS-1$
		chckbxmntmHideFullyMissing.addItemListener(e -> {
			final EnumSet<FilterOptions> options = report.getModel().getFilterOptions();
			if(e.getStateChange() == ItemEvent.SELECTED)
				options.add(FilterOptions.HIDEMISSING);
			else
				options.remove(FilterOptions.HIDEMISSING);
			report.getModel().filter(options.toArray(new FilterOptions[0]));
		});
		popupMenu.add(chckbxmntmHideFullyMissing);
	}

	/**
	 * Adds the popup.
	 *
	 * @param component the component
	 * @param popup the popup
	 */
	private static void addPopup(final Component component, final JPopupMenu popup)
	{
		component.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			private void showMenu(final MouseEvent e)
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
