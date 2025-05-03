package common.commands;

import server.managers.CommandManager;
import client.utility.console.Console;

public class History extends Command {
    private final Console console;
    private final CommandManager commandManager;

    public History(Console console, CommandManager commandManager) {
        super("history", "output the last 5 common.commands (without arguments)");
        this.console = console;
        this.commandManager = commandManager;
    }

    @Override
    public boolean apply(String[] arguments) {
        if(!arguments[1].isEmpty()){
            console.println("Utilization: " + getName());
            return false;
        }
        console.println("Last common.commands:");
        for(String cmd : commandManager.getCommandHistory()){
            console.println(cmd);
        }
        return true;
    }
}
