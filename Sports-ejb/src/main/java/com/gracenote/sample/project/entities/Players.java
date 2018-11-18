/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
//import org.hibernate.annotations.ColumnDefault;

/**
 *
 * @author Gbenga
 */
@Entity
@Table(name = "PLAYERS")
@NamedQueries({
    @NamedQuery(name = "Players.findAll", query = "SELECT g FROM Players g")
    , @NamedQuery(name = "Players.findById", query = "SELECT g FROM Players g WHERE g.id = :id")
    , @NamedQuery(name = "Players.findByFirstName", query = "SELECT g FROM Players g WHERE g.firstName = :firstName")
    , @NamedQuery(name = "Players.findByLastName", query = "SELECT g FROM Players g WHERE g.lastName = :lastName")
    , @NamedQuery(name = "Players.findAllByTeam", query = "SELECT g FROM Players g WHERE g.team.teamId = :teamId")
    , @NamedQuery(name = "Players.findByPlayerId", query = "SELECT g FROM Players g WHERE g.playerId = :playerId")
})
public class Players implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonbTransient
    @TableGenerator(
            name = "Players_gen",
            table = "DB_PK_table",
            pkColumnValue = "Players_seq",
            valueColumnName = "SEQ_TYPE"
    )
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Players_gen")
    @Column(name = "ID")
    private String id;

    //  @Basic(optional = false)
    @NotNull
    @JsonbProperty("playerId")
    @Column(name = "PLAYER_ID", unique = true)
    private Long playerId;

    // @Basic(optional = false)
    @NotNull
    @JsonbProperty("firstName")
    @Size(min = 1, max = 30)
    @Column(name = "FIRST_NAME")
    private String firstName;

    //@Basic(optional = false)
    @NotNull
    @JsonbProperty("lastName")
    @Size(min = 1, max = 30)
    @Column(name = "LAST_NAME")
    private String lastName; // Ordered Asc

    // @Basic(optional = false)
    @JsonbProperty("BirthDate")
    @Column(name = "BIRTH_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthDate;

    @NotNull
    @JsonbProperty("teamId")
    @JoinColumn(name = "TEAM_ID", nullable = false)
    @ManyToOne(optional = false)
    private Team team;

    @Version
    @JsonbTransient
   // @ColumnDefault("1")
    private long version;

    public Players() {
    }

    public Players(Long playerId, String firstName, String lastName, Date birthDate) {
        this.playerId = playerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    public String getId() {
        return id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public long getVersion() {
        return version;
    }

    protected void setVersion(long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.id);
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
        final Players other = (Players) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Players{"
                + "id=" + id
                + ", playerId=" + playerId
                + ", firstName=" + firstName
                + ", lastName=" + lastName
                + ", birthDate=" + birthDate
                + '}';
    }

}
