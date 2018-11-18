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
@Table(name = "LEAGUE")
@NamedQueries({
    @NamedQuery(name = "League.findAll", query = "SELECT g FROM League g")
    , @NamedQuery(name = "League.findById", query = "SELECT g FROM League g WHERE g.id = :id")
    , @NamedQuery(name = "League.findByLeagueId", query = "SELECT g FROM League g WHERE g.leagueId = :leagueId")
    , @NamedQuery(name = "League.findByLeagueName", query = "SELECT g FROM League g WHERE g.leagueName = :leagueName")
})
public class League implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonbTransient
    @TableGenerator(
            name = "league_gen",
            table = "DB_PK_table",
            pkColumnValue = "league_seq",
            valueColumnName = "SEQ_TYPE"
    )
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "league_gen")
    @Column(name = "ID")
    private String id;

    // @Basic(optional = false)
    @NotNull
    @JsonbProperty("id")
    @Column(name = "LEAGUE_ID", unique = true)
    private Long leagueId;

    @NotNull
    @JsonbProperty("name")
    // @Basic(optional = false)
    @Size(min = 1, max = 40)
    @Column(name = "LEAGUE_NAME")
    private String leagueName; // Ordered Asc

//    @IndexedEmbedded
    @JsonbProperty("teams")
    @JoinColumn(name = "TEAM_ID")
    @OneToMany(mappedBy = "league", cascade = {CascadeType.ALL})
    @OrderBy("name ASC")
    private Collection<Team> leagueTeams = new ArrayList<>();

    @Version
    @JsonbTransient
    //@ColumnDefault("1")
    private long version;

    public League() {
    }

    public League(Long leagueId, String leagueName) {
        this.leagueId = leagueId;
        this.leagueName = leagueName;
    }

    public String getId() {
        return id;
    }

    public Long getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Long leagueId) {
        this.leagueId = leagueId;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }

    public Collection<Team> getLeagueTeams() {
        return leagueTeams;
    }

    public void setLeagueTeams(Collection<Team> leagueTeams) {
        this.leagueTeams = leagueTeams;
    }

    public void addTeam(Team team) {
        if (!this.leagueTeams.contains(team)) {
            this.leagueTeams.add(team);
        }
    }

    public void removeTeam(Team team) {
        if (!this.leagueTeams.contains(team)) {
            this.leagueTeams.remove(team);
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
        hash = 73 * hash + Objects.hashCode(this.id);
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
        final League other = (League) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "League{"
                + "leagueId=" + leagueId
                + ", leagueName=" + leagueName
                //+ ", leagueTeams=" + leagueTeams
                // + ", leagueSeasons=" + leagueSeasons
                + '}';
    }

}
