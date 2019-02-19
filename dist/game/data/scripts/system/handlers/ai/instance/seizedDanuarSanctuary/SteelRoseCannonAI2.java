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
package system.handlers.ai.instance.seizedDanuarSanctuary;

import java.util.List;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import system.handlers.ai.ActionItemNpcAI2;

/**
 * @author Rinzler (Encom)
 */
@AIName("steelrosecannon2")
public class SteelRoseCannonAI2 extends ActionItemNpcAI2
{
	@Override
	protected void handleUseItemFinish(Player player)
	{
		if (!player.getInventory().decreaseByItemId(186000254, 1))
		{
			PacketSendUtility.broadcastPacket(player, new SM_MESSAGE(player, "You must have <Seal Breaking Magic Cannonball>", ChatType.BRIGHT_YELLOW_CENTER), true);
			return;
		}
		final WorldPosition worldPosition = player.getPosition();
		if (worldPosition.isInstanceMap())
		{
			// Seized Danuar Sanctuary 4.8
			if (worldPosition.getMapId() == 301140000)
			{
				// A heavy door has opened somewhere.
				PacketSendUtility.npcSendPacketTime(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5_Under_02_Canon, 5000);
				SkillEngine.getInstance().getSkill(getOwner(), 21126, 60, getOwner()).useNoAnimationSkill(); // Destroy Seal.
				ThreadPoolManager.getInstance().schedule(() -> despawnNpc(233142), 5000);
			}
			// Danuar Sanctuary 4.8
			else if (worldPosition.getMapId() == 301380000)
			{
				// A heavy door has opened somewhere.
				PacketSendUtility.npcSendPacketTime(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5_Under_02_Canon, 5000);
				SkillEngine.getInstance().getSkill(getOwner(), 21126, 60, getOwner()).useNoAnimationSkill(); // Destroy Seal.
				ThreadPoolManager.getInstance().schedule(() -> despawnNpc(233142), 5000);
			}
		}
	}
	
	void despawnNpc(int npcId)
	{
		if (getPosition().getWorldMapInstance().getNpcs(npcId) != null)
		{
			final List<Npc> npcs = getPosition().getWorldMapInstance().getNpcs(npcId);
			for (Npc npc : npcs)
			{
				npc.getController().onDelete();
			}
		}
	}
}