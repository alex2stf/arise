package com.arise.canter;

import com.arise.core.tools.MapUtil;

import java.util.List;
import java.util.Map;

import static com.arise.core.tools.StringUtil.hasText;
import static com.arise.core.tools.ThreadUtil.startDaemon;

/**
 * Created by alex2 on 20/03/2024.
 */
public class JsonCommand extends Command<Object>{
    List<Map> cmds;
    String retVal;
    public JsonCommand(String id) {
        super(id);
    }

    private void storeResultIfNecessary(Object o, Map c){
        String storeKey = MapUtil.getString(c, "store-key");
        if (hasText(storeKey)){
            getRegistry().store(storeKey, o);
        }
    }


    @Override
    public Object execute(final List<String> arguments) {
        final Object res[] = new Object[]{null};
        for (final Map c: cmds){
            final String commandId = MapUtil.getString(c, "id");
            final List<String> args = MapUtil.getList(c, "args");
            String asyncMode = MapUtil.getString(c, "async");

            if ("daemon".equalsIgnoreCase(asyncMode)){
                startDaemon(new Runnable() {
                    @Override
                    public void run() {
                        res[0] = getRegistry().execute(commandId, Command.parseArgs(args, arguments));
                        storeResultIfNecessary(res[0], c);
                    }
                }, ("async-cmd-" + commandId));
            } else {
                res[0] = getRegistry().execute(commandId, Command.parseArgs(args, arguments));
                storeResultIfNecessary(res[0], c);
            }



        }
        if(hasText(retVal)) {
            return getRegistry().executeCommandLine(retVal);
        }
        return (res[0]);
    }
}
