package de.bookflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.bookflow.dto.HotelRequest;
import de.bookflow.dto.HotelResponse;
import de.bookflow.entity.Hotel;
import de.bookflow.exception.ResourceNotFoundException;
import de.bookflow.repository.HotelRepository;

@ExtendWith(MockitoExtension.class)
public class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelService hotelService;

    @Test
    void createHotel_withValidRequest_returnsHotel() {
        // Arrange
        HotelRequest request = new HotelRequest("Test Hotel", "Main Street 1", "Germany", "Berlin");

        when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        HotelResponse response = hotelService.createHotel(request);

        // Assert
        assertThat(response.name()).isEqualTo("Test Hotel");
        assertThat(response.address()).isEqualTo("Main Street 1");
        assertThat(response.country()).isEqualTo("Germany");
        assertThat(response.city()).isEqualTo("Berlin");
    }

    @Test
    void getHotelById_withUnknownId_throwsResourceNotFoundException() {
        // Arrange
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> hotelService.getHotelById(99L)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hotel not found with id 99");
    }

    @Test
    void updateHotel_withExistingHotel_appliesNewValues() {
        // Arrange
        HotelRequest request = new HotelRequest("Test Hotel Updated", "Main Street 2", "France", "Paris");

        Hotel hotel = new Hotel("TestName", "Main Street 1", "Germany", "Berlin");
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        HotelResponse response = hotelService.updateHotel(1L, request);

        // Assert
        assertThat(response.name()).isEqualTo("Test Hotel Updated");
        assertThat(response.address()).isEqualTo("Main Street 2");
        assertThat(response.country()).isEqualTo("France");
        assertThat(response.city()).isEqualTo("Paris");
    }

    @Test
    void deleteHotel_withExistingHotel_deletesIt() {
        // Arrange
        Hotel hotel = new Hotel("TestName", "Main Street 1", "Germany", "Berlin");
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        // Act
        hotelService.deleteHotel(1L);

        // Assert
        verify(hotelRepository).delete(hotel);
    }

    @Test
    void deleteHotel_withUnknownId_throwsResourceNotFoundException() {
        // Arrange
        when(hotelRepository.findById(1L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> hotelService.deleteHotel(1L)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hotel not found with id 1");
        verify(hotelRepository, never()).delete(any());
    }
}
