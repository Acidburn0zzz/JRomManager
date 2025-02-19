package jrm.server.datasources;

import java.io.File;

import jrm.profile.report.Note;
import jrm.profile.report.Report;
import jrm.profile.report.Subject;
import jrm.profile.report.SubjectSet;
import jrm.server.datasources.XMLRequest.Operation;

public class ReportTreeXMLResponse extends XMLResponse
{

	public ReportTreeXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}

	@Override
	protected void fetch(Operation operation) throws Exception
	{
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		
		Report report = request.session.report;
		if(operation.hasData("src"))
		{
			final File srcfile = new File(operation.getData("src"));
			final File reportfile = Report.getReportFile(request.session, srcfile);
			if(request.session.tmp_report==null || !(request.session.tmp_report.getReportFile(request.session).equals(reportfile) && request.session.tmp_report.getFileModified()==reportfile.lastModified()))
				request.session.tmp_report = Report.load(request.session, srcfile);
			report = request.session.tmp_report;
		}
		
		int parentID = Integer.valueOf(operation.getData("ParentID"));
		if(parentID==0)
		{
			int start, end;
			int nodecount = report.getHandler().getFilteredReport().size();
			writer.writeElement("startRow", Integer.toString(start=Math.min(nodecount-1,operation.startRow)));
			writer.writeElement("endRow", Integer.toString(end=Math.min(nodecount-1,operation.endRow)));
			writer.writeElement("totalRows", Integer.toString(nodecount));
	
			if(nodecount>0)
			{
				writer.writeStartElement("data");
				for(int i = start; i <= end; i++)
				{
					Subject s = report.getHandler().getFilteredReport().get(i);
					writer.writeStartElement("record");
					writer.writeAttribute("ID", Integer.toString(s.getId()));
					writer.writeAttribute("ParentID", Integer.toString(parentID));
					writer.writeAttribute("title", s.getHTML());
					writer.writeAttribute("class", s.getClass().getSimpleName());
					if(s instanceof SubjectSet)
					{
						writer.writeAttribute("status", ((SubjectSet)s).getStatus().toString());
						writer.writeAttribute("hasNotes", Boolean.toString(((SubjectSet)s).hasNotes()));
						writer.writeAttribute("isFixable", Boolean.toString(((SubjectSet)s).isFixable()));
					}
					writer.writeAttribute("isFolder", Boolean.toString(s.getNotes().size()!=0));
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
		}
		else
		{
			Subject subject = report.getHandler().getFilteredReport().findSubject(parentID);
			if(subject!=null)
			{
				int nodecount = subject.size();
				writer.writeElement("startRow", "0");
				writer.writeElement("endRow", Integer.toString(nodecount-1));
				writer.writeElement("totalRows", Integer.toString(nodecount));
				writer.writeStartElement("data");
				for(Note n : subject)
				{
					writer.writeStartElement("record");
					writer.writeAttribute("ID", Integer.toString(n.getId()));
					writer.writeAttribute("ParentID", Integer.toString(parentID));
					writer.writeAttribute("title", n.getHTML());
					writer.writeAttribute("class", n.getClass().getSimpleName());
					writer.writeAttribute("isFolder", Boolean.toString(false));
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
		}
		writer.writeEndElement();
	}
}
