package jrm.ui.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import jrm.profile.report.FilterOptions;

@SuppressWarnings("serial")
public class BatchTrrntChkReportTreeModel extends DefaultTreeModel
{
	/** The org root. */
	private final TrntChkReportNode org_root;
	
	/** The filter options. */
	private List<FilterOptions> filterOptions = new ArrayList<>();

	public BatchTrrntChkReportTreeModel(TrntChkReportNode root)
	{
		super(root);
		org_root = root;
	}

	/**
	 * Inits the clone.
	 */
	public void initClone()
	{
		setRoot(new TrntChkReportNode(org_root.getReport().clone(filterOptions)));
	}

	/**
	 * Filter.
	 *
	 * @param filterOptions the filter options
	 */
	public void filter(final FilterOptions... filterOptions)
	{
		filter(Arrays.asList(filterOptions));
	}

	/**
	 * Filter.
	 *
	 * @param filterOptions the filter options
	 */
	public void filter(final List<FilterOptions> filterOptions)
	{
		this.filterOptions = filterOptions;
		setRoot(new TrntChkReportNode(org_root.getReport().clone(filterOptions)));
	}

	/**
	 * Gets the filter options.
	 *
	 * @return the filter options
	 */
	public EnumSet<FilterOptions> getFilterOptions()
	{
		if(filterOptions.size()==0)
			return EnumSet.noneOf(FilterOptions.class);
		return EnumSet.copyOf(filterOptions);
	}
}
