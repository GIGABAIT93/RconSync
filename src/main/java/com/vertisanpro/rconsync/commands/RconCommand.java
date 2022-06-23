package com.vertisanpro.rconsync.commands;

import com.google.common.collect.Lists;
import com.vertisanpro.rconsync.lib.AuthenticationException;
import org.bukkit.command.CommandSender;
import com.vertisanpro.rconsync.RconSync ;
import com.vertisanpro.rconsync.Message;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import com.vertisanpro.rconsync.lib.Rcon;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.*;


public class RconCommand extends AbstractCommand {

    private static FileConfiguration config = RconSync.getInstance().getConfig();
    private ConfigurationSection serversData = config.getConfigurationSection("servers");
    private List servers = getServers();
    public RconCommand() {
        super("rcon");

    }

    @Override
    public void execute(CommandSender sender, String label, String @NotNull [] args) {
        if (args.length == 0) {
            Message.usage.send(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("rcon.reload")) {
                Message.noPermission.send(sender);
                return;
            }
            RconSync.getInstance().reloadConfig();
            config = RconSync.getInstance().getConfig();
            serversData = config.getConfigurationSection("servers");
            servers = getServers();
            Message.reload.send(sender);
            return;
        }


        if (args.length > 1) {
            String  command = String.join(" ", Arrays.asList(args).subList(1, args.length).toArray(new String[]{}));
            if (args[0].equalsIgnoreCase("all")) {
                for (Object serv : servers) {
                    if (serverPerm(sender, serv.toString())) {
                        sendCommand(sender, serv.toString(), command);
                    }
                }
            } else {
                if (servers.contains(args[0])) {
                    if (!serverPerm(sender, args[0])) {
                        Message.noPermission.send(sender);
                        return;
                    }
                    sendCommand(sender, args[0], command);
                } else {
                    Message.serverNotExist.replace("{server}", args[0]).send(sender);
                }
            }
        } else {
            Message.noRconCommand.send(sender);
        }
    }


    private void sendCommand(CommandSender sender, String server, String command) {
        try {
            String host = getServerParam(server, "ip");
            int port = Integer.parseInt(getServerParam(server, "port"));
            byte[] pass = getServerParam(server, "pass").getBytes();
            Rcon conn = new Rcon(host, port, pass);
            String resp = conn.command(command);
            conn.disconnect();
            if (Objects.equals(resp.trim(), "")) {resp = "No response from server";}
            Message.rconResponse.replace("{server}", server).replace("{reply}", resp).send(sender);
        } catch (AuthenticationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean serverPerm(@NotNull CommandSender sender, String server) {
        if (!sender.hasPermission(getServerParam(server, "perms"))) {
            Message.noPermission.send(sender);
            return false;
        }
        return true;
    }

    private String getServerParam(String server, String key) {
        return (String) serversData.get(server + "." + key);
    }

    private @NotNull List getServers() {
        return Arrays
                .asList(Objects
                        .requireNonNull(config
                        .getConfigurationSection("servers"))
                        .getKeys(false).toArray());
    }

    @Override
    public List<String> complete(CommandSender sender, String @NotNull [] args) {
        if (args.length == 1) {
            ArrayList<String> resp = Lists.newArrayList("reload", "all");
            for (Object serv : this.getServers()) {
                resp.add(serv.toString());
            }
            return resp;
        }
        return Lists.newArrayList();
    }
}