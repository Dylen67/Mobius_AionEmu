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
package system.handlers.ai.worlds.theobomos;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;

import system.handlers.ai.ActionItemNpcAI2;

/**
 * @author Rinzler (Encom)
 */
@AIName("dried_out_vine")
public class Dried_Out_VineAI2 extends ActionItemNpcAI2
{
	@Override
	protected void handleUseItemFinish(Player player)
	{
		if (player.getLevel() >= 45)
		{
			switch (getNpcId())
			{
				case 730169: // Dried-Out Vine.
				{
					TeleportService2.teleportTo(player, 210060000, 2456.042f, 2388.615f, 32.53795f, (byte) 31, TeleportAnimation.BEAM_ANIMATION);
					break;
				}
				case 730170: // Dried-Out Vine.
				{
					TeleportService2.teleportTo(player, 210060000, 2841.572f, 2500.023f, 40.20959f, (byte) 39, TeleportAnimation.BEAM_ANIMATION);
					break;
				}
				case 730171: // Dried-Out Vine.
				{
					TeleportService2.teleportTo(player, 210060000, 1971.371f, 2676.447f, 61.50000f, (byte) 49, TeleportAnimation.BEAM_ANIMATION);
					break;
				}
				case 730172: // Dried-Out Vine.
				{
					TeleportService2.teleportTo(player, 210060000, 2254.681f, 2839.995f, 58.37074f, (byte) 46, TeleportAnimation.BEAM_ANIMATION);
					break;
				}
				case 730173: // Dried-Out Vine.
				{
					TeleportService2.teleportTo(player, 210060000, 2674.758f, 2947.456f, 37.47572f, (byte) 44, TeleportAnimation.BEAM_ANIMATION);
					break;
				}
			}
		}
		else
		{
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 27));
		}
	}
}