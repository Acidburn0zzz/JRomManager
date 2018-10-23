package jrm.server.datasources;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.Machine;
import jrm.profile.data.MachineList;
import jrm.server.datasources.XMLRequest.Operation;
import jrm.server.datasources.XMLRequest.Operation.Sorter;

public class AnywareListXMLResponse extends XMLResponse
{

	public AnywareListXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	private AnywareList<?> get_list(Operation operation) throws Exception
	{
		String list = operation.data.get("list");
		final AnywareList<?> al;
		if(list==null)
			al = null;
		else if(list.equals("*"))
			al = request.session.curr_profile.machinelist_list.get(0);
		else
			al = request.session.curr_profile.machinelist_list.softwarelist_list.getByName(list);
		return al;
	}
	
	private void write_record(final AnywareList<?> al, final Anyware aw)
	{
		try
		{
			writer.writeEmptyElement("record");
			writer.writeAttribute("list", al instanceof MachineList?"*":al.getBaseName());
			writer.writeAttribute("status", aw.getStatus().toString());
			writer.writeAttribute("name", aw.getBaseName());
			writer.writeAttribute("description", aw.getDescription().toString());
			writer.writeAttribute("have",  String.format("%d/%d", aw.countHave(), aw.countAll()));
			if(aw.cloneof!=null)
			{
				writer.writeAttribute("cloneof",  aw.cloneof);
				writer.writeAttribute("cloneof_status",  al.getByName(aw.cloneof).getStatus().toString());
			}
			if(aw instanceof Machine)
			{
				Machine m = (Machine)aw;
				MachineList ml = (MachineList)al;
				if(m.isbios)
					writer.writeAttribute("type", "BIOS");
				else if(m.isdevice)
					writer.writeAttribute("type", "DEVICE");
				else if(m.ismechanical)
					writer.writeAttribute("type", "MECHANICAL");
				else
					writer.writeAttribute("type", "STANDARD");
				if(m.romof!=null)
				{
					writer.writeAttribute("romof",  m.romof);
					writer.writeAttribute("romof_status",  al.getByName(m.romof).getStatus().toString());
				}
				if(m.sampleof!=null)
				{
					writer.writeAttribute("sampleof",  m.sampleof);
					writer.writeAttribute("sampleof_status", ml.samplesets.getByName(m.sampleof).getStatus().toString());
				}
			}
			writer.writeAttribute("selected", Boolean.toString(aw.selected));
		}
		catch (XMLStreamException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected void fetch(Operation operation) throws Exception
	{
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		final boolean reset = Boolean.valueOf(operation.data.get("reset"));
		final AnywareList<?> al = get_list(operation);
		if(al != null)
		{
			if(reset)
				al.reset();
			final String lname = operation.data.containsKey("name")?operation.data.get("name").toLowerCase():null;
			final String ldesc = operation.data.containsKey("description")?operation.data.get("description").toLowerCase():null;
			final List<Anyware> list = al.getFilteredList().stream().filter(machine -> {
				if(lname!=null)
					if(!machine.getBaseName().toLowerCase().contains(lname))
						return false;
				if(ldesc!=null)
					if(!machine.description.toString().toLowerCase().contains(ldesc))
						return false;
				return true;
			}).sorted((o1,o2)->{
				if(operation.sort.size()>0)
				{
					for(Sorter s : operation.sort)
					{
						switch(s.name)
						{
							case "name":
							{
								int ret = (s.desc ? o2 : o1).getBaseName().compareToIgnoreCase((s.desc ? o1 : o2).getBaseName());
								if (ret != 0)
									return ret;
								break;
							}
							case "description":
							{
								int ret = (s.desc ? o2 : o1).description.toString().compareToIgnoreCase((s.desc ? o1 : o2).description.toString());
								if (ret != 0)
									return ret;
								break;
							}
						}
					}
					return 0;
				}
				else
					return o1.getBaseName().compareToIgnoreCase(o2.getBaseName());
			}).collect(Collectors.toList());
			fetch_array(operation, list.size(), (i, count) -> {
				write_record(al, list.get(i));
			});
		}
		writer.writeEndElement();
	}
	
	@Override
	protected void update(Operation operation) throws Exception
	{
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		final AnywareList<?> al = get_list(operation);
		if(al != null)
		{
			String name = operation.data.get("name");
			if(name != null)
			{
				Anyware aw = al.getByName(name);
				if(aw != null)
				{
					Boolean selected = Boolean.valueOf(operation.data.get("selected"));
					if(selected != null)
						aw.selected = selected;
					write_record(al, aw);
				}
			}
		}
		writer.writeEndElement();
	}
}
