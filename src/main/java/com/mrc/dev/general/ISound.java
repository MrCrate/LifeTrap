package com.mrc.dev.general;

import java.util.Collection;
import java.util.Iterator;

import com.mrc.dev.LifeTrap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import ru.leymooo.antirelog.Antirelog;
import ru.leymooo.antirelog.manager.PvPManager;

public class ISound {
   private final int radius;
   private final Sound sound;
   private final float volume;
   private final float pitch;

   public void play(Player player) {
      Location loc = player.getLocation();
      Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, (double) this.radius, (double) this.radius, (double) this.radius);

      for (Entity entity : entities) {
         if (entity instanceof Player) {
            Player targetPlayer = (Player) entity;

            // Check if the target player is valid and online
            if (targetPlayer.isValid() && targetPlayer.isOnline()) {
               targetPlayer.playSound(targetPlayer.getLocation(), this.sound, this.volume, this.pitch);

               // Retrieve AntiRelog instance and PvpManager
               Antirelog antiRelog = LifeTrap.getAntiRelog();
               if (antiRelog != null) {
                  PvPManager pvpManager = antiRelog.getPvpManager();
                  if (pvpManager != null) {
                     // Perform PvP interaction handling
                     pvpManager.playerDamagedByPlayer(player, targetPlayer);
                  } else {
                     Bukkit.getLogger().warning("PvpManager is null in LifeTrap.getAntiRelog()");
                  }
               } else {
                  Bukkit.getLogger().warning("AntiRelog is null in LifeTrap.getAntiRelog()");
               }
            } else {
               Bukkit.getLogger().warning("Target player is not valid or not online");
            }
         }
      }
   }

   public ISound(int radius, Sound sound, float volume, float pitch) {
      this.radius = radius;
      this.sound = sound;
      this.volume = volume;
      this.pitch = pitch;
   }
}
