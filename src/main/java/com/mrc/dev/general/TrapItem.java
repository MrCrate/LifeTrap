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
import com.sk89q.worldedit.function.RegionMaskingFilter;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TrapItem extends AbstractItem {
   private final Material slab;
   private final File schem;

   public TrapItem(String key, String displayName, int workTime, Material material, int cooldown, List<String> lore, ISound iSound, Map<Integer, Material> structure, Material slab, Map rgFlags) {
      super(key, displayName, workTime, material, cooldown, lore, iSound, structure, rgFlags);
      this.slab = slab;
      this.schem = new File(LifeTrap.getInstance().getDataFolder(), "trap.schem");
   }

   public boolean use(Player player) {
      if (!this.checkCooldown(player)) {
         return false;
      } else if (this.pasteSchem(player.getLocation())) {
         player.setCooldown(this.getMaterial(), this.getCooldown());
         return true;
      } else {
         return false;
      }
   }

   private boolean pasteSchem(Location loc) {
      ClipboardFormat format = ClipboardFormats.findByFile(this.schem);
      if (format == null) {
         LifeTrap.getInstance().getLogger().warning("Формат схемы не найден для файла: " + this.schem.getName());
         return false;
      }

      boolean out = false;

      try (FileInputStream fis = new FileInputStream(this.schem);
           ClipboardReader reader = format.getReader(fis)) {

         Clipboard clipboard = reader.read();
         out = this.createTrapRegion(loc);
         if (!out) {
            return false;
         }

         try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(loc.getWorld()))) {
            BlockMask filterR = new BlockMask(clipboard, new HashSet<>());
            filterR.add(new BaseBlock[]{BlockTypes.STONE.getDefaultState().toBaseBlock()});
            RandomPattern rPattern = new RandomPattern();
            this.getStructure().forEach((chance, material) -> {
               rPattern.add(BlockTypes.get("minecraft:" + material.name().toLowerCase()).getDefaultState(), (double)chance);
            });
            BlockReplace replace = new BlockReplace(clipboard, rPattern);
            RegionMaskingFilter filter = new RegionMaskingFilter(filterR, replace);
            RegionVisitor visitor = new RegionVisitor(clipboard.getRegion(), filter);
            Operations.complete(visitor);

            BlockMask filterR2 = new BlockMask(clipboard, new HashSet<>());
            filterR2.add(new BaseBlock[]{BlockTypes.CLAY.getDefaultState().toBaseBlock()});
            ArrayList<String> list = new ArrayList<>();
            list.add("top");
            BlockState rPattern2 = BlockTypes.get("minecraft:" + this.slab.name().toLowerCase()).getDefaultState().with(new EnumProperty("type", list), "top");
            BlockReplace replace2 = new BlockReplace(clipboard, rPattern2);
            RegionMaskingFilter filter2 = new RegionMaskingFilter(filterR2, replace2);
            RegionVisitor visitor2 = new RegionVisitor(clipboard.getRegion(), filter2);
            Operations.complete(visitor2);

            ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
            clipboardHolder.setTransform((new AffineTransform()).rotateY(90.0D));
            Operation operation = clipboardHolder.createPaste(editSession).ignoreAirBlocks(false).to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ())).build();
            Operations.complete(operation);

            Bukkit.getScheduler().runTaskLater(LifeTrap.getInstance(), () -> {
               editSession.undo(editSession);
            }, (long)this.getWorkTime());

         } catch (WorldEditException e) {
            LifeTrap.getInstance().getLogger().warning("Error on paste schem -> trap.schem");
            e.printStackTrace();
         }

      } catch (IOException e) {
         e.printStackTrace();
      }

      return out;
   }
}
