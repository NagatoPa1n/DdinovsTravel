package uz.nagato.touragency.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.nagato.touragency.settings.entity.SettingGroup;
import uz.nagato.touragency.settings.repository.SettingGroupRepository;
import uz.nagato.touragency.tour.repository.TourRepository;

/**
 * One-shot data fix: tours created before the site switched to so'm carry {@code USD},
 * so the storefront still prints a dollar sign for them.
 * <p>
 * Only the label changes — amounts are left exactly as they are, on the understanding
 * that the figures are re-entered as real so'm prices in the admin. A marker in the
 * {@code pricing} settings group makes this run once and never again, so a currency an
 * editor deliberately sets on a tour afterwards is not flipped back on the next restart.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TourCurrencyRelabeller implements ApplicationRunner {

    private static final String SETTINGS_GROUP = "pricing";
    private static final String MARKER = "currencyRelabelledToUzs";
    private static final String CURRENCY = "UZS";

    private final TourRepository tourRepository;
    private final SettingGroupRepository settingGroupRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        SettingGroup pricing = settingGroupRepository.findById(SETTINGS_GROUP)
                .orElseGet(() -> new SettingGroup(SETTINGS_GROUP));
        if (Boolean.TRUE.equals(pricing.getData().get(MARKER))) {
            return;
        }

        int relabelled = tourRepository.relabelCurrency(CURRENCY);
        pricing.getData().put(MARKER, true);
        settingGroupRepository.save(pricing);

        log.info("Relabelled {} tour(s) to {} — amounts unchanged", relabelled, CURRENCY);
    }
}
