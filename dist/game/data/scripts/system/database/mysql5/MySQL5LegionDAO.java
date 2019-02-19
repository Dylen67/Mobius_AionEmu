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
package system.database.mysql5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.dao.LegionDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionEmblem;
import com.aionemu.gameserver.model.team.legion.LegionEmblemType;
import com.aionemu.gameserver.model.team.legion.LegionHistory;
import com.aionemu.gameserver.model.team.legion.LegionHistoryType;
import com.aionemu.gameserver.model.team.legion.LegionJoinRequest;
import com.aionemu.gameserver.model.team.legion.LegionTerritory;
import com.aionemu.gameserver.model.team.legion.LegionWarehouse;

import javolution.util.FastList;

/**
 * @author Simple
 * @modified cura
 */

public class MySQL5LegionDAO extends LegionDAO
{
	
	/** Logger */
	private static final Logger log = LoggerFactory.getLogger(MySQL5LegionDAO.class);
	/** Legion Queries */
	private static final String INSERT_LEGION_QUERY = "INSERT INTO legions(id, `name`) VALUES (?, ?)";
	private static final String SELECT_LEGION_QUERY1 = "SELECT * FROM legions WHERE id=?";
	private static final String SELECT_LEGION_QUERY2 = "SELECT * FROM legions WHERE name=?";
	private static final String DELETE_LEGION_QUERY = "DELETE FROM legions WHERE id = ?";
	private static final String UPDATE_LEGION_QUERY = "UPDATE legions SET name=?, level=?, contribution_points=?, deputy_permission=?, centurion_permission=?, legionary_permission=?, volunteer_permission=?, disband_time=?, description=?, joinType=?, minJoinLevel=?, territory=? WHERE id=?";
	/** Legion Description Queries **/
	private static final String UPDATE_LEGION_DESCRIPTION_QUERY = "UPDATE legions SET description=?, joinType=?, minJoinLevel=? WHERE id=?";
	/** Announcement Queries **/
	private static final String INSERT_ANNOUNCEMENT_QUERY = "INSERT INTO legion_announcement_list(`legion_id`, `announcement`, `date`) VALUES (?, ?, ?)";
	private static final String SELECT_ANNOUNCEMENTLIST_QUERY = "SELECT * FROM legion_announcement_list WHERE legion_id=? ORDER BY date ASC LIMIT 0,7;";
	private static final String DELETE_ANNOUNCEMENT_QUERY = "DELETE FROM legion_announcement_list WHERE legion_id = ? AND date = ?";
	/** Emblem Queries **/
	private static final String INSERT_EMBLEM_QUERY = "INSERT INTO legion_emblems(legion_id, emblem_id, color_r, color_g, color_b, emblem_type, emblem_data) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_EMBLEM_QUERY = "UPDATE legion_emblems SET emblem_id=?, color_r=?, color_g=?, color_b=?, emblem_type=?, emblem_data=? WHERE legion_id=?";
	private static final String SELECT_EMBLEM_QUERY = "SELECT * FROM legion_emblems WHERE legion_id=?";
	/** Storage Queries **/
	private static final String SELECT_STORAGE_QUERY = "SELECT `item_unique_id`, `item_id`, `item_count`, `item_color`, `color_expires`, `item_creator`, `expire_time`, `activation_count`, `is_equiped`, `slot`, `enchant`, `enchant_bonus`, `item_skin`, `fusioned_item`, `optional_socket`, `optional_fusion_socket`, `charge`, `rnd_bonus`, `rnd_count`, `wrappable_count`, `is_packed`, `tempering_level`, `is_topped`, `strengthen_skill`, `skin_skill`, `luna_reskin`, `reduction_level`, `is_seal` FROM `inventory` WHERE `item_owner`=? AND `item_location`=? AND `is_equiped`=?";
	/** History Queries **/
	private static final String INSERT_HISTORY_QUERY = "INSERT INTO legion_history(`legion_id`, `date`, `history_type`, `name`, `tab_id`, `description`) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String SELECT_HISTORY_QUERY = "SELECT * FROM `legion_history` WHERE legion_id=? ORDER BY date ASC;";
	private static final String CLEAR_LEGION_SIEGE = "UPDATE siege_locations SET legion_id=0 WHERE legion_id=?";
	/** Requests Queries **/
	private static final String INSERT_RECRUIT_LIST_QUERY = "INSERT INTO legion_join_requests(`legionId`, `playerId`, `playerName`, `playerClassId`, `playerRaceId`, `playerLevel`, `playerGenderId`, `joinRequestMsg`, `date`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String SELECT_RECRUIT_LIST_QUERY = "SELECT * FROM legion_join_requests WHERE legionId=? ORDER BY date ASC;";
	private static final String DELETE_RECRUIT_LIST_QUERY = "DELETE FROM legion_join_requests WHERE legionId = ? AND playerId = ?";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isNameUsed(String name)
	{
		final PreparedStatement s = DB.prepareStatement("SELECT count(id) as cnt FROM legions WHERE ? = legions.name");
		try
		{
			s.setString(1, name);
			final ResultSet rs = s.executeQuery();
			rs.next();
			return rs.getInt("cnt") > 0;
		}
		catch (SQLException e)
		{
			log.error("Can't check if name " + name + ", is used, returning possitive result", e);
			return true;
		}
		finally
		{
			DB.close(s);
		}
	}
	
