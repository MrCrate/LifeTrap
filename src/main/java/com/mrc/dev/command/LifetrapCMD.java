/* Decompiler 621ms, total 1105ms, lines 116 */
package com.mrc.dev.command;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.mrc.dev.Config;
import com.mrc.dev.LifeTrap;
import com.mrc.dev.general.AbstractItem;

public class LifetrapCMD extends AbstractCommand {
   public LifetrapCMD() {
      super("lifetrap");
   }

   public void execute(CommandSender sender, String label, String[] args) {
      if (!this.checkPerm(sender, "lifetrap.lifetrap")) {
         if (args.length >= 1) {
            String var4 = args[0];
            byte var5 = -1;
            switch(var4.hashCode()) {
            case -934641255:
               if (var4.equals("reload")) {
                  var5 = 0;
               }
               break;
            case 3173137:
               if (var4.equals("give")) {
                  var5 = 1;
               }
            }

            switch(var5) {
            case 0:
               if (this.checkPerm(sender, "lifetrap.reload")) {
                  return;
               }

               LifeTrap.getInstance().reload(sender);
               break;
            case 1:
               this.give(sender, args);
            }

         }
      }
   }

   private void give(CommandSender sender, String[] args) {
      if (!this.checkPerm(sender, "lifetrap.lifetrap-give")) {
         if (args.length != 4) {
            sender.sendMessage(Config.getMsg("give"));
         } else {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
               sender.sendMessage(Config.getMsg("offline-player"));
            } else {
               AbstractItem item = LifeTrap.getInstance().getItems().get(args[2]);
               if (item == null) {
                  sender.sendMessage(Config.getMsg("error-give"));
               } else {
                  try {
                     int count = Integer.parseInt(args[3]);
                     ItemStack itemG = item.getItem();
                     itemG.setAmount(count);
                     if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(new ItemStack[]{itemG});
                     } else {
                        player.getWorld().dropItem(player.getLocation(), itemG);
                     }
                  } catch (NumberFormatException var7) {
                     sender.sendMessage(Config.getMsg("error-give"));
                  }

               }
            }
         }
      }
   }

   public List<String> complete(CommandSender sender, String[] args) {
      ArrayList out;
      if (args.length == 1) {
         out = new ArrayList();
         if (sender.hasPermission("lifetrap.reload")) {
            out.add("reload");
         }

         if (sender.hasPermission("lifetrap.lifetrap-give")) {
            out.add("give");
         }

         return out;
      } else if (args.length == 2) {
         return (List)Bukkit.getOnlinePlayers().stream().map(OfflinePlayer::getName).collect(Collectors.toList());
      } else if (args.length == 3 && sender.hasPermission("lifetrap.lifetrap-give")) {
         return Lists.newArrayList(LifeTrap.getInstance().getItems().keySet());
      } else if (args.length == 4 && sender.hasPermission("lifetrap.lifetrap-give")) {
         out = new ArrayList();

         for(int i = 1; i < 10; ++i) {
            out.add(String.valueOf(i));
         }

         return out;
      } else {
         return Lists.newArrayList();
      }
   }
}
