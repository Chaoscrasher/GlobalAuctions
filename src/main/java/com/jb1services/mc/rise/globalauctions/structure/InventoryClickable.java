package com.jb1services.mc.rise.globalauctions.structure;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface InventoryClickable<PLUGIN> extends ItemStackable
{
	public Optional<Inventory> onClick(PLUGIN t, InventoryClickEvent e);
}
