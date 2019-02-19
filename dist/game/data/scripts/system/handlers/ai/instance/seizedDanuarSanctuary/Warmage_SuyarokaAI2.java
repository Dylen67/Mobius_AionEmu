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
package system.handlers.ai.instance.seizedDanuarSanctuary;

import java.util.List;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;

import system.handlers.ai.AggressiveNpcAI2;

/**
 * @author Rinzler (Encom)
 */
@AIName("suyaroka")
public class Warmage_SuyarokaAI2 extends AggressiveNpcAI2
{
	private int stage = 0;
	private boolean isStart = false;
	
	@Override
	protected void handleCreatureAggro(Creature creature)
	{
		super.handleCreatureAggro(creature);
		wakeUp();
	}
	
	@Override
	protected void handleAttack(Creature creature)
	{
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
		wakeUp();
	}
	
	private void wakeUp()
	{
		isStart = true;
	}
	
	private void checkPercentage(int hpPercentage)
	{
		if ((hpPercentage <= 90) && (stage < 1))
		{
			stage1();
			stage = 1;
		}
	}
	
	void stage1()
	{
		final int delay = 50000;
		if (isAlreadyDead() || !isStart)
		{
			return;
		}
		SkillEngine.getInstance().getSkill(getOwner(), 20657, 1, getOwner()).useNoAnimationSkill(); // Summoning Ritual.
		ShebanMysticalTyrhund();
		scheduleDelayStage1(delay);
	}
	
	private void ShebanMysticalTyrhund()
	{
		if (!isAlreadyDead())
		{
			ThreadPoolManager.getInstance().schedule((Runnable) () ->
			{
				if (!isAlreadyDead())
				{
					spawn(284455, 1051.3069f, 694.83075f, 282.0391f, (byte) 14); // Sheban Mystical Tyrhund.
					spawn(284455, 1062.1957f, 694.9131f, 282.0391f, (byte) 51); // Sheban Mystical Tyrhund.
				}
			}, 3000);
		}
	}
	
	private void scheduleDelayStage1(int delay)
	{
		if (!isStart && !isAlreadyDead())
		{
			return;
		}
		ThreadPoolManager.getInstance().schedule((Runnable) () -> stage1(), delay);
	}
	
	private void despawnNpcs(int npcId)
	{
		final List<Npc> npcs = getPosition().getWorldMapInstance().getNpcs(npcId);
		for (Npc npc : npcs)
		{
			if (npc != null)
			{
				npc.getController().onDelete();
			}
		}
	}
	
	@Override
	protected void handleBackHome()
	{
		super.handleBackHome();
		despawnNpcs(284455); // Sheban Mystical Tyrhund.
		isStart = false;
		stage = 0;
	}
	
	@Override
	protected void handleDied()
	{
		super.handleDied();
		despawnNpcs(284455); // Sheban Mystical Tyrhund.
		isStart = false;
		stage = 0;
	}
}