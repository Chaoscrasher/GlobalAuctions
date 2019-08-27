package com.jb1services.mc.rise.globalauctions.events;

import java.util.List;
import java.util.Optional;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.chaoscrasher.events.ChaosEventListener;
import com.chaoscrasher.events.InventoryEventListener;
import com.chaoscrasher.utils.Debuggable;
import com.jb1services.mc.rise.globalauctions.main.GlobalAuctionsPlugin;
import com.jb1services.mc.rise.globalauctions.structure.Auction;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;


public class GlobalAuctionEvents extends InventoryEventListener implements Debuggable {

	public GlobalAuctionEvents(JavaPlugin plugin)
	{
		super(plugin);
		// TODO Auto-generated constructor stub
	}
	
	public GlobalAuctionsPlugin getPlugin()
	{
		return (GlobalAuctionsPlugin) plugin;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e)
	{
		Optional<Economy> econ = getPlugin().getEconomy();
		if (econ.isPresent())
		{
			Economy eco = econ.get();
			if (detectGlobalAuctionsInventory(e))
			{
				Inventory inv = e.getClickedInventory();
				Player player = (Player) e.getWhoClicked();
				if (e.getCurrentItem() != null)
				{
					ItemStack clicked = e.getCurrentItem();
					if (clicked.getItemMeta().getDisplayName().equals(GlobalAuctionsPlugin.ASK_EXECUTE_NAME) ||
							clicked.getItemMeta().getDisplayName().equals(GlobalAuctionsPlugin.SELL_EXECUTE_NAME))
					{
						ItemStack itemFromInv = inv.getItem(GlobalAuctionsPlugin.DEFAULT_ITEM_SLOT);
						List<String> lore = clicked.getItemMeta().getLore();
						Optional<Auction> auco = Auction.fromLore(getPlugin().getAuctionsDatabase(), lore);
						if (auco.isPresent())
						{
							Auction auc = auco.get();
							ItemStack auci = auc.getAuctionedItem();
							debug1("is from inv: " + itemFromInv + "\nis from auc: " + auc.getAuctionedItem() + "\neq: " + itemFromInv.equals(auc.getAuctionedItem()));
							auc.finalizeAuction(e.getView(), getPlugin(), player);
						}
						else
							throw new IllegalStateException("ItemSlot doesn't contain item!");
					}
				}
				e.setCancelled(true);
			}
		}
		else
			throw new IllegalStateException("Economy not initialized!");
	}
	
	public boolean detectGlobalAuctionsInventory(InventoryClickEvent e)
	{
		return clickedOnNotOwnedInventory(e) && clickedOnInventoryWhereTitlePred(e, tit -> tit.startsWith(GlobalAuctionsPlugin.INVENTORY_TITLE_PREFIX));
	}

	@Override
	public boolean isd1()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isd2()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
