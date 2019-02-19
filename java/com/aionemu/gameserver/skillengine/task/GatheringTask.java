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
package com.aionemu.gameserver.skillengine.task;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;
import com.aionemu.gameserver.model.templates.gather.Material;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHER_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHER_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 * @author Antraxx
 * @author Kamikaze
 */
public class GatheringTask extends AbstractCraftTask
{
	private final GatherableTemplate template;
	private final Material material;
	
	public GatheringTask(Player requestor, Gatherable gatherable, Material material, int skillLvlDiff)
	{
		super(requestor, gatherable, skillLvlDiff);
		template = gatherable.getObjectTemplate();
		this.material = material;
		itemQuality = DataManager.ITEM_DATA.getItemTemplate(this.material.getItemid()).getItemQuality();
		currentSuccessValue = 0;
		currentFailureValue = 0;
		maxSuccessValue = (itemQuality.getQualityId() + 1) * 20;
		maxFailureValue = (itemQuality.getQualityId() + 1) * 30;
	}
	
	@Override
	protected void onInteractionAbort()
	{
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, 0, 0, 5));
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 2));
	}
	
	@Override
	protected void onInteractionFinish()
	{
		((Gatherable) responder).getController().completeInteraction();
	}
	
	@Override
	protected void onInteractionStart()
	{
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, maxSuccessValue, maxFailureValue, 0));
		onInteraction();
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 0), true);
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 1), true);
	}
	
	@Override
	protected void analyzeInteraction()
	{
		final int critVal = Rnd.get(55000) / (skillLvlDiff + 1);
		if (critVal < CraftConfig.CRAFT_CHANCE_PURPLE_CRIT)
		{
			critType = CraftCritType.PURPLE;
			currentSuccessValue = maxSuccessValue;
			return;
		}
		else if (critVal < CraftConfig.CRAFT_CHANCE_BLUE_CRIT)
		{
			critType = CraftCritType.BLUE;
		}
		else if (critVal < CraftConfig.CRAFT_CHANCE_INSTANT)
		{
			critType = CraftCritType.INSTANT;
			currentSuccessValue = maxSuccessValue;
			return;
		}
		if (CraftConfig.CRAFT_CHECK_TASK)
		{
			if (task == null)
			{
				return;
			}
		}
		double mod = (Math.sqrt((double) skillLvlDiff / 450f) * 100f) + (Rnd.nextGaussian() * 10f);
		mod -= itemQuality.getQualityId();
		if (mod < 0)
		{
			currentFailureValue -= (int) mod;
		}
		else
		{
			currentSuccessValue += (int) mod;
		}
		if (currentSuccessValue >= maxSuccessValue)
		{
			currentSuccessValue = maxSuccessValue;
		}
		else if (currentFailureValue >= maxFailureValue)
		{
			currentFailureValue = maxFailureValue;
		}
	}
	
	@Override
	protected void sendInteractionUpdate()
	{
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, critType.getPacketId()));
		if (critType == CraftCritType.BLUE)
		{
			critType = CraftCritType.NONE;
		}
	}
	
	@Override
	protected boolean onInteraction()
	{
		if (currentSuccessValue == maxSuccessValue)
		{
			return onSuccessFinish();
		}
		if (currentFailureValue == maxFailureValue)
		{
			onFailureFinish();
			return true;
		}
		analyzeInteraction();
		sendInteractionUpdate();
		return false;
	}
	
	@Override
	protected void onFailureFinish()
	{
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 1));
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 7));
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 3), true);
	}
	
	@Override
	protected boolean onSuccessFinish()
	{
		PacketSendUtility.sendPacket(requestor, SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHER_SUCCESS_1_BASIC(new DescriptionId(material.getNameid())));
		PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 2), true);
		PacketSendUtility.sendPacket(requestor, new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 6));
		if (template.getEraseValue() > 0)
		{
			requestor.getInventory().decreaseByItemId(template.getRequiredItemId(), template.getEraseValue());
		}
		ItemService.addItem(requestor, material.getItemid(), requestor.getRates().getGatheringCountRate());
		if (requestor.isInInstance())
		{
			requestor.getPosition().getWorldMapInstance().getInstanceHandler().onGather(requestor, (Gatherable) responder);
		}
		((Gatherable) responder).getController().rewardPlayer(requestor);
		return true;
	}
}