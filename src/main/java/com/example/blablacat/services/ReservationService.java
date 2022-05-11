package com.example.blablacat.services;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.css.Counter;

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
        userHasCourseDto.setUserName(reservationEntity.getUserEntity().getUsername());
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
    public  Integer save(ReservationDto dto) {
            ReservationEntity entity = new ReservationEntity();
            UserEntity userEntity = userRepository.findById(dto.getUser_id()).get();
            entity.setUserEntity(userEntity);
            CourseEntity courseEntity = courseRepository.findById(dto.getCourse_id()).get();
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
    public Integer numberPageMax() {
        List<ReservationEntity> list = reservationRepository.findAll();
        return list.size() / 3 ;
    }


    @Override
    public List<ReservationDto> getAllReservationsPage(Integer page, Integer size) {
        List<ReservationEntity> list = reservationRepository.findAll(PageRequest.of(page, size)).getContent();
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
}
