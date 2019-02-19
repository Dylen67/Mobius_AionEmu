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
package system.handlers.ai.instance.drakenspireDepths;

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
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

import system.handlers.ai.AggressiveNpcAI2;

/**
 * @author Rinzler (Encom)
 */
@AIName("heatvent_protector")
public class Heatvent_ProtectorAI2 extends AggressiveNpcAI2
{
	private Future<?> tornadoTask;
	private boolean canThink = true;
	private Future<?> flamekiteGeistTask;
	private Future<?> heatventProtectorTask;
	private final AtomicBoolean isAggred = new AtomicBoolean(false);
	private final AtomicBoolean is50EventStarted = new AtomicBoolean(false);
	private final AtomicBoolean is30EventStarted = new AtomicBoolean(false);
	
	@Override
	protected void handleAttack(Creature creature)
	{
		super.handleAttack(creature);
		if (isAggred.compareAndSet(false, true))
		{
			switch (getNpcId())
			{
				case 236228: // Heatvent Protector.
				{
					// If the Protectors are not defeated in 5 minutes, the Detachment's Rush Squad will sacrifice themselves to destroy the Fount.
					NpcShoutsService.getInstance().sendMsg(getOwner(), 1402684, 0);
					// In 1 minute, the Detachment's Rush Squad will resolve to sacrifice themselves and attempt to destroy the Fount.
					NpcShoutsService.getInstance().sendMsg(getOwner(), 1402685, 240000);
					// In a moment, the Detachment's Rush Squad, armed with the resolve to sacrifice themselves, will attack the Fount.
					NpcShoutsService.getInstance().sendMsg(getOwner(), 1402686, 270000);
					heatventProtectorTask = ThreadPoolManager.getInstance().schedule((Runnable) () ->
					{
						AI2Actions.deleteOwner(Heatvent_ProtectorAI2.this);
						// Thanks to the sacrifice of the Detachment's Rush Squad, the Protectors' Fount has been destroyed.
						PacketSendUtility.npcSendPacketTime(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDSeal_Twin_06, 0);
					}, 300000);
					break;
				}
			}
		}
		checkPercentage(getLifeStats().getHpPercentage());
	}
	
	@Override
	protected void handleSpawned()
	{
		super.handleSpawned();
		switch (getNpcId())
		{
			case 236228:
			{
				shareSource();
				break;
			}
		}
	}
	
	private void shareSource()
	{
		SkillEngine.getInstance().getSkill(getOwner(), 20770, 1, getOwner()).useNoAnimationSkill(); // Heatvent Protector.
		SkillEngine.getInstance().getSkill(getOwner(), 21643, 1, getOwner()).useNoAnimationSkill(); // Share Source.
	}
	
	private void checkPercentage(int percentage)
	{
		if (percentage <= 50)
		{
			if (is50EventStarted.compareAndSet(false, true))
			{
				startTornadoTask();
			}
		}
		if (percentage <= 30)
		{
			if (is30EventStarted.compareAndSet(false, true))
			{
				startFlamekiteGeistTask();
			}
		}
	}
	
	private void startTornadoTask()
	{
		tornadoTask = ThreadPoolManager.getInstance().scheduleAtFixedRate((Runnable) () ->
		{
			if (isAlreadyDead())
			{
				cancelTornadoTask();
				cancelFlamekiteGeistTask();
				cancelHeatventProtectorTask();
			}
			else
			{
				SkillEngine.getInstance().getSkill(getOwner(), 21645, 60, getOwner()).useNoAnimationSkill(); // Raging Hellfire.
				final List<Player> players = getLifedPlayers();
				if (!players.isEmpty())
				{
					final int size = players.size();
					if (players.size() < 6)
					{
						for (Player p : players)
						{
							spawnTornado(p);
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
							spawnTornado(players.get(Rnd.get(players.size())));
						}
					}
				}
			}
		}, 20000, 40000);
	}
	
	private void startFlamekiteGeistTask()
	{
		flamekiteGeistTask = ThreadPoolManager.getInstance().scheduleAtFixedRate((Runnable) () ->
		{
			if (isAlreadyDead())
			{
				cancelTornadoTask();
				cancelFlamekiteGeistTask();
				cancelHeatventProtectorTask();
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
							spawnFlamekiteGeist(p);
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
							spawnFlamekiteGeist(players.get(Rnd.get(players.size())));
						}
					}
				}
			}
		}, 20000, 40000);
	}
	
	private void spawnTornado(Player player)
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
					spawn(855625, x, y, z, (byte) 0); // Tornado.
				}
			}, 3000);
		}
	}
	
	private void spawnFlamekiteGeist(Player player)
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
					spawn(855622, x, y, z, (byte) 0); // Flamekite Geist.
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
	
	void cancelTornadoTask()
	{
		if ((tornadoTask != null) && !tornadoTask.isDone())
		{
			tornadoTask.cancel(true);
		}
	}
	
	void cancelFlamekiteGeistTask()
	{
		if ((flamekiteGeistTask != null) && !flamekiteGeistTask.isDone())
		{
			flamekiteGeistTask.cancel(true);
		}
	}
	
	void cancelHeatventProtectorTask()
	{
		if ((heatventProtectorTask != null) && !heatventProtectorTask.isDone())
		{
			heatventProtectorTask.cancel(true);
		}
	}
	
	private void deleteHelpers()
	{
		final WorldMapInstance instance = getPosition().getWorldMapInstance();
		if (instance != null)
		{
			deleteNpcs(instance.getNpcs(855622)); // Flamekite Geist.
			deleteNpcs(instance.getNpcs(855625)); // Tornado.
			deleteNpcs(instance.getNpcs(855708));
		}
	}
	
	@Override
	protected void handleDied()
	{
		cancelTornadoTask();
		cancelFlamekiteGeistTask();
		cancelHeatventProtectorTask();
		final WorldPosition p = getPosition();
		if (p != null)
		{
			deleteNpcs(p.getWorldMapInstance().getNpcs(855622)); // Flamekite Geist.
			deleteNpcs(p.getWorldMapInstance().getNpcs(855625)); // Tornado.
			deleteNpcs(p.getWorldMapInstance().getNpcs(855708));
		}
		spawn(833054, 533.9348f, 149.07944f, 1681.8224f, (byte) 45); // Treasure Box.
		spawn(833056, 527.98773f, 154.735f, 1681.8225f, (byte) 105); // Treasure Box.
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
		cancelTornadoTask();
		cancelFlamekiteGeistTask();
		cancelHeatventProtectorTask();
		super.handleDespawned();
	}
	
	@Override
	protected void handleBackHome()
	{
		canThink = true;
		cancelTornadoTask();
		cancelFlamekiteGeistTask();
		cancelHeatventProtectorTask();
		deleteHelpers();
		isAggred.set(false);
		is50EventStarted.set(false);
		is30EventStarted.set(false);
		super.handleBackHome();
	}
}