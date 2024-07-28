package com.mrc.dev;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import com.mrc.dev.general.ISound;
import com.mrc.dev.general.PlastItem;
import com.mrc.dev.general.TrapItem;

public class Config {
   private static final HashMap<String, String> messages = new HashMap<>();
   private List<String> blackRg = new ArrayList<>();

   public Config(FileConfiguration cfg) {
      this.loadBlackRg(cfg);
      this.loadMessages(cfg);
      this.loadTrap(cfg);
      this.loadPlast(cfg);
   }

   private void loadBlackRg(FileConfiguration cfg) {
      List<String> list = cfg.getStringList("rg-black-list");
      if (list != null) {
         this.blackRg = new ArrayList<>(list);
      }
   }

   private void loadPlast(FileConfiguration cfg) {
      ConfigurationSection plast = cfg.getConfigurationSection("plast");
      if (plast == null) {
         this.warning("plast is null");
         return;
      }

      String name = plast.getString("name", "default");
      int time = plast.getInt("time", 15) * 20;
      String mName = plast.getString("item");
      Material item = mName == null ? null : Material.getMaterial(mName);

      if (item == null) {
         this.warning("undefined material " + mName);
         return;
      }

      int cooldown = plast.getInt("cooldown", 10) * 20;
      List<String> lore = plast.getStringList("lore");
      ISound iSound = this.parseSound(plast.getConfigurationSection("sound"));

      if (iSound == null) {
         return;
      }

      Map<Integer, Material> structure = this.parseStructure(plast.getConfigurationSection("structure"));
      if (structure == null) {
         return;
      }

      Map<StateFlag, State> rgFlags = this.parseRgFlags(plast.getConfigurationSection("region-flags"));
      PlastItem plastItem = new PlastItem("plast", name, time, item, cooldown, lore, iSound, structure);
      LifeTrap.getInstance().getItems().put("plast", plastItem);
   }

   private Map<StateFlag, State> parseRgFlags(ConfigurationSection section) {
      Map<StateFlag, State> rgFlags = new HashMap<>();
      if (section == null) {
         this.warning("region-flags section is null");
         return rgFlags;
      }

      FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
      for (String flag : section.getKeys(false)) {
         Flag<?> flag1 = flagRegistry.get(flag);
         if (flag1 == null) {
            this.warning("Flag " + flag + " not found in registry");
            continue;
         }
         try {
            State state = State.valueOf(section.getString(flag));
            if (flag1 instanceof StateFlag) {
               StateFlag stateFlag = (StateFlag) flag1;
               rgFlags.put(stateFlag, state);
            } else {
               this.warning(flag + " is not a StateFlag");
            }
         } catch (IllegalArgumentException e) {
            this.warning("Invalid state value for flag " + flag + ": " + section.getString(flag));
         }
      }

      return rgFlags;
   }

   private void loadTrap(FileConfiguration cfg) {
      ConfigurationSection trap = cfg.getConfigurationSection("trap");
      if (trap == null) {
         this.warning("trap is null");
         return;
      }

      String name = trap.getString("name", "default");
      int time = trap.getInt("time", 15) * 20;
      String mName = trap.getString("item");
      Material item = mName == null ? null : Material.getMaterial(mName);

      if (item == null) {
         this.warning("undefined material " + mName);
         return;
      }

      int cooldown = trap.getInt("cooldown", 10) * 20;
      List<String> lore = trap.getStringList("lore");
      ISound iSound = this.parseSound(trap.getConfigurationSection("sound"));

      if (iSound == null) {
         return;
      }

      Map<Integer, Material> structure = this.parseStructure(trap.getConfigurationSection("structure"));
      if (structure == null) {
         return;
      }

      String sName = trap.getString("slab");
      if (sName == null) {
         this.warning("trap.slab is null");
         return;
      }

      Material slab = Material.getMaterial(sName);
      if (slab == null) {
         this.warning("trap.slab material is undefined");
         return;
      }

      Map rgFlags = this.parseRgFlags(trap.getConfigurationSection("region-flags"));
      TrapItem trapItem = new TrapItem("trap", name, time, item, cooldown, lore, iSound, structure, slab, rgFlags);
      LifeTrap.getInstance().getItems().put("trap", trapItem);
   }

   private Map<Integer, Material> parseStructure(ConfigurationSection section) {
      Map<Integer, Material> structure = new HashMap<>();
      if (section == null) {
         this.warning("structure is null");
         return null;
      }

      for (String key : section.getKeys(false)) {
         ConfigurationSection block = section.getConfigurationSection(key);
         if (block != null) {
            String mName = block.getString("material");
            if (mName == null) {
               this.warning(key + ".material name is null");
               continue;
            }

            Material material = Material.getMaterial(mName);
            if (material == null) {
               this.warning(key + ".material is undefined");
               continue;
            }

            int count = block.getInt("count", 50);
            structure.put(count, material);
         }
      }

      return structure;
   }

   private ISound parseSound(ConfigurationSection section) {
      if (section == null) {
         this.warning("sound is null");
         return null;
      }

      int radius = section.getInt("radius", 3);
      String sName = section.getString("name");
      if (sName == null) {
         this.warning("sound name is null");
         return null;
      }

      Sound sound;
      try {
         sound = Sound.valueOf(sName);
      } catch (IllegalArgumentException e) {
         this.warning("Undefined sound " + sName);
         return null;
      }

      float volume = (float) section.getDouble("volume", 0.5D);
      float pitch = (float) section.getDouble("pitch", 0.1D);
      return new ISound(radius, sound, volume, pitch);
   }

   public static String getMsg(String key) {
      return messages.get(key);
   }

   private void loadMessages(FileConfiguration cfg) {
      ConfigurationSection section = cfg.getConfigurationSection("messages");
      if (section == null) {
         this.warning("messages section is null");
         return;
      }

      for (String id : section.getKeys(false)) {
         String message = section.getString(id);
         messages.put(id, message);
      }
   }

   private void warning(String message) {
      String msg = "§4[LieTrap] Config warning -> §c" + message;
      Bukkit.getConsoleSender().sendMessage(msg);
   }

   public List<String> getBlackRg() {
      return this.blackRg;
   }
}
