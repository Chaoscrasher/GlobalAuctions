package com.jb1services.mc.rise.globalauctions.structure;

import static com.jb1services.mc.rise.globalauctions.main.GlobalAuctionsPlugin.*;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.chaoscrasher.global.ChaosBukkit;
import com.jb1services.mc.rise.globalauctions.main.GlobalAuctionsPlugin;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class Auction implements ConfigurationSerializable, InventoryClickable<GlobalAuctionsPlugin>
{
	private UUIDS creator;
	private Integer id;
	private ItemStack auctionedItem;
	private double price;
	private boolean isAsk;
	
	public Auction(Map<String, Object> map)
	{
		this.creator = (UUIDS) map.get("creator");
		this.id = (Integer) map.get("id");
		this.auctionedItem = (ItemStack) map.get("auctionedItem");
		this.price = (double) map.get("price");
		this.isAsk = (boolean) map.get("isAsk");
	}
	
	public Auction(AuctionsDatabase adb, UUID creator, ItemStack itemStack, double price, boolean ask)
	{
		this(adb, UUIDS.of(creator), itemStack, price, ask);
	}
	
	public Auction(AuctionsDatabase adb, UUID creator, ItemStack itemStack, double price)
	{
		this(adb, UUIDS.of(creator), itemStack, price, false);
	}
	
	public Auction(AuctionsDatabase adb, UUIDS creator, ItemStack itemStack, double price, boolean ask)
	{
		this(creator, adb.nextFreeId(), itemStack, price, ask);
	}

	/*
	private Auction(UUID creator, UUID uuid, ItemStack itemStack, double price, boolean isAsk)
	{
		this(UUIDS.of(creator), UUIDS.of(uuid), itemStack, price, isAsk);
	}
	*/
	
	private Auction(UUIDS creator, Integer id, ItemStack itemStack, double price, boolean isAsk)
	{
		this.creator = creator;
		this.id = id;
		if (itemStack.getType().equals(Material.AIR))
			throw new IllegalStateException("Cannot auction item stack of Material.AIR!");
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

	public int getId()
	{
		return id;
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
		return plugin.getInventoryTitle().replace("{TYPE}", isAsk ? ChatColor.DARK_GREEN+"Ask" : ChatColor.DARK_RED+"Auction").replace("{USER_NAME}", pname);
	}
	
	public void showToPlayer(GlobalAuctionsPlugin plugin, Player player)
	{
		player.openInventory(toInventory(plugin));
	}
	
	public void save(ConfigurationSection csec)
	{
		csec.set("id", id);
		csec.set("item", auctionedItem.serialize());
		csec.set("price", price);
		csec.set("sell", isAsk);
	}
	
	public static Auction load(ConfigurationSection csec)
	{
		UUIDS creator = UUIDS.fromString(csec.getString("creator"));
		int id = csec.getInt("id");
		ItemStack is = ItemStack.deserialize(csec.getConfigurationSection("item").getValues(true));
		double price = csec.getDouble("price");
		boolean sell = csec.getBoolean("sell");
		return new Auction(creator, id, is, price, sell);
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> hm = new HashMap<>();
		hm.put("creator", creator);
		hm.put("id", id);
		hm.put("auctionedItem", auctionedItem);
		hm.put("price", price);
		hm.put("isAsk", isAsk);
		return hm;
	}
	
	public static Optional<Auction> fromLore(AuctionsDatabase db, List<String> lore)
	{
		if (lore.size() == GlobalAuctionsPlugin.DEFAULT_LORE_SIZE)
		{
			String pname = lore.get(0).substring(lore.get(0).indexOf(GlobalAuctionsPlugin.PLAYER_PREFIX) + GlobalAuctionsPlugin.PLAYER_PREFIX.length());
			int aucid = Integer.valueOf(lore.get(1).substring(lore.get(1).indexOf(GlobalAuctionsPlugin.AUCTION_PREFIX) + GlobalAuctionsPlugin.AUCTION_PREFIX.length()));
			double price = Double.valueOf(lore.get(2).substring(lore.get(2).indexOf(GlobalAuctionsPlugin.PRICE_PREFIX) + GlobalAuctionsPlugin.PRICE_PREFIX.length()));
			Optional<OfflinePlayer> op = ChaosBukkit.getOfflinePlayerByName(pname);
			if (op.isPresent())
			{
				Optional<Auction> auco = db.getAuction(UUIDS.of(op.get().getUniqueId()), aucid, price);
				return auco;
			}
			return Optional.empty();
		}
		else
			throw new IllegalStateException("Lore is not of size "+GlobalAuctionsPlugin.DEFAULT_LORE_SIZE+"!");
	}
	
	private ItemStack makeBuyItem()
	{
		List<String> lore = Arrays.asList(GlobalAuctionsPlugin.PLAYER_PREFIX + getCreatorAsOfflinePlayer().getName(), GlobalAuctionsPlugin.AUCTION_PREFIX + id, GlobalAuctionsPlugin.PRICE_PREFIX + price);
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((auctionedItem == null) ? 0 : auctionedItem.hashCode());
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isAsk ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(price);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
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
					if (id == that.id)
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
					op.getPlayer().sendMessage(ChatColor.GREEN + player.getName() + " completed your ask " + id + "\n" + 
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
				op.getPlayer().sendMessage(ChatColor.GREEN + player.getName() + " completed your auction " + id + "\n" + 
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
		return ChatColor.GREEN + "You completed "+(isAsk ? ChatColor.DARK_GREEN + "Ask " : ChatColor.RED + "Sell ") + " \n" + ChatColor.GOLD + id + ChatColor.GREEN + "\nby " + getCreatorAsOfflinePlayer().getName() + "!";
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

	
	
	public ItemStack makeMenuItemStack()
	{
		ItemStack is = new ItemStack(this.auctionedItem.getType(), this.auctionedItem.getAmount());
		ItemMeta im = is.getItemMeta();
		if (im != null)
		{
			im.setLore(Arrays.asList("Creator: " + Bukkit.getOfflinePlayer(this.creator.getUUID()).getName(), AUCTION_ITEM_AUCTION_LORE.replace(AUCTION_ITEM_AUCTION_LORE_PLACEHOLDER, id+""), "Price: " + this.getPrice()));
			is.setItemMeta(im);
			return is;
		}
		else return null;
	}
	
	public String toIngameString(boolean includeCreator)
	{
		return (includeCreator ? getCreatorAsOfflinePlayer().getName() + ": " : "") + id + " " + (isAsk ? "(Ask)" : "(Sell)") +"\n" + auctionedItem.getType() + " x" + auctionedItem.getAmount() + " for " + price;
	}

	@Override
	public ItemStack stack() 
	{
		return makeMenuItemStack();
	}

	@Override
	public Optional<Inventory> onClick(GlobalAuctionsPlugin plugin, InventoryClickEvent e) 
	{
		return Optional.of(toInventory(plugin));
	}
}