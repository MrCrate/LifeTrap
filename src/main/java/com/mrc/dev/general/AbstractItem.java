/* Decompiler 1733ms, total 1960ms, lines 203 */
package com.mrc.dev.general;

import com.mrc.dev.LifeTrap;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.mrc.dev.Config;

public abstract class AbstractItem {
   private final String key;
   private final String displayName;
   private final int workTime;
   private final Material material;
   private final int cooldown;
   private final List<String> lore;
   private final ISound iSound;
   private final Map<Integer, Material> structure;
   private final Map<StateFlag, State> rgFlags;
   private ItemStack item;

   public ItemStack getItem() {
      return this.item != null ? this.item : this.createItem();
   }

   private ItemStack createItem() {
      this.item = new ItemStack(this.material);
      this.item.setAmount(1);
      ItemMeta itemMeta = this.item.getItemMeta();
      PersistentDataContainer nbt = itemMeta.getPersistentDataContainer();
      nbt.set(new NamespacedKey(LifeTrap.getInstance(), "LifeTrap"), PersistentDataType.STRING, this.key);
      itemMeta.setDisplayName(this.displayName);
      itemMeta.setLore(this.lore);
      this.item.setItemMeta(itemMeta);
      return this.item;
   }

   protected boolean checkCooldown(Player player) {
      if (player.hasCooldown(this.material)) {
         player.sendMessage(Config.getMsg("cooldown-msg").replace("%sec%", String.valueOf(player.getCooldown(this.material) / 20)));
         return false;
      } else {
         return true;
      }
   }

