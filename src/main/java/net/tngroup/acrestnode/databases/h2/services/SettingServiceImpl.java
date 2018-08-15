package net.tngroup.acrestnode.databases.h2.services;

import net.tngroup.acrestnode.databases.h2.models.Setting;
import net.tngroup.acrestnode.databases.h2.repositories.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingServiceImpl implements SettingService {

    private SettingRepository settingRepository;

    @Autowired
    public SettingServiceImpl(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Override
    public Setting getByName(String name) {
        return settingRepository.findByName(name).orElse(null);
    }

    @Override
    public void updateByName(String name, String value) {
        Setting setting = settingRepository.findByName(name).orElse(null);
        if (setting != null) {
            setting.setValue(value);
            settingRepository.save(setting);
        } else {
            settingRepository.save(new Setting(name, value));
        }
    }

    @Override
    public void add(Setting setting) {
        settingRepository.save(setting);
    }

    @Override
    public void deleteAll() {
        settingRepository.deleteAll();
    }
}
