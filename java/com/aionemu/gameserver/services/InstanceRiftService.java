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
package com.aionemu.gameserver.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.shedule.InstanceSchedule;
import com.aionemu.gameserver.configs.shedule.InstanceSchedule.Instance;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.instancerift.InstanceRiftLocation;
import com.aionemu.gameserver.model.instancerift.InstanceRiftStateType;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.instanceriftspawns.InstanceRiftSpawnTemplate;
import com.aionemu.gameserver.services.instanceriftservice.InstanceStartRunnable;
import com.aionemu.gameserver.services.instanceriftservice.Rift;
import com.aionemu.gameserver.services.instanceriftservice.RiftInstance;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

import javolution.util.FastMap;

/**
 * @author Rinzler (Encom)
 */

public class InstanceRiftService
{
	private InstanceSchedule instanceSchedule;
	private Map<Integer, InstanceRiftLocation> instanceRift;
	private static final int duration = CustomConfig.INSTANCE_RIFT_DURATION;
	private final Map<Integer, RiftInstance<?>> activeInstanceRift = new FastMap<Integer, RiftInstance<?>>().shared();
	
	public void initInstanceLocations()
	{
		if (CustomConfig.INSTANCE_RIFT_ENABLED)
		{
			instanceRift = DataManager.INSTANCE_RIFT_DATA.getInstanceRiftLocations();
			for (InstanceRiftLocation loc : getInstanceRiftLocations().values())
			{
				spawn(loc, InstanceRiftStateType.CLOSED);
			}
		}
		else
		{
			instanceRift = Collections.emptyMap();
		}
	}
	
	public void initInstance()
	{
		if (CustomConfig.INSTANCE_RIFT_ENABLED)
		{
			instanceSchedule = InstanceSchedule.load();
			for (Instance instance : instanceSchedule.getInstancesList())
			{
				for (String instanceTime : instance.getInstanceTimes())
				{
					CronService.getInstance().schedule(new InstanceStartRunnable(instance.getId()), instanceTime);
				}
			}
		}
	}
	
	public void startInstanceRift(int id)
	{
		final RiftInstance<?> rift;
		synchronized (this)
		{
			if (activeInstanceRift.containsKey(id))
			{
				return;
			}
			rift = new Rift(instanceRift.get(id));
			activeInstanceRift.put(id, rift);
		}
		rift.start();
		instanceRiftMsg(id);
		ThreadPoolManager.getInstance().schedule(() -> stopInstanceRift(id), duration * 3600 * 1000);
	}
	
	public void stopInstanceRift(int id)
	{
		if (!isInstanceRiftInProgress(id))
		{
			return;
		}
		RiftInstance<?> rift;
		synchronized (this)
		{
			rift = activeInstanceRift.remove(id);
		}
		if ((rift == null) || rift.isClosed())
		{
			return;
		}
		rift.stop();
	}
	
	public void spawn(InstanceRiftLocation loc, InstanceRiftStateType estate)
	{
		if (estate.equals(InstanceRiftStateType.OPEN))
		{
		}
		final List<SpawnGroup2> locSpawns = DataManager.SPAWNS_DATA2.getInstanceRiftSpawnsByLocId(loc.getId());
		for (SpawnGroup2 group : locSpawns)
		{
			for (SpawnTemplate st : group.getSpawnTemplates())
			{
				final InstanceRiftSpawnTemplate instanceRifttemplate = (InstanceRiftSpawnTemplate) st;
				if (instanceRifttemplate.getEStateType().equals(estate))
				{
					loc.getSpawned().add(SpawnEngine.spawnObject(instanceRifttemplate, 1));
				}
			}
		}
	}
	
	public boolean instanceRiftMsg(int id)
	{
		switch (id)
		{
			case 1:
			{
				World.getInstance().doOnAllPlayers(player -> PacketSendUtility.sendSys3Message(player, "\uE04C", "<Instance Rift> is now open !!!"));
				return true;
			}
			default:
			{
				return false;
			}
		}
	}
	
	public void despawn(InstanceRiftLocation loc)
	{
		for (VisibleObject npc : loc.getSpawned())
		{
			((Npc) npc).getController().cancelTask(TaskId.RESPAWN);
			npc.getController().onDelete();
		}
		loc.getSpawned().clear();
	}
	
	public boolean isInstanceRiftInProgress(int id)
	{
		return activeInstanceRift.containsKey(id);
	}
	
	public Map<Integer, RiftInstance<?>> getActiveInstanceRift()
	{
		return activeInstanceRift;
	}
	
	public int getDuration()
	{
		return duration;
	}
	
	public InstanceRiftLocation getInstanceRiftLocation(int id)
	{
		return instanceRift.get(id);
	}
	
	public Map<Integer, InstanceRiftLocation> getInstanceRiftLocations()
	{
		return instanceRift;
	}
	
	public static InstanceRiftService getInstance()
	{
		return InstanceRiftServiceHolder.INSTANCE;
	}
	
	private static class InstanceRiftServiceHolder
	{
		static final InstanceRiftService INSTANCE = new InstanceRiftService();
	}
}