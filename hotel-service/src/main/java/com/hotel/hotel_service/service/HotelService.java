package com.hotel.hotel_service.service;

import com.hotel.hotel_service.Iservice.IHotelService;
import com.hotel.hotel_service.client.ReviewServiceClient;
import com.hotel.hotel_service.client.RoomServiceClient;
import com.hotel.hotel_service.client.UserServiceClient;
import com.hotel.hotel_service.dto.request.SearchHotel;
import com.hotel.hotel_service.dto.request.UpdateHotelProfileRequest;
import com.hotel.hotel_service.dto.response.HotelDetail;
import com.hotel.hotel_service.dto.response.HotelProfile;
import com.hotel.hotel_service.dto.response.Hotels;
import com.hotel.hotel_service.dto.response.SearchHotelResult;
import com.hotel.hotel_service.model.Amenities;
import com.hotel.hotel_service.model.Home;
import com.hotel.hotel_service.model.HomeAmeneties;
import com.hotel.hotel_service.model.HomeImage;
import com.hotel.hotel_service.repository.AmenityRepository;
import com.hotel.hotel_service.repository.HomeAmenityRepository;
import com.hotel.hotel_service.repository.HomeImageRepository;
import com.hotel.hotel_service.repository.HomeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService implements IHotelService {

    @Autowired
    private HomeRepository homeRepository;
    @Autowired
    private AmenityRepository amenityRepository;
    @Autowired
    private HomeAmenityRepository homeAmenityRepository;
    @Autowired
    private HomeImageRepository homeImageRepository;
    @Autowired
    private RoomServiceClient roomServiceClient;
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private ReviewServiceClient reviewServiceClient;

    private Hotels mapHomeToHotels(Home home) {
        Hotels hotelResponse = new Hotels();
        hotelResponse.hotelId = home.getId();
        hotelResponse.hotelName = home.getHome_name();
        hotelResponse.rating = home.getRating();
        hotelResponse.street = home.getStreet();
        hotelResponse.district = home.getDistrict();
        hotelResponse.city = home.getCity();
        hotelResponse.country = home.getCountry();

        // Lấy giá phòng thấp nhất từ room-service
        double minPrice = home.getPrice_per_night(); // fallback
        try {
            Map<String, Object> priceResponse = roomServiceClient.getMinRoomPrice(home.getId());
            if (priceResponse != null) {
                Object found = priceResponse.get("found");
                Object minPriceVal = priceResponse.get("minPrice");
                if (Boolean.TRUE.equals(found) && minPriceVal != null) {
                    minPrice = ((Number) minPriceVal).doubleValue();
                }
            }
        } catch (Exception e) {
            // room-service không khả dụng → giữ price_per_night
        }
        hotelResponse.pricePerNight = minPrice;

        try {
            Map<String, Object> countResponse = reviewServiceClient.getCountCommentByHotelId(home.getId());
            Object cnt = countResponse.get("countComment");
            hotelResponse.reviewCount = cnt != null ? ((Number) cnt).intValue() : 0;
        } catch (Exception e) {
            hotelResponse.reviewCount = 0;
        }

        List<String> amenitiesList = new ArrayList<>();
        List<HomeAmeneties> homeAmenetiesList = homeAmenityRepository.findHomeAmenetiesByHome_Id(home.getId());
        for (HomeAmeneties homeAmeneties : homeAmenetiesList) {
            if (homeAmeneties.getAmenity() != null) {
                amenitiesList.add(homeAmeneties.getAmenity().getAmenity_name());
            }
        }
        hotelResponse.aminities = String.join(", ", amenitiesList);

        List<HomeImage> homeImages = homeImageRepository.findHomeImageByHome_Id(home.getId());
        if (!homeImages.isEmpty()) {
            hotelResponse.imageUrl = homeImages.get(0).getImageUrl();
        } else {
            hotelResponse.imageUrl = "";
        }

        return hotelResponse;
    }

    @Override
    public List<Hotels> getFamousHotels() {
        List<Home> homes = homeRepository.findByRatingGreaterThan(4.0);
        if (homes.isEmpty()) {
            return null;
        }
        return homes.parallelStream()
                .map(this::mapHomeToHotels)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Hotels> getAllHotels() {
        List<Home> homes = homeRepository.findAll();
        if (homes.isEmpty()) {
            return null;
        }
        return homes.parallelStream()
                .map(this::mapHomeToHotels)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public HotelDetail getHotelDetail(int hotelId) {
        Optional<Home> hotelOptional = homeRepository.findById(hotelId);
        if (hotelOptional.isPresent()) {
            HotelDetail hotelDetail = new HotelDetail();
            Home home = hotelOptional.get();
            hotelDetail.hotelName = home.getHome_name();
            hotelDetail.rating = home.getRating();
            hotelDetail.street = home.getStreet();
            hotelDetail.district = home.getDistrict();
            hotelDetail.city = home.getCity();
            hotelDetail.country = home.getCountry();
            hotelDetail.description = home.getDescription();

            List<String> amenitiesList = new ArrayList<>();
            List<HomeAmeneties> homeAmenetiesList = homeAmenityRepository.findHomeAmenetiesByHome_Id(hotelId);
            if (homeAmenetiesList.isEmpty()) {
                hotelDetail.aminities = String.join(", ", "");
            }
            for (HomeAmeneties homeAmeneties : homeAmenetiesList) {
                if (homeAmeneties.getAmenity() != null) {
                    amenitiesList.add(homeAmeneties.getAmenity().getAmenity_name());
                }
            }
            hotelDetail.aminities = String.join(", ", amenitiesList);

            List<String> images = new ArrayList<>();
            List<HomeImage> homeImages = homeImageRepository.findHomeImageByHome_Id(hotelId);
            if (homeImages.isEmpty()) {
                hotelDetail.imageUrl = String.join(", ", "");
            }
            for (HomeImage homeImage : homeImages) {
                if(homeImage.getImageUrl() != null) {
                    images.add(homeImage.getImageUrl());
                }
            }
            hotelDetail.imageUrl = String.join(", ", images);

            return hotelDetail;
        }
        return null;
    }

    @Override
    public HotelProfile getHotelProfile(int hotelId) {
        Optional<Home> hotelOptional = homeRepository.findById(hotelId);
        if (hotelOptional.isPresent()) {
            HotelProfile profile = new HotelProfile();
            Home home = hotelOptional.get();
            profile.hotelName = home.getHome_name();
            profile.street = home.getStreet();
            profile.district = home.getDistrict();
            profile.city = home.getCity();
            profile.country = home.getCountry();
            profile.description = home.getDescription();

            List<String> amenitiesList = new ArrayList<>();
            List<HomeAmeneties> homeAmenetiesList = homeAmenityRepository.findHomeAmenetiesByHome_Id(hotelId);
            for (HomeAmeneties homeAmeneties : homeAmenetiesList) {
                if (homeAmeneties.getAmenity() != null) {
                    amenitiesList.add(homeAmeneties.getAmenity().getAmenity_name());
                }
            }
            profile.aminities = amenitiesList;

            List<String> images = new ArrayList<>();
            List<HomeImage> homeImages = homeImageRepository.findHomeImageByHome_Id(hotelId);
            for (HomeImage homeImage : homeImages) {
                if(homeImage.getImageUrl() != null) {
                    images.add(homeImage.getImageUrl());
                }
            }
            profile.imageUrl = images;

            return profile;
        }
        return null;
    }

    @Override
    public void updateHotelProfile(int hotelId, UpdateHotelProfileRequest request) {
        Optional<Home> hotelOptional = homeRepository.findById(hotelId);
        if (hotelOptional.isPresent()) {
            Home home = hotelOptional.get();
            // Update basic info
            home.setHome_name(request.getHotelName());
            home.setDescription(request.getDescription());
            home.setStreet(request.getStreet());
            home.setDistrict(request.getDistrict());
            home.setCity(request.getCity());
            home.setCountry(request.getCountry());
            homeRepository.save(home);

            // Update Amenities
            List<HomeAmeneties> oldAmenities = homeAmenityRepository.findHomeAmenetiesByHome_Id(hotelId);
            homeAmenityRepository.deleteAll(oldAmenities);

            if (request.getAmenities() != null) {
                for (String amenityName : request.getAmenities()) {
                    Amenities amenity = amenityRepository.findByName(amenityName).orElseGet(() -> {
                        Amenities newAmenity = new Amenities();
                        newAmenity.setAmenity_name(amenityName);
                        newAmenity.setCategory("GENERAL"); // Default category
                        return amenityRepository.save(newAmenity);
                    });

                    HomeAmeneties homeAmenity = new HomeAmeneties();
                    homeAmenity.setHome(home);
                    homeAmenity.setAmenity(amenity);
                    homeAmenityRepository.save(homeAmenity);
                }
            }

            // Update Images
            List<HomeImage> oldImages = homeImageRepository.findHomeImageByHome_Id(hotelId);
            homeImageRepository.deleteAll(oldImages);

            if (request.getImageUrls() != null) {
                for (String url : request.getImageUrls()) {
                    HomeImage homeImage = new HomeImage();
                    homeImage.setHome(home);
                    homeImage.setImageUrl(url);
                    homeImage.setCreatedAt(java.time.LocalDateTime.now());
                    homeImageRepository.save(homeImage);
                }
            }
        }
    }

    @Override
    public List<SearchHotelResult> searchHotels(SearchHotel searchHotel) {
        List<Home> homes = homeRepository.findByCityContainingIgnoreCase(searchHotel.city);
        if (homes.isEmpty()) {
            return new ArrayList<>();
        }

        return homes.parallelStream()
            .map(home -> {
                try {
                    Map<String, Object> roomResponse = roomServiceClient.getAvailableRoomCount(
                        home.getId(),
                        searchHotel.checkInDate,
                        searchHotel.checkOutDate,
                        searchHotel.numberOfGuests,
                        searchHotel.numberOfRooms
                    );

                    Integer availableRooms = (Integer) roomResponse.get("availableRooms");
                    Integer totalRooms = (Integer) roomResponse.get("totalRooms");

                    if (availableRooms != null && availableRooms >= searchHotel.numberOfRooms) {
                        SearchHotelResult result = new SearchHotelResult();
                        result.setHotelId(home.getId());
                        result.setHotelName(home.getHome_name());
                        result.setStreet(home.getStreet());
                        result.setDistrict(home.getDistrict());
                        result.setCity(home.getCity());
                        result.setCountry(home.getCountry());
                        result.setRating(home.getRating());
                        result.setAvailableRooms(availableRooms);
                        result.setTotalRooms(totalRooms != null ? totalRooms : 0);

                        List<HomeImage> homeImages = homeImageRepository.findHomeImageByHome_Id(home.getId());
                        if (!homeImages.isEmpty()) {
                            result.setImageUrl(homeImages.get(0).getImageUrl());
                        } else {
                            result.setImageUrl("");
                        }

                        return result;
                    }
                } catch (Exception e) {
                    // Log error và tiếp tục với khách sạn tiếp theo
                    System.err.println("Error checking availability for hotel " + home.getId() + ": " + e.getMessage());
                }
                return null;
            })
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean existsById(int hotelId) {
        Optional<Home> home = homeRepository.findById(hotelId);
        if (home.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public HotelDetail getHotelDetailForBooking(int hotelId) {
        Optional<Home> hotelOptional = homeRepository.findById(hotelId);
        if (hotelOptional.isPresent()) {
            HotelDetail hotelDetail = new HotelDetail();
            Home home = hotelOptional.get();
            hotelDetail.hotelName = home.getHome_name();
            hotelDetail.street = home.getStreet();
            hotelDetail.district = home.getDistrict();
            hotelDetail.city = home.getCity();
            hotelDetail.country = home.getCountry();

            List<HomeImage> homeImages = homeImageRepository.findHomeImageByHome_Id(home.getId());
            if (homeImages.isEmpty()) {
                hotelDetail.imageUrl = "";
            } else {
                hotelDetail.imageUrl = homeImages.get(0).getImageUrl();
            }

            return hotelDetail;
        }
        return null;
    }

    @Override
    public int getHotelId(int userId) {
        Optional<Home> home = homeRepository.findByOwnerId(userId);
        if (home.isPresent()) {
            return home.get().getId();
        }
        return 0;
    }

    @Override
    public int getOwnerIdByHotelId(int hotelId) {
        Optional<Home> home = homeRepository.findById(hotelId);
        if (home.isPresent()) {
            return home.get().getOwnerId();
        }
        return 0;
    }

    @Override
    public int getHotelId(int ownerId, Integer role) {
        if (role == 2) {
            Optional<Home> home = homeRepository.findByOwnerId(ownerId);
            if (home.isPresent()) {
                return home.get().getId();
            }
        }
        else if(role == 4 || role == 5) {
            Map<String, Object> response = userServiceClient.getHotelId(ownerId);
            Integer hotelId = (Integer) response.get("hotelId");
            return hotelId;
        }
        return 0;

    }
    @Override
    public List<String> getAllAmenities() {
        return amenityRepository.findAll()
                .stream()
                .map(Amenities::getAmenity_name)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }


}
