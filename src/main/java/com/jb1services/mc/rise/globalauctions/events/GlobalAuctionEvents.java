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
import com.chaoscrasher.global.CCStringUtils;
import com.chaoscrasher.utils.Debuggable;
import com.jb1services.mc.rise.globalauctions.main.GlobalAuctionsPlugin;
import com.jb1services.mc.rise.globalauctions.structure.Auction;
import com.jb1services.mc.rise.globalauctions.structure.AuctionsDatabase;
import com.jb1services.mc.rise.globalauctions.structure.inventories.AsksMenu;
import com.jb1services.mc.rise.globalauctions.structure.inventories.SellsMenu;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;


public class GlobalAuctionEvents extends InventoryEventListener implements Debuggable {

	private AsksMenu asksMenu;
	private SellsMenu sellsMenu;
	
	public GlobalAuctionEvents(JavaPlugin plugin, AuctionsDatabase adb)
	{
		super(plugin);
		this.asksMenu = new AsksMenu(adb);
		this.sellsMenu = new SellsMenu(adb);
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
			if (globalAuctionsInventoryDetected(e))
			{
				Player player = (Player) e.getWhoClicked();
				e.setCancelled(true);
				if (auctionMenuDetected(e))
				{
					auctionProcessing(e);
				}
				else if (mainMenuDetected(e))
				{
					mainMenuProcessing(e);
				}
				else if (asksMenu.detect(e))
				{
					Optional<Inventory> nInv = asksMenu.onClick(getPlugin(), e);
					if (nInv.isPresent())
					{
						player.openInventory(nInv.get());
					}
				}
 				else if (sellsMenu.detect(e))
				{
					Optional<Inventory> nInv = sellsMenu.onClick(getPlugin(), e);
					if (nInv.isPresent())
					{
						player.openInventory(nInv.get());
					}
				}
				else if (getPlugin().getItemRoulette().detect(e))
				{
					Optional<Inventory> nInv = getPlugin().getItemRoulette().onClick(getPlugin(), e);
					if (nInv != null)
					{
						if (nInv.isPresent())
						{
							player.openInventory(nInv.get());
						}
					}
					else
						player.closeInventory();
				}
				/*
				else if (detectAskMenu(e))
				{
					asksSellsMenuClickProcessing(e, player);
				}
				*/
				/*
				else if (detectSellMenu(e))
				{
					asksSellsMenuClickProcessing(e, player);
				}
				else if (detectRouletteMenu(e))
				{
					rouletteMenuProcessing(e, player);
				}
				*/
			}
		}
		else
			throw new IllegalStateException("Economy not initialized!");
	}
	
	public boolean globalAuctionsInventoryDetected(InventoryClickEvent e)
	{
		return clickedOnNotOwnedInventory(e) && clickedOnInventoryWhereTitlePred(e, tit -> tit.startsWith(GlobalAuctionsPlugin.GAS));
	}
	
	public boolean mainMenuDetected(InventoryClickEvent e)
	{
		return clickedOnNotOwnedInventory(e) && clickedOnInventoryWhereTitlePred(e, tit -> tit.startsWith(GlobalAuctionsPlugin.MAIN_MENU_TITLE));
	}
	
	private void mainMenuProcessing(InventoryClickEvent e)
	{
		Inventory inv = e.getClickedInventory();
		Player player = (Player) e.getWhoClicked();
		if (e.getCurrentItem() != null)
		{
			ItemStack clicked = e.getCurrentItem();
			if (clicked.getItemMeta().getDisplayName().equals(GlobalAuctionsPlugin.MAIN_MENU_SELLS_TITLE))
			{
				Optional<Inventory> sellso = sellsMenu.toInventory(0);//getPlugin().getAuctionsDatabase().makeSellsInventory(0);
				if (sellso.isPresent())
				{
					player.openInventory(sellso.get());
				}
				else
					player.sendMessage("There are no open sells currently!");
			}
			else if (clicked.getItemMeta().getDisplayName().equals(GlobalAuctionsPlugin.MAIN_MENU_ASKS_TITLE))
			{
				Optional<Inventory> askso = asksMenu.toInventory(0);//getPlugin().getAuctionsDatabase().makeAsksInventory(0);
				if (askso.isPresent())
				{
					player.openInventory(askso.get());
				}
				else
					player.sendMessage("There are no open asks currently!");
			}
		}
		e.setCancelled(true);
	}
	
	public boolean auctionMenuDetected(InventoryClickEvent e)
	{
		return clickedOnNotOwnedInventory(e) && clickedOnInventoryWhereTitlePred(e, tit -> CCStringUtils.containsIgnoreFiller(GlobalAuctionsPlugin.AUCTION_TITLE, tit));
	}
	
	private void auctionProcessing(InventoryClickEvent e)
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
	
	private int getPage(InventoryClickEvent e)
	{
		return Integer.valueOf(GlobalAuctionsPlugin.ROULETTE_MENU_FILLER.getParsedValueUS(0, e.getView().getTitle()));
	}
	
	/*
	private void movePage(InventoryClickEvent e, Player p, int delta)
	{
		int page = getPage(e);
		p.openInventory(getPlugin().getItemRoulette().toInventory(page+1).get());
	}
	*/
	
	/*
	private void rouletteMenuProcessing(InventoryClickEvent e, Player p)
	{
		if (e.getCurrentItem().equals(GlobalAuctionsPlugin.NEXT_PAGE_ICON.getSymbol()))
		{
			movePage(e, p, 1);
		}
		else if (e.getCurrentItem().equals(GlobalAuctionsPlugin.PREVIOUS_PAGE_ICON.getSymbol()))
		{
			movePage(e, p, -1);
		}
	}
	*/
	
	private void asksSellsMenuClickProcessing(InventoryClickEvent e, Player player)
	{
		Inventory inv = e.getClickedInventory();
		ItemStack cs = inv.getItem(e.getSlot());
		if (cs != null)
		{
			Optional<Auction> auco = getPlugin().getAuctionsDatabase().getAuctionFromItemStackSymbol(cs);
			if (auco.isPresent())
			{
				auco.get().showToPlayer(getPlugin(), player);
			}
			else if (GlobalAuctionsPlugin.NEXT_PAGE_ICON.getSymbol().equals(cs))
			{
				
			}
			else if (GlobalAuctionsPlugin.PREVIOUS_PAGE_ICON.getSymbol().equals(cs))
			{
				
			}
		}
	}
	
	public boolean detectMainMenu(InventoryClickEvent e)
	{
		return clickedOnNotOwnedInventory(e) && clickedOnInventoryWhereTitlePred(e, tit -> tit.startsWith(GlobalAuctionsPlugin.MAIN_MENU_TITLE));
	}
	
	public boolean detectSellMenu(InventoryClickEvent e)
	{
		return clickedOnNotOwnedInventory(e) && clickedOnInventoryWhereTitlePred(e, tit -> tit.startsWith(GlobalAuctionsPlugin.SELLS_MENU_TITLE));
	}
	
	public boolean detectAskMenu(InventoryClickEvent e)
	{
		return clickedOnNotOwnedInventory(e) && clickedOnInventoryWhereTitlePred(e, tit -> tit.startsWith(GlobalAuctionsPlugin.ASKS_MENU_TITLE));
	}
	
	public boolean detectRouletteMenu(InventoryClickEvent e)
	{
		return clickedOnNotOwnedInventory(e) && clickedOnInventoryWhereTitlePred(e, tit -> GlobalAuctionsPlugin.ROULETTE_MENU_FILLER.matches(tit));
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
