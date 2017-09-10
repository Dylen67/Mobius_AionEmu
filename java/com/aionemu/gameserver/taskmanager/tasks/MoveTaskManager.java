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
package com.aionemu.gameserver.taskmanager.tasks;

import static com.aionemu.gameserver.taskmanager.parallel.ForEach.forEach;

import com.aionemu.commons.utils.internal.chmv8.ForkJoinTask;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.taskmanager.AbstractPeriodicTaskManager;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneUpdateService;
import com.google.common.base.Predicate;

import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * @author ATracer
 * @reworked Rolandas, parallelized by using Fork/Join framework
 */
public class MoveTaskManager extends AbstractPeriodicTaskManager
{
	final FastMap<Integer, Creature> movingCreatures = new FastMap<Integer, Creature>().shared();
	
	public static final int UPDATE_PERIOD = 100;
	
	private final Predicate<Creature> CREATURE_MOVE_PREDICATE = creature ->
	{
		creature.getMoveController().moveToDestination();
		if (creature.getAi2().poll(AIQuestion.DESTINATION_REACHED))
		{
			movingCreatures.remove(creature.getObjectId());
			creature.getAi2().onGeneralEvent(AIEventType.MOVE_ARRIVED);
			ZoneUpdateService.getInstance().add(creature);
		}
		else
		{
			creature.getAi2().onGeneralEvent(AIEventType.MOVE_VALIDATE);
		}
		return true;
	};
	
	MoveTaskManager()
	{
		super(UPDATE_PERIOD);
	}
	
	public void addCreature(Creature creature)
	{
		movingCreatures.put(creature.getObjectId(), creature);
	}
	
	public void removeCreature(Creature creature)
	{
		movingCreatures.remove(creature.getObjectId());
	}
	
	@Override
	public void run()
	{
		final FastList<Creature> copy = new FastList<>();
		for (FastMap.Entry<Integer, Creature> e = movingCreatures.head(), mapEnd = movingCreatures.tail(); (e = e.getNext()) != mapEnd;)
		{
			copy.add(e.getValue());
		}
		final ForkJoinTask<Creature> task = forEach(copy, CREATURE_MOVE_PREDICATE);
		if (task != null)
		{
			ThreadPoolManager.getInstance().getForkingPool().invoke(task);
		}
	}
	
	public static MoveTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		static final MoveTaskManager INSTANCE = new MoveTaskManager();
	}
}