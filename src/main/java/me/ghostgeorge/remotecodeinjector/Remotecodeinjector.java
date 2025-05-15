package me.ghostgeorge.remotecodeinjector;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public final class Remotecodeinjector extends JavaPlugin {
    // key variable, gets changed with remote injection
    public static Boolean key = false;
    public static String pluginMessage = "";
    public static String serverurl = "";

    @Override
    public void onEnable() {
        // Plugin startup logic
        //start of injection
        /*
        key = true;
        */
        //end of injection
        // Executes license check method
        executeRemote();
        // Checks server response
        if (key == true) {
            getLogger().warning("Authentication successful");
            getLogger().warning("Code successfully injected from web");
            return;
        }
        // If auth fails
        getLogger().warning("Authentication failed. Contact plugin developer");
        getLogger().warning("Disabling plugin");
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().warning("Plugin disabled");
    }

    public boolean executeRemote() {
        try {
            // Contact the server
            URL url = new URL(serverurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder script = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }
            reader.close();

            // Rhino: create context and execute
            Context ctx = Context.enter();
            ctx.setOptimizationLevel(-1); // Compatibility mode
            Scriptable scope = ctx.initStandardObjects();

            // Bind variables
            ScriptableObject.putProperty(scope, "key", key);
            ScriptableObject.putProperty(scope, "pluginMessage", pluginMessage);

            // Evaluate the script
            ctx.evaluateString(scope, script.toString(), "remote.js", 1, null);

            // Read values back
            Object keyObj = ScriptableObject.getProperty(scope, "key");
            Object pluginMsgObj = ScriptableObject.getProperty(scope, "pluginMessage");

            if (keyObj instanceof Boolean) {
                key = (Boolean) keyObj;
            }
            if (pluginMsgObj instanceof String) {
                pluginMessage = (String) pluginMsgObj;
            }

        } catch (Exception e) {
            getLogger().severe("Failed to run remote code: " + e.getMessage());
        } finally {
            Context.exit();
        }
        return false;
    }
}
