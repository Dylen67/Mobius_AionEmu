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
package system.handlers.ai.instance.theobomosTestChamber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.actions.CreatureActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

import system.handlers.ai.AggressiveNpcAI2;

/****/
/**
 * Author Rinzler, Ranastic (Encom) /
 ****/

@AIName("IDF6_Lap_GodElemental_67_Ah")
public class Desecrated_IfritAI2 extends AggressiveNpcAI2
{
	private Future<?> phaseTask;
	private boolean canThink = true;
	private final AtomicBoolean isAggred = new AtomicBoolean(false);
	private final AtomicBoolean isStartedEvent = new AtomicBoolean(false);
	
	@Override
	protected void handleSpawned()
	{
		super.handleSpawned();
		elementalLordship();
	}
	
	private void elementalLordship()
	{
		SkillEngine.getInstance().getSkill(getOwner(), 22744, 1, getOwner()).useNoAnimationSkill(); // Elemental Lordship.
	}
	
	@Override
	protected void handleAttack(Creature creature)
	{
		super.handleAttack(creature);
		if (isAggred.compareAndSet(false, true))
		{
			switch (getNpcId())
			{
				case 220426: // Desecrated Ifrit.
				{
					// You have 15 minutes to find the Brilliant Elemental before it flees.
					PacketSendUtility.npcSendPacketTime(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_Teo_T_TimeAttack_01, 0);
					// Fractured Elemental Lord is boiling over. Gather together to spread the damage out.
					PacketSendUtility.npcSendPacketTime(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_Teo_T_Boss_Skill_01, 20000);
					// The Fractured Elemental is building up power. Gather together to disperse the damage.
					PacketSendUtility.npcSendPacketTime(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_Teo_T_Boss_Skill_02, 40000);
					break;
				}
			}
		}
		checkPercentage(getLifeStats().getHpPercentage());
	}
	
	private void checkPercentage(int hpPercentage)
	{
		if (hpPercentage <= 95)
		{
			if (isStartedEvent.compareAndSet(false, true))
			{
				startPhaseTask();
			}
		}
		if (hpPercentage <= 70)
		{
			if (isStartedEvent.compareAndSet(false, true))
			{
				startPhaseTask();
			}
		}
		if (hpPercentage <= 50)
		{
			if (isStartedEvent.compareAndSet(false, true))
			{
				startPhaseTask();
			}
		}
		if (hpPercentage <= 30)
		{
			if (isStartedEvent.compareAndSet(false, true))
			{
				startPhaseTask();
			}
		}
		if (hpPercentage <= 10)
		{
			if (isStartedEvent.compareAndSet(false, true))
			{
				startPhaseTask();
			}
		}
	}
	
	private void startPhaseTask()
	{
		phaseTask = ThreadPoolManager.getInstance().scheduleAtFixedRate((Runnable) () ->
		{
			if (isAlreadyDead())
			{
				cancelPhaseTask();
			}
			else
			{
				// A massive blast of elemental power will soon explode with destructive force.
				PacketSendUtility.npcSendPacketTime(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_Teo_T_Boss_Skill_03, 0);
				SkillEngine.getInstance().getSkill(getOwner(), 22743, 10, getOwner()).useNoAnimationSkill(); // Elemental Explosion.
				final List<Player> players = getLifedPlayers();
				if (!players.isEmpty())
				{
					final int size = players.size();
					if (players.size() < 6)
					{
						for (Player p : players)
						{
							spawnIfritSoul(p);
						}
					}
					else
					{
						final int count = Rnd.get(6, size);
						for (int i = 0; i < count; i++)
						{
							if (players.isEmpty())
							{
								break;
							}
							spawnIfritSoul(players.get(Rnd.get(players.size())));
						}
					}
				}
			}
		}, 20000, 40000);
	}
	
	private void spawnIfritSoul(Player player)
	{
		final float x = player.getX();
		final float y = player.getY();
		final float z = player.getZ();
		if ((x > 0) && (y > 0) && (z > 0))
		{
			ThreadPoolManager.getInstance().schedule((Runnable) () ->
			{
				if (!isAlreadyDead())
				{
					spawn(237252, x, y, z, (byte) 0); // Ifrit's Soul.
				}
			}, 3000);
		}
	}
	
	@Override
	public boolean canThink()
	{
		return canThink;
	}
	
	private List<Player> getLifedPlayers()
	{
		final List<Player> players = new ArrayList<>();
		for (Player player : getKnownList().getKnownPlayers().values())
		{
			if (!CreatureActions.isAlreadyDead(player))
			{
				players.add(player);
			}
		}
		return players;
	}
	
	void cancelPhaseTask()
	{
		if ((phaseTask != null) && !phaseTask.isDone())
		{
			phaseTask.cancel(true);
		}
	}
	
	private void deleteHelpers()
	{
		final WorldMapInstance instance = getPosition().getWorldMapInstance();
		if (instance != null)
		{
			deleteNpcs(instance.getNpcs(237252)); // Ifrit's Soul.
		}
	}
	
	@Override
	protected void handleDied()
	{
		cancelPhaseTask();
		final WorldPosition p = getPosition();
		if (p != null)
		{
			deleteNpcs(p.getWorldMapInstance().getNpcs(237252)); // Ifrit's Soul.
		}
		// The Brilliant Elemental has disappeared.
		PacketSendUtility.npcSendPacketTime(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_Teo_T_End_01, 0);
		// The Brilliant Elemental has failed to find a focus for its power and has faded.
		PacketSendUtility.npcSendPacketTime(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_Teo_T_End_02, 10000);
		super.handleDied();
	}
	
	private void deleteNpcs(List<Npc> npcs)
	{
		for (Npc npc : npcs)
		{
			if (npc != null)
			{
				npc.getController().onDelete();
			}
		}
	}
	
	@Override
	protected void handleDespawned()
	{
		cancelPhaseTask();
		super.handleDespawned();
	}
	
	@Override
	protected void handleBackHome()
	{
		canThink = true;
		cancelPhaseTask();
		deleteHelpers();
		isAggred.set(false);
		isStartedEvent.set(false);
		super.handleBackHome();
	}
}