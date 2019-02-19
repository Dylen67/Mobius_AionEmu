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
package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.world.World;

/**
 * This class is for controlling VisibleObjects [players, npc's etc]. Its controlling movement, visibility etc.
 * @author -Nemesiss-
 * @param <T>
 */
public abstract class VisibleObjectController<T extends VisibleObject>
{
	private T owner;
	
	public void setOwner(T owner)
	{
		this.owner = owner;
	}
	
	public T getOwner()
	{
		return owner;
	}
	
	public void see(VisibleObject object)
	{
	}
	
	public void notSee(VisibleObject object, boolean isOutOfRange)
	{
	}
	
	public void delete()
	{
		if (getOwner().isSpawned())
		{
			World.getInstance().despawn(getOwner());
		}
		
		World.getInstance().removeObject(getOwner());
	}
	
	public void onBeforeSpawn()
	{
	}
	
	public void onAfterSpawn()
	{
		
	}
	
	public void onDespawn()
	{
	}
	
	public void onDelete()
	{
		if (getOwner().isInWorld())
		{
			this.onDespawn();
			this.delete();
		}
	}
}