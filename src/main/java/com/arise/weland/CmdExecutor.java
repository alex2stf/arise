package com.arise.weland;

import com.arise.canter.CommandRegistry;

import java.io.File;

import static com.arise.canter.DefaultCommands.PROCESS_EXEC;

public class CmdExecutor {

    public static void main(String[] args) {
        CommandRegistry.getInstance()
                .addCommand(PROCESS_EXEC)
                .registerDefaultCommands()
        ;
        CommandRegistry.getInstance().loadJsonResource("src/main/resources#weland/config/commands/commands_win_edge.json");
        CommandRegistry.getInstance().execute("set-desktop-background", new String[]{"termen", "text",
            new File("src/main/resources/python_scripts").getAbsolutePath()
        });
    }
}
