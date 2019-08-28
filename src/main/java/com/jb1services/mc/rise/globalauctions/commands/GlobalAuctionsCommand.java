package com.jb1services.mc.rise.globalauctions.commands;

import com.chaoscrasher.commands.ChaosCommandExecutor;
import com.chaoscrasher.commands.arglen.ArgLenFour;
import com.chaoscrasher.commands.arglen.ArgLenOne;
import com.chaoscrasher.commands.arglen.ArgLenThree;
import com.chaoscrasher.commands.arglen.ArgLenTwo;
import com.chaoscrasher.global.ChaosBukkit;
import com.jb1services.mc.rise.globalauctions.main.GlobalAuctionsPlugin;
import com.jb1services.mc.rise.globalauctions.structure.Auction;
import com.jb1services.mc.rise.globalauctions.structure.UUIDS;

import net.md_5.bungee.api.ChatColor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

public class GlobalAuctionsCommand extends ChaosCommandExecutor 
{
	private GlobalAuctionsPlugin plugin;
	
	public GlobalAuctionsCommand(GlobalAuctionsPlugin plugin)
	{
		this.plugin = plugin;
		ArgLenTwo<Boolean, Boolean> cmd1 = new ArgLenTwo<>(Boolean.class, Boolean.class, true, false,
				a0 -> a0.equalsIgnoreCase("show"),
				a1 -> a1.equalsIgnoreCase("my"))
				.defineEffectAB(this::onShowMyAuctions)
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
		
		ArgLenThree<Boolean, Boolean, Double> randomAuctionWithPriceCommand = new ArgLenThree<>(Boolean.class, Boolean.class, Double.class, true, true,
				a0 -> a0.equalsIgnoreCase("make"),
				a1 -> a1.equalsIgnoreCase("rauc"))
				.defineEffectABC(this::onRandomAuctionWithPrice)
				.applyTo(this);
		
		ArgLenTwo<Boolean, Boolean> randomAuctionCommand = new ArgLenTwo<>(Boolean.class, Boolean.class, true, true,
				a0 -> a0.equalsIgnoreCase("make"),
				a1 -> a1.equalsIgnoreCase("rauc"))
				.defineEffectAB(this::onRandomAuction)
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
		
		ArgLenTwo<Boolean, Boolean> rouletteAddCommand = new ArgLenTwo<>(Boolean.class, Boolean.class, true, true,
				a0 -> a0.equalsIgnoreCase("roulette"),
				a1 -> a1.equalsIgnoreCase("add"))
				.defineEffectAB(this::onRouletteAdd)
				.applyTo(this);
		
		ArgLenThree<Boolean, Boolean, Integer> rouletteAddWithWeightCommand = new ArgLenThree<>(Boolean.class, Boolean.class, Integer.class, true, true,
				a0 -> a0.equalsIgnoreCase("roulette"),
				a1 -> a1.equalsIgnoreCase("add"))
				.defineEffectABC(this::onRouletteAddWithWeight)
				.applyTo(this);
		
		ArgLenThree<Boolean, Boolean, Integer> rouletteRemoveCommand = new ArgLenThree<>(Boolean.class, Boolean.class, Integer.class, true, true,
				a0 -> a0.equalsIgnoreCase("roulette"),
				a1 -> a1.equalsIgnoreCase("remove"))
				.defineEffectABC(this::onRouletteRemove)
				.applyTo(this);
		
		ArgLenTwo<Boolean, Boolean> rouletteShowCommand = new ArgLenTwo<>(Boolean.class, Boolean.class, true, true,
				a0 -> a0.equalsIgnoreCase("roulette"),
				a1 -> a1.equalsIgnoreCase("show"))
				.defineEffectAB(this::onRouletteShow)
				.applyTo(this);
		
		ArgLenTwo<Boolean, Boolean> rouletteSwitchCommand = new ArgLenTwo<>(Boolean.class, Boolean.class, true, true,
				a0 -> a0.equalsIgnoreCase("roulette"),
				a1 -> a1.equalsIgnoreCase("switch"))
				.defineEffectAB(this::onRouletteSwitch)
				.applyTo(this);
		
		ArgLenTwo<Boolean, Boolean> listAuctionsCommand = new ArgLenTwo<>(Boolean.class, Boolean.class, true, true,
				a0 -> a0.equalsIgnoreCase("list"),
				a1 -> a1.contains("auc"))
				.defineEffectAB(this::onListAuctions)
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
		player.getInventory().setItemInMainHand(null);
		sendGreen("Auction " + auction.getUuid() + " created!");
	}
	
