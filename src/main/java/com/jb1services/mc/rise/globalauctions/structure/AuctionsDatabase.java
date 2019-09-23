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
	private Map<UUIDS, Map<UUIDS, Auction>> auctions = new HashMap<>();
	
	public AuctionsDatabase()
	{
		
	}
	
	public AuctionsDatabase(Map<String, Object> data)
	{
		for (String key : data.keySet())
		{
			Object val = data.get(key);
			if (val instanceof Auction)
			{
				Auction auction = (Auction) val;
				String[] splits = key.split("\\.");
				UUIDS uuids = UUIDS.fromString(splits[0]);
				UUIDS auuids = UUIDS.fromString(splits[1]);
				auctions.putIfAbsent(uuids, new HashMap<>());
				auctions.get(uuids).put(auuids, auction);
			}
		}
	}
	
	public void addAuction(Auction auction)
	{
		Map<UUIDS, Auction> uauctions; 
		if (auctions.containsKey(auction.getCreator()))
		{
			uauctions = auctions.get(auction.getCreator());
		}
		else
			uauctions = new HashMap<>();
		
		uauctions.put(auction.getUuid(), auction);
		auctions.put(auction.getCreator(), uauctions);
	}
	
	/**
	 * Returns the one auction that contains the given uUIDPart.
	 * If there is more than one or no auction that contains
	 * the given uUIDPart, an empty Optional is returned.
	 */
	public Map<UUIDS, Auction> getAuctions(String uUIDPart)
	{
		Map<UUIDS, Auction> fittingAuctions = new HashMap<>();
		for (UUIDS user : auctions.keySet())
		{
			List<Auction> uauctions = getAuctions(uUIDPart, user);
			uauctions.forEach(auc -> fittingAuctions.put(user, auc));
		}
		return fittingAuctions;
	}
	
	public List<Auction> getAuctions(String uUIDPart, UUID creator)
	{
		return getAuctions(uUIDPart, UUIDS.of(creator));
	}
	
	public List<Auction> getAuctions(String uUIDPart, UUIDS creator)
	{
		List<Auction> fittingAuctions = new ArrayList<>();
		Map<UUIDS, Auction> usersAuctions = auctions.get(creator);
		for (UUIDS key : usersAuctions.keySet())
		{
			Auction auc = usersAuctions.get(key);
			if (auc.getUuid().toString().startsWith(uUIDPart))
			{
				if (!key.equals(auc.getUuid()))
					System.err.println("WARNING!! UUIDS KEY DIDN'T MATCH AUCTION UUIDS!");
				else
				{
					fittingAuctions.add(auc);
				}
			}
		}
		return fittingAuctions;
	}
	
	public void removeAuction(Auction auction)
	{
		auctions.get(auction.getCreator()).remove(auction.getUuid());
	}
	
	public Map<UUIDS, Auction> getAuctions(UUID user)
	{
		Map<UUIDS, Auction> aucs = getAuctions(UUIDS.of(user));
		return aucs;
	}
	
	public Map<UUIDS, Auction> getAuctions(UUIDS user)
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
			plugin.getConfig().set("auctions", auctions);
			plugin.saveConfig();
		}
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> aucs = new HashMap<>();
		for (UUIDS uuids : auctions.keySet())
		{
			aucs.put(uuids.toString(), auctions.get(uuids));
		}
		return aucs;
	}
	
	public Optional<Auction> getAuction(UUIDS plyr, UUIDS auction)
	{
		if (auctions.containsKey(plyr) && auctions.get(plyr).containsKey(auction))
			return Optional.of(auctions.get(plyr).get(auction));
		return Optional.empty();
	}

	public Map<UUIDS, Map<UUIDS, Auction>> getAuctions()
	{
		return Collections.unmodifiableMap(auctions);
	}
	
	public List<Auction> getSells()
	{
		List<Auction> sells = new ArrayList<>();
		for (Map<UUIDS, Auction> submap : auctions.values())
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
		for (Map<UUIDS, Auction> submap : auctions.values())
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
	
	public Optional<Auction> getAuction(UUIDS uuid)
	{
		for (Map<UUIDS, Auction> map: auctions.values())
		{
			if (map.containsKey(uuid))
				return Optional.of(map.get(uuid));
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
					UUIDS uuid = UUIDS.fromString(aucstr.split(GlobalAuctionsPlugin.AUCTION_PREFIX)[1]);
					Optional<Auction> auco = getAuction(uuid);
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
}
