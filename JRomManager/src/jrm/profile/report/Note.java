package jrm.profile.report;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import jrm.misc.HTMLRenderer;

public abstract class Note implements TreeNode,HTMLRenderer
{
	Subject parent;

	public Note()
	{
	}

	@Override
	public abstract String toString();

	@Override
	public TreeNode getChildAt(final int childIndex)
	{
		return null;
	}

	@Override
	public int getChildCount()
	{
		return 0;
	}

	@Override
	public TreeNode getParent()
	{
		return parent;
	}

	@Override
	public int getIndex(final TreeNode node)
	{
		return -1;
	}

	@Override
	public boolean getAllowsChildren()
	{
		return false;
	}

	@Override
	public boolean isLeaf()
	{
		return true;
	}

	@Override
	public Enumeration<?> children()
	{
		return null;
	}
}
