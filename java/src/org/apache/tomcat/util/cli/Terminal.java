/**
 * 
 */
package org.apache.tomcat.util.cli;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.tomcat.util.cli.commands.HelpCommand;

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

            adjustClassLoader();

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
    private static void adjustClassLoader() throws MalformedURLException {
        URL[] urls = new URL[0];

        String catalinaHome = System.getenv("CATALINA_HOME");

        if (catalinaHome == null) {
            catalinaHome = System.getProperty("catalina.home");
        }

        if ((catalinaHome != null) && (!catalinaHome.isEmpty())) {
            String libDir = catalinaHome + File.separator + "lib";

            System.out.println("lib:" + libDir);

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

    private final Map<String, Command> registry;

    /**
     * 
     */
    public Terminal() {

        this.registry = new HashMap<String, Command>();

        ServiceLoader<Command> loader = ServiceLoader.load(Command.class);

        for (Command command : loader) {
            if (command.getClass().isAnnotationPresent(Descriptor.class)) {
                Descriptor x = command.getClass().getAnnotation(
                        Descriptor.class);
                if (!registry.containsKey(x.name())) {
                    registry.put(x.name(), command);
                }
            }
        }

        // Deliberately override any other help commands
        registry.put("help", new HelpCommand(this.registry.values()));
    }

    /**
     * @param arguments
     */
    public void process(String[] arguments) {

        CommandProcessor processor = new CommandProcessor(registry);

        Env env2 = processor.getEnvironment(arguments);

        CommandParser parser = new CommandParser(registry, arguments);
        boolean interactive = parser.isInteractive();

        // Is there a more elegant solution?
        // If it's the first time, or we're interactive
        int index = 0;

        while ((index == 0) || interactive) {

            index++;

            // check this first, just in case
            if (parser.isExit())
                break;

            Environment env = parser.getEnvironment();

            if (!parser.hasCommand()) {
                env.sysout("Usage: ");
            }
            else if (parser.foundCommand()) {

                Command command = parser.getCommand();

                if (parser.isVerbose())
                    env.sysout("Initialising command '%s'...\n",
                            parser.getCommandName());

                command.init(env);

                if (parser.isVerbose())
                    env.sysout("Executing '%s'...\n", parser.getCommandName());

                try {
                    command.execute();
                }
                catch (Throwable t) {
                    env.sysout(t);
                }
            }
            else {
                env.sysout("Command '%s' not found\n", parser.getCommandName());
            }

            if (interactive) {
                env2 = processor.getEnvironment(env2.readPrompt());
                parser = new CommandParser(registry, env.readPrompt());
            }
        }
    }
}
