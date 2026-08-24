package uz.nagato.touragency.common.jpa;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import uz.nagato.touragency.tour.entity.ItineraryDay;

import java.util.List;

/** Day-by-day itinerary stored inside the tour row. */
@Converter
public class ItineraryConverter extends JsonAttributeConverter<List<ItineraryDay>> {

    public ItineraryConverter() {
        super(new TypeReference<>() {
        });
    }

    @Override
    protected List<ItineraryDay> empty() {
        return List.of();
    }
}
