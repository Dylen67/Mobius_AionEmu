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
package system.handlers.ai.instance.steelRake;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;

import system.handlers.ai.ActionItemNpcAI2;

/**
 * @author Rinzler (Encom)
 */
@AIName("accountant_cabin_exit")
public class AccountantCabinExitAI2 extends ActionItemNpcAI2
{
	@Override
	protected void handleUseItemFinish(Player player)
	{
		switch (getNpcId())
		{
			case 730766: // Accountant's Cabin Exit.
			{
				switch (player.getWorldId())
				{
					case 300100000: // Steel Rake 1.5
					{
						if (player.getCommonData().getRace() == Race.ASMODIANS)
						{
							PacketSendUtility.sendMessage(player, "you enter <Pandaemonium>");
							TeleportService2.teleportTo(player, 120010000, 950.000f, 1150.000f, 195.000f, (byte) 60, TeleportAnimation.BEAM_ANIMATION);
						}
						else if (player.getCommonData().getRace() == Race.ELYOS)
						{
							PacketSendUtility.sendMessage(player, "you enter <Sanctum>");
							TeleportService2.teleportTo(player, 110010000, 1867.000f, 2068.000f, 517.000f, (byte) 60, TeleportAnimation.BEAM_ANIMATION);
						}
						break;
					}
					case 300460000: // Steel Rake Cabin 3.0
					{
						if (player.getCommonData().getRace() == Race.ASMODIANS)
						{
							PacketSendUtility.sendMessage(player, "you enter <Pandaemonium>");
							TeleportService2.teleportTo(player, 120010000, 950.000f, 1150.000f, 195.000f, (byte) 60, TeleportAnimation.BEAM_ANIMATION);
						}
						else if (player.getCommonData().getRace() == Race.ELYOS)
						{
							PacketSendUtility.sendMessage(player, "you enter <Sanctum>");
							TeleportService2.teleportTo(player, 110010000, 1867.000f, 2068.000f, 517.000f, (byte) 60, TeleportAnimation.BEAM_ANIMATION);
						}
						break;
					}
				}
				break;
			}
		}
	}
}