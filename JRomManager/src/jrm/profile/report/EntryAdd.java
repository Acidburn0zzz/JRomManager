package jrm.profile.report;

import java.io.Serializable;

import org.apache.commons.text.StringEscapeUtils;

import jrm.locale.Messages;
import jrm.profile.data.EntityBase;
import jrm.profile.data.Entry;

/**
 * Entry can be added
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryAdd extends Note implements Serializable
{
	/**
	 * the related {@link EntityBase} (a Rom, a Disk, or a Sample)
	 */
	final EntityBase entity;
	/**
	 * The {@link Entry} to add
	 */
	final Entry entry;

	/**
	 * The constructor for this entry report
	 * @param entity the related {@link EntityBase} (a Rom, a Disk, or a Sample)
	 * @param entry the {@link Entry} to add
	 */
	public EntryAdd(final EntityBase entity, final Entry entry)
	{
		this.entity = entity;
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryAddAdd"), parent.ware.getFullName(), entity.getNormalizedName(), entry.parent.file.getName(), entry.file); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryAddAdd")), toBlue(parent.ware.getFullName()), toBold(entity.getNormalizedName()), toItalic(entry.parent.file.getName()), toBold(entry.file))); //$NON-NLS-1$
	}
}
