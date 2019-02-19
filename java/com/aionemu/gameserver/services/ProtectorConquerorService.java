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

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SERIAL_KILLER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.conquerors.Conqueror;
import com.aionemu.gameserver.services.conquerors.ConquerorBuffs;
import com.aionemu.gameserver.services.protectors.Protector;
import com.aionemu.gameserver.services.protectors.ProtectorBuffs;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

import javolution.util.FastList;
import javolution.util.FastMap;

public class ProtectorConquerorService
{
	// private static final Logger log = LoggerFactory.getLogger(ProtectorConquerorService.class);
	
	final FastMap<Integer, Protector> protectors = new FastMap<>();
	final FastMap<Integer, Conqueror> conquerors = new FastMap<>();
	
	private final FastMap<Integer, FastMap<Integer, Player>> worldConqueror = new FastMap<>();
	private final FastMap<Integer, FastMap<Integer, Player>> worldProtectors = new FastMap<>();
	
	private static final FastMap<Integer, WorldType> handledWorlds = new FastMap<>();
	private final int refresh = CustomConfig.PROTECTOR_CONQUEROR_REFRESH;
	private final int levelDiff = CustomConfig.PROTECTOR_CONQUEROR_LEVEL_DIFF;
	private ProtectorBuffs protectorBuff;
	private ConquerorBuffs conquerorBuff;
	
	public enum WorldType
	{
		ASMODIANS,
		ELYOS,
		USEALL;
	}
	
