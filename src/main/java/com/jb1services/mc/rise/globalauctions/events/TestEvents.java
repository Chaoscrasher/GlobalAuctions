package com.jb1services.mc.rise.globalauctions.events;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.chaoscrasher.events.ChaosEventListener;


public class TestEvents extends ChaosEventListener {

	public TestEvents(JavaPlugin plugin)
	{
		super(plugin);
		// TODO Auto-generated constructor stub
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Block block = event.getBlock();
		event.getPlayer().sendMessage("That block is of type " + block.getType());
	}

	public void onBlockBreak(BlockBreakEvent event)
	{
		Block block = event.getBlock();
		event.getPlayer().sendMessage("That block is of type " + block.getType());
	}
}
