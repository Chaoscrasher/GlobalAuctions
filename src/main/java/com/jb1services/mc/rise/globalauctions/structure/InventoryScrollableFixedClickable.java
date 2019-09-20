package com.jb1services.mc.rise.globalauctions.structure;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import com.chaoscrasher.inventory.InventoryIcon;

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
			List<? extends ItemStackable> list = this.getMyStackz.apply(e);
			if (list.size() > slot)
			{
				ItemStackable is = list.get(slot);
				if (is != null && is instanceof InventoryClickable)
				{
					InventoryClickable<PLUGIN> ic = (InventoryClickable<PLUGIN>) is;
					return ic.onClick(plugin, e);
				}
			}
		}
		return Optional.empty();
	}
}
