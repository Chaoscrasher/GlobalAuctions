package com.jb1services.mc.rise.globalauctions.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class UUIDS implements ConfigurationSerializable
{
	private UUID uuid;

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<>();
		map.put("uuid", uuid.toString());
		return map;
	}
	
	public UUIDS(Map<String, Object> map)
	{
		this.uuid = UUID.fromString((String) map.get("uuid"));
	}
	
	public UUIDS(UUID uuid)
	{
		this.uuid = uuid;
	}
	
	public UUID getUUID() 
	{
		return uuid;
	}
	
	public static UUIDS randomUUIDS()
	{
		return new UUIDS(UUID.randomUUID());
	}
	
	public static UUIDS fromString(String str)
	{
		return new UUIDS(UUID.fromString(str));
	}
	
	public static UUIDS of(UUID uuid)
	{
		return new UUIDS(uuid);
	}
	
	@Override
	public String toString()
	{
		return uuid.toString();
	}
	
	@Override
	public int hashCode()
	{
		return uuid.hashCode();
	}
	
	@Override
	public boolean equals(Object that)
	{
		if (that != null)
		{
			if (that.getClass().equals(this.getClass()))
			{
				UUIDS to = (UUIDS) that;
				if (to.uuid == this.uuid || to.uuid.equals(this.uuid))
					return true;
			}
		}
		return false;
	}
}
