package mobi.chouette.exchange.gtfs.importer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.naming.InitialContext;

import lombok.extern.log4j.Log4j;
import mobi.chouette.common.Color;
import mobi.chouette.common.Context;
import mobi.chouette.common.FileUtils;
import mobi.chouette.common.chain.Command;
import mobi.chouette.common.chain.CommandFactory;
import mobi.chouette.exchange.gtfs.Constant;
import mobi.chouette.exchange.gtfs.parser.GtfsAgencyParser;
import mobi.chouette.exchange.gtfs.parser.GtfsCalendarParser;
import mobi.chouette.exchange.gtfs.parser.GtfsRouteParser;
import mobi.chouette.exchange.gtfs.parser.GtfsStopParser;
import mobi.chouette.exchange.gtfs.parser.GtfsTransferParser;
import mobi.chouette.exchange.gtfs.parser.GtfsTripParser;
import mobi.chouette.exchange.importer.ParserFactory;
import mobi.chouette.exchange.report.ActionReport;
import mobi.chouette.exchange.report.FileInfo;
import mobi.chouette.exchange.report.FileInfo.FILE_STATE;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@Log4j
public class GtfsValidationCommand implements Command, Constant {
	
	public static final String COMMAND = "GtfsValidationCommand";
	
	private static final List<String> processableFiles = Arrays.asList(
		  "agency.txt", 
		  "stops.txt", 
		  "routes.txt", 
		  "trips.txt",
		  "stop_times.txt",
		  "calendar.txt",
		  "calendar_dates.txt",
		  "frequencies.txt",
		  "transfers.txt"
		);

	@Override
	public boolean execute(Context context) throws Exception {		
		boolean result = ERROR;
		
		Monitor monitor = MonitorFactory.start(COMMAND);
		
		// check ignored files
		Path path = Paths.get(context.get(PATH).toString(), INPUT);
		List<Path> list = FileUtils
				.listFiles(path, "*");
		ActionReport report = (ActionReport) context.get(REPORT);
		for (Path fileName : list) {
			if (!processableFiles.contains(fileName.getName(-1).toString()))
			{
				FileInfo file = new FileInfo();
				file.setName(fileName.getName(-1).toString());
				file.setStatus(FILE_STATE.UNCHECKED);
				report.getFiles().add(file);
			}
		}
		try {
			
			// agency.txt
			GtfsAgencyParser agencyParser = (GtfsAgencyParser) ParserFactory
					.create(GtfsAgencyParser.class.getName());
			agencyParser.validate(context);

			// stops.txt
			GtfsStopParser stopParser = (GtfsStopParser) ParserFactory
					.create(GtfsStopParser.class.getName());
			stopParser.validate(context);

			// route.txt
			GtfsRouteParser routeParser = (GtfsRouteParser) ParserFactory
					.create(GtfsRouteParser.class.getName());
			routeParser.validate(context);

			// trips.txt & stop_times.txt & frequency.txt
			GtfsTripParser tripParser = (GtfsTripParser) ParserFactory
					.create(GtfsTripParser.class.getName());
			tripParser.validate(context);

			// calendar.txt & calendar_dates.txt & frequencies.txt
			GtfsCalendarParser calendarParser = (GtfsCalendarParser) ParserFactory
					.create(GtfsCalendarParser.class.getName());
			calendarParser.validate(context);

			// transfers.txt
			GtfsTransferParser transferParser = (GtfsTransferParser) ParserFactory
					.create(GtfsTransferParser.class.getName());
			transferParser.validate(context);

			result = SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
		
		log.info(Color.MAGENTA + monitor.stop() + Color.NORMAL);

		return result;
	}

	public static class DefaultCommandFactory extends CommandFactory {

		@Override
		protected Command create(InitialContext context) throws IOException {
			Command result = new GtfsValidationCommand();
			return result;
		}
	}

	static {
		CommandFactory.factories.put(GtfsValidationCommand.class.getName(),
				new DefaultCommandFactory());
	}

}