	public void initSystem()
	{
		if (!CustomConfig.PROTECTOR_CONQUEROR_ENABLE)
		{
			return;
		}
		for (String world : CustomConfig.PROTECTOR_CONQUEROR_WORLDS.split(","))
		{
			if ("".equals(world))
			{
				break;
			}
			final int worldId = Integer.parseInt(world);
			final int worldType = Integer.parseInt(String.valueOf(world.charAt(1)));
			protectorBuff = new ProtectorBuffs();
			conquerorBuff = new ConquerorBuffs();
			final WorldType type = worldType > 0 ? worldType > 1 ? WorldType.ASMODIANS : WorldType.ELYOS : WorldType.USEALL;
			handledWorlds.put(worldId, type);
		}
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
		{
			for (Protector info1 : protectors.values())
			{
				if ((info1.victims > 0) && !isEnemyWorld(info1.getOwner()))
				{
					info1.victims -= CustomConfig.PROTECTOR_CONQUEROR_DECREASE;
					final int newRank1 = getRanks(info1.victims);
					if (info1.getRank() != newRank1)
					{
						info1.setRank(newRank1);
						PacketSendUtility.sendPacket(info1.getOwner(), new SM_SERIAL_KILLER(true, info1.getRank()));
					}
					if (info1.victims < 1)
					{
						info1.victims = 0;
						protectors.remove(info1.getOwner().getObjectId());
					}
				}
			}
			for (Conqueror info2 : conquerors.values())
			{
				if ((info2.victims > 0) && !isEnemyWorld(info2.getOwner()))
				{
					info2.victims -= CustomConfig.PROTECTOR_CONQUEROR_DECREASE;
					final int newRank2 = getRanks(info2.victims);
					if (info2.getRank() != newRank2)
					{
						info2.setRank(newRank2);
						PacketSendUtility.sendPacket(info2.getOwner(), new SM_SERIAL_KILLER(true, info2.getRank()));
					}
					if (info2.victims < 1)
					{
						info2.victims = 0;
						conquerors.remove(info2.getOwner().getObjectId());
					}
				}
			}
		}, refresh * 60000, refresh * 60000);
	}
	
	public FastMap<Integer, Player> getWorldProtector(int worldId)
	{
		if (worldProtectors.containsKey(worldId))
		{
			return worldProtectors.get(worldId);
		}
		final FastMap<Integer, Player> protectors = new FastMap<>();
		worldProtectors.putEntry(worldId, protectors);
		return protectors;
	}
	
	public FastMap<Integer, Player> getWorldConqueror(int worldId)
	{
		if (worldConqueror.containsKey(worldId))
		{
			return worldConqueror.get(worldId);
		}
		final FastMap<Integer, Player> killers = new FastMap<>();
		worldConqueror.putEntry(worldId, killers);
		return killers;
	}
	
	public void onProtectorConquerorLogin(Player player)
	{
		if (!CustomConfig.PROTECTOR_CONQUEROR_ENABLE)
		{
			return;
		}
		if (protectors.containsKey(player.getObjectId()))
		{
			player.setProtectorInfo(protectors.get(player.getObjectId()));
			player.getProtectorInfo().refreshOwner(player);
		}
		if (conquerors.containsKey(player.getObjectId()))
		{
			player.setConquerorInfo(conquerors.get(player.getObjectId()));
			player.getConquerorInfo().refreshOwner(player);
		}
	}
	
	public void onLogout(Player player)
	{
		if (!CustomConfig.PROTECTOR_CONQUEROR_ENABLE)
		{
			return;
		}
		onLeaveMap(player);
	}
	
	public void onEnterMap(Player player)
	{
		if (!CustomConfig.PROTECTOR_CONQUEROR_ENABLE)
		{
			return;
		}
		final int worldId = player.getWorldId();
		final Protector info = player.getProtectorInfo();
		final Conqueror infoConqueror = player.getConquerorInfo();
		if (!isHandledWorld(worldId))
		{
			return;
		}
		if (!isEnemyWorld(player))
		{ // Protector.
			final int objId = player.getObjectId();
			info.setRank(1);
			if (info.getRank() >= 1)
			{
				// You are now a Protector.
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GUARD_UP_1LEVEL);
			}
			if (info.getRank() >= 2)
			{
				// You are now an Indomitable Protector.
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GUARD_UP_2LEVEL);
			}
			if (info.getRank() >= 3)
			{
				// You are now a Valiant Protector.
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GUARD_UP_3LEVEL);
			}
			PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(false, info.getRank()));
			final FastMap<Integer, Player> world = getWorldProtector(worldId);
			if (!world.containsKey(objId))
			{
				world.putEntry(objId, player);
			}
			protectorBuff.applyRankEffect(player, info.getRank());
			World.getInstance().getWorldMap(worldId).getWorldMapInstanceById(player.getInstanceId()).doOnAllPlayers(victim ->
			{
				if (!player.getRace().equals(victim.getRace()))
				{
					PacketSendUtility.sendPacket(victim, new SM_SERIAL_KILLER(world.values()));
				}
			});
		}
		else if (isEnemyWorld(player))
		{ // Conqueror.
			final int objId = player.getObjectId();
			infoConqueror.setRank(1);
			if (infoConqueror.getRank() >= 1)
			{
				// You are now a Conqueror.
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_SLAYER_UP_1LEVEL);
			}
			if (infoConqueror.getRank() >= 2)
			{
				// You are now an Furious Conqueror.
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_SLAYER_UP_2LEVEL);
			}
			if (infoConqueror.getRank() >= 3)
			{
				// You are now a Berserk Conqueror.
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_SLAYER_UP_3LEVEL);
			}
			PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(false, infoConqueror.getRank()));
			final FastMap<Integer, Player> world = getWorldConqueror(worldId);
			if (!world.containsKey(objId))
			{
				world.putEntry(objId, player);
			}
			conquerorBuff.applyEffect(player, infoConqueror.getRank());
			World.getInstance().getWorldMap(worldId).getWorldMapInstanceById(player.getInstanceId()).doOnAllPlayers(victim ->
			{
				if (!player.getRace().equals(victim.getRace()))
				{
					PacketSendUtility.sendPacket(victim, new SM_SERIAL_KILLER(world.values()));
				}
			});
		}
		else
		{
			PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(getWorldProtector(worldId).values()));
			PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(getWorldConqueror(worldId).values()));
		}
		player.clearKnownlist();
		PacketSendUtility.broadcastPacketAndReceive(player, new SM_PLAYER_INFO(player, false));
		player.updateKnownlist();
	}
	
	public void onLeaveMap(Player player)
	{
		final int worldId = player.getWorldId();
		if (!isHandledWorld(worldId))
		{
			return;
		}
		if (!isEnemyWorld(player))
		{ // Protector.
			final Protector info = player.getProtectorInfo();
			final FastList<Player> kill = new FastList<>();
			final FastMap<Integer, Player> guards = getWorldProtector(worldId);
			kill.addAll(guards.values());
			guards.remove(player.getObjectId());
			if (info.getRank() > 0)
			{
				info.setRank(0);
				protectorBuff.endEffect(player);
				for (Player victim : World.getInstance().getWorldMap(worldId).getWorldMapInstanceById(player.getInstanceId()).getPlayersInside())
				{
					if (!player.getRace().equals(victim.getRace()))
					{
						PacketSendUtility.sendPacket(victim, new SM_SERIAL_KILLER(kill));
					}
				}
			}
		}
		else if (isEnemyWorld(player))
		{ // Conqueror.
			final Conqueror info = player.getConquerorInfo();
			final FastList<Player> kill = new FastList<>();
			final FastMap<Integer, Player> killers = getWorldConqueror(worldId);
			kill.addAll(killers.values());
			killers.remove(player.getObjectId());
			if (info.getRank() > 0)
			{
				info.setRank(0);
				conquerorBuff.endEffect(player);
				for (Player victim : World.getInstance().getWorldMap(worldId).getWorldMapInstanceById(player.getInstanceId()).getPlayersInside())
				{
					if (!player.getRace().equals(victim.getRace()))
					{
						PacketSendUtility.sendPacket(victim, new SM_SERIAL_KILLER(kill));
					}
				}
			}
		}
	}
	
	public void updateIcons(Player player)
	{
		if (!isEnemyWorld(player))
		{
			PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(getWorldProtector(player.getWorldId()).values()));
		}
		else if (isEnemyWorld(player))
		{
			PacketSendUtility.sendPacket(player, new SM_SERIAL_KILLER(getWorldConqueror(player.getWorldId()).values()));
		}
	}
	
	public void updateRanks(Player killer, Player victim)
	{
		if (!isEnemyWorld(killer))
		{ // Protector.
			final Protector info = killer.getProtectorInfo();
			if (killer.getLevel() >= (victim.getLevel() + levelDiff))
			{
				final int rank = getRanks(++info.victims);
				if (info.getRank() >= 1)
				{
					// You are now a Protector.
					PacketSendUtility.sendPacket(killer, SM_SYSTEM_MESSAGE.STR_MSG_GUARD_UP_1LEVEL);
				}
				if (info.getRank() >= 2)
				{
					// You are now an Indomitable Protector.
					PacketSendUtility.sendPacket(killer, SM_SYSTEM_MESSAGE.STR_MSG_GUARD_UP_2LEVEL);
				}
				if (info.getRank() >= 3)
				{
					// You are now a Valiant Protector.
					PacketSendUtility.sendPacket(killer, SM_SYSTEM_MESSAGE.STR_MSG_GUARD_UP_3LEVEL);
				}
				if (info.getRank() != rank)
				{
					info.setRank(rank);
					protectorBuff.applyRankEffect(killer, rank);
					final FastMap<Integer, Player> guards = getWorldProtector(killer.getWorldId());
					PacketSendUtility.sendPacket(killer, new SM_SERIAL_KILLER(true, info.getRank()));
					World.getInstance().getWorldMap(killer.getWorldId()).getWorldMapInstanceById(killer.getInstanceId()).doOnAllPlayers(observed ->
					{
						if (!killer.getRace().equals(observed.getRace()))
						{
							PacketSendUtility.sendPacket(observed, new SM_SERIAL_KILLER(guards.values()));
						}
					});
				}
				if (!protectors.containsKey(killer.getObjectId()))
				{
					protectors.put(killer.getObjectId(), info);
				}
			}
		}
		else if (isEnemyWorld(killer))
		{ // Conqueror.
			final Conqueror info = killer.getConquerorInfo();
			if (killer.getLevel() >= (victim.getLevel() + levelDiff))
			{
				final int rank = getRanks(++info.victims);
				if (info.getRank() >= 1)
				{
					// You are now a Conqueror.
					PacketSendUtility.sendPacket(killer, SM_SYSTEM_MESSAGE.STR_MSG_SLAYER_UP_1LEVEL);
				}
				if (info.getRank() >= 2)
				{
					// You are now an Furious Conqueror.
					PacketSendUtility.sendPacket(killer, SM_SYSTEM_MESSAGE.STR_MSG_SLAYER_UP_2LEVEL);
				}
				if (info.getRank() >= 3)
				{
					// You are now a Berserk Conqueror.
					PacketSendUtility.sendPacket(killer, SM_SYSTEM_MESSAGE.STR_MSG_SLAYER_UP_3LEVEL);
				}
				if (info.getRank() != rank)
				{
					info.setRank(rank);
					conquerorBuff.applyEffect(killer, rank);
					final FastMap<Integer, Player> killers = getWorldConqueror(killer.getWorldId());
					PacketSendUtility.sendPacket(killer, new SM_SERIAL_KILLER(true, info.getRank()));
					World.getInstance().getWorldMap(killer.getWorldId()).getWorldMapInstanceById(killer.getInstanceId()).doOnAllPlayers(observed ->
					{
						if (!killer.getRace().equals(observed.getRace()))
						{
							PacketSendUtility.sendPacket(observed, new SM_SERIAL_KILLER(killers.values()));
						}
					});
				}
				if (!conquerors.containsKey(killer.getObjectId()))
				{
					conquerors.put(killer.getObjectId(), info);
				}
			}
		}
	}
	
	int getRanks(int kills)
	{
		return kills > CustomConfig.PROTECTOR_CONQUEROR_2ND_RANK_KILLS ? 2 : kills > CustomConfig.PROTECTOR_CONQUEROR_1ST_RANK_KILLS ? 1 : 0;
	}
	
	public void onKillProtectorConqueror(Player killer, Player victim)
	{
		if (!isEnemyWorld(victim))
		{
			final Protector info = victim.getProtectorInfo();
			victim.getPosition().getWorldMapInstance().doOnAllPlayers(player ->
			{
				if (killer.getRace().equals(player.getRace()) && MathUtil.isIn3dRange(victim, player, 30))
				{
					SkillEngine.getInstance().applyEffectDirectly(buffId(killer, info), player, player, 0);
				}
			});
		}
		else if (isEnemyWorld(victim))
		{
			final Conqueror conqueror = victim.getConquerorInfo();
			victim.getPosition().getWorldMapInstance().doOnAllPlayers(player ->
			{
				if (killer.getRace().equals(player.getRace()) && MathUtil.isIn3dRange(victim, player, 30))
				{
					SkillEngine.getInstance().applyEffectDirectly(buffId(killer, conqueror), player, player, 0);
				}
			});
		}
	}
	
	public boolean isHandledWorld(int worldId)
	{
		return handledWorlds.containsKey(worldId);
	}
	
	public boolean isEnemyWorld(Player player)
	{
		if (handledWorlds.containsKey(player.getWorldId()))
		{
			final WorldType homeType = player.getRace().equals(Race.ASMODIANS) ? WorldType.ASMODIANS : WorldType.ELYOS;
			return !handledWorlds.get(player.getWorldId()).equals(homeType);
		}
		return false;
	}
	
	int buffId(Player player, Protector info)
	{
		if (info.getRank() > 0)
		{
			return player.getRace() == Race.ELYOS ? 8610 : 8611;
		}
		return 0;
	}
	
	int buffId(Player player, Conqueror info)
	{
		if (info.getRank() > 0)
		{
			return player.getRace() == Race.ELYOS ? 8610 : 8611;
		}
		return 0;
	}
	
	public static ProtectorConquerorService getInstance()
	{
		return ProtectorConquerorService.SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ProtectorConquerorService instance = new ProtectorConquerorService();
	}
}