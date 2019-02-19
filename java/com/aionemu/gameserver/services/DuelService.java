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

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.DuelResult;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DUEL;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.ZoneInstance;

import javolution.util.FastMap;

/**
 * @author Simple, Sphinx, xTz
 * @reworked Kill3r
 */
public class DuelService
{
	private static Logger log = LoggerFactory.getLogger(DuelService.class);
	
	private final FastMap<Integer, Integer> duels;
	private final FastMap<Integer, Future<?>> timeOutTask;
	
	public static DuelService getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private DuelService()
	{
		duels = new FastMap<Integer, Integer>().shared();
		timeOutTask = new FastMap<Integer, Future<?>>().shared();
		log.info("[Duel Service] Has Started !");
	}
	
	public void onDuelRequest(Player requester, Player responder)
	{
		if (requester.isInsideZoneType(ZoneType.PVP) || responder.isInsideZoneType(ZoneType.PVP))
		{
			PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_PARTNER_INVALID(responder.getName()));
			return;
		}
		if (isDueling(requester.getObjectId()) || isDueling(responder.getObjectId()))
		{
			PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_HE_REJECT_DUEL(responder.getName()));
			return;
		}
		for (ZoneInstance zone : responder.getPosition().getMapRegion().getZones(responder))
		{
			if (((!zone.isOtherRaceDuelsAllowed()) && (!responder.getRace().equals(requester.getRace()))) || ((!zone.isSameRaceDuelsAllowed()) && (responder.getRace().equals(requester.getRace()))))
			{
				PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_MSG_DUEL_CANT_IN_THIS_ZONE);
				return;
			}
		}
		final RequestResponseHandler rrh = new RequestResponseHandler(requester)
		{
			@Override
			public void denyRequest(Creature requester, Player responder)
			{
				rejectDuelRequest((Player) requester, responder);
			}
			
			@Override
			public void acceptRequest(Creature requester, Player responder)
			{
				startDuel((Player) requester, responder);
			}
		};
		responder.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_ACCEPT_REQUEST, rrh);
		PacketSendUtility.sendPacket(responder, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_ACCEPT_REQUEST, 0, 0, requester.getName()));
		PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_DUEL_REQUESTED(requester.getName()));
	}
	
	public void confirmDuelWith(Player requester, Player responder)
	{
		if (requester.isEnemy(responder))
		{
			return;
		}
		final RequestResponseHandler rrh = new RequestResponseHandler(responder)
		{
			@Override
			public void denyRequest(Creature requester, Player responder)
			{
			}
			
			@Override
			public void acceptRequest(Creature requester, Player responder)
			{
				cancelDuelRequest(responder, (Player) requester);
			}
		};
		requester.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_WITHDRAW_REQUEST, rrh);
		PacketSendUtility.sendPacket(requester, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_WITHDRAW_REQUEST, 0, 0, responder.getName()));
		PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_REQUEST_TO_PARTNER(responder.getName()));
	}
	
	void rejectDuelRequest(Player requester, Player responder)
	{
		PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_HE_REJECT_DUEL(responder.getName()));
		PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_DUEL_REJECT_DUEL(requester.getName()));
	}
	
	void cancelDuelRequest(Player owner, Player target)
	{
		PacketSendUtility.sendPacket(target, SM_SYSTEM_MESSAGE.STR_DUEL_REQUESTER_WITHDRAW_REQUEST(owner.getName()));
		PacketSendUtility.sendPacket(owner, SM_SYSTEM_MESSAGE.STR_DUEL_WITHDRAW_REQUEST(target.getName()));
	}
	
	void startDuel(Player requester, Player responder)
	{
		PacketSendUtility.sendPacket(requester, SM_DUEL.SM_DUEL_STARTED(responder.getObjectId()));
		PacketSendUtility.sendPacket(responder, SM_DUEL.SM_DUEL_STARTED(requester.getObjectId()));
		startDuelMsg(requester, responder);
		createDuel(requester.getObjectId(), responder.getObjectId());
		createTask(requester, responder);
	}
	
	private void startDuelMsg(Player player1, Player player2)
	{
		World.getInstance().doOnAllPlayers(object ->
		{
			if (MathUtil.isInRange(player1, object, 100))
			{
				// A duel between %0 and %1 has started.
				PacketSendUtility.sendPacket(object, SM_SYSTEM_MESSAGE.STR_DUEL_START_BROADCAST(player2.getName(), player1.getName()));
			}
		});
	}
	
	private void loseDuelMsg(Player player1, Player player2)
	{
		World.getInstance().doOnAllPlayers(object ->
		{
			if (MathUtil.isInRange(player1, object, 100))
			{
				// %0 defeated %1 in a duel.
				PacketSendUtility.sendPacket(object, SM_SYSTEM_MESSAGE.STR_DUEL_STOP_BROADCAST(player2.getName(), player1.getName()));
			}
		});
	}
	
	void drawDuelMsg(Player player1, Player player2)
	{
		World.getInstance().doOnAllPlayers(object ->
		{
			if (MathUtil.isInRange(player1, object, 100))
			{
				// The duel between %0 and %1 was a draw.
				PacketSendUtility.sendPacket(object, SM_SYSTEM_MESSAGE.STR_DUEL_TIMEOUT_BROADCAST(player2.getName(), player1.getName()));
			}
		});
	}
	
	public void loseDuel(Player player)
	{
		if (!isDueling(player.getObjectId()))
		{
			return;
		}
		final int opponnentId = duels.get(player.getObjectId());
		final Player opponent = World.getInstance().findPlayer(opponnentId);
		if (opponent != null)
		{
			opponent.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.DEBUFF);
			opponent.getController().cancelCurrentSkill();
			if (player.getSummon() != null)
			{
				SummonsService.doMode(SummonMode.GUARD, player.getSummon(), UnsummonType.UNSPECIFIED);
			}
			if (opponent.getSummon() != null)
			{
				SummonsService.doMode(SummonMode.GUARD, opponent.getSummon(), UnsummonType.UNSPECIFIED);
			}
			if (player.getSummonedObj() != null)
			{
				player.getSummonedObj().getController().cancelCurrentSkill();
			}
			if (opponent.getSummonedObj() != null)
			{
				opponent.getSummonedObj().getController().cancelCurrentSkill();
			}
			loseDuelMsg(player, opponent);
			PacketSendUtility.sendPacket(opponent, new SM_QUEST_ACTION(0, 0));
			PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(0, 0));
			PacketSendUtility.sendPacket(opponent, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_YOU_WIN, player.getName()));
			PacketSendUtility.sendPacket(player, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_YOU_LOSE, opponent.getName()));
		}
		removeDuel(player.getObjectId(), opponnentId);
	}
	
	public void loseArenaDuel(Player player)
	{
		if (!isDueling(player.getObjectId()))
		{
			return;
		}
		player.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.DEBUFF);
		player.getController().cancelCurrentSkill();
		final int opponnentId = duels.get(player.getObjectId());
		final Player opponent = World.getInstance().findPlayer(opponnentId);
		if (opponent != null)
		{
			opponent.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.DEBUFF);
			opponent.getController().cancelCurrentSkill();
		}
		removeDuel(player.getObjectId(), opponnentId);
	}
	
	private void createTask(Player requester, Player responder)
	{
		final Future<?> task = ThreadPoolManager.getInstance().schedule(() ->
		{
			if (isDueling(requester.getObjectId(), responder.getObjectId()))
			{
				drawDuelMsg(requester, responder);
				PacketSendUtility.sendPacket(requester, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_TIMEOUT, requester.getName()));
				PacketSendUtility.sendPacket(responder, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_TIMEOUT, responder.getName()));
				DuelService.this.removeDuel(requester.getObjectId(), responder.getObjectId());
			}
		}, 5 * 60 * 1000);
		PacketSendUtility.sendPacket(requester, new SM_QUEST_ACTION(0, 300));
		PacketSendUtility.sendPacket(responder, new SM_QUEST_ACTION(0, 300));
		timeOutTask.put(requester.getObjectId(), task);
		timeOutTask.put(responder.getObjectId(), task);
	}
	
	public boolean isDueling(int playerObjId)
	{
		return duels.containsKey(playerObjId) && duels.containsValue(playerObjId);
	}
	
	public boolean isDueling(int playerObjId, int targetObjId)
	{
		return duels.containsKey(playerObjId) && (duels.get(playerObjId) == targetObjId);
	}
	
	public void createDuel(int requesterObjId, int responderObjId)
	{
		duels.put(requesterObjId, responderObjId);
		duels.put(responderObjId, requesterObjId);
	}
	
	void removeDuel(int requesterObjId, int responderObjId)
	{
		duels.remove(requesterObjId);
		duels.remove(responderObjId);
		removeTask(requesterObjId);
		removeTask(responderObjId);
		final Player requester = World.getInstance().findPlayer(requesterObjId);
		final Player responder = World.getInstance().findPlayer(responderObjId);
		PacketSendUtility.sendPacket(requester, new SM_QUEST_ACTION(4, (0)));
		PacketSendUtility.sendPacket(responder, new SM_QUEST_ACTION(4, (0)));
	}
	
	private void removeTask(int playerId)
	{
		final Future<?> task = timeOutTask.get(playerId);
		if ((task != null) && !task.isDone())
		{
			task.cancel(true);
			timeOutTask.remove(playerId);
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final DuelService instance = new DuelService();
	}
}