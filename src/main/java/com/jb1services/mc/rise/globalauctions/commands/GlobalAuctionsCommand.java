package com.jb1services.mc.rise.globalauctions.commands;

import com.chaoscrasher.commands.ChaosCommandExecutor;
import com.chaoscrasher.commands.arglen.ArgLenFour;
import com.chaoscrasher.commands.arglen.ArgLenOne;
import com.chaoscrasher.commands.arglen.ArgLenThree;
import com.chaoscrasher.commands.arglen.ArgLenTwo;
import com.jb1services.mc.rise.globalauctions.main.GlobalAuctionsPlugin;
import com.jb1services.mc.rise.globalauctions.structure.Auction;
import com.jb1services.mc.rise.globalauctions.structure.UUIDS;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

public class GlobalAuctionsCommand extends ChaosCommandExecutor 
{
	private GlobalAuctionsPlugin plugin;
	
	public GlobalAuctionsCommand(GlobalAuctionsPlugin plugin)
	{
		this.plugin = plugin;
		ArgLenOne<Boolean> cmd1 = new ArgLenOne<>(Boolean.class, true, false,
				a0 -> a0.equalsIgnoreCase("show"))
				.defineEffectA(this::onShowMyAuctions)
				.applyTo(this);
		
		ArgLenTwo<Boolean, String> cmd2 = new ArgLenTwo<>(Boolean.class, String.class, true, false,
				a0 -> a0.equalsIgnoreCase("show"))
				.defineEffectAB(this::onShowAuctions)
				.applyTo(this);
		
		ArgLenTwo<Boolean, Double> cmd3 = new ArgLenTwo<>(Boolean.class, Double.class, true, false,
				a0 -> a0.equalsIgnoreCase("make"))
				.defineEffectAB(this::onNewAuction)
				.applyTo(this);
		
		ArgLenThree<Boolean, Boolean, Double> cmd4 = new ArgLenThree<>(Boolean.class, Boolean.class, Double.class, true, false,
				a0 -> a0.equalsIgnoreCase("make"),
				a1 -> a1.equalsIgnoreCase("ask"))
				.defineEffectABC(this::onNewAsk)
				.applyTo(this);
		
		ArgLenTwo<Boolean, String> cancelCommand = new ArgLenTwo<>(Boolean.class, String.class, true, false,
				a0 -> a0.equalsIgnoreCase("cancel"))
				.defineEffectAB(this::onCancelAuction)
				.applyTo(this);
		
		ArgLenOne<Boolean> saveCommand = new ArgLenOne<>(Boolean.class, false, true,
				a0 -> a0.equalsIgnoreCase("save"))
				.defineEffectA(this::onSave)
				.applyTo(this);
		
		ArgLenOne<Boolean> loadCommand = new ArgLenOne<>(Boolean.class, false, true,
				a0 -> a0.equalsIgnoreCase("load"))
				.defineEffectA(this::onLoad)
				.applyTo(this);
	}
	
	public void onNewAuction(ArgLenTwo<Boolean, Double> alen)
	{
		double price = alen.getDataBNonOptional();
		if (plugin.negativePriceAllowed() || price >= 0)
		{
			Auction auction = new Auction(player.getUniqueId(), player.getItemInHand(), price, false);
			finishAuction(auction);
		}
		else
			sendRed("Sorry, but a negative price is not allowed!");
	}
	
	public void onNewAsk(ArgLenThree<Boolean, Boolean, Double> alen)
	{
		double price = alen.getDataCNonOptional();
		if (plugin.negativePriceAllowed() || price >= 0)
		{
			Auction auction = new Auction(player.getUniqueId(), player.getItemInHand(), price, true);
			finishAuction(auction);
		}
		else
			sendRed("Sorry, but a negative price is not allowed!");
	}
	
	public void finishAuction(Auction auction)
	{
		plugin.getAuctionsDatabase().addAuction(auction);
		player.setItemInHand(null);
		sendGreen("Auction " + auction.getUuid() + " created!");
	}
	
