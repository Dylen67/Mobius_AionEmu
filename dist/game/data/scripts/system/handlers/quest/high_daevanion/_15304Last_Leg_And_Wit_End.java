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
package system.handlers.quest.high_daevanion;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestDialog;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Rinzler (Encom)
 */
public class _15304Last_Leg_And_Wit_End extends QuestHandler
{
	private static final int questId = 15304;
	private static final int[] Ab1NewMobs =
	{
		883285,
		883286,
		883287,
		883288,
		883290,
		883291,
		883297,
		883298,
		883299
	};
	
	public _15304Last_Leg_And_Wit_End()
	{
		super(questId);
	}
	
	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		return defaultOnLvlUpEvent(env);
	}
	
	@Override
	public void register()
	{
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(805327).addOnQuestStart(questId); // Rike.
		qe.registerQuestNpc(805327).addOnTalkEvent(questId); // Rike.
		qe.registerQuestNpc(805328).addOnTalkEvent(questId); // Efaion.
		for (int mob : Ab1NewMobs)
		{
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		final int targetId = env.getTargetId();
		if ((qs == null) || (qs.getStatus() == QuestStatus.NONE))
		{
			if (targetId == 805327) // Rike.
			{
				switch (env.getDialog())
				{
					case START_DIALOG:
					{
						return sendQuestDialog(env, 4762);
					}
					case ACCEPT_QUEST:
					case ACCEPT_QUEST_SIMPLE:
					{
						return sendQuestStartDialog(env);
					}
					case REFUSE_QUEST_SIMPLE:
					{
						return closeDialogWindow(env);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START)
		{
			final int var = qs.getQuestVarById(0);
			if (targetId == 805328) // Efaion.
			{
				switch (env.getDialog())
				{
					case START_DIALOG:
					{
						if (var == 0)
						{
							return sendQuestDialog(env, 1011);
						}
						else if (var == 1)
						{
							return sendQuestDialog(env, 1352);
						}
						else if (var == 2)
						{
							return sendQuestDialog(env, 1693);
						}
						else if (var == 3)
						{
							return sendQuestDialog(env, 2036);
						}
					}
					case SELECT_ACTION_1012:
					{
						if (var == 0)
						{
							return sendQuestDialog(env, 1012);
						}
					}
					case STEP_TO_1:
					{
						changeQuestStep(env, 0, 1, false);
						return closeDialogWindow(env);
					}
					case SET_REWARD:
					{
						giveQuestItem(env, 182215862, 1); // Daevanion Pants Prototype.
						removeQuestItem(env, 182215835, 1); // Kaisinel's Improved Pattern Of Ease.
						changeQuestStep(env, 3, 4, true);
						return closeDialogWindow(env);
					}
					case CHECK_COLLECTED_ITEMS:
					{
						return checkQuestItems(env, 1, 2, false, 10000, 10001);
					}
					case FINISH_DIALOG:
					{
						if (var == 1)
						{
							defaultCloseDialog(env, 2, 2);
						}
						else if (var == 1)
						{
							defaultCloseDialog(env, 1, 1);
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			if (targetId == 805327) // Rike.
			{
				if (env.getDialog() == QuestDialog.START_DIALOG)
				{
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if ((qs != null) && (qs.getStatus() == QuestStatus.START))
		{
			final int var = qs.getQuestVarById(0);
			if (var == 2)
			{
				final int var1 = qs.getQuestVarById(1);
				if ((var1 >= 0) && (var1 < 59))
				{
					return defaultOnKillEvent(env, Ab1NewMobs, var1, var1 + 1, 1);
				}
				else if (var1 == 59)
				{
					qs.setQuestVar(3);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}
}