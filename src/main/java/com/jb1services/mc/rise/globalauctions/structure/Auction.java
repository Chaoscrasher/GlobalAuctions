package com.jb1services.mc.rise.globalauctions.structure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.jb1services.mc.rise.globalauctions.main.GlobalAuctionsPlugin;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class Auction implements ConfigurationSerializable
{
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
	
	public Auction(UUID creator, ItemStack itemStack, double price, boolean ask)
	{
		this(UUIDS.of(creator), UUIDS.randomUUIDS(), itemStack, price, ask);
	}
	
	public Auction(UUID creator, ItemStack itemStack, double price)
	{
		this(UUIDS.of(creator), UUIDS.randomUUIDS(), itemStack, price, false);
	}
	
	public Auction(UUIDS creator, ItemStack itemStack, double price, boolean ask)
	{
		this(creator, UUIDS.randomUUIDS(), itemStack, price, ask);
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
	
	public String makeInventoryTitle(GlobalAuctionsPlugin plugin)
	{
		String pname = Bukkit.getOfflinePlayer(creator.getUUID()).getName();
		return plugin.getInventoryTitle().replace("{TYPE}", isAsk ? ChatColor.DARK_RED+"Auction" : ChatColor.DARK_GREEN+"Ask").replace("{USER_NAME}", pname);
	}
	
	public void showToPlayer(GlobalAuctionsPlugin plugin, Player player)
	{
		player.openInventory(toInventory(plugin));
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
	
	public static Optional<Auction> fromLore(AuctionsDatabase db, List<String> lore)
	{
		if (lore.size() == GlobalAuctionsPlugin.DEFAULT_LORE_SIZE)
		{
			UUIDS plyr = UUIDS.fromString(lore.get(0).substring(lore.get(0).indexOf(GlobalAuctionsPlugin.PLAYER_PREFIX) + GlobalAuctionsPlugin.PLAYER_PREFIX.length()));
			UUIDS aucuid = UUIDS.fromString(lore.get(1).substring(lore.get(1).indexOf(GlobalAuctionsPlugin.AUCTION_PREFIX) + GlobalAuctionsPlugin.AUCTION_PREFIX.length()));
//			double price = Double.valueOf(lore.get(2).substring(lore.get(2).indexOf(GlobalAuctionsPlugin.PRICE_PREFIX) + GlobalAuctionsPlugin.PRICE_PREFIX.length()));
			Optional<Auction> auco = db.getAuction(plyr, aucuid);
			return auco;
		}
		else
			throw new IllegalStateException("Lore is not of size "+GlobalAuctionsPlugin.DEFAULT_LORE_SIZE+"!");
	}
	
	private ItemStack makeBuyItem()
	{
		List<String> lore = Arrays.asList(GlobalAuctionsPlugin.PLAYER_PREFIX + creator.toString(), GlobalAuctionsPlugin.AUCTION_PREFIX + uuid.toString(), GlobalAuctionsPlugin.PRICE_PREFIX + price);
		ItemStack is = new ItemStack(Material.GOLD_INGOT);
		ItemMeta im = is.getItemMeta();
		im.setLore(lore);
		im.setDisplayName(isAsk ? GlobalAuctionsPlugin.ASK_EXECUTE_NAME : GlobalAuctionsPlugin.SELL_EXECUTE_NAME);
		is.setItemMeta(im);
		return is;
	}
	
	public Inventory toInventory(GlobalAuctionsPlugin plugin)
	{
		Inventory inv = Bukkit.createInventory(null, 9, makeInventoryTitle(plugin));
		inv.setItem(GlobalAuctionsPlugin.DEFAULT_ITEM_SLOT, auctionedItem);
		inv.setItem(inv.getSize()-1, makeBuyItem());
		return inv;
	}
	
	public OfflinePlayer getCreatorAsOfflinePlayer()
	{
		return Bukkit.getOfflinePlayer(creator.getUUID());
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((auctionedItem == null) ? 0 : auctionedItem.hashCode());
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
		result = prime * result + (isAsk ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(price);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}
	
	private boolean finalizeAsk(Economy eco, AuctionsDatabase db, Player player)
	{
		if (player.getInventory().contains(auctionedItem))
		{
			OfflinePlayer op = getCreatorAsOfflinePlayer();
			if (op.isOnline())
			{
				if (eco.getBalance(op) >= getPrice())
				{
					eco.withdrawPlayer(op, getPrice());
					player.getInventory().remove(auctionedItem);
					op.getPlayer().sendMessage(ChatColor.GREEN + player.getName() + " completed your ask " + getUuid() + "\n" + 
							getAuctionedItem().getType() + "x " + getAuctionedItem().getAmount() + "\nfor " +
							getPrice());
					op.getPlayer().getInventory().addItem(getAuctionedItem());
					player.sendMessage(executedSuccess());
					return true;
				}
				else
					player.sendMessage(ChatColor.RED + "Sorry, but "+op.getName()+" doesn't currently have the money to complete this ask!");
			}
			else
				player.sendMessage(ChatColor.DARK_RED + "Sorry, but " + op.getName() + " needs to be online for that!");
		}
		else
			player.sendMessage(ChatColor.RED + "Sorry, but you don't have the item to complete this ask!");
		return false;
	}
	
	private boolean finalizeSell(Economy eco, AuctionsDatabase db, Player player)
	{
		if (eco.getBalance(player) >= getPrice())
		{
			OfflinePlayer op = getCreatorAsOfflinePlayer();
			if (op.isOnline())
			{
				eco.withdrawPlayer(player, getPrice());
				op.getPlayer().getInventory().addItem(getAuctionedItem());
				op.getPlayer().sendMessage(ChatColor.GREEN + player.getName() + " completed your auction " + getUuid() + "\n" + 
						getAuctionedItem().getType() + "x " + getAuctionedItem().getAmount() + "\nfor " +
						getPrice());
				player.sendMessage(executedSuccess());
				return true;
			}
			else
				player.sendMessage(ChatColor.DARK_RED + "Sorry, but " + op.getName() + " needs to be online for that!");
		}
		else
			player.sendMessage(ChatColor.RED + "Sorry, but you don't have the money to complete this auction!");
		return false;
	}
	
	private String executedSuccess()
	{
		return ChatColor.GREEN + "You completed "+(isAsk ? ChatColor.DARK_GREEN + "Ask " : ChatColor.RED + "Sell ") + " \n" + ChatColor.GOLD + getUuid() + ChatColor.GREEN + "\nby " + getCreatorAsOfflinePlayer().getName() + "!";
	}

	public boolean finalizeAuction(InventoryView iv, GlobalAuctionsPlugin plugin, Player player)
	{
		Economy eco = plugin.getEconomy().get();
		AuctionsDatabase db = plugin.getAuctionsDatabase();
		boolean res = false;
		if (isAsk())
		{
			res = finalizeAsk(eco, db, player);
		}
		else
		{
			res = finalizeSell(eco, db, player);
		}
		
		if (res)
		{
			db.removeAuction(this);
			iv.close();
		}
		return res;
	}

	@Override
	public boolean equals(Object thato)
	{
		if (this == thato)
			return true;
		if (thato != null)
		{
			if (thato.getClass().equals(this.getClass()))
			{
				Auction that = (Auction) thato;
				if (creator == that.creator || creator.equals(that.creator))
				{
					if (uuid == that.uuid || uuid.equals(that.uuid))
					{
						if (auctionedItem == that.auctionedItem || auctionedItem.equals(that.auctionedItem))
						{
							return price == that.price && isAsk == that.isAsk;
						}
					}
				}
			}
		}
		return false;
	}
	
	public String toIngameString(boolean includeCreator)
	{
		return (includeCreator ? getCreatorAsOfflinePlayer().getName() + ": " : "") + uuid + " " + (isAsk ? "(Ask)" : "(Sell)") +"\n" + auctionedItem.getType() + " x" + auctionedItem.getAmount() + " for " + price;
	}
}