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
package system.handlers.ai.instance.beshmundirTemple;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

import system.handlers.ai.AggressiveNpcAI2;

/**
 * @author xTz
 */
@AIName("manadar")
public class ManadarAI2 extends AggressiveNpcAI2
{
	private boolean isStart = false;
	
	@Override
	protected void handleAttack(Creature creature)
	{
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}
	
	private void checkPercentage(int hpPercentage)
	{
		if ((hpPercentage <= 90) && !isStart)
		{
			isStart = true;
			check();
		}
	}
	
	@Override
	protected void handleBackHome()
	{
		isStart = false;
		super.handleBackHome();
	}
	
	void check()
	{
		if (getPosition().isSpawned() && !isAlreadyDead() && isStart)
		{
			for (int i = 0; i < 5; i++)
			{
				final int distance = Rnd.get(4, 11);
				int nrNpc = Rnd.get(1, 2);
				switch (nrNpc)
				{
					case 1:
					{
						nrNpc = 281545;
						break;
					}
					case 2:
					{
						nrNpc = 281756;
						break;
					}
				}
				rndSpawnInRange(nrNpc, distance);
			}
			doSchedule();
		}
	}
	
	private void rndSpawnInRange(int npcId, float distance)
	{
		final float direction = Rnd.get(0, 199) / 100f;
		final float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		final float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		spawn(npcId, getPosition().getX() + x1, getPosition().getY() + y1, getPosition().getZ(), (byte) 0);
	}
	
	private void doSchedule()
	{
		ThreadPoolManager.getInstance().schedule((Runnable) () -> check(), 6000);
	}
}