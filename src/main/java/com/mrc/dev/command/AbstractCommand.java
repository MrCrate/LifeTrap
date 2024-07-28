/* Decompiler 525ms, total 927ms, lines 67 */
package com.mrc.dev.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mrc.dev.LifeTrap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import com.mrc.dev.Config;

public abstract class AbstractCommand implements CommandExecutor, TabCompleter {
   public AbstractCommand(String command) {
      PluginCommand pluginCommand = LifeTrap.getInstance().getCommand(command);
      if (pluginCommand != null) {
         pluginCommand.setExecutor(this);
      }

   }

   public abstract void execute(CommandSender var1, String var2, String[] var3);

   public List<String> complete(CommandSender sender, String[] args) {
      return null;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
      this.execute(sender, s, strings);
      return true;
   }

   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      return this.filter(this.complete(sender, args), args);
   }

   private List<String> filter(List<String> list, String[] args) {
      if (list == null) {
         return null;
      } else {
         String last = args[args.length - 1];
         List<String> result = new ArrayList();
         Iterator var5 = list.iterator();

         while(var5.hasNext()) {
            String arg = (String)var5.next();
            if (arg.toLowerCase().startsWith(last.toLowerCase())) {
               result.add(arg);
            }
         }

         return result;
      }
   }

   protected boolean checkPerm(CommandSender sender, String p) {
      if (!sender.hasPermission(p)) {
         sender.sendMessage(Config.getMsg("no-permissions"));
         return true;
      } else {
         return false;
      }
   }
}
