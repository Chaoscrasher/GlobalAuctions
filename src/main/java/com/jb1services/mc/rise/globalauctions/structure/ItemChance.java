package com.jb1services.mc.rise.globalauctions.structure;

import org.bukkit.inventory.ItemStack;

public class ItemChance
{
	private ItemStack item;
	private int weight;
	
	public ItemChance(ItemStack item, int weight)
	{
		super();
		this.item = item;
		this.weight = weight;
	}
	
	public ItemStack getItem()
	{
		return item;
	}
	
	public int getWeight()
	{
		return weight;
	}
}
