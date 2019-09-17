package com.jb1services.mc.rise.globalauctions.structure.inventories;

import java.util.List;
import java.util.function.Supplier;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import com.chaoscrasher.inventory.InventoryIcon;
import com.jb1services.mc.rise.globalauctions.main.GlobalAuctionsPlugin;
import com.jb1services.mc.rise.globalauctions.structure.AuctionsDatabase;
import com.jb1services.mc.rise.globalauctions.structure.InventoryScrollableFixedClickable;
import com.jb1services.mc.rise.globalauctions.structure.InventoryScrollableFixedView;
import com.jb1services.mc.rise.globalauctions.structure.ItemStackable;

public class SellsMenu extends InventoryScrollableFixedClickable<GlobalAuctionsPlugin>
{
	private AuctionsDatabase aucdb;
	
	public SellsMenu(AuctionsDatabase aucdb) 
	{
		super(GlobalAuctionsPlugin.SELLS_MENU_TITLE, ChatColor.WHITE, GlobalAuctionsPlugin.NEXT_PAGE_ICON, GlobalAuctionsPlugin.PREVIOUS_PAGE_ICON, (e) -> aucdb.getSells());
		this.aucdb = aucdb;
	}

	@Override
	public List<? extends ItemStackable> getInventoryStacks() 
	{
		return aucdb.getSells();
	}
}
