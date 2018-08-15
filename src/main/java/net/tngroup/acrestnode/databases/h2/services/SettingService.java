package net.tngroup.acrestnode.databases.h2.services;


import net.tngroup.acrestnode.databases.h2.models.Setting;

public interface SettingService {

    Setting getByName(String name);

    void updateByName(String name, String value);

    void add(Setting setting);

    void deleteAll();

}
