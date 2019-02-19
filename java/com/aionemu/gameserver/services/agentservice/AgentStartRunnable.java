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
package com.aionemu.gameserver.services.agentservice;

import java.util.Map;

import com.aionemu.gameserver.model.agent.AgentLocation;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AgentService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Rinzler (Encom)
 */

public class AgentStartRunnable implements Runnable
{
	final int id;
	
	public AgentStartRunnable(int id)
	{
		this.id = id;
	}
	
	@Override
	public void run()
	{
		// The Agent battle will start in 10 minutes.
		AgentService.getInstance().agentBattleMsg1(id);
		ThreadPoolManager.getInstance().schedule(() -> AgentService.getInstance().agentBattleMsg2(id), 300000);
		ThreadPoolManager.getInstance().schedule(() ->
		{
			final Map<Integer, AgentLocation> locations = AgentService.getInstance().getAgentLocations();
			for (AgentLocation loc : locations.values())
			{
				if (loc.getId() == id)
				{
					AgentService.getInstance().startAgentFight(loc.getId());
				}
			}
			World.getInstance().doOnAllPlayers(player -> PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LDF4_Advance_GodElite));
		}, 600000);
	}
}