package com.example.blablacat.services;

import com.example.blablacat.dto.CourseDto;
import com.example.blablacat.dto.ReservationDto;
import com.example.blablacat.entity.CourseEntity;
import com.example.blablacat.entity.UserEntity;
import com.example.blablacat.entity.ReservationEntity;
import com.example.blablacat.repository.CourseRepository;
import com.example.blablacat.repository.UserRepository;
import com.example.blablacat.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService implements IReservationService {
    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    CourseRepository courseRepository;

    @Override
    public ReservationDto toDto(ReservationEntity reservationEntity ) {
        ReservationDto userHasCourseDto = new ReservationDto();
        userHasCourseDto.setDisplayName(reservationEntity.getUserEntity().getFirstName() + " " + reservationEntity.getUserEntity().getLastName() );
        userHasCourseDto.setUsername(reservationEntity.getCourseEntity().getUserEntity().getUsername());
        userHasCourseDto.setDisplayArrivalAddress(reservationEntity.getCourseEntity().getStreetArrival() + ", " + reservationEntity.getCourseEntity().getArrivalZipCode() + " " + reservationEntity.getCourseEntity().getCityArrival());
        userHasCourseDto.setDisplayDepartureAddress(reservationEntity.getCourseEntity().getStreetDeparture() + ", " + reservationEntity.getCourseEntity().getDepartureZipCode() + " " + reservationEntity.getCourseEntity().getCityDeparture());
        userHasCourseDto.setUser_id(reservationEntity.getUserEntity().getId());
        userHasCourseDto.setCourse_id(reservationEntity.getCourseEntity().getId());
        userHasCourseDto.setDisplayDate(reservationEntity.getCourseEntity().getDate());
        return userHasCourseDto;
    }

    @Override
    public ReservationDto get(Integer id) {
        return toDto(reservationRepository.findById(id).get());
    }


    @Override
    public  Integer save(CourseDto dto, Integer userId) {
            ReservationEntity entity = new ReservationEntity();
            UserEntity userEntity = userRepository.findById(userId).get();
            entity.setUserEntity(userEntity);
            CourseEntity courseEntity = courseRepository.findById(dto.getId()).get();
            entity.setCourseEntity(courseEntity);
            entity.setCreatedAt(LocalDateTime.now());
            entity = reservationRepository.saveAndFlush(entity);
            return entity.getUser_has_course_id();
        }

    @Override
    public List<ReservationDto> getAllReservations() {
            List<ReservationEntity> list = reservationRepository.findAll();
            List<ReservationDto> listFinal = new ArrayList<>();

            for(ReservationEntity entity: list){
                listFinal.add(this.toDto(entity));
            }
            return listFinal;
        }

    @Override
    public Integer numberPageMaxReservationByUser(Integer userId) {
        List<ReservationEntity> list = reservationRepository.findAllByUserEntityAndDeletedAtNullAndCourseEntity_DateAfter(userRepository.findById(userId).get(), LocalDateTime.now());
        return list.size() / 5 ;
    }


    @Override
    public List<ReservationDto> getAllReservationsByUserPage(Integer page, Integer size, Integer userId) {

        UserEntity user = userRepository.findById(userId).get();
        List<ReservationEntity> list = reservationRepository.findAllByUserEntityAndDeletedAtNullAndCourseEntity_DateAfter(user, PageRequest.of(page, size), LocalDateTime.now()).getContent();

        List<ReservationDto> listFinal = new ArrayList<>();

        for(ReservationEntity entity: list){
            listFinal.add(this.toDto(entity));
        }
        return listFinal;
    }

    @Override
    public Boolean exists(Integer id) {
        return reservationRepository.existsById(id);
    }

    @Override
    public void deleteReservation(Integer courseId, Integer userId) {

        CourseEntity courseEntity = this.courseRepository.findById(courseId).get();
        UserEntity userEntity = this.userRepository.findById(userId).get();
        ReservationEntity resEntity = this.reservationRepository.findByCourseEntityAndUserEntity(courseEntity, userEntity);

        resEntity.setDeletedAt(LocalDateTime.now());
        this.reservationRepository.saveAndFlush(resEntity);

        System.out.println("Id de la réservation supprimée = " + resEntity.getId());
    }
}
