package jrm.server.ws;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

import jrm.misc.BreakException;
import jrm.profile.Profile;
import jrm.profile.scan.Scan;
import jrm.server.WebSession;

public class ProfileWS
{
	private final WebSckt ws;

	public ProfileWS(WebSckt ws)
	{
		this.ws = ws;
	}

	void load(JsonObject jso)
	{
		(ws.session.worker = new Worker(()->{
			WebSession session = ws.session;
			if (session.curr_profile != null)
				session.curr_profile.saveSettings();
			session.worker.progress = new ProgressWS(ws);
			try
			{
				session.curr_profile = Profile.load(session, new File(jso.get("params").asObject().getString("path", null)), session.worker.progress);
				session.curr_profile.nfo.save(session);
				session.report.setProfile(session.curr_profile);
			}
			catch(BreakException ex)
			{
			}
			session.worker.progress.close();
			session.worker.progress = null;
			session.lastAction = new Date();
			loaded(session.curr_profile);
			new CatVerWS(ws).loaded(session.curr_profile);
			new NPlayersWS(ws).loaded(session.curr_profile);
		})).start();
	}
	
	void scan(JsonObject jso)
	{
		(ws.session.worker = new Worker(()->{
			WebSession session = ws.session;
			session.worker.progress = new ProgressWS(ws);
			try
			{
				session.curr_scan = new Scan(session.curr_profile, session.worker.progress);
			}
			catch(BreakException ex)
			{
			}
			session.worker.progress.close();
			session.worker.progress = null;
			session.lastAction = new Date();
			scanned(session.curr_scan);
		})).start();
	}
	
	void setProperty(JsonObject jso)
	{
		JsonObject pjso = jso.get("params").asObject();
		for(Member m : pjso)
		{
			JsonValue value = m.getValue();
			if(value.isBoolean())
				ws.session.curr_profile.setProperty(m.getName(), value.asBoolean());
			else if(value.isString())
				ws.session.curr_profile.setProperty(m.getName(), value.asString());
			else
				ws.session.curr_profile.setProperty(m.getName(), value.toString());
		}
	}
	
	@SuppressWarnings("serial")
	void loaded(final Profile profile)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Profile.loaded");
					add("params", new JsonObject() {{
						add("success", profile!=null);
						if(profile!=null)
						{
							add("name", profile.getName());
							if(profile.systems!=null)
							{
								add("systems", new JsonArray() {{
									profile.systems.forEach(s-> add(new JsonObject() {{
										add("name", s.toString());
										add("selected", s.isSelected(profile));
										add("property", s.getPropertyName());
										add("type", s.getType().toString());
									}}));
								}});
							}
							if(profile.settings!=null)
								add("settings",profile.settings.asJSO());
						}
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	
	@SuppressWarnings("serial")
	void scanned(final Scan scan)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Profile.scanned");
					add("params", new JsonObject() {{
						add("success", scan!=null);
						if(scan!=null)
							add("actions", scan.actions.stream().mapToInt(Collection::size).sum());
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
