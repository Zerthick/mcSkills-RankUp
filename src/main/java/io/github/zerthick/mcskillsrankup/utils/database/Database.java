/*
 * Copyright (C) 2018  Zerthick
 *
 * This file is part of mcSkills-RankUp.
 *
 * mcSkills-RankUp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * mcSkills-RankUp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mcSkills-RankUp.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.zerthick.mcskillsrankup.utils.database;

import io.github.zerthick.mcskillsrankup.McSkillsRankUp;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Database {

    private SqlService sql;
    private String databaseUrl;
    private Logger logger;

    public Database(McSkillsRankUp plugin) throws SQLException {
        logger = plugin.getLogger();
        String configDir = plugin.getDefaultConfigDir().toString();
        databaseUrl = "jdbc:h2:" + configDir + "/playerdata;mode=MySQL";
        createDatabaseTables();
    }

    private DataSource getDataSource() throws SQLException {
        if (this.sql == null) {
            Optional<SqlService> sqlServiceOptional = Sponge.getServiceManager().provide(SqlService.class);
            this.sql = sqlServiceOptional.orElseThrow(SQLException::new);
        }
        return sql.getDataSource(databaseUrl);
    }

    private void createDatabaseTables() throws SQLException {
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS" +
                "  `playerData` (" +
                "  `playerUUID` VARCHAR(36) NOT NULL," +
                "  `ladderID` VARCHAR(20) NOT NULL," +
                "  `groupID` VARCHAR(20) NOT NULL," +
                "  PRIMARY KEY (`playerUUID`, `ladderID`));";

        Connection conn = getDataSource().getConnection();
        Statement createStatement = conn.createStatement();
        createStatement.execute(sqlCreateTable);
        logger.info("Database Connection Established!");
    }

    public Optional<Map<String, String>> getPlayerData(UUID playerUUID) {

        Map<String, String> playerGroups = new HashMap<>();

        // Create connection and statement
        try (
                Connection conn = getDataSource().getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT ladderID, groupID " +
                        "FROM playerData " +
                        "WHERE playerUUID = ?")
        ) {
            // set uuid to provided uuid
            ps.setString(1, String.valueOf(playerUUID));
            ResultSet rs = ps.executeQuery();

            // check if no data
            if (!rs.isBeforeFirst()) {
                return Optional.empty();
            }

            // for each result
            while (rs.next()) {
                String ladderID = rs.getString("ladderID");
                String groupID = rs.getString("groupID");
                playerGroups.put(ladderID, groupID);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        // return skills map
        return Optional.of(playerGroups);
    }

    public void savePlayerData(UUID playerUUID, Map<String, String> playerGroups) {

        try (
                Connection conn = getDataSource().getConnection();
                PreparedStatement ps = conn.prepareStatement("INSERT INTO playerData SET playerUUID = ?, ladderID = ?, groupID = ?" +
                        "ON DUPLICATE KEY UPDATE " +
                        "groupID = VALUES(groupID)")
        ) {
            playerGroups.forEach((k, v) -> {
                try {
                    ps.setString(1, playerUUID.toString());
                    ps.setString(2, k);
                    ps.setString(3, v);
                    ps.addBatch();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
            });
            ps.executeBatch();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}