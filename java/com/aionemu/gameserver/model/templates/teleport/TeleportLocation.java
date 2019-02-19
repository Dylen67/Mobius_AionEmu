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
package com.aionemu.gameserver.model.templates.teleport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author ATracer
 */
@XmlRootElement(name = "telelocation")
@XmlAccessorType(XmlAccessType.FIELD)
public class TeleportLocation
{
	@XmlAttribute(name = "loc_id", required = true)
	private int locId;
	
	@XmlAttribute(name = "teleportid")
	private int teleportid;
	
	@XmlAttribute(name = "price", required = true)
	private int price;
	
	@XmlAttribute(name = "pricePvp")
	private int pricePvp;
	
	@XmlAttribute(name = "requiredQuest")
	private int requiredQuest;
	
	@XmlAttribute(name = "type", required = true)
	private TeleportType type;
	
	public int getLocId()
	{
		return locId;
	}
	
	public int getTeleportId()
	{
		return teleportid;
	}
	
	public int getPrice()
	{
		return price;
	}
	
	public int getPricePvp()
	{
		return pricePvp;
	}
	
	public int getRequiredQuest()
	{
		return requiredQuest;
	}
	
	public TeleportType getType()
	{
		return type;
	}
}