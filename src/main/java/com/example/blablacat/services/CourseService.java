package com.example.blablacat.services;

import com.example.blablacat.dto.CourseDto;
import com.example.blablacat.dto.CoursePermanentDto;
import com.example.blablacat.entity.CourseEntity;
import com.example.blablacat.entity.ReservationEntity;
import com.example.blablacat.entity.UserEntity;
import com.example.blablacat.repository.CourseRepository;
import com.example.blablacat.repository.ReservationRepository;
import com.example.blablacat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseService implements ICourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Override
    public CourseDto toDto(CourseEntity entity) {
        CourseDto cdto = new CourseDto();
        cdto.setId(entity.getId());
        cdto.setDriverUsername(entity.getUserEntity().getUsername());
        cdto.setDate(entity.getDate());
        cdto.setCityDeparture(entity.getCityDeparture());
        cdto.setDepartureZipCode(entity.getDepartureZipCode());
        cdto.setStreetDeparture(entity.getStreetDeparture());
        cdto.setCityArrival(entity.getCityArrival());
        cdto.setArrivalZipCode(entity.getArrivalZipCode());
        cdto.setStreetArrival(entity.getStreetArrival());
        cdto.setNumberPlace(entity.getNumberPlace());

        return cdto;
    }

    @Override
    public List<CourseDto> getAllCourse() {
        List<CourseEntity> list = courseRepository.findAll();
        List<CourseDto> listDto = new ArrayList<>();
        for (int i=0; i<list.size(); i++) {
            CourseEntity entity = list.get(i);
            CourseDto dto = this.toDto(entity);
            listDto.add(dto);
        }
        return listDto;
    }

    @Override
    public List<CourseDto> getAllCoursesValid() {
        List<CourseEntity> list = courseRepository.findAllByDeletedAtNullAndDateAfter(LocalDateTime.now());
        List<CourseDto> listDto = new ArrayList<>();

        for(CourseEntity entity : list){
            listDto.add(this.toDto(entity));
        }

        System.out.println(listDto);

        return listDto;
    }

    @Override
    public List<CourseDto> getLastFiveCoursesCreated() {
        List<CourseEntity> list = courseRepository.findFirst5ByOrderByCreatedAtDesc();

        List<CourseDto> listDto = new ArrayList<>();

        for(CourseEntity entity : list){
            listDto.add(this.toDto(entity));
        }

        return listDto;
    }


    @Override
    public List<CourseDto> getAllCoursesByCity(String city) {
        List<CourseEntity> list = courseRepository.findByCityDepartureLikeOrCityArrivalLikeAndDeletedAtNullAndDateAfter("%"+city+"%", "%"+city+"%", LocalDateTime.now());
        List<CourseDto> listDto = new ArrayList<>();

        for(CourseEntity ce : list) {
            listDto.add(this.toDto(ce));
        }
        return listDto;
    }

    @Override
    public List<CourseDto> getAllCoursesByStreet(String street) {
        List<CourseEntity> list = courseRepository.findByStreetDepartureLikeOrStreetArrivalLikeAndDeletedAtNullAndDateAfter("%"+street+"%", "%"+street+"%", LocalDateTime.now());
        List<CourseDto> listDto = new ArrayList<>();

        for(CourseEntity ce : list) {
            listDto.add(this.toDto(ce));
        }
        return listDto;
    }

    @Override
    public List<CourseDto> getAllCoursesByZipcode(String zipcode) {
        List<CourseEntity> list = courseRepository.getAllCoursesByZipCode(zipcode, zipcode, LocalDateTime.now());
        List<CourseDto> listDto = new ArrayList<>();

        for(CourseEntity ce : list) {
            listDto.add(this.toDto(ce));
        }
        return listDto;
    }

    @Override
    public Integer numberPageMaxCourseByUser(Integer userId) {
        List<CourseEntity> list = courseRepository.findAllByUserEntityAndDeletedAtNullAndDateAfter(userRepository.findById(userId).get(),LocalDateTime.now());
        return list.size() / 5 ;
    }

    @Override
    public List<CourseDto> getAllCoursesByUserPage(Integer page, Integer size, Integer userId) {
        UserEntity user = userRepository.findById(userId).get();
        List<CourseEntity> list = courseRepository.findAllByUserEntityAndDeletedAtNullAndDateAfter(user, PageRequest.of(page, size),LocalDateTime.now()).getContent();

        List<CourseDto> listFinal = new ArrayList<>();

        for(CourseEntity entity: list){
            listFinal.add(this.toDto(entity));
        }
        return listFinal;
    }

    @Override
    public void addPermanentCourses(CoursePermanentDto cpDto) {
        List<Integer> jours = new ArrayList<>();//liste des valeurs numériques des jours permanents sélectionnés

        if(cpDto.getMonday())
            jours.add(1);
        if(cpDto.getTuesday())
            jours.add(2);
        if(cpDto.getWednesday())
            jours.add(3);
        if(cpDto.getThursday())
            jours.add(4);
        if(cpDto.getFriday())
            jours.add(5);

        List<CourseEntity> allCourses = new ArrayList<>();

        for (LocalDate date = cpDto.getDateDebut(); date.isBefore(cpDto.getDateFin().plusDays(1)); date = date.plusDays(1)){

            for(int i : jours){
                if(i == date.getDayOfWeek().getValue()){
                    CourseEntity entity = new CourseEntity();

                    entity.setUserEntity(userRepository.findById(cpDto.getId()).get());
                    entity.setCityDeparture(cpDto.getCityDeparture());
                    entity.setStreetDeparture(cpDto.getStreetDeparture());
                    entity.setDepartureZipCode(cpDto.getDepartureZipCode());
                    entity.setCityArrival(cpDto.getCityArrival());
                    entity.setStreetArrival(cpDto.getStreetArrival());
                    entity.setArrivalZipCode(cpDto.getArrivalZipCode());
                    entity.setNumberPlace(cpDto.getNumberPlace());
                    entity.setCreatedAt(LocalDateTime.now());

                    entity.setDate(LocalDateTime.of(date,cpDto.getTime()));
                    allCourses.add(entity);
                }
            }
        }
        this.courseRepository.saveAllAndFlush(allCourses);
    }

    @Override
    public void deleteCourse(Integer courseId) {
        CourseEntity courseEntity = this.courseRepository.findById(courseId).get();
        System.out.println(courseEntity.getUserEntity().getUsername());

        //déplacer dans reservationService ?
        List<ReservationEntity> listReservations = this.reservationRepository.findAllByCourseEntity(courseEntity);

        for (ReservationEntity resEntity: listReservations){
            resEntity.setDeletedAt(LocalDateTime.now());
        }

        if(listReservations.size() > 0)
            this.reservationRepository.saveAllAndFlush(listReservations);

        courseEntity.setDeletedAt(LocalDateTime.now());
        this.courseRepository.saveAndFlush(courseEntity);
    }

    @Override
    public Integer numberPageMaxOfCourses() {
        List<CourseEntity> list = courseRepository.findAllByDeletedAtNullAndDateAfter(LocalDateTime.now());
        return list.size() / 12 ;
    }

    @Override
    public List<CourseDto> getAllCoursesByPages(Integer page, Integer size) {
        List<CourseEntity> list = courseRepository.findAllByDeletedAtNullAndDateAfter(PageRequest.of(page, size),LocalDateTime.now()).getContent();
        List<CourseDto> listFinal = new ArrayList<>();

        for(CourseEntity entity: list){
            listFinal.add(this.toDto(entity));
        }
        return listFinal;
    }

    @Override
    public Integer addCourse(Integer userId, LocalDateTime date, String cityDeparture, Integer departureZipCode, String streetDeparture, String cityArrival, Integer arrivalZipCode, String streetArrival, Integer numberPlace) {
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setDate(date);
        courseEntity.setUserEntity(userRepository.findById(userId).get());
        courseEntity.setCityDeparture(cityDeparture);
        courseEntity.setDepartureZipCode(departureZipCode);
        courseEntity.setStreetDeparture(streetDeparture);
        courseEntity.setCityArrival(cityArrival);
        courseEntity.setArrivalZipCode(arrivalZipCode);
        courseEntity.setStreetArrival(streetArrival);
        courseEntity.setNumberPlace(numberPlace);
        courseEntity.setCreatedAt(LocalDateTime.now());

        courseRepository.saveAndFlush(courseEntity);
        return courseEntity.getId();
    }


}
