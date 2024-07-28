package com.mrc.dev;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import com.mrc.dev.general.AbstractItem;
import com.mrc.dev.general.PlastItem;
import com.mrc.dev.general.TrapItem;

public class BukkitEventHandler implements Listener {
   private final Map<Long, Location> use = new HashMap<>();

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Action action = event.getAction();
      if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {
         ItemStack item = event.getItem();
         if (item != null) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {
               PersistentDataContainer nbt = itemMeta.getPersistentDataContainer();
               if (!nbt.isEmpty()) {
                  NamespacedKey nbtKey = new NamespacedKey(LifeTrap.getInstance(), "LifeTrap");
                  String key = nbt.get(nbtKey, PersistentDataType.STRING);
                  HashMap<String, AbstractItem> items = LifeTrap.getInstance().getItems();
                  AbstractItem useItem = items.get(key);
                  if (useItem != null) {
                     Player player = event.getPlayer();
                     Location loc = player.getLocation();
                     Collection<Entity> players;

                     if (useItem instanceof TrapItem) {
                        TrapItem trapItem = (TrapItem) useItem;
                        event.setCancelled(true);
                        if (trapItem.use(player)) {
                           item.setAmount(item.getAmount() - 1);
                           trapItem.playSound(player);
                           players = loc.getWorld().getNearbyEntities(loc, 3.0D, 3.0D, 3.0D);
                           for (Entity p : players) {
                              if (p instanceof Player) {
                                 Player pl = (Player) p;
                                 if (LifeTrap.getAntiRelog() != null && LifeTrap.getAntiRelog().getPvpManager() != null) {
                                    LifeTrap.getAntiRelog().getPvpManager().playerDamagedByPlayer(player, pl);
                                 }
                              }
                           }
                           this.use.put(System.currentTimeMillis(), loc);
                        }
                     }

                     if (useItem instanceof PlastItem) {
                        PlastItem plastItem = (PlastItem) useItem;
                        event.setCancelled(true);
                        if (plastItem.use(player)) {
                           item.setAmount(item.getAmount() - 1);
                           plastItem.playSound(player);
                           players = loc.getWorld().getNearbyEntities(loc, 3.0D, 3.0D, 3.0D);
                           for (Entity p : players) {
                              if (p instanceof Player) {
                                 Player pl = (Player) p;
                                 if (LifeTrap.getAntiRelog() != null && LifeTrap.getAntiRelog().getPvpManager() != null) {
                                    LifeTrap.getAntiRelog().getPvpManager().playerDamagedByPlayer(player, pl);
                                 }
                              }
                           }
                           this.use.put(System.currentTimeMillis(), loc);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @NotNull
   private double[] getNearBy(Location loc, int useTime) {
      double[] nearBy = new double[]{10.0D};
      HashMap<Long, Location> mapCopy = new HashMap<>(this.use);
      int useTimeS = useTime * 50;
      mapCopy.forEach((mKey, mLoc) -> {
         if (System.currentTimeMillis() - mKey > (long) useTimeS) {
            this.use.remove(mKey);
         } else {
            double dist = loc.distance(mLoc);
            if (dist < nearBy[0]) {
               nearBy[0] = dist;
            }
         }
      });
      return nearBy;
   }
}