	@Override
	public Collection<Integer> getLegionIdsWithTerritories()
	{
		final Collection<Integer> legionIds = new ArrayList<>();
		final PreparedStatement s = DB.prepareStatement("SELECT id FROM legions WHERE territory > 0");
		try
		{
			final ResultSet rs = s.executeQuery();
			while (rs.next())
			{
				legionIds.add(rs.getInt("id"));
			}
		}
		catch (SQLException e)
		{
			log.error("Error on getting legions with territoryId... Error: ", e);
		}
		finally
		{
			DB.close(s);
		}
		return legionIds;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean saveNewLegion(Legion legion)
	{
		final boolean success = DB.insertUpdate(INSERT_LEGION_QUERY, preparedStatement ->
		{
			log.debug("[DAO: MySQL5LegionDAO] saving new legion: " + legion.getLegionId() + " " + legion.getLegionName());
			
			preparedStatement.setInt(1, legion.getLegionId());
			preparedStatement.setString(2, legion.getLegionName());
			preparedStatement.execute();
		});
		return success;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeLegion(Legion legion)
	{
		DB.insertUpdate(UPDATE_LEGION_QUERY, stmt ->
		{
			log.debug("[DAO: MySQL5LegionDAO] storing player " + legion.getLegionId() + " " + legion.getLegionName());
			
			stmt.setString(1, legion.getLegionName());
			stmt.setInt(2, legion.getLegionLevel());
			stmt.setLong(3, legion.getContributionPoints());
			stmt.setInt(4, legion.getDeputyPermission());
			stmt.setInt(5, legion.getCenturionPermission());
			stmt.setInt(6, legion.getLegionaryPermission());
			stmt.setInt(7, legion.getVolunteerPermission());
			stmt.setInt(8, legion.getDisbandTime());
			stmt.setString(9, legion.getLegionDescription());
			stmt.setInt(10, legion.getLegionJoinType());
			stmt.setInt(11, legion.getMinLevel());
			stmt.setInt(12, ((legion.getTerritory() != null) && (legion.getTerritory().getId() > 0)) ? legion.getTerritory().getId() : 0);
			stmt.setInt(13, legion.getLegionId());
			if (!legion.getJoinRequestMap().isEmpty())
			{
				for (LegionJoinRequest ljr : legion.getJoinRequestMap().values())
				{
					storeLegionJoinRequest(ljr);
				}
			}
			stmt.execute();
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Legion loadLegion(String legionName)
	{
		final Legion legion = new Legion();
		final boolean success = DB.select(SELECT_LEGION_QUERY2, new ParamReadStH()
		{
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException
			{
				stmt.setString(1, legionName);
			}
			
			@Override
			public void handleRead(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					legion.setLegionName(legionName);
					legion.setLegionId(resultSet.getInt("id"));
					legion.setLegionLevel(resultSet.getInt("level"));
					legion.addContributionPoints(resultSet.getLong("contribution_points"));
					final int terrId = resultSet.getInt("territory");
					final LegionTerritory t = new LegionTerritory(terrId);
					if (terrId > 0)
					{
						t.setLegionId(legion.getLegionId());
						t.setLegionName(legion.getLegionName());
					}
					legion.setTerritory(t);
					legion.setLegionPermissions(resultSet.getShort("deputy_permission"), resultSet.getShort("centurion_permission"), resultSet.getShort("legionary_permission"), resultSet.getShort("volunteer_permission"));
					legion.setDisbandTime(resultSet.getInt("disband_time"));
				}
			}
		});
		log.debug("[MySQL5LegionDAO] Loaded " + legion.getLegionId() + " legion.");
		return (success && (legion.getLegionId() != 0)) ? legion : null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Legion loadLegion(int legionId)
	{
		final Legion legion = new Legion();
		final boolean success = DB.select(SELECT_LEGION_QUERY1, new ParamReadStH()
		{
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException
			{
				stmt.setInt(1, legionId);
			}
			
			@Override
			public void handleRead(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					legion.setLegionId(legionId);
					legion.setLegionName(resultSet.getString("name"));
					legion.setLegionLevel(resultSet.getInt("level"));
					legion.addContributionPoints(resultSet.getLong("contribution_points"));
					final int terrId = resultSet.getInt("territory");
					final LegionTerritory t = new LegionTerritory(terrId);
					if (terrId > 0)
					{
						t.setLegionId(legion.getLegionId());
						t.setLegionName(legion.getLegionName());
					}
					legion.setTerritory(t);
					legion.setLegionPermissions(resultSet.getShort("deputy_permission"), resultSet.getShort("centurion_permission"), resultSet.getShort("legionary_permission"), resultSet.getShort("volunteer_permission"));
					legion.setDescription(resultSet.getString("description"));
					legion.setJoinType(resultSet.getInt("joinType"));
					legion.setMinJoinLevel(resultSet.getInt("minJoinLevel"));
					legion.setDisbandTime(resultSet.getInt("disband_time"));
					for (LegionJoinRequest ljr : loadLegionJoinRequests(legion.getLegionId()))
					{
						legion.addJoinRequest(ljr);
					}
				}
			}
		});
		log.debug("[MySQL5LegionDAO] Loaded " + legion.getLegionId() + " legion.");
		return (success && (legion.getLegionName() != "")) ? legion : null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteLegion(int legionId)
	{
		PreparedStatement statement = DB.prepareStatement(DELETE_LEGION_QUERY);
		try
		{
			statement.setInt(1, legionId);
		}
		catch (SQLException e)
		{
			log.error("deleteLegion #1", e);
		}
		DB.executeUpdateAndClose(statement);
		
		statement = DB.prepareStatement(CLEAR_LEGION_SIEGE);
		try
		{
			statement.setInt(1, legionId);
		}
		catch (SQLException e)
		{
			log.error("deleteLegion #2", e);
		}
		DB.executeUpdateAndClose(statement);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] getUsedIDs()
	{
		final PreparedStatement statement = DB.prepareStatement("SELECT id FROM legions", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		
		try
		{
			final ResultSet rs = statement.executeQuery();
			rs.last();
			final int count = rs.getRow();
			rs.beforeFirst();
			final int[] ids = new int[count];
			for (int i = 0; i < count; i++)
			{
				rs.next();
				ids[i] = rs.getInt("id");
			}
			return ids;
		}
		catch (SQLException e)
		{
			log.error("Can't get list of id's from legions table", e);
		}
		finally
		{
			DB.close(statement);
		}
		
		return new int[0];
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports(String s, int i, int i1)
	{
		return MySQL5DAOUtils.supports(s, i, i1);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public TreeMap<Timestamp, String> loadAnnouncementList(int legionId)
	{
		final TreeMap<Timestamp, String> announcementList = new TreeMap<>();
		
		final boolean success = DB.select(SELECT_ANNOUNCEMENTLIST_QUERY, new ParamReadStH()
		{
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException
			{
				stmt.setInt(1, legionId);
			}
			
			@Override
			public void handleRead(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					final String message = resultSet.getString("announcement");
					final Timestamp date = resultSet.getTimestamp("date");
					
					announcementList.put(date, message);
				}
			}
		});
		
		log.debug("[MySQL5LegionDAO] Loaded announcementList " + legionId + " legion.");
		
		return success ? announcementList : null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean saveNewAnnouncement(int legionId, Timestamp currentTime, String message)
	{
		final boolean success = DB.insertUpdate(INSERT_ANNOUNCEMENT_QUERY, preparedStatement ->
		{
			log.debug("[DAO: MySQL5LegionDAO] saving new announcement.");
			
			preparedStatement.setInt(1, legionId);
			preparedStatement.setString(2, message);
			preparedStatement.setTimestamp(3, currentTime);
			preparedStatement.execute();
		});
		return success;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeAnnouncement(int legionId, Timestamp unixTime)
	{
		final PreparedStatement statement = DB.prepareStatement(DELETE_ANNOUNCEMENT_QUERY);
		try
		{
			statement.setInt(1, legionId);
			statement.setTimestamp(2, unixTime);
		}
		catch (SQLException e)
		{
			log.error("Some crap, can't set int parameter to PreparedStatement", e);
		}
		DB.executeUpdateAndClose(statement);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeLegionEmblem(int legionId, LegionEmblem legionEmblem)
	{
		if (!validEmblem(legionEmblem))
		{
			return;
		}
		if (!(checkEmblem(legionId)))
		{
			createLegionEmblem(legionId, legionEmblem);
		}
		else
		{
			switch (legionEmblem.getPersistentState())
			{
				case UPDATE_REQUIRED:
				{
					updateLegionEmblem(legionId, legionEmblem);
					break;
				}
				case NEW:
				{
					createLegionEmblem(legionId, legionEmblem);
					break;
				}
			}
		}
		legionEmblem.setPersistentState(PersistentState.UPDATED);
	}
	
	private boolean validEmblem(LegionEmblem legionEmblem)
	{
		return (legionEmblem.getEmblemType().toString().equals("CUSTOM") && (legionEmblem.getCustomEmblemData() == null)) ? false : true;
	}
	
	/**
	 * @param legionid
	 * @return
	 */
	public boolean checkEmblem(int legionid)
	{
		final PreparedStatement st = DB.prepareStatement(SELECT_EMBLEM_QUERY);
		try
		{
			st.setInt(1, legionid);
			
			final ResultSet rs = st.executeQuery();
			
			if (rs.next())
			{
				return true;
			}
		}
		catch (SQLException e)
		{
			log.error("Can't check " + legionid + " legion emblem: ", e);
		}
		finally
		{
			DB.close(st);
		}
		return false;
	}
	
	/**
	 * @param legionId
	 * @param legionEmblem
	 */
	private void createLegionEmblem(int legionId, LegionEmblem legionEmblem)
	{
		DB.insertUpdate(INSERT_EMBLEM_QUERY, preparedStatement ->
		{
			preparedStatement.setInt(1, legionId);
			preparedStatement.setInt(2, legionEmblem.getEmblemId());
			preparedStatement.setInt(3, legionEmblem.getColor_r());
			preparedStatement.setInt(4, legionEmblem.getColor_g());
			preparedStatement.setInt(5, legionEmblem.getColor_b());
			preparedStatement.setString(6, legionEmblem.getEmblemType().toString());
			preparedStatement.setBytes(7, legionEmblem.getCustomEmblemData());
			preparedStatement.execute();
		});
	}
	
	/**
	 * @param legionId
	 * @param legionEmblem
	 */
	private void updateLegionEmblem(int legionId, LegionEmblem legionEmblem)
	{
		DB.insertUpdate(UPDATE_EMBLEM_QUERY, stmt ->
		{
			stmt.setInt(1, legionEmblem.getEmblemId());
			stmt.setInt(2, legionEmblem.getColor_r());
			stmt.setInt(3, legionEmblem.getColor_g());
			stmt.setInt(4, legionEmblem.getColor_b());
			stmt.setString(5, legionEmblem.getEmblemType().toString());
			stmt.setBytes(6, legionEmblem.getCustomEmblemData());
			stmt.setInt(7, legionId);
			stmt.execute();
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public LegionEmblem loadLegionEmblem(int legionId)
	{
		final LegionEmblem legionEmblem = new LegionEmblem();
		
		DB.select(SELECT_EMBLEM_QUERY, new ParamReadStH()
		{
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException
			{
				stmt.setInt(1, legionId);
			}
			
			@Override
			public void handleRead(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					legionEmblem.setEmblem(resultSet.getInt("emblem_id"), resultSet.getInt("color_r"), resultSet.getInt("color_g"), resultSet.getInt("color_b"), LegionEmblemType.valueOf(resultSet.getString("emblem_type")), resultSet.getBytes("emblem_data"));
				}
			}
		});
		legionEmblem.setPersistentState(PersistentState.UPDATED);
		
		return legionEmblem;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public LegionWarehouse loadLegionStorage(Legion legion)
	{
		final LegionWarehouse inventory = new LegionWarehouse(legion);
		final int legionId = legion.getLegionId();
		final int storage = StorageType.LEGION_WAREHOUSE.getId();
		final int equipped = 0;
		
		DB.select(SELECT_STORAGE_QUERY, new ParamReadStH()
		{
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException
			{
				stmt.setInt(1, legionId);
				stmt.setInt(2, storage);
				stmt.setInt(3, equipped);
			}
			
			@Override
			public void handleRead(ResultSet rset) throws SQLException
			{
				while (rset.next())
				{
					final int itemUniqueId = rset.getInt("item_unique_id");
					final int itemId = rset.getInt("item_id");
					final long itemCount = rset.getLong("item_count");
					final int itemColor = rset.getInt("item_color");
					final int colorExpireTime = rset.getInt("color_expires");
					final String itemCreator = rset.getString("item_creator");
					final int expireTime = rset.getInt("expire_time");
					final int activationCount = rset.getInt("activation_count");
					final int isEquiped = rset.getInt("is_equiped");
					final int slot = rset.getInt("slot");
					final int enchant = rset.getInt("enchant");
					final int enchantBonus = rset.getInt("enchant_bonus");
					final int itemSkin = rset.getInt("item_skin");
					final int fusionedItem = rset.getInt("fusioned_item");
					final int optionalSocket = rset.getInt("optional_socket");
					final int optionalFusionSocket = rset.getInt("optional_fusion_socket");
					final int charge = rset.getInt("charge");
					final Integer randomNumber = rset.getInt("rnd_bonus");
					final int rndCount = rset.getInt("rnd_count");
					final int wrappingCount = rset.getInt("wrappable_count");
					final int isPacked = rset.getInt("is_packed");
					final int temperingLevel = rset.getInt("tempering_level");
					final int isTopped = rset.getInt("is_topped");
					final int strengthenSkill = rset.getInt("strengthen_skill");
					final int skinSkill = rset.getInt("skin_skill");
					final int isLunaReskin = rset.getInt("luna_reskin");
					final int reductionLevel = rset.getInt("reduction_level");
					final int unSeal = rset.getInt("is_seal");
					final Item item = new Item(itemUniqueId, itemId, itemCount, itemColor, colorExpireTime, itemCreator, expireTime, activationCount, isEquiped == 1, false, slot, storage, enchant, enchantBonus, itemSkin, fusionedItem, optionalSocket, optionalFusionSocket, charge, randomNumber, rndCount, wrappingCount, isPacked == 1, temperingLevel, isTopped == 1, strengthenSkill, skinSkill, isLunaReskin == 1, reductionLevel, unSeal);
					item.setPersistentState(PersistentState.UPDATED);
					inventory.onLoadHandler(item);
				}
			}
		});
		return inventory;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadLegionHistory(Legion legion)
	{
		
		final Collection<LegionHistory> history = legion.getLegionHistory();
		
		DB.select(SELECT_HISTORY_QUERY, new ParamReadStH()
		{
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException
			{
				stmt.setInt(1, legion.getLegionId());
			}
			
			@Override
			public void handleRead(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					history.add(new LegionHistory(LegionHistoryType.valueOf(resultSet.getString("history_type")), resultSet.getString("name"), resultSet.getTimestamp("date"), resultSet.getInt("tab_id"), resultSet.getString("description")));
				}
			}
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean saveNewLegionHistory(int legionId, LegionHistory legionHistory)
	{
		final boolean success = DB.insertUpdate(INSERT_HISTORY_QUERY, preparedStatement ->
		{
			preparedStatement.setInt(1, legionId);
			preparedStatement.setTimestamp(2, legionHistory.getTime());
			preparedStatement.setString(3, legionHistory.getLegionHistoryType().toString());
			preparedStatement.setString(4, legionHistory.getName());
			preparedStatement.setInt(5, legionHistory.getTabId());
			preparedStatement.setString(6, legionHistory.getDescription());
			preparedStatement.execute();
		});
		return success;
	}
	
	@Override
	public void updateLegionDescription(Legion legion)
	{
		DB.insertUpdate(UPDATE_LEGION_DESCRIPTION_QUERY, stmt ->
		{
			stmt.setString(1, legion.getLegionDescription());
			stmt.setInt(2, legion.getLegionJoinType());
			stmt.setInt(3, legion.getMinLevel());
			stmt.setInt(4, legion.getLegionId());
			stmt.execute();
		});
	}
	
	@Override
	public void storeLegionJoinRequest(LegionJoinRequest legionJoinRequest)
	{
		DB.insertUpdate(INSERT_RECRUIT_LIST_QUERY, stmt ->
		{
			stmt.setInt(1, legionJoinRequest.getLegionId());
			stmt.setInt(2, legionJoinRequest.getPlayerId());
			stmt.setString(3, legionJoinRequest.getPlayerName());
			stmt.setInt(4, legionJoinRequest.getPlayerClass());
			stmt.setInt(5, legionJoinRequest.getRace());
			stmt.setInt(6, legionJoinRequest.getLevel());
			stmt.setInt(7, legionJoinRequest.getGenderId());
			stmt.setString(8, legionJoinRequest.getMsg());
			stmt.setTimestamp(9, legionJoinRequest.getDate());
			stmt.execute();
		});
	}
	
	@Override
	public FastList<LegionJoinRequest> loadLegionJoinRequests(int legionId)
	{
		final FastList<LegionJoinRequest> requestList = new FastList<>();
		DB.select(SELECT_RECRUIT_LIST_QUERY, new ParamReadStH()
		{
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException
			{
				stmt.setInt(1, legionId);
			}
			
			@Override
			public void handleRead(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					final LegionJoinRequest ljr = new LegionJoinRequest();
					ljr.setLegionId(resultSet.getInt("legionId"));
					ljr.setPlayerId(resultSet.getInt("playerId"));
					ljr.setPlayerName(resultSet.getString("playerName"));
					ljr.setPlayerClass(resultSet.getInt("playerClassId"));
					ljr.setRace(resultSet.getInt("playerRaceId"));
					ljr.setLevel(resultSet.getInt("playerLevel"));
					ljr.setGenderId(resultSet.getInt("playerGenderId"));
					ljr.setDate(resultSet.getTimestamp("date"));
					requestList.add(ljr);
				}
			}
		});
		return requestList;
	}
	
	@Override
	public void deleteLegionJoinRequest(int legionId, int playerId)
	{
		final PreparedStatement statement = DB.prepareStatement(DELETE_RECRUIT_LIST_QUERY);
		try
		{
			statement.setInt(1, legionId);
			statement.setInt(2, playerId);
		}
		catch (SQLException e)
		{
		}
		DB.executeUpdateAndClose(statement);
	}
	
	@Override
	public void deleteLegionJoinRequest(LegionJoinRequest ljr)
	{
		deleteLegionJoinRequest(ljr.getLegionId(), ljr.getPlayerId());
	}
}