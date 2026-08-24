package uz.nagato.touragency.tour.entity;

/** One day of a tour itinerary. Stored inside the tour row as JSON, not as its own table. */
public record ItineraryDay(int day, String title, String description) {
}
