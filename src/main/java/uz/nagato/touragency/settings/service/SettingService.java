package uz.nagato.touragency.settings.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.nagato.touragency.common.exception.BadRequestException;
import uz.nagato.touragency.settings.entity.SettingGroup;
import uz.nagato.touragency.settings.repository.SettingGroupRepository;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingService {

    /**
     * Groups the admin UI knows about. Restricted so a typo in a client request
     * cannot quietly create an orphan settings row that nothing ever reads.
     */
    private static final Set<String> KNOWN_GROUPS = Set.of("general", "contact", "social");

    private final SettingGroupRepository repository;

    /** Returns an empty map rather than 404 so a never-saved group still renders its form. */
    public Map<String, Object> find(String group) {
        String key = normalise(group);
        return repository.findById(key)
                .map(SettingGroup::getData)
                .orElseGet(LinkedHashMap::new);
    }

    /**
     * Merges the incoming keys over what is stored, so a screen that only submits
     * the fields it owns cannot wipe the rest of the group.
     */
    @Transactional
    public Map<String, Object> save(String group, Map<String, Object> values) {
        String key = normalise(group);
        SettingGroup entity = repository.findById(key).orElseGet(() -> new SettingGroup(key));

        Map<String, Object> merged = new LinkedHashMap<>(entity.getData());
        if (values != null) {
            merged.putAll(values);
        }
        entity.setData(merged);

        return repository.save(entity).getData();
    }

    private String normalise(String group) {
        String key = group == null ? "" : group.trim().toLowerCase(Locale.ENGLISH);
        if (!KNOWN_GROUPS.contains(key)) {
            throw new BadRequestException("Unknown settings group: " + group);
        }
        return key;
    }
}
