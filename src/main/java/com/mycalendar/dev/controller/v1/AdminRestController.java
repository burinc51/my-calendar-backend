package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.response.AdminStatsResponse;
import com.mycalendar.dev.repository.EventRepository;
import com.mycalendar.dev.repository.GroupRepository;
import com.mycalendar.dev.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminRestController {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final EventRepository eventRepository;

    public AdminRestController(UserRepository userRepository, GroupRepository groupRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long totalGroups = groupRepository.count();
        long totalEvents = eventRepository.count();
        // Notes are currently local-only in the frontend, so we return 0 for backend stats
        long totalNotes = 0;

        return new AdminStatsResponse(totalUsers, totalGroups, totalEvents, totalNotes);
    }
}
