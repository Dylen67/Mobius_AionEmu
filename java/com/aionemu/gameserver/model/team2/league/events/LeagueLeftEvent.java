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
package com.aionemu.gameserver.model.team2.league.events;

import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team2.league.League;
import com.aionemu.gameserver.model.team2.league.LeagueMember;
import com.aionemu.gameserver.model.team2.league.LeagueService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 */
public class LeagueLeftEvent extends AlwaysTrueTeamEvent implements Predicate<LeagueMember>
{
	private final League league;
	final PlayerAlliance alliance;
	private final LeaveReson reason;
	
	public static enum LeaveReson
	{
		LEAVE,
		EXPEL,
		DISBAND;
	}
	
	public LeagueLeftEvent(League league, PlayerAlliance alliance)
	{
		this(league, alliance, LeaveReson.LEAVE);
	}
	
	public LeagueLeftEvent(League league, PlayerAlliance alliance, LeaveReson reason)
	{
		this.league = league;
		this.alliance = alliance;
		this.reason = reason;
	}
	
	@Override
	public void handleEvent()
	{
		league.removeMember(alliance.getTeamId());
		league.apply(this);
		switch (reason)
		{
			case LEAVE:
			{
				alliance.sendPacket(new SM_ALLIANCE_INFO(alliance));
				checkDisband();
				break;
			}
			case EXPEL:
			{
				alliance.sendPacket(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.UNION_BAN_ME, league.getLeaderObject().getLeader().getName()));
				checkDisband();
				break;
			}
			case DISBAND:
			{
				alliance.sendPacket(new SM_ALLIANCE_INFO(alliance));
				break;
			}
		}
	}
	
	private final void checkDisband()
	{
		if (league.onlineMembers() <= 1)
		{
			LeagueService.disband(league);
		}
	}
	
	@Override
	public boolean apply(LeagueMember member)
	{
		final PlayerAlliance leagueAlliance = member.getObject();
		leagueAlliance.applyOnMembers(member1 ->
		{
			switch (reason)
			{
				case LEAVE:
				{
					PacketSendUtility.sendPacket(member1, new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.UNION_LEAVE, alliance.getLeader().getName()));
					break;
				}
				case EXPEL:
				{
					PacketSendUtility.sendPacket(member1, new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.UNION_BAN_HIM, alliance.getLeader().getName()));
					break;
				}
			}
			return true;
		});
		return true;
	}
}