package com.jb1services.mc.rise.globalauctions.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.chaoscrasher.inventory.InventoryFiller;
import com.chaoscrasher.inventory.InventoryIcon;
import com.jb1services.mc.rise.globalauctions.main.GlobalAuctionsPlugin;

public abstract class InventoryScrollableFixedClickable<PLUGIN> extends InventoryScrollableFixedView
{	
	public InventoryScrollableFixedClickable(String baseTitle, ChatColor pageExtensionChatColor,
			InventoryIcon forwardIcon, InventoryIcon backwardIcon,
			Function<InventoryClickEvent, List<? extends ItemStackable>> getMyStackz) {
		super(baseTitle, pageExtensionChatColor, forwardIcon, backwardIcon, getMyStackz);
		// TODO Auto-generated constructor stub
	}

	public InventoryScrollableFixedClickable(String baseTitle, InventoryIcon forwardIcon, InventoryIcon backwardIcon,
			Function<InventoryClickEvent, List<? extends ItemStackable>> getMyStackz) {
		super(baseTitle, forwardIcon, backwardIcon, getMyStackz);
		// TODO Auto-generated constructor stub
	}
	
	protected InventoryScrollableFixedClickable(String baseTitle, InventoryIcon forwardIcon, InventoryIcon backwardIcon) {
		this(baseTitle, forwardIcon, backwardIcon, null);
	}

	public Optional<Inventory> onClick(PLUGIN plugin, InventoryClickEvent e)
	{
		Optional<Inventory> invo = super.onClick(e);
		if (invo.isPresent())
		{
			return invo;
		}
		else
		{
			int slot = e.getRawSlot();
			ItemStackable is = this.getMyStackz.apply(e).get(slot);
			if (is != null && is instanceof InventoryClickable)
			{
				InventoryClickable<PLUGIN> ic = (InventoryClickable<PLUGIN>) is;
				return ic.onClick(plugin, e);
			}
		}
		return Optional.empty();
	}
}
