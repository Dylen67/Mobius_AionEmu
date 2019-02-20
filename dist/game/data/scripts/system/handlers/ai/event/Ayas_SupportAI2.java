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
package system.handlers.ai.event;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.controllers.effect.PlayerEffectController;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

import system.handlers.ai.GeneralNpcAI2;

/**
 * @author Rinzler (Encom)
 */
@AIName("ayas_support")
public class Ayas_SupportAI2 extends GeneralNpcAI2
{
	@Override
	protected void handleDialogStart(Player player)
	{
		switch (getNpcId())
		{
			// Elyos.
			case 833671: // Helpful Ayas.
			case 833672: // Friendly Ayas.
			{
				// Asmodians.
			}
			case 833673: // Helpful Ayas.
			case 833674: // Friendly Ayas.
			{
				super.handleDialogStart(player);
				break;
			}
			default:
			{
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
				break;
			}
		}
	}
	
	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex)
	{
		final QuestEnv env = new QuestEnv(getOwner(), player, questId, dialogId);
		env.setExtendedRewardIndex(extendedRewardIndex);
		final PlayerEffectController effectController = player.getEffectController();
		if (QuestEngine.getInstance().onDialog(env) && (dialogId != 1011))
		{
			return true;
		}
		if (dialogId == 10000)
		{
			int skillId = 0;
			switch (getNpcId())
			{
				case 833671: // Helpful Ayas E.
				case 833673: // Helpful Ayas A.
				{
					skillId = 11047; // Helpful Ayas' Cheer.
					effectController.removeEffect(11049);
					break;
				}
				case 833672: // Friendly Ayas E.
				case 833674: // Friendly Ayas A.
				{
					skillId = 11049; // Friendly Ayas' Cheer.
					effectController.removeEffect(11047);
					break;
				}
			}
			SkillEngine.getInstance().getSkill(getOwner(), skillId, 1, player).useNoAnimationSkill();
		}
		else if ((dialogId == 1011) && (questId != 0))
		{
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), dialogId, questId));
		}
		return true;
	}
}