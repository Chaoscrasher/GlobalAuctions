package com.jb1services.mc.rise.globalauctions.structure;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.chaoscrasher.inventory.InventoryIcon;

public class ClickableInventoryIcon<PLUGIN> extends InventoryIcon implements InventoryClickable<PLUGIN>
{
	private BiFunction<PLUGIN, InventoryClickEvent, Optional<Inventory>> onClick;
	
	public ClickableInventoryIcon(ItemStack baseItem, String name, List<String> lore, BiFunction<PLUGIN, InventoryClickEvent, Optional<Inventory>> onClick) {
		super(baseItem, name, lore);
		this.onClick = onClick;
	}

	public ClickableInventoryIcon(ItemStack baseItem, String name, BiFunction<PLUGIN, InventoryClickEvent, Optional<Inventory>> onClick) {
		super(baseItem, name);
		this.onClick = onClick;
	}

	public ClickableInventoryIcon(Material baseMaterial, List<String> lore, BiFunction<PLUGIN, InventoryClickEvent, Optional<Inventory>> onClick) {
		super(baseMaterial, lore);
		this.onClick = onClick;
	}

	public ClickableInventoryIcon(Material baseMaterial, String name, List<String> lore, BiFunction<PLUGIN, InventoryClickEvent, Optional<Inventory>> onClick) {
		super(baseMaterial, name, lore);
		this.onClick = onClick;
	}

	public ClickableInventoryIcon(Material baseMaterial, String name, BiFunction<PLUGIN, InventoryClickEvent, Optional<Inventory>> onClick) {
		super(baseMaterial, name);
		this.onClick = onClick;
	}

	public ClickableInventoryIcon(ItemStack baseItem, List<String> lore, BiFunction<PLUGIN, InventoryClickEvent, Optional<Inventory>> onClick) {
		super(baseItem, lore);
		this.onClick = onClick;
	}

	@Override
	public ItemStack stack() 
	{
		return super.getSymbol();
	}

	@Override
	public Optional<Inventory> onClick(PLUGIN t, InventoryClickEvent p) 
	{
		return onClick.apply(t, p);
	}

}
