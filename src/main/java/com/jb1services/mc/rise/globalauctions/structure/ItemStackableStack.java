package com.jb1services.mc.rise.globalauctions.structure;

import org.bukkit.inventory.ItemStack;

public class ItemStackableStack implements ItemStackable
{
	private ItemStack stack;
	
	public ItemStackableStack(ItemStack stack)
	{
		this.stack = stack;
	}
	
	public ItemStack stack()
	{
		return stack;
	}
}
