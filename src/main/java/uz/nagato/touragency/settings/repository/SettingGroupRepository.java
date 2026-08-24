package uz.nagato.touragency.settings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.nagato.touragency.settings.entity.SettingGroup;

public interface SettingGroupRepository extends JpaRepository<SettingGroup, String> {
}
