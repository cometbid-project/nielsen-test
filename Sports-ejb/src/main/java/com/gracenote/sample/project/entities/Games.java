/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
//import org.hibernate.annotations.ColumnDefault;
//import org.hibernate.search.annotations.IndexedEmbedded;

/**
 *
 * @author Gbenga
 */
@Entity
@Table(name = "GAMES")
@NamedQueries({
    @NamedQuery(name = "Games.findAll", query = "SELECT g FROM Games g")
    , @NamedQuery(name = "Games.findById", query = "SELECT g FROM Games g WHERE g.id = :id")
    , @NamedQuery(name = "Games.findBySeasonId", query = "SELECT g FROM Games g WHERE g.season.seasonId = :seasonId")
    , @NamedQuery(name = "Games.findAllByLeagueAndSeason", 
            query = "SELECT g FROM Games g WHERE g.season.seasonId = :seasonId and g.league.leagueId = :leagueId")
    , @NamedQuery(name = "Games.findByGameId", query = "SELECT g FROM Games g WHERE g.gameId = :gameId")
    , @NamedQuery(name = "Games.findByGameDate", query = "SELECT g FROM Games g WHERE g.gameDate = :gameDate")
    , @NamedQuery(name = "Games.findByGoals", query = "SELECT g FROM Games g WHERE g.goals = :goals")
    , @NamedQuery(name = "Games.findByPenalty", query = "SELECT g FROM Games g WHERE g.penalty = :penalty")
})
public class Games implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonbTransient
    @TableGenerator(
            name = "Games_gen",
            table = "DB_PK_table",
            pkColumnValue = "Games_seq",
            valueColumnName = "SEQ_TYPE"
    )
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Games_gen")
    @Column(name = "ID")
    private String id;

    @NotNull
    @JsonbProperty("leagueId")
    @JoinColumn(name = "LEAGUE_ID", nullable = false)
    @ManyToOne(optional = false)
    private League league;

    @NotNull
    @JsonbProperty("seasonId")
    @JoinColumn(name = "SEASON_ID", nullable = false)
    @ManyToOne(optional = false)
    private GameSeason season;

    @JsonbProperty("gameId")
    @Column(name = "GAME_ID", unique = true)
    private Long gameId;

    @JsonbProperty("gameDate")
    @Column(name = "GAME_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date gameDate;

    @JsonbProperty("goals")
    @Column(name = "GOALS")
    private Integer goals;

    @JsonbProperty("penalties")
    @Column(name = "PENALTY")
    private Integer penalty;

    @JsonbProperty("outcome")
    @Column(name = "FINAL_RESULT")
    private Integer finalResult;

    //@IndexedEmbedded
    @JsonbProperty("teams")
    @JoinColumn(name = "TEAMS", nullable = false)
    @OneToMany(cascade = {CascadeType.ALL})
    private Collection<Team> teams = new ArrayList<>();

    @Version
    @JsonbTransient
  //  @ColumnDefault("1")
    private long version;

    public Games() {
    }

    public Games(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public GameSeason getSeason() {
        return season;
    }

    public void setSeason(GameSeason season) {
        this.season = season;
    }

    public Date getGameDate() {
        return gameDate;
    }

    public void setGameDate(Date gameDate) {
        this.gameDate = gameDate;
    }

    public Integer getGoals() {
        return goals;
    }

    public void setGoals(Integer goals) {
        this.goals = goals;
    }

    public Integer getPenalty() {
        return penalty;
    }

    public void setPenalty(Integer penalty) {
        this.penalty = penalty;
    }

    public Collection<Team> getTeams() {
        return teams;
    }

    public void setTeams(Collection<Team> teams) {
        this.teams = teams;
    }

    public void addTeam(Team team) {
        if (!this.teams.contains(team)) {
            this.teams.add(team);
        }
    }

    public void removeTeam(Team team) {
        if (!this.teams.contains(team)) {
            this.teams.remove(team);
        }
    }

    public Integer getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(Integer finalResult) {
        this.finalResult = finalResult;
    }

    public long getVersion() {
        return version;
    }

    protected void setVersion(long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Games)) {
            return false;
        }
        Games other = (Games) object;
        return !((this.id == null && other.id != null)
                || (this.id != null && !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        return "com.gracenote.sample.project.entities.Games[ id=" + id + " ]";
    }

}
