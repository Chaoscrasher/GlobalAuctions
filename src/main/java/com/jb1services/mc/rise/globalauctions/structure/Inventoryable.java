package com.jb1services.mc.rise.globalauctions.structure;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.chaoscrasher.inventory.InventoryIcon;

public abstract class Inventoryable 
{
	public static int MAX_PAGE_SIZE = 54;
	
	public abstract List<? extends ItemStackable> getInventoryStacks();
	
	public List<ItemStack> getStacks()
	{
		return getInventoryStacks().stream().map(isa -> isa.stack()).collect(Collectors.toList());
	}
	
	abstract String getTitle();
	
	abstract InventoryHolder getHolder();
	
	abstract Optional<InventoryIcon> getForwardIcon();
	
	abstract Optional<InventoryIcon> getBackwardIcon();
	
	/*
	protected Inventory toInventory(boolean addForwardIcon, boolean addBackwardIcon)
	{
		List<ItemStack> stackz = getStacks();
		int buttonSlots = (addBackwardIcon ? 1 : 0) + (addForwardIcon ? 1 : 0);
		int totalSlots = stackz.size() + buttonSlots;
		int actualSize = (int) (9 * Math.ceil(totalSlots / 9.0));
		actualSize = actualSize > MAX_PAGE_SIZE ? MAX_PAGE_SIZE : actualSize;
		Inventory inv = Bukkit.createInventory(getHolder(), actualSize, getTitle());
		
		int i = 0;
		while (i < actualSize - buttonSlots)
		{
			inv.setItem(i, stackz.get(i));
			i++;
		}
		
		for (int j = 0; j < buttonSlots;  j++)
		{
			inv.setItem(i + j, stackz.get(i + j));
		}
		
		return inv;
	}
	*/
	
	public abstract boolean detect(InventoryClickEvent e);
	
	public abstract boolean isForwardClick(InventoryClickEvent e);
				
	public abstract boolean isBackwardClick(InventoryClickEvent e);
}
