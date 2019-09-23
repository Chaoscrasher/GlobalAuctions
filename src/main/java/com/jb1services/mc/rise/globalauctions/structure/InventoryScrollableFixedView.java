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

import com.chaoscrasher.inventory.InventoryIcon;
import com.chaoscrasher.inventory.InventoryTitleFiller;

public abstract class InventoryScrollableFixedView extends Inventoryable
{
	private static final String TITLE_PAGE_SEPERATOR = ": ";
	
	private final String baseTitle;
	protected final InventoryTitleFiller filler;
	private final InventoryIcon forwardIcon;
	private final InventoryIcon backwardIcon;
	
	protected Function<InventoryClickEvent, List<? extends ItemStackable>> getMyStackz;
	
	public InventoryScrollableFixedView(String baseTitle, ChatColor pageExtensionChatColor, InventoryIcon forwardIcon, InventoryIcon backwardIcon, Function<InventoryClickEvent, List<? extends ItemStackable>> getMyStackz)
	{
		this.baseTitle = baseTitle;
		this.forwardIcon = forwardIcon;
		this.backwardIcon = backwardIcon;
		this.filler = new InventoryTitleFiller(baseTitle + TITLE_PAGE_SEPERATOR + "page {PAGE}");
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
		if (page >= 0)
		{
			List<ItemStack> stackz = getStacks();
			int sslot = page*54;
			if (stackz.size() > sslot)
			{
				int overallItemCount = stackz.size() - sslot;
				boolean nextButton = overallItemCount > 54;
				boolean previousButton = sslot > 53;
				int buttonSlots = (nextButton ? 1 : 0) + (previousButton ? 1 : 0); 
				int minInvSize = (int) (9 * Math.ceil(overallItemCount / 9.0));
				minInvSize = minInvSize > MAX_PAGE_SIZE ? MAX_PAGE_SIZE : minInvSize;
				Inventory inv = Bukkit.createInventory(getHolder(), minInvSize, getFullTitle(page));

				int i = 0;
				if (previousButton)
				{
					inv.setItem(i, backwardIcon.getSymbol());
					i++;
				}
				
				while (i < minInvSize - buttonSlots && i < overallItemCount)
				{
					inv.setItem(i, stackz.get(sslot+i));
					i++;
				}
				
				if (nextButton)
				{
					inv.setItem(i, forwardIcon.getSymbol());
					i++;
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
			int page = Integer.valueOf(filler.getParsedValueUS(0, e.getView().getTitle()));
			return toInventory(page+1);
		}
		else if (isBackwardClick(e))
		{
			int page = Integer.valueOf(filler.getParsedValueUS(0, e.getView().getTitle()));
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
