package mobi.chouette.exchange.neptune.importer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import lombok.extern.log4j.Log4j;
import mobi.chouette.common.Context;
import mobi.chouette.common.chain.Chain;
import mobi.chouette.common.chain.ChainImpl;
import mobi.chouette.common.chain.Command;
import mobi.chouette.common.chain.CommandFactory;
import mobi.chouette.exchange.importer.RegisterCommand;
import mobi.chouette.exchange.importer.TransactionnalCommand;
import mobi.chouette.exchange.importer.UncompressCommand;

@Stateless(name = MainCommand.COMMAND)
@Log4j
public class MainCommand implements Command, Constant {

	public static final String COMMAND = "MainCommand";

	@Override
	public boolean execute(Context context) throws Exception {
		boolean result = ERROR;

		try {
			InitialContext ctx = (InitialContext) context.get(INITIAL_CONTEXT);

			// uncompress data
			Command command = CommandFactory.create(ctx,
					UncompressCommand.class.getName());
			command.execute(context);

			Chain chain = new ChainImpl();

			Path path = Paths.get(context.get(PATH) + INPUT);
			DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.xml");
			for (Path file : stream) {

				context.put(FILE, path.toString());
				
				Chain transac = (Chain) CommandFactory.create(ctx,
						TransactionnalCommand.class.getName());

				// parser
				Command parser = CommandFactory.create(ctx,
						NeptuneParserCommand.class.getName());

				transac.add(parser);

				// register
//				Command register = CommandFactory.create(ctx,
//						RegisterCommand.class.getName());
//				transac.add(register);

				chain.add(transac);
			}

			chain.execute(context);

			result = SUCCESS;
		} catch (Exception e) {
			log.error(e);
		}

		return result;
	}

	public static class DefaultCommandFactory extends CommandFactory {

		@Override
		protected Command create(InitialContext context) throws IOException {
			Command result = null;
			try {
				result = (Command) context.lookup(JAVA_MODULE + COMMAND);
			} catch (NamingException e) {
				log.error(e);
			}
			return result;
		}
	}

	static {
		CommandFactory factory = new DefaultCommandFactory();
		CommandFactory.factories.put(MainCommand.class.getName(), factory);
	}
}
