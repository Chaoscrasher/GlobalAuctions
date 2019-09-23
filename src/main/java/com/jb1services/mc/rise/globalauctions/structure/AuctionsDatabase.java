package com.jb1services.mc.rise.globalauctions.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chaoscrasher.utils.Debuggable;
import com.jb1services.mc.rise.globalauctions.main.GlobalAuctionsPlugin;

public class AuctionsDatabase implements ConfigurationSerializable, Debuggable
{	
	private Map<UUIDS, Map<Integer, Auction>> auctions = new HashMap<>();
	
	public AuctionsDatabase()
	{
		
	}
	
	public AuctionsDatabase(Map<String, Object> data)
	{
		this.auctions = (Map<UUIDS, Map<Integer, Auction>>) data.get("auctions");
	}
	
	public void addAuction(Auction auction)
	{
		Map<Integer, Auction> uauctions; 
		if (auctions.containsKey(auction.getCreator()))
		{
			uauctions = auctions.get(auction.getCreator());
		}
		else
			uauctions = new HashMap<>();
		
		uauctions.put(auction.getId(), auction);
		auctions.put(auction.getCreator(), uauctions);
	}
	
	public void removeAuction(Auction auction)
	{
		auctions.get(auction.getCreator()).remove(auction.getId());
	}
	
	public Map<Integer, Auction> getAuctions(UUID user)
	{
		Map<Integer, Auction> aucs = getAuctions(UUIDS.of(user));
		return aucs;
	}
	
	public Map<Integer, Auction> getAuctions(UUIDS user)
	{
		if (auctions.containsKey(user))
			return auctions.get(user);
		else
			return new HashMap<>();
	}
	
	public boolean hasUserAuctions(UUIDS user)
	{
		return auctions.containsKey(user);
	}
	
	public void save(GlobalAuctionsPlugin plugin) throws IOException
	{
		if (!auctions.isEmpty())
		{
			plugin.getConfig().set("auctions", this);
			plugin.saveConfig();
		}
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> aucs = new HashMap<>();
		aucs.put("auctions", auctions);
		return aucs;
	}
	
	public Optional<Auction> getAuction(UUIDS plyr, int auction, double price)
	{
		if (auctions.containsKey(plyr) && auctions.get(plyr).containsKey(auction))
		{
			Auction auc = auctions.get(plyr).get(auction);
			if (auc.getPrice() == price)
				return Optional.of(auc);
		}
		return Optional.empty();
	}

	public Map<UUIDS, Map<Integer, Auction>> getAuctions()
	{
		return Collections.unmodifiableMap(auctions);
	}
	
	public List<Auction> getSells()
	{
		List<Auction> sells = new ArrayList<>();
		for (Map<Integer, Auction> submap : auctions.values())
		{
			for (Auction auc : submap.values())
			{
				if (!auc.isAsk())
					sells.add(auc);
			}
		}
		return sells;
	}
	
	public List<Auction> getAsks()
	{
		List<Auction> asks = new ArrayList<>();
		for (Map<Integer, Auction> submap : auctions.values())
		{
			for (Auction auc : submap.values())
			{
				if (auc.isAsk())
					asks.add(auc);
			}
		}
		return asks;
	}
	
	public Optional<Inventory> makeSellsInventory(int page)
	{
		List<Auction> sells = getSells();
		if (!sells.isEmpty())
		{
			int necessarySlots = (int) (9 * Math.ceil(sells.size() / 9.0));
			if (necessarySlots > 54)
			{
				debug1("Necessary slots would have been > 54, only displaying 54.");
			}
			int slots = necessarySlots > 54 ? 54 : necessarySlots;
			Inventory inv = Bukkit.createInventory(null, slots, GlobalAuctionsPlugin.SELLS_MENU_TITLE);
			for (int i = 0; i < sells.size(); i++)
			{
				inv.setItem(i, sells.get(i).makeMenuItemStack());
				i++;
			}
			return Optional.of(inv);
		}
		return Optional.empty();
	}
	
	public List<ItemStack> getAskPageItems(int page)
	{
		final List<ItemStack> items = getAsks().stream().map(auc -> auc.makeMenuItemStack()).collect(Collectors.toList());
		List<ItemStack> retItems = new ArrayList<>();
		int startIndex = page * 54;
		if (startIndex < items.size())
		{
			retItems = items.stream().filter(item -> items.indexOf(item) >= startIndex && items.indexOf(item) < startIndex+54).collect(Collectors.toList());
			return items;
		}
		return retItems;
	}
	
	public List<ItemStack> getSellsPageItems(int page)
	{
		final List<ItemStack> items = getSells().stream().map(auc -> auc.makeMenuItemStack()).collect(Collectors.toList());
		List<ItemStack> retItems = new ArrayList<>();
		int startIndex = page * 54;
		if (startIndex < items.size())
		{
			retItems = items.stream().filter(item -> items.indexOf(item) >= startIndex && items.indexOf(item) < startIndex+54).collect(Collectors.toList());
			return items;
		}
		return retItems;
	}
	
	public Optional<Inventory> makeAsksInventory(int page)
	{
		List<Auction> asks = getAsks();
		if (!asks.isEmpty())
		{
			int necessarySlots = (int) (9 * Math.ceil(asks.size() / 9.0));
			if (necessarySlots > 54)
			{
				debug1("Necessary slots would have been > 54, only displaying 54.");
			}
			int slots = necessarySlots > 54 ? 54 : necessarySlots;
			Inventory inv = Bukkit.createInventory(null, slots, GlobalAuctionsPlugin.ASKS_MENU_TITLE);
			for (int i = 0; i < asks.size(); i++)
			{
				inv.setItem(i, asks.get(i).makeMenuItemStack());
				i++;
			}
			return Optional.of(inv);
		}
		return Optional.empty();
	}
	
	public Optional<Auction> getAuctionById(int id)
	{
		for (Map<Integer, Auction> map: auctions.values())
		{
			if (map.containsKey(id))
				return Optional.of(map.get(id));
		}
		return Optional.empty();
	}
	
	public Optional<Auction> getAuctionFromItemStackSymbol(ItemStack is)
	{
		try
		{
			List<String> lore = is.getItemMeta().getLore();
			if (lore.size() > 1)
			{
				String aucstr = lore.get(0);
				if (aucstr.startsWith(GlobalAuctionsPlugin.AUCTION_PREFIX))
				{
					int id = Integer.valueOf(aucstr.split(GlobalAuctionsPlugin.AUCTION_PREFIX)[1]);
					Optional<Auction> auco = getAuctionById(id);
					return auco;
				}
			}
		}
		catch (Exception e){e.printStackTrace();}
		return Optional.empty();
	}

	@Override
	public boolean isd1() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isd2() {
		// TODO Auto-generated method stub
		return false;
	}

	public void clear() 
	{
		this.auctions.clear();
	}
	
	public int nextFreeId() 
	{
		List<Integer> usedIds = new ArrayList<>();
		for (Map<Integer, Auction> auctionsm : auctions.values())
		{
			usedIds.addAll(auctionsm.keySet());
		}
		for (int i = 0; i < Integer.MAX_VALUE; i++)
		{
			if (!usedIds.contains(i))
				return i;
		}
		throw new IllegalStateException("Using more IDs than possible Integer numbers!");
	}
	
	public int size()
	{
		int size = 0;
		for (Map<Integer, Auction> auctionsm : auctions.values())
		{
			size += auctionsm.size();
		}
		return size;
	}
}
