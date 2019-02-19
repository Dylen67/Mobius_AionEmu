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
package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.templates.tribe.Tribe;

import gnu.trove.map.hash.THashMap;

/**
 * @author ATracer
 */
@XmlRootElement(name = "tribe_relations")
@XmlAccessorType(XmlAccessType.FIELD)
public class TribeRelationsData
{
	@XmlElement(name = "tribe", required = true)
	protected List<Tribe> tribeList;
	
	protected THashMap<TribeClass, Tribe> tribeNameMap = new THashMap<>();
	
	void afterUnmarshal(Unmarshaller u, Object parent)
	{
		for (Tribe tribe : tribeList)
		{
			tribeNameMap.put(tribe.getName(), tribe);
		}
		tribeList = null;
	}
	
	public int size()
	{
		return tribeNameMap.size();
	}
	
	public boolean hasAggressiveRelations(TribeClass tribeName)
	{
		final Tribe tribe = tribeNameMap.get(tribeName);
		if (tribe == null)
		{
			return false;
		}
		final Tribe baseTribe = tribe.isBasic() ? (Tribe) tribeNameMap.get(tribe.getBase()) : null;
		return !tribe.getAggressive().isEmpty() || ((baseTribe != null) && !baseTribe.getAggressive().isEmpty());
	}
	
	public boolean hasHostileRelations(TribeClass tribeName)
	{
		final Tribe tribe = tribeNameMap.get(tribeName);
		if (tribe == null)
		{
			return false;
		}
		final Tribe baseTribe = tribe.isBasic() ? tribeNameMap.get(tribe.getBase()) : null;
		return !tribe.getHostile().isEmpty() || ((baseTribe != null) && !baseTribe.getHostile().isEmpty());
	}
	
	public boolean hasSupportRelations(TribeClass tribeName)
	{
		final Tribe tribe = tribeNameMap.get(tribeName);
		if (tribe == null)
		{
			return false;
		}
		final Tribe baseTribe = tribe.isBasic() ? tribeNameMap.get(tribe.getBase()) : null;
		return !tribe.getSupport().isEmpty() || ((baseTribe != null) && !baseTribe.getSupport().isEmpty());
	}
	
	public boolean hasFriendRelations(TribeClass tribeName)
	{
		final Tribe tribe = tribeNameMap.get(tribeName);
		if (tribe == null)
		{
			return false;
		}
		final Tribe baseTribe = tribe.isBasic() ? tribeNameMap.get(tribe.getBase()) : null;
		return !tribe.getFriendly().isEmpty() || ((baseTribe != null) && !baseTribe.getFriendly().isEmpty());
	}
	
	public boolean hasNoneRelations(TribeClass tribeName)
	{
		final Tribe tribe = tribeNameMap.get(tribeName);
		if (tribe == null)
		{
			return false;
		}
		final Tribe baseTribe = tribe.isBasic() ? tribeNameMap.get(tribe.getBase()) : null;
		return !tribe.getNone().isEmpty() || ((baseTribe != null) && !baseTribe.getNone().isEmpty());
	}
	
	public boolean hasNeutralRelations(TribeClass tribeName)
	{
		final Tribe tribe = tribeNameMap.get(tribeName);
		if (tribe == null)
		{
			return false;
		}
		final Tribe baseTribe = tribe.isBasic() ? tribeNameMap.get(tribe.getBase()) : null;
		return !tribe.getNeutral().isEmpty() || ((baseTribe != null) && !baseTribe.getNeutral().isEmpty());
	}
	
	public boolean isAggressiveRelation(TribeClass tribeName1, TribeClass tribeName2)
	{
		final Tribe tribe1 = tribeNameMap.get(tribeName1);
		final Tribe tribe2 = tribeNameMap.get(tribeName2);
		if ((tribe1 == null) || (tribe2 == null))
		{
			return false;
		}
		if (isFriendlyRelation(tribeName1, tribeName2))
		{
			return false;
		}
		return tribe1.getAggressive().contains(tribeName2) || (tribe2.isBasic() && tribe1.getAggressive().contains(tribe2.getBase()));
	}
	
	public boolean isSupportRelation(TribeClass tribeName1, TribeClass tribeName2)
	{
		final Tribe tribe1 = tribeNameMap.get(tribeName1);
		final Tribe tribe2 = tribeNameMap.get(tribeName2);
		if ((tribe1 == null) || (tribe2 == null))
		{
			return false;
		}
		return tribe1.getSupport().contains(tribeName2) || (tribe2.isBasic() && tribe1.getAggressive().contains(tribe2.getBase()));
	}
	
	public boolean isFriendlyRelation(TribeClass tribeName1, TribeClass tribeName2)
	{
		final Tribe tribe1 = tribeNameMap.get(tribeName1);
		final Tribe tribe2 = tribeNameMap.get(tribeName2);
		if ((tribe1 == null) || (tribe2 == null))
		{
			return false;
		}
		return tribe1.getFriendly().contains(tribeName2) || (tribe2.isBasic() && tribe1.getFriendly().contains(tribe2.getBase()));
	}
	
	public boolean isNeutralRelation(TribeClass tribeName1, TribeClass tribeName2)
	{
		final Tribe tribe1 = tribeNameMap.get(tribeName1);
		final Tribe tribe2 = tribeNameMap.get(tribeName2);
		if ((tribe1 == null) || (tribe2 == null))
		{
			return false;
		}
		return tribe1.getNeutral().contains(tribeName2) || (tribe2.isBasic() && tribe1.getNeutral().contains(tribe2.getBase()));
	}
	
	public boolean isNoneRelation(TribeClass tribeName1, TribeClass tribeName2)
	{
		final Tribe tribe1 = tribeNameMap.get(tribeName1);
		final Tribe tribe2 = tribeNameMap.get(tribeName2);
		if ((tribe1 == null) || (tribe2 == null))
		{
			return false;
		}
		return tribe1.getNone().contains(tribeName2) || (tribe2.isBasic() && tribe1.getNone().contains(tribe2.getBase()));
	}
	
	public boolean isHostileRelation(TribeClass tribeName1, TribeClass tribeName2)
	{
		final Tribe tribe1 = tribeNameMap.get(tribeName1);
		final Tribe tribe2 = tribeNameMap.get(tribeName2);
		if ((tribe1 == null) || (tribe2 == null))
		{
			return false;
		}
		return tribe1.getHostile().contains(tribeName2) || (tribe2.isBasic() && tribe1.getHostile().contains(tribe2.getBase()));
	}
	
	public boolean hasAnySupporter(TribeClass tribeName)
	{
		final Tribe tribe1 = tribeNameMap.get(tribeName);
		if (tribe1 == null)
		{
			return false;
		}
		for (TribeClass tribe2 : tribeNameMap.keySet())
		{
			if (isSupportRelation(tribe2, tribeName))
			{
				return true;
			}
		}
		return false;
	}
}