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
package system.handlers.ai.instance.aturamSkyFortress;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;

import system.handlers.ai.AggressiveNpcAI2;

/**
 * @author xTz
 */
@AIName("explosion_shadows")
public class ExplosionShadowsAI2 extends AggressiveNpcAI2
{
	private final AtomicBoolean isHome = new AtomicBoolean(true);
	
	@Override
	protected void handleCreatureAggro(Creature creature)
	{
		super.handleCreatureAggro(creature);
		if (isHome.compareAndSet(true, false))
		{
			SkillEngine.getInstance().getSkill(getOwner(), 19428, 1, getOwner()).useNoAnimationSkill();
			getPosition().getWorldMapInstance().getDoors().get(2).setOpen(true);
			getPosition().getWorldMapInstance().getDoors().get(17).setOpen(true);
			doSchedule();
		}
	}
	
	@Override
	protected void handleBackHome()
	{
		isHome.set(true);
		super.handleBackHome();
	}
	
	private void doSchedule()
	{
		if (!isAlreadyDead())
		{
			ThreadPoolManager.getInstance().schedule((Runnable) () ->
			{
				if (!isAlreadyDead())
				{
					SkillEngine.getInstance().getSkill(getOwner(), 19425, 49, getOwner()).useNoAnimationSkill();
					ThreadPoolManager.getInstance().schedule((Runnable) () ->
					{
						if (!isAlreadyDead())
						{
							check();
						}
					}, 1500);
				}
			}, 3000);
		}
	}
	
	void check()
	{
		getPosition().getWorldMapInstance().getDoors().get(17).setOpen(false);
		getPosition().getWorldMapInstance().getDoors().get(2).setOpen(false);
		getKnownList().doOnAllPlayers(player ->
		{
			if (player.getEffectController().hasAbnormalEffect(19502) || player.getEffectController().hasAbnormalEffect(21807) || player.getEffectController().hasAbnormalEffect(21808))
			{
				final Npc npc = (Npc) spawn(799657, player.getX(), player.getY(), player.getZ(), player.getHeading());
				player.getEffectController().removeEffect(19502);
				player.getEffectController().removeEffect(21807);
				player.getEffectController().removeEffect(21808);
				ThreadPoolManager.getInstance().schedule((Runnable) () ->
				{
					if ((npc != null) && !npc.getLifeStats().isAlreadyDead())
					{
						npc.getController().onDelete();
					}
				}, 4000);
			}
		});
		AI2Actions.deleteOwner(this);
	}
}