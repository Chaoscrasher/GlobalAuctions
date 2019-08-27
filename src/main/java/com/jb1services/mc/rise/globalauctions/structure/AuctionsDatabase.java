package com.jb1services.mc.rise.globalauctions.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import com.jb1services.mc.rise.globalauctions.main.GlobalAuctionsPlugin;

public class AuctionsDatabase implements ConfigurationSerializable
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
		plugin.getConfig().set("auctions", auctions);
		plugin.saveConfig();
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
}
