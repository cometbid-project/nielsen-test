/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.ut.vo;

import java.util.Collection;
import javax.validation.Valid;

/**
 *
 * @author Gbenga
 */
public class CountryContainer {

    private Collection<@Valid JsonEntityBuilder> countryList;

    public Collection<JsonEntityBuilder> getCountryList() {
        return countryList;
    }

    public void setCountryList(Collection<JsonEntityBuilder> countryList) {
        this.countryList = countryList;
    }

}
