/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
//import org.hibernate.annotations.ColumnDefault;
//import org.hibernate.search.annotations.IndexedEmbedded;

/**
 *
 * @author Gbenga
 */
@Entity
@Table(name = "TEAM")
@NamedQueries({
    @NamedQuery(name = "Team.findAll", query = "SELECT g FROM Team g")
    , @NamedQuery(name = "Team.findById", query = "SELECT g FROM Team g WHERE g.id = :id")
    , @NamedQuery(name = "Team.findByName", query = "SELECT g FROM Team g WHERE g.name = :name")
    , @NamedQuery(name = "Team.findByTeamId", query = "SELECT g FROM Team g WHERE g.teamId = :teamId")
    , @NamedQuery(name = "Team.findAllByLeague", query = "SELECT g FROM Team g WHERE g.league.leagueId = :leagueId")
})
public class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonbTransient
    @TableGenerator(
            name = "TeamSeason_gen",
            table = "DB_PK_table",
            pkColumnValue = "TeamSeason_seq",
            valueColumnName = "SEQ_TYPE"
    )
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TeamSeason_gen")
    @Column(name = "ID")
    private String id;

    // @Basic(optional = false)
    @NotNull
    @JsonbProperty("teamId")
    @Column(name = "TEAM_ID", unique = true)
    private Long teamId;

    // @Basic(optional = false)
    @NotNull
    @JsonbProperty("name")
    @Size(min = 1, max = 30)
    @Column(name = "NAME")
    private String name; // Ordered Asc

    //@IndexedEmbedded
    @JsonbTransient
    @JoinColumn(name = "PLAYER_ID")
    @OneToMany(mappedBy = "team", cascade = {CascadeType.ALL})
    @OrderBy("lastName ASC")
    private Collection<Players> players = new ArrayList<>();

    // @Basic(optional = false)
    @NotNull
    @JsonbProperty("league")
    @JoinColumn(name = "LEAGUE_ID", nullable = false)
    @ManyToOne(optional = false)
    private League league;

    @Version
    @JsonbTransient
   // @ColumnDefault("1")
    private long version;

    public Team() {
    }

    public Team(String id) {
        this.id = id;
    }

    public Team(Long teamId, String name, League league) {
        this.teamId = teamId;
        this.name = name;
        this.league = league;
    }

    public String getId() {
        return id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public Collection<Players> getPlayers() {
        return players;
    }

    public void setPlayers(Collection<Players> players) {
        this.players = players;
    }

    public void addPlayers(Players player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    public void removePlayers(Players player) {
        if (players.contains(player)) {
            players.remove(player);
        }
    }

    public long getVersion() {
        return version;
    }

    protected void setVersion(long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Team other = (Team) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Team{"
                + "teamId=" + teamId
                + ", name=" + name
                + ", leagueId=" + league
                + '}';
    }

}
