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
package system.handlers.ai.instance.esoterrace;

import java.util.List;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.windstreams.Location2D;
import com.aionemu.gameserver.model.templates.windstreams.WindstreamTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_WINDSTREAM_ANNOUNCE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rinzler (Encom)
 */
@AIName("Esoterrace_WindStream_2")
public class Esoterrace_WindStream_2AI2 extends NpcAI2
{
	@Override
	protected void handleSpawned()
	{
		super.handleSpawned();
		final Npc npc = getOwner();
		startWindStream(npc);
		windStreamAnnounce(getOwner(), 0);
	}
	
	private void startWindStream(Npc npc)
	{
		ThreadPoolManager.getInstance().schedule(() ->
		{
			final Npc npc2 = (Npc) spawn(300250000, 703055, 392.27563f, 543.89026f, 318.3265f, (byte) 18, 0, 1);
			windStreamAnnounce(npc2, 0);
			despawnNpc(703054);
			spawn(703055, 392.27563f, 543.89026f, 318.3265f, (byte) 18);
			PacketSendUtility.broadcastPacket(npc2, new SM_WINDSTREAM_ANNOUNCE(1, 300250000, 159, 0));
			if (npc2 != null)
			{
				npc2.getController().onDelete();
			}
			if (npc != null)
			{
				npc.getController().onDelete();
			}
		}, 5000);
	}
	
	private void windStreamAnnounce(Npc npc, int state)
	{
		final WindstreamTemplate template = DataManager.WINDSTREAM_DATA.getStreamTemplate(npc.getPosition().getMapId());
		for (Location2D wind : template.getLocations().getLocation())
		{
			if (wind.getId() == 159)
			{
				wind.setState(state);
				break;
			}
		}
		npc.getPosition().getWorld().doOnAllPlayers(player -> PacketSendUtility.sendPacket(player, new SM_WINDSTREAM_ANNOUNCE(1, 300250000, 159, state)));
	}
	
	void despawnNpc(int npcId)
	{
		if (getPosition().getWorldMapInstance().getNpcs(npcId) != null)
		{
			final List<Npc> npcs = getPosition().getWorldMapInstance().getNpcs(npcId);
			for (Npc npc : npcs)
			{
				npc.getController().onDelete();
			}
		}
	}
}