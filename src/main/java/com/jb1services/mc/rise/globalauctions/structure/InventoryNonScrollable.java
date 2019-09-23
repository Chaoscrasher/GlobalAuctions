package com.jb1services.mc.rise.globalauctions.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.chaoscrasher.inventory.InventoryIcon;

public abstract class InventoryNonScrollable extends Inventoryable
{
	private List<? extends ItemStackable> items = new ArrayList<>();
	
	@Override
	public List<? extends ItemStackable> getInventoryStacks() 
	{
		return items;
	}

	@Override
	Optional<InventoryIcon> getForwardIcon() 
	{
		return Optional.empty();
	}

	@Override
	Optional<InventoryIcon> getBackwardIcon() {
		return Optional.empty();
	}

	@Override
	public boolean detect(InventoryClickEvent e) 
	{
		return e.getView().getTitle().equals(getTitle());
	}

	@Override
	public boolean isForwardClick(InventoryClickEvent e) 
	{
		return false;
	}

	@Override
	public boolean isBackwardClick(InventoryClickEvent e) 
	{
		return false;
	}
	
}
