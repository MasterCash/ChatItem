package me.dadus33.chatitem;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.events.ListenerPriority;
import me.dadus33.chatitem.commands.CIReload;
import me.dadus33.chatitem.filters.Log4jFilter;
import me.dadus33.chatitem.json.JSONManipulator;
import me.dadus33.chatitem.json.JSONManipulatorPost1_7_10;
import me.dadus33.chatitem.json.JSONManipulatorPre1_7_10;
import me.dadus33.chatitem.listeners.ChatEventListener;
import me.dadus33.chatitem.listeners.ChatPacketListener;
import me.dadus33.chatitem.utils.Config;
import me.dadus33.chatitem.utils.CustomConfig;
import me.dadus33.chatitem.utils.General;
import me.dadus33.chatitem.utils.Storage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import java.io.IOException;
import java.util.logging.Level;

public class ChatItem extends JavaPlugin {

    public final static int CFG_VER = 4;
    private static ChatItem instance;
    private ChatEventListener chatEventListener;
    private CustomConfig handler;
    private Log4jFilter filter;
    private Config config;
    private Storage storage;
    private ProtocolManager pm;
    private ChatPacketListener listener;
    private static JSONManipulator manip;
    public static boolean post17 = false;

    public static void reload(CommandSender sender) {
        ChatItem obj = getInstance();
        obj.handler = new CustomConfig(obj);
        obj.config = new Config("config");
        obj.pm = ProtocolLibrary.getProtocolManager();
        if (obj.config.file == null || obj.config.fileConfig == null) {
            obj.handler.saveDefaultConfig(obj.config);
        }
        obj.storage = new Storage(obj.config, obj.handler);
        General.init(obj.storage);
        General.checkConfigVersion();
        obj.listener.setStorage(obj.storage);
        obj.chatEventListener.setStorage(obj.storage);
        obj.filter.setStorage(obj.storage);
        if (!obj.storage.RELOAD_MESSAGE.isEmpty())
            sender.sendMessage(obj.storage.RELOAD_MESSAGE);
    }

    public static ChatItem getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        handler = new CustomConfig(this);
        config = new Config("config");
        pm = ProtocolLibrary.getProtocolManager();
        if (config.file == null || config.fileConfig == null) {
            handler.saveDefaultConfig(config);
        }
        storage = new Storage(config, handler);
        General.init(storage);
        General.checkConfigVersion();
        listener = new ChatPacketListener(this, ListenerPriority.HIGHEST, storage, PacketType.Play.Server.CHAT);
        AsynchronousManager am = pm.getAsynchronousManager();
        AsyncListenerHandler packetListenerAsyncThread = am.registerAsyncHandler(listener);
        packetListenerAsyncThread.start();
        CIReload rld = new CIReload();
        Bukkit.getPluginCommand("cireload").setExecutor(rld);
        chatEventListener = new ChatEventListener(storage);
        Bukkit.getPluginManager().registerEvents(chatEventListener, this);
        if(isMc18OrLater()) {
            manip = new JSONManipulatorPost1_7_10();
            post17 = true;
        }
        else
            manip = new JSONManipulatorPre1_7_10();

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, ChatColor.RED + "Couldn't start metrics!");
        }
        filter = new Log4jFilter(storage);
    }


    public void onDisable() {
        instance = null;
        post17 = false;
    }

    private boolean isMc18OrLater(){
        switch(getVersion(Bukkit.getServer())){
            case "v1_8_R1": return true;
            case "v1_8_R2": return true;
            case "v1_8_R3": return true;
            case "v1_9_R1": return true;
            case "v1_9_R2": return true;
            case "v1_10_R1": return true;
            case "v1_10_R2": return true;
            default: return false;
        }
    }


    private static String getVersion(Server server) {
        final String packageName = server.getClass().getPackage().getName();

        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }


    public static JSONManipulator getManipulator(){
        return manip;
    }




}
