/**
 * 
 */
package org.pidster.tomcat.util.cli;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.pidster.tomcat.util.cli.commands.HelpCommand;

/**
 * @author pidster
 */
public class Terminal {

    /**
     * @param arguments
     */
    public static void main(String[] arguments) {

        try {
            if (arguments == null)
                arguments = new String[0];

            modifyClassLoader();

            Terminal terminal = new Terminal();
            terminal.process(arguments);

        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws MalformedURLException
     */
    private static void modifyClassLoader() throws MalformedURLException {
        URL[] urls = new URL[0];

        String catalinaHome = System.getenv("CATALINA_HOME");

        if (catalinaHome == null) {
            catalinaHome = System.getProperty("catalina.home");
        }

        if ((catalinaHome != null) && (!catalinaHome.isEmpty())) {
            String libDir = catalinaHome + File.separator + "lib";

            File libs = new File(libDir);
            File[] files = libs.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith(".jar")) {
                        return true;
                    }
                    return false;
                }
            });

            List<URL> jars = new ArrayList<URL>();
            for (File f : files) {
                jars.add(f.toURI().toURL());
            }

            urls = new URL[jars.size()];
            urls = jars.toArray(urls);
        }

        Thread t = Thread.currentThread();
        ClassLoader cl = t.getContextClassLoader();
        TerminalLoader tl = new TerminalLoader(urls, cl);
        t.setContextClassLoader(tl);
    }

    // ---------------------------------------------------------------------

    private final CommandProcessor processor;
    private final CommandRegistry registry;
    private final OptionParser parser;
    private final Environment environment;

    /**
     * 
     */
    public Terminal() {

        this.registry = new CommandRegistry();

        ServiceLoader<Command> loader = ServiceLoader.load(Command.class);

        for (Command command : loader) {
            this.registry.register(command);
        }

        // Deliberately override any other help commands
        this.registry.register("help", new HelpCommand(registry.commands()));
        this.parser = new OptionParser(registry.getOptions());
        this.processor = new CommandProcessor();
        this.environment = new Environment();
    }

    /**
     * @param arguments
     */
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
                environment.sysout("Usage: \n");
                break;
            }

            // if we have a command and we didn't match it
            if (line.hasCommand()
                    && !registry.isRegistered(line.getCommandName())) {
                environment.sysout("Command '%s' not found\n",
                        line.getCommandName());
            }

            // if we found a command
            else if (registry.isRegistered(line.getCommandName())) {

                Command command = registry.get(line.getCommandName());

                try {

                    Map<Option, String> activeOptions = parser.activeOptions(
                            line.getOptions(), command);

                    CommandConfig config = new CommandConfig(environment,
                            line.getArguments(), activeOptions);

                    command.configure(config);

                    command.execute();
                }
                catch (Throwable t) {
                    environment.sysout(t);
                }
                finally {
                    command.cleanup();
                }
            }

            // Update the command line, if we're still running
            if (interactive)
                line = processor.parseArguments(environment.readPrompt());
        }
    }

}
