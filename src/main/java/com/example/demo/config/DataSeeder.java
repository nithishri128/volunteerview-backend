package com.example.demo.config;

import com.example.demo.entity.Opportunity;
import com.example.demo.entity.Organization;
import com.example.demo.entity.SystemUser;
import com.example.demo.entity.VolunteerEnrollment;
import com.example.demo.repository.OpportunityRepository;
import com.example.demo.repository.OrganizationRepository;
import com.example.demo.repository.SystemUserRepository;
import com.example.demo.repository.VolunteerEnrollmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final SystemUserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OpportunityRepository opportunityRepository;
    private final VolunteerEnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(SystemUserRepository userRepository,
                       OrganizationRepository organizationRepository,
                       OpportunityRepository opportunityRepository,
                       VolunteerEnrollmentRepository enrollmentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.opportunityRepository = opportunityRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() != 0) {
            return;
        }

        SystemUser admin = userRepository.save(SystemUser.builder()
                .username("admin")
                .email("admin@volunteerview.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .fullName("Platform Admin")
                .role(SystemUser.Role.PLATFORM_ADMIN)
                .status(SystemUser.UserStatus.APPROVED)
                .isActive(true)
                .build());

        SystemUser coordinator1 = userRepository.save(SystemUser.builder()
                .username("coordinator")
                .email("coordinator@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Coordinator One")
                .role(SystemUser.Role.ORGANIZATION_COORDINATOR)
                .status(SystemUser.UserStatus.APPROVED)
                .isActive(true)
                .build());

        Organization helpingHands = organizationRepository.save(Organization.builder()
                .name("Helping Hands")
                .mission("A non-profit organization helping the community.")
                .address("123 Main St")
                .contactEmail("contact@helpinghands.org")
                .status(Organization.OrganizationStatus.APPROVED)
                .coordinator(coordinator1)
                .build());
        coordinator1.setOrganization(helpingHands);
        userRepository.save(coordinator1);

        SystemUser coordinator2 = userRepository.save(SystemUser.builder()
                .username("coordinator2")
                .email("coord2@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Coordinator Two")
                .role(SystemUser.Role.ORGANIZATION_COORDINATOR)
                .status(SystemUser.UserStatus.APPROVED)
                .isActive(true)
                .build());

        Organization greenPeace = organizationRepository.save(Organization.builder()
                .name("Green Peace")
                .mission("Protecting the environment and promoting peace.")
                .address("456 Earth Way")
                .contactEmail("contact@greenpeace-demo.org")
                .status(Organization.OrganizationStatus.APPROVED)
                .coordinator(coordinator2)
                .build());
        coordinator2.setOrganization(greenPeace);
        userRepository.save(coordinator2);

        SystemUser coordinator3 = userRepository.save(SystemUser.builder()
                .username("coordinator3")
                .email("coord3@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Coordinator Three")
                .role(SystemUser.Role.ORGANIZATION_COORDINATOR)
                .status(SystemUser.UserStatus.APPROVED)
                .isActive(true)
                .build());

        Organization safeHaven = organizationRepository.save(Organization.builder()
                .name("Safe Haven Animal Shelter")
                .mission("Rescuing and rehoming abandoned animals.")
                .address("789 Pet Lane")
                .contactEmail("contact@safehaven-demo.org")
                .status(Organization.OrganizationStatus.APPROVED)
                .coordinator(coordinator3)
                .build());
        coordinator3.setOrganization(safeHaven);
        userRepository.save(coordinator3);

        SystemUser volunteer = userRepository.save(SystemUser.builder()
                .username("volunteer")
                .email("volunteer@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Val Volunteer")
                .role(SystemUser.Role.VOLUNTEER)
                .status(SystemUser.UserStatus.APPROVED)
                .isActive(true)
                .build());

        Opportunity foodDrive = opportunityRepository.save(Opportunity.builder()
                .title("Food Drive")
                .description("Collect and distribute food donations to families in need.")
                .location("Community Center")
                .category("Community")
                .scheduledDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(13, 0))
                .maxCapacity(20)
                .status(Opportunity.OpportunityStatus.OPEN)
                .organization(helpingHands)
                .build());

        Opportunity treePlanting = opportunityRepository.save(Opportunity.builder()
                .title("Tree Planting")
                .description("Plant trees in the local park to help the environment.")
                .location("Riverside Park")
                .category("Environment")
                .scheduledDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .maxCapacity(30)
                .status(Opportunity.OpportunityStatus.OPEN)
                .organization(greenPeace)
                .build());

        Opportunity beachCleanup = opportunityRepository.save(Opportunity.builder()
                .title("Beach Cleanup")
                .description("Clean up litter and debris along the shoreline.")
                .location("Sunset Beach")
                .category("Environment")
                .scheduledDate(LocalDate.now().plusDays(14))
                .startTime(LocalTime.of(7, 0))
                .endTime(LocalTime.of(11, 0))
                .maxCapacity(25)
                .status(Opportunity.OpportunityStatus.OPEN)
                .organization(greenPeace)
                .build());

        Opportunity tutoring = opportunityRepository.save(Opportunity.builder()
                .title("Tutoring Kids")
                .description("Provide after-school tutoring for underprivileged children.")
                .location("Helping Hands HQ")
                .category("Education")
                .scheduledDate(LocalDate.now().plusDays(5))
                .startTime(LocalTime.of(15, 0))
                .endTime(LocalTime.of(17, 0))
                .maxCapacity(10)
                .status(Opportunity.OpportunityStatus.OPEN)
                .organization(helpingHands)
                .build());

        Opportunity shelterHelp = opportunityRepository.save(Opportunity.builder()
                .title("Animal Shelter Help")
                .description("Assist with feeding, cleaning, and walking shelter animals.")
                .location("Safe Haven Shelter")
                .category("Animal Welfare")
                .scheduledDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(14, 0))
                .maxCapacity(15)
                .status(Opportunity.OpportunityStatus.OPEN)
                .organization(safeHaven)
                .build());

        Opportunity elderlyCare = opportunityRepository.save(Opportunity.builder()
                .title("Elderly Care")
                .description("Spend time with and assist elderly residents at a care home.")
                .location("Sunny Days Care Home")
                .category("Community")
                .scheduledDate(LocalDate.now().plusDays(6))
                .startTime(LocalTime.of(13, 0))
                .endTime(LocalTime.of(16, 0))
                .maxCapacity(12)
                .status(Opportunity.OpportunityStatus.OPEN)
                .organization(helpingHands)
                .build());

        List<Opportunity> opportunities = List.of(
                foodDrive, treePlanting, beachCleanup, tutoring, shelterHelp, elderlyCare);
        List<VolunteerEnrollment.EnrollmentStatus> statuses = List.of(
                VolunteerEnrollment.EnrollmentStatus.PENDING,
                VolunteerEnrollment.EnrollmentStatus.APPROVED,
                VolunteerEnrollment.EnrollmentStatus.STARTED,
                VolunteerEnrollment.EnrollmentStatus.COMPLETED,
                VolunteerEnrollment.EnrollmentStatus.REJECTED,
                VolunteerEnrollment.EnrollmentStatus.CANCELLED);

        for (int i = 0; i < opportunities.size(); i++) {
            Opportunity opportunity = opportunities.get(i);
            VolunteerEnrollment.EnrollmentStatus status = statuses.get(i);

            VolunteerEnrollment enrollment = VolunteerEnrollment.builder()
                    .volunteer(volunteer)
                    .opportunity(opportunity)
                    .status(status)
                    .hoursLogged(status == VolunteerEnrollment.EnrollmentStatus.COMPLETED ? 4.0 : 0.0)
                    .build();
            enrollmentRepository.save(enrollment);

            opportunity.setEnrolledCount(opportunity.getEnrolledCount() + 1);
            opportunityRepository.save(opportunity);
        }
    }
}