	public void onShowAuctions(ArgLenTwo<Boolean, String> alen)
	{
		Optional<OfflinePlayer> plo = ChaosBukkit.getOfflinePlayerByName(alen.getDataBNonOptional());
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
					auc.showToPlayer(plugin, player);
				}
				else
					sendGreen("These are the auctions fitting your search!:\n" + createAuctionsString(aucs));
			}
			else
				sendRed("Sorry, but there is no auction that contains '" + alen.getDataBNonOptional() + "'.");
		}
		else
			sendRed("Sorry, but please supply at least 5 characters!");
	}
		
	public void onShowMyAuctions(ArgLenTwo<Boolean, Boolean> alen)
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
			plugin.getItemRoulette().save(plugin);
			sendGreen("Saved roulette!");
		} catch (IOException e)
		{
			sendRed("Saving DB failed!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onLoad(ArgLenOne<Boolean> alen)
	{
		try
		{
			plugin.loadAuctionsDatabase();
			sendGreen("Reloaded DB!");
			plugin.loadItemRoulette();
			sendGreen("Reloaded roulette!");
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
	
	public void onRouletteAdd(ArgLenTwo<Boolean, Boolean> alen)
	{
		if (player.getInventory().getItemInMainHand() != null)
		{
			plugin.getItemRoulette().add(player.getInventory().getItemInMainHand(), 0);
			sendGreen("Item added with weight " + 0 + "!");
		}
		else
			sendRed("You need an item in hand!");
	}
	
	public void onRouletteAddWithWeight(ArgLenThree<Boolean, Boolean, Integer> alen)
	{
		if (alen.getDataCNonOptional() >= 0)
		{
			if (player.getInventory().getItemInMainHand() != null)
			{
				plugin.getItemRoulette().add(player.getInventory().getItemInMainHand(), alen.getDataCNonOptional());
				sendGreen("Item added with weight " + alen.getDataCNonOptional() + "!");
			}
			else
				sendRed("You need an item in hand!");
		}
		else
			sendRed("Please use a weight >= 0!");
	}
	
	public void onRouletteRemove(ArgLenThree<Boolean, Boolean, Integer> alen)
	{
		if (alen.getDataCNonOptional() >= 0)
		{
			Optional<ItemStack> iso = plugin.getItemRoulette().getStack(alen.getDataCNonOptional());
			if (iso.isPresent())
			{
				ItemStack is = iso.get();
				plugin.getItemRoulette().removeStack(alen.getDataCNonOptional());
				sendGreen("ItemStack "+ChatColor.WHITE+alen.getDataCNonOptional()+ChatColor.DARK_GREEN+" (" + is.getType() + " x" + is.getAmount() + ", weight "+ plugin.getItemRoulette().getItemRoulette().get(is) + ") " + ChatColor.GREEN + "was removed from the Item Roulette!");
			}
			else
				sendRed("Sorry, but you only have " + ChatColor.WHITE + plugin.getItemRoulette().getItemRoulette().size() + ChatColor.RED + " items in the item roulette!");
		}
		else
			sendRed("Please use an index >= 0!");
	}
	
	public void onRouletteShow(ArgLenTwo<Boolean, Boolean> alen)
	{
		Map<ItemStack, Integer> ir = plugin.getItemRoulette().getItemRoulette();
		if (!ir.isEmpty())
		{
			for (ItemStack key : ir.keySet())
			{
				sendGold(key + "\nweight: " + ir.get(key));
			}
		}
		else
			player.sendMessage("You don't have any items set-up!");
	}
	
	public void onRandomAuctionWithPrice(ArgLenThree<Boolean, Boolean, Double> alen)
	{
		if (alen.getDataCNonOptional() >= 0)
		{
			Auction auc = new Auction(player.getUniqueId(), plugin.getItemRoulette().decide().get(), alen.getDataCNonOptional());
			plugin.getAuctionsDatabase().addAuction(auc);
			sendGreen("Auction " + ChatColor.WHITE + auc.getUuid() + " is live!");
		}
		else 
			sendRed("Sorry, only positive prices are allowed!");
	}
	
	public void onRandomAuction(ArgLenTwo<Boolean, Boolean> alen)
	{
		Random rnd = new Random();
		int cost = rnd.nextInt(plugin.getRandomPriceBound());
		Auction auc = new Auction(player.getUniqueId(), plugin.getItemRoulette().decide().get(), cost);
		sendGreen("Random Auction " + ChatColor.WHITE + auc.getUuid() + ChatColor.GREEN + " is live!");
	}
	
	public void onRouletteSwitch(ArgLenTwo<Boolean, Boolean> alen)
	{
		plugin.getItemRoulette().switchMode();
		sendGreen("ItemStack is now set up so that " + ChatColor.WHITE + (plugin.getItemRoulette().isRandom() ? "all items have the same probability" : "all items have their own probability") + ChatColor.GREEN + "!");
	}
	
	public void onListAuctions(ArgLenTwo<Boolean, Boolean> alen)
	{
		sender.sendMessage(plugin.getAllAuctionsMessage());
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
}
