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
package system.handlers.ai;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 * @modified Kashim
 * @Reworked Kill3r
 */
@AIName("trap")
public class TrapNpcAI2 extends NpcAI2
{
	private Future<?> despawnTask;
	public static int EVENT_SET_TRAP_RANGE = 1;
	
	@Override
	protected void handleCreatureSee(Creature creature)
	{
		super.handleCreatureSee(creature);
		tryActivateTrap(creature);
	}
	
	@Override
	protected void handleCreatureMoved(Creature creature)
	{
		super.handleCreatureMoved(creature);
		tryActivateTrap(creature);
	}
	
	private void tryActivateTrap(Creature creature)
	{
		if (despawnTask != null)
		{
			return;
		}
		if (!creature.getLifeStats().isAlreadyDead() && !creature.isInVisualState(CreatureVisualState.BLINKING) && isInRange(creature, getOwner().getAggroRange()))
		{
			final Creature creator = (Creature) getCreator();
			if (!creator.isEnemy(creature))
			{
				return;
			}
			explode(creature);
		}
	}
	
	@Override
	protected void handleCustomEvent(int eventId, Object... args)
	{
		if (eventId == EVENT_SET_TRAP_RANGE)
		{
			// ?
		}
	}
	
	private void explode(Creature creature)
	{
		if (setStateIfNot(AIState.FIGHT))
		{
			getOwner().unsetVisualState(CreatureVisualState.HIDE1);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_PLAYER_STATE(getOwner()));
			AI2Actions.targetCreature(this, creature);
			final NpcSkillEntry npcSkill = getSkillList().getRandomSkill();
			if (npcSkill != null)
			{
				AI2Actions.useSkill(this, npcSkill.getSkillId());
			}
			despawnTask = ThreadPoolManager.getInstance().schedule(new TrapDelete(this), 5000);
		}
	}
	
	@Override
	public boolean isMoveSupported()
	{
		return false;
	}
	
	@Override
	protected AIAnswer pollInstance(AIQuestion question)
	{
		switch (question)
		{
			case SHOULD_DECAY:
			{
				return AIAnswers.NEGATIVE;
			}
			case SHOULD_RESPAWN:
			{
				return AIAnswers.NEGATIVE;
			}
			case SHOULD_REWARD:
			{
				return AIAnswers.NEGATIVE;
			}
			default:
			{
				return null;
			}
		}
	}
	
	private static final class TrapDelete implements Runnable
	{
		private TrapNpcAI2 ai;
		
		TrapDelete(TrapNpcAI2 ai)
		{
			this.ai = ai;
		}
		
		@Override
		public void run()
		{
			AI2Actions.deleteOwner(ai);
			ai = null;
		}
	}
}