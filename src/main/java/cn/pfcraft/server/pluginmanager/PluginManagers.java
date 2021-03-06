package cn.pfcraft.server.pluginmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;

public class PluginManagers {

	public static boolean hasPermission(CommandSender sender) {
		if (sender != Bukkit.getServer().getConsoleSender()) {
			if (sender.isOp())
				return true;

			sender.sendMessage("§7请输入正确的指令");
			return false;
		}
		return true;
	}

	public static boolean loadPluginCommand(CommandSender sender, String label, String[] split) {
		if (!hasPermission(sender))
			return true;

		if (split.length < 2) {
			sender.sendMessage(ChatColor.GOLD + "/" + label + " load <plugin> §b- 加载插件");
			return true;
		}
        String jarName = split[1] + (split[1].endsWith(".jar") ? "" : ".jar");
        File toLoad = new File("plugins" + File.separator + jarName);

        if (!toLoad.exists()) {
            jarName = split[1] + (split[1].endsWith(".jar") ? ".unloaded" : ".jar.unloaded");
            toLoad = new File("plugins" + File.separator + jarName);
            if (!toLoad.exists()) {
                sender.sendMessage("§c没有发现文件 " + split[1] + ".jar");
                return true;
            } else {
                String fileName = jarName.substring(0, jarName.length() - (".unloaded".length()));
                toLoad = new File("plugins" + File.separator + fileName);
                File unloaded = new File("plugins" + File.separator + jarName);
                unloaded.renameTo(toLoad);
            }
        }

		PluginDescriptionFile desc = Control.getDescription(toLoad);
		if (desc == null) {
            sender.sendMessage("§cjar不包含plugin.yml文件");
			return true;
		}
        final Plugin[] pl = Bukkit.getPluginManager().getPlugins();
        ArrayList<Plugin> plugins = new ArrayList<Plugin>(java.util.Arrays.asList(pl));
        for(Plugin p: plugins) {
            if (desc.getName().equals(p.getName())) {
                sender.sendMessage(desc.getName()+"§7无法重复加载");
                return true;
            }
        }
		Plugin p = null;
		if ((p = Control.loadPlugin(toLoad)) != null) {
			Control.enablePlugin(p);
            sender.sendMessage(p.getDescription().getName()+p.getDescription().getVersion()+"加载成功");
		} else
		sender.sendMessage(split[1]+"无法加载!(查看控制台了解详细信息.)");

		return true;
	}

	public static boolean unloadPluginCommand(final CommandSender sender, final String label, final String[] split) {
		if (!hasPermission(sender))
			return true;

		if (split.length < 2) {
			sender.sendMessage(ChatColor.GOLD + "/" + label + " unload <plugin> §b- 卸载插件");
			return true;
		}

		final Plugin p = Bukkit.getServer().getPluginManager().getPlugin(split[1]);

		if (p == null)
			sender.sendMessage("§7没有发现插件" + split[1]);
		else {
			if (Control.unloadPlugin(p, true))
				sender.sendMessage(p.getDescription().getName()+p.getDescription().getVersion()+"卸载成功");
			else
				sender.sendMessage(split[1]+"无法卸载!(查看控制台了解详细信息.)");
		}

		return true;
	}

	public static boolean reloadPluginCommand(final CommandSender sender, final String label, final String[] split) {
		if (!hasPermission(sender))
			return true;

		if (split.length < 2) {
			sender.sendMessage(ChatColor.GOLD + "/" + label + " reload <plugin> §b- 重载插件");
			return true;
		}

		final Plugin p = Bukkit.getServer().getPluginManager().getPlugin(split[1]);

		if (p == null)
			sender.sendMessage("§7没有发现插件" + split[1]);
		else {
			final File file = Control.getFile((JavaPlugin) p);

			if (file == null) {
                sender.sendMessage(p.getName()+"jar文件丢失了");
				return true;
			}

			File name = new File("plugins" + File.separator + file.getName());
			JavaPlugin loaded = null;
			if (!Control.unloadPlugin(p, false))
				sender.sendMessage(split[1]+"卸载时发生错误");
			else if ((loaded = (JavaPlugin) Control.loadPlugin(name)) == null)
				sender.sendMessage(split[1]+"重载时发生错误");

			Control.enablePlugin(loaded);
			sender.sendMessage(split[1]+"重载成功");
		}
		return true;
	}
}
