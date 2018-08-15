package net.tngroup.acrestnode.databases.h2.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@Entity
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String value;

    public Setting(String name, String value) {
        this.name = name;
        this. value = value;
    }

}