	public void onShowAuctions(ArgLenTwo<Boolean, String> alen)
	{
		Optional<OfflinePlayer> plo = getOfflinePlayerByName(alen.getDataBNonOptional());
		if (plo.isPresent())
		{
			OfflinePlayer op = plo.get();
			Map<UUIDS, Auction> auctions = plugin.getAuctionsDatabase().getAuctions(op.getUniqueId());
			if (!auctions.isEmpty())
			{
				String str = "";
				for (Auction auc : auctions.values())
				{
					str += auc.getUuid() + ":\n" + auc.getAuctionedItem().getType() + " x" + auc.getAuctionedItem().getAmount() + " for " + auc.getPrice() + "\n\n";
				}
				sendGreen("These are '"+op.getName()+"'s auctions!\n" + str);
			}
			else
				sendRed("Sorry, but '"+op.getName()+"' doesn't have any auctions!");
		}
		else if (alen.getDataBNonOptional().length() >= 5)
		{
			Map<UUIDS, Auction> aucs = plugin.getAuctionsDatabase().getAuctions(alen.getDataBNonOptional());
			if (!aucs.isEmpty())
			{
				if (aucs.size() == 1)
				{
					Auction auc = aucs.values().iterator().next();
					sendGreen("Showing you auction " + auc.getUuid());
					auc.showToPlayer(player);
				}
				else
					sendGreen("These are the auctions fitting your search!:\n" + createAuctionsString(aucs));
			}
			else
				sendRed("Sorry, but there are either more than one or no auction that contain '" + alen.getDataBNonOptional());
		}
		else
			sendRed("Sorry, but please supply at least 5 characters!");
	}
	
	public void onShowMyAuctions(ArgLenOne<Boolean> alen)
	{
		Map<UUIDS, Auction> auctions = plugin.getAuctionsDatabase().getAuctions(player.getUniqueId());
		if (!auctions.isEmpty())
		{
			sendGreen("These are your auctions!\n" + createAuctionsString(auctions));
		}
		else
			sendRed("Sorry, but you don't have any auctions!");
	}
	
	public void onCancelAuction(ArgLenTwo<Boolean, String> alen)
	{
		List<Auction> aucs = plugin.getAuctionsDatabase().getAuctions(alen.getDataBNonOptional(), player.getUniqueId());
		if (!aucs.isEmpty())
		{
			if (aucs.size() == 1)
			{
				Auction auc = aucs.get(0);
				plugin.getAuctionsDatabase().removeAuction(auc);
			}
			else
				sendRed("Sorry, but there is more than one auction with an ID that starts with '" + alen.getDataBNonOptional() + "'!");
		}
		else
			sendRed("Sorry, but you don't have any auctions!");
	}
	
	public void onSave(ArgLenOne<Boolean> alen)
	{
		try
		{
			plugin.getAuctionsDatabase().save(plugin);
			sendGreen("Saved DB!");
		} catch (IOException e)
		{
			sendRed("Saving DB failed!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onLoad(ArgLenOne<Boolean> alen)
	{
		plugin.loadAuctionsDatabase();
		sendGreen("Reloaded DB!");
	}

	@Override
	public String getCommandShorthand()
	{
		return "gas";
	}
	
	private String createAuctionsString(Map<UUIDS, Auction> auctions)
	{
		return createAuctionsString(auctions.values());
	}
	
	private String createAuctionsString(Collection<Auction> auctions)
	{
		String str = "";
		for (Auction auc : auctions)
		{
			str += auc.getUuid() + ":\n" + auc.getAuctionedItem().getType() + " x" + auc.getAuctionedItem().getAmount() + " for " + auc.getPrice() + "\n\n";
		}
		return str;
	}
	
	public Optional<OfflinePlayer> getOfflinePlayerByName(String name)
	{
		for (OfflinePlayer op : Bukkit.getOfflinePlayers())
		{
			if (op.getName().equals(name))
				return Optional.of(op);
		}
		return Optional.empty();
	}
}
