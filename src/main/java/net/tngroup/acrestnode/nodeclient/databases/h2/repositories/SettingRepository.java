package net.tngroup.acrestnode.nodeclient.databases.h2.repositories;

import net.tngroup.acrestnode.nodeclient.databases.h2.models.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Integer> {

    Optional<Setting> findByName(String name);

}
