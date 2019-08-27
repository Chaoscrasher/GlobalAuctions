package com.jb1services.mc.rise.globalauctions.main;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.jb1services.mc.rise.globalauctions.commands.GlobalAuctionsCommand;
import com.jb1services.mc.rise.globalauctions.events.TestEvents;
import com.jb1services.mc.rise.globalauctions.structure.Auction;
import com.jb1services.mc.rise.globalauctions.structure.AuctionsDatabase;
import com.jb1services.mc.rise.globalauctions.structure.UUIDS;

public class GlobalAuctionsPlugin extends JavaPlugin {

	public static GlobalAuctionsPlugin instance;
	
	private AuctionsDatabase auctionsDatabase = new AuctionsDatabase();

	@Override
	public void onEnable()
	{
		instance = this;
		new GlobalAuctionsCommand(this);
		new TestEvents(this);
		this.saveDefaultConfig();
		ConfigurationSerialization.registerClass(AuctionsDatabase.class);
		ConfigurationSerialization.registerClass(Auction.class);
		ConfigurationSerialization.registerClass(UUIDS.class);
		/*
		if (getConfig().getConfigurationSection("auctions") != null)
		{
			System.err.println((getConfig().getConfigurationSection("auctions") != null ? "Did " : "Did not ") + "find auctions in config. Trying to load!");
			loadAuctionsDatabase();
		}
		*/
		System.out.println("GlobalAuctions loaded!");
	}

	@Override
	public void onDisable()
	{
		/*
		try
		{
			auctionsDatabase.save(this);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	
	public void loadAuctionsDatabase()
	{
		this.reloadConfig();
		System.out.println("auctions " + (getConfig().getConfigurationSection("auctions") != null ? "exist" : "don't exist") + "!");
		this.auctionsDatabase = new AuctionsDatabase(getConfig()
				.getConfigurationSection("auctions")
				.getValues(true));
	}
	
	public AuctionsDatabase getAuctionsDatabase()
	{
		return auctionsDatabase;
	}
	
	public boolean negativePriceAllowed()
	{
		return false;
	}
}
