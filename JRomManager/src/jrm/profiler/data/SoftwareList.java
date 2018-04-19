package jrm.profiler.data;

import java.awt.Component;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.ui.ReportFrame;

@SuppressWarnings("serial")
public class SoftwareList extends AnywareList<Software> implements Serializable,Comparable<SoftwareList>
{
	public String name;	// required
	public StringBuffer description = new StringBuffer();
	
	private List<Software> s_list = new ArrayList<>();
	public Map<String, Software> s_byname = new HashMap<>();

	public SoftwareList()
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
		columns = new String[] {"", "name","description","cloneof"};
		columnsTypes = new Class<?>[] { Object.class,  Object.class, String.class, Object.class };
		columnsWidths = new int[] {-20, 40, 200, 40};
		columnsRenderers = new TableCellRenderer[] {
			new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof Software)
					{
						super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
						switch(((Software)value).own_status)
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
				{
					setIcon(null);
				}
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof Software)
					{
						super.getTableCellRendererComponent(table, ((Software)value).name, isSelected, hasFocus, row, column);
						return this;
					}
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			},
			null, 
			new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof Software)
					{
						super.getTableCellRendererComponent(table, ((Software)value).name, isSelected, hasFocus, row, column);
						switch(((Software)value).own_status)
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
	
	public boolean add(Software software)
	{
		software.sl = this;
		s_byname.put(software.name, software);
		return s_list.add(software);
	}

	@Override
	public int compareTo(SoftwareList o)
	{
		return this.name.compareTo(o.name);
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
			case 3:	return getFilteredList().get(rowIndex).cloneof!=null?s_byname.get(getFilteredList().get(rowIndex).cloneof):null;
		}
		return null;
	}
	
	@Override
	protected List<Software> getList()
	{
		return s_list;
	}
}
