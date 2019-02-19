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
package system.handlers.admincommands;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ginho1
 */
public class Assault extends AdminCommand
{
	public Assault()
	{
		super("assault");
	}
	
	@Override
	public void execute(Player admin, String... params)
	{
		if ((params.length > 4) || (params.length < 3))
		{
			onFail(admin, null);
			return;
		}
		int radius;
		int amount;
		int despawnTime = 0;
		try
		{
			radius = Math.abs(Integer.parseInt(params[0]));
			amount = Integer.parseInt(params[1]);
			if (params.length == 4)
			{
				despawnTime = Math.abs(Integer.parseInt(params[3]));
			}
		}
		catch (NumberFormatException e)
		{
			PacketSendUtility.sendMessage(admin, "You should only input integers as radius, amount and despawn time.");
			return;
		}
		
		if (radius > 100)
		{
			PacketSendUtility.sendMessage(admin, "Radius can't be higher than 100.");
			return;
		}
		
		if ((amount < 1) || (amount > 100))
		{
			PacketSendUtility.sendMessage(admin, "Amount should be between 1-100.");
			return;
		}
		
		if (despawnTime > (60 * 60))
		{
			PacketSendUtility.sendMessage(admin, "You can't have a despawn time longer than 1hr.");
			return;
		}
		
		final List<Integer> idList = new ArrayList<>();
		if ((params[2]).equals("tier20"))
		{
			idList.add(210799);
			idList.add(211961);
			idList.add(213831);
			idList.add(253739);
			idList.add(210566);
			idList.add(210745);
		}
		else if (params[2].equals("tier30"))
		{
			idList.add(210997);
			idList.add(213831);
			idList.add(213547);
			idList.add(253739);
			idList.add(210942);
			idList.add(212631);
		}
		else if (params[2].equals("balaur4"))
		{
			idList.add(210997);
			idList.add(255704);
			idList.add(211962);
			idList.add(213240);
			idList.add(214387);
			idList.add(213547);
		}
		else if (params[2].equals("balaur5"))
		{
			idList.add(250187);
			idList.add(250187);
			idList.add(250187);
			idList.add(250182);
			idList.add(250182);
			idList.add(250182);
			idList.add(250187);
		}
		else if (params[2].equals("dredgion"))
		{
			idList.add(258236);
			idList.add(258238);
			idList.add(258243);
			idList.add(258241);
			idList.add(258237);
			idList.add(258240);
			idList.add(258239);
			idList.add(258242);
			idList.add(250187);
			idList.add(250182);
		}
		else
		{
			for (String npcId : params[2].split(","))
			{
				try
				{
					idList.add(Integer.parseInt(npcId));
				}
				catch (NumberFormatException e)
				{
					PacketSendUtility.sendMessage(admin, "You should only input integers as NPC ids.");
					return;
				}
			}
			if (idList.size() == 0)
			{
				return;
			}
		}
		
		Creature target;
		if (admin.getTarget() != null)
		{
			target = (Creature) admin.getTarget();
		}
		else
		{
			target = admin;
		}
		
		final float x = target.getX();
		final float y = target.getY();
		final float z = target.getZ();
		final byte heading = target.getHeading();
		final int worldId = target.getWorldId();
		
		int templateId;
		SpawnTemplate spawn = null;
		
		final float interval = (float) ((Math.PI * 2.0f) / amount);
		float x1;
		float y1;
		int spawnCount = 0;
		
		VisibleObject visibleObject;
		final List<VisibleObject> despawnList = new ArrayList<>();// will hold the list of spawned mobs
		
		for (int i = 0; amount > i; i++)
		{
			templateId = idList.get((int) (Math.random() * idList.size()));
			x1 = (float) (Math.cos(interval * i) * radius);
			y1 = (float) (Math.sin(interval * i) * radius);
			spawn = SpawnEngine.addNewSpawn(worldId, templateId, x + x1, y + y1, z, heading, 0);
			
			if (spawn == null)
			{
				PacketSendUtility.sendMessage(admin, "There is no npc: " + templateId);
				return;
			}
			visibleObject = SpawnEngine.spawnObject(spawn, 1);
			if (despawnTime > 0)
			{
				despawnList.add(visibleObject);
			}
			
			spawnCount++;
		}
		
		if (despawnTime > 0)
		{
			PacketSendUtility.sendMessage(admin, "Despawn time active: " + despawnTime + "sec");
			despawnThem(admin, despawnList, despawnTime);
		}
		
		PacketSendUtility.sendMessage(admin, spawnCount + " npc have been spawned.");
	}
	
	private void despawnThem(Player admin, List<VisibleObject> despawnList, int despawnTime)
	{
		ThreadPoolManager.getInstance().schedule(() ->
		{
			int despawnCount = 0;
			for (VisibleObject visObj : despawnList)
			{
				if ((visObj != null) && visObj.isSpawned())
				{
					visObj.getController().delete();
					despawnCount++;
				}
			}
			PacketSendUtility.sendMessage(admin, despawnCount + " npc have been deleted.");
		}, despawnTime * 1000);
	}
	
	@Override
	public void onFail(Player player, String message)
	{
		final String syntax = "Syntax: //assault <radius> <amount> <npc_id1, npc_id2,...| tier20 | tier30 |...> <despawn time in secs>";
		PacketSendUtility.sendMessage(player, syntax);
	}
}