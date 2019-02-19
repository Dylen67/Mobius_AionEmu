/*
 * This file is part of the Aion-Emu project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.spawnengine;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Rinzler (Encom)
 */
public class ShugoImperialTombSpawnManager
{
	private static final Logger log = LoggerFactory.getLogger(ShugoImperialTombSpawnManager.class);
	private static final ConcurrentLinkedQueue<VisibleObject> tomb = new ConcurrentLinkedQueue<>();
	
	public void start()
	{
		final String[] times = EventsConfig.IMPERIAL_TOMB_TIMES.split("\\|");
		for (String cron : times)
		{
			CronService.getInstance().schedule(() ->
			{
				for (RiftEnum rift : RiftEnum.values())
				{
					spawnImperialTomb(rift);
				}
			}, cron);
			log.info("Scheduled <Shugo Imperial Tomb 4.3>: based on cron expression: " + cron + " Duration: " + EventsConfig.IMPERIAL_TOMB_TIMER + " in minutes");
		}
	}
	
	static void spawnImperialTomb(RiftEnum rift)
	{
		final SpawnTemplate spawn = SpawnEngine.addNewSpawn(rift.getWorldId(), rift.getNpcId(), rift.getX(), rift.getY(), rift.getZ(), (byte) 0, 0);
		final VisibleObject visibleObject = SpawnEngine.spawnObject(spawn, 1);
		tomb.add(visibleObject);
		scheduleDelete(visibleObject);
		sendAnnounce(visibleObject);
	}
	
	private static void scheduleDelete(VisibleObject visObj)
	{
		ThreadPoolManager.getInstance().schedule(() ->
		{
			if ((visObj != null) && visObj.isSpawned())
			{
				visObj.getController().delete();
				tomb.remove(visObj);
			}
		}, EventsConfig.IMPERIAL_TOMB_TIMER * 60 * 1000);
	}
	
	public static void sendImperialStatus(Player activePlayer)
	{
		for (VisibleObject visObj : tomb)
		{
			if (visObj.getWorldId() == activePlayer.getWorldId())
			{
				sendMessage(activePlayer, visObj.getObjectTemplate().getTemplateId());
			}
		}
	}
	
	public static void sendAnnounce(VisibleObject visObj)
	{
		if (visObj.isSpawned())
		{
			final WorldMapInstance worldInstance = visObj.getPosition().getMapRegion().getParent();
			worldInstance.doOnAllPlayers(player ->
			{
				if (player.isSpawned())
				{
					sendMessage(player, visObj.getObjectTemplate().getTemplateId());
				}
			});
		}
	}
	
	public static void sendMessage(Player player, int npc_id)
	{
		switch (npc_id)
		{
			case 831117:
			{
				PacketSendUtility.sendSys3Message(player, "\uE09B", "<Shugo Imperial Tomb> is now open !!!");
				break;
			}
		}
	}
	
	public enum RiftEnum
	{ // Shugo Imperial Tomb 4.3/4.8
		Indiarunark_Sanctum(831117, 110010000, 1454.038f, 1520.621f, 573.0719f, (byte) 60),
		Indiarunark_Inggison(831117, 210050000, 1358.8662f, 299.00287f, 588.7499f, (byte) 0),
		Indiarunark_Cygnea(831117, 210070000, 2930.079f, 825.9626f, 569.5f, (byte) 71),
		Alberto_Pandaemonium(831131, 120010000, 1584.4727f, 1405.4204f, 193.09547f, (byte) 0),
		Alberto_Gelkmaros(831131, 220070000, 1794.8785f, 2914.2793f, 554.80853f, (byte) 0),
		Alberto_Enshar(831131, 220070000, 471.96454f, 2319.1738f, 216.45724f, (byte) 23);
		
		private final int npc_id;
		private final int worldId;
		private final float x;
		private final float y;
		private final float z;
		private final byte h;
		
		private RiftEnum(int npc_id, int worldId, float x, float y, float z, byte heading)
		{
			this.npc_id = npc_id;
			this.worldId = worldId;
			this.x = x;
			this.y = y;
			this.z = z;
			h = heading;
		}
		
		public int getNpcId()
		{
			return npc_id;
		}
		
		public int getWorldId()
		{
			return worldId;
		}
		
		public float getX()
		{
			return x;
		}
		
		public float getY()
		{
			return y;
		}
		
		public float getZ()
		{
			return z;
		}
		
		public byte getHeading()
		{
			return h;
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ShugoImperialTombSpawnManager instance = new ShugoImperialTombSpawnManager();
	}
	
	public static ShugoImperialTombSpawnManager getInstance()
	{
		return SingletonHolder.instance;
	}
}