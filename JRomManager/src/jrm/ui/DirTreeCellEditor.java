package jrm.ui;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

public class DirTreeCellEditor extends DefaultTreeCellEditor
{
	public DirTreeCellEditor(final JTree tree, final DefaultTreeCellRenderer renderer)
	{
		super(tree, renderer);
	}

	public DirTreeCellEditor(final JTree tree, final DefaultTreeCellRenderer renderer, final TreeCellEditor editor)
	{
		super(tree, renderer, editor);
	}

	@Override
	public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected, final boolean expanded, final boolean leaf, final int row)
	{
		final TreePath path = tree.getPathForRow(row);
		if(path.getPathCount() > 1)
			return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		return renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, /* hasFocus */true);
	}
}
