package com.jb1services.mc.rise.globalauctions.structure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import com.jb1services.mc.rise.globalauctions.main.GlobalAuctionsPlugin;

public class ItemStackRoulette implements ConfigurationSerializable
{
	private boolean random;
	private Map<ItemStack, Integer> itemRoulette = new HashMap<>();

	public ItemStackRoulette(Map<ItemStack, Integer> itemRoulette)
	{
		super();
		this.itemRoulette = itemRoulette;
	}
	
	public ItemStackRoulette(ItemChance... chances)
	{
		for (ItemChance ic : chances)
		{
			itemRoulette.put(ic.getItem(), ic.getWeight());
		}
	}
	
	public boolean isRandom()
	{
		return random;
	}

	public Map<ItemStack, Integer> getItemRoulette()
	{
		return Collections.unmodifiableMap(itemRoulette);
	}

	public static ItemStackRoulette deserialize(Map<String, Object> map)
	{
		ItemStackRoulette ir = new ItemStackRoulette();
		for (String str : map.keySet())
		{
			Object val = map.get(str);
			if (val instanceof Map)
			{
				Map<String, Object> imap = (Map<String, Object>) val;
				ir.add((ItemStack) imap.get("item"), (Integer) imap.get("weight"));
				/*ItemStack is = (ItemStack) imap.get("item");
				Integer weight = (Integer) imap.get("weight")
				ir.add(imap.get, weight);
				return tR*/
			}
		}
		if (map.isEmpty())
			return new ItemStackRoulette();
		return ir;
	}
	
	public void add(ItemStack is, int weight)
	{
		itemRoulette.put(is, weight);
	}
	
	public Optional<ItemStack> decide()
	{
		Random rnd = new Random();
		
		if (!random)
		{
			int sum = 0;
			
			for (ItemStack is : itemRoulette.keySet())
			{
				sum += itemRoulette.get(is);
			}	
			
			int rval = rnd.nextInt(sum);
			
			sum = 0;
			for (ItemStack is : itemRoulette.keySet())
			{
				sum += itemRoulette.get(is);
				if (sum >= rval)
					return Optional.of(is);
			}
			return Optional.empty();
		}
		int i = 0;
		int ind = rnd.nextInt(itemRoulette.size());
		Iterator<ItemStack> it = itemRoulette.keySet().iterator();
		while (i < ind)
		{
			it.next();
		}
		return Optional.of(it.next());
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<>();
		int count = 0;
		map.put("random", random);
		for (ItemStack is : itemRoulette.keySet())
		{
			map.put(count+"", subMap(is));
		}
		return map;
	}
	
	public Map<String, Object> subMap(ItemStack is)
	{
		Map<String, Object> map = new HashMap<>();
		map.put("item", is);
		map.put("weight", itemRoulette.get(is));
		return map;
	}
	
	public void save(GlobalAuctionsPlugin plugin)
	{
		if (!itemRoulette.isEmpty())
		{
			plugin.getConfig().set("roulette", this);
			plugin.saveConfig();
		}
	}
}
	