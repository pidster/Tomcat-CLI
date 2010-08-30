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
import java.util.Set;

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

    /**
     * 
     */
    public Terminal() {

        registry = new CommandRegistry();

        ServiceLoader<Command> loader = ServiceLoader.load(Command.class);

        for (Command command : loader) {
            registry.register(command);
        }

        // Deliberately override any other help commands
        registry.register("help", new HelpCommand(registry.commands()));
        processor = new CommandProcessor();
    }

    /**
     * @param arguments
     */
    public void process(String[] arguments) {

        Environment environment = new Environment();

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

                    Class<?> c = command.getClass();
                    Descriptor d = c.getAnnotation(Descriptor.class);

                    for (Option option : d.options()) {

                        if (line.isOptionSet(option.extended())
                                || line.isOptionSet(String.valueOf(option
                                        .trigger()))) {
                            continue;
                        }

                        if (option.required()) {
                            // ERROR!
                        }
                    }

                    Map<Option, String> options = matchOptions(registry, line);

                    CommandConfig config = createConfig(environment, line);

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

    /**
     * @param registry
     * @param line
     * @return
     */
    private Map<Option, String> matchOptions(CommandRegistry registry,
            CommandLine line) {

        Set<String> optionNames = line.getOptions().keySet();
        for (String optionName : optionNames) {
            if (registry.hasOption(line.getCommandName(), optionName)) {

            }
        }

        return null;
    }

    /**
     * @param env
     * @param cl
     * @return
     */
    private CommandConfig createConfig(Environment env, CommandLine cl) {

        CommandConfig c = new CommandConfig(env, cl.getArguments(),
                cl.getOptions());

        return c;
    }
}
