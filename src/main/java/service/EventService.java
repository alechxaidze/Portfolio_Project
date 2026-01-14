package service;

import model.Event;
import model.EventType;
import model.Portfolio;
import model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EventService {

    public EventService() {
    }

    public void addGlobalEvent(User user, String title, String description, LocalDate date, EventType type) {
        Event event = new Event(title, description, date, type);
        user.addGlobalEvent(event);
        UserService.save();
    }

    public void addPortfolioEvent(Portfolio portfolio, String title, String description, LocalDate date,
            EventType type) {
        Event event = new Event(title, description, date, type, portfolio.getId());
        portfolio.getEvents().add(event);
        UserService.save();
    }

    public List<Event> getAllEvents(User user) {
        List<Event> allEvents = new ArrayList<>(user.getGlobalEvents());

        for (Portfolio portfolio : user.getPortfolios()) {
            allEvents.addAll(portfolio.getEvents());
        }

        // Sort by date
        allEvents.sort(Comparator.comparing(Event::getDate).reversed());
        return allEvents;
    }

    public List<Event> getEventsInRange(User user, LocalDate start, LocalDate end) {
        return getAllEvents(user).stream()
                .filter(e -> !e.getDate().isBefore(start) && !e.getDate().isAfter(end))
                .collect(Collectors.toList());
    }
    public List<Event> getPortfolioEvents(Portfolio portfolio) {
        List<Event> events = new ArrayList<>(portfolio.getEvents());
        events.sort(Comparator.comparing(Event::getDate).reversed());
        return events;
    }
    public void deleteEvent(User user, Event event) {
        if (event.isGlobal()) {
            user.getGlobalEvents().removeIf(e -> e.getId().equals(event.getId()));
        } else {
            for (Portfolio portfolio : user.getPortfolios()) {
                portfolio.getEvents().removeIf(e -> e.getId().equals(event.getId()));
            }
        }
        UserService.save();
    }
    public List<Event> getEventsByType(User user, EventType type) {
        return getAllEvents(user).stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.toList());
    }
}
