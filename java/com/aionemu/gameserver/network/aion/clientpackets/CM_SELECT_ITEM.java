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
package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.decomposable.SelectItem;
import com.aionemu.gameserver.model.templates.decomposable.SelectItems;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SELECT_ITEM_ADD;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Rework LightNing (ENCOM)
 */
public class CM_SELECT_ITEM extends AionClientPacket
{
	int uniqueItemId;
	int index;
	
	@SuppressWarnings("unused")
	private int unk;
	
	public CM_SELECT_ITEM(int opcode, AionConnection.State state, AionConnection.State... restStates)
	{
		super(opcode, state, restStates);
	}
	
	@Override
	protected void readImpl()
	{
		uniqueItemId = readD();
		unk = readD();
		index = readC();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getConnection().getActivePlayer();
		final Item item = player.getInventory().getItemByObjId(uniqueItemId);
		if (item == null)
		{
			return;
		}
		final int nameId = item.getNameId();
		sendPacket(new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), player.getObjectId(), uniqueItemId, item.getItemId(), 1000, 0, 0));
		final ItemUseObserver observer = new ItemUseObserver()
		{
			@Override
			public void abort()
			{
				player.getController().cancelTask(TaskId.ITEM_USE);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400453, new DescriptionId(nameId)));
				player.getObserveController().removeObserver(this);
				sendPacket(new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), player.getObjectId(), uniqueItemId, item.getItemId(), 0, 2, 2));
			}
		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() ->
		{
			player.getObserveController().removeObserver(observer);
			sendPacket(new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), player.getObjectId(), uniqueItemId, item.getItemId(), 0, 1, 1));
			final boolean delete = player.getInventory().decreaseByObjectId(uniqueItemId, 1L);
			if (delete)
			{
				final SelectItems selectitem = DataManager.DECOMPOSABLE_SELECT_ITEM_DATA.getSelectItem(player.getPlayerClass(), player.getRace(), item.getItemId());
				final SelectItem st = selectitem.getItems().get(index);
				ItemService.addItem(player, st.getSelectItemId(), st.getCount());
				sendPacket(new SM_SELECT_ITEM_ADD(uniqueItemId, index));
			}
		}, 1000));
	}
}