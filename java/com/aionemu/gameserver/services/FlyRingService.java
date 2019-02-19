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
package com.aionemu.gameserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;

/**
 * @author xavier
 */
public class FlyRingService
{
	Logger log = LoggerFactory.getLogger(FlyRingService.class);
	
	private static class SingletonHolder
	{
		protected static final FlyRingService instance = new FlyRingService();
	}
	
	public static FlyRingService getInstance()
	{
		return SingletonHolder.instance;
	}
	
	FlyRingService()
	{
		for (FlyRingTemplate t : DataManager.FLY_RING_DATA.getFlyRingTemplates())
		{
			final FlyRing f = new FlyRing(t, 0);
			f.spawn();
			log.debug("Added " + f.getName() + " at m=" + f.getWorldId() + ",x=" + f.getX() + ",y=" + f.getY() + ",z=" + f.getZ());
		}
	}
}
