package jrm.ui;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import jrm.Messages;
import jrm.misc.Settings;
import jrm.profile.Export;
import jrm.profile.Export.ExportType;
import jrm.profile.Profile;
import jrm.profile.ProfileNFOMame;
import jrm.profile.ProfileNFOMame.MameStatus;
import jrm.profile.data.*;
import jrm.ui.controls.JRMFileChooser;

@SuppressWarnings("serial")
public class ProfileViewer extends JDialog
{
	private JTable tableEntity;
	private JTable tableW;
	private JTable tableWL;
	private JTextField txtSearch;

	public ProfileViewer(final Window owner, final Profile profile)
	{
		super(owner);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ProfileViewer.class.getResource("/jrm/resources/rom.png")));
		setTitle(Messages.getString("ProfileViewer.this.title")); //$NON-NLS-1$

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		final JPanel panelWare = new JPanel();
		splitPane.setLeftComponent(panelWare);
		panelWare.setLayout(new BorderLayout(0, 0));

		final JSplitPane splitPaneWLW = new JSplitPane();
		splitPaneWLW.setOneTouchExpandable(true);
		splitPaneWLW.setContinuousLayout(true);
		splitPaneWLW.setResizeWeight(0.25);
		panelWare.add(splitPaneWLW, BorderLayout.CENTER);

		tableEntity = new JTable();
		tableEntity.setPreferredScrollableViewportSize(new Dimension(1200, 300));
		tableEntity.setShowGrid(false);
		tableEntity.setShowHorizontalLines(false);
		tableEntity.setShowVerticalLines(false);
		tableEntity.setRowSelectionAllowed(false);
		tableEntity.setFillsViewportHeight(true);
		tableEntity.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		final JPanel panelW = new JPanel();
		splitPaneWLW.setRightComponent(panelW);
		// panelWare.add(panelSL, BorderLayout.NORTH);
		panelW.setLayout(new BorderLayout(0, 0));

		final JToolBar toolBarW = new JToolBar();
		panelW.add(toolBarW, BorderLayout.SOUTH);

		final JToggleButton tglbtnMissingW = new JToggleButton("");
		tglbtnMissingW.setSelected(true);
		tglbtnMissingW.setToolTipText(Messages.getString("ProfileViewer.tglbtnMissingW.toolTipText")); //$NON-NLS-1$
		tglbtnMissingW.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/folder_closed_red.png")));
		toolBarW.add(tglbtnMissingW);

		final JToggleButton tglbtnPartialW = new JToggleButton("");
		tglbtnPartialW.setSelected(true);
		tglbtnPartialW.setToolTipText(Messages.getString("ProfileViewer.tglbtnPartialW.toolTipText")); //$NON-NLS-1$
		tglbtnPartialW.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/folder_closed_orange.png")));
		toolBarW.add(tglbtnPartialW);

		final JToggleButton tglbtnCompleteW = new JToggleButton("");
		tglbtnCompleteW.setSelected(true);
		tglbtnCompleteW.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/folder_closed_green.png")));
		tglbtnCompleteW.setToolTipText(Messages.getString("ProfileViewer.tglbtnCompleteW.toolTipText")); //$NON-NLS-1$
		toolBarW.add(tglbtnCompleteW);

		final JPanel panel_1 = new JPanel();
		panel_1.setBorder(null);
		toolBarW.add(panel_1);
		final GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 286, 166, 0 };
		gbl_panel_1.rowHeights = new int[] { 20, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		final JLabel lblSearch = new JLabel(Messages.getString("ProfileViewer.lblSearch.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblSearch = new GridBagConstraints();
		gbc_lblSearch.insets = new Insets(0, 0, 0, 5);
		gbc_lblSearch.anchor = GridBagConstraints.EAST;
		gbc_lblSearch.gridx = 0;
		gbc_lblSearch.gridy = 0;
		panel_1.add(lblSearch, gbc_lblSearch);

		txtSearch = new JTextField();
		txtSearch.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(final KeyEvent e)
			{
				final String search = txtSearch.getText();
				@SuppressWarnings("unchecked")
				final
				int row = ((AnywareList<Anyware>) tableW.getModel()).find(search);
				if(row >= 0)
				{
					tableW.setRowSelectionInterval(row, row);
					tableW.scrollRectToVisible(tableW.getCellRect(row, 0, true));
				}
			}
		});
		final GridBagConstraints gbc_txtSearch = new GridBagConstraints();
		gbc_txtSearch.fill = GridBagConstraints.VERTICAL;
		gbc_txtSearch.anchor = GridBagConstraints.WEST;
		gbc_txtSearch.gridx = 1;
		gbc_txtSearch.gridy = 0;
		panel_1.add(txtSearch, gbc_txtSearch);
		txtSearch.setText("");
		txtSearch.setColumns(20);

		tglbtnMissingW.addItemListener(e -> setFilterW(tglbtnMissingW.isSelected(), tglbtnPartialW.isSelected(), tglbtnCompleteW.isSelected()));
		tglbtnPartialW.addItemListener(e -> setFilterW(tglbtnMissingW.isSelected(), tglbtnPartialW.isSelected(), tglbtnCompleteW.isSelected()));
		tglbtnCompleteW.addItemListener(e -> setFilterW(tglbtnMissingW.isSelected(), tglbtnPartialW.isSelected(), tglbtnCompleteW.isSelected()));

		final JScrollPane scrollPaneW = new JScrollPane();
		panelW.add(scrollPaneW);

		tableW = new JTable();
		scrollPaneW.setViewportView(tableW);
		tableW.setPreferredScrollableViewportSize(new Dimension(600, 400));
		tableW.setFillsViewportHeight(true);
		tableW.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableW.setShowGrid(false);
		tableW.setShowHorizontalLines(false);
		tableW.setShowVerticalLines(false);
		tableW.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		final JPanel panel = new JPanel();
		splitPaneWLW.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		final JScrollPane scrollPaneWL = new JScrollPane();
		panel.add(scrollPaneWL);

		tableWL = new JTable();
		scrollPaneWL.setViewportView(tableWL);
		tableWL.setPreferredScrollableViewportSize(new Dimension(300, 400));
		tableWL.setModel(profile.machinelist_list);
		tableWL.setTableHeader(new JTableHeader(tableWL.getColumnModel())
		{
			@Override
			public String getToolTipText(final MouseEvent e)
			{
				return columnModel.getColumn(columnModel.getColumnIndexAtX(e.getPoint().x)).getHeaderValue().toString();
			}
		});
		tableWL.setFillsViewportHeight(true);
		tableWL.setShowGrid(false);
		tableWL.setShowHorizontalLines(false);
		tableWL.setShowVerticalLines(false);
		tableWL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		final JPopupMenu popupMenu = new JPopupMenu();
		ProfileViewer.addPopup(tableWL, popupMenu);

		final JMenu mnExportAll = new JMenu("Export All");
		popupMenu.add(mnExportAll);

		final JMenu mnExportAllFiltered = new JMenu("Filtered");
		mnExportAll.add(mnExportAllFiltered);

		final JMenuItem mntmFilteredAsLogiqxDat = new JMenuItem("as Logiqx dat");
		mnExportAllFiltered.add(mntmFilteredAsLogiqxDat);
		mntmFilteredAsLogiqxDat.addActionListener(e -> export(ExportType.DATAFILE, true, null));

		final JMenuItem mntmFilteredAsMameDat = new JMenuItem("as Mame dat");
		mnExportAllFiltered.add(mntmFilteredAsMameDat);
		mntmFilteredAsMameDat.addActionListener(e -> export(ExportType.MAME, true, null));

		final JMenuItem mntmFilteredAsSoftwareLists = new JMenuItem("as Software Lists dat");
		mnExportAllFiltered.add(mntmFilteredAsSoftwareLists);
		mntmFilteredAsSoftwareLists.addActionListener(e -> export(ExportType.SOFTWARELIST, true, null));

		final JMenuItem mntmAllAsLogiqxDat = new JMenuItem("as Logiqx dat");
		mntmAllAsLogiqxDat.setEnabled(false);
		mnExportAll.add(mntmAllAsLogiqxDat);
		mntmAllAsLogiqxDat.addActionListener(e -> export(ExportType.DATAFILE, false, null));

		final JMenuItem mntmAllAsMameDat = new JMenuItem("as Mame dat");
		mntmAllAsMameDat.setEnabled(false);
		mnExportAll.add(mntmAllAsMameDat);
		mntmAllAsMameDat.addActionListener(e -> export(ExportType.MAME, false, null));

		final JMenuItem mntmAllAsSoftwareLists = new JMenuItem("as Software Lists dat");
		mntmAllAsSoftwareLists.setEnabled(false);
		mnExportAll.add(mntmAllAsSoftwareLists);
		mntmAllAsSoftwareLists.addActionListener(e -> export(ExportType.SOFTWARELIST, false, null));

		final JMenu mnExportSelected = new JMenu("Export selected");
		popupMenu.add(mnExportSelected);

		final JMenu mnExportSelectedFiltered = new JMenu("Filtered");
		mnExportSelected.add(mnExportSelectedFiltered);

		final JMenuItem mntmSelectedFilteredAsSoftwareList = new JMenuItem("as Software List dat");
		mnExportSelectedFiltered.add(mntmSelectedFilteredAsSoftwareList);
		mntmSelectedFilteredAsSoftwareList.addActionListener(e -> export(ExportType.SOFTWARELIST, true, (SoftwareList) tableWL.getModel().getValueAt(tableWL.getSelectedRow(), 0)));

		final JMenuItem mntmSelectedAsSoftwareLists = new JMenuItem("as Software Lists dat");
		mntmSelectedAsSoftwareLists.setEnabled(false);
		mnExportSelected.add(mntmSelectedAsSoftwareLists);
		mntmSelectedAsSoftwareLists.addActionListener(e -> export(ExportType.SOFTWARELIST, false, (SoftwareList) tableWL.getModel().getValueAt(tableWL.getSelectedRow(), 0)));

		popupMenu.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuCanceled(final PopupMenuEvent e)
			{
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e)
			{
			}

			@Override
			public void popupMenuWillBecomeVisible(final PopupMenuEvent e)
			{
				final boolean has_machines = Profile.curr_profile.machinelist_list.getList().stream().mapToInt(ml -> ml.getList().size()).sum() > 0;
				final boolean has_filtered_machines = Profile.curr_profile.machinelist_list.getFilteredStream().mapToInt(m -> (int) m.countAll()).sum() > 0;
				final boolean has_selected_swlist = tableWL.getSelectedRowCount() == 1 && tableWL.getModel() instanceof AnywareListList<?> && ((AnywareListList<?>) tableWL.getModel()).getValueAt(tableWL.getSelectedRow(), 0) instanceof SoftwareList;
				mntmAllAsMameDat.setEnabled(has_machines);
				mntmAllAsLogiqxDat.setEnabled(has_machines);
				mntmAllAsSoftwareLists.setEnabled(Profile.curr_profile.machinelist_list.softwarelist_list.size() > 0);
				mntmFilteredAsMameDat.setEnabled(has_filtered_machines);
				mntmFilteredAsLogiqxDat.setEnabled(has_filtered_machines);
				mntmFilteredAsSoftwareLists.setEnabled(Profile.curr_profile.machinelist_list.softwarelist_list.getFilteredStream().count() > 0);
				mntmSelectedAsSoftwareLists.setEnabled(has_selected_swlist);
				mntmSelectedFilteredAsSoftwareList.setEnabled(has_selected_swlist);
			}
		});

		final JToolBar toolBarWL = new JToolBar();
		panel.add(toolBarWL, BorderLayout.SOUTH);

		final JToggleButton tglbtnMissingWL = new JToggleButton("");
		tglbtnMissingWL.setSelected(true);
		tglbtnMissingWL.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/disk_multiple_red.png")));
		tglbtnMissingWL.setToolTipText(Messages.getString("ProfileViewer.tglbtnMissingWL.toolTipText")); //$NON-NLS-1$
		toolBarWL.add(tglbtnMissingWL);

		final JToggleButton tglbtnPartialWL = new JToggleButton("");
		tglbtnPartialWL.setSelected(true);
		tglbtnPartialWL.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/disk_multiple_orange.png")));
		tglbtnPartialWL.setToolTipText(Messages.getString("ProfileViewer.tglbtnPartialWL.toolTipText")); //$NON-NLS-1$
		toolBarWL.add(tglbtnPartialWL);

		final JToggleButton tglbtnCompleteWL = new JToggleButton("");
		tglbtnCompleteWL.setSelected(true);
		tglbtnCompleteWL.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/disk_multiple_green.png")));
		tglbtnCompleteWL.setToolTipText(Messages.getString("ProfileViewer.tglbtnCompleteWL.toolTipText")); //$NON-NLS-1$
		toolBarWL.add(tglbtnCompleteWL);

		tglbtnMissingWL.addItemListener(e -> setFilterWL(tglbtnMissingWL.isSelected(), tglbtnPartialWL.isSelected(), tglbtnCompleteWL.isSelected()));
		tglbtnPartialWL.addItemListener(e -> setFilterWL(tglbtnMissingWL.isSelected(), tglbtnPartialWL.isSelected(), tglbtnCompleteWL.isSelected()));
		tglbtnCompleteWL.addItemListener(e -> setFilterWL(tglbtnMissingWL.isSelected(), tglbtnPartialWL.isSelected(), tglbtnCompleteWL.isSelected()));

		tableWL.getSelectionModel().addListSelectionListener(e -> {
			if(!e.getValueIsAdjusting())
			{
				final ListSelectionModel model = (ListSelectionModel) e.getSource();
				final TableModel tablemodel = tableWL.getModel();
				if(model != null && tablemodel != null)
				{
					if(!model.isSelectionEmpty())
					{
						final AnywareList<?> anywarelist = (AnywareList<?>) tablemodel.getValueAt(model.getMinSelectionIndex(), 0);
						anywarelist.reset();
						tableW.setModel(anywarelist);
						tableW.setTableHeader(new JTableHeader(tableW.getColumnModel())
						{
							@Override
							public String getToolTipText(final MouseEvent e)
							{
								return columnModel.getColumn(columnModel.getColumnIndexAtX(e.getPoint().x)).getHeaderValue().toString();
							}
						});
						if(tableW.getRowCount() > 0)
							tableW.setRowSelectionInterval(0, 0);
						for(int i = 0; i < tableW.getColumnModel().getColumnCount(); i++)
						{
							final TableColumn column = tableW.getColumnModel().getColumn(i);
							column.setCellRenderer(anywarelist.getColumnRenderer(i));
							final int width = anywarelist.getColumnWidth(i);
							if(width > 0)
							{
								column.setMinWidth(width / 2);
								column.setPreferredWidth(width);
							}
							else if(width < 0)
							{
								column.setMinWidth(-width);
								column.setMaxWidth(-width);
							}
						}
					}
					else
					{
						tableW.setModel(new DefaultTableModel());
						tableEntity.setModel(new DefaultTableModel());
					}
				}
			}
		});
		tableW.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(final MouseEvent e)
			{
				if(e.getClickCount() == 2)
				{
					final JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					if(row >= 0)
					{
						final AnywareList<?> tablemodel = (AnywareList<?>) target.getModel();
						final int column = target.columnAtPoint(e.getPoint());
						final Object obj = tablemodel.getValueAt(row, column);
						if(obj instanceof Anyware)
						{
							final Anyware ware = (Anyware) obj;
							if(column > 1)
							{
								row = tablemodel.find(ware);
								if(row >= 0)
								{
									target.setRowSelectionInterval(row, row);
									target.scrollRectToVisible(target.getCellRect(row, 0, true));
								}
							}
							else if(ware.getStatus() == AnywareStatus.COMPLETE)
							{
								if(Profile.curr_profile != null)
								{
									final Profile profile = Profile.curr_profile;
									if(profile.nfo.mame.getStatus() == MameStatus.UPTODATE)
									{
										final ProfileNFOMame mame = profile.nfo.mame;
										String[] args = null;
										if(ware instanceof Software)
										{
											final List<String> rompaths = new ArrayList<>(Collections.singletonList(profile.getProperty("roms_dest_dir", "")));
											if(profile.getProperty("swroms_dest_dir_enabled", false))
												rompaths.add(profile.getProperty("swroms_dest_dir", ""));
											if(profile.getProperty("disks_dest_dir_enabled", false))
												rompaths.add(profile.getProperty("disks_dest_dir", ""));
											if(profile.getProperty("swdisks_dest_dir_enabled", false))
												rompaths.add(profile.getProperty("swdisks_dest_dir", ""));
											System.out.println(((Software) ware).sl.name + ", " + ((Software) ware).compatibility);
											final Machine machine = profile.machinelist_list.findMachine(((Software) ware).sl.name, ((Software) ware).compatibility);
											if(machine != null)
											{
												System.out.println("-> " + machine.name + " " + ware.name);
												args = new String[] { mame.getFile().getAbsolutePath(), machine.name, ware.name, "-homepath", mame.getFile().getParent(), "-rompath", rompaths.stream().collect(Collectors.joining(";")) };
											}
										}
										else
										{
											final List<String> rompaths = new ArrayList<>(Collections.singletonList(profile.getProperty("roms_dest_dir", "")));
											if(profile.getProperty("disks_dest_dir_enabled", false))
												rompaths.add(profile.getProperty("disks_dest_dir", ""));
											args = new String[] { mame.getFile().getAbsolutePath(), ware.name, "-homepath", mame.getFile().getParent(), "-rompath", rompaths.stream().collect(Collectors.joining(";")) };
										}
										if(args != null)
										{
											final ProcessBuilder pb = new ProcessBuilder(args).directory(mame.getFile().getParentFile()).redirectErrorStream(true).redirectOutput(new File(mame.getFile().getParentFile(), "JRomManager.log"));
											try
											{
												pb.start().waitFor();
											}
											catch(InterruptedException | IOException e1)
											{
												JOptionPane.showMessageDialog(ProfileViewer.this, e1.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
											}
										}
									}
									else
									{
										JOptionPane.showMessageDialog(ProfileViewer.this, "Mame is not available or is obsolete (" + profile.nfo.mame.getStatus() + ")", "Error", JOptionPane.ERROR_MESSAGE);
									}
								}
								else
								{
									JOptionPane.showMessageDialog(ProfileViewer.this, "No profile", "Error", JOptionPane.ERROR_MESSAGE);
								}
							}
							else
							{
								JOptionPane.showMessageDialog(ProfileViewer.this, "Can't launch incomplete set (" + ware.getStatus() + ")", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		});
		tableW.getSelectionModel().addListSelectionListener(e -> {
			if(!e.getValueIsAdjusting())
			{
				final ListSelectionModel model = (ListSelectionModel) e.getSource();
				final TableModel tablemodel = tableW.getModel();
				if(model != null && tablemodel != null)
				{
					if(!model.isSelectionEmpty())
					{
						final Anyware anyware = (Anyware) tablemodel.getValueAt(model.getMinSelectionIndex(), 0);
						anyware.reset();
						tableEntity.setModel(anyware);
						tableEntity.setTableHeader(new JTableHeader(tableEntity.getColumnModel())
						{
							@Override
							public String getToolTipText(final MouseEvent e)
							{
								return columnModel.getColumn(columnModel.getColumnIndexAtX(e.getPoint().x)).getHeaderValue().toString();
							}
						});
						for(int i = 0; i < tableEntity.getColumnModel().getColumnCount(); i++)
						{
							final TableColumn column = tableEntity.getColumnModel().getColumn(i);
							column.setCellRenderer(Anyware.getColumnRenderer(i));
							final int width = Anyware.getColumnWidth(i);
							if(width > 0)
							{
								column.setMinWidth(width / 2);
								column.setPreferredWidth(width);
							}
							else if(width < 0)
							{
								final Component component = column.getCellRenderer().getTableCellRendererComponent(tableEntity, null, false, false, 0, i);
								final int pixwidth = component.getFontMetrics(component.getFont()).stringWidth(String.format("%0" + (-width) + "d", 0));
								column.setMinWidth(pixwidth / 2);
								column.setPreferredWidth(pixwidth);
								column.setMaxWidth(pixwidth);
							}
						}
					}
					else
					{
						tableEntity.setModel(new DefaultTableModel());
					}
				}
			}
		});

		final JPanel panelEntity = new JPanel();
		splitPane.setRightComponent(panelEntity);
		panelEntity.setLayout(new BorderLayout(0, 0));

		final JToolBar toolBarEntity = new JToolBar();
		panelEntity.add(toolBarEntity, BorderLayout.SOUTH);

		final JToggleButton tglbtnBad = new JToggleButton("");
		tglbtnBad.setSelected(true);
		tglbtnBad.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/icons/bullet_red.png")));
		tglbtnBad.setToolTipText(Messages.getString("ProfileViewer.tglbtnBad.toolTipText")); //$NON-NLS-1$
		toolBarEntity.add(tglbtnBad);

		final JToggleButton tglbtnOK = new JToggleButton("");
		tglbtnOK.setSelected(true);
		tglbtnOK.setToolTipText(Messages.getString("ProfileViewer.tglbtnOK.toolTipText")); //$NON-NLS-1$
		tglbtnOK.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/icons/bullet_green.png")));
		toolBarEntity.add(tglbtnOK);

		tglbtnBad.addItemListener(e -> setFilterE(tglbtnBad.isSelected(), tglbtnOK.isSelected()));
		tglbtnOK.addItemListener(e -> setFilterE(tglbtnBad.isSelected(), tglbtnOK.isSelected()));

		final JScrollPane scrollPaneEntity = new JScrollPane();
		panelEntity.add(scrollPaneEntity, BorderLayout.CENTER);

		scrollPaneEntity.setViewportView(tableEntity);

		reset(profile);
		pack();
		setVisible(true);
	}

	private void export(final ExportType type, final boolean filtered, final SoftwareList selection)
	{
		new Thread(() -> {
			final FileNameExtensionFilter fnef = new FileNameExtensionFilter(Messages.getString("MainFrame.DatFile"), "xml", "dat");
			new JRMFileChooser<Void>(JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY, Optional.ofNullable(Settings.getProperty("MainFrame.ChooseExeOrDatToExport", (String) null)).map(File::new).orElse(null), null, Arrays.asList(fnef), "Choose destination file", false).show(ProfileViewer.this, chooser -> {
				final Progress progress = new Progress(ProfileViewer.this);
				progress.setVisible(true);
				Settings.setProperty("MainFrame.ChooseExeOrDatToExport", chooser.getCurrentDirectory().getAbsolutePath());
				final File selectedfile = chooser.getSelectedFile();
				final File file = fnef.accept(selectedfile) ? selectedfile : new File(selectedfile.getAbsolutePath() + ".xml");
				new Export(Profile.curr_profile, file, type, filtered, selection, progress);
				progress.dispose();
				return null;
			});
		}).start();
	}

	public void setFilterWL(final boolean missing, final boolean partial, final boolean complete)
	{
		final EnumSet<AnywareStatus> filter = EnumSet.of(AnywareStatus.UNKNOWN);
		if(missing)
			filter.add(AnywareStatus.MISSING);
		if(partial)
			filter.add(AnywareStatus.PARTIAL);
		if(complete)
			filter.add(AnywareStatus.COMPLETE);
		((AnywareListList<?>) tableWL.getModel()).setFilter(filter);
		if(tableWL.getRowCount() > 0)
			tableWL.setRowSelectionInterval(0, 0);
	}

	public void setFilterW(final boolean missing, final boolean partial, final boolean complete)
	{
		final EnumSet<AnywareStatus> filter = EnumSet.of(AnywareStatus.UNKNOWN);
		if(missing)
			filter.add(AnywareStatus.MISSING);
		if(partial)
			filter.add(AnywareStatus.PARTIAL);
		if(complete)
			filter.add(AnywareStatus.COMPLETE);
		((AnywareList<?>) tableW.getModel()).setFilter(filter);
		if(tableW.getRowCount() > 0)
			tableW.setRowSelectionInterval(0, 0);
	}

	public void setFilterE(final boolean ko, final boolean ok)
	{
		final EnumSet<EntityStatus> filter = EnumSet.of(EntityStatus.UNKNOWN);
		if(ko)
			filter.add(EntityStatus.KO);
		if(ok)
			filter.add(EntityStatus.OK);
		((Anyware) tableEntity.getModel()).setFilter(filter);
		if(tableEntity.getRowCount() > 0)
			tableEntity.setRowSelectionInterval(0, 0);
	}

	public void clear()
	{
		tableEntity.setModel(new DefaultTableModel());
		tableW.setModel(new DefaultTableModel());
		tableWL.setModel(new DefaultTableModel());
	}

	public void reset(final Profile profile)
	{
		final AnywareListList<?> model = profile.machinelist_list;
		model.reset();
		tableWL.setModel(model);
		for(int i = 0; i < tableWL.getColumnModel().getColumnCount(); i++)
		{
			final TableColumn column = tableWL.getColumnModel().getColumn(i);
			column.setCellRenderer(model.getColumnRenderer(i));
			final int width = model.getColumnWidth(i);
			if(width > 0)
			{
				column.setMinWidth(width / 2);
				column.setPreferredWidth(width);
			}
			else if(width < 0)
			{
				column.setMinWidth(-width);
				column.setMaxWidth(-width);
			}
		}
		if(tableWL.getRowCount() > 0)
			tableWL.setRowSelectionInterval(0, 0);
	}

	public void reload()
	{
		TableModel tablemodel = tableWL.getModel();
		if(tablemodel instanceof AnywareListList<?>)
			((AnywareListList<?>) tablemodel).fireTableChanged(new TableModelEvent(tablemodel, 0, ((AnywareListList<?>) tablemodel).getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
		tablemodel = tableW.getModel();
		if(tablemodel instanceof AnywareList<?>)
			((AnywareList<?>) tablemodel).fireTableChanged(new TableModelEvent(tablemodel, 0, ((AnywareList<?>) tablemodel).getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
		tablemodel = tableEntity.getModel();
		if(tablemodel instanceof Anyware)
			((Anyware) tablemodel).fireTableChanged(new TableModelEvent(tablemodel, 0, ((Anyware) tablemodel).getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
	}

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
