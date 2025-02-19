/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.manager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.security.Session;

/**
 * The Profile NFO file managing class with tolerant manual (de)serialization
 * @author optyfr
 */
public final class ProfileNFO implements Serializable, HTMLRenderer
{
	private static final long serialVersionUID = 2L;

	/**
	 * The Profile {@link File} (can be a jrm, a dat, or an xml file)
	 */
	public File file = null;
	/**
	 * The name to show in GUI (equals to file name by default)
	 */
	public String name = null;
	/**
	 * The {@link ProfileNFOStats} stats sub class
	 */
	public ProfileNFOStats stats = new ProfileNFOStats();
	/**
	 * The {@link ProfileNFOMame} mame sub class
	 */
	public ProfileNFOMame mame = new ProfileNFOMame();

	/**
	 * fields declaration for manual serialization
	 * @serialField file File the file linked to this profile info
	 * @serialField name String the name of the profile
	 * @serialField stats ProfileNFOStats the stats related to the profile
	 * @serialField mame ProfileNFOMame the mame infos relates to the profile
	 */
	private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("file", File.class), new ObjectStreamField("name", String.class), new ObjectStreamField("stats", ProfileNFOStats.class), new ObjectStreamField("mame", ProfileNFOMame.class), }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/**
	 * Manually write serialization
	 * @serialData Use {@link ObjectOutputStream.PutField} object to add to persistent fields buffer, then {@link ObjectOutputStream#writeFields()}
	 * @param stream the destination {@link ObjectOutputStream}
	 * @throws IOException
	 */
	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("file", file); //$NON-NLS-1$
		fields.put("name", name); //$NON-NLS-1$
		fields.put("stats", stats); //$NON-NLS-1$
		fields.put("mame", mame); //$NON-NLS-1$
		stream.writeFields();
	}

	/**
	 * Manually read serialization
	 * @param stream the destination {@link ObjectInputStream}
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		file = (File) fields.get("file", null); //$NON-NLS-1$
		name = (String) fields.get("name", null); //$NON-NLS-1$
		stats = (ProfileNFOStats) fields.get("stats", new ProfileNFOStats()); //$NON-NLS-1$
		mame = (ProfileNFOMame) fields.get("mame", new ProfileNFOMame()); //$NON-NLS-1$
	}

	/**
	 * internal constructor
	 * @param file the file to attach
	 */
	private ProfileNFO(final File file)
	{
		this.file = file;
		name = file.getName();
		stats.created = new Date();
		if(isJRM())
			loadJrm(file);
	}

	/**
	 * return the nfo file derived from the attached file
	 * @param file the attached file candidate
	 * @return the nfo {@link File}
	 */
	private static File getFileNfo(final Session session, final File file)
	{
		return session.getUser().settings.getWorkFile(file.getParentFile(), file.getName(), ".nfo");
	}

	/**
	 * Delete NFO old location, change attached file, then save to new location
	 * @param file new file to attach
	 */
	public void relocate(final Session session, final File file)
	{
		ProfileNFO.getFileNfo(session, this.file).delete();
		this.file = file;
		name = file.getName();
		save(session);
	}

	/**
	 * Load or create a ProfileNFO from an attached file
	 * @param file the attached file from which to derive NFO file
	 * @return the {@link ProfileNFO}
	 */
	public static ProfileNFO load(final Session session, final File file)
	{
		final File filenfo = ProfileNFO.getFileNfo(session, file);
		if(filenfo.lastModified() >= file.lastModified()) // $NON-NLS-1$
		{
			try(ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filenfo))))
			{
				ProfileNFO nfo = (ProfileNFO) ois.readObject();
				if(nfo.file!=null)
					return nfo;
			}
			catch(final Throwable e)
			{
			//	Log.err(e.getMessage(),e);
			}
		}
		return new ProfileNFO(file);
	}

	/**
	 * Save this ProfileNFO, and save JRM if the attached file is a JRM type file
	 */
	public void save(final Session session)
	{
		if(isJRM()) try
		{
			long modified = file.lastModified();
			saveJrm(file, mame.fileroms, mame.filesl);
			if(modified!=0)
				file.setLastModified(modified);
		}
		catch (ParserConfigurationException | TransformerException e)
		{
			Log.err(e.getMessage(),e);
		}
		try(ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(ProfileNFO.getFileNfo(session, file)))))
		{
			oos.writeObject(this);
		}
		catch(final Throwable e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	/**
	 * Does the attached file is a JRM file
	 * @return true if it's a JRM file
	 */
	public boolean isJRM()
	{
		return FilenameUtils.getExtension(file.getName()).equals("jrm"); //$NON-NLS-1$
	}

	/**
	 * Load JRM file and fill up {@link #mame}
	 * @param jrmfile the JRM file to load
	 */
	public void loadJrm(final File jrmfile)
	{
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		try
		{
			final SAXParser parser = factory.newSAXParser();
			parser.parse(jrmfile, new DefaultHandler()
			{
				private boolean in_jrm = false;

				@Override
				public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
				{
					if(qName.equalsIgnoreCase("JRomManager")) //$NON-NLS-1$
					{
						in_jrm = true;
					}
					else if(qName.equalsIgnoreCase("Profile") && in_jrm) //$NON-NLS-1$
					{
						for(int i = 0; i < attributes.getLength(); i++)
						{
							switch(attributes.getQName(i).toLowerCase())
							{
								case "roms": //$NON-NLS-1$
									mame.fileroms = new File(jrmfile.getParentFile(), attributes.getValue(i));
									break;
								case "sl": //$NON-NLS-1$
									mame.filesl = new File(jrmfile.getParentFile(), attributes.getValue(i));
									break;
							}
						}
					}
				}

				@Override
				public void endElement(final String uri, final String localName, final String qName) throws SAXException
				{
					if(qName.equalsIgnoreCase("JRomManager")) //$NON-NLS-1$
					{
						in_jrm = false;
					}
				}
			});
		}
		catch(ParserConfigurationException | SAXException | IOException e)
		{
//			JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			Log.err(e.getMessage(),e);
		}
	}

	/**
	 * save mame infos in JRM file
	 * @param JrmFile the JRM file to save
	 * @param roms_file the mame roms file
	 * @param sl_file the software list file
	 * @return return the {@code JrmFile}
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static File saveJrm(final File JrmFile, final File roms_file, final File sl_file) throws ParserConfigurationException, TransformerException
	{
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		final Document doc = docBuilder.newDocument();
		final Element rootElement = doc.createElement("JRomManager"); //$NON-NLS-1$
		doc.appendChild(rootElement);
		final Element profile = doc.createElement("Profile"); //$NON-NLS-1$
		profile.setAttribute("roms", roms_file.getName()); //$NON-NLS-1$
		if(sl_file != null)
			profile.setAttribute("sl", sl_file.getName()); //$NON-NLS-1$
		rootElement.appendChild(profile);
		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		final Transformer transformer = transformerFactory.newTransformer();
		final DOMSource source = new DOMSource(doc);
		final StreamResult result = new StreamResult(JrmFile);
		transformer.transform(source, result);
		return JrmFile;
	}
	
	/**
	 * Delete all related files to 
	 * @return true on success
	 */
	public boolean delete()
	{
		if(file.delete())
		{
			mame.delete();
			new File(file.getAbsolutePath() + ".cache").delete(); //$NON-NLS-1$
			new File(file.getAbsolutePath() + ".nfo").delete(); //$NON-NLS-1$
			new File(file.getAbsolutePath() + ".properties").delete(); //$NON-NLS-1$
			return true;
		}
		return false;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getVersion()
	{
		return toHTML(stats.version == null ? toGray("???") : toNoBR(stats.version)); //$NON-NLS-1$
	}
	
	public String getHaveSets()
	{
		return toHTML(stats.haveSets == null ? (stats.totalSets == null ? toGray("?/?") : String.format("%s/%d", toGray("?"), stats.totalSets)) : String.format("%s/%d", stats.haveSets == 0 && stats.totalSets > 0 ? toRed("0") : (stats.haveSets.equals(stats.totalSets) ? toGreen(stats.haveSets + "") : toOrange(stats.haveSets + "")), stats.totalSets)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	}
	
	public String getHaveRoms()
	{
		return toHTML(stats.haveRoms == null ? (stats.totalRoms == null ? toGray("?/?") : String.format("%s/%d", toGray("?"), stats.totalRoms)) : String.format("%s/%d", stats.haveRoms == 0 && stats.totalRoms > 0 ? toRed("0") : (stats.haveRoms.equals(stats.totalRoms) ? toGreen(stats.haveRoms + "") : toOrange(stats.haveRoms + "")), stats.totalRoms)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	}
	
	public String getHaveDisks()
	{
		return toHTML(stats.haveDisks == null ? (stats.totalDisks == null ? toGray("?/?") : String.format("%s/%d", toGray("?"), stats.totalDisks)) : String.format("%s/%d", stats.haveDisks == 0 && stats.totalDisks > 0 ? toRed("0") : (stats.haveDisks.equals(stats.totalDisks) ? toGreen(stats.haveDisks + "") : toOrange(stats.haveDisks + "")), stats.totalDisks)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	}
	
	public String getCreated()
	{
		return toHTML(stats.created == null ? toGray("????-??-?? ??:??:??") : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(stats.created)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getScanned()
	{
		return toHTML(stats.scanned == null ? toGray("????-??-?? ??:??:??") : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(stats.scanned)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getFixed()
	{
		return toHTML(stats.fixed == null ? toGray("????-??-?? ??:??:??") : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(stats.fixed)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static List<ProfileNFO> list(Session session, File dir)
	{
		List<ProfileNFO> rows = new ArrayList<>();
		if(dir != null && dir.exists())
		{
			File filedir = dir;
			if(filedir!=null)
			{
				File[] files = filedir.listFiles((FilenameFilter) (dir1, name) -> {
					final File f = new File(dir1, name);
					if(f.isFile())
						if(!Arrays.asList("cache", "properties", "nfo", "jrm1", "jrm2").contains(FilenameUtils.getExtension(name))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							return true;
					return false;
				});
				if(files!=null)
				{
					Arrays.asList(files).stream().map(f -> {
						return ProfileNFO.load(session, f);
					}).forEach(pnfo -> {
						rows.add(pnfo);
					});
				}
			}
		}
		return rows;
	}
}
