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
package system.handlers.ai.worlds.eltnen;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;

/**
 * @author Rinzler (Encom)
 */
@AIName("mysterious_crate")
public class Mysterious_CrateAI2 extends NpcAI2
{
	@Override
	protected void handleDied()
	{
		switch (Rnd.get(1, 7))
		{
			case 1:
			{
				spawn(211793, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading()); // MuMu Mon.
				break;
			}
			case 2:
			{
				spawn(211794, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading()); // MuMu Zoo.
				break;
			}
			case 3:
			{
				spawn(211795, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading()); // Cursed Camu.
				break;
			}
			case 4:
			{
				spawn(211796, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading()); // Cursed Miku.
				break;
			}
			case 5:
			{
				spawn(211797, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading()); // Cursed Muku.
				break;
			}
			case 6:
			{
				spawn(211798, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading()); // Arrogant Amurru.
				break;
			}
			case 7:
			{
				spawn(211800, getOwner().getX(), getOwner().getY(), getOwner().getZ(), getOwner().getHeading()); // Chaos Dracus.
				break;
			}
		}
		super.handleDied();
		AI2Actions.deleteOwner(this);
		AI2Actions.scheduleRespawn(this);
	}
}