   public static boolean isPlayerInLieTrapRegion(Player player) {
      Location loc = player.getLocation();
      RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()));
      if (regionManager == null) {
         Bukkit.getConsoleSender().sendMessage("§c[LieTrap] region manager is null");
         return false;
      } else {
         boolean out = false;
         int priority = Integer.MIN_VALUE;
         Iterator var5 = regionManager.getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ())).getRegions().iterator();

         while(var5.hasNext()) {
            ProtectedRegion region = (ProtectedRegion)var5.next();
            if (region.getPriority() > priority) {
               priority = region.getPriority();
               out = LifeTrap.getInstance().getCfg().getBlackRg().contains(region.getId());
            }
         }

         return out;
      }
   }

   protected boolean createTrapRegion(Location loc) {
      RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()));
      if (regionManager == null) {
         Bukkit.getConsoleSender().sendMessage("§c[LieTrap] region manager is null");
         return false;
      } else {
         BlockVector3 minPoint = BlockVector3.at(loc.getX() - 2.0D, loc.getY() - 1.0D, loc.getZ() - 2.0D);
         BlockVector3 maxPoint = BlockVector3.at(loc.getX() + 2.0D, loc.getY() + 3.0D, loc.getZ() + 2.0D);
         String regionName = getRegionName(new Location(loc.getWorld(), (double)maxPoint.getX(), (double)maxPoint.getY(), (double)maxPoint.getZ()));
         ProtectedCuboidRegion rg = new ProtectedCuboidRegion(regionName, minPoint, maxPoint);
         Iterator var7 = this.rgFlags.entrySet().iterator();

         while(var7.hasNext()) {
            Entry<StateFlag, State> flag = (Entry)var7.next();
            rg.setFlag((StateFlag)flag.getKey(), (State)flag.getValue());
         }

         rg.setPriority(Integer.MAX_VALUE);
         LifeTrap.getInstance().getCfg().getBlackRg().add(regionName);
         if (checkApplicableRegions(regionManager, rg)) {
            return false;
         } else {
            regionManager.addRegion(rg);
            Bukkit.getScheduler().runTaskLater(LifeTrap.getInstance(), () -> {
               LifeTrap.getInstance().getCfg().getBlackRg().remove(regionName);
               regionManager.removeRegion(regionName);
            }, (long)this.getWorkTime());
            return true;
         }
      }
   }

   public abstract boolean use(Player player);

   protected boolean createPlastRegion(Location loc, int[] offset) {
      // Check if location or offset array is null
      if (loc == null || offset == null || offset.length != 6) {
         Bukkit.getConsoleSender().sendMessage("§c[LifeTrap] Location or offset array is null/invalid.");
         return false;
      }

      // Get WorldGuard instance
      WorldGuard wg = WorldGuard.getInstance();
      if (wg == null) {
         Bukkit.getConsoleSender().sendMessage("§c[LifeTrap] WorldGuard instance is null.");
         return false;
      }

      // Get platform from WorldGuard instance
      Platform platform = (Platform) wg.getPlatform();
      if (platform == null) {
         Bukkit.getConsoleSender().sendMessage("§c[LifeTrap] WorldGuard platform is null.");
         return false;
      }

      // Get region manager from WorldGuard platform
      RegionManager regionManager = ((WorldGuardPlatform) platform).getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()));
      if (regionManager == null) {
         Bukkit.getConsoleSender().sendMessage("§c[LifeTrap] Region manager is null.");
         return false;
      }

      // Define region points
      BlockVector3 minPoint = BlockVector3.at(loc.getX() + offset[0], loc.getY() + offset[1], loc.getZ() + offset[2]);
      BlockVector3 maxPoint = BlockVector3.at(loc.getX() + offset[3], loc.getY() + offset[4], loc.getZ() + offset[5]);
      String regionName = getRegionName(new Location(loc.getWorld(), maxPoint.getX(), maxPoint.getY(), maxPoint.getZ()));
      ProtectedCuboidRegion rg = new ProtectedCuboidRegion(regionName, minPoint, maxPoint);

      // Set flags
      for (Entry<StateFlag, State> flag : this.rgFlags.entrySet()) {
         rg.setFlag(flag.getKey(), flag.getValue());
      }

      // Set region priority
      rg.setPriority(Integer.MAX_VALUE);
      LifeTrap.getInstance().getCfg().getBlackRg().add(regionName);

      // Check for overlapping regions
      if (checkApplicableRegions(regionManager, rg)) {
         Bukkit.getConsoleSender().sendMessage("§c[LifeTrap] Overlapping region detected.");
         return false;
      }

      // Add region to the manager
      regionManager.addRegion(rg);

      // Schedule task to remove the region after a certain time
      Bukkit.getScheduler().runTaskLater(LifeTrap.getInstance(), () -> {
         LifeTrap.getInstance().getCfg().getBlackRg().remove(regionName);
         regionManager.removeRegion(regionName);
      }, this.getWorkTime());

      return true;
   }



   private static boolean checkApplicableRegions(RegionManager regionManager, ProtectedCuboidRegion rg) {
      int priority = Integer.MIN_VALUE;
      boolean out = false;
      Iterator var4 = regionManager.getApplicableRegions(rg).iterator();

      while(var4.hasNext()) {
         ProtectedRegion region = (ProtectedRegion)var4.next();
         if (region.getPriority() > priority) {
            priority = region.getPriority();
            out = LifeTrap.getInstance().getCfg().getBlackRg().contains(region.getId());
         }
      }

      return out;
   }

   public static String getRegionName(Location loc) {
      return "lietrap_%s_x%d_y%d_z%d".formatted(new Object[]{loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()});
   }

   public void playSound(Player player) {
      this.iSound.play(player);
   }

   public AbstractItem(String key, String displayName, int workTime, Material material, int cooldown, List<String> lore, ISound iSound, Map<Integer, Material> structure, Map<StateFlag, State> rgFlags) {
      this.key = key;
      this.displayName = displayName;
      this.workTime = workTime;
      this.material = material;
      this.cooldown = cooldown;
      this.lore = lore;
      this.iSound = iSound;
      this.structure = structure;
      this.rgFlags = rgFlags;
   }

   public int getWorkTime() {
      return this.workTime;
   }

   public Material getMaterial() {
      return this.material;
   }

   public int getCooldown() {
      return this.cooldown;
   }

   public Map<Integer, Material> getStructure() {
      return this.structure;
   }
}
