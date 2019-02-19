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
package system.handlers.quest.daevanion;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestDialog;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Rinzler (Encom)
 */
public class _80295Durable_Daevanion_Weapon extends QuestHandler
{
	private static final int questId = 80295;
	
	public _80295Durable_Daevanion_Weapon()
	{
		super(questId);
	}
	
	@Override
	public void register()
	{
		qe.registerQuestNpc(831387).addOnQuestStart(questId);
		qe.registerQuestNpc(831387).addOnTalkEvent(questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
		{
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		}
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if ((qs == null) || (qs.getStatus() == QuestStatus.NONE))
		{
			if (targetId == 831387)
			{
				if (env.getDialog() == QuestDialog.START_DIALOG)
				{
					final int plate = player.getEquipment().itemSetPartsEquipped(302);
					final int chain = player.getEquipment().itemSetPartsEquipped(303);
					final int leather = player.getEquipment().itemSetPartsEquipped(301);
					final int cloth = player.getEquipment().itemSetPartsEquipped(300);
					final int gunslinger = player.getEquipment().itemSetPartsEquipped(372);
					if ((plate != 5) && (chain != 5) && (leather != 5) && (cloth != 5) && (gunslinger != 5))
					{
						return sendQuestDialog(env, 1003);
					}
					return sendQuestDialog(env, 4762);
				}
				return sendQuestStartDialog(env);
			}
		}
		if (qs == null)
		{
			return false;
		}
		final int var = qs.getQuestVarById(0);
		if (qs.getStatus() == QuestStatus.START)
		{
			if (targetId == 831387)
			{
				switch (env.getDialog())
				{
					case START_DIALOG:
					{
						if (var == 0)
						{
							return sendQuestDialog(env, 1011);
						}
					}
					case CHECK_COLLECTED_ITEMS:
					{
						if (var == 0)
						{
							return checkQuestItems(env, 0, 1, true, 5, 0);
						}
						break;
					}
					case SELECT_ACTION_1352:
					{
						if (var == 0)
						{
							return sendQuestDialog(env, 1352);
						}
					}
				}
			}
			return false;
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			if (targetId == 831387)
			{
				return sendQuestEndDialog(env);
			}
			return false;
		}
		return false;
	}
}