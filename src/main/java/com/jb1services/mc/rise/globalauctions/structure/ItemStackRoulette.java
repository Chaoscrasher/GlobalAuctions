package com.jb1services.mc.rise.globalauctions.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chaoscrasher.global.ChaosBukkit;
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
			count++;
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
	
	public Optional<ItemStack> getStack(int ind)
	{
		int i = 0;
		ItemStack toR = null;
		for (ItemStack key : this.itemRoulette.keySet())
		{
			if (i == ind)
			{
				toR = key;
				break;
			}
			i++;
		}
		return Optional.ofNullable(toR);
	}
	
	public boolean removeStack(int ind)
	{
		int i = 0;
		ItemStack toR = null;
		for (ItemStack key : this.itemRoulette.keySet())
		{
			if (i == ind)
			{
				toR = key;
				break;
			}
			i++;
		}
		if (toR != null)
		{
			itemRoulette.remove(toR);
			return true;
		}
		return false;
	}

	public void switchMode()
	{
		this.random = !random;
	}
	
	public List<ItemStack> getPageItems(int page)
	{
		final List<ItemStack> items = itemRoulette.keySet().stream().collect(Collectors.toList());
		List<ItemStack> retItems = new ArrayList<>();
		int startIndex = page * 54;
		if (startIndex < itemRoulette.size())
		{
			retItems = items.stream().filter(item -> items.indexOf(item) >= startIndex && items.indexOf(item) < startIndex+54).collect(Collectors.toList());
			return items;
		}
		return retItems;
	}
	
	/**
	 * Makes the inventory containing the necessary items of the given page.
	 * @return Empty optional if anything doesn't make sense, valid Inventory otherwise.
	 */
	public Optional<Inventory> asInventory(int page)
	{
		if (itemRoulette.size() > 0)
		{
			List<ItemStack> iss = getPageItems(page);
			if (iss.size() > 0)
			{
				int nslots = (int) Math.ceil(iss.size() / 9.0) * 9;
				int slots = nslots > 54 ? 54 : nslots;
				Inventory inv = Bukkit.createInventory(null, slots, GlobalAuctionsPlugin.ROULETTE_MENU_TITLE.makeFilledOutUS(page+""));
				for (int i = 0; i < iss.size(); i++)
				{
					ItemStack cnd = iss.get(i).clone();
					if (i < 53)
					{
						ChaosBukkit.applyLore(cnd, Arrays.asList("weight: " + itemRoulette.get(cnd)));
						inv.setItem(i, cnd);
					}
					else if (i == 53)
					{
						if (page > 0)
							inv.setItem(53, GlobalAuctionsPlugin.ROULETTE_PREVIOUS_PAGE_ICON.getSymbol());
						else
							inv.setItem(i, cnd);
					}
					else if (i == 54)
						inv.setItem(54, GlobalAuctionsPlugin.ROULETTE_NEXT_PAGE_ICON.getSymbol());
				}
				return Optional.of(inv);
			}
		}
		return Optional.empty();
	}
}
	