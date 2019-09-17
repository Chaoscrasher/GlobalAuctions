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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.chaoscrasher.inventory.InventoryFiller;
import com.chaoscrasher.inventory.InventoryIcon;

public abstract class InventoryScrollableFixedView extends Inventoryable
{
	private static final String TITLE_PAGE_SEPERATOR = ": ";
	
	private final String baseTitle;
	protected final InventoryFiller filler;
	private final InventoryIcon forwardIcon;
	private final InventoryIcon backwardIcon;
	
	protected Function<InventoryClickEvent, List<? extends ItemStackable>> getMyStackz;
	
	public InventoryScrollableFixedView(String baseTitle, ChatColor pageExtensionChatColor, InventoryIcon forwardIcon, InventoryIcon backwardIcon, Function<InventoryClickEvent, List<? extends ItemStackable>> getMyStackz)
	{
		this.baseTitle = baseTitle;
		this.forwardIcon = forwardIcon;
		this.backwardIcon = backwardIcon;
		this.filler = new InventoryFiller(baseTitle + TITLE_PAGE_SEPERATOR + "page {PAGE}");
		this.getMyStackz = getMyStackz;
	}
	
	public InventoryScrollableFixedView(String baseTitle, InventoryIcon forwardIcon, InventoryIcon backwardIcon, Function<InventoryClickEvent, List<? extends ItemStackable>> getMyStackz)
	{
		this(baseTitle, ChatColor.WHITE, forwardIcon, backwardIcon, getMyStackz);
	}
	
	public String getFullTitle(int page)
	{
		return filler.makeFilledOutUS(page+"");
	}
	
	public int getCurrentPage(InventoryView e)
	{
		return Integer.valueOf(filler.getParsedValueUS(0, e.getTitle()));
	}
	
	/*
	List<ItemStack> getStacksInternal(InventoryClickEvent e) 
	{
		return getMyStackz.apply(e).stream().map(sable -> sable.stack()).collect(Collectors.toList());
	}
	*/
	
	public Optional<Inventory> toInventory(int page)
	{
		System.out.println("KEK");
		if (page >= 0)
		{
			List<ItemStack> stackz = getStacks();
			int sslot = page*54;
			if (stackz.size() > sslot)
			{
				int totalSlots = stackz.size() - sslot;
				int buttonSlots = (totalSlots > 54 ? 1 : 0) + (sslot > 53 ? 1 : 0); 
				int actualSize = (int) (9 * Math.ceil(totalSlots / 9.0));
				actualSize = actualSize > MAX_PAGE_SIZE ? MAX_PAGE_SIZE : actualSize;
				Inventory inv = Bukkit.createInventory(getHolder(), actualSize, getFullTitle(page));
				
				int i = 0;
				while (i < actualSize - buttonSlots)
				{
					inv.setItem(i, stackz.get(sslot+i));
					i++;
				}
				
				for (int j = 0; j < buttonSlots;  j++)
				{
					inv.setItem(i + j, stackz.get(sslot + i + j));
				}
				
				return Optional.of(inv);
			}
		}
		return Optional.empty();
	}

	@Override
	Optional<InventoryIcon> getForwardIcon() 
	{
		return Optional.of(forwardIcon);
	}

	@Override
	Optional<InventoryIcon> getBackwardIcon() {
		return Optional.of(backwardIcon);
	}
	
	public Optional<Inventory> onClick(InventoryClickEvent e)
	{
		if (isForwardClick(e))
		{
			int page = Integer.valueOf(filler.getParsedValueUS(0));
			return toInventory(page+1);
		}
		else if (isBackwardClick(e))
		{
			int page = Integer.valueOf(filler.getParsedValueUS(0));
			return toInventory(page-1);
		}
		return Optional.empty();
	}

	@Override
	public boolean detect(InventoryClickEvent e) 
	{
		return filler.matches(e.getView().getTitle());
	}

	@Override
	public boolean isForwardClick(InventoryClickEvent e) 
	{
		ItemStack item = e.getCurrentItem();
		return detect(e) && item != null && item.equals(forwardIcon.getSymbol());
	}

	@Override
	public boolean isBackwardClick(InventoryClickEvent e) 
	{
		ItemStack item = e.getCurrentItem();
		return detect(e) && item != null && item.equals(backwardIcon.getSymbol());
	}

	@Override
	String getTitle() 
	{
		return baseTitle;
	}

	@Override
	InventoryHolder getHolder() 
	{
		return null;
	}
}
