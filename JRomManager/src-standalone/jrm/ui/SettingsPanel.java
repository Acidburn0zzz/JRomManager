package jrm.ui;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import jrm.locale.Messages;
import jrm.security.Session;

@SuppressWarnings("serial")
public class SettingsPanel extends JPanel
{

	/** The settings pane. */
	private JTabbedPane settingsPane;


	/**
	 * Create the panel.
	 */
	public SettingsPanel(final Session session)
	{
		this.setLayout(new BorderLayout(0, 0));

		settingsPane = new JTabbedPane(SwingConstants.TOP);
		this.add(settingsPane);

		buildSettingsCompressorsTab(session);
		buildSettingsDebugTab(session);
	}

	/**
	 * 
	 */
	private void buildSettingsCompressorsTab(final Session session)
	{
		SettingsCompressorsPanel compressors = new SettingsCompressorsPanel(session);
		settingsPane.addTab(Messages.getString("MainFrame.Compressors"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/compress.png")), compressors, null); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void buildSettingsDebugTab(final Session session)
	{
		SettingsDbgPanel debug = new SettingsDbgPanel(session);
		settingsPane.addTab(Messages.getString("MainFrame.Debug"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/bug.png")), debug, null); //$NON-NLS-1$ //$NON-NLS-2$
	}
}

