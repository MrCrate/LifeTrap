package com.mrc.dev.general;

import com.mrc.dev.LifeTrap;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class PlastItem extends AbstractItem {
   private static final Map<StateFlag, StateFlag.State> rgFlags = null;
   private final File schemH = new File(LifeTrap.getInstance().getDataFolder(), "plast-h.schem");
   private final File schemV0 = new File(LifeTrap.getInstance().getDataFolder(), "plast-v0.schem");
   private final File schemV45 = new File(LifeTrap.getInstance().getDataFolder(), "plast-v45.schem");

   public PlastItem(String key, String displayName, int workTime, Material material, int cooldown, List<String> lore, ISound iSound, Map<Integer, Material> structure) {
      super(key, displayName, workTime, material, cooldown, lore, iSound, structure, rgFlags);
   }

   @Override
   public boolean use(Player player) {
      if (!this.checkCooldown(player)) {
         return false;
      } else if (this.pasteSchematic(player.getLocation())) {
         player.setCooldown(this.getMaterial(), this.getCooldown());
         return true;
      } else {
         return false;
      }
   }

   private boolean pasteSchematic(Location loc) {
      float pitch = loc.getPitch();
      if (pitch <= 45.0F && pitch >= -45.0F) {
         return handleVerticalPaste(loc);
      } else {
         return pasteHorizontal(loc);
      }
   }

   private boolean handleVerticalPaste(Location loc) {
      float yaw = loc.getYaw();
      int[] region = new int[6];
      File schemFile = null;
      int rotation = 0;

      if (yaw > 157.5D || yaw <= -157.5D) {
         region = new int[]{-2, -1, -1, 2, 3, -2};
         schemFile = this.schemV0;
      } else if (yaw > -157.5D && yaw <= -112.5D) {
         region = new int[]{0, 0, -4, 4, 4, 0};
         schemFile = this.schemV45;
      } else if (yaw > -112.5D && yaw <= -67.5D) {
         region = new int[]{1, -1, -2, 2, 3, 2};
         schemFile = this.schemV0;
         rotation = 270;
      } else if (yaw > -67.5D && yaw <= -22.5D) {
         region = new int[]{4, 0, 0, 0, 4, 4};
         schemFile = this.schemV45;
         rotation = 270;
      } else if (yaw > -22.5D && yaw <= 22.5D) {
         region = new int[]{2, -1, 1, -2, 3, 2};
         schemFile = this.schemV0;
         rotation = 180;
      } else if (yaw > 22.5D && yaw <= 67.5D) {
         region = new int[]{0, 0, 4, -4, 4, 0};
         schemFile = this.schemV45;
         rotation = 180;
      } else if (yaw > 67.5D && yaw <= 112.5D) {
         region = new int[]{-1, -1, 2, -2, 3, -2};
         schemFile = this.schemV0;
         rotation = 90;
      } else if (yaw > 112.5D && yaw <= 157.5D) {
         region = new int[]{-4, 0, 0, 0, 4, -4};
         schemFile = this.schemV45;
         rotation = 90;
      }

      if (!this.createPlastRegion(loc, region)) {
         return false;
      }

      if (schemFile != null) {
         this.pasteVertical(loc, rotation, schemFile);
      }

      return true;
   }

   private boolean pasteHorizontal(Location loc) {
      File schematicFile = this.schemH;
      ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);

      if (format == null) {
         Bukkit.getLogger().severe("Schematic file format is null for " + schematicFile.getName());
         return false;
      }

      try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
         Clipboard clipboard = reader.read();
         try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(loc.getWorld()))) {
            ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
            if (loc.getPitch() < -45.0F) {
               clipboardHolder.setTransform(new AffineTransform().rotateZ(180.0D));
               loc.add(0.0D, 2.0D, 0.0D);
               if (!this.createPlastRegion(loc, new int[]{-2, 2, -2, 2, 1, 2})) {
                  return false;
               }
            } else {
               if (!this.createPlastRegion(loc, new int[]{-2, -2, -2, 2, -1, 2})) {
                  return false;
               }
            }

            applyPatternsAndPaste(clipboardHolder, editSession, loc);
            return true;
         } catch (WorldEditException ex) {
            ex.printStackTrace();
            return false;
         }
      } catch (IOException e) {
         Bukkit.getLogger().severe("Failed to read schematic file: " + schematicFile.getName());
         e.printStackTrace();
         return false;
      }
   }

   private void pasteVertical(Location loc, int rotate, File schem) {
      ClipboardFormat format = ClipboardFormats.findByFile(schem);

      if (format == null) {
         Bukkit.getLogger().severe("Schematic file format is null for " + schem.getName());
         return;
      }

      try (ClipboardReader reader = format.getReader(new FileInputStream(schem))) {
         Clipboard clipboard = reader.read();
         try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(loc.getWorld()))) {
            ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
            clipboardHolder.setTransform(new AffineTransform().rotateY(rotate));
            applyPatternsAndPaste(clipboardHolder, editSession, loc);
         } catch (WorldEditException ex) {
            ex.printStackTrace();
         }
      } catch (IOException e) {
         Bukkit.getLogger().severe("Failed to read schematic file: " + schem.getName());
         e.printStackTrace();
      }
   }

   private void applyPatternsAndPaste(ClipboardHolder clipboardHolder, EditSession editSession, Location loc) throws WorldEditException {
      BlockMask filterR = new BlockMask(clipboardHolder.getClipboard(), new HashSet<>());
      filterR.add(BlockTypes.STONE.getDefaultState().toBaseBlock());
      RandomPattern rPattern = new RandomPattern();
      this.getStructure().forEach((chance, material) -> {
         rPattern.add(BlockTypes.get("minecraft:" + material.name().toLowerCase()).getDefaultState(), (double) chance);
      });
      BlockReplace replace = new BlockReplace(clipboardHolder.getClipboard(), rPattern);
      Operations.complete(clipboardHolder.createPaste(editSession)
              .to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))
              .ignoreAirBlocks(false)
              .build());

      Bukkit.getScheduler().runTaskLater(LifeTrap.getInstance(), () -> {
          editSession.undo(editSession);
      }, this.getWorkTime());
   }

   public boolean createPlastRegion(Location loc, int[] region) {
      // Add your implementation of createPlastRegion method here
      return true;
   }
}