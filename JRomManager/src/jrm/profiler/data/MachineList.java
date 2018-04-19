package jrm.profiler.data;

import java.awt.Component;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.ui.ReportFrame;

@SuppressWarnings("serial")
public final class MachineList extends AnywareList<Machine> implements Serializable
{
	private ArrayList<Machine> m_list = new ArrayList<>();
	public HashMap<String, Machine> m_byname = new HashMap<>();

	public MachineList()
	{
		initTransient();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransient();
	}

	protected void initTransient()
	{
		super.initTransient();
		columns = new String[] { "", "name", "description", "cloneof", "romof" };
		columnsTypes = new Class<?>[] {Object.class,  Object.class, String.class, Object.class, Object.class};
		columnsWidths = new int[] {-20, 40, 200, 40, 40};
		columnsRenderers = new TableCellRenderer[] {
			new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof Machine)
					{
						super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
						switch(((Machine)value).own_status)
						{
							case COMPLETE:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_green.png")));
								break;
							case PARTIAL:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_orange.png")));
								break;
							case MISSING:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_red.png")));
								break;
							case UNKNOWN:
							default:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_gray.png")));
								break;
						}
						return this;
					}
					setIcon(null);
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			},
			new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof Machine)
					{
						super.getTableCellRendererComponent(table, ((Machine)value).name, isSelected, hasFocus, row, column);
						if(((Machine)value).isbios)
							setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/application_osx_terminal.png")));
						else if(((Machine)value).isdevice)
							setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/computer.png")));
						else if(((Machine)value).ismechanical)
							setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/wrench.png")));
						else
							setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/joystick.png")));
						return this;
					}
					setIcon(null);
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			},
			null, 
			new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof Machine)
					{
						super.getTableCellRendererComponent(table, ((Machine)value).name, isSelected, hasFocus, row, column);
						switch(((Machine)value).own_status)
						{
							case COMPLETE:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_green.png")));
								break;
							case PARTIAL:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_orange.png")));
								break;
							case MISSING:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_red.png")));
								break;
							case UNKNOWN:
							default:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_gray.png")));
								break;
						}
						return this;
					}
					setIcon(null);
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			}, 
			new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof Machine)
					{
						super.getTableCellRendererComponent(table, ((Machine)value).name, isSelected, hasFocus, row, column);
						switch(((Machine)value).own_status)
						{
							case COMPLETE:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_green.png")));
								break;
							case PARTIAL:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_orange.png")));
								break;
							case MISSING:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_red.png")));
								break;
							case UNKNOWN:
							default:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_gray.png")));
								break;
						}
						return this;
					}
					setIcon(null);
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			}
		}; 
	}
	
	@Override
	public int getRowCount()
	{
		return getFilteredList().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch(columnIndex)
		{
			case 0:	return getFilteredList().get(rowIndex);
			case 1:	return getFilteredList().get(rowIndex);
			case 2:	return getFilteredList().get(rowIndex).description.toString();
			case 3:	return getFilteredList().get(rowIndex).cloneof!=null?m_byname.get(getFilteredList().get(rowIndex).cloneof):null;
			case 4:	return getFilteredList().get(rowIndex).romof!=null&&!getFilteredList().get(rowIndex).romof.equals(getFilteredList().get(rowIndex).cloneof)?m_byname.get(getFilteredList().get(rowIndex).romof):null;
		}
		return null;
	}

	@Override
	protected List<Machine> getList()
	{
		return m_list;
	}

}
