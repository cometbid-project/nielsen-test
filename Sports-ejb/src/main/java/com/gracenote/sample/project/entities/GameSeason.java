/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
//import org.hibernate.annotations.ColumnDefault;

/**
 *
 * @author Gbenga
 */
@Entity
@Table(name = "SEASON")
@NamedQueries({
    @NamedQuery(name = "GameSeason.findAll", query = "SELECT g FROM GameSeason g")
    , @NamedQuery(name = "GameSeason.findById", query = "SELECT g FROM GameSeason g WHERE g.id = :id")   
    , @NamedQuery(name = "GameSeason.findBySeasonId", query = "SELECT g FROM GameSeason g WHERE g.seasonId = :seasonId")
    , @NamedQuery(name = "GameSeason.findBySeason", query = "SELECT g FROM GameSeason g WHERE g.season = :season")
})
public class GameSeason implements Serializable {

    @Id
    @JsonbTransient
    @TableGenerator(
            name = "GameSeason_gen",
            table = "DB_PK_table",
            pkColumnValue = "GameSeason_seq",
            valueColumnName = "SEQ_TYPE"
    )
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "GameSeason_gen")
    @Column(name = "ID")
    private String id;

    @NotBlank
    @JsonbProperty("seasonId")
    @Basic(optional = false)
    @NotNull
    @Column(name = "SEASON_ID", unique = true)
    private Long seasonId;

    @NotBlank
    @JsonbProperty("season")
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "SEASON")
    private String season;

    @Version
    @JsonbTransient
   // @ColumnDefault("1")
    private long version;

    public GameSeason() {
    }

    public GameSeason(Long seasonId, String season) {
        this.seasonId = seasonId;
        this.season = season;
    }

    public String getId() {
        return id;
    }

    public Long getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(Long seasonId) {
        this.seasonId = seasonId;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
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
        hash = 23 * hash + Objects.hashCode(this.id);
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
        final GameSeason other = (GameSeason) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GameSeason{"
                + "seasonId=" + seasonId
                + ", season=" + season
                + '}';
    }

}
