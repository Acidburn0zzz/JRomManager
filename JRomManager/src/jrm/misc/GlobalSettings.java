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
package jrm.misc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.zip.CRC32;

import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.security.User;

/**
 * The Settings back-end
 * @author optyfr
 *
 */
public class GlobalSettings extends Settings
{
	public final User parent;
	/**
	 * @param parent
	 */
	public GlobalSettings(User parent)
	{
		super();
		this.parent = parent;
		loadSettings();
	}
	
	/**
	 * Return the current work path, the one where we save working dirs (xml, cache, backup, ...)
	 * By default, this where the program reside... but if multiuser mode is enabled, it will be $HOME/.jrommanager (or %HOMEPATH%\.jrommanager for Windows)
	 * @return the current working path
	 */
	public Path getWorkPath()
	{
		if(parent.parent.server)
		{
			final String prop = System.getProperty("jrommanager.dir");
			final Path work = (prop != null ? Paths.get(prop) : Paths.get(System.getProperty("user.dir"))).toAbsolutePath().normalize(); //$NON-NLS-1$ //$NON-NLS-2$
			if (!Files.exists(work))
			{
				try
				{
					Files.createDirectories(work);
				}
				catch (IOException e)
				{
					Log.err(e.getMessage(),e);
				}
			}
			return work;
		}
		else if (parent.parent.multiuser)
		{
			final Path work = Paths.get(System.getProperty("user.home"), ".jrommanager").toAbsolutePath().normalize(); //$NON-NLS-1$ //$NON-NLS-2$
			if (!Files.exists(work))
			{
				try
				{
					Files.createDirectories(work);
				}
				catch (IOException e)
				{
					Log.err(e.getMessage(),e);
				}
			}
			return work;
		}
		return Paths.get(".").toAbsolutePath().normalize(); //$NON-NLS-1$
	}
	
	public File getWorkFile(final File parent, final String name, final String ext)
	{
		if(!parent.getAbsoluteFile().toPath().startsWith(getWorkPath().toAbsolutePath()))
		{
			final CRC32 crc = new CRC32();
			crc.update(new File(parent, name).getAbsolutePath().getBytes());
			final File work = getWorkPath().resolve("work").toFile(); //$NON-NLS-1$
			work.mkdirs();
			return new File(work, String.format("%08x", crc.getValue()) + ext); //$NON-NLS-1$
			
		}
		return new File(parent, name + ext); 
	}

	public String getLogPath()
	{
		Path path = getWorkPath().resolve("logs");
		if (!Files.exists(path))
		{
			try
			{
				Files.createDirectories(path);
			}
			catch (IOException e)
			{
				Log.err(e.getMessage(),e);
			}
		}
		return path.toString();
	}


	/**
	 * get the temporary path 
	 * @param local if local is true, then a "tmp" dir is created into current workpath (multiuser) or a subdir from default temp dir (not multiuser). If local is false, the default system temporary path is directly returned 
	 * @return a valid temporary path
	 */
	public Path getTmpPath(boolean local)
	{
		if(local)
		{
			if(parent.parent.multiuser)
			{
				try
				{
					return Files.createDirectories(getWorkPath().resolve("tmp")); //$NON-NLS-1$
				}
				catch (IOException e)
				{
					Log.err(e.getMessage(),e);
				}
			}
			try
			{
				return Files.createTempDirectory("JRM"); //$NON-NLS-1$
			}
			catch (IOException e)
			{
				Log.err(e.getMessage(),e);
			}
		}
		return Paths.get(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
	}
	
	
	/**
	 * get settings file
	 * @return a {@link File} wich is the settings file
	 */
	private File getSettingsFile()
	{
		final File workdir = getWorkPath().toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
		final File cachedir = new File(workdir, "settings"); //$NON-NLS-1$
		final File settingsfile = new File(cachedir, parent.name+".properties"); //$NON-NLS-1$
		settingsfile.getParentFile().mkdirs();
		return settingsfile;

	}

	/**
	 * save current settings to settings file
	 */
	public void saveSettings()
	{
		saveSettings(getSettingsFile());
	}

	/**
	 * load settings from settings file
	 */
	public void loadSettings()
	{
		loadSettings(getSettingsFile());
	}
	
	/**
	 * return a .cache file located at the same place than the provided profile file
	 * @param file the Profile info file from which the cache will be derived
	 * @return the cache file
	 */
	public File getCacheFile(final File file)
	{
		return getWorkFile(file.getParentFile(), file.getName(), ".cache");  //$NON-NLS-1$
	}

	/**
	 * return the properties file associated with profile file
	 * @param file the profile file
	 * @return the settings file
	 */
	public File getProfileSettingsFile(final File file)
	{
		return getWorkFile(file.getParentFile(), file.getName(), ".properties"); //$NON-NLS-1$
	}

	/**
	 * Save settings as XML
	 * @param file the file from which derive the {@link Properties} file
	 * @param settings the {@link Properties} to save, can be <code>null</code>
	 * @return the saved {@link Properties}
	 * @throws IOException
	 */
	public ProfileSettings saveProfileSettings(final File file, ProfileSettings settings) throws IOException
	{
		if (settings == null)
			settings = new ProfileSettings();
		settings.saveSettings(getProfileSettingsFile(file));
		return settings;
	}

	/**
	 * Load settings
	 * @param file the file from which derive the {@link Properties} file
	 * @param settings the {@link Properties} to load, can be <code>null</code>
	 * @return the loaded {@link Properties}
	 * @throws IOException
	 */
	public ProfileSettings loadProfileSettings(File file, ProfileSettings settings) throws IOException
	{
		if (settings == null)
			settings = new ProfileSettings();
		if (getProfileSettingsFile(file).exists())
			settings.loadSettings(getProfileSettingsFile(file));
		settings.merge_mode = MergeOptions.valueOf(settings.getProperty("merge_mode", MergeOptions.SPLIT.toString()));
		settings.hash_collision_mode = HashCollisionOptions.valueOf(settings.getProperty("hash_collision_mode", HashCollisionOptions.SINGLEFILE.toString()));
		settings.implicit_merge = settings.getProperty("implicit_merge", false);
		return settings;
	}

	@Override
	protected void propagate(String property, String value)
	{
		// TODO Auto-generated method stub
		
	}
}
