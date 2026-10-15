package de.bookflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.jayway.jsonpath.JsonPath;

import de.bookflow.entity.Role;
import de.bookflow.entity.User;
import de.bookflow.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private Long postAndGetId(String url, String json) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return idFrom(result);
    }

    private Long idFrom(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    @Test
    void bookingFlow_createConfirmCancelAndRebook() throws Exception {
        // Create a hotel through the API
        Long hotelId = postAndGetId("/api/hotels", """
                {
                    "name": "Flow Hotel",
                    "address": "Flow Street 1",
                    "country": "Germany",
                    "city": "Hamburg"
                }
                """);

        // Create a room in that hotel
        Long roomId = postAndGetId("/api/rooms", """
                {
                    "hotelId": %d,
                    "number": 501,
                    "type": "DOUBLE",
                    "pricePerNight": 100.00,
                    "capacity": 2
                }
                """.formatted(hotelId));

        // Create a customer (no user endpoint yet)
        User user = userRepository.save(
                new User("Flow", "Tester", "flow@bookflow.dev", "secret", Role.CUSTOMER));

        // Book 3 nights
        LocalDate checkIn = LocalDate.now().plusDays(30);
        LocalDate checkOut = checkIn.plusDays(3);

        String bookingJson = """
                {
                    "roomId": %d,
                    "userId": %d,
                    "checkInDate": "%s",
                    "checkOutDate": "%s"
                }
                """.formatted(roomId, user.getId(), checkIn, checkOut);

        // Create a booking
        MvcResult created = mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(300.0))
                .andReturn();
        Long bookingId = idFrom(created);

        // Create overlap booking
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isConflict());

        // Confirm a booking
        mockMvc.perform(patch("/api/bookings/{id}/confirm", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Cancel a booking
        mockMvc.perform(patch("/api/bookings/{id}/cancel", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Create second overlap booking after cancel
        MvcResult rebooked = mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson))
                .andExpect(status().isCreated())
                .andReturn();

        assertThat(idFrom(rebooked)).isNotEqualTo(bookingId);
    }
}
