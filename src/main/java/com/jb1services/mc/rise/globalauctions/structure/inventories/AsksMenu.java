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

public class AsksMenu extends InventoryScrollableFixedClickable<GlobalAuctionsPlugin>
{
	private AuctionsDatabase aucdb;
	
	public AsksMenu(AuctionsDatabase aucdb) 
	{
		super(GlobalAuctionsPlugin.ASKS_MENU_TITLE, ChatColor.WHITE, GlobalAuctionsPlugin.NEXT_PAGE_ICON, GlobalAuctionsPlugin.PREVIOUS_PAGE_ICON, (e) -> aucdb.getAsks());
		this.aucdb = aucdb;
	}

	@Override
	public List<? extends ItemStackable> getInventoryStacks() 
	{
		return aucdb.getAsks();
	}
}
