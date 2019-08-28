package com.jb1services.mc.rise.globalauctions.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.chaoscrasher.utils.VaultCollection;
import com.jb1services.mc.rise.globalauctions.commands.GlobalAuctionsCommand;
import com.jb1services.mc.rise.globalauctions.events.GlobalAuctionEvents;
import com.jb1services.mc.rise.globalauctions.structure.Auction;
import com.jb1services.mc.rise.globalauctions.structure.AuctionsDatabase;
import com.jb1services.mc.rise.globalauctions.structure.ItemStackRoulette;
import com.jb1services.mc.rise.globalauctions.structure.UUIDS;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class GlobalAuctionsPlugin extends JavaPlugin {

	public static final String INVENTORY_TITLE_PREFIX = ChatColor.DARK_RED+"G"+ChatColor.GOLD+"A"+ChatColor.DARK_GREEN+"S"+ChatColor.WHITE+": ";
	public static final String INVENTORY_TITLE_CONTENT = "{TYPE} by {USER_NAME}.";
	public static final String INVENTORY_TITLE = INVENTORY_TITLE_PREFIX + INVENTORY_TITLE_CONTENT;
	public static final String ASK_EXECUTE_NAME = ChatColor.DARK_GREEN + "SELL";
	public static final String SELL_EXECUTE_NAME = ChatColor.DARK_RED + "BUY";
	
	public static final String PLAYER_PREFIX = "Player: ";
	public static final String AUCTION_PREFIX = "Auction: ";
	public static final String PRICE_PREFIX = "Price: ";
	
	public static final int DEFAULT_ITEM_SLOT = 0;
	public static final int DEFAULT_LORE_SIZE = 3;
	
	public static GlobalAuctionsPlugin instance;
	
	private AuctionsDatabase auctionsDatabase = new AuctionsDatabase();
	private ItemStackRoulette itemRoulette = new ItemStackRoulette();
	private VaultCollection vcol;

	@Override
	public void onEnable()
	{
		instance = this;
		new GlobalAuctionsCommand(this);
		new GlobalAuctionEvents(this);
		this.saveDefaultConfig();
		ConfigurationSerialization.registerClass(AuctionsDatabase.class);
		ConfigurationSerialization.registerClass(Auction.class);
		ConfigurationSerialization.registerClass(UUIDS.class);
		ConfigurationSerialization.registerClass(ItemStackRoulette.class);
		vcol = new VaultCollection(this);
		
		boolean foundA = getConfig().getConfigurationSection("auctions") != null;
		System.err.println((foundA ? "Did " : "Did not ") + "find auctions in config.");
		if (foundA)
		{
			try
			{
				loadAuctionsDatabase();
				System.out.println("Auctions loaded successfully!");
			} catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidConfigurationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		boolean foundR = getConfig().get("roulette") != null;
		System.err.println((foundR ? "Did " : "Did not ") + "find roulette in config.");
		if (foundR)
		{
			
			try
			{
				loadItemRoulette();
				System.out.println("Roulette loaded successfully!");
			} catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidConfigurationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.err.println("GlobalAuctions loaded!");
	}

	@Override
	public void onDisable()
	{
		try
		{
			auctionsDatabase.save(this);
			itemRoulette.save(this);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadAuctionsDatabase() throws FileNotFoundException, IOException, InvalidConfigurationException
	{
		this.reloadConfig();
		Object object = getConfig().get("auctions");
		System.out.println("auctions " + (getConfig().getConfigurationSection("auctions") != null ? "exist" : "don't exist") + "!");
		this.auctionsDatabase = new AuctionsDatabase(getConfig()
				.getConfigurationSection("auctions")
				.getValues(true));
	}
	
	public void loadItemRoulette() throws FileNotFoundException, IOException, InvalidConfigurationException
	{
		this.reloadConfig();
		ItemStackRoulette roulette = getConfig().getObject("roulette", ItemStackRoulette.class);
		this.itemRoulette = roulette;
		/*
		this.getConfig().load(Paths.get(getConfig().getCurrentPath()).toFile());
		System.out.println("roulette " + (getConfig().getConfigurationSection("roulette") != null ? "exists" : "doesn't exist") + "!");
		this.itemRoulette = ItemStackRoulette.deserialize(getConfig()
				.getConfigurationSection("roulette")
				.getValues(true));
				*/
		
		
	}
	
	public AuctionsDatabase getAuctionsDatabase()
	{
		return auctionsDatabase;
	}
	
	public ItemStackRoulette getItemRoulette()
	{
		return itemRoulette;
	}
	
	public boolean negativePriceAllowed()
	{
		return false;
	}
	
	public String getInventoryTitle()
	{
		return INVENTORY_TITLE;
	}
	
	public boolean getCanBuyOwn()
	{
		return getConfig().getBoolean("can-buy-own");
	}
	
	public int getRandomPriceBound()
	{
		return getConfig().getInt("r-price-bound");
	}
	
	public Optional<Economy> getEconomy()
	{
		return Optional.ofNullable(vcol.getEconomy());
	}
	
	public String getAllAuctionsMessage()
	{
		Map<UUIDS, Map<UUIDS, Auction>> aucs = auctionsDatabase.getAuctions();  
		String str = "";
		for (UUIDS uuids : aucs.keySet())
		{
			str += Bukkit.getOfflinePlayer(uuids.getUUID()).getName() + "\n\n";
			Map<UUIDS, Auction> smp = aucs.get(uuids);
			for (Auction auc : smp.values())
			{
				str += auc.toIngameString(false);
			}
		}
		return str;
	}
}
