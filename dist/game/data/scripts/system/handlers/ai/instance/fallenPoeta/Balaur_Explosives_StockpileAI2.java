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
package system.handlers.ai.instance.fallenPoeta;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;

import system.handlers.ai.AggressiveNpcAI2;

/**
 * @author Rinzler (Encom)
 */
@AIName("Balaur_Explosives_Stockpile")
public class Balaur_Explosives_StockpileAI2 extends AggressiveNpcAI2
{
	@Override
	public void think()
	{
	}
	
	@Override
	protected void handleSpawned()
	{
		attackBoost();
		super.handleSpawned();
	}
	
	private void attackBoost()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate((Runnable) () ->
		{
			AI2Actions.targetCreature(Balaur_Explosives_StockpileAI2.this, getPosition().getWorldMapInstance().getNpc(243682)); // Lieutenant Anuhart.
			AI2Actions.useSkill(Balaur_Explosives_StockpileAI2.this, 0);
		}, 3000, 8000);
	}
	
	@Override
	public boolean isMoveSupported()
	{
		return false;
	}
}