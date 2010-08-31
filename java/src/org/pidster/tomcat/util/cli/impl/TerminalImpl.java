/**
 * 
 */
package org.pidster.tomcat.util.cli.impl;

import java.util.Map;

import org.pidster.tomcat.util.cli.Command;
import org.pidster.tomcat.util.cli.CommandConfig;
import org.pidster.tomcat.util.cli.CommandLine;
import org.pidster.tomcat.util.cli.CommandProcessor;
import org.pidster.tomcat.util.cli.Option;
import org.pidster.tomcat.util.cli.OptionParser;
import org.pidster.tomcat.util.cli.Terminal;
import org.pidster.tomcat.util.cli.commands.HelpCommand;

/**
 * @author pidster
 */
public class TerminalImpl implements Terminal {

    private final CommandProcessor processor;
    private final CommandRegistryImpl registry;
    private final OptionParser parser;
    private final EnvironmentImpl environmentImpl;

    /**
     * 
     */
    public TerminalImpl() {

        this.registry = new CommandRegistryImpl();
        this.processor = new CommandProcessorImpl();
        this.environmentImpl = new EnvironmentImpl();
        this.parser = new OptionParser(registry.getOptions());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.Terminal#register(java.lang.Iterable)
     */
    @Override
    public void register(Iterable<Command> commands) {

        for (Command command : commands) {
            this.registry.register(command);
        }

        // Deliberately override any other help commands
        this.registry.register("help", new HelpCommand(registry.commands()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pidster.tomcat.util.cli.Terminal#process(java.lang.String[])
     */
    @Override
    public void process(String[] arguments) {

        CommandLine line = processor.parseArguments(arguments);
        boolean interactive = processor.isInteractive();

        // Is there a more elegant solution?
        // If it's the first time, or we're interactive
        while (processor.first() || interactive) {

            // check this first, just in case
            if (processor.isExit())
                break;

            // if there's no command and we're not interactive
            if (!line.hasCommand() && (!interactive)) {
                environmentImpl.sysout("Usage: \n");
                break;
            }

            // if we have a command and we didn't match it
            if (line.hasCommand()
                    && !registry.isRegistered(line.getCommandName())) {
                environmentImpl.sysout("Command '%s' not found\n",
                        line.getCommandName());
            }

            // if we found a command
            else if (registry.isRegistered(line.getCommandName())) {

                Command command = registry.get(line.getCommandName());

                try {

                    Map<Option, String> activeOptions = parser.activeOptions(
                            line.getOptions(), command);

                    CommandConfig config = new CommandConfigImpl(
                            environmentImpl, line.getArguments(), activeOptions);

                    command.configure(config);

                    command.execute();
                }
                catch (Throwable t) {
                    environmentImpl.sysout(t);
                }
                finally {
                    command.cleanup();
                }
            }

            // Update the command line, if we're still running
            if (interactive)
                line = processor.parseArguments(environmentImpl.readPrompt());
        }
    }

}
