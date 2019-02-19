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
package system.handlers.ai.instance.tiamatStronghold;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.world.WorldPosition;

import system.handlers.ai.AggressiveNpcAI2;

/**
 * @author Ranastic (Encom)
 */
@AIName("brigade_general_tahabata")
public class BrigadeGeneralTahabataAI2 extends AggressiveNpcAI2
{
	private int phase = 0;
	private final AtomicBoolean isAggred = new AtomicBoolean(false);
	
	@Override
	protected void handleAttack(Creature creature)
	{
		super.handleAttack(creature);
		if (isAggred.compareAndSet(false, true))
		{
			getPosition().getWorldMapInstance().getDoors().get(610).setOpen(false);
		}
		checkPercentage(getLifeStats().getHpPercentage());
	}
	
	private void checkPercentage(int hpPercentage)
	{
		if ((hpPercentage == 90) && (phase < 1))
		{
			phase = 1;
			phase1();
			startPhase();
			sendMsg(1500716);
		}
		if ((hpPercentage == 50) && (phase < 2))
		{
			phase = 2;
			phase2();
			startPhase();
			sendMsg(1500716);
		}
		if ((hpPercentage == 20) && (phase < 3))
		{
			phase = 3;
			phase3();
			startPhase();
			sendMsg(1500716);
		}
	}
	
	private void phase1()
	{
		ThreadPoolManager.getInstance().schedule((Runnable) () -> spawn(283116, 679.88f, 1068.88f, 497.88f, (byte) 0), 5000);
	}
	
	private void phase2()
	{
		ThreadPoolManager.getInstance().schedule((Runnable) () -> spawn(283118, 679.88f, 1068.88f, 497.88f, (byte) 0), 5000);
	}
	
	private void phase3()
	{
		ThreadPoolManager.getInstance().schedule((Runnable) () ->
		{
			spawn(283120, 679.88f, 1068.88f, 497.88f, (byte) 0); // Tahabata's Altar 3.
			spawn(283102, 679.88f, 1068.88f, 497.88f, (byte) 0); // Fire Tornado.
		}, 5000);
	}
	
	private void startPhase()
	{
		AI2Actions.useSkill(this, 20060); // Lava Eruption.
	}
	
	void startParalyze()
	{
		AI2Actions.useSkill(this, 20761); // Flame Terror.
	}
	
	@Override
	protected void handleDied()
	{
		final WorldPosition p = getPosition();
		if (p != null)
		{
			deleteNpcs(p.getWorldMapInstance().getNpcs(283102));
			deleteNpcs(p.getWorldMapInstance().getNpcs(283116));
			deleteNpcs(p.getWorldMapInstance().getNpcs(283118));
			deleteNpcs(p.getWorldMapInstance().getNpcs(283120));
		}
		sendMsg(1500717);
		spawn(701541, 676.50964f, 1066.1975f, 497.75186f, (byte) 25); // Brigade General Tahabata Chest.
		super.handleDied();
		AI2Actions.deleteOwner(this);
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
	
	private void sendMsg(int msg)
	{
		NpcShoutsService.getInstance().sendMsg(getOwner(), msg, getObjectId(), 0, 0);
	}
}