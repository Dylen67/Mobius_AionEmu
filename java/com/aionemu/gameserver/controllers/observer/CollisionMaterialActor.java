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
package com.aionemu.gameserver.controllers.observer;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.materials.MaterialActTime;
import com.aionemu.gameserver.model.templates.materials.MaterialSkill;
import com.aionemu.gameserver.model.templates.materials.MaterialTemplate;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.gametime.DayTime;
import com.aionemu.gameserver.utils.gametime.GameTime;
import com.aionemu.gameserver.utils.gametime.GameTimeManager;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author Rolandas
 */
public class CollisionMaterialActor extends AbstractCollisionObserver implements IActor
{
	private final MaterialTemplate actionTemplate;
	private final AtomicReference<MaterialSkill> currentSkill = new AtomicReference<>();
	
	public CollisionMaterialActor(Creature creature, Spatial geometry, MaterialTemplate actionTemplate)
	{
		super(creature, geometry, CollisionIntention.MATERIAL.getId());
		this.actionTemplate = actionTemplate;
	}
	
	private MaterialSkill getSkillForTarget(Creature creature)
	{
		if (creature instanceof Player)
		{
			final Player player = (Player) creature;
			if (player.isProtectionActive())
			{
				return null;
			}
		}
		MaterialSkill foundSkill = null;
		for (MaterialSkill skill : actionTemplate.getSkills())
		{
			if (skill.getTarget().isTarget(creature))
			{
				foundSkill = skill;
				break;
			}
		}
		if (foundSkill == null)
		{
			return null;
		}
		int weatherCode = -1;
		if (creature.getActiveRegion() == null)
		{
			return null;
		}
		final List<ZoneInstance> zones = creature.getActiveRegion().getZones(creature);
		for (ZoneInstance regionZone : zones)
		{
			if (regionZone.getZoneTemplate().getZoneType() == ZoneClassName.WEATHER)
			{
				final Vector3f center = geometry.getWorldBound().getCenter();
				if (!regionZone.getAreaTemplate().isInside3D(center.x, center.y, center.z))
				{
					continue;
				}
				final int weatherZoneId = DataManager.ZONE_DATA.getWeatherZoneId(regionZone.getZoneTemplate());
				weatherCode = WeatherService.getInstance().getWeatherCode(creature.getWorldId(), weatherZoneId);
				break;
			}
		}
		
		final boolean dependsOnWeather = geometry.getName().indexOf("WEATHER") != -1;
		if (dependsOnWeather && (weatherCode > 0))
		{
			return null;
		}
		if (foundSkill.getTime() == null)
		{
			return foundSkill;
		}
		final GameTime gameTime = (GameTime) GameTimeManager.getGameTime().clone();
		if ((foundSkill.getTime() == MaterialActTime.DAY) && (weatherCode == 0))
		{
			return foundSkill;
		}
		if (gameTime.getDayTime() == DayTime.NIGHT)
		{
			if (foundSkill.getTime() == MaterialActTime.NIGHT)
			{
				return foundSkill;
			}
		}
		else
		{
			return foundSkill;
		}
		
		return null;
	}
	
	@Override
	public void onMoved(CollisionResults collisionResults)
	{
		if (collisionResults.size() == 0)
		{
			return;
		}
		// if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && (creature instanceof Player))
		// {
		// final Player player = (Player) creature;
		// }
		act();
	}
	
	@Override
	public void act()
	{
		final MaterialSkill actSkill = getSkillForTarget(creature);
		if (currentSkill.getAndSet(actSkill) != actSkill)
		{
			if (actSkill == null)
			{
				return;
			}
			if (creature.getEffectController().hasAbnormalEffect(actSkill.getId()))
			{
				return;
			}
			final Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
			{
				if (!creature.getEffectController().hasAbnormalEffect(actSkill.getId()))
				{
					// if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && (creature instanceof Player))
					// {
					// final Player player = (Player) creature;
					// }
					final Skill skill = SkillEngine.getInstance().getSkill(creature, actSkill.getId(), actSkill.getSkillLevel(), creature);
					skill.getEffectedList().add(creature);
					skill.useWithoutPropSkill();
				}
			}, 0, (long) (actSkill.getFrequency() * 1000));
			creature.getController().addTask(TaskId.MATERIAL_ACTION, task);
		}
	}
	
	@Override
	public void abort()
	{
		final Future<?> existingTask = creature.getController().getTask(TaskId.MATERIAL_ACTION);
		if (existingTask != null)
		{
			creature.getController().cancelTask(TaskId.MATERIAL_ACTION);
		}
		currentSkill.set(null);
	}
	
	@Override
	public void died(Creature creature)
	{
		abort();
	}
	
	@Override
	public void setEnabled(boolean enable)
	{
	}
}