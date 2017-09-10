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
package system.handlers.ai.instance.illuminaryObelisk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.actions.CreatureActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

import system.handlers.ai.AggressiveNpcAI2;

/**
 * @author Rinzler (Encom)
 */
@AIName("test_weapon_dynatoum")
public class Test_Weapon_DynatoumAI2 extends AggressiveNpcAI2
{
	private Future<?> phaseTask;
	private boolean canThink = true;
	private Future<?> testDynatoumFormTask;
	private final AtomicBoolean isAggred = new AtomicBoolean(false);
	private final AtomicBoolean isStartedEvent = new AtomicBoolean(false);
	
	@Override
	protected void handleAttack(Creature creature)
	{
		super.handleAttack(creature);
		if (isAggred.compareAndSet(false, true))
		{
			switch (getNpcId())
			{
				case 233740: // Test Weapon Dynatoum.
				{
					/**
					 * You have about 6 minutes to finish the boss, so all party members must be ready before activating the seal.
					 */
					// The Test Weapon Dynatoum's bomb timer has begun its countdown.
					NpcShoutsService.getInstance().sendMsg(getOwner(), 1402143, 0);
					// Test Weapon Dynatoum will go off in 5 minutes.
					NpcShoutsService.getInstance().sendMsg(getOwner(), 1402144, 60000);
					// Test Weapon Dynatoum will go off in 1 minute.
					NpcShoutsService.getInstance().sendMsg(getOwner(), 1402145, 300000);
					// Test Weapon Dynatoum has detonated.
					NpcShoutsService.getInstance().sendMsg(getOwner(), 1402146, 360000);
					testDynatoumFormTask = ThreadPoolManager.getInstance().schedule((Runnable) () -> AI2Actions.deleteOwner(Test_Weapon_DynatoumAI2.this), 360000);
					break;
				}
			}
		}
		checkPercentage(getLifeStats().getHpPercentage());
	}
	
	private void checkPercentage(int hpPercentage)
	{
		if (hpPercentage <= 85)
		{
			if (isStartedEvent.compareAndSet(false, true))
			{
				startPhaseTask();
			}
		}
		if (hpPercentage <= 55)
		{
			if (isStartedEvent.compareAndSet(false, true))
			{
				startPhaseTask();
			}
		}
		if (hpPercentage <= 35)
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
				final List<Player> players = getLifedPlayers();
				if (!players.isEmpty())
				{
					final int size = players.size();
					if (players.size() < 6)
					{
						for (Player p : players)
						{
							spawnMaintenanceDevice(p);
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
							spawnMaintenanceDevice(players.get(Rnd.get(players.size())));
						}
					}
				}
			}
		}, 20000, 40000);
	}
	
	private void spawnMaintenanceDevice(Player player)
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
					spawn(284861, x, y, z, (byte) 0); // Maintenance Device.
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
	
	private void cancelTestDynatoumFormTask()
	{
		if ((testDynatoumFormTask != null) && !testDynatoumFormTask.isDone())
		{
			testDynatoumFormTask.cancel(true);
		}
	}
	
	private void deleteHelpers()
	{
		final WorldMapInstance instance = getPosition().getWorldMapInstance();
		if (instance != null)
		{
			deleteNpcs(instance.getNpcs(284861)); // Maintenance Device.
		}
	}
	
	@Override
	protected void handleSpawned()
	{
		super.handleSpawned();
		switch (getNpcId())
		{
			case 233740:
			{
				boost();
				break;
			}
		}
	}
	
	@Override
	protected void handleDespawned()
	{
		cancelPhaseTask();
		cancelTestDynatoumFormTask();
		super.handleDespawned();
	}
	
	@Override
	protected void handleBackHome()
	{
		canThink = true;
		deleteHelpers();
		cancelPhaseTask();
		isAggred.set(false);
		isStartedEvent.set(false);
		cancelTestDynatoumFormTask();
		super.handleBackHome();
	}
	
	@Override
	protected void handleDied()
	{
		cancelPhaseTask();
		cancelTestDynatoumFormTask();
		final WorldPosition p = getPosition();
		if (p != null)
		{
			deleteNpcs(p.getWorldMapInstance().getNpcs(284861)); // Maintenance Device.
		}
		super.handleDied();
	}
	
	private void boost()
	{
		SkillEngine.getInstance().getSkill(getOwner(), 21671, 1, getOwner()).useNoAnimationSkill(); // Boost.
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
}