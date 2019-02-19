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
package system.handlers.ai.instance.dragonLordRefuge;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

import javolution.util.FastMap;

/**
 * @author Rinzler (Encom)
 */
@AIName("kahrun2")
public class KahrunAI2 extends NpcAI2
{
	private final FastMap<Integer, VisibleObject> portal = new FastMap<>();
	
	@Override
	protected void handleDialogStart(Player player)
	{
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}
	
	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex)
	{
		final int instanceId = getPosition().getInstanceId();
		if (dialogId == 10000)
		{
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10));
			switch (getNpcId())
			{
				case 800429: // Kahrun (Reian Leader).
				{
					final SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(300520000, 730625, 503.219757f, 516.651733f, 242.604065f, (byte) 0); // Blood Red Jewel.
					template.setEntityId(4);
					portal.put(730625, SpawnEngine.spawnObject(template, instanceId));
					AI2Actions.deleteOwner(KahrunAI2.this);
					break;
				}
			}
		}
		return true;
	}
}