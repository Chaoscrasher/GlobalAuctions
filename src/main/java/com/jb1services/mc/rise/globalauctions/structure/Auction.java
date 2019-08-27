package com.jb1services.mc.rise.globalauctions.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class Auction implements ConfigurationSerializable
{
	public static final String INVENTORY_TITLE = "{TYPE} by {USER_NAME}.";
	
	private UUIDS creator;
	private UUIDS uuid;
	private ItemStack auctionedItem;
	private double price;
	private boolean isAsk;
	
	public Auction(Map<String, Object> map)
	{
		this.creator = (UUIDS) map.get("creator");
		this.uuid = (UUIDS) map.get("uuid");
		this.auctionedItem = (ItemStack) map.get("auctionedItem");
		this.price = (double) map.get("price");
		this.isAsk = (boolean) map.get("isAsk");
	}
	
	public Auction(UUID creator, ItemStack itemStack, double price, boolean sell)
	{
		this(UUIDS.of(creator), UUIDS.randomUUIDS(), itemStack, price, sell);
	}
	
	public Auction(UUIDS creator, ItemStack itemStack, double price, boolean sell)
	{
		this(creator, UUIDS.randomUUIDS(), itemStack, price, sell);
	}

	/*
	private Auction(UUID creator, UUID uuid, ItemStack itemStack, double price, boolean isAsk)
	{
		this(UUIDS.of(creator), UUIDS.of(uuid), itemStack, price, isAsk);
	}
	*/
	
	private Auction(UUIDS creator, UUIDS uuid, ItemStack itemStack, double price, boolean isAsk)
	{
		super();
		this.creator = creator;
		this.uuid = uuid;
		this.auctionedItem = itemStack;
		this.price = price;
		this.isAsk = isAsk;
	}
	
	public UUIDS getCreator()
	{
		return creator;
	}

	public boolean isAsk()
	{
		return isAsk;
	}

	public UUIDS getUuid()
	{
		return uuid;
	}

	public ItemStack getAuctionedItem()
	{
		return auctionedItem;
	}
	
	public double getPrice()
	{
		return price;
	}
	
	public String makeInventoryTitle()
	{
		String pname = Bukkit.getOfflinePlayer(creator.getUUID()).getName();
		return INVENTORY_TITLE.replace("{TYPE}", isAsk ? ChatColor.DARK_RED+"Auction" : ChatColor.DARK_GREEN+"Ask").replace("{USER_NAME}", pname);
	}
	
	public void showToPlayer(Player player)
	{
		Inventory inv = Bukkit.createInventory(null, 9, makeInventoryTitle());
		player.openInventory(inv);
	}
	
	public void save(ConfigurationSection csec)
	{
		csec.set("uuid", uuid);
		csec.set("item", auctionedItem.serialize());
		csec.set("price", price);
		csec.set("sell", isAsk);
	}
	
	public static Auction load(ConfigurationSection csec)
	{
		UUIDS creator = UUIDS.fromString(csec.getString("creator"));
		UUIDS uuid = UUIDS.fromString(csec.getString("uuid"));
		ItemStack is = ItemStack.deserialize(csec.getConfigurationSection("item").getValues(true));
		double price = csec.getDouble("price");
		boolean sell = csec.getBoolean("sell");
		return new Auction(creator, uuid, is, price, sell);
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> hm = new HashMap<>();
		hm.put("creator", creator);
		hm.put("uuid", uuid);
		hm.put("auctionedItem", auctionedItem);
		hm.put("price", price);
		hm.put("isAsk", isAsk);
		return hm;
	}
}